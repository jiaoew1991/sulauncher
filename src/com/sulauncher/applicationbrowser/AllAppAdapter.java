package com.sulauncher.applicationbrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.sulauncher.MainActivity;
import com.sulauncher.R;
import com.sulauncher.SuApp;
import com.sulauncher.Utils;
import com.sulauncher.search.HanziToPinyin;
import com.sulauncher.search.IconResizer;
import com.sulauncher.search.HanziToPinyin.Token;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * This adapter is used for AllAppActivity to deal with all 
 * of the applications on device.
 * @author jiaoew
 */

public class AllAppAdapter extends BaseAdapter implements Filterable {
	public static int GRID_ITEM = 0;
	public static int LIST_ITEM = 1;
	public static int HORIZONTAL_ITEM = 2;
	
    private final Object lock = new Object();
    private ArrayList<AllAppListItem> mOriginalValues;

//    private static IconResizer mIconResizer = new IconResizer(MainActivity.getTheInstance().getApplicationContext(), MainActivity.getTheInstance().getIconSize(100));;
    private static IconResizer mIconResizer = null;
    protected final LayoutInflater mInflater;

    private static PackageManager mPackageManager = null;
    private static List<AllAppListItem> mActivitiesList = null;
    private List<AllAppListItem> mPrivateList = null;
    private Filter mFilter;
    private Context mContext = null;
    
    public AllAppAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (mActivitiesList == null) {
        	mActivitiesList = makeListItems();
        	int count = mActivitiesList.size();
    		for (int i = 0; i < count; i++) {
    			bindIcon(mActivitiesList.get(i));
    		}
        }
        mPrivateList = new ArrayList<AllAppListItem>(mActivitiesList);
    }

    public Intent intentForPosition(int position) {
        if (mActivitiesList == null) {
            return null;
        }

        Intent intent = new Intent();
        intent.setComponent(null);
        AllAppListItem item = mPrivateList.get(position);
        intent.setClassName(item.packageName, item.className);
        if (item.extras != null) {
            intent.putExtras(item.extras);
        }
        return intent;
    }

    public AllAppListItem itemForPosition(int position) {
        if (mPrivateList == null) {
            return null;
        }
        return mPrivateList.get(position);
    }
    public AllAppListItem getItemByPackageName(String pkName) {
    	if (mPrivateList == null) {
    		return null;
    	}
    	for (AllAppListItem item : mPrivateList) {
    		if (item.packageName.equals(pkName)) {
    			return item;
    		}
		}
    	return null;
    }
    public static AllAppListItem getItem(String name) {
    	for (AllAppListItem item : mActivitiesList) {
			if (item.packageName.equals(name))
				return item;
		}
    	return null;
    }
    public int getCount() {
        return mPrivateList != null ? mPrivateList.size() : 0;
    }
    public static int getListCount() {
        return mActivitiesList!= null ? mActivitiesList.size() : 0;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    int infalteId = R.layout.all_app_grid_list_item;
    public void setInflateId(int id) {
    	infalteId = id;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
//        if (convertView == null) {
            view = mInflater.inflate(infalteId, parent, false);
//        } else {
//            view = convertView;
//        }
        bindView(view, mPrivateList.get(position));
        return view;
    }

    private int iconPosition = GRID_ITEM;
    public void setIconPosition(int flag) {
    	iconPosition = flag;
    }
    private void bindView(View view, AllAppListItem item) {
        TextView text = (TextView) view;
        if (item.icon == null) {
        	Drawable drawable = item.resolveInfo.loadIcon(mContext.getPackageManager());
        	if (drawable != null) {
                item.icon = getIconResizer().createIconThumbnail(drawable);
        	} else {
        		drawable = null;
        	}
        } else {
        	if (iconPosition == GRID_ITEM) {
        		text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
				text.setText(item.label.toString());
			} else if (iconPosition == LIST_ITEM) {
        		text.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
				text.setText(item.label.toString());
			} else if (iconPosition == HORIZONTAL_ITEM) {
        		text.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null);
        		String str = Utils.makeStringComfortable(item.label.toString(), 8);
				text.setText(str);
			}
        }
    }
    public static void bindIcon(AllAppListItem item) {
        if (item.icon == null) {
        	Drawable drawable = item.resolveInfo.loadIcon(getPackageManager());
        	if (drawable != null) {
                item.icon = getIconResizer().createIconThumbnail(drawable);
        	}
        }
    }
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }
    public static void preload() {
    	if (mActivitiesList == null) {
        	mActivitiesList = makeListItems();
        	int count = getListCount();
    		for (int i = 0; i < count; i++) {
    			bindIcon(mActivitiesList.get(i));
    		}
    	}
    }
    /**
     * Perform the query to determine which results to show and return a list of them.
     */
    public static List<AllAppListItem> makeListItems() {
        // Load all matching activities and sort correctly
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(mainIntent, /* no flags */ 0);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(getPackageManager()));

        ArrayList<AllAppListItem> result = new ArrayList<AllAppListItem>(list.size());
        int listSize = list.size();
        Log.d("info", listSize + "");
        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
//            Log.d("info", resolveInfo.toString());
            result.add(new AllAppListItem(getPackageManager(), resolveInfo));
        }
        return result;
    }

    /**
     * An array filters constrains the content of the array adapter with a prefix. Each
     * item that does not start with the supplied prefix is removed from the list.
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (lock) {
                    mOriginalValues = new ArrayList<AllAppListItem>(mActivitiesList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    ArrayList<AllAppListItem> list = new ArrayList<AllAppListItem>(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                ArrayList<AllAppListItem> values = mOriginalValues;
                int count = values.size();

                ArrayList<AllAppListItem> newValues = new ArrayList<AllAppListItem>(count);

                for (int i = 0; i < count; i++) {
                    AllAppListItem item = values.get(i);

                    String[] words = item.label.toString().toLowerCase().split(" ");
                    int wordCount = words.length;

                    for (int k = 0; k < wordCount; k++) {
                        final String word = words[k];

                        if (word.startsWith(prefixString)) {
                            newValues.add(item);
                            break;
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

		@SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
			mActivitiesList = (List<AllAppListItem>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
    public void removeItems(List<AllAppListItem> list) {
    	for (AllAppListItem item : list) {
			mPrivateList.remove(item);
		}
    }
	public void removeItem(int pos) {
		mPrivateList.remove(pos);
		notifyDataSetChanged();
		
	}
	public void addItem(AllAppListItem item) {
		mPrivateList.add(item);
		notifyDataSetChanged();
	}
	public HashMap<String, Integer> getNamePosMap() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < mPrivateList.size(); i++) {
			AllAppListItem item = mPrivateList.get(i);
		    CharSequence label = item.label;
			String first = null;
			ArrayList<Token> tokens = HanziToPinyin.getInstance().get(label.toString());  
		    if (tokens != null && tokens.size() > 0) {  
		    	Token token = tokens.get(0);
	            if (Token.PINYIN == token.type) {  
	            	first = String.valueOf(token.target.charAt(0));
	            } else {  
	            	first = "0";
	            }  
		    }
			if (!Character.isLetter(first.charAt(0))) {
				first = "#";
			}
			if (!map.containsKey(first)) {
				map.put(first, i);
			}
		}
		return map;
	}

	public static void replaceItem(int pos, AllAppListItem item) {
		mActivitiesList.set(pos, item);
	}
	public static void removeStaticListItem(AllAppListItem item) {
		mActivitiesList.remove(item);
	}
	public static void addStaticListItem(AllAppListItem item) {
		mActivitiesList.add(item);
	}
	public void removeItem(AllAppListItem item) {
		mPrivateList.remove(item);
	}
	public static IconResizer getIconResizer() {
        if (mIconResizer == null) {
            mIconResizer = new IconResizer(MainActivity.getTheInstance().getApplicationContext(), SuApp.ALL_APP_ICON_SIZE);
        }
		return mIconResizer;
	}
	public static PackageManager getPackageManager() {
		if (mPackageManager == null) {
			try {
				mPackageManager = MainActivity.getTheInstance().getPackageManager();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return mPackageManager;
	}
}
 
