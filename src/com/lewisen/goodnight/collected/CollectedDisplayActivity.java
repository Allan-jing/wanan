package com.lewisen.goodnight.collected;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lewisen.goodnight.DisplayImage;
import com.lewisen.goodnight.MyApplication;
import com.lewisen.goodnight.R;
import com.lewisen.goodnight.cache.SaveImage;
import com.lewisen.goodnight.mainview.MyPopupWindow;
import com.lewisen.goodnight.mainview.Share;
import com.lewisen.goodnight.mainview.ShareInterface;
import com.lewisen.goodnight.player.PlayerControl;
import com.umeng.socialize.controller.UMServiceFactory;

public class CollectedDisplayActivity extends Activity implements
		ShareInterface {
	private ImageButton backButton;
	private ImageButton menuButton;
	private DisplayImage displayImage;
	private PlayerControl playerControl;
	private Collected collected;
	private MyPopupWindow myPopupWindow;
	final com.umeng.socialize.controller.UMSocialService umSocialService = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MyApplication.appConfig.getNightModeSwitch()) {
			this.setTheme(R.style.NightTheme);
		} else {
			this.setTheme(R.style.DayTheme);
		}

		displayImage = new DisplayImage();
		myPopupWindow = new MyPopupWindow(this, umSocialService, false);

		// ��ͼ��ʼ��
		viewInit();

		collected = (Collected) getIntent().getSerializableExtra("collected");
		// ��ʾ�ղ�����
		collectedDisplay(collected);

	}

	private void collectedDisplay(Collected collected) {
		String type = collected.getType();
		TextView date = (TextView) findViewById(R.id.date_home);
		TextView title = (TextView) findViewById(R.id.title_home);
		TextView author = (TextView) findViewById(R.id.author_home);

		TextView authorIntro = (TextView) findViewById(R.id.author_intro_home);
		View topLine = (View) findViewById(R.id.line_home);
		View bottomLine = (View) findViewById(R.id.line_bottom_home);
		ProgressBar loading = (ProgressBar) findViewById(R.id.loading_bar_home);
		loading.setVisibility(View.GONE);
		topLine.setVisibility(View.VISIBLE);
		bottomLine.setVisibility(View.VISIBLE);

		date.setText(collected.getDate());
		authorIntro.setText(collected.getAuthorIntro());
		if (("HomePage".equals(type)) || ("ArticlePage".equals(type))) {
			title.setText(collected.getTitle());
			author.setText(collected.getAuthor());
		}

		// ��ʾͼ�ĵ����Բ���
		LinearLayout imageTextLayout = (LinearLayout) findViewById(R.id.layout_image_text_home);
		LayoutInflater inflater = getLayoutInflater();
		RelativeLayout imageText = (RelativeLayout) inflater.inflate(
				R.layout.image_text, imageTextLayout, false);

		if ("ArticlePage".equals(type)) {
			TextView textView = (TextView) imageText
					.findViewById(R.id.text_image_text);
			textView.setVisibility(View.VISIBLE);
			textView.setText(collected.getText());
			// ������ͼ
			imageTextLayout.addView(imageText);
		} else if ("PicturePage".equals(type)) {
			title.setVisibility(View.GONE);
			author.setVisibility(View.GONE);
			TextView textView = (TextView) imageText
					.findViewById(R.id.text_image_text);
			textView.setVisibility(View.VISIBLE);
			textView.setText(collected.getText());
			ImageView imageView = (ImageView) imageText
					.findViewById(R.id.picture_image_text);
			imageView.setVisibility(View.VISIBLE);
			displayImage.displayImage(imageView, collected.getImageSrc());
			// ������ͼ
			imageTextLayout.addView(imageText);
		} else if ("HomePage".equals(type)) {
			String[] imageSrcPart = collected.getImageSrc().split("###");
			String[] textPart = collected.getText().split("###");
			int textLength = textPart.length;// �ı��ָ�Ϊ�������֣���1��ʼ
			// int imageLength = imageSrcPart.length;

			for (int i = 0; i < textLength; i++) {
				// ����һ������
				imageText = (RelativeLayout) inflater.inflate(
						R.layout.image_text, imageTextLayout, false);
				ImageView imageView = (ImageView) imageText
						.findViewById(R.id.picture_image_text);
				imageView.setVisibility(View.VISIBLE);
				TextView textView = (TextView) imageText
						.findViewById(R.id.text_image_text);
				textView.setVisibility(View.VISIBLE);
				final String imageSrc = imageSrcPart[i];
				//��ͼƬ�����ж�
				if ((imageSrc != null) && (!imageSrc.isEmpty())) {
					imageView.setVisibility(View.VISIBLE);
					displayImage.displayImage(imageView, imageSrc);
					imageView.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View v) {
							SaveImage saveImage = new SaveImage();
							String path = saveImage.saveImage(displayImage
									.getImageCachePath(imageSrc));
							if (path != null) {
								Toast.makeText(CollectedDisplayActivity.this,
										"ͼƬ�ѱ��浽" + path, Toast.LENGTH_LONG)
										.show();
							}
							return true;
						}
					});
				}
				// �����@@@ ����������Ҫ������ ������ͼ����ʾ��ǰ��
				if (textPart[i].contains("@@@")) {
					String musicTitle = collected.getMusicTitle();
					String musicAuthor = collected.getMusicAuthor();
					String musicURL = collected.getMusicSrc();
					String musicImage = collected.getMusicImage();
					if ((musicTitle == null) || (musicAuthor == null)
							|| (musicURL == null)) {
						return;
					}
					RelativeLayout musicView = (RelativeLayout) inflater
							.inflate(R.layout.music_view, imageTextLayout,
									false);
					SeekBar seekBar = (SeekBar) musicView
							.findViewById(R.id.music_progress);
					TextView musicTimeText = (TextView) musicView
							.findViewById(R.id.music_time);
					ImageButton playButton = (ImageButton) musicView
							.findViewById(R.id.music_button);
					TextView musicTitleView = (TextView) musicView
							.findViewById(R.id.music_title);
					TextView musicAuthorView = (TextView) musicView
							.findViewById(R.id.music_author);
					if (musicImage != null) {
						ImageView musicIcon = (ImageView) musicView
								.findViewById(R.id.music_icon);
						displayImage.displayImage(musicIcon, musicImage);
					}
					musicTitleView.setText(musicTitle);
					musicAuthorView.setText(musicAuthor);
					// �������ֳ�ʼ��
					playerControl = new PlayerControl();
					playerControl.playerControlInit(seekBar, musicTimeText,
							playButton, musicURL, this);
					// ������ֲ��Ž��浽����ͼ
					imageTextLayout.addView(musicView);

					textView.setText(textPart[i].replace("@@@", ""));
				} else {
					textView.setText(textPart[i]);
				}

				imageTextLayout.addView(imageText);
			}
		}

	}

	private void viewInit() {
		// �Զ������
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.collected_display);
		// ���ñ���Ϊĳ��layout
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titlebar);
		// ���ؼ�
		backButton = (ImageButton) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		backButton.setVisibility(View.VISIBLE);
		menuButton = (ImageButton) findViewById(R.id.menu);
		menuButton.setVisibility(View.VISIBLE);
		menuButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myPopupWindow.showMenuWindow(menuButton,
						CollectedDisplayActivity.this);
			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (playerControl != null) {
			playerControl.stopMusic();
		}
	}

	@Override
	public Share shareContent(boolean isCollected) {
		// TODO Auto-generated method stub
		Share share = new Share();
		share.setName(collected.getType() + collected.getId());
		share.setTitle(collected.getTitle());
		int len = collected.getText().length();
		if (len > 50) {
			len = 50;
		}
		share.setContent(collected.getText().substring(0, len));// �����ȡǰ50��

		return share;
	}

}
