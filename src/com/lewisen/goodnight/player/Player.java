package com.lewisen.goodnight.player;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

public class Player implements OnBufferingUpdateListener, OnCompletionListener,
		OnPreparedListener {

	public MediaPlayer mediaPlayer; // ý�岥����
	private SeekBar seekBar; // �϶���
	private Timer mTimer = new Timer(); // ��ʱ��
	private MyTimerTask timerTask;
	private Handler handler;
	private TextView musicTimeText;// ����ʱ��
	private boolean startPlayState = false;// ����״̬ ��true�ǲ��ţ�falseΪ������

	// ��ʼ��������
	public Player() {
		super();
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// ����ý��������
			mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setLooping(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void initPlayer(SeekBar seekBar, TextView musicTimeText) {
		this.seekBar = seekBar;
		this.musicTimeText = musicTimeText;
		if (timerTask != null) {
			timerTask.cancel();
		}
		timerTask = new MyTimerTask();
		handler = new MyHandler();

	}

	// ��ʱ��
	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			if (mediaPlayer == null) {
				return;
			} else if (mediaPlayer.isPlaying() && seekBar.isPressed() == false) {
				handler.sendEmptyMessage(0); // ������Ϣ
			}
		}
	}

	class MyHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			if (mediaPlayer == null) {
				return;
			}
			int position = mediaPlayer.getCurrentPosition();
			int duration = mediaPlayer.getDuration();// ����
			if (duration > 0) {
				// ������ȣ���ȡ���������̶�*��ǰ���ֲ���λ�� / ��ǰ����ʱ����
				long pos = seekBar.getMax() * position / duration;
				seekBar.setProgress((int) pos);

				SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
				String ms = formatter.format(duration - position);
				musicTimeText.setText("-" + ms);
			}
		}
	}

	public void play() {
		try {
			mediaPlayer.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param url
	 *            url��ַ
	 */
	public void playUrl(String url) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(url); // ��������Դ
			mediaPlayer.prepare(); // prepare�Զ�����
			mediaPlayer.setLooping(true);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ��ͣ
	public void pause() {
		try {
			mediaPlayer.pause();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// ��ͣ
	public void start() {
		try {
			mediaPlayer.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	// ֹͣ
	public void stop() {
		if (mediaPlayer != null) {
			try {
				mediaPlayer.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			// mediaPlayer.release();
			// mediaPlayer = null;
		}
	}

	// �ͷ���Դ
	public void release() {
		if (mediaPlayer != null) {
			mTimer.cancel();
			try {
				mediaPlayer.stop();
				mediaPlayer.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			mediaPlayer = null;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (startPlayState) {// ��������ڼ�ı䲥��״̬����
			mp.start();
			// ÿһ�봥��һ��
			mTimer.schedule(timerTask, 0, 1000);
		}

		// Log.e("mediaPlayer", "onPrepared");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// Log.e("mediaPlayer", "onCompletion");
	}

	/**
	 * �������
	 */
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		seekBar.setSecondaryProgress(percent);
		// int currentProgress = seekBar.getMax()
		//		* mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
		// Log.e(currentProgress + "% play", percent + " buffer");
	}

	public boolean isStartPlayState() {
		return startPlayState;
	}

	public void setStartPlayState(boolean startPlayState) {
		this.startPlayState = startPlayState;
	}

}
