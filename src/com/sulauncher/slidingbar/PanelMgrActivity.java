package com.sulauncher.slidingbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sulauncher.MainActivity;
import com.sulauncher.R;
import com.sulauncher.applicationbrowser.AllAppListItem;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class PanelMgrActivity extends ListActivity{

//	private static final String NAME = "name";
//	private static final String ICON = "icon";
//	List<List<Map<String, Object>>> childData = null;
	BaseAdapter exAdapter  = null;
//	List<SlideAdapter> sAdapters = null;
	List<String> groupData = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(R.string.panel_setting);
		Intent intent = getIntent();
		ArrayList<CharSequence> names = intent.getCharSequenceArrayListExtra("names");
//		sAdapters = MainActivity.getTheInstance().getSlideAdapters();
		groupData = new ArrayList<String>();
//		childData = new ArrayList<List<Map<String,Object>>>();
//		int index = 0;
		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			CharSequence name = (CharSequence) iterator.next();
			groupData.add(name.toString());
		}
//		for (SlideAdapter slideAdapter : sAdapters) {
//			List<AllAppListItem> items = slideAdapter.getList();
//			Map<String, String> tMap = new HashMap<String, String>();
//			tMap.put(NAME, names.get(index).toString());
//			groupData.add(names.get(index).toString());
			
//			List<Map<String, Object>> curChildList = new ArrayList<Map<String,Object>>();
//			for (AllAppListItem item: items) {
//				Map<String, Object> tChildMap = new HashMap<String, Object>();
//				tChildMap.put(NAME, item.label);
//				tChildMap.put(ICON, item.icon);
//				curChildList.add(tChildMap);
//			}
//			childData.add(curChildList);
//			index ++;
//		}
//		exAdapter = new SimpleAdapter(this, groupData, resource, NAME, to);
		exAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, groupData);
		this.setListAdapter(exAdapter);
		ListView mList = getListView();
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent();
				intent.setClass(PanelMgrActivity.this, PanelMgrSubActivity.class);
				intent.putExtra("position", arg2);
				startActivity(intent);
			}
		});
//		mList.setOnChildClickListener(new OnChildClickListener() {
//			
//			@Override
//			public boolean onChildClick(ExpandableListView parent, View v,
//					int groupPosition, int childPosition, long id) {
//				childData.get(groupPosition).remove(childPosition);
//				exAdapter.notifyDataSetChanged();
//				MainActivity.getTheInstance().slideAdapterRemoveItem(groupPosition, childPosition);
//				return false;
//			}
//		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.panel_menu, menu);
		return true;
	}
	public boolean showDeleteDialog(MenuItem item) {
		Builder builder = new Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle(R.string.choose_delete_group)
		.setItems(groupData.toArray(new CharSequence[groupData.size()]), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (groupData.size() > 1) {
					groupData.remove(which);
					exAdapter.notifyDataSetChanged();
					MainActivity.getTheInstance().removeSlideAdapter(which);
				}
			}
		});
    	builder.create().show();	
		return true;
	}
}
