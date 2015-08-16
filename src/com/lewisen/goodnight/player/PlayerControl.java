package com.lewisen.goodnight.player;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lewisen.goodnight.MyApplication;
import com.lewisen.goodnight.R;

/**
 * �����������ֿ���
 * 
 * @author Lewisen
 * 
 */
public class PlayerControl {
	private Player player = null;
	private String lastUrl = null;

	public PlayerControl() {
		player = new Player();
	}

	public void playerControlInit(final SeekBar seekBar,
			final TextView musicTimeText, final ImageButton playButton,
			final String url, final Context context) {

		playButton.setOnClickListener(new OnClickListener() {
			boolean state = false;// ��ǰ״̬��falseΪֹͣ trueΪ����

			@Override
			public void onClick(View v) {
				if (!state) {// ����

					if (player.isStartPlayState()) {
						Toast.makeText(context, "����ֹͣ��ǰ���ڲ��ŵ�����",
								Toast.LENGTH_SHORT).show();
						return;
					}

					// �ڲ�������ǰ�ж��Ƿ�Ϊwifi���ӣ�������ʾ�û��Ƿ񲥷�
					ConnectivityManager manager = (ConnectivityManager) context
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo info = manager.getActiveNetworkInfo();
					if (info == null) {
						Toast.makeText(context, "û������", Toast.LENGTH_SHORT)
								.show();
						return;
					} else if (info.getType() != ConnectivityManager.TYPE_WIFI) {
						new AlertDialog.Builder(context)
								.setMessage("����û������WIFI,ȷ��Ҫ������������?")
								.setPositiveButton("ȷ��",
										new DialogInterface.OnClickListener() {// ���ȷ����ť
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {// ȷ����ť����Ӧ�¼�
												state = true;
												startPlay(seekBar,
														musicTimeText,
														playButton, url,
														context);
											}
										})
								.setNegativeButton("ȡ��",
										new DialogInterface.OnClickListener() {// ��ӷ��ذ�ť
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {// ��Ӧ�¼�
												return;
											}
										}).show();
					} else {// WIFI����״̬
						state = true;
						startPlay(seekBar, musicTimeText, playButton, url,
								context);
					}
				} else {// ֹͣ
					state = false;
					Toast.makeText(context, "ֹͣ����", Toast.LENGTH_SHORT).show();
					if (MyApplication.appConfig.getNightModeSwitch()) {
						playButton
								.setImageResource(R.drawable.music_play_night);
					} else {
						playButton.setImageResource(R.drawable.music_play);
					}

					pauseMusic();
				}
			}
		});
	}

	/**
	 * ��ʼ��������
	 * 
	 * @param url
	 */
	public void playMusic(final String url) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				player.setStartPlayState(true);
				player.playUrl(url);
			}
		}).start();

	}

	public void stopMusic() {
		if (player != null) {
			player.setStartPlayState(false);
			player.stop();
			// player = null;
		}
	}

	public void pauseMusic() {
		if (player != null) {
			player.setStartPlayState(false);
			// Log.d("DEBUG", "startPlayState player != null");
			player.pause();
		}
	}

	public void startMusic() {
		if (player != null) {
			player.setStartPlayState(true);
			player.start();
		}
	}

	public void release() {
		if (player != null) {
			player.setStartPlayState(false);
			player.release();
			player = null;
		}
	}

	private synchronized void startPlay(final SeekBar seekBar,
			final TextView musicTimeText, final ImageButton playButton,
			final String url, final Context context) {
		if (MyApplication.appConfig.getNightModeSwitch()) {
			playButton.setImageResource(R.drawable.music_stop_night);
		} else {
			playButton.setImageResource(R.drawable.music_stop);
		}
		if ((seekBar.getSecondaryProgress() == 0) || (!url.equals(lastUrl))) {
			Toast.makeText(context, "�������ڼ���", Toast.LENGTH_SHORT).show();
			// ��ʼ����ǰ���ŵĽ�����
			player.initPlayer(seekBar, musicTimeText);
			seekBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
			playMusic(url);
			lastUrl = url;
		} else {
			startMusic();
		}
	}

	class SeekBarChangeEvent implements OnSeekBarChangeListener {
		int progress;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// ԭ����(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
			this.progress = progress * player.mediaPlayer.getDuration()
					/ seekBar.getMax();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// seekTo()�Ĳ����������ӰƬʱ������֣���������seekBar.getMax()��Ե�����
			player.mediaPlayer.seekTo(progress);
		}

	}

}
