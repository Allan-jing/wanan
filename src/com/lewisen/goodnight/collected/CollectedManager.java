package com.lewisen.goodnight.collected;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.lewisen.goodnight.OperateDB;

public class CollectedManager {
	public static final String HOME_C = "home_id";
	public static final String ART_C = "art_id";
	public static final String PIC_C = "pic_id";
	public static final int DEFAULT_COUNT = 20;
	private Context context;

	public CollectedManager(Context context) {
		this.context = context;
	}

	public int getIdFromProperties(String key) {

		SharedPreferences preferences = context.getSharedPreferences(
				"collected", Context.MODE_PRIVATE);
		return preferences.getInt(key, DEFAULT_COUNT);

	}

	public void saveIdToProperties(String key, int value) {
		SharedPreferences preferences = context.getSharedPreferences(
				"collected", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> queryDb(int count, OperateDB db, T t) {
		List<T> lists = new ArrayList<T>();
		if (count == 0) {
			return null;
		}
		for (int i = CollectedManager.DEFAULT_COUNT; i < CollectedManager.DEFAULT_COUNT
				+ count; i++) {
			t = (T) db.getFromDbUseId(i);
			if (t != null) {
				lists.add(t);
			}
		}
		return lists;
	}

	/**
	 * ɾ�������ղ�
	 * 
	 * @param id
	 *            ɾ�����������id
	 * @param key
	 *            ɾ�����ݵ�key,���еĳ���
	 * @param typeId
	 *            ����HomePage21
	 * @param db
	 *            ����
	 */
	public void deleteCollected(int id, String key, String typeId, OperateDB db) {
		db.deleteFromDb(id);
		saveIdToProperties(typeId, DEFAULT_COUNT);// �洢ΪĬ��ֵ����ɾ�����ղص���Ŀ
	}
}
