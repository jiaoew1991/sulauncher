package com.sulauncher.slidingbar;

import java.util.List;

import com.sulauncher.MainActivity;
import com.sulauncher.R;
import com.sulauncher.applicationbrowser.AllAppAdapter;
import com.sulauncher.applicationbrowser.AllAppListItem;
import com.sulauncher.widget.SwipeDeleteListView;
import com.sulauncher.widget.SwipeDeleteListView.OnItemDeleteListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PanelMgrSubActivity extends Activity {

	List<AllAppListItem> mList = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_title);
		SwipeDeleteListView mSwipeListView = (SwipeDeleteListView) findViewById(R.id.swipe_listview);
		Intent intent = this.getIntent();
		final int pos = intent.getIntExtra("position", 0);
		SlideAdapter sAdapter = MainActivity.getTheInstance().getSlideAdapters().get(pos);
		mList = sAdapter.getList();
		PhotoAdapter mAdapter = new PhotoAdapter(this);
		mSwipeListView.setAdapter(mAdapter);
		mSwipeListView.setOnItemDeleteListener(new OnItemDeleteListener() {
			
			@Override
			public void onItemDelete(int position) {
				MainActivity.getTheInstance().slideAdapterRemoveItem(pos, position);
			}
		});
	}
	class PhotoAdapter extends BaseAdapter {

		Context mContext;
		private LayoutInflater mInflater = null;
		public PhotoAdapter(Context context) {
			mContext = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView text = (TextView) mInflater.inflate(R.layout.all_app_linear_list_item, parent, false);
			AllAppListItem item = mList.get(position);
			AllAppListItem itemCopy = new AllAppListItem(mContext.getPackageManager(), item.resolveInfo);
			text.setText(itemCopy.label);
			if (itemCopy.icon == null) {
				Drawable drawable = itemCopy.resolveInfo.loadIcon(mContext.getPackageManager());
				if (drawable != null) {
					itemCopy.icon = AllAppAdapter.getIconResizer().createIconThumbnail(drawable);
					text.setCompoundDrawablesWithIntrinsicBounds(itemCopy.icon, null, null, null);
				}
			} else {
					text.setCompoundDrawablesWithIntrinsicBounds(itemCopy.icon, null, null, null);
			}
			return text;
		}
		
	}
}
