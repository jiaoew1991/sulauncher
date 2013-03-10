package com.sulauncher.search;

import java.util.LinkedList;

import com.sulauncher.MainActivity;
import com.sulauncher.db.DatabaseManager;
import com.sulauncher.search.ListWithTag.ListTags;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * The ListView of Searhing result lists
 * @author YuMS
 */
public abstract class SearchListView extends ListView{
	private static final String TAG = "SearchListView";
	protected int oneHeight;
	protected int height;
	protected LinkedList<ListWithTag> mData;
	protected ListWithTag mListWithTag;
	protected int preTouch = -1;
	protected int nowTouch;
	protected Context context;
	public SearchListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public SearchListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	protected abstract void onActionUp(final Context context, View view, GradientTextView preTouchingChildView);
	protected abstract void onPickUpList();
	protected void init(final Context context, AttributeSet attrs) {
		Log.d(TAG, "list view init");
		oneHeight = 0;
		setChoiceMode(CHOICE_MODE_SINGLE);
		setClickable(false);
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.d(TAG, MainActivity.getTheInstance().getSearchLists().getHeight() + "");
				GradientTextView touchingChildView = null;
				GradientTextView preTouchingChildView = null;
				float nowY, nowX;
				if (mListWithTag == null) {
					onPickUpList();
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					onActionUp(context, v, preTouchingChildView);
					break;
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					Message msg = MainActivity.mainHandler.obtainMessage();
					msg.arg1 = MainActivity.Messages.START_HIDING_SUPAD3DVIEW;
					MainActivity.mainHandler.sendMessage(msg);
					nowY = event.getY();
					nowX = event.getX();
					if (nowY < 0 || nowX < 0) {
						nowTouch = -1;
					} else {
						if (oneHeight == 0) {
							if (getChildCount() != 0) {
								oneHeight = (getBottom() - getTop()) / getChildCount();
							}
						}
						if (oneHeight != 0) {
							nowTouch = (int)(nowY / oneHeight);
						} else {
							nowTouch = -1;
						}
					}
					if (nowTouch != preTouch) {
						preTouchingChildView = (GradientTextView) v.getTag();
						if (preTouchingChildView != null) {
							preTouchingChildView.setTouching(false, preTouchingChildView);
						}
						if (nowTouch >= 0 && nowTouch < getAdapter().getCount()) {
							touchingChildView = (GradientTextView) getChildAt(nowTouch);
							if (touchingChildView != null) {
								touchingChildView.setTouching(true, touchingChildView);
								v.setTag(touchingChildView);
							}
							preTouch = nowTouch;
						} else {
							preTouch = -1;
							v.setTag(null);
						}
					}
					break;
				}
				return true;
			}
		});
	}
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		SearchListAdapter mAdapter = (SearchListAdapter) adapter;
		mAdapter.setNoOverlapNumber(4);
		Log.d(TAG, MainActivity.getTheInstance().getSearchLists().getHeight() + "");
	}
}
