package com.clearcrane.logic.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.provider.MaterialRequest;
import com.clearcrane.util.ClearConfig;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R;

public class TermForcedView extends VoDBaseView {
    private final String TAG = "TermForcedView";
    private String typeName = "";
    private ImageView ivFull;
    private boolean isForced;
    private int termType = 0; //0：图片   1：视频


    public void init(Context ctx, String u, String type) {
        super.init(ctx, u);
        this.typeName = type == null ? "" : type;
        view = LayoutInflater.from(ctx).inflate(R.layout.fullscreen_image, null);
        initLayoutFromXml();
        ivFull.setVisibility(View.VISIBLE);
        startplay();
    }

    public void init(Context ctx, String u, String type, boolean isForced) {
        super.init(ctx, u);
        this.isForced = isForced;
        this.typeName = type == null ? "" : type;
        view = LayoutInflater.from(ctx).inflate(R.layout.fullscreen_image, null);
        initLayoutFromXml();
        if (isForced) {
            ivFull.setVisibility(View.GONE);
            return;
        }
        startplay();
    }

    public void showImageView() {
        ivFull.setVisibility(View.VISIBLE);
    }

    public void hideImageView() {
        ivFull.setVisibility(View.GONE);
    }

    private void initLayoutFromXml() {
        ivFull = (ImageView) view.findViewById(R.id.full_image_pic);
    }

    public void startplay() {
        if (typeName.equals("1")) {
            playVideo();
            termType = 1;
        } else if (typeName.equals("4")) {
            showPicture();
            termType = 0;
        }
    }

    public void stopplay() {
        if (typeName.equals("1")) {
            stopVideo();
        } else if (typeName.equals("4")) {
            ivFull.setImageBitmap(null);
        }
    }


    private void showPicture() {
        MaterialRequest mr = new MaterialRequest(this.context, ivFull, ClearConfig.TYPE_IMAGE);
        mr.execute(this.url);

    }


    private void stopVideo() {
        VoDViewManager.getInstance().hideLiveVideo();
    }


    private void playVideo() {
        Log.e(TAG, "startPlay TermForcedView " + url);
        VoDViewManager.getInstance().showMovieVideo();
        VoDViewManager.getInstance().startMovieVideo(url);
        VoDViewManager.getInstance().setMovieViewCompleteListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                // TODO Auto-generated method stub
                //由于使用setlooping以及seekto()方法也无效，所以就重新设置播放器url实现循环。
                arg0.stop();
                arg0.reset();
                VoDViewManager.getInstance().startMovieVideo(url);
            }
        });
    }

    public int getTermForcedType() {
        return termType;
    }

    public String getUrl() {
        return url;
    }

}
