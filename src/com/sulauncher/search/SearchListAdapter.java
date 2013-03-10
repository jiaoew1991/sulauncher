package com.sulauncher.search;

import java.io.InputStream;
import java.util.LinkedList;

import com.sulauncher.MainActivity;
import com.sulauncher.R;
import com.sulauncher.SuApp;
import com.sulauncher.Utils;
import com.sulauncher.search.ListWithTag.ListTags;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapter for searching result lists
 * @author YuMS
 */
public class SearchListAdapter extends BaseAdapter {
	private static String TAG = "searchlistadapter";
	private static int MinOneHeight = 60;
	private static Drawable DefaultAvatar = null;
	private LayoutInflater mInflater;
	private LinkedList<ListWithTag> mData;
	private ListWithTag mListWithTag;
	private int itemResource;
	private IconResizer mIconResizer;
	private Context context;
	private int noOverlapNumber = 4;
	private int totalShowNumber = 4;
	private int parentHeight = 0;
	private int supadHeight = 0;
	private int oneHeight = 0;
	PackageManager pm = null;
	ContentResolver cr = null;

	public SearchListAdapter(Context context, ListTags tag) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Log.d("debug", "mInflater is" + mInflater.toString());
		this.mData = ListUpdater.instance().getResult();
		this.context = context;
		mIconResizer = new IconResizer(context, SuApp.SEARCH_ICON_SIZE);
		for (ListWithTag it : mData) {
			if (it.getTag() == tag) {
				mListWithTag = it;
				break;
			}
		}
//		Log.d(TAG, mListWithTag.toString());
		pm = context.getPackageManager();
		cr = context.getContentResolver();
		itemResource = R.layout.result_list_item;
		Log.d(TAG, mData.toString());
	}

	/**
	 * The number of items in the list is determined by the number of
	 * speeches in our array.
	 * 
	 * @see android.widget.ListAdapter#getCount()
	 */
	@Override
	public int getCount() {
		int count;
		if (mListWithTag != null) {
				count = mListWithTag.getNumInList();
		} else {
			count = 0;
		}
		if (count > totalShowNumber) count = totalShowNumber;
		return count;
	}

	/**
	 * Since the data comes from an array, just returning the index is
	 * sufficent to get at the data. If we were using a more complex data
	 * structure, we would return whatever object represents one row in the
	 * list.
	 * 
	 * @see android.widget.ListAdapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return position;
	}

	/**
	 * Use the array index as a unique id.
	 * 
	 * @see android.widget.ListAdapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Make a view to hold each row.
	 * 
	 * @see android.widget.ListAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mListWithTag == null || mListWithTag.getList().isEmpty()) {
			return convertView;
		}
		if (parentHeight == 0 || supadHeight == 0) {
			Log.d(TAG, "getting parentHeight");
			parentHeight = MainActivity.getTheInstance().getSearchLists().getHeight();
			Log.d(TAG, parentHeight + "");
			supadHeight = MainActivity.getTheInstance().getSupad3DView().getHeight();
			supadHeight = supadHeight * 9 / 10;
			Log.d(TAG, parentHeight + "");
			oneHeight = (parentHeight - supadHeight) / noOverlapNumber;
			if (oneHeight != 0) {
				while (oneHeight < MinOneHeight && noOverlapNumber > 1) {
					noOverlapNumber--;
					oneHeight = (parentHeight - supadHeight) / noOverlapNumber;
				}
				if (oneHeight < MinOneHeight && noOverlapNumber == 1) {
					oneHeight = MinOneHeight;
				}
				totalShowNumber = parentHeight / oneHeight - 1;
			}
		}
		DataItem item = mListWithTag.getList().get(position);
		View view = null;
		GradientTextView textView;
		Drawable mDrawable;
		if (convertView == null) {
			view = mInflater.inflate(itemResource, null);
			if (view == null) {
				Log.d(TAG, "view is null");
			}
		} else {
			view = convertView;
		}
		try {
		textView = (GradientTextView) view;
		textView.setHeight(oneHeight);
		int times = item.visitTimes;
		textView.setText(item.itemName);
		textView.setMinAlpha(times < 10 ? times * 10 + 100 : 200);
//		textView.setMinimumHeight((parent.getBottom() - parent.getTop()) / (showNumber + 1));
		if (item instanceof AppItem) {
			AppItem applicationItem = (AppItem)item;
			if (item.itemIcon == null) {
				mDrawable = applicationItem.resolveInfo.loadIcon(pm);
				if (mDrawable != null) {
					item.itemIcon = mIconResizer.createIconThumbnail(mDrawable);
				}
			}
            textView.setCompoundDrawablesWithIntrinsicBounds(item.itemIcon, null, null, null);
		} else if (item instanceof ContactItem) {
			ContactItem contactItem = (ContactItem)item;
			if (item.itemIcon == null) {
				mDrawable = loadContactPhoto(cr, contactItem.contactID);
				if (mDrawable != null) {
					item.itemIcon = mIconResizer.createIconThumbnail(mDrawable);
				}
			}
			if (item.itemIcon != null) {
	            textView.setCompoundDrawablesWithIntrinsicBounds(item.itemIcon, null, null, null);
			} else {
				textView.setCompoundDrawablesWithIntrinsicBounds(Utils.createTextDrawable(item.itemKey, SuApp.SEARCH_ICON_SIZE), null, null, null);
			}
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return view;
	}

	@Override
	public boolean isEnabled(int position) {
		return (position >= 0);
	}
	
	public Drawable loadContactPhoto(ContentResolver cr, long id) {
		Uri uri = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, id);
		InputStream input = ContactsContract.Contacts
				.openContactPhotoInputStream(cr, uri);
		if (input == null) {
			return null;
		}
		return new BitmapDrawable(context.getResources(),
				BitmapFactory.decodeStream(input));
	}
	
	public void setNoOverlapNumber(int length) {
		noOverlapNumber = length;
	}
	
	public Drawable getDefaultAvatar() {
//		if (DefaultAvatar == null) {
//			DefaultAvatar = context.getResources().getDrawable(R.drawable.ic_contact_picture);
//			DefaultAvatar = mIconResizer.createIconThumbnail(DefaultAvatar);
//		}
		return DefaultAvatar;
	}
}