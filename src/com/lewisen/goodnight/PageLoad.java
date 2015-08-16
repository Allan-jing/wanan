package com.lewisen.goodnight;

import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

public class PageLoad {
	public static final int GET_RESOURCE_ERR = -1;
	int lastPage = 0;

	/**
	 * ҳ����¿���
	 * 
	 * @param currentMaxId
	 *            ��ǰ���ID
	 * @param page
	 *            ��ǰ��ʾ��ҳ��
	 * @param netState
	 *            ����״̬��־
	 * @param mHandler
	 *            ��Ϣ����
	 * @param stopThread
	 *            �߳�ֹͣ��־
	 * @param operateDB
	 *            ���ݿ��������
	 * @param requestName
	 *            Servlet�����������
	 * @param loadedPage
	 *            �Ѿ����ص���ҳ��
	 */
	public void pageUpdateControl(int currentMaxId, int page, boolean netState,
			Handler mHandler, boolean stopThread, OperateDB operateDB,
			String requestName, int loadedPage) {
		int pageID = currentMaxId + 1 - page;
		// ��ʾ���ݵ�ҳ�����Ǵ�1��10
		if ((page != 0) && (page < 11)) {
			// Log.d("DEBUG", "PageID" + pageID + " lastPage:" + lastPage
			// + " page:" + page + " loadedPage:" + loadedPage
			// + " currentMaxId" + currentMaxId);

			// ������������ʱ�������£���ǰҳ��û�м��أ���ҳ����������ҳ��������ã�
			if ((page > loadedPage)
					&& ((pageID == (lastPage - 1)) || (pageID == currentMaxId))
					&& netState) {
				// Log.d("DEBUG", "��������");
				this.loadFromNet(mHandler, page, requestName, pageID,
						stopThread, operateDB);
			} else {// ����ֱ�Ӽ������ݿ����� if ((pageID >= lastPage) || (!netState))
			// Log.d("DEBUG", "�������ݿ�");
				this.loadFromDB(mHandler, page, pageID, stopThread, operateDB);
			}
			lastPage = pageID;// ���汾��ҳ��id
		}
	}

	/**
	 * �������������,���ұ������ݵ����ݿ�
	 * 
	 * @param mHandler
	 *            ��Ϣ����handler
	 * @param page
	 *            ��ʾҳ���id ��1--10 ��Ӧʮ������
	 * @param requestName
	 *            �����servlet������
	 * @param requestID
	 *            ����servlet�Ķ�Ӧ�����ݵı��
	 * @param stopThread
	 *            ֹͣ����handler��ʶ
	 * @param saveToDbImpl
	 *            ����Ϊ�գ�����ø����е�saveToDb����
	 */
	public void loadFromNet(final Handler mHandler, final int page,
			final String requestName, final int requestID,
			final boolean stopThread, final OperateDB operateDB) {

		new Thread(new Runnable() {
			@Override
			public void run() {

				// System.out.println("��ʼ����������Դ");
				JSONObject jsonObject = GetJsonFromNet.getJsonObj(requestName,
						requestID, 1);

				// �����ǰ���̼߳��صĽ��治����ʾ�������ٷ���message���������handler����messageʱ��ָ���쳣
				// Ŀǰ���������:������һ����ǰ����ı�־λ��stopThread��ÿ�η���messageǰ�жϣ�����ÿ�δ���handlerǰ�жϡ�
				// ��δ�뵽�����õķ�����������Բ�������������һ��handler
				if (!stopThread) {
					if ((jsonObject != null) && (operateDB != null)) {
						Object obj = operateDB.saveToDb(jsonObject, page);
						mHandler.obtainMessage(page, obj).sendToTarget();
					} else {
						mHandler.obtainMessage(GET_RESOURCE_ERR).sendToTarget();
					}
				}
			}
		}).start();
	}

	/**
	 * ���ر������ݿ�����
	 * 
	 * @param mHandler
	 *            ������Ϣ�Ķ���
	 * @param page
	 *            ��ʾ��ҳ�� 1--10
	 * @param pageID
	 *            �������ݿ��ҳ��id
	 * @param stopThread
	 * @param operateDB
	 */
	private void loadFromDB(final Handler mHandler, final int page,
			final int pageID, final boolean stopThread,
			final OperateDB operateDB) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (!stopThread) {
					Object obj = operateDB.getFromDb(pageID);
					mHandler.obtainMessage(page, obj).sendToTarget();
				}
			}
		}).start();
	}

}
