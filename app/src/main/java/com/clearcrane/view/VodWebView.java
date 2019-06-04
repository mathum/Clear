package com.clearcrane.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.clearcrane.vod.R;

/**
 * 加载网页
 * 
 * @author SlientLeaves 2016年5月26日 下午3:25:11
 */
public class VodWebView extends VoDBaseView {

	private WebView mWebView = null;
	private ProgressDialog mProgressDialog = null;

	public void init(Context context, String url) {
		this.context = context;
		this.url = url;
		view = LayoutInflater.from(context).inflate(R.layout.web_view, null);
		initView();
		doOperation();
	}

	private void initView() {
		mWebView = (WebView) view.findViewById(R.id.wv_contents);

		// 启用支持javascript
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDefaultTextEncodingName("UTF -8");
		settings.setJavaScriptCanOpenWindowsAutomatically(true);  
		settings.setUseWideViewPort(true);//关键点  
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);  
		settings.setDisplayZoomControls(false);  
		settings.setAllowFileAccess(true); // 允许访问文件  
		settings.setBuiltInZoomControls(true); // 设置显示缩放按钮 
		settings.setSupportZoom(true); // 支持缩放 
		

		// 优先使用缓存
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

	}

	private void doOperation() {
		load(url);
		
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				mProgressDialog.dismiss();
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				
				mWebView.loadUrl("file:///android_asset/error.html");
			}
		});
		
		
	}

	public void load(String url) {

		if (mWebView != null) {
			mWebView.loadUrl(url);
			mProgressDialog = ProgressDialog.show(context, null, "页面加载中，请稍后..");
			mWebView.reload();
		}
	}

}
