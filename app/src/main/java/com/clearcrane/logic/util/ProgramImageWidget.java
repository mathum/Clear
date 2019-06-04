package com.clearcrane.logic.util;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.clearcrane.constant.ClearConstant;
import com.clearcrane.databean.Mp3Info;
import com.clearcrane.service.PerfectPlayerService;
import com.clearcrane.util.ImageUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProgramImageWidget extends ProgramBaseWidget {
    public static final String CTL_ACTION = "com.zxb.action.CTL_ACTION"; // 控制动作
    private String curImageUrl;
    private ImageView mImageView;
    private ImageView nextView;
    private String nextImageUrl;
    private boolean isSinglePic = true; // 是否只有一张图片，若是只有1张就不用预加载了
    private int curIndex;
    private Handler handler = new Handler();
    // private List<ProgramResource> backgroundMusicList = null;
    private List<ProgramResource> showPictureList = null;
    private List<Mp3Info> musicList = null;
    private Runnable run = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            handler.removeCallbacks(run);

            if (isSinglePic)
                return;
            Log.e(TAG, "run run run");
            refreshCurIndex();
            showCurImage();
            preLoadNextView();
            Log.e("aaaa", "第" + curIndex + "次切换：" + showPictureList.get(curIndex).getDuration());
            handler.postDelayed(run, (showPictureList.get(curIndex).getDuration()) * 1000);
        }
    };
    // private Timer mTimer;

    @Override
    public void initView() {
        // TODO,FIXME
        mResourceList = new ArrayList<>();
        // backgroundMusicList = new ArrayList<>();
        showPictureList = new ArrayList<>();
        musicList = new ArrayList<>();
        // mViewPager = new ViewPager(mContext);
        mImageView = new ImageView(mContext);
        // mProgramLayout.addView(mViewPager);
        mProgramLayout.addView(mImageView);
        final LayoutParams layoutParams = (LayoutParams) mImageView.getLayoutParams();
        layoutParams.leftMargin = marginLeft;
        layoutParams.topMargin = marginTop;
        layoutParams.width = widgetWidth;
        layoutParams.height = widgetHeight;
        mImageView.setLayoutParams(layoutParams);
    }

    /*
     * TODO,FIXME just a imageview to cache do nothing call winter
     */
    private void preLoadNextView() {
        if (isSinglePic) {
            return;
        }
        if (nextView == null) {
            nextView = new ImageView(mContext);
        }
        initNextImageUrl();
        // MaterialRequest mr = new MaterialRequest(mContext, nextView,
        // ClearConfig.TYPE_IMAGE);
        // mr.execute(nextImageUrl);

        ImageUtil.displayImage(nextImageUrl, nextView);

    }

    private void initNextImageUrl() {
        if (curIndex + 1 == showPictureList.size()) {
            nextImageUrl = showPictureList.get(0).getUrl();
        } else {
            nextImageUrl = showPictureList.get(curIndex + 1).getUrl();
        }
    }

    private void showCurImage() {
        // MaterialRequest mr = new MaterialRequest(mContext, mImageView,
        // ClearConfig.TYPE_IMAGE);
        getCurImageUrl();
        // mr.execute(curImageUrl);
        // ImageLoader.getInstance().displayImage(curImageUrl, mImageView);
        ImageUtil.displayImage(curImageUrl, mImageView);
    }

    @Override
    public void play() {
        // TODO Auto-generated method stub
        // final ImageView imageView = new ImageView(mContext);
        Log.e("xbb", "programImageWidget" + mResourceList.size());
        if (mResourceList.size() == 0 || mResourceList == null) {
            Log.e(TAG, "error mResourceList is kong or null!");
            return;
        }
        for (ProgramResource pr : mResourceList) {
            Log.e("xbb", "type id : " + pr.getType_id());
            switch (pr.getType_id()) {
                // pdf资源
                case 3:
                    showPictureList.add(pr);
                    break;
                // 图片资源
                case 4:
                    showPictureList.add(pr);
                    break;
                // 背景音乐资源
                case 8:
                    Mp3Info mp3Info = new Mp3Info();
                    mp3Info.playURL = pr.getUrl();
                    musicList.add(mp3Info);
                    break;
            }
        }
        mImageView.setMaxWidth(widgetWidth);
        mImageView.setMaxHeight(widgetHeight);
        mImageView.setScaleType(ScaleType.FIT_XY);
        if (showPictureList.size() > 1) {
            isSinglePic = false;
            curIndex = 0;
        }
        Log.e(TAG, "play mResourceList " + showPictureList.size());

        // 初始化图片切换间隔时间（图文计划播是要求每个图文之间的时间可修改）
        if (showPictureList.size() > 0)
            duration = showPictureList.get(0).getDuration();

        startAutoPlay();
        startMusicPlay();
    }

    private void startMusicPlay() {
        if (musicList.size() == 0) {
            return;
        }
        playMusic(0, ClearConstant.PLAY_MSG);

        // 设置播放模式为全部循环
        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", 2);
        mContext.sendBroadcast(intent);

        // 设置播放内容
        Intent broadIntent = new Intent(CTL_ACTION);
        broadIntent.putExtra("musicList", (Serializable) musicList);
        broadIntent.putExtra("control", 5);
        mContext.sendBroadcast(broadIntent);

    }

    public void playMusic(int musicPosition, int message) {
        Intent intent = new Intent(mContext, PerfectPlayerService.class);
        Log.e("xb", musicList.get(musicPosition).getPlayURL());
        intent.putExtra("url", musicList.get(musicPosition).getPlayURL());
        intent.putExtra("listPosition", musicPosition);
        intent.putExtra("MSG", message);
        mContext.startService(intent);
    }

    // 开始图片轮播
    private void startAutoPlay() {
        if (showPictureList.size() == 0) {
            return;
        }
        getCurImageUrl();
        showCurImage();

        if (isSinglePic) {
            return;
        }
        preLoadNextView();
        handler.postDelayed(run, duration * 1000);
    }

    private void getCurImageUrl() {
        curImageUrl = showPictureList.get(curIndex).getUrl();
    }

    // TimerTask task = new TimerTask() {
    //
    // @Override
    // public void run() {
    // // TODO Auto-generated method stub
    // if(isSinglePic)
    // return;
    // Log.e(TAG,"run run run");
    // refreshCurIndex();
    // showCurImage();
    // preLoadNextView();
    // }
    // };

    private void refreshCurIndex() {
        curIndex += 1;
        if (curIndex >= showPictureList.size()) {
            curIndex = 0;
        }
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        // 停止图片轮播
        handler.removeCallbacks(run);

        // 停止音乐播放
        Intent broadIntent = new Intent(CTL_ACTION);
        broadIntent.putExtra("control", 0);
        mContext.sendBroadcast(broadIntent);
    }

    @Override
    public void addWorkResource(ProgramResource resource) {
        // TODO Auto-generated method stub
        mResourceList.add(resource);
    }
    // private void stopTask(){
    // if(mTimer != null){
    // mTimer.cancel();
    // mTimer = null;
    // }
    // }
    // private void startTask(){
    // mTimer = new Timer();
    // mTimer.schedule(task, mResourceList.get(curIndex).getDuration());
    // }

    @Override
    public int getTypeId() {
        // TODO Auto-generated method stub
        return 2;
    }
}
