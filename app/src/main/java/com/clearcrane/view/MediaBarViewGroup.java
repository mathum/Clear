package com.clearcrane.view;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

import com.clearcrane.interfaces.AudioVolumeChangeListener;
import com.clearcrane.interfaces.UIChangeListener;

public class MediaBarViewGroup extends RelativeLayout {

	// 提供访问控制音量和钤声模式的操作。
	private AudioManager mAudioManager;
	private int mMaxVolume;
	private boolean isFirstShow = true;
	private AudioVolumeChangeListener mAudioVolumeListener;
	private UIChangeListener mUIChangeListener;

	public MediaBarViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 通过getSystemService(Context.AUDIO_SERVICE)方法获得AudioManager实例对象。
		this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		// 取得当前手机的音量，最大值为7，最小值为0，当为0时，手机自动将模式调整为“震动模式”。
		this.mMaxVolume = this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, this.mMaxVolume / 2,
				AudioManager.FLAG_PLAY_SOUND);
	}

	public final void setUIChangeListener(UIChangeListener _UIChangeListener) {
		this.mUIChangeListener = _UIChangeListener;
		if (!this.isFirstShow)
			_UIChangeListener.changeUI();
	}

	public final void setAudioVolumeListener(AudioVolumeChangeListener audioVolumeListener) {
		this.mAudioVolumeListener = audioVolumeListener;
	}

	public boolean dispatchKeyEvent(KeyEvent paramKeyEvent) {

		if (!VodMusicPlayView.mIsShowPlayList) {
			int keyCode = paramKeyEvent.getKeyCode();

			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {

				if (paramKeyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					// 向上取整
					int volume = (int) Math.ceil(
							this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 0.01D * this.mMaxVolume);
					if (volume > this.mMaxVolume) {
						volume = this.mMaxVolume;
					}

					this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
					// Toast.makeText(getContext(),
					// keyCode + "=" + "KEYCODE_DPAD_UP" + " volume=" + volume +
					// " maxVolume:" + this.mMaxVolume,
					// Toast.LENGTH_SHORT).show();
					if (this.mAudioVolumeListener != null)
						this.mAudioVolumeListener.updateVolume(volume, this.mMaxVolume);
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// 向下取整

				int _volume = (int) Math
						.floor(this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 0.01D * this.mMaxVolume);
				if (_volume < 0) {
					_volume = 0;
				}

				this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, _volume, AudioManager.FLAG_PLAY_SOUND);

				// Toast.makeText(getContext(),
				// keyCode + "=" + "KEYCODE_DPAD_DOWN" + " volume=" + _volume +
				// " maxVolume:" + this.mMaxVolume,
				// Toast.LENGTH_SHORT).show();
				if (this.mAudioVolumeListener != null)
					this.mAudioVolumeListener.updateVolume(_volume, this.mMaxVolume);
				return true;
			}
		}

		return false;
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			if ((this.isFirstShow) && (this.mUIChangeListener != null))
				this.mUIChangeListener.changeUI();
			this.isFirstShow = false;
		}
	}

}
