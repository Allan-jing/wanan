package com.lewisen.goodnight.userPage;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

import com.lewisen.goodnight.R;

public class SetMenu extends Activity {

	private ImageButton backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// �Զ������
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.set);
		// ���ñ���Ϊĳ��layout
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titlebar);

		backButton = (ImageButton) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		backButton.setVisibility(View.VISIBLE);

	}

	

}
