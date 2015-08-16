package com.lewisen.goodnight.cache;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * ����Ӧ����Ϣ
 * 
 * @author Administrator
 */
public class AppConfig {

	private SharedPreferences innerConfig;

	private static final String KEY_NIGHT_MODE_SWITCH = "night_mode_switch";
	private static final String SW = "ad_switch";

	public AppConfig(final Context context) {
		innerConfig = context.getSharedPreferences("app_config",
				Application.MODE_PRIVATE);
	}

	// ҹ��ģʽ
	public boolean getNightModeSwitch() {
		return innerConfig.getBoolean(KEY_NIGHT_MODE_SWITCH, false);
	}

	public void setNightModeSwitch(boolean on) {
		Editor editor = innerConfig.edit();
		editor.putBoolean(KEY_NIGHT_MODE_SWITCH, on);
		editor.commit();
	}

	public boolean isDisplayAD() {
		return innerConfig.getBoolean(SW, false);
	}

	public void setDisplayAD(boolean on) {
		Editor editor = innerConfig.edit();
		editor.putBoolean(SW, on);
		editor.commit();
	}

	/**
	 * ���
	 */
	public void clear() {
		Editor editor = innerConfig.edit();
		editor.clear();
		editor.commit();
	}
}