package com.sulauncher.search;

import com.sulauncher.MainActivity;
import com.sulauncher.db.DatabaseManager;
import com.sulauncher.search.ListWithTag.ListTags;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * List view for applications search result
 * @author YuMS
 */
public class AppListView extends SearchListView{
	public AppListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public AppListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
//	private void init(final Context context) {
//		setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				GradientTextView touchingChildView;
//				GradientTextView preTouchingChildView;
//				float nowY, nowX;
//				switch (event.getAction()) {
//				case MotionEvent.ACTION_UP:
//					break;
//				case MotionEvent.ACTION_DOWN:
//				case MotionEvent.ACTION_MOVE:
//					Message msg = MainActivity.mainHandler.obtainMessage();
//					msg.arg1 = MainActivity.Messages.START_HIDING_SUPAD3DVIEW;
//					MainActivity.mainHandler.sendMessage(msg);
//					nowY = event.getY();
//					nowX = event.getX();
//					if (nowY < 0 || nowX < 0) {
//						nowTouch = -1;
//					} else {
//						if (oneHeight == 0) {
//							if (getChildCount() != 0) {
//								oneHeight = (getBottom() - getTop()) / getChildCount();
//							}
//						}
//						if (oneHeight != 0) {
//							nowTouch = (int)(nowY / oneHeight);
//						} else {
//							nowTouch = -1;
//						}
//					}
//					if (nowTouch != preTouch) {
//						preTouchingChildView = (GradientTextView) v.getTag();
//						if (preTouchingChildView != null) {
//							preTouchingChildView.setTouching(false, preTouchingChildView);
//						}
//						if (nowTouch >= 0 && nowTouch < getAdapter().getCount()) {
//							touchingChildView = (GradientTextView) getChildAt(nowTouch);
//							if (touchingChildView != null) {
//								touchingChildView.setTouching(true, touchingChildView);
//								v.setTag(touchingChildView);
//							}
//							preTouch = nowTouch;
//						} else {
//							preTouch = -1;
//							v.setTag(null);
//						}
//					}
//					break;
//				}
//				return true;
//			}
//		});
//	}
	@Override
	protected void onActionUp(Context context, View view, GradientTextView preTouchingChildView) {
		preTouchingChildView = (GradientTextView) view.getTag();
		Log.d("applistview", "UP");
		if (preTouchingChildView != null) {
			try {
				preTouchingChildView.setTouching(false, preTouchingChildView);
				DataItem item = mListWithTag.getList().get(preTouch);
				preTouch = -1;
				Log.d("applistview", "is item app?");
				if (item instanceof AppItem) {
					Log.d("applistview", "yes");
					Intent launchIntent = new Intent();  
					AppItem applicationItem = (AppItem)item;
					String intent = applicationItem.resolveInfo.activityInfo.packageName;
					item.visitTimes++;
					DatabaseManager.touchApp(intent);
					launchIntent.setComponent(new ComponentName(applicationItem.resolveInfo.activityInfo.packageName,applicationItem.resolveInfo.activityInfo.name));  
					context.startActivity(launchIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		view.setTag(null);
	}
	@Override
	protected void onPickUpList() {
		mData = ListUpdater.instance().getResult();
		for (ListWithTag it : mData) {
			if (it.getTag() == ListTags.APP) {
				mListWithTag = it;
				break;
			}
		}
	}
}