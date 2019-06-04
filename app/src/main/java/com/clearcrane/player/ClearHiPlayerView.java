package com.clearcrane.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.hisilicon.android.mediaplayer.HiMediaPlayer;
import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnBufferingUpdateListener;
import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnCompletionListener;
import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnErrorListener;
import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnInfoListener;
import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnPreparedListener;
import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnSeekCompleteListener;
import com.hisilicon.android.mediaplayer.HiMediaPlayer.OnVideoSizeChangedListener;

import java.util.Calendar;
import java.util.List;

/**
 * 
 * TODO, FIXME 1. figure out the relationship about, onMeasure, layout, scaling
 * 2. add KeyEvent support
 * 
 */

public class ClearHiPlayerView extends SurfaceView implements
        MediaPlayerControl {

    /* states */
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_SUSPEND = 6;
    private static final int STATE_RESUME = 7;
    private static final int STATE_SUSPEND_UNSUPPORTED = 8;
    private static String TAG = "HiMediaPlayer";

    private HiMediaPlayer mMediaPlayer = null;
    private SurfaceHolder mSurfaceHolder = null;
    private int mSurfaceX = 0;
    private int mSurfaceY = 0;
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    private boolean isLooping = false;

    private Uri mUri;
    private long mDuration = -1;
    private int mVideoWidth;
    private int mVideoHeight;

    private int mSeekWhenPrepared = 0; // recording the seek position while
                                       // preparing
    private int mCurrentBufferPercentage;

    private Context mContext;

    /* event to notify caller */
    private OnCompletionListener mOnCompletionListener = null;
    private OnPreparedListener mOnPreparedListener = null;
    private OnErrorListener mOnErrorListener = null;
    private OnSeekCompleteListener mOnSeekCompleteListener = null;
    private OnInfoListener mOnInfoListener = null;
    private OnBufferingUpdateListener mOnBufferingUpdateListener = null;

    private MediaController mMediaController = null;
    
    private long lastVideoPrepareTimeStamp = -1;
    private long lastVideoLoadTimeCost = -1;
    private long lastVideoBuffingStart = -1;

    public ClearHiPlayerView(Context context) {
        super(context);
        initView(context);
    }

    public ClearHiPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initView(context);
    }

    public ClearHiPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    @SuppressWarnings("deprecation")
    private void initView(Context context) {
        mContext = context;
        getHolder().addCallback(mSHCallback);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        setZOrderMediaOverlay(false);
        setZOrderOnTop(false);
        
        if (context instanceof Activity) {
            ((Activity) context)
                    .setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }

    /* TODO FIXME make sure it won't make any trouble ... */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        Log.d("hemeiplayer onmeasure","with:"+width + "height"+ height);
        setMeasuredDimension(width, height);
        
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                int h) {
            Log.d(TAG,"video surface changed. w: " + w + " h: " + h);
            mSurfaceHolder = holder;
            // mSurfaceWidth = w;
            // mSurfaceHeight = h;
            // if (mMediaController != null) {
            // if (mMediaController.isShowing())
            // mMediaController.hide();
            // mMediaController.show();
            // }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG,"video surface created");
            mSurfaceHolder = holder;
            Log.d("hemeiplayer surfacecreat","");

            if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND
                    && mTargetState == STATE_RESUME) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
                resume();
            } else {
                openVideo();
            }
            
            setZOrderMediaOverlay(false);
            setZOrderOnTop(false);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG,"video surface destroyed");
            mSeekWhenPrepared = getCurrentPosition();
            mSurfaceHolder = null;
            if (mMediaController != null)
                mMediaController.hide();
            release(false);
            // stop();
        }
    };

    /**
     * Be careful to use this api. make sure you put video view in a
     * framelayout. Otherwise, do NOT call this api.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public void setDisplayArea(int x, int y, int width, int height) {
         Log.i(TAG,"SetDisplayArea: " + x + " " + y + " " + width + " " + height);
         Log.d("hemeiplayer set display","with:"+width + "height"+ height);
         mSurfaceX = x;
        mSurfaceY = y;
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        // if (mSurfaceHolder == null) {
        // return ;
        // }

        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();

        lp.leftMargin = x;
        lp.topMargin = y;
        lp.width = width;
        lp.height = height;

        post(new Runnable() {
            @Override
            public void run() {
                setLayoutParams(lp);
            }
        });
        
        

        if (mMediaPlayer != null){
        	Log.d("hemeiplayer setVideoRange","with:"+mSurfaceWidth + "height"+ mSurfaceHeight);
            mMediaPlayer.setVideoRange(mSurfaceX, mSurfaceY, mSurfaceWidth,
                    mSurfaceHeight);}
                    
    }

    /* events get from mediaplayer */
    OnPreparedListener mPreparedListener = new OnPreparedListener() {
        public void onPrepared(HiMediaPlayer mp) {
            Log.d(TAG,"onPrepared. video width: " + mp.getVideoWidth() + " height: "
                    + mp.getVideoHeight());
            mCurrentState = STATE_PREPARED;
            
            lastVideoLoadTimeCost = (Calendar.getInstance()).getTimeInMillis()
            		- lastVideoPrepareTimeStamp;

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mMediaPlayer);
            if (mMediaController != null)
                mMediaController.setEnabled(true);
            
            Parcel requestParcel = Parcel.obtain();
            requestParcel.writeInt(2);
            requestParcel.writeInt(0);
            Parcel replayParcel = Parcel.obtain();
            mp.invoke(requestParcel, replayParcel);
            
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            if (mSeekWhenPrepared != 0)
                seekTo(mSeekWhenPrepared);

            /* this flag change by caller invoke start() */
            if (mTargetState == STATE_PLAYING) {
                start();
                if (mMediaController != null)
                    mMediaController.show();
            }
            Log.d("hemeiplayer prepare1","with:"+mVideoWidth + "height"+ mVideoHeight);

            Log.d("hemeiplayer prepare","with:"+mSurfaceWidth + "height"+ mSurfaceHeight);
            if (mSurfaceWidth > 0 && mSurfaceHeight > 0) {
                setDisplayArea(mSurfaceX, mSurfaceY, mSurfaceWidth, mSurfaceHeight);
            }
        }
    };

    OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(HiMediaPlayer mp, int width, int height) {
            // TODO Auto-generated method stub
            Log.d(TAG,"onVideoSizeChanged:"+ width + "*" + height);
            Log.d("hemeiplayer changed","with:"+width + "height"+ height);

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            Log.d("hemeiplayer changed2","with:"+mVideoWidth + "height"+ mVideoWidth);

            // TODO, FIXME, check if we need to adopt any position related
            // setting
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(HiMediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG,"Error: %d, %d"+ framework_err+ impl_err);
           /* 
            ClearLog.LogError("PLAYER\tPlay\tERREVENT\t" + mUri.toString()
            		+ "\t" + framework_err 
            		+ "\t" + impl_err);*/

            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaController != null)
                mMediaController.hide();

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err,
                        impl_err))
                    return true;
            }
            /*
             * if (getWindowToken() != null) { int message = framework_err ==
             * MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ?
             * getResources() .getIdentifier(
             * "VideoView_error_text_invalid_progressive_playback", "string",
             * mContext.getPackageName()) : getResources().getIdentifier(
             * "VideoView_error_text_unknown", "string",
             * mContext.getPackageName());
             * 
             * new AlertDialog.Builder(mContext) .setTitle(
             * getResources().getIdentifier( "VideoView_error_title", "string",
             * mContext.getPackageName())) .setMessage(message)
             * .setPositiveButton( getResources().getIdentifier(
             * "VideoView_error_button", "string", mContext.getPackageName()),
             * new DialogInterface.OnClickListener() { public void
             * onClick(DialogInterface dialog, int whichButton) { if
             * (mOnCompletionListener != null) mOnCompletionListener
             * .onCompletion(mMediaPlayer); } }).setCancelable(false).show(); }
             */
            return true;
        }
    };

    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        public void onCompletion(HiMediaPlayer mp) {
            Log.i(TAG,"onCompletion");
            /*
             * on some devices, e.g huawei P6, mediaplayer.setLooping() doesn't
             * work...
             */
            if (isLooping == true) {
                openVideo();
                return;
            }

            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null)
                mMediaController.hide();
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    };

    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        public void onBufferingUpdate(HiMediaPlayer mp, int percent) {
            // Log.d(TAG,"buffering " + percent);
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null)
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    };
    private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(HiMediaPlayer mp) {
            Log.d(TAG,"onSeekComplete");
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(HiMediaPlayer mp, int what, int extra) {
            Log.d(TAG,"onInfo: (%d, %d)"+ what + extra);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (mMediaPlayer != null) {
                if (what == HiMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                	lastVideoBuffingStart = Calendar.getInstance().getTimeInMillis();
                	mMediaPlayer.pause();
                    // if (mMediaBufferingIndicator != null)
                    // mMediaBufferingIndicator.setVisibility(View.VISIBLE);
                } else if (what == HiMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                	/*ClearLog.LogInfo("PLAYER\tBuffering\t\t" 
                			+ (Calendar.getInstance().getTimeInMillis() - lastVideoBuffingStart) 
                			+ "ms\t" + mUri.toString());*/
                	mMediaPlayer.start();
                    // if (mMediaBufferingIndicator != null)
                    // mMediaBufferingIndicator.setVisibility(View.GONE);
                }
            }
            return true;
        }
    };

    /* main operations */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        mSeekWhenPrepared = 0;
        openVideo();
        // requestLayout();
        // invalidate();
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            Log.d(TAG,"openVideo failed due to empty uri or surface not created. uri: "
                    + mUri);
            return;
        }

        // end the player if any existed
        // release(false);
        stop();

        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;

            if (mMediaPlayer == null) {
                HiMediaPlayer hiMediaPlayer = null;
                if (mUri != null) {
                    hiMediaPlayer = new HiMediaPlayer();
                    // ((IjkMediaPlayer) ijkMediaPlayer)
                    // .setAvOption(AvFormatOption_HttpDetectRangeSupport.Disable);
                }
                mMediaPlayer = hiMediaPlayer;
                // mMediaPlayer.setLooping(isLooping);
                mMediaPlayer.setOnPreparedListener(mPreparedListener);
                mMediaPlayer
                        .setOnVideoSizeChangedListener(mSizeChangedListener);
                if (mCompletionListener != null) {				
                	mMediaPlayer.setOnCompletionListener(mCompletionListener);
				}
                mMediaPlayer.setOnErrorListener(mErrorListener);
                mMediaPlayer
                        .setOnBufferingUpdateListener(mBufferingUpdateListener);
                mMediaPlayer.setOnInfoListener(mInfoListener);
                mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
                // mMediaPlayer.setOnTimedTextListener(mTimedTextListener);
            }
            Log.e(TAG,"openVideo setDisplay");
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setDataSource(mUri.toString());

            // for ffmpeg player. not supported by native player
            // mMediaPlayer.setBufferSize(mBufSize);
            // mMediaPlayer
            // .setVideoChroma(mVideoChroma == MediaPlayer.VIDEOCHROMA_RGB565 ?
            // MediaPlayer.VIDEOCHROMA_RGB565
            // : MediaPlayer.VIDEOCHROMA_RGBA);
            lastVideoPrepareTimeStamp = Calendar.getInstance().getTimeInMillis();
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (Exception ex) {
            Log.e(TAG,"Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    HiMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
                mUri = null;
            }
        }
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public void setOnPreparedListener(OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }

    public boolean canPause() {
        return true;
    }

    public void resume() {
        if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
            mTargetState = STATE_RESUME;
        } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
            openVideo();
        } else {
            start();
        }
    }

    public boolean canSeekBackward() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean canSeekForward() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return mCurrentBufferPercentage;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState())
            return (int) mMediaPlayer.getCurrentPosition();
        return 0;
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0)
                return (int) mDuration;
            mDuration = mMediaPlayer.getDuration();
            return (int) mDuration;
        }
        mDuration = -1;
        return (int) mDuration;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setFreezeMode(0);
            mMediaPlayer.reset();
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setFreezeMode(1);
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mUri = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    /* TODO, FIXME, */
    public void setVideoScalingMode(int mode) {
        if (mMediaPlayer != null) {
            // mMediaPlayer.setVideoScalingMode(mode);
        }
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this
                    .getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());

            if (mUri != null) {
                List<String> paths = mUri.getPathSegments();
                String name = paths == null || paths.isEmpty() ? "null" : paths
                        .get(paths.size() - 1);
                // mMediaController.setFileName(name);
            }
        }
    }

    public void enableMediaController(boolean enable) {
        if (enable && mMediaController == null)
            mMediaController = new MediaController(mContext);

        attachMediaController();
    }

    public void seekTo(long pos) {
        int pos_int = (int) pos;
        seekTo(pos_int);
    }

    public void setLooping(boolean enable) {
        isLooping = enable;
        if (mMediaPlayer != null) {
             mMediaPlayer.setLooping(isLooping);
        }
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null)
            toggleMediaControlsVisiblity();
        return false;
    }

    public int getAudioSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public long getLastVideoLoadTimeCost() {
    	return lastVideoLoadTimeCost;
    }
}