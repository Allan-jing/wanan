package com.lewisen.goodnight;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

/**
 * ��ȡ������json����
 * 
 * @author Lewisen
 */
public class GetJsonFromNet {

	/**
	 * @param requestName�����Servlet������
	 * @param requestID�����ҳ��ID��
	 * @param sort�����ҳ������
	 *            1:HomePage/ArticlePage/PicturePage; 2:MaxId; 3:like����,�Լ���������
	 * @return
	 */
	public static JSONObject getJsonObj(String requestName, int requestID,
			int sort) {
		// ����HttpClient����
		HttpClient httpClient = new DefaultHttpClient();
		// �������ӳ�ʱʱ������λ����
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
		// ���ö�ȡ��ʱ,��λ����
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				30000);
		// ������������Ķ���,�����Ƿ��ʷ�������ַ
		String url = null;
		if (sort == 1) {
			url = MyServer.URL + requestName + "?id=" + requestID;
		} else if (sort == 2) {
			url = MyServer.URL + requestName + "?order=get";
		} else if (sort == 3) {
			url = MyServer.URL + requestName;
		}
		// Log.d("DEBUG", "url = " + url);
		HttpGet httpGet = new HttpGet(url);

		// ִ�����󣬻�ȡ�����������Ķ���
		try {
			HttpResponse response = httpClient.execute(httpGet);
			// �����Ӧ��״̬�Ƿ�������������ص�������200�������������404���ǿͻ��˴��������505���Ƿ���������
			int result = response.getStatusLine().getStatusCode();
			if (200 == result) {
				// ����Ӧ������ȡ������
				HttpEntity entity = response.getEntity();
				InputStream in = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				JSONTokener jsonTokener = new JSONTokener(reader.readLine());
				
				in.close();
				return (JSONObject) jsonTokener.nextValue();
			}
		} catch (Exception e) {
			// System.out.println("GetJsonFromNet ���ӷ���������");
			e.printStackTrace();
		}
		return null;

	}
}
