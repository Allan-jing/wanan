package com.lewisen.goodnight.picturePage;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.lewisen.goodnight.mainview.Share;
import com.lewisen.goodnight.mainview.ShareInterface;
import com.umeng.analytics.MobclickAgent;

public class PicturePageFragment extends Fragment implements ShareInterface {
	private View picturePageView = null;
	private ViewPager viewPager = null;// ҳ������
	private List<View> listViews = null; // Tabҳ���б�
	private Handler mHandler = null;
	private DisplayImage displayImage = null;
	private MaxID maxID = null;
	private MyPagerAdapter myPagerAdapter = null;
	private PicturePageDBManager picturePageDBManager = null;
	private PageLoad pageLoad = null;
	private int currentMaxId = 0;// ��ǰ�����ID
	private boolean netState = true;// �����״̬ true ������������ �� false ���粻ͨ
	private static boolean stopThread = false;
	private int loadedPage = 0;
	private LikeManager likeManager = null;
	private final String mPageName = "PicturePage";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		stopThread = false;
		displayImage = new DisplayImage();
		pageLoad = new PageLoad();
		maxID = new MaxID();
		picturePageDBManager = new PicturePageDBManager(getActivity());
		likeManager = new LikeManager(getActivity());
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// System.out.println("���� PicturePageFragment onCreateView");
		viewPagerInit(inflater, container);
		mHandlerInit();
		// ��ȡ���ش洢�������ҳid
		currentMaxId = maxID.getMaxIdFromProperties(getActivity(),
				"picturePageMaxId", 0);

		return picturePageView;
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
	public void onDestroy() {
		stopThread = true;
		// picturePageDBManager.closeDB();
		super.onDestroy();
	}

	/**
	 * @author Lewisen
	 * 
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			// System.out.println("page ѡ��" + arg0);
			if (arg0 == 0) {
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
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
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
					stopThread, picturePageDBManager, MyServer.PICTURE_PAGE,
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

	private void mHandlerInit() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				PicturePage picturePage = null;
				netState = true;// Ĭ��������ͨ����
				if (!stopThread) {
					if ((msg.what > 0) && (msg.what < 11)) {
						picturePage = (PicturePage) msg.obj;
						if (picturePage != null) {
							picturePageDisplay(picturePage,
									listViews.get(msg.what));
							likeManager.getLikeCountFromNet(mPageName
									+ picturePage.getPicturePageID(), mHandler,
									stopThread, msg.what);
						}
					} else {
						switch (msg.what) {
						case MaxID.GET_ID_SUCCESS:
							int maxId = currentMaxId;// ��ʱ�洢
							currentMaxId = maxID.getMaxIdFromProperties(
									getActivity(), "picturePageMaxId", 0);

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
	 * ҳ�����ݸ���
	 * 
	 * @param picturePage
	 *            Ҫ��ʾ������
	 * @param page
	 *            ���ĸ�����ҳ����ʾ 1,2,3,
	 * @param view
	 */
	private void picturePageDisplay(PicturePage picturePage, View view) {
		TextView date = (TextView) view.findViewById(R.id.date_picture);
		TextView readCount = (TextView) view
				.findViewById(R.id.readcount_picture);
		TextView text = (TextView) view.findViewById(R.id.text_picture);
		TextView authorIntro = (TextView) view
				.findViewById(R.id.author_intro_picture);
		ImageView imageView = (ImageView) view
				.findViewById(R.id.picture_picture);

		ImageView eye = (ImageView) view.findViewById(R.id.eye_picture);
		View topLine = (View) view.findViewById(R.id.line_picture);
		View bottomLine = (View) view.findViewById(R.id.line_bottom_picture);
		ProgressBar loading = (ProgressBar) view
				.findViewById(R.id.loading_bar_picture);

		loading.setVisibility(View.GONE);
		eye.setVisibility(View.VISIBLE);
		topLine.setVisibility(View.VISIBLE);
		bottomLine.setVisibility(View.VISIBLE);

		final String imageSrc = picturePage.getImageSrc();
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
		date.setText(picturePage.getDate());
		readCount.setText(picturePage.getReadCount() + "");
		text.setText(picturePage.getText());
		authorIntro.setText(picturePage.getAuthorIntro());

		// �����������ݺ���ʾ��likebar�Ĳ���
		LinearLayout likeBar = (LinearLayout) view
				.findViewById(R.id.like_bar_picture);
		likeBar.setVisibility(View.VISIBLE);
	}

	private void viewPagerInit(LayoutInflater inflater, ViewGroup container) {
		picturePageView = inflater
				.inflate(R.layout.viewpaper, container, false);
		viewPager = (ViewPager) picturePageView.findViewById(R.id.viewpaper);
		// ��Ҫ��ҳ��ʾ��Viewװ��list��
		listViews = new ArrayList<View>();
		listViews.add(inflater.inflate(R.layout.blank_viewpaper, null));
		// ���ʮ��ҳ��
		for (int i = 0; i < 10; i++) {
			listViews.add(inflater.inflate(R.layout.picture_page, null));
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
		share.setTargetUrl(MyServer.SHARE_URL + "type=3&id=" + pageID);
		share.setName(mPageName + pageID);
		// �����ݿ��ȡ��ǰ��ʾ������
		PicturePage picturePage = picturePageDBManager.getPicturePage(pageID);
		if (picturePage != null) {
			int len = picturePage.getText().length();
			if (len > 50) {
				len = 50;
			}
			share.setContent(picturePage.getText().substring(0, len));
			share.setTitle("ͼ��--���Ķ�");

			// ���������ղ�
			if (isCollected) {
				CollectedManager manager = new CollectedManager(getActivity());
				// �����ǰҳ��û�б��ղع�����ô��ȡ��ֵΪĬ�ϵ�
				if (manager.getIdFromProperties(share.getName()) == CollectedManager.DEFAULT_COUNT) {
					int id = manager
							.getIdFromProperties(CollectedManager.PIC_C);
					picturePage.setId(id);
					try {
						picturePageDBManager.add(picturePage);
					} catch (SQLException e) {
						picturePageDBManager.deleteFromDb(id);
						try {
							picturePageDBManager.add(picturePage);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
					// ������¼��ǰ�洢������
					manager.saveIdToProperties(CollectedManager.PIC_C, id + 1);
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
