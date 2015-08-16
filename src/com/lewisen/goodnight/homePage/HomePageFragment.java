package com.lewisen.goodnight.homePage;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lewisen.goodnight.DisplayImage;
import com.lewisen.goodnight.MaxID;
import com.lewisen.goodnight.MyServer;
import com.lewisen.goodnight.PageLoad;
import com.lewisen.goodnight.R;
import com.lewisen.goodnight.cache.SaveImage;
import com.lewisen.goodnight.collected.CollectedManager;
import com.lewisen.goodnight.like.Like;
import com.lewisen.goodnight.like.LikeManager;
import com.lewisen.goodnight.mainview.MainView;
import com.lewisen.goodnight.mainview.Share;
import com.lewisen.goodnight.mainview.ShareInterface;
import com.lewisen.goodnight.player.PlayerControl;
import com.umeng.analytics.MobclickAgent;

/**
 * @author Lewisen
 * 
 */
public class HomePageFragment extends Fragment implements ShareInterface {
	private View homePageView = null;// ��fragment����
	private ViewPager viewPager = null;// ҳ������
	private List<View> listViews = null; // Tabҳ���б�
	private Handler mHandler = null;
	LayoutInflater mInflater = null;
	private DisplayImage displayImage = null;
	private MaxID maxID = null;
	private HomePageDBManager homePageDBManager = null;
	private MyPagerAdapter myPagerAdapter = null;
	private PageLoad pageLoad = null;
	private int currentMaxId = 0;// ��ǰ�����ID
	private boolean netState = true;// �����״̬ true ������������ �� false ���粻ͨ
	private static boolean stopThread = false;// ��ǰ���治��ʾʱ��Ҫֹͣ������ص��̷߳���message�������ָ��
	private int loadedPage = 0;// ���������Ѿ���������ص�ҳ�����ֵ
	private LikeManager likeManager = null;
	private final String mPageName = "HomePage";

	private PlayerControl playerControl;
	boolean[] pageViewState = { false, false, false, false, false, false,
			false, false, false, false, false };// ������ʾ״̬
												// falseΪû�м��ص�����,ȷ���������治���ظ������view

	@Override
	public void onCreate(Bundle savedInstanceState) {
		stopThread = false;
		displayImage = new DisplayImage();
		pageLoad = new PageLoad();
		maxID = new MaxID();
		homePageDBManager = new HomePageDBManager(getActivity());
		likeManager = new LikeManager(getActivity());

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(mPageName);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(mPageName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// System.out.println("���� HomePageFragment onCreateView");
		mInflater = inflater;
		viewPagerInit(inflater, container);
		mHanderInit();
		// ��ȡ���ش洢�������ҳid
		currentMaxId = maxID.getMaxIdFromProperties(getActivity(),
				"homePageMaxId", 0);
		return homePageView;
	}

	@Override
	public void onDestroy() {
		// displayImage.cleanCache();
		stopThread = true;// ע��! ��ֹ�����̷߳���handler

		for (int i = 0; i < pageViewState.length; i++) {
			pageViewState[i] = false;
		}
		if (playerControl != null) {
			playerControl.stopMusic();
		}
		// Log.d("DEBUG", "onDestroy");
		super.onDestroy();
	}

	/**
	 * viewPagerҳ���л�������
	 * 
	 * @author Lewisen
	 * 
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			// System.out.println("page ѡ��" + arg0);
			if (arg0 == 0) {
				// ��ȡ����Id
				maxID.saveMaxIdToProper(getActivity(), mHandler);
				viewPager.setCurrentItem(1);
				Toast.makeText(getActivity(), "���ڸ���...", Toast.LENGTH_SHORT)
						.show();
			} else if (arg0 == (listViews.size() - 1)) {
				viewPager.setCurrentItem((listViews.size() - 2));
				Toast.makeText(getActivity(), "û�и���������", Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	/**
	 * handler��ʼ��
	 */
	private void mHanderInit() {

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				HomePage homePage = null;
				netState = true;// Ĭ��������ͨ����
				if (!stopThread) {
					if ((msg.what > 0) && (msg.what < 11)) {
						homePage = (HomePage) msg.obj;
						if (homePage != null) {
							homePageDisplay(homePage, listViews.get(msg.what));
							// ��������ʾ��Ϻ� ����likebar
							likeManager.getLikeCountFromNet(mPageName
									+ homePage.getHomePageID(), mHandler,
									stopThread, msg.what);
						}
					} else {
						switch (msg.what) {
						case MaxID.GET_ID_SUCCESS:
							int maxId = currentMaxId;// ��ʱ�洢
							currentMaxId = maxID.getMaxIdFromProperties(
									getActivity(), "homePageMaxId", 0);

							if (maxId == currentMaxId) {
								Toast.makeText(getActivity(), "��������������",
										Toast.LENGTH_LONG).show();
							} else {
								// �ֶ�����ҳ�� �����instantiateItem
								myPagerAdapter.notifyDataSetChanged();
							}
							// ������������ ��ҳ���Ѿ����ص��ڶ�ҳʱ���ٷ���ˢ�²ſ�����������
							if (loadedPage > 2) {
								loadedPage = 0;// �ֶ�ˢ�� ����Ѿ����ص�ҳ��ֵ
								// �ֶ�����ҳ�� �����instantiateItem
								myPagerAdapter.notifyDataSetChanged();
							}

							break;
						case PageLoad.GET_RESOURCE_ERR:
							netState = false;
							myPagerAdapter.notifyDataSetChanged();// �ֶ�����ҳ��
							Toast.makeText(getActivity(), "��ȡ����ʧ��",
									Toast.LENGTH_LONG).show();
							break;
						case LikeManager.GET_LIKE_SUCCESS:
							Like like = (Like) msg.obj;

							likeManager.disLikeState(like,
									listViews.get(like.getPage()));

							break;
						}

					}
				}
				super.handleMessage(msg);
			}
		};
	}

	/**
	 * ViewPager������
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		// �ӵ�ǰcontainer��ɾ��ָ��λ�ã�position arg1����View;
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
			// System.out.println("destroyItem position " + arg1);
		}

		@Override
		// ����Ҫ������VIew�ĸ���
		public int getCount() {
			return mListViews.size();
		}

		@Override
		// ���� ��һ������ǰ��ͼ��ӵ�container�У��ڶ������ص�ǰView
		public Object instantiateItem(View arg0, int arg1) {

			pageLoad.pageUpdateControl(currentMaxId, arg1, netState, mHandler,
					stopThread, homePageDBManager, MyServer.HOME_PAGE,
					loadedPage);

			if (arg1 > loadedPage) { // �����Ѿ����ص�ҳ�����ֵ
				loadedPage = arg1;
			}

			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public int getItemPosition(Object object) {
			// return super.getItemPosition(object);
			// ����NONE��Ϊ���ֶ����½���
			return POSITION_NONE;
		}

	}

	/**
	 * ҳ�����ݸ���
	 * 
	 * @param homePage
	 *            Ҫ��ʾ������
	 * @param view
	 */
	private void homePageDisplay(HomePage homePage, View view) {

		TextView date = (TextView) view.findViewById(R.id.date_home);
		TextView readCount = (TextView) view.findViewById(R.id.readcount_home);
		TextView title = (TextView) view.findViewById(R.id.title_home);
		TextView author = (TextView) view.findViewById(R.id.author_home);

		TextView authorIntro = (TextView) view
				.findViewById(R.id.author_intro_home);
		ImageView eye = (ImageView) view.findViewById(R.id.eye_home);
		View topLine = (View) view.findViewById(R.id.line_home);
		View bottomLine = (View) view.findViewById(R.id.line_bottom_home);
		ProgressBar loading = (ProgressBar) view
				.findViewById(R.id.loading_bar_home);
		loading.setVisibility(View.GONE);
		eye.setVisibility(View.VISIBLE);
		topLine.setVisibility(View.VISIBLE);
		bottomLine.setVisibility(View.VISIBLE);

		date.setText(homePage.getDate());
		readCount.setText(homePage.getReadCount() + "");
		title.setText(homePage.getTitle());
		author.setText(homePage.getAuthor());
		authorIntro.setText(homePage.getAuthorIntro());
		// ��ʾͼ�ĵ����Բ���
		LinearLayout imageTextLayout = (LinearLayout) view
				.findViewById(R.id.layout_image_text_home);
		// ���Ƴ������Ѿ����ƵĽ���
		// imageTextLayout.removeAllViews();

		// ��һ�μ��ظ�ҳ��
		if (!pageViewState[homePage.getId()]) {

			// ͼ�ķָ���ʾ ����������ʾ
			imageTextDisplay(homePage, imageTextLayout);
			pageViewState[homePage.getId()] = true;
		}
		// �����������ݺ���ʾ��likebar�Ĳ���
		LinearLayout likeBar = (LinearLayout) view
				.findViewById(R.id.like_bar_home);
		likeBar.setVisibility(View.VISIBLE);
	}

	/**
	 * ���ֲ�����bar
	 * 
	 * @param imageTextLayout
	 */
	private void musicPlayer(LinearLayout imageTextLayout, HomePage homePage) {

		// ���homePage������û��music���ݣ����ȡ��StringΪnull
		String title = homePage.getMusicTitle();
		String author = homePage.getMusicAuthor();
		String musicURL = homePage.getMusicURL();
		String musicImage = homePage.getMusicImage();

		// Log.d("DEBUG", "  musicTitle=" + (title == null) + "   musicAuthor="
		// + author + "   musicURL=" + musicURL);
		// Ϊnull,��ת������ʾ
		if ((title == null) || (author == null) || (musicURL == null)) {
			return;
		}
		RelativeLayout musicView = (RelativeLayout) mInflater.inflate(
				R.layout.music_view, imageTextLayout, false);
		SeekBar seekBar = (SeekBar) musicView.findViewById(R.id.music_progress);
		TextView musicTimeText = (TextView) musicView
				.findViewById(R.id.music_time);
		ImageButton playButton = (ImageButton) musicView
				.findViewById(R.id.music_button);
		TextView musicTitle = (TextView) musicView
				.findViewById(R.id.music_title);
		TextView musicAuthor = (TextView) musicView
				.findViewById(R.id.music_author);
		if (musicImage != null) {
			ImageView musicIcon = (ImageView) musicView
					.findViewById(R.id.music_icon);
			displayImage.displayImage(musicIcon, musicImage);
		}

		musicTitle.setText(title);
		musicAuthor.setText(author);
		// �������ֳ�ʼ��
		MainView mainView = (MainView) this.getActivity();
		playerControl = mainView.getPlayContler();
		playerControl.playerControlInit(seekBar, musicTimeText, playButton,
				musicURL, getActivity());
		// ������ֲ��Ž��浽����ͼ
		imageTextLayout.addView(musicView);
	}

	/**
	 * ͼ�ķָ���ʾ
	 * 
	 * @param homePage
	 * @param imageTextLayout
	 */
	private void imageTextDisplay(HomePage homePage,
			LinearLayout imageTextLayout) {
		String[] imageSrcPart = homePage.getImageSrc().split("###");
		String[] textPart = homePage.getText().split("###");

		RelativeLayout imageText = null;

		int textLength = textPart.length;// �ı��ָ�Ϊ�������֣���1��ʼ
		// int imageLength = imageSrcPart.length;
		// 7.28ȥ������ҳͼ�ķָ����һ�µ�Ҫ�󣬸�Ϊ���ķָ�Ϊ��
		// if (textLength == imageLength) {
		for (int i = 0; i < textLength; i++) {
			// ����һ������
			imageText = (RelativeLayout) mInflater.inflate(R.layout.image_text,
					imageTextLayout, false);
			ImageView imageView = (ImageView) imageText
					.findViewById(R.id.picture_image_text);

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
							Toast.makeText(getActivity(), "ͼƬ�ѱ��浽" + path,
									Toast.LENGTH_LONG).show();
						}
						return true;
					}
				});
			}

			// �����@@@ ����������Ҫ������ ������ͼ����ʾ��ǰ��
			if (textPart[i].contains("@@@")) {
				musicPlayer(imageTextLayout, homePage);// �������ֲ�����
				textView.setText(textPart[i].replace("@@@", ""));
			} else {
				textView.setText(textPart[i]);
			}

			imageTextLayout.addView(imageText);

			// ע�͵���Ϊ �ڲ���@@@�ĵط����벥����������ʹ�õ��ǣ��������ֲ��ŵĽ�������϶���ʾ
			// ����������Ƿ���@@@
			// String[] text = textPart[i].split("@@@");
			// int length = text.length;
			// �����@@@
			// if (length == 2) {
			// textView.setText(text[0]);
			// // ��ʾ@@@���ϵ�����
			// imageTextLayout.addView(imageText);
			// // ���벥��������
			// musicPlayer(imageTextLayout, homePage);
			// // ������ʾ@@@���µ���������
			// imageText = (RelativeLayout) mInflater.inflate(
			// R.layout.image_text, imageTextLayout, false);
			// ImageView imageView1 = (ImageView) imageText
			// .findViewById(R.id.picture_image_text);
			// imageView1.setVisibility(View.GONE);// ���ɼ�
			// TextView textView1 = (TextView) imageText
			// .findViewById(R.id.text_image_text);
			// textView1.setVisibility(View.VISIBLE);
			// textView1.setText(text[1]);
			// imageTextLayout.addView(imageText);
			// } else {
			// textView.setText(textPart[i]);
			// imageTextLayout.addView(imageText);
			// }

			// }
		}
	}

	private void viewPagerInit(LayoutInflater inflater, ViewGroup container) {
		homePageView = inflater.inflate(R.layout.viewpaper, container, false);
		viewPager = (ViewPager) homePageView.findViewById(R.id.viewpaper);
		// ��Ҫ��ҳ��ʾ��Viewװ��list��
		listViews = new ArrayList<View>();
		listViews.add(inflater.inflate(R.layout.blank_viewpaper, null));
		// ���ʮ��ҳ��
		for (int i = 0; i < 10; i++) {
			listViews.add(inflater.inflate(R.layout.home_page, null));
		}
		listViews.add(inflater.inflate(R.layout.blank_viewpaper, null));
		myPagerAdapter = new MyPagerAdapter(listViews);
		viewPager.setAdapter(myPagerAdapter);
		// ���õ�ǰ��1��ʼ 0λ�հ׵�viewpaper
		viewPager.setCurrentItem(1);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	@Override
	public Share shareContent(boolean isCollected) {
		Share share = new Share();
		// ��ȡ��ǰҳ����ʾ������ID
		int pageID = currentMaxId + 1 - viewPager.getCurrentItem();
		// �����ݿ��ȡ��ǰ��ʾ������
		share.setTargetUrl(MyServer.SHARE_URL + "type=1&id=" + pageID);
		share.setName(mPageName + pageID);
		HomePage homePage = homePageDBManager.getHomePage(pageID);
		if (homePage != null) {
			share.setTitle(homePage.getTitle());
			int len = homePage.getText().length();
			if (len > 50) {
				len = 50;
			}
			share.setContent(homePage.getText().substring(0, len)
					.replace("###", "").replace("@@@", ""));// �����ȡǰ50��

			// ���������ղ�
			if (isCollected) {
				CollectedManager manager = new CollectedManager(getActivity());
				// �����ǰҳ��û�б��ղع�����ô��ȡ��ֵΪĬ�ϵ�
				if (manager.getIdFromProperties(share.getName()) == CollectedManager.DEFAULT_COUNT) {
					int id = manager
							.getIdFromProperties(CollectedManager.HOME_C);
					homePage.setId(id);
					try {
						homePageDBManager.add(homePage);
					} catch (SQLException e) {
						homePageDBManager.deleteFromDb(id);
						try {
							homePageDBManager.add(homePage);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
					// ������¼��ǰ�洢������
					manager.saveIdToProperties(CollectedManager.HOME_C, id + 1);
					// ������¼��ǰ���������
					manager.saveIdToProperties(share.getName(), id + 1);

					share.setName("success");
				} else {
					share.setName("haved");
				}
			}
		}
		return share;
	}
}
