package com.sulauncher.gesturemgr;

import java.util.List;

import com.sulauncher.MainActivity;
import com.sulauncher.R;
import com.sulauncher.db.DatabaseManager;
import com.sulauncher.db.PatternHashMap;
import com.sulauncher.supad3d.Supad3DUtils;
import com.sulauncher.supad3d.Supad3DView;
import com.sulauncher.supad3d.ButtonBack;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for pattern drawing of gesture manager
 * @author Jiaoew
 */
public class GestureMgrDrawActivity extends Activity {

	private Supad3DView mLockPatternView = null;
	private Button finishButton = null;
	private TextView mTextView = null;
	private Intent mIntent = null;
	private List<ButtonBack> mPattern = null;
	private String patternString = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_set_layout);
		setTitle(R.string.setting_gesture_title);
		mIntent = this.getIntent();
		Log.d("gesture", "package: " + mIntent.getExtras().getString("packageName") + ", class: " + mIntent.getExtras().getShort("className"));
		mTextView = (TextView) findViewById(R.id.textView1);
		PackageManager pManager = getPackageManager();
		try {
			ApplicationInfo appInfo = pManager.getApplicationInfo(mIntent.getExtras().getString("packageName"), PackageManager.GET_META_DATA);
			mTextView.setText(pManager.getApplicationLabel(appInfo));
			Drawable draw = pManager.getApplicationIcon(appInfo);
			mTextView.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		finishButton = (Button) findViewById(R.id.finishButton);
		mLockPatternView = (Supad3DView) findViewById(R.id.setting_lockView);
		finishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("setting", "finish");
				if (mPattern == null || mPattern.size() <= 1) {
					Toast.makeText(GestureMgrDrawActivity.this, R.string.please_setting_geture, Toast.LENGTH_SHORT).show();
					return;
				}
				patternString = Supad3DUtils.patternToString(mPattern);
				String packName = mIntent.getExtras().getString("packageName");
				if (DatabaseManager.havePattern(patternString)) {
					displayGestureDialog(packName);
				}
				else {
					DatabaseManager.addPattern(patternString, packName);
					PatternHashMap.addPettern(patternString, packName);
					Toast.makeText(GestureMgrDrawActivity.this, R.string.add_gesture_success, Toast.LENGTH_SHORT).show();
					mLockPatternView.clearPattern();
					Intent intent = new Intent(GestureMgrDrawActivity.this, GestureMgrActivity.class);
					intent.putExtra("pattern", patternString);
					startActivity(intent);
					GestureMgrDrawActivity.this.finish();
				}
			}
		});
		mLockPatternView.setOnPatternListener(new Supad3DView.OnPatternListener() {
			@Override
			public void onPatternStart() {
			}
			@Override
			public void onPatternDetected(List<ButtonBack> pattern) {
				Log.d("setting", "check");
				mPattern = pattern;
				if (pattern.size() == 1) {
					mLockPatternView.clearPattern();
					mPattern = null;
				}
			}
			@Override
			public void onPatternCleared() {
			}
			
			@Override
			public void onPatternCellAdded(List<ButtonBack> pattern) {
				
			}
		});
	}
	private void displayGestureDialog(final String packName) {
		Builder builder = new Builder(this);
    	builder.setTitle(R.string.gesture_used);
    	builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				GestureMgrDrawActivity.this.doPositiveClick(packName);
			}
		});
    	builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	builder.create().show();
	}
	public void doPositiveClick(String packName) {
		DatabaseManager.delPattern(patternString);
		DatabaseManager.addPattern(patternString, packName);
		Toast.makeText(GestureMgrDrawActivity.this, R.string.add_gesture_success, Toast.LENGTH_SHORT).show();
		mLockPatternView.clearPattern();
		MainActivity.returnFromSelf = true;
		startActivity(new Intent(GestureMgrDrawActivity.this, GestureMgrActivity.class));
		GestureMgrDrawActivity.this.finish();
	}

}
