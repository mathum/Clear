package com.clearcrane.logic.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.clearcrane.logic.util.ProgramBaseWidget;
import com.clearcrane.logic.util.ProgramLayoutParam;
import com.clearcrane.logic.util.ProgramResource;
import com.clearcrane.logic.util.ProgramWidgetFactory;
import com.clearcrane.view.VoDBaseView;
import com.clearcrane.vod.R;

import java.util.ArrayList;
import java.util.List;

public class ProgramView extends VoDBaseView {

    private final String TAG = "ProgramView";

    private TextView tvInfo;
    private ImageView ivWait;
    private FrameLayout flBase;

    private String name;
    private String backgroundUrl;
    private ArrayList<ProgramBaseWidget> regionList;

    private boolean isWidgetsOk; //to ensure init success!
    public String startTime;
    public String endTime;

    @Override
    public void init(Context ctx, String u) {
        // TODO Auto-generated method stub
        super.init(ctx, u);
        regionList = new ArrayList<>();
        view = LayoutInflater.from(context).inflate(R.layout.program_view, null);
        initLayoutInXml();

    }


    private void initLayoutInXml() {
        tvInfo = (TextView) view.findViewById(R.id.tv_info);
        tvInfo.setText("计划播放中");
        tvInfo.setVisibility(View.VISIBLE);
        ivWait = (ImageView) view.findViewById(R.id.iv_waitImage);
        flBase = (FrameLayout) view.findViewById(R.id.fl_program_view);
    }

    public void initWidgets(List<ProgramLayoutParam> list) {
        isWidgetsOk = false;
        if (list == null || list.size() == 0)
            return;
        for (int i = 0; i < list.size(); i++) {
            ProgramLayoutParam plp = list.get(i);
            Log.e("xbb", "typeId:" + plp.typeId);
            ProgramBaseWidget pbw = ProgramWidgetFactory.createWidget(plp);
            if (pbw == null) {
                continue;
            }
            pbw.init(context, plp, flBase, startTime, endTime);
            regionList.add(pbw);
        }
        if (regionList.size() != 0)
            isWidgetsOk = true;
    }

    public void setLifeTime(String start, String end) {
        startTime = start;
        endTime = end;
    }

    /**
     * TODO,FIXME,
     * sort the resource?
     *
     * @param resourceList
     */
    public void setWidgetResource(List<ProgramResource> resourceList) {
        Log.e(TAG, "setWidgetResource list = " + resourceList.size());
        for (ProgramResource resource : resourceList) {
            int rid = resource.getLayoutParamId();
            for (int i = 0; i < regionList.size(); i++) {
                ProgramBaseWidget pbw = regionList.get(i);
                if (pbw.getmRegionId() == rid) {
                    Log.e(TAG, "setWidgetResource resourcr " + resource.getUrl());
                    pbw.addWorkResource(resource);
                }
            }
        }
    }


    public void play() {
        //safe play
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (ProgramBaseWidget pbw : regionList) {
            Log.e("111111111111111", "!1111111111111111");
            pbw.play();
        }
    }

    public void stop() {
        for (ProgramBaseWidget pbw : regionList) {
            pbw.stop();
        }
    }

    //由于当前暂时是一个计划列表只有一种布局，所以可以直接获取布局列表中得第一个
    public int getProgramViewId() {
        return regionList.get(0).getTypeId();
    }
}
