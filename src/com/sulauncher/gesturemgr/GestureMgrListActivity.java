package com.sulauncher.gesturemgr;

import java.util.ArrayList;
import java.util.HashMap;

import com.sulauncher.R;
import com.sulauncher.Utils;
import com.sulauncher.applicationbrowser.AllAppAdapter;
import com.sulauncher.applicationbrowser.AllAppListItem;
import com.sulauncher.search.HanziToPinyin;
import com.sulauncher.search.HanziToPinyin.Token;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Selecting application part of Gesture management
 * @author jiaoew
 */
public class GestureMgrListActivity extends ListActivity {
	protected ListView mListView = null;
	private LinearLayout alphaContainer = null;
	private TextView mOverlay = null;
	protected AllAppAdapter mAdapter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_app_linear);
		setTitle(R.string.choose_app_title);
		mAdapter = new AllAppAdapter(this);
		mAdapter.setIconPosition(AllAppAdapter.LIST_ITEM);
		mAdapter.setInflateId(R.layout.all_app_linear_list_item);
		mListView = this.getListView();
		alphaContainer = (LinearLayout) findViewById(R.id.alphabetBar);
		mOverlay = (TextView) findViewById(R.id.linear_overlay);
		initContainer();
		setListAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent();
				AllAppListItem item = mAdapter.itemForPosition(arg2);
				intent.putExtra("packageName", item.packageName);
				intent.putExtra("className", item.className);
				intent.setClass(GestureMgrListActivity.this, GestureMgrDrawActivity.class);
				startActivity(intent);
				GestureMgrListActivity.this.finish();
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
//					Utils.hideView(mOverlay);
					mOverlay.setVisibility(View.INVISIBLE);
				} else {
					mOverlay.setVisibility(View.VISIBLE);
//					Utils.showView(mOverlay);
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				AllAppListItem item = mAdapter.itemForPosition(firstVisibleItem);
				ArrayList<Token> tokens = HanziToPinyin.getInstance().get(item.label.toString());
				Token token = tokens.get(0);
				String text;
				if (Token.PINYIN == token.type) {
					text = token.target.charAt(0) + "";
				} else
					text = "#";
				mOverlay.setText(text);
			}
		});
	}
	HashMap<String, Integer> map = null;
	private void initContainer() {
		String[] alphas = {
				"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
				"R", "S", "T", "U", "V", "W", "X", "Y", "Z"
		};
		final TextView[] mTextView = new TextView[27];
		map = mAdapter.getNamePosMap();
		Log.d("debug", map.toString() + " " + map.size());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.weight = 1;
		for (int i = 0; i < mTextView.length; i++) {
			mTextView[i] = new TextView(this);
			mTextView[i].setLayoutParams(params);
			mTextView[i].setText(alphas[i]);
			mTextView[i].setGravity(Gravity.CENTER);
			mTextView[i].setTextSize(14);
			alphaContainer.addView(mTextView[i]);
		}
		alphaContainer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				LinearLayout linear = (LinearLayout) v;
				int itemHeight = linear.getHeight() / 27;
				int itemPos = (int) (event.getY() / itemHeight) ;
				if (itemPos == 0) {
					mListView.setSelectionFromTop(map.get("#"), 0);
				}
				else {
					int id = 'A' + itemPos - 1;
					while (map.get(String.valueOf((char)(id))) == null) {
						id--;
					}
					mListView.setSelection(map.get(String.valueOf((char)(id))));
				}
				return true;
			}
		});
	}

}
