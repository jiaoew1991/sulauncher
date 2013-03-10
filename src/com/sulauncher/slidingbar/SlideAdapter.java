package com.sulauncher.slidingbar;

import java.util.ArrayList;
import java.util.List;

import com.sulauncher.R;
import com.sulauncher.applicationbrowser.AllAppAdapter;
import com.sulauncher.applicationbrowser.AllAppListItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SlideAdapter extends BaseAdapter {

	private List<AllAppListItem> mList = new ArrayList<AllAppListItem>();
	private Context mContext;
	private LayoutInflater inflater = null;
	
	public SlideAdapter(Context context) {
		super();
		mContext = context;
		inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public void setListData(List<AllAppListItem> list) {
		mList = list;
	}
	public List<AllAppListItem> getList() {
		return mList;
	}
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text = (TextView) inflater.inflate(R.layout.all_app_grid_list_item, parent, false);
		AllAppListItem item = mList.get(position);
		text.setText(item.label);
		if (item.icon == null) {
			Drawable drawable = item.resolveInfo.loadIcon(mContext.getPackageManager());
			if (drawable != null) {
				item.icon = AllAppAdapter.getIconResizer().createIconThumbnail(drawable);
				text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
			}
		} else {
				text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
		}
		return text;
	}

	public View bindView(int position, ViewGroup parent) {
//		TextView text = (TextView) inflater.inflate(R.layout.all_app_grid_list_item, null);
		AllAppListItem item = mList.get(position);
		return bindView(item, parent);
//		text.setText(item.label);
//		if (item.icon == null) {
//			Drawable drawable = item.resolveInfo.loadIcon(mContext.getPackageManager());
//			if (drawable != null) {
//				item.icon = AllAppAdapter.getIconResizer().createIconThumbnail(drawable);
//				text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
//			}
//		} else {
//				text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
//		}
//		return text;
	}
	public View bindView(AllAppListItem item, ViewGroup parent) {
		TextView text = (TextView) inflater.inflate(R.layout.all_app_grid_list_item, parent, false);
		text.setText(item.label);
		if (item.icon == null) {
			Drawable drawable = item.resolveInfo.loadIcon(mContext.getPackageManager());
			if (drawable != null) {
				item.icon = AllAppAdapter.getIconResizer().createIconThumbnail(drawable);
				text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
			}
		} else {
				text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
		}
		return text;
	}
}
