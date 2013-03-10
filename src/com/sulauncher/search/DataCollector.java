package com.sulauncher.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sulauncher.db.DatabaseManager;
import com.sulauncher.search.ListWithTag.ListTags;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

/**
 * Preload Pinyin, icons, information
 * @author YuMS
 */
public class DataCollector {
	private static String TAG = "datacollector";
	private static LinkedList<ListWithTag> data = null;
	public static HashMap<String, ResolveInfo> packageNameToAppItem = null;
	private static Context context;

	public DataCollector(Context context) {
		data = new LinkedList<ListWithTag>();
		packageNameToAppItem = new HashMap<String, ResolveInfo>();
		DataCollector.context = context;
		collectAll();
	}

	public void collectAll() {
		Log.d(TAG, "Tidying database");
		DatabaseManager.decVisitTimes();
		Log.d(TAG, "Starting application");
		collectApplications();
		Log.d(TAG, "Starting contacts");
		collectContacts();
		Log.d(TAG, "Data collectted");
	}

	public void collectApplications() {
		PackageManager pm = context.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
		boolean newList = false;
		ListWithTag appListWithTag = findListWithTag(ListTags.APP);
		if (appListWithTag == null) {
			newList = true;
		}
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);  
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0); 
		Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));  
		LinkedList<AppItem> appArr = new LinkedList<AppItem>();
		int count = resolveInfos.size();
//		Log.d(TAG, Integer.toString(count));
		for (ResolveInfo resolveInfo : resolveInfos) {
//			Log.d(TAG, (String)resolveInfo.loadLabel(pm));
//			Log.d(TAG, resolveInfo.toString());
			try {
				packageNameToAppItem.put(resolveInfo.activityInfo.packageName, resolveInfo);
				String name = ((String) resolveInfo.loadLabel(pm)).trim();
				appArr.add(new AppItem(name, resolveInfo));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (newList) {
			appListWithTag = new ListWithTag(appArr, ListTags.APP);
			appListWithTag.setNumInList(count);
			data.add(appListWithTag);
		} else {
			appListWithTag.setList(appArr);
		}
	}

	public void collectContacts() {
		boolean newList = false;
		ContentResolver cr = context.getContentResolver();
//		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
//				null, null, "display_name  COLLATE LOCALIZED ASC");
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		Log.d(TAG, "gotContentResolver");
		ListWithTag conListWithTag = findListWithTag(ListTags.CON);
		if (conListWithTag == null) {
			newList = true;
		}
		LinkedList<DataItem> conArr = new LinkedList<DataItem>();
		int nameFieldColumnIndex;
		String name;
		int contactId;
		try {
		while (cursor.moveToNext()) {
			nameFieldColumnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			name = cursor.getString(nameFieldColumnIndex);
			contactId = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(BaseColumns._ID)));
			conArr.add(new ContactItem(name, contactId));
		}
		cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG, "got contacts");
		if (newList) {
			data.add(new ListWithTag(conArr, ListTags.CON));
		} else {
			conListWithTag.setList(conArr);
		}
	}
//	public void collectRecentApp() {
//		boolean newList = false;
//		Log.d(TAG, "in collect");
//		try {
//		final PackageManager manager = context.getPackageManager();
//		final ActivityManager tasksManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//		final List<ActivityManager.RecentTaskInfo> recentTasks = tasksManager.getRecentTasks(
//				MAX_RECENT_TASKS, 0);
//		ListWithTag recentAppListWithTag = findListWithTag(ListTags.RECENTAPP);
//		if (recentAppListWithTag == null) {
//			newList = true;
//		}
//		LinkedList<AppItem> recentAppList = new LinkedList<AppItem>();
//
//		Log.d(TAG, recentTasks.toString());
//		final int count = recentTasks.size();
//		int numInList = 0;
//		Log.d(TAG, count + "");
//
//		Log.d(TAG, "in collect2");
//		for (int i = count - 1; i >= 0; i--) {
//			Intent intent = recentTasks.get(i).baseIntent;
//
//			if (Intent.ACTION_MAIN.equals(intent.getAction()) &&
//					!intent.hasCategory(Intent.CATEGORY_HOME)) {
//
//				ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
//				if (resolveInfo != null) {
//					Log.d(TAG, (String)resolveInfo.loadLabel(manager));
//					recentAppList.addFirst(new AppItem((String)resolveInfo.loadLabel(manager), resolveInfo));
//					numInList++;
//				}
//				
//			}
//		}
//		Log.d(TAG, "numInList" + numInList);
//		if (newList) {
//			recentAppListWithTag = new ListWithTag(recentAppList, ListTags.RECENTAPP);
//			recentAppListWithTag.setNumInList(numInList);
//			data.add(recentAppListWithTag);
//		} else {
//			recentAppListWithTag.setList(recentAppList);
//			recentAppListWithTag.setNumInList(numInList);
//		}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		Log.d(TAG, "finish collecting recent");
//	}

	public LinkedList<ListWithTag> getData() {
		return data;
	}
	
	private ListWithTag findListWithTag(ListTags tag) {
		for (Iterator<ListWithTag> iterator = data.iterator(); iterator.hasNext();) {
			ListWithTag nowItem = (ListWithTag) iterator.next();
			if (nowItem.getTag() == tag) {
				return nowItem;
			}
		}
		return null;
	}
}
