package com.sulauncher.gesturemgr;

import java.util.ArrayList;
import java.util.List;

import com.sulauncher.MainActivity;
import com.sulauncher.R;
import com.sulauncher.SuApp;
import com.sulauncher.db.DatabaseManager;
import com.sulauncher.db.PatternHashMap;
import com.sulauncher.search.IconResizer;
import com.sulauncher.supad.SuPadUtils;
import com.sulauncher.supad.SuPadView.Cell;
import com.sulauncher.widget.SwipeDeleteListView;
import com.sulauncher.widget.SwipeDeleteListView.OnItemDeleteListener;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Gesture manager activity
 * @author Jiaoew
 */
public class GestureMgrActivity extends Activity {
	private SharedPreferences sp = null;
	private IconResizer mIconResizer = new IconResizer(this, SuApp.ALL_APP_ICON_SIZE);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTitle(R.string.setting_gesture_title);
		setContentView(R.layout.custom_title);
		sp = getSharedPreferences("count", 0);
		int count = sp.getInt("count", 0);
		if (count < 2) {
			Toast.makeText(this, R.string.guide_2, Toast.LENGTH_SHORT).show();
			Editor editor = sp.edit();
			editor.putInt("count", count++);
			editor.commit();
		}
		final PhotoAdapater adapter = new PhotoAdapater(this);
//		ListView mListView = this.getListView();
		SwipeDeleteListView mListView = (SwipeDeleteListView) findViewById(R.id.swipe_listview);
		mListView.setAdapter(adapter);
//		mListView.setListAdapter(adapter);
		mListView.setOnItemDeleteListener(new OnItemDeleteListener() {
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				DatabaseManager.delPattern(SuPadUtils.patternToString(patternList.get(arg2)));
//				PatternHashMap.delPettern(SuPadUtils.patternToString(patternList.get(arg2)));
//				patternList.remove(arg2);
//				iconList.remove(arg2);
//				appNameList.remove(arg2);
//				adapter.removeItem();
//			}

			@Override
			public void onItemDelete(int pos) {
				DatabaseManager.delPattern(SuPadUtils.patternToString(patternList.get(pos)));
				PatternHashMap.delPettern(SuPadUtils.patternToString(patternList.get(pos)));
				patternList.remove(pos);
				iconList.remove(pos);
				appNameList.remove(pos);
				adapter.removeItem();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		startActivity(new Intent().setClass(this, MainActivity.class));
		this.finish();
	}

	private Bitmap createGestureBitmap(List<Cell> pattern) {
		final int width = 100;
		final int height = 100;
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		Paint circlePaint = new Paint();
		circlePaint.setColor(Color.WHITE);
		circlePaint.setAntiAlias(true);
		Paint arrowPaint = new Paint();
		arrowPaint.setAntiAlias(true);
		arrowPaint.setColor(Color.WHITE);
		arrowPaint.setAlpha(128);
		arrowPaint.setStrokeWidth(3);
		
		circlePaint.setStrokeWidth(1);
		circlePaint.setStyle(Paint.Style.STROKE);
		for (int i = 1; i <= 3; i++) {
			for (int j = 1; j <= 4; j++) {
				canvas.drawCircle((float)(width / 4 * i), (float)(height * (2 * j - 1) / 8.0), 3.0f, circlePaint);
			}
		}
		Cell lastCell = null;
		for (Cell cell : pattern) {
			circlePaint.setColor(Color.WHITE);
			circlePaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle((float)(width / 4 * (cell.getColumn() + 1)),
					(float)(height * (2 * cell.getRow() + 1) / 8), 3.0f, circlePaint);
			circlePaint.setColor(Color.GREEN);
			circlePaint.setStyle(Paint.Style.STROKE);
			canvas.drawCircle((float)(width / 4 * (cell.getColumn() + 1)),
					(float)(height * (2 * cell.getRow() + 1) / 8), 10.0f, circlePaint);
			if (lastCell != null) {
				canvas.drawLine((float)(width / 4 * (cell.getColumn() + 1)), (float)(height * (2 * cell.getRow() + 1) / 8),
						(float)(width / 4 * (lastCell.getColumn() + 1)), (float)(height * (2 * lastCell.getRow() + 1) / 8), arrowPaint);
			}
			lastCell = cell;
		}
		return output;
	}
	private List<Drawable> iconList = new ArrayList<Drawable>();
	private List<String> appNameList = new ArrayList<String>();
	private List<List<Cell>> patternList = new ArrayList<List<Cell>>();

	private class PhotoAdapater extends BaseAdapter {
		private LayoutInflater mInflater = null;
		public PhotoAdapater(Context context) {
			super();
			Log.d("photo", "new adapter");
	        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			Cursor cursor = DatabaseManager.getPatternCursor();
			PackageManager pManager = getPackageManager();
			while (cursor.moveToNext()) {
				String pattern = cursor.getString(cursor.getColumnIndex("pattern"));
				String packName = cursor.getString(cursor.getColumnIndex("intent"));
				ApplicationInfo appInfo;
				try {
					appInfo = pManager.getApplicationInfo(packName, PackageManager.GET_META_DATA);
					Log.d("photo", appInfo.toString());
					CharSequence appName = pManager.getApplicationLabel(appInfo);
					Drawable draw = pManager.getApplicationIcon(appInfo);
					iconList.add(mIconResizer.createIconThumbnail(draw));
					appNameList.add(appName.toString());
					patternList.add(SuPadUtils.stringToPattern(pattern));
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
			cursor.close();
		}

		@Override
		public int getCount() {
			return appNameList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return patternList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view;
	        if (convertView == null) {
	            view = (TextView)mInflater.inflate(R.layout.all_app_linear_list_item, parent, false);
	        } else {
	            view = (TextView)convertView;
	        }
	        view.setText(appNameList.get(position));
	        view.setCompoundDrawablesWithIntrinsicBounds(iconList.get(position), null,
	        		new BitmapDrawable(createGestureBitmap(patternList.get(position))), null);
	        return view;
		}
		public void removeItem() {
			this.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actions, menu);
		return true;
	}

	public boolean startListActivity(MenuItem item) {
		startActivity(new Intent().setClass(GestureMgrActivity.this, GestureMgrListActivity.class));
		this.finish();
		return true;
	}
//	private class AddActionProvider extends ActionProvider {
//
//		private Context mContext;
//		public AddActionProvider(Context context) {
//			super(context);
//			mContext = context;
//		}
//
//		@Override
//		public View onCreateActionView() {
//			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
//            View view = layoutInflater.inflate(R.layout.action_bar_settings_action_provider, null);
//            ImageButton button = (ImageButton) view.findViewById(R.id.action_add_button);
//            // Attach a click listener for launching the system settings.
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                	Intent intent = new Intent();
//                	intent.setClass(GestureMgrActivity.this, GestureMgrListActivity.class);
//                    mContext.startActivity(intent);
//                }
//            });
//            return view;
//		}
//		
//	}
}
