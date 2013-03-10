package com.sulauncher.search;

import com.sulauncher.db.DatabaseManager;

import android.content.pm.ResolveInfo;

/**
 * An application item
 * @author YuMS
 */
public class AppItem extends DataItem{
	public ResolveInfo resolveInfo;
	public AppItem(String itemName, ResolveInfo resolveInfo) {
		super(itemName);
		this.resolveInfo = resolveInfo;
 		this.visitTimes = DatabaseManager.getRecentAppTimes(resolveInfo.activityInfo.packageName);
	}
}