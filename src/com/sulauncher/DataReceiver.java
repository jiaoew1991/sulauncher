package com.sulauncher;


import java.util.ArrayList;

import com.sulauncher.db.DatabaseManager;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class DataReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String name = intent.getDataString().substring(8);
//		SharedPreferences sp = context.getSharedPreferences(SuApp.REMOVE_SP_NAME, Context.MODE_PRIVATE);
		if (name.equals("com.sulauncher")) 
			return;
		if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
			 Log.d("remove", intent.getData() + "");
		} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
//			 Log.d("remove", intent.getData() + "");
			 Log.d("remove", name);
			 ArrayList<ContentValues> fNames = DatabaseManager.getFolderNameAndID();
//			 Log.d("remove", "size: " + fNames.size());
			 for (ContentValues cv : fNames) {
				 Log.d("remove", cv.getAsLong("id") + cv.getAsString("name"));
				 boolean ret = DatabaseManager.delAppInFolder(name, cv.getAsLong("id"));
				 Log.d("remove", "result : " + ret);
			}
		}
	}
}
