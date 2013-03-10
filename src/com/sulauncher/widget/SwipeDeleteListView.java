package com.sulauncher.widget;
 
 import com.sulauncher.R;

import android.animation.LayoutTransition;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
 
 public class SwipeDeleteListView extends ScrollView implements SwipeHelper.Callback {
 	private BaseAdapter mAdapter;
 	private SwipeHelper mSwipeHelper;
 	private LinearLayout mLinearLayout;
 	private Context mContext;
 
 	public SwipeDeleteListView(Context mContext, AttributeSet attrs) {
 		super(mContext, attrs, 0);
 		this.mContext = mContext;
 		float densityScale = getResources().getDisplayMetrics().density;
 		float pagingTouchSlop = ViewConfiguration.get(mContext)
 				.getScaledPagingTouchSlop();
 		mSwipeHelper = new SwipeHelper(SwipeHelper.X, this, densityScale,
 				pagingTouchSlop);
 	}
 
 	public void setAdapter(BaseAdapter Adapter) {
 		mAdapter = Adapter;
 		mAdapter.registerDataSetObserver(new DataSetObserver() {
             public void onChanged() {
                 update();
             }
 
             public void onInvalidated() {
                 update();
             }
         });
 		update();
 	}
 	
 	@Override
 	public void removeViewInLayout(final View view) {
 		dismissChild(view);
 	}
 
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 		return mSwipeHelper.onInterceptTouchEvent(ev)
 				|| super.onInterceptTouchEvent(ev);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 		return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
 	}
 
 	public void dismissChild(View v) {
 		mSwipeHelper.dismissChild(v, 0);
 	}
 
 	@Override
 	public View getChildAtPosition(MotionEvent ev) {
 		final float x = ev.getX() + getScrollX();
 		final float y = ev.getY() + getScrollY();
 		for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
 			View item = mLinearLayout.getChildAt(i);
 			if (item.getVisibility() == View.VISIBLE && x >= item.getLeft()
 					&& x < item.getRight() && y >= item.getTop()
 					&& y < item.getBottom()) {
 				return item;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public View getChildContentView(View v) {
 		return v;
 	}
 
 	@Override
 	public boolean canChildBeDismissed(View v) {
 		return true;
 	}
 
 	@Override
 	public void onBeginDrag(View v) {
 		requestDisallowInterceptTouchEvent(true);
 		v.setActivated(true);
 	}
 
 	@Override
 	public void onChildDismissed(View v) {
 		int index = mLinearLayout.indexOfChild(v);
 		mLinearLayout.removeView(v);
 		if (mListener != null) {
 			mListener.onItemDelete(index);
 		}
 	}
 
 	@Override
 	public void onDragCancelled(View v) {
 		v.setActivated(false);
 	}
 
 	
 	
 	@Override
 	protected void onFinishInflate() {
 		super.onFinishInflate();
 		setScrollbarFadingEnabled(true);
 		mLinearLayout = (LinearLayout) findViewById(R.id.linear_layout);
 	}
 	
 	
 	private void update()
 	{
         mLinearLayout.removeAllViews();
         for (int i = 0; i < mAdapter.getCount(); i++) {
             View old = null;
             if (i < mLinearLayout.getChildCount()) {
                 old = mLinearLayout.getChildAt(i);
                 old.setVisibility(View.VISIBLE);
             }
             final View view = mAdapter.getView(i, old, mLinearLayout);
 
             if (old == null) {
                 OnTouchListener noOpListener = new OnTouchListener() {
                     @Override
                     public boolean onTouch(View v, MotionEvent event) {
                         return true;
                     }
                 };
 
                 view.setOnClickListener(new OnClickListener() {
                     public void onClick(View v) {
                     }
                 });
                 // We don't want a click sound when we dimiss recents
                 view.setSoundEffectsEnabled(false);
 
                 OnClickListener launchAppListener = new OnClickListener() {
                     public void onClick(View v) {
                     }
                 };
 
                 OnLongClickListener longClickListener = new OnLongClickListener() {
                     public boolean onLongClick(View v) {
                         return true;
                     }
                 };
 
                 mLinearLayout.addView(view);
             }
         }
         for (int i = mAdapter.getCount(); i < mLinearLayout.getChildCount(); i++) {
             mLinearLayout.getChildAt(i).setVisibility(View.GONE);
         }
 	}
 	private OnItemDeleteListener mListener;
 	public void setOnItemDeleteListener(OnItemDeleteListener listener) {
 		mListener = listener;
 	}
 	public interface OnItemDeleteListener {
 		public void onItemDelete(int pos);
 	}
 }
 