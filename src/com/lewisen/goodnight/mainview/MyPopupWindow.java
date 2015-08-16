package com.lewisen.goodnight.mainview;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.lewisen.goodnight.MyServer;
import com.lewisen.goodnight.R;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class MyPopupWindow implements OnClickListener {

	private Context context;
	private UMSocialService umSocialService;
	private ShareInterface shareInterface;
	private Share share;
	private PopupWindow popupWindow;
	private boolean disCollectButton;

	/**
	 * @param context
	 * @param umSocialService
	 * @param disCollectButton�Ƿ���ʾ�ղذ���
	 */
	public MyPopupWindow(Context context, UMSocialService umSocialService,
			boolean disCollectButton) {
		this.context = context;
		this.umSocialService = umSocialService;
		this.disCollectButton = disCollectButton;
	}

	public void showMenuWindow(View view, ShareInterface shareInterface) {
		this.shareInterface = shareInterface;
		View popupWindowView = LayoutInflater.from(context).inflate(
				R.layout.popup_window, null, false);

		Button shareButton = (Button) popupWindowView
				.findViewById(R.id.share_button);
		Button commentButton = (Button) popupWindowView
				.findViewById(R.id.comment_button);

		shareButton.setOnClickListener(this);
		commentButton.setOnClickListener(this);
		if (disCollectButton) {
			Button favButton = (Button) popupWindowView
					.findViewById(R.id.favourites_button);
			favButton.setOnClickListener(this);
		} else {
			popupWindowView.findViewById(R.id.favourites_button).setVisibility(
					View.GONE);
			popupWindowView.findViewById(R.id.line2).setVisibility(View.GONE);
		}

		popupWindow = new PopupWindow(popupWindowView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

		popupWindow.setTouchable(true);

		popupWindow.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Log.d("DEBUG", "OnTouchListener");
				return false;
			}
		});
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(0));// ��ʵ������
		popupWindow.setAnimationStyle(R.style.AnimationPreview);
		// ��ʾpopup
		popupWindow.showAsDropDown(view, 0, 0);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_button:
			share = shareInterface.shareContent(false);
			if (share != null) {
				shareContentSet();
				// �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ
				umSocialService.openShare((Activity) context, false);
			}
			break;
		case R.id.comment_button:
			// Log.d("DEBUG", "comment_button share.getName()=" +
			// share.getName());
			String name = "com.lewisen.comment";
			share = shareInterface.shareContent(false);
			if (share != null) {
				name = share.getName();

				UMSocialService umComment = UMServiceFactory
						.getUMSocialService(name);
				// �Ƿ�ǿ�Ƶ�¼����ܷ�������. ȡֵΪfalse��ʾ�����ο���ݷ�������
				umComment.openComment(context, false);
			}
			break;

		case R.id.favourites_button:
			if (shareInterface != null) {
				String state = shareInterface.shareContent(true).getName();
				if ("success".equals(state)) {
					Toast.makeText(context, "����ӵ��ղ�", Toast.LENGTH_LONG).show();
				} else if ("haved".equals(state)) {
					Toast.makeText(context, "�Ѵ��ڣ���ɾ�����ղ���ǰ���ҵ��ղأ���������Ŀɾ��",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(context, "�ղ�ʧ�ܣ�������", Toast.LENGTH_LONG).show();
			}
			if (popupWindow != null) {
				popupWindow.dismiss();
			}
			break;
		default:
			break;
		}
	}

	private void shareContentSet() {
		UMImage urlImage = new UMImage(context, MyServer.APP_ICON);
		String targetUrl = share.getTargetUrl();
		// ΢��
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		weixinContent.setShareContent(share.getContent());
		weixinContent.setTitle(share.getTitle());
		weixinContent.setTargetUrl(targetUrl);
		weixinContent.setShareMedia(urlImage);
		umSocialService.setShareMedia(weixinContent);

		// ��������Ȧ���������
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setShareContent(share.getContent());
		circleMedia.setTitle(share.getTitle());
		circleMedia.setShareMedia(urlImage);
		circleMedia.setTargetUrl(targetUrl);
		umSocialService.setShareMedia(circleMedia);

		// ����QQ�ռ��������
		QZoneShareContent qzone = new QZoneShareContent();
		qzone.setShareContent(share.getContent());
		qzone.setTargetUrl(targetUrl);
		qzone.setTitle(share.getTitle());
		qzone.setShareMedia(urlImage);
		umSocialService.setShareMedia(qzone);

		// QQ
		QQShareContent qqShareContent = new QQShareContent();
		qqShareContent.setShareContent(share.getContent());
		qqShareContent.setTitle(share.getTitle());
		qqShareContent.setShareMedia(urlImage);
		qqShareContent.setTargetUrl(targetUrl);
		umSocialService.setShareMedia(qqShareContent);
	}

}
