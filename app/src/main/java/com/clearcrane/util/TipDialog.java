/**
 * @author xujifu
 * @copyright clear
 * @date 2014-06-20
 * @description 消息弹出框
 */
package com.clearcrane.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.clearcrane.view.VoDViewManager;
import com.clearcrane.vod.R;

public class TipDialog extends Dialog {
	
	public TipDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TipDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	
	public static class Builder{
		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;
		private String TAG = "buildertiplog";
		
		private OnClickListener positiveButtonClickListener;
		private OnClickListener negativeButtonClickListener;


		public Builder(Context context){
			this.context = context;
		}

		public Builder setMessage(String message){
			this.message = message;
			return this;
		}

		public Builder setTitle(String title){
			this.title = title;
			return this;
		}

		/**
		 * set the Dialog message from resource
		 *
		 * @param message
		 * @return
		 */
		public Builder setMessage(int message){
			this.message = (String)context.getText(message);
			return this;
		}

		/**
		 * set the Dialog title from resource
		 *
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title){
			this.title = (String)context.getText(title);
			return this;
		}

		public Builder setContentView(View v){
			this.contentView = v;
			return this;
		}



		/**
		 * Set the positive button resource and it's listener
		 *
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText,
				OnClickListener listener){
			this.positiveButtonText = (String)context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(String positiveButtonText,
				OnClickListener listener){
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}


		public Builder setNegativeButton(String negativeButtonText,
				OnClickListener listener){
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}


		/**
		 * Set the negative button resource and it's listener
		 *
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText,
				OnClickListener listener){
			this.negativeButtonText = (String)context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		public TipDialog create(){
			LayoutInflater inflater = (LayoutInflater)context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//初始化dialog
			final TipDialog dialog = new TipDialog(context, com.clearcrane.vod.R.style.Dialog);
			View layout = inflater.inflate(R.layout.dialog_layout, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			//设置标题
			((TextView)layout.findViewById(R.id.dialog_title)).setText(title);
			//监听返回
			dialog.setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface arg0) {
					// TODO Auto-generated method stub
					Log.i(TAG, "cancel listener" );
					if(VoDViewManager.getInstance().isStarted < 3){
						VoDViewManager.getInstance().resetForegroundView();
					}
				}
				
			});
			//设置OKbutton
			if(positiveButtonText != null){
				((Button)layout.findViewById(R.id.positivebutton))
					.setText(positiveButtonText);
				if(positiveButtonClickListener != null){
					((Button)layout.findViewById(R.id.positivebutton))
						.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								positiveButtonClickListener.onClick(dialog, 
										DialogInterface.BUTTON_POSITIVE);
							}
						});
				}
				
			}else {
				((Button)layout.findViewById(R.id.positivebutton))
					.setVisibility(View.GONE);
			}
			if (negativeButtonText != null) {  
                ((Button) layout.findViewById(R.id.negativebutton))
                        .setText(negativeButtonText);  
                if (negativeButtonClickListener != null) {  
                    ((Button) layout.findViewById(R.id.negativebutton))
                            .setOnClickListener(new View.OnClickListener() {  
                                public void onClick(View v) {  
                                    negativeButtonClickListener.onClick(dialog,  
                                            DialogInterface.BUTTON_NEGATIVE);  
                                }  
                            });  
                }
               
            } else {  
                layout.findViewById(R.id.negativebutton).setVisibility(
                        View.GONE);  
            }
			if (message != null) {  
                ((TextView) layout.findViewById(R.id.dialog_message)).setText(message);
            } else if (contentView != null) {  
//                ((LinearLayout) layout.findViewById(R.id.dialog_message))  
//                        .removeAllViews();  
//                ((LinearLayout) layout.findViewById(R.id.dialog_message)).addView(  
//                        contentView, new LayoutParams(  
//                                LayoutParams.WRAP_CONTENT,  
//                                LayoutParams.WRAP_CONTENT));  
            }
			dialog.setContentView(layout);
			return dialog;
		}
		
		
	}


}
