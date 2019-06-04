/**
 * @author xujifu
 * @copyright clear
 * @data 2014-06-20
 * @description 本地应用列表View，列出所有的应用程序
 */
package com.clearcrane.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clearcrane.util.AppInfo;
import com.clearcrane.util.AppInfoProvider;
import com.clearcrane.util.AppListAdapter;
import com.clearcrane.vod.R;

import java.util.List;

public class VodApplicationView extends VoDBaseView {
	
	private RelativeLayout mApplicationTitle;
	private LinearLayout mApplicationTitleLeft;
	private ImageView mApplicationTitlePic;
	private TextView mApplicationTitleName;
	private TextView mApplicationName;
	private TextView mCurFocus;
	
	private ImageView mBaseLine;
	private ListView mApplicationList;
	private LinearLayout mApplicationCommand;
	private AppInfoProvider mAppInfoProvider;
	private List<AppInfo> mAppList;
	private AppListAdapter mAppListAdapter;
	
	public void init(Context ctx, String u){
		super.init(ctx, u);
		
		view = LayoutInflater.from(context).inflate(R.layout.application_view, null);
		
		initLayoutInXml();
	}

	private void initLayoutInXml() {
		// TODO Auto-generated method stub
		mApplicationTitle = (RelativeLayout)view.findViewById(R.id.application_title);
		mApplicationTitleLeft = (LinearLayout)view.findViewById(R.id.application_title_left);
		mApplicationTitlePic = (ImageView)view.findViewById(R.id.application_title_pic);
		mApplicationTitleName = (TextView)view.findViewById(R.id.application_title_name);
		mApplicationName = (TextView)view.findViewById(R.id.application_title_msgname);
		mCurFocus = (TextView)view.findViewById(R.id.application_curFocus);
		
		mBaseLine = (ImageView)view.findViewById(R.id.application_baseline);
		mApplicationList = (ListView)view.findViewById(R.id.application_context);
		mApplicationCommand = (LinearLayout)view.findViewById(R.id.application_command);
		
		mAppInfoProvider = new AppInfoProvider(context);
		mAppList = mAppInfoProvider.getAllApps();
		mAppListAdapter = new AppListAdapter(context, mAppList);
		mApplicationList.setAdapter(mAppListAdapter);
		
		mApplicationList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				PackageManager packageManager = context.getPackageManager();
			    Intent intent = packageManager.getLaunchIntentForPackage(mAppList.get(position).getPackageName()); 
			    context.startActivity(intent); 
			}
			
		});
	}
	
	public boolean onKeyBack(){
		return false;
	}
}
