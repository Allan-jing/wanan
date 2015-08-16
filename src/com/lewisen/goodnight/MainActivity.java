package com.lewisen.goodnight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.lewisen.goodnight.secondView.SecondActivity;
import com.umeng.onlineconfig.OnlineConfigAgent;

/**
 * APP����ͼƬ����
 * 
 * @author Lewisen
 * 
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = View.inflate(this, R.layout.activity_main, null);
		view.findViewById(R.id.first_text).setVisibility(View.VISIBLE);
		view.findViewById(R.id.wanan_image).setVisibility(View.VISIBLE);
		view.findViewById(R.id.wanan_text).setVisibility(View.VISIBLE);
		setContentView(view);

		// ����չʾ������,����ͨ�������������˿���Ӧ�ó���Ľ���
		AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		// ��������Ӽ�������
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				// �ж��Ƿ���ʾ���
				if (!MyApplication.appConfig.isDisplayAD()) {
					// ������ʾ�ڶ���ͼƬ
					redirectTo();
				} else {
					// ��ʾ�������
					redirectToAd();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});

		MaxID maxID = new MaxID();
		maxID.saveMaxIdToProper(this, null);

		onlineConfig();

	}

	private void onlineConfig() {
		// �������߲���
		OnlineConfigAgent.getInstance().updateOnlineConfig(this);
		String adSwitch = OnlineConfigAgent.getInstance().getConfigParams(this,
				"adSwitch");
		if ("false".equals(adSwitch)) {// ����ʾ���
			MyApplication.appConfig.setDisplayAD(false);
		} else if ("true".equals(adSwitch)) {// ��ʾ��� �´�
			MyApplication.appConfig.setDisplayAD(true);
		}
	}

	/**
	 * ��ת���ڶ�������ķ���
	 */
	private void redirectTo() {
		Intent intent = new Intent(this, SecondActivity.class);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);

		finish();
	}

	/**
	 * ��ת��������ķ���
	 */
	private void redirectToAd() {
		Intent intent = new Intent(this, CSplashActivity.class);
		startActivity(intent);
		finish();
	}

}
