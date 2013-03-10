package com.sulauncher;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.app.ProgressDialog;
import android.util.Log;

public class SuApp extends Application {
	
	public static final int SIDEBAR_SIZE = 50;
	public static final int TRANS_DELAY = 500;
	public static final int ALL_APP_ICON_SIZE = 80;
	public static final int SEARCH_ICON_SIZE = 50;

	public static final String REMOVE_SP_NAME = "remove";
	private ProgressDialog progress;
	@Override
	public void onCreate() {
		super.onCreate();
//		progress = new ProgressDialog(this);
//		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		progress.setIcon(android.R.attr.alertDialogIcon);
//		progress.setTitle(R.string.init_mainactivity);
//		progress.show();
		Log.d("suapp", "dialog show");
//		Builder builder = new Builder(this, AlertDialog.THEME_HOLO_DARK);
//		builder.set
//		builder.show()
		Log.d("suapp", "app create");
	}

	public void dismissProgress() {
//		progress.dismiss();
		Log.d("suapp", "dialog dismiss");
	}
}
