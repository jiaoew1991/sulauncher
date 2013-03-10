package com.sulauncher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.miscwidgets.widget.Panel;
import org.miscwidgets.widget.Panel.OnPanelListener;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sulauncher.R;
import com.sulauncher.applicationbrowser.AllAppAdapter;
import com.sulauncher.applicationbrowser.AllAppListItem;
import com.sulauncher.applicationbrowser.AllAppView;
import com.sulauncher.db.DatabaseManager;
import com.sulauncher.db.PatternHashMap;
import com.sulauncher.gesturemgr.GestureMgrActivity;
import com.sulauncher.search.AppListView;
import com.sulauncher.search.ConListView;
import com.sulauncher.search.DataCollector;
import com.sulauncher.search.GestureHintFrameLayout;
import com.sulauncher.search.ListUpdater;
import com.sulauncher.search.ListWithTag.ListTags;
import com.sulauncher.search.ListUpdater.KeyChangeType;
import com.sulauncher.search.SearchListAdapter;
import com.sulauncher.supad3d.ButtonBack;
import com.sulauncher.slidingbar.PanelMgrActivity;
import com.sulauncher.slidingbar.PanelSlideView;
import com.sulauncher.slidingbar.PanelSlideView.OnChangeTextListener;
import com.sulauncher.slidingbar.SlideAdapter;
import com.sulauncher.supad3d.Supad3DT9Translator;
import com.sulauncher.supad3d.Supad3DUtils;
import com.sulauncher.supad3d.Supad3DView;
import com.sulauncher.supad3d.Supad3DView.DisplayMode;
import com.sulauncher.supad3d.Supad3DView.OnPatternListener;
import com.sulauncher.supad3d.Supad3DView.OnResetPatternListener;
import com.sulauncher.widget.DraggableGridView;
import com.sulauncher.widget.DraggableGridView.OnFlingListener;
import com.sulauncher.widget.HorizontalListView;
import com.sulauncher.widget.gridview.OnRearrangeListener;
/**
 * MainAcitivity of Su Launcher
 * @author YuMS  &&  Jiaoew
 */
/**
 * @author YuMS
 *
 */
@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	public static class Messages {
		public static final int RESULT_LIST_UPDATE_FINISH = 1;
		public static final int SHOW_SUPAD3DVIEW = 2;
		public static final int HIDE_SUPAD3DVIEW = 3;
		public static final int START_HIDING_SUPAD3DVIEW = 4;
		public static final int START_PANELCONTENT_ADJUST = 5;
	}
	/**
	 * statics
	 */
	private static final String TAG = "MainActivity";
	private DisplayMetrics disMetrics = new DisplayMetrics();
	public static boolean returnFromSelf = false;
	private static final int MAX_FAVORITE_CONTAIN = 12;
	private static final int MAX_FOLDER_SIZE = 5;
	
	/**
	 * View variables
	 */
	private LinearLayout searchLists;
	public LinearLayout getSearchLists() {
		return searchLists;
	}
	private FrameLayout supadFrameLayout = null;
	private ImageView listMask = null;
	private Supad3DView supad3DView = null;
	public Supad3DView getSupad3DView() {
		return supad3DView;
	}
	private TextView clockText = null;
	private SearchListAdapter conAdapter = null;
	private SearchListAdapter appAdapter = null;
	private ConListView conList = null;
	private AppListView appList = null;
	private TextView searchText = null;
	private ImageView backgroundMaskView = null;
	private View dock_shortcut = null;
	
	private SharedPreferences sp = null;
	public static Handler mainHandler = null;
	private DataCollector dc = null;
	
	/**
	 * Status record
	 */
	private static Object updatingListLock = new Object();
	private static boolean updatingListLocked = false;
	private boolean isSupadCoverViewsInitiated = false;
	private boolean switchingToSearch = true;
	private ButtonBack lastCell = null;
	private Date switchTime = null;
	private MainActivityStatuses status = MainActivityStatuses.DEFAULT_DESKTOP;
	
	/**
	 * The instance of Su Launcher' s main activity, for context use
	 */
	private static MainActivity theInstance = null;
	public static MainActivity getTheInstance() {
		return theInstance;
	}
	/**
	 * Initiate time view
	 */
	private Time mTime = new Time();
	private IntentFilter timeFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
	private IntentFilter addPackageFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
	private IntentFilter removePackageFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
	private BroadcastReceiver clockOutOfTimeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateTime();
		}
	};
	private BroadcastReceiver addPackageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};
	private BroadcastReceiver removePackageReceiver = new  BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("remove", "activitiy: " + intent.getDataString() + ", action: " + intent.getAction());
			String name = intent.getDataString().substring(8);
			if (name.equals("com.sulauncher")) return;
			AllAppListItem item  = AllAppAdapter.getItem(name);
			if (item == null) {
				return;
			}
			AllAppAdapter.removeStaticListItem(item);
			for (AllAppAdapter adp : mAllAdapters) {
				adp.removeItem(item);
			}
			int panelCount = 0;
			for (SlideAdapter sadp : mAdapters) {
				List<AllAppListItem> list = sadp.getList();
				int index = -1;
				int count = 0;
				for (AllAppListItem listItem : list) {
					if (listItem.packageName.equals(item.packageName)) {
						index = count;
						break;
					}
					count ++;
				}
				Log.d("remove", "index: " + index);
				if (index != -1) {
					list.remove(index);
					Panel p = mPanels.get(panelCount);
					DraggableGridView dgv = (DraggableGridView) p.findViewById(R.id.panel_drawer_content_grid);
					dgv.removeViewAt(index);
					//				sadp.notifyDataSetChanged();
				}
				panelCount++;
			}
			mAppView.removeItem(item);
		}
	};
	/**
	 * For time update use
	 */
	private void updateTime() {
		mTime.setToNow();
		DecimalFormat format = new DecimalFormat("00");
		String ywd = mTime.year+ "-" + (mTime.month + 1) + "-" + mTime.monthDay;
		String hm = format.format(mTime.hour) + " : " + format.format(mTime.minute);
		
		SpannableString ss = new SpannableString(hm + "  " + ywd);
		ss.setSpan(new RelativeSizeSpan(0.8f), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(new RelativeSizeSpan(0.2f), 9, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		clockText.setText(ss);
	}
	/**
	 * Initiate dock bar
	 */
	private ImageButton dial = null;
	private ImageButton sms = null;
	private ImageButton allApp = null;
	private ImageButton hideSupadButton = null;
	private ImageButton removeButton = null;
	private ImageButton dialOutImage = null;
	private void initDockBar() {
		dial = (ImageButton) findViewById(R.id.dial_image);
		dial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_DIAL);
				startActivity(intent);
			}
		});
		sms = (ImageButton) findViewById(R.id.sms_image);
		sms.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setClassName("com.android.mms", "com.android.mms.ui.ConversationList");
				startActivity(intent);
			}
		});
		dialOutImage = (ImageButton) findViewById(R.id.dial_out_image);
		dialOutImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + searchText.getText()));
			    startActivity(intent);
			}
			
		});
		allApp = (ImageButton) findViewById(R.id.all_image);
		allApp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setClass(MainActivity.this, AllAppActivity.class);
//				startActivity(intent);
				changeStatus(MainActivityStatuses.ALL_APP_SHOWN);
			}
		});
		hideSupadButton = (ImageButton) findViewById(R.id.supad_image);
		hideSupadButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleSupad3DView();
			}

		});
		removeButton = (ImageButton) findViewById(R.id.remove_number);
		removeButton.setVisibility(View.INVISIBLE);
		removeButton.setEnabled(false);
		removeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (status == MainActivityStatuses.SEARCHING) {
					searchText.setText(searchText.getText().subSequence(0, searchText.length() - 1));
					updateSearchResult(KeyChangeType.NOTAPPEND);
					if (searchText.length() == 0) {
						changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
					}
				} 
			}
		});
		removeButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				if (status == MainActivityStatuses.SEARCHING) {
					searchText.setText("");
					updateSearchResult(KeyChangeType.NOTAPPEND);
					changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
				} 
				return true;
			}
		});
	}
	
	/**
	 * Initiate gestureHintFrameLayout
	 */
	private void initGestureHintFrameLayout() {
		int height = supadFrameLayout.getHeight();
		height -= supad3DView.getHeight() * 9 / 10;
		Log.d(TAG, "gestureHint height" + height);
		int width = supadFrameLayout.getWidth();
		gestureHintFrameLayout.init(width, height, this);
	}
	
	private String patternString = null;
	/**
	 * initiate Supad
	 */
	private void initSupad() {
		supad3DView.setOnPatternListener(new OnPatternListener() {
			@Override
			public void onPatternStart() {
				Log.d("MainActivity", "patternStart");
				if (status == MainActivityStatuses.DEFAULT_DESKTOP) {
					changeStatus(MainActivityStatuses.SEARCHING_OR_SWIPING);
				}
			}
			@Override
			public void onPatternDetected(List<ButtonBack> pattern) {
				Log.d("MainActivity", "patternDetected");
				patternString = Supad3DUtils.patternToString(pattern);
				if (pattern.size() == 1) {
					if (status == MainActivityStatuses.SEARCHING_OR_SWIPING) {
						changeStatus(MainActivityStatuses.SEARCHING);
					}
				} else {
//					changeStatus(MainActivityStatuses.SWIPING);
					if (DatabaseManager.havePattern(patternString)) {
						int x = lastCell.getRow();
						int y = lastCell.getColumn();
//						Utils.hideView(supadCoverViews[x][y]);
//						supadCoverViews[x][y].setImageResource(R.drawable.blank_blue);
						String packageName = DatabaseManager.getPattern(patternString);
						Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
						startActivity(intent);
					} else if (pattern.size() > 1){
						supad3DView.setDisplayMode(DisplayMode.Wrong);
						Toast.makeText(MainActivity.this, R.string.gesture_inexistence, Toast.LENGTH_SHORT).show();
					}
				}
				supad3DView.clearPattern();
				if (status == MainActivityStatuses.SWIPING) {
					changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
				}
				gestureHintFrameLayout.resetBlankDrawables();
			}
			@Override
			public void onPatternCleared() {
				Log.d("MainActivity", "patternCleared");
			}
			
			@Override
			public void onPatternCellAdded(List<ButtonBack> pattern){
				Log.d(TAG, "patternadd");
				if (status == MainActivityStatuses.SEARCHING) {
					return;
				}
				if (status == MainActivityStatuses.SEARCHING_OR_SWIPING) {
					changeStatus(MainActivityStatuses.SWIPING);
				}
				PackageManager pm = MainActivity.this.getPackageManager();
				ButtonBack currentCell = pattern.get(pattern.size() - 1);
				int x = currentCell.getRow();
				int y = currentCell.getColumn();
				boolean currentCellHavePattern = false;
				String patternString = Supad3DUtils.patternToString(pattern);
				gestureHintFrameLayout.setBlankDrawable(x, y);
				if (PatternHashMap.havePattern(patternString)) {
					currentCellHavePattern = true;
					String packName = PatternHashMap.getPattern(patternString);
					try {
						ApplicationInfo appInfo = pm.getApplicationInfo(packName, PackageManager.GET_META_DATA);
						Drawable icon = pm.getApplicationIcon(appInfo);
						gestureHintFrameLayout.renewSquareToMatch(x, y);
						gestureHintFrameLayout.renewMatchSquare(icon);
						Log.d(TAG, "show one");
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
				} else {
					gestureHintFrameLayout.renewMatchToBlank();
				}
				int lastX = -1, lastY = -1;
				if (lastCell != null) {
					lastX = lastCell.getRow();
					lastY = lastCell.getColumn();
				}
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 3; j++) {
						if (currentCellHavePattern && i == x && j == y) {
							continue;
						}
						pattern.add(supad3DView.getmButtonBacks()[i][j]);
						patternString = Supad3DUtils.patternToString(pattern);
						if (lastCell != null && lastX == i && lastY == j) {
							gestureHintFrameLayout.renewSquareToBlank(i, j);
						} else {
							gestureHintFrameLayout.renewSquareToBlankWithoutAnim(i, j);
						}
						if (PatternHashMap.havePattern(patternString)) {
							Log.d("MainActivity", "row: " + i + "column: " + j);
							String packName = PatternHashMap.getPattern(patternString);
							try {
								ApplicationInfo appInfo = pm.getApplicationInfo(packName, PackageManager.GET_META_DATA);
								Drawable icon = pm.getApplicationIcon(appInfo);
								gestureHintFrameLayout.renewSquare(i, j, icon);
								Log.d(TAG, "show one");
							} catch (NameNotFoundException e) {
								e.printStackTrace();
							}
						}
						pattern.remove(pattern.size() - 1);
					}
				}
				if (currentCellHavePattern) {
					lastCell = currentCell;
				} else {
					lastCell = null;
				}
			}
		});
		supad3DView.setOnResetPatternListener(new OnResetPatternListener() {
			@Override
			public void onResetPattern() {
				if (!isSupadCoverViewsInitiated) {
					initGestureHintFrameLayout();
					isSupadCoverViewsInitiated = true;
				}
				Log.d("MainActivity", "resetpattern");
			}
		});
		supad3DView.setOnClickListener(new Supad3DView.OnClickListener() {
			@Override
			public void onClick(int column, int row) {
				Log.d("MainActivity", "click column: " + column + ", row: " + row);
				if (row < 3) {
					searchText.append((column + row * 3 + 1) +"");
				}
				else {
					switch(column){
					case 0:
						searchText.append("*");
						break;
					case 1:
						searchText.append("0");
						break;
					case 2:
						searchText.append("#");
					}
				}	
				if (searchText.length() == 1) {
					updateSearchResult(KeyChangeType.NOTAPPEND);
				} else {
					updateSearchResult(KeyChangeType.APPEND);
				}
			}
		});
	}
	//
	/**
	 * Update search result(Search text changed)
	 * @param change The change type
	 */
	private void updateSearchResult(final KeyChangeType change) {
		Thread thread = new Thread() {
			public void run() {
				String str = searchText.getText().toString();
				if (change == KeyChangeType.NOTAPPEND) {
					String[] strArr = new String[str.length()];
					for (int i = 0; i < str.length(); i++) {
						strArr[i] = Supad3DT9Translator.tr(str.charAt(i));
					}
					synchronized (updatingListLock) {
						Log.d(TAG, "not append get lock");
						updatingListLocked = true;
						if (switchingToSearch) {
							Date now = new Date();
							while (now.before(switchTime)) {
								Log.d(TAG, "notappend sleeping");
								try {
									Thread.sleep(100);
									Log.d(TAG, "Sleeping");
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								now = new Date();
							}
							switchingToSearch = false;
						}
						ListUpdater.instance().renew(strArr);
						updatingListLocked = false;
					}
				} else {
					synchronized (updatingListLock) {
						Log.d(TAG, "append get lock");
						updatingListLocked = true;
						if (switchingToSearch) {
							Log.d(TAG, "append switchingToSearch true");
							Date now = new Date();
							while (now.before(switchTime)) {
								Log.d(TAG, "append sleeping");
								try {
									Thread.sleep(100);
									Log.d(TAG, "Sleeping");
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								now = new Date();
							}
							switchingToSearch = false;
						}
						ListUpdater.instance().renew(Supad3DT9Translator.tr(str.charAt(str.length() - 1)));
						updatingListLocked = false;
					}
				}
				Message msg = mainHandler.obtainMessage();
				msg.arg1 = Messages.RESULT_LIST_UPDATE_FINISH;
				mainHandler.sendMessage(msg);
			}
		};
		thread.start();
		thread = null;
	}
//	/**
//	 * hide search lists with animation
//	 */
//	private void hideSearchLists(Animation animation) {
//		hideView(conList, animation);
//		hideView(appList, animation);
//	}
	/**
	 * Sudden hide search lists
	 */
	private void hideSearchLists() {
		Utils.hideView(conList);
		Utils.hideView(appList);
	}
//	/**
//	 * Show search lists with animation
//	 */
//	private void showSearchLists(Animation animation) {
//		showView(conList, animation);
//		showView(appList, animation);
//	}
	/**
	 * Sudden show search lists
	 */
	private void showSearchLists() {
		Utils.showView(conList);
		Utils.showView(appList);
	}

	/**
	 * Initiate the status
	 */
	private void initStatus() {
		hideSearchLists();
		Utils.hideView(searchText);
		Utils.hideView(backgroundMaskView);
		Utils.hideView(dialOutImage);
		Utils.hideView(removeButton);
		Utils.hideView(mAppContainer);
		Utils.hideView(mAppView.findViewById(R.id.my_Grid));
		Utils.hideView(mAppView);
		Utils.hideView(listMask);
		Utils.hideView(gestureHintFrameLayout);
		searchText.setText("");
		switchTime = new Date();
		switchTime.setTime(switchTime.getTime() + Integer.MAX_VALUE);
		status = MainActivityStatuses.DEFAULT_DESKTOP;
//		changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
	}
	/**
	 * Switch between defined status
	 * @param toStatus
	 */
	public void changeStatus(MainActivityStatuses toStatus) {
		switch (toStatus) {
//		case DEFAULT:
//			Utils.showView(mainRelative, Animations.getAlphaZoomInAnimation());
//			Utils.hideView(mAppView, Animations.getAlphaZoomOutAnimation());
//			status = MainActivityStatuses.DEFAULT;
//			Log.d(TAG, "change to default");
		case DEFAULT_DESKTOP:
			hideSearchLists();
			Utils.hideView(mAppView, Animations.getAlphaZoomOutAnimation());
			Utils.hideView(searchText, Animations.getAlphaTopOutAnimation());
			Utils.hideView(backgroundMaskView, Animations.getAlphaOutAnimation());
			Utils.hideView(dialOutImage, Animations.getAlphaBottomOutAnimation());
			Utils.hideView(removeButton, Animations.getAlphaBottomOutAnimation());
			Utils.hideView(mAppContainer, Animations.getAlphaLeftOutAnimation());
			Utils.hideView(gestureHintFrameLayout);
//			Utils.hideView(mAppView, Animations.getAlphaZoomOutAnimation());
			Utils.hideView(listMask);
			Utils.showView(mainRelative, Animations.getAlphaZoomInAnimation());
			Utils.showView(clockText, Animations.getAlphaTopInAnimation());
			Utils.showView(dock_shortcut, Animations.getAlphaBottomInAnimation());
			Utils.showView(allApp, Animations.getAlphaBottomInAnimation());
			Utils.showView(panelFrameLayout, Animations.getAlphaRightInAnimation());
//			Utils.showView(supad3DView);
			if (curPanel != null) {
				lastPanel = curPanel;
				curPanel.setOpen(false, true);
				curPanel = null;
			}
			supad3DView.startShowing(true);
			supad3DView.setMoveable(true);
//			supad3DView.startShrinking();
			switchingToSearch = true;
			status = MainActivityStatuses.DEFAULT_DESKTOP;
			Log.d(TAG, "change to default desktop");
			break;
		case SEARCHING_OR_SWIPING:
			Utils.showView(backgroundMaskView, Animations.getAlphaInAnimation());
			Utils.showView(searchText, Animations.getAlphaTopInAnimation());
			Utils.showView(listMask, Animations.getAlphaInAnimation());
			Utils.hideView(clockText, Animations.getAlphaTopOutAnimation());
			Utils.hideView(dock_shortcut, Animations.getAlphaBottomOutAnimation());
			Utils.hideView(allApp, Animations.getAlphaBottomOutAnimation());
			Utils.hideView(panelFrameLayout, Animations.getAlphaRightOutAnimation());
			searchText.setText("");
			supad3DView.setMoveable(true);
			supad3DView.startExpanding();
			switchTime = new Date();
			switchTime.setTime(switchTime.getTime() + 500);
			status = MainActivityStatuses.SEARCHING_OR_SWIPING;
			Log.d(TAG, "change to searching or swiping");
			break;
		case SEARCHING:
			supad3DView.setMoveable(false);
			Utils.showView(removeButton, Animations.getAlphaBottomInAnimation());
			Utils.showView(dialOutImage, Animations.getAlphaBottomInAnimation());
//			showView(hideSupadButton, Animations.getAlphaBottomInAnimation());
			status = MainActivityStatuses.SEARCHING;
			Log.d(TAG, "change to searching");
			break;
		case SWIPING:
			Utils.showView(gestureHintFrameLayout, Animations.getAlphaInAnimation());
			supad3DView.setMoveable(true);
			status = MainActivityStatuses.SWIPING;
			Log.d(TAG, "change to swiping");
			break;
		case ALL_APP_SHOWN:
			Utils.showView(mAppView.findViewById(R.id.my_Grid));
			Utils.showView(mAppView, Animations.getAlphaZoomInAnimation());
			Utils.hideView(mainRelative, Animations.getAlphaZoomOutAnimation());
			Utils.hideView(dock_shortcut, Animations.getAlphaBottomOutAnimation());
//			Utils.hideView(supad3DView);
			if (supad3DView.isEnabled()) {
				supad3DView.startHiding();
			}
			status = MainActivityStatuses.ALL_APP_SHOWN;
			Log.d(TAG, "change to all app shown");
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle() == getResources().getText(R.string.system_setting)) {
			startActivity(new Intent(Settings.ACTION_SETTINGS));
		}
		else if (item.getTitle() == getResources().getText(R.string.wallpaper_setting)) {
			startActivity(new Intent(Intent.ACTION_SET_WALLPAPER));  
		}
		else if (item.getTitle() == getResources().getText(R.string.quit_app)) {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.getPackageManager().clearPackagePreferredActivities(this.getPackageName());
			startActivity(startMain);
		}
		else if (item.getTitle() == getResources().getText(R.string.setting_gesture_title)) {
			startActivity(new Intent().setClass(this, GestureMgrActivity.class));
		}
		else if (item.getTitle() == getResources().getText(R.string.panel_setting)) {
			Intent intent = new Intent();
			ArrayList<CharSequence> list = new ArrayList<CharSequence>();
			for (ContentValues cv: mContentList) {
				list.add(cv.getAsString("name"));
			}
			intent.putCharSequenceArrayListExtra("names", list);
			startActivity(intent.setClass(this, PanelMgrActivity.class));
		}
		else if (item.getTitle() == getResources().getText(R.string.add_panel_setting)) {
			if (mPanels.size() < MAX_FOLDER_SIZE) {
				makeNewPanel();
				addFolder();
				adjustHanlePosition();
			} else {
				Toast.makeText(this, R.string.panel_full_toast, Toast.LENGTH_SHORT).show();
			}
		}
		return true;
	}
	private int[] panelHandlerResId = new int[] {
		R.drawable.panel_handler_selector_1,
		R.drawable.panel_handler_selector_2,
		R.drawable.panel_handler_selector_3,
		R.drawable.panel_handler_selector_4,
		R.drawable.panel_handler_selector_5,
	};
	private void adjustHanlePosition() {
		int len = mPanels.size();
		int handleHeight = mPanels.get(0).getHandle().getLayoutParams().height;
		for (int i = 0; i < len; i++) {
			View handle = mPanels.get(i).getHandle();
			mPanels.get(i).setHandleBackgroud(getResources().getDrawable(panelHandlerResId[i]));
			handle.setBackgroundDrawable(getResources().getDrawable(panelHandlerResId[i]));
			LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) handle.getLayoutParams();
			params.setMargins(0, 0, 0,handleHeight * (i - 2));
			handle.setLayoutParams(params);
		}
	}
	private HorizontalListView mAppContainer = null;
	private List<ContentValues> mContentList = null;
	private FrameLayout panelFrameLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "oncreate");
		if (theInstance == null) {
			theInstance = MainActivity.this;
		}
		sp = getSharedPreferences("isFirst", Context.MODE_PRIVATE);
		if (sp.getBoolean("isFirst", true)) {
			Log.d("first", "first time");
			showGuideViews();
			sp.edit().putBoolean("isFirst", false);
			sp.edit().commit();
		}
		
		//Initiate views
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindowManager().getDefaultDisplay().getMetrics(disMetrics);
		setContentView(R.layout.main);
		mInflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainRelative = (RelativeLayout) findViewById(R.id.mainRelative);
		supadFrameLayout = (FrameLayout) findViewById(R.id.supadFrameLayout);
		listMask = (ImageView) findViewById(R.id.listMask);
		supad3DView = (Supad3DView) findViewById(R.id.supad3DView);
		Log.d(TAG, supad3DView.getHeight() + "");
		clockText = (TextView) findViewById(R.id.clockText);
		searchText = (TextView) findViewById(R.id.searchText);
		backgroundMaskView = (ImageView) findViewById(R.id.backgroundMask);
		dock_shortcut = findViewById(R.id.dock_shortcut);
		mAppContainer = (HorizontalListView) findViewById(R.id.app_container);
		panelFrameLayout = (FrameLayout) findViewById(R.id.PanelFrameLayout);
		searchLists = (LinearLayout) findViewById(R.id.searchLists);
		conList = (ConListView)findViewById(R.id.conList);
		appList = (AppListView)findViewById(R.id.appList);
		mAppView = (AllAppView)findViewById(R.id.myGrid);
		gestureHintFrameLayout = (GestureHintFrameLayout) findViewById(R.id.gestureHintFrameLayout);
		updateTime();
		initDockBar();
		initSupad();
//		initAppContainer();
		searchText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (status == MainActivityStatuses.SEARCHING) {
					changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
				}
			}
		});
		
		//Initiate search data
		Log.d(TAG, searchLists.getHeight() + "");
		dc = new DataCollector(this);
		ListUpdater.instance().setResult(dc.getData());
		conAdapter = new SearchListAdapter(getApplicationContext(), ListTags.CON);
		appAdapter = new SearchListAdapter(getApplicationContext(), ListTags.APP);
		conList.setAdapter(conAdapter);
		appList.setAdapter(appAdapter);
		
		//Initiate status
		initStatus();
		Log.d(TAG, conList.getHeight() + "");
		Log.d(TAG, supad3DView.getHeight() + "");
		
		//Initiate folders
		mContentList = DatabaseManager.getFolderNameAndID();
		if (mContentList.size() == 0) {
			makeNewPanel();
			addFolder();
			adjustHanlePosition();
		} else {
			for (ContentValues cv : mContentList) {
				Panel panel = makeNewPanel();
				List<String> pkName = DatabaseManager.getAppsInFolder(cv.getAsLong("id"));
				TextView text = (TextView) ((RelativeLayout)panel.getContent()).findViewById(R.id.panel_name);
				text.setText(cv.getAsString("name"));
				text =  (TextView) ((RelativeLayout)panel.getContent()).findViewById(R.id.panel_edit_name);
				text.setText(cv.getAsString("name"));
				DraggableGridView gv = (DraggableGridView) ((RelativeLayout)panel.getContent()).findViewById(R.id.panel_drawer_content_grid);
				int index = mPanels.indexOf(panel);
				SlideAdapter slideAdapter = mAdapters.get(index);
				AllAppAdapter allAdapter = mAllAdapters.get(index);
				for (String string : pkName) {
					AllAppListItem item = allAdapter.getItemByPackageName(string);
					List<AllAppListItem> list = slideAdapter.getList();
					AllAppListItem itemCopy = new AllAppListItem(getPackageManager(), item.resolveInfo);
					list.add(itemCopy);
					gv.addView(bindFavoriteView(itemCopy));
//					gv.addView(slideAdapter.bindView(item, gv));
					allAdapter.removeItem(item);
				}
//				slideAdapter.notifyDataSetChanged();
				allAdapter.notifyDataSetChanged();
			}
			adjustHanlePosition();
		}
		lastPanel = (Panel) mPanelFrame.getChildAt(0);
		
		mainHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.arg1) {
				case Messages.RESULT_LIST_UPDATE_FINISH:
					if (!updatingListLocked && status == MainActivityStatuses.SEARCHING) {
						showSearchLists();
						if (!conAdapter.isEmpty()) {
							conAdapter.notifyDataSetChanged();
						} else {
							conAdapter.notifyDataSetInvalidated();
						}
						if (!appAdapter.isEmpty()) {
							appAdapter.notifyDataSetInvalidated();
						} else {
							appAdapter.notifyDataSetChanged();
						}
					}
					break;
				case Messages.HIDE_SUPAD3DVIEW:
					Utils.hideView(supad3DView);
					break;
				case Messages.SHOW_SUPAD3DVIEW:
					Utils.showView(supad3DView);
					break;
				case Messages.START_HIDING_SUPAD3DVIEW:
					supad3DView.startHiding();
					Utils.hideView(listMask);
					break;
				case Messages.START_PANELCONTENT_ADJUST:
					if (mPanels != null) {
						for (Panel mp : mPanels) {
							DraggableGridView favorite = (DraggableGridView) mp.findViewById(R.id.panel_drawer_content_grid);
							View v = favorite.getChildAt(0);
							if (v != null) {
								int ch = v.getHeight();
								int space = (favorite.getHeight() - 3 * ch - v.getPaddingBottom() * 3 - favorite.getPaddingBottom() * 2) / 2;
								//					space = (space > 0) ? space : 0;
		//							favorite.setVerticalSpacing(space);
								mPanelContentSpacing = space;
							}
						}
					}
				}
			}
		};
		Thread preloadAllAppThread = new Thread() {
			public void run() {
				AllAppAdapter.preload();
			}
		};
		preloadAllAppThread.start();
		mGesture = new GestureDetector(this, new HomeGestureListener());
	}
//	private void initAppContainer() {
//		LayoutTransition transition = new LayoutTransition();
//        PropertyValuesHolder pvhLeft =
//                PropertyValuesHolder.ofInt("left", 0, 1);
//        PropertyValuesHolder pvhTop =
//                PropertyValuesHolder.ofInt("top", 0, 1);
//        PropertyValuesHolder pvhRight =
//                PropertyValuesHolder.ofInt("right", 0, 1);
//        PropertyValuesHolder pvhBottom =
//                PropertyValuesHolder.ofInt("bottom", 0, 1);
//        Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
//        Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
//        Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
//        PropertyValuesHolder pvhRotation =
//                PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
//        Animator customChangingDisappearingAnim = ObjectAnimator.ofPropertyValuesHolder(
//                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation).
//                setDuration(transition.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
//        customChangingDisappearingAnim.addListener(new AnimatorListenerAdapter() {
//            public void onAnimationEnd(Animator anim) {
//                View view = (View) ((ObjectAnimator) anim).getTarget();
//                view.setRotation(0f);
//            }
//        });	
//        Animator customDisappearingAnim = ObjectAnimator.ofFloat(null, "rotationX", 0f, 90f).
//                setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));
//        customDisappearingAnim.addListener(new AnimatorListenerAdapter() {
//            public void onAnimationEnd(Animator anim) {
//                View view = (View) ((ObjectAnimator) anim).getTarget();
//                view.setRotationX(0f);
//            }
//        }); 
////		transition.setAnimator(LayoutTransition.CHANGE_APPEARING, animator);
//		transition.setAnimator(LayoutTransition.DISAPPEARING, customDisappearingAnim);
//		transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, customChangingDisappearingAnim);
//		mAppContainer.setLayoutTransition(transition);
//	}
//	
	private int mPanelContentSpacing = -1;
	/**
	 * Add a folder
	 */
	private void addFolder() {
		String name = getResources().getString(R.string.folder_name);
		long folderId = DatabaseManager.addFolder(name);
		ContentValues cv = new ContentValues();
		cv.put("id", folderId);
		cv.put("name", name);
		mContentList.add(cv);
	}
	public List<SlideAdapter> getSlideAdapters() {
		return mAdapters;
	}
	public void slideAdapterRemoveItem(int groupPosition, int childPosition) {
		SlideAdapter sAdapter = mAdapters.get(groupPosition);
		AllAppListItem item = sAdapter.getList().get(childPosition);
		sAdapter.getList().remove(childPosition);
		DraggableGridView dgv = (DraggableGridView) mPanels.get(mAdapters.indexOf(sAdapter)).findViewById(R.id.panel_drawer_content_grid);
		dgv.removeViewAt(childPosition);
//		sAdapter.notifyDataSetChanged();
		ContentValues cv = mContentList.get(groupPosition);
		DatabaseManager.delAppInFolder(item.packageName, cv.getAsLong("id"));
//		mAllAdapters.get(groupPosition).addItem(item);
//		mAppContainer.isShown();
	}
	public void removeSlideAdapter(int which) {
		mAdapters.remove(which);
		View view = mPanels.get(which);
		mPanels.remove(which);
		mPanelFrame.removeView(view);
		mAllAdapters.remove(which);
		ContentValues cv = mContentList.get(which);
		DatabaseManager.delFolderByID(cv.getAsLong("id"));
		mContentList.remove(cv);
		adjustHanlePosition();
	}
	List<SlideAdapter> mAdapters = new ArrayList<SlideAdapter>();
	List<PanelSlideView> mPanels = new ArrayList<PanelSlideView>();
	List<AllAppAdapter> mAllAdapters = new ArrayList<AllAppAdapter>();
	LayoutInflater mInflator = null;
	private Panel curPanel = null;
	private Panel lastPanel = null;
	private FrameLayout mPanelFrame = null;
	private RelativeLayout mainRelative = null;
	private AllAppView mAppView = null;
	private GestureHintFrameLayout gestureHintFrameLayout = null;
	private GestureDetector mGesture;
	private Panel makeNewPanel() {
		Log.d("speed", "make start");
		mPanelFrame = (FrameLayout)findViewById(R.id.PanelFrameLayout);
		mInflator.inflate(R.layout.panel_layout, mPanelFrame);
		final PanelSlideView newPanel = (PanelSlideView) mPanelFrame.getChildAt(mPanelFrame.getChildCount() - 1);
		RelativeLayout content = (RelativeLayout) newPanel.getContent();
		final DraggableGridView favorite = (DraggableGridView) content.findViewById(R.id.panel_drawer_content_grid);
		final SlideAdapter adp = new SlideAdapter(this);
		newPanel.setOnPanelListener(new OnPanelListener() {
			@Override
			public void onPanelOpened(Panel panel) {
				int index = mPanels.indexOf(panel);
				for (int i = 0 ; i < mPanels.size(); i++) {
					if (i != index) {
						Panel tPanel = mPanels.get(i);
						if (tPanel.isOpen()) {
							tPanel.setOpen(false, true);
						} else {
							tPanel.bringToFront();
						}
					}
				}
//				Utils.hideView(supad3DView, Animations.getAlphaBottomOutAnimation());
				if (supad3DView.isEnabled()) {
					supad3DView.startHiding();
				}
				Utils.hideView(supad3DView, Animations.getAlphaBottomOutAnimation());
				curPanel = panel;
			}
			
			@Override
			public void onPanelClosed(Panel panel) {
				panel.bringToFront();
			}

			@Override
			public void onPanelTouch(Panel panel) {
				int index = mPanels.indexOf(panel);
				if (mAppContainer.isShown()) {
					if (supad3DView.isEnabled()) {
						supad3DView.startHiding();
					}
					Utils.hideView(mAppContainer, Animations.getAlphaLeftOutAnimation());
				}
				for (int i = 0 ; i < mPanels.size(); i++) {
					if (i != index) {
						Panel tPanel = mPanels.get(i);
						if (tPanel.isOpen()) {
							tPanel.setOpen(false, true);
						} else {
							tPanel.bringToFront();
						}
					}
				}
//				Utils.hideView(supad3DView, Animations.getAlphaBottomOutAnimation());
//				supad3DView.startHiding();
				if (supad3DView.isEnabled()) {
					supad3DView.startHiding();
				}
				curPanel = panel;
				((PanelSlideView)curPanel).change2Text();			
				Message msg = MainActivity.mainHandler.obtainMessage();
				msg.arg1 = MainActivity.Messages.START_PANELCONTENT_ADJUST;
				MainActivity.mainHandler.sendMessage(msg);
			}
		});
		mAdapters.add(adp);
//		favorite.setAdapter(adp);
//		if (mPanelContentSpacing != -1)	 
//			favorite.setVerticalSpacing(mPanelContentSpacing);
		favorite.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!mAppContainer.isShown()) {
					Intent intent = new Intent();
					AllAppListItem item = adp.getList().get(arg2);
					intent.setClassName(item.packageName, item.className);
					if (item.extras != null) {
						intent.putExtras(item.extras);
					}
					startActivity(intent);
				} else {
//					adp.getList().remove(arg2);
//					slideAdapterRemoveItem(mAdapters.indexOf(adp), arg2);
				}
			}
		});
		favorite.setOnRearrangeListener(new OnRearrangeListener() {
			
			@Override
			public void onRearrange(int oldIndex, int newIndex) {
				int index = mPanels.indexOf(newPanel);
				List<AllAppListItem> list = mAdapters.get(index).getList();
				AllAppListItem item = list.get(oldIndex);
				list.remove(oldIndex);
				list.add(newIndex, item);
			}
		});
		mPanels.add(newPanel);	
	
		
		final AllAppAdapter allAdapter = new AllAppAdapter(this);
		allAdapter.removeItems(adp.getList());
		allAdapter.setInflateId(R.layout.all_app_horizontal_list_item);
		allAdapter.setIconPosition(AllAppAdapter.HORIZONTAL_ITEM);
		mAllAdapters.add(allAdapter);
		ImageButton addButton = (ImageButton) content.findViewById(R.id.panel_add_favorite_button);
		addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mAppContainer.isShown()) {
					Animation anim = Animations.getAlphaLeftOutAnimation();
					anim.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							hideSupadButton.setEnabled(false);
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							hideSupadButton.setEnabled(true);
						}
					});
					Utils.hideView(mAppContainer, anim);
//					changeStatus(MainActivityStatuses.PANEL_OPEN);
				} else {
//					changeStatus(MainActivityStatuses.SELECTED_LIST_OPEN);
					Utils.showView(mAppContainer, Animations.getAlphaRightInAnimation());
					mAppContainer.setAdapter(allAdapter);
					mAppContainer.bringToFront();
//					Utils.showView(mAppContainer, Animations.getAlphaLeftInAnimation());
					mAppContainer.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View view, int position,
								long arg3) {
							if (adp.getCount() < MAX_FAVORITE_CONTAIN) {
								AllAppListItem item = allAdapter.itemForPosition(position);
								final List<AllAppListItem> list = adp.getList();
								final ContentValues cv = mContentList.get(mAdapters.indexOf(adp));
								if (!list.contains(item)) {
									final TextView v = (TextView) view;
									Animation anim = Animations.getZoomInDisappearAnimation();
									anim.setDuration(100);
									v.startAnimation(anim);
									final AllAppListItem itemCopy = new AllAppListItem(MainActivity.this.getPackageManager(), item.resolveInfo);
									final int pos = position;
									anim.setAnimationListener(new AnimationListener() {

										@Override
										public void onAnimationStart(Animation animation) {
										}

										@Override
										public void onAnimationRepeat(Animation animation) {
										}

										@Override
										public void onAnimationEnd(Animation animation) {
											list.add(itemCopy);
//											favorite.addView(adp.bindView(itemCopy, favorite));
											favorite.addView(bindFavoriteView(itemCopy));
											DatabaseManager.addAppInFolder(itemCopy.packageName, cv.getAsLong("id"));
											allAdapter.removeItem(pos);
										}
									});
								}
							} else {
								Context context = MainActivity.this;
								Animation anim = AnimationUtils.loadAnimation(context, R.anim.shake);
								view.startAnimation(anim);
								Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
								vibrator.vibrate(100);
							}
						}
					});
				}
			}
		});
//		favorite.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
//			
//			@Override
//			public void onChildViewRemoved(View parent, View child) {
//			}
//			
//			@Override
//			public void onChildViewAdded(View parent, View child) {
//				ViewGroup group = (ViewGroup) parent;
//				ContentValues cv = mContentList.get(mAdapters.indexOf(adp));
//				int count = DatabaseManager.getAppsInFolder(cv.getAsLong("id")).size();
//				if (count == group.getChildCount() && mAppContainer.isShown()) {
//					child.startAnimation(Animations.getZoomOutAppearAnimation());
//				}
//				Log.d(TAG, "favorite child count: " + group.getChildCount());
//				Log.d(TAG, "index of child: " + group.indexOfChild(child));
//			}
//		});
		favorite.setOnFlingListener(new OnFlingListener() {
			
			@Override
			public void onFling(View v, MotionEvent e) {
				if (newPanel.isShown()) {
					newPanel.setOpen(false, true);
					if (mAppContainer.isShown()) {
						Utils.hideView(mAppContainer, Animations.getAlphaLeftOutAnimation());
					}
				}
			}
		});
		newPanel.setOnChangeTextListener(new OnChangeTextListener() {
			@Override
			public void onChangeText(String text) {
				int index = mPanels.indexOf(newPanel);
				ContentValues cv = mContentList.get(index);
				cv.put("name", text);
				DatabaseManager.updateFolderNameByID(cv.getAsLong("id"), text);
			}
		});
		Log.d("speed", "done");
		return newPanel;
	}
	private View bindFavoriteView(AllAppListItem item) {
		ImageView view = new ImageView(this);
		int size = 120;
		Bitmap bitmap = Bitmap.createBitmap(size, size * 3 / 2, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		item.icon = item.resolveInfo.loadIcon(getPackageManager());
		Drawable drawable = AllAppAdapter.getIconResizer().createIconThumbnail(item.icon);
//		item.icon.setBounds(0, 0, 100, 100);
//		item.icon.draw(canvas);
		canvas.drawBitmap(Utils.drawableToBitmap(drawable), (size - SuApp.ALL_APP_ICON_SIZE) / 2, (size -  SuApp.ALL_APP_ICON_SIZE) / 2, null);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(size / 6);
		paint.setTextAlign(Align.CENTER);
//		String label = Utils.makeStringComfortable(item.label.toString(), 9);
		List<String> label = Utils.makeMultiLineString(item.label.toString(), 10);
//		StringBuffer label = new StringBuffer(item.label.toString());
//		if (label.length() > 10) {
//			label.delete(10, label.length());d
////			label.substring(0, 9);
//			label.append("...");
//		} else if (label.toString().getBytes().length > 6) {
//			label.delete(6, label.length());
////			label.substring(0, 9);
//			label.append("...");
//		}
		int y = size * 5 / 4;
		for (int i = 0; i < Math.min(2, label.size()); i++) {
			canvas.drawText(label.get(i), size / 2, y, paint);
			y += paint.getTextSize();
		}
//		canvas.drawText(label, size / 2, size * 5 / 4, paint);
		view.setImageDrawable(new BitmapDrawable(bitmap));
		return view;
	}
	private void showGuideViews() {
		Toast.makeText(this, R.string.guide_1, Toast.LENGTH_LONG).show();
		Toast.makeText(this, R.string.guide_3, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		return super.onTouchEvent(event);
		return mGesture.onTouchEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.registerReceiver(clockOutOfTimeReceiver, timeFilter);
//		this.registerReceiver(addPackageReceiver, addPackageFilter);
//		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
//		intentFilter.addDataScheme("package");
//		this.registerReceiver(removePackageReceiver, intentFilter);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
//		if (!returnFromSelf) {
//			overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
//		} else {
//			returnFromSelf = false;
//		}
		changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
//		initStatus();
		updateTime();
	}
	@Override
	protected void onStop() {
		this.unregisterReceiver(clockOutOfTimeReceiver);
//		this.unregisterReceiver(addPackageReceiver);
//		this.unregisterReceiver(removePackageReceiver);
		super.onStop();
	}
	@Override
	public void onBackPressed() {
		if (status == MainActivityStatuses.SEARCHING) {
			if (searchText.length() > 1) {
				searchText.setText(searchText.getText().subSequence(0, searchText.length() - 1));
				updateSearchResult(KeyChangeType.NOTAPPEND);
			} else {
				changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
			}
		} else {
			if (mAppContainer.isShown()) {
				Utils.hideView(mAppContainer, Animations.getAlphaLeftOutAnimation());
			} else if (curPanel != null && curPanel.isOpen()) {
				lastPanel = curPanel;
				curPanel.setOpen(false, true);
				curPanel = null;
			} else if (supad3DView.isEnabled()) {
				supad3DView.startHiding();
			} else {
				changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
			}
		}
	}
	public int getIconSize(int pxSize) {
		return (int) (pxSize * disMetrics.density + 0.5);
	}
	class HomeGestureListener extends SimpleOnGestureListener {

		private static final int MIN_FLING_DISTANCE = 100;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
			int dx = 0, dy = 0;
			int absdx = 0, absdy = 0;
			if (e1 != null) {
				x1 = (int) e1.getX();
				y1 = (int) e1.getY();
			}
			if (e2 != null) {
				x2 = (int) e2.getX();
				y2 = (int) e2.getY();
			}
			dx = x1 - x2;
			dy = y1 - y2;
			absdx = Math.abs(x1 - x2);
			absdy = Math.abs(y1 - y2);
			if (dx > MIN_FLING_DISTANCE && absdy < 2 * MIN_FLING_DISTANCE) {
				if (supad3DView.isEnabled()) {
					supad3DView.startHiding();
				}
				if (lastPanel != null) {
					curPanel = lastPanel;
					curPanel.setOpen(true, true);
				}
				return true;
			} else if (curPanel != null && -dx > MIN_FLING_DISTANCE && absdy < 2 * MIN_FLING_DISTANCE) {
				if (mAppContainer.isShown()) {
					Utils.hideView(mAppContainer, Animations.getAlphaLeftOutAnimation());
				}
				if (supad3DView.isEnabled()) {
					supad3DView.startHiding();
				}
				lastPanel = curPanel;
				curPanel.setOpen(false, true);
				curPanel = null;
				return true;
			} else if (absdy > MIN_FLING_DISTANCE && absdx < 2 * MIN_FLING_DISTANCE) {
				toggleSupad3DView();
				return true;
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
	}
	private void toggleSupad3DView() {
		if (supad3DView.isEnabled()) {
			if (supad3DView.isAnimating() && !supad3DView.isShowing()){
				if (status == MainActivityStatuses.SEARCHING) {
					Utils.showView(listMask, Animations.getAlphaInAnimation()); 
					supad3DView.startShowing(false);
				} else {
					supad3DView.startShowing(true);
				}
			} else {
				supad3DView.startHiding();
				if (status == MainActivityStatuses.SEARCHING) {
					Utils.hideView(listMask, Animations.getAlphaOutAnimation()); 
				}
			}
		} else {
			if (mAppContainer.isShown()) {
				Utils.hideView(mAppContainer, Animations.getAlphaLeftOutAnimation());
			}
			if (curPanel != null) {
				lastPanel = curPanel;
				curPanel.setOpen(false, true);
				curPanel = null;
			}
			if (status == MainActivityStatuses.SEARCHING) {
				supad3DView.startShowing(false);
				Utils.showView(listMask, Animations.getAlphaInAnimation()); 
			} else {
				supad3DView.startShowing(true);
			}
		}
	}
}

