package com.lewisen.goodnight.secondView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.lewisen.goodnight.GetJsonFromNet;
import com.lewisen.goodnight.MyServer;

public class SecondImage {
	private Context context;

	public SecondImage(Context context) {
		this.context = context;
	}

	public void newPathThread() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				String path = null;

				// ��ȡ����ID�� MaxIdManageServlet?order=get
				JSONObject jsonObject = GetJsonFromNet.getJsonObj(
						MyServer.SECOND_IMAGE + "?order=get", 0, 3);

				if (jsonObject != null) {
					try {
						path = jsonObject.getString("path");
						// Log.d("DEBUG", "path=" + path);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (!"null".equals(path)) {
						if (path.equals(getPath()) && (getState())) {
							// Log.d("DEBUG", "����...");
							return;
						} else {
							saveInfo("state", false);

							try {
								path = URLEncoder.encode(path, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							String url = MyServer.PICTURE_URL + path;
							String filesDir = context.getExternalFilesDir(null)
									.getPath();
							downloadImage(url, filesDir);

							saveInfo("lastPath", path);
						}
					}

				}
			}
		}).start();
	}

	/**
	 * ������Ϣ
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public void saveInfo(String key, boolean value) {
		SharedPreferences preferences = context.getSharedPreferences("second",
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void saveInfo(String key, String value) {
		SharedPreferences preferences = context.getSharedPreferences("second",
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * ��ȡ״̬
	 * 
	 * @param context
	 * @return
	 */
	public boolean getState() {
		SharedPreferences preferences = context.getSharedPreferences("second",
				Context.MODE_PRIVATE);
		return preferences.getBoolean("state", false);
	}

	/**
	 * ��ȡ�Ѿ����ص�ͼƬ·��
	 * 
	 * @param context
	 * @return
	 */
	public String getPath() {
		SharedPreferences preferences = context.getSharedPreferences("second",
				Context.MODE_PRIVATE);
		return preferences.getString("lastPath", null);
	}

	/**
	 * ��ȡ�����ͼƬ·��
	 * 
	 * @return
	 */
	public String getImageDir() {
		SharedPreferences preferences = context.getSharedPreferences("second",
				Context.MODE_PRIVATE);
		return preferences.getString("imageDir", null);
	}

	/**
	 * ��������ͼƬ��sd�����洢·��ΪimageDir
	 * 
	 * @param url
	 * @param filesDir
	 */
	private void downloadImage(String url, String filesDir) {
		// ����HttpClient����
		HttpClient httpClient = new DefaultHttpClient();
		// �������ӳ�ʱʱ������λ����
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
		// ���ö�ȡ��ʱ,��λ����
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				30000);
		// Log.d("DEBUG", "url=" + url);
		HttpGet httpGet = new HttpGet(url);

		// ִ�����󣬻�ȡ�����������Ķ���
		try {
			HttpResponse response = httpClient.execute(httpGet);
			// �����Ӧ��״̬�Ƿ�������������ص�������200�������������404���ǿͻ��˴��������505���Ƿ���������
			int result = response.getStatusLine().getStatusCode();
			if (200 == result) {
				// ����Ӧ������ȡ������
				HttpEntity entity = response.getEntity();
				byte[] data = EntityUtils.toByteArray(entity);
				String[] imageFiles = url.split("%5C");
				String imageDir = filesDir + File.separator
						+ imageFiles[imageFiles.length - 1];
				FileOutputStream out = new FileOutputStream(new File(imageDir));
				// Log.d("DEBUG", "imageDir=" + imageDir);

				saveInfo("imageDir", imageDir);
				out.write(data);
				out.close();

				saveInfo("state", true);// �������سɹ�״̬
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʾ�Ѿ����ص�ͼƬ
	 * 
	 * @param imageView
	 * @return ��ʾ����Ƿ�ɹ�
	 */
	public boolean displayImage(ImageView imageView) {
		String imageDir = getImageDir();

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;// �Ż��ڴ�
		Bitmap bitmap = BitmapFactory.decodeFile(imageDir, options);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			return true;
		} else {
			return false;
		}
	}

}
