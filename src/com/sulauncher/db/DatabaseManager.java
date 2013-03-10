package com.sulauncher.db;

import java.util.ArrayList;

import com.sulauncher.MainActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database for gesture
 * @author YuMS
 */
public class DatabaseManager {
	private static Context context = MainActivity.getTheInstance().getApplicationContext();
	private static DatabaseHelper dbHelper;
	private static SQLiteDatabase db;
	private static boolean initiated = false;
	private static final int THRESHOLD_TIMES = 20;
	private static final int THRESHOLD_AMOUNT = 20;
	static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("database", "onCreate");
			db.execSQL("create table pattern(pattern VARCHAR(20),intent TEXT(1000))");
			db.execSQL("create table recentapp(intent TEXT(1000),times INT)");
			db.execSQL("create table recentcon(contact_id INT,times INT)");
			db.execSQL("create table folder(name TEXT(100))");
			db.execSQL("create table appinfolder(intent TEXT(1000), folder LONG)");
			Log.d("database", "Created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
	public DatabaseManager() {
	}
	
	private static void init() {
		dbHelper = new DatabaseHelper(context, "su.db", null, 1);
		db = dbHelper.getWritableDatabase();
		initiated = true;
	}
	public static long addPattern(String pattern, String intent) {
		if (!initiated) {
			init();
		}
		ContentValues cv = new ContentValues();
		cv.put("pattern", pattern);
		cv.put("intent", intent);
		return db.insert("pattern", null, cv);
	}
	public static String getPattern(String pattern) {
		if (!initiated) {
			init();
		}
		String intent = "";
		Cursor cursor = db.query("pattern", new String[]{"pattern", "intent"}, "pattern=?", new String[]{pattern}, null, null, null);
		if (cursor.moveToNext()) {
			intent = cursor.getString(cursor.getColumnIndex("intent"));
		}
		return intent;
	}
	public static boolean havePattern(String pattern) {
		if (!initiated) {
			init();
		}
		Cursor cursor = db.query("pattern", new String[]{"pattern", "intent"}, "pattern=?", new String[]{pattern}, null, null, null);
		if (cursor.moveToNext()) {
			return true;
		}
		return false;
	}
	public static boolean delPattern(String pattern) {
		if (!initiated) {
			init();
		}
		return (db.delete("pattern", "pattern=?", new String[]{pattern}) > 0);
	}
	public static boolean cleanPatterns() {
		if (!initiated) {
			init();
		}
		return (db.delete("pattern", "1", null) > 0);
	}
	public static Cursor getPatternCursor() {
		if (!initiated) {
			init();
		}
		Cursor cursor = db.rawQuery("select * from pattern", null); 
		Log.d("remove", cursor.getColumnCount()+"count");
		return cursor;
	}
	public static long addRecentApp(String intent) {
		if (!initiated) {
			init();
		}
		ContentValues cv = new ContentValues();
		cv.put("intent", intent);
		cv.put("times", 1);
		return db.insert("recentapp", null, cv);
	}
	public static int getRecentAppTimes(String intent) {
		if (!initiated) {
			init();
		}
		int times = 0;
		Cursor cursor = db.query("recentapp", new String[]{"intent", "times"}, "intent=?", new String[]{intent}, null, null, null);
		if (cursor.moveToNext()) {
			times = cursor.getInt(cursor.getColumnIndex("times"));
		}
		return times;
	}
	public static boolean haveApp(String intent) {
		if (!initiated) {
			init();
		}
		Cursor cursor = db.query("recentapp", new String[]{"intent"}, "intent=?", new String[]{intent}, null, null, null);
		if (cursor.moveToNext()) {
			return true;
		}
		return false;
	}
	public static boolean touchApp(String intent) {
		if (!initiated) {
			init();
		}
		int times = 0;
		Cursor cursor = db.query("recentapp", new String[]{"times"}, "intent=?", new String[]{intent}, null, null, null);
		if (cursor.moveToNext()) {
			times = cursor.getInt(cursor.getColumnIndex("times"));
		} else {
			addRecentApp(intent);
		}
		times++;
		ContentValues cv = new ContentValues();
		cv.put("times", times);
		return (db.update("recentapp", cv, "intent=?", new String[]{intent}) > 0);
	}
	public static long addRecentCon(int contactID) {
		if (!initiated) {
			init();
		}
		ContentValues cv = new ContentValues();
		cv.put("intent", contactID);
		cv.put("times", 1);
		return db.insert("recentcon", null, cv);
	}
	public static int getRecentConTimes(int contactID) {
		if (!initiated) {
			init();
		}
		int times = 0;
		Cursor cursor = db.query("recentcon", new String[]{"contact_id", "times"}, "contact_id=?", new String[]{contactID + ""}, null, null, null);
		if (cursor.moveToNext()) {
			times = cursor.getInt(cursor.getColumnIndex("times"));
		}
		return times;
	}
	public static boolean haveCon(int contactID) {
		if (!initiated) {
			init();
		}
		Cursor cursor = db.query("recentcon", new String[]{"contact_id"}, "contact_id=?", new String[]{contactID + ""}, null, null, null);
		if (cursor.moveToNext()) {
			return true;
		}
		return false;
	}
	public static boolean touchCon(int contactID) {
		if (!initiated) {
			init();
		}
		int times = 0;
		Cursor cursor = db.query("recentcon", new String[]{"contact_id"}, "contact_id=?", new String[]{contactID + ""}, null, null, null);
		if (cursor.moveToNext()) {
			times = cursor.getInt(cursor.getColumnIndex("times"));
		} else {
			addRecentCon(contactID);
		}
		times++;
		ContentValues cv = new ContentValues();
		cv.put("times", times);
		return (db.update("recentcon", cv, "contact_id=?", new String[]{contactID + ""}) > 0);
	}
	public static boolean decVisitTimes() {
		if (!initiated) {
			init();
		}
		Cursor cursor = db.rawQuery("select * from recentcon", null); 
		int times;
		int visited = 0;
		int maxTimes = 0;
		int contactID;
		int diff;
		while (cursor.moveToNext()) {
			visited++;
			times = cursor.getInt(cursor.getColumnIndex("times"));
			if (times > maxTimes) {
				maxTimes = times;
			}
		}
		if (maxTimes > THRESHOLD_TIMES && visited > THRESHOLD_AMOUNT) {
			diff = maxTimes - THRESHOLD_TIMES;
			cursor.moveToFirst();
			cursor.moveToPrevious();
			while (cursor.moveToNext()) {
				times = cursor.getInt(cursor.getColumnIndex("times"));
				if (times > 0) {
					times = times > diff ? times - diff : 0;
					contactID = cursor.getInt(cursor.getColumnIndex("contact_id"));
					ContentValues cv = new ContentValues();
					cv.put("times", times);
					db.update("recentcon", cv, "contact_id=?", new String[]{contactID+""});
				}
			}
		}
		cursor = db.rawQuery("select * from recentapp", null); 
		String intent;
		while (cursor.moveToNext()) {
			visited++;
			times = cursor.getInt(cursor.getColumnIndex("times"));
			if (times > maxTimes) {
				maxTimes = times;
			}
		}
		if (maxTimes > THRESHOLD_TIMES && visited > THRESHOLD_AMOUNT) {
			diff = maxTimes - THRESHOLD_TIMES;
			cursor.moveToFirst();
			cursor.moveToPrevious();
			while (cursor.moveToNext()) {
				times = cursor.getInt(cursor.getColumnIndex("times"));
				if (times > 0) {
					times = times > diff ? times - diff : 0;
					intent = cursor.getString(cursor.getColumnIndex("intent"));
					ContentValues cv = new ContentValues();
					cv.put("times", times);
					db.update("recentapp", cv, "intent=?", new String[]{intent});
				}
			}
		}
		return true;
	}
	
	public static long addFolder(String name) {
		if (!initiated) {
			init();
		}
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		return db.insert("folder", null, cv);
	}
	
	public static String getFolderNameByID(long id) {
		if (!initiated) {
			init();
		}
		String name = "";
		Cursor cursor = db.query("folder", new String[]{"rowid", "name"}, "rowid=?", new String[]{id + ""}, null, null, null);
		if (cursor.moveToNext()) {
			name = cursor.getString(cursor.getColumnIndex("name"));
		}
		return name;
	}
	
	public static boolean updateFolderNameByID(long id, String name) {
		if (!initiated) {
			init();
		}
		ContentValues cv = new ContentValues();
		cv.put("name", name);
		return (db.update("folder", cv, "rowid=?", new String[]{id + ""}) > 0);
	}
	
	public static ArrayList<ContentValues> getFolderNameAndID() {
		if (!initiated) {
			init();
		}
		ArrayList<ContentValues> cvs = new ArrayList<ContentValues>();
		Cursor cursor = db.rawQuery("select rowid,* from folder", null); 
		while (cursor.moveToNext()) {
			ContentValues cv = new ContentValues();
			cv.put("name", cursor.getString(cursor.getColumnIndex("name")));
			cv.put("id", cursor.getLong(cursor.getColumnIndex("rowid")));
			cvs.add(cv);
		}
		return cvs;
	}
	
	public static boolean delFolderByID(long id) {
		if (!initiated) {
			init();
		}
		return (db.delete("folder", "rowid=?", new String[]{id + ""}) > 0 &&
				db.delete("appinfolder", "folder=?", new String[]{id + ""}) >= 0);
	}
	
	public static long addAppInFolder(String intent, long folderID) {
		if (!initiated) {
			init();
		}
		ContentValues cv = new ContentValues();
		cv.put("intent", intent);
		cv.put("folder", folderID);
		return db.insert("appinfolder", null, cv);
	}
	
	public static ArrayList<String> getAppsInFolder(long folderID) {
		ArrayList<String> intents = new ArrayList<String>();
		Cursor cursor = db.query("appinfolder", new String[]{"intent", "folder"}, "folder=?", new String[]{folderID + ""}, null, null, null);
		while (cursor.moveToNext()) {
			intents.add(cursor.getString(cursor.getColumnIndex("intent")));
		}
		return intents;
	}
	
	public static boolean delAppInFolder(String intent, long folderID) {
		if (!initiated) {
			init();
		}
		return (db.delete("appinfolder", "folder=? AND intent=?", new String[]{folderID + "", intent}) > 0);
	}
}