package com.sulauncher.applicationbrowser;

import java.util.Random;

import com.sulauncher.MainActivity;
import com.sulauncher.MainActivityStatuses;
import com.sulauncher.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class AllAppView extends RelativeLayout {
	Intent mIntent;
	PackageManager mPackageManager;
	private ImageButton mImageButton = null;
	private AllAppAdapter mAdapter;
	private GridView mGridView = null;

	Vibrator vibrator = null;
	Random rand = new Random();

	public AllAppView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mPackageManager = context.getPackageManager();
        if (!isInEditMode()) {
	        mAdapter = new AllAppAdapter(context);
        }
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
        mImageButton = (ImageButton) findViewById(R.id.homeButton);
        mGridView = (GridView) findViewById(R.id.my_Grid);
        
        mIntent = new Intent();
        mIntent.setComponent(null);

        mGridView.setAdapter(mAdapter);
        mGridView.setTextFilterEnabled(true);
        mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				Intent intent = intentForPosition(position);
				getContext().startActivity(intent);
			}
		});
        mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int arg2, long arg3) {
				Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
				v.startAnimation(anim);
				vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(100);
				return true;
			}
		});
        mImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)getContext()).changeStatus(MainActivityStatuses.DEFAULT_DESKTOP);
			}
		});
	}
	public void removeItem(AllAppListItem item) {
		mAdapter.removeItem(item);
	}
	/** * Return the actual Intent for a specific position in our
	 * {@link android.widget.ListView}.
	 * @param position The item whose Intent to return
	 */
	protected Intent intentForPosition(int position) {
		AllAppAdapter adapter = (AllAppAdapter) mAdapter;
		return adapter.intentForPosition(position);
	}

	/**
	 * Return the {@link AllAppListItem} for a specific position in our
	 * {@link android.widget.ListView}.
	 * @param position The item to return
	 */
	protected AllAppListItem itemForPosition(int position) {
		AllAppAdapter adapter = (AllAppAdapter) mAdapter;
		return adapter.itemForPosition(position);
	}
}
