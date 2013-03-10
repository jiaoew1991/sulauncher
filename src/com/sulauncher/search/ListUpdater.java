package com.sulauncher.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.sulauncher.search.ListWithTag.ListTags;

import android.util.Log;
/**
 * A single instance updater
 * @author YuMS
 */
public class ListUpdater {
	private static String TAG = "listupdater";
	public static enum KeyChangeType{APPEND, NOTAPPEND};
	//Single instance
	private static ListUpdater theInstance = null;
	private boolean[] nextAvailable;
	private LinkedList<ListWithTag> result;
	private ListUpdater() {
			result = new LinkedList<ListWithTag>();
			nextAvailable = new boolean[26];
	}
	public static ListUpdater instance() {
		if (null == theInstance) {
			theInstance = new ListUpdater();
		}
		return theInstance;
	}
	public void renew(final String[] key) {
		for (ListWithTag nowList : result) {
			if (nowList.getTag() != ListTags.RECENTAPP) {
				renewHelper(key, nowList);
			}
		};
	}
	public void renew(final String key) {
		for (ListWithTag nowList : result) {
			if (nowList.getTag() != ListTags.RECENTAPP) {
				renewHelperWithAppend(key, nowList);
			}
		};
	}
	private void renewHelperWithAppend(String key, ListWithTag nowList) {
		String nowKey, nowImportant;
		boolean following;
		DataItem item;
		nowList.setNumInList(0);
		@SuppressWarnings("unchecked")
		LinkedList<DataItem> list = (LinkedList<DataItem>) nowList.getList();
		if (list.isEmpty()) {
			Log.d(TAG, "Oh, it' s empty");
			return;
		}
		for (ListIterator<DataItem> it = list.listIterator(); it.hasNext();) {
			item = it.next();
			if (item.match < 0) {
				continue;
			}
			nowImportant = item.importantKey;
			following = false;
			nowKey = item.itemKey;
			Log.d(TAG, item.match + " " + item.lastMatch + " " + item.lastMatchImportant);
			Log.d(TAG, nowImportant + " " + nowKey);
			if (nowKey.length() == item.lastMatch + 1) {
			} else if (key.contains(nowKey.charAt(item.lastMatch + 1) + "")) {
				following = true;
				item.match++;
				item.lastMatch++;
			} else {
				for (int i = item.lastMatchImportant + 1; i < nowImportant.length(); i++) {
					if (key.contains(nowImportant.charAt(i) + "")) {
						following = true;
						item.match += 5;
						item.lastMatchImportant = i;
						item.lastMatch = item.importantPos.get(item.lastMatchImportant);
						break;
					}
				}
			}
			if (following) {
				nowList.numInListInc(1);
			} else {
				item.match = -1;
			}
			if (item.match > 0) {
				item.match = item.match / nowImportant.length() + item.visitTimes * 10;
			}
		}
		Log.d(TAG, "appended");
		Collections.sort(list, new Comparator<DataItem>() {
			@Override
			public int compare(DataItem arg0, DataItem arg1) {
				return ((arg0.match < arg1.match) ? 1 :
					(arg0.match == arg1.match ? 0 : -1));
			}
		});
		Log.d(TAG, "list renewed");
	}
		
	private void renewHelper(String[] key, ListWithTag nowList) {
		int i, j;
		int itemSize, keySize;
		String nowKey, nowImportant;
		boolean following;
		DataItem item;
		keySize = key.length;
		nowList.setNumInList(0);
		@SuppressWarnings("unchecked")
		LinkedList<DataItem> list = (LinkedList<DataItem>) nowList.getList();
		
		if (list.isEmpty()) {
			Log.d(TAG, "Oh, it' s empty");
			return;
		}
		for (ListIterator<DataItem> it = list.listIterator(); it.hasNext();) {
			item = it.next();
			nowImportant = item.importantKey;
			following = false;
			item.match = 0;
			item.lastMatch = -1;
			item.lastMatchImportant = -1;
			if (key.length > 0) {
				for (i = 0; i < nowImportant.length(); i++) {
					if (key[0].contains(nowImportant.charAt(i) + "")) {
						following = true;
						break;
					}
				}
				if (following) {
					nowKey = item.itemKey;
					itemSize = item.itemKey.length();
					i = j = 0;
					item.lastMatchImportant = 0;
//					keyPointer = 1;
					following = true;
					while (i < keySize && j < itemSize) {
						if (following) {
							if (key[i].contains(nowKey.charAt(j) + "")) {
								item.match++;
								item.lastMatch = j;
								i++;
							} else {
								following = false;
							}
						}
						if (nowKey.charAt(j) == ' ') {
							following = true;
							item.lastMatchImportant++;
//							keyPointer++;
						}
						j++;
					}
					if (i < keySize) {
						item.match = -1;
					}
					if (item.match > 0) {
						nowList.numInListInc(1);
//						if (keyPointer < nowImportant.length()) {
//							addNextAvailable(nowImportant.substring(keyPointer));
//						}
//						if (j < nowKey.length()) {
//							addNextAvailable(nowKey.charAt(j));
//						}
					}
				} else {
					item.match = -1;
				}
				if (item.match > 0) {
					item.match = item.match / nowImportant.length() + item.visitTimes * 10;
				}
			}
		}
		Log.d(TAG, "calculated");
		Collections.sort(list, new Comparator<DataItem>() {
			@Override
			public int compare(DataItem arg0, DataItem arg1) {
				return ((arg0.match < arg1.match) ? 1 :
					(arg0.match == arg1.match ? 0 : -1));
			}
		});
		Log.d(TAG, "list renewed");
	}
	
	public void addNextAvailable(String str) {
		try {
		if (str.length() > 0) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) >= 'a' && str.charAt(i) <='z') {
					nextAvailable[str.charAt(i) - 'a'] = true;
				}
			}
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void addNextAvailable(char ch) {
		if (ch >= 'a' && ch <='z') {
			nextAvailable[ch - 'a'] = true;
		}
	}
	
	public void showListHelper() {
		try{
		for (ListWithTag nowList : result) {
			Log.d(TAG, "tag is " + nowList.getTag());
			@SuppressWarnings("unchecked")
			LinkedList<DataItem> list = (LinkedList<DataItem>) nowList.getList();
			for (ListIterator<DataItem> it = list.listIterator();it.hasNext();) {
				DataItem item = it.next();
				Log.d(TAG, item.itemName + " " + item.itemKey + " " + item.importantKey);
			}
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public boolean[] getNextAvailable() {
		return nextAvailable;
	}
	public void setResult(LinkedList<ListWithTag> data) {
		result = data;
	}
	public LinkedList<ListWithTag> getResult() {
		return result;
	}
}
