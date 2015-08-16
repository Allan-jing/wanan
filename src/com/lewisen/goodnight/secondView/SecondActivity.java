package com.lewisen.goodnight.secondView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.lewisen.goodnight.R;
import com.lewisen.goodnight.mainview.MainView;

public class SecondActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.activity_main, null);
		SecondImage secondImage = new SecondImage(this);
		ImageView secondImageView = (ImageView) view
				.findViewById(R.id.first_view);

		if (secondImage.getState()) {
			// Log.d("DEBUG", "��ʾSDͼƬ");
			secondImage.displayImage(secondImageView);
		} else {
			// Log.d("DEBUG", "��ʾdrawableͼƬ");
			secondImageView.setImageResource(R.drawable.second);
		}

		secondImageView.setScaleType(ImageView.ScaleType.FIT_XY);
		setContentView(view);

		// ����չʾ������,����ͨ�������������˿���Ӧ�ó���Ľ���
		AlphaAnimation aa = new AlphaAnimation(0.8f, 1.0f);
		aa.setDuration(2500);
		view.startAnimation(aa);
		// ��������Ӽ�������
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {

				redirectTo();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});

		secondImage.newPathThread();

	}

	/**
	 * ��ת��������ķ���
	 */
	private void redirectTo() {
		Intent intent = new Intent(this, MainView.class);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		finish();
	}

}
