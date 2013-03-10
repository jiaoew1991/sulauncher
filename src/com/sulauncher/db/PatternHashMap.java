package com.sulauncher.db;

import java.util.HashMap;
import android.database.Cursor;
/**
 * A copy of database in memory
 * @author jiaoew
 */
public class PatternHashMap {
	private static HashMap<String, String> map = null;
	public static void initialize() {
		map = new HashMap<String, String>();
		Cursor cursor = DatabaseManager.getPatternCursor();
		while (cursor.moveToNext()) {
			String pattern = cursor.getString(cursor.getColumnIndex("pattern"));
			String packName = cursor.getString(cursor.getColumnIndex("intent"));
			map.put(pattern, packName);
		}
		cursor.close();
	}
	public static boolean havePattern(String pattern) {
		if (map == null) {
			initialize();
		}
		return map.containsKey(pattern);
	}
	public static String getPattern(String pattern) {
		if (map == null) {
			initialize();
		}
		return map.get(pattern);
	}
	public static void addPettern(String pattern, String intent) {
		if (map == null) {
			initialize();
		}
		map.put(pattern, intent);
	}
	public static void delPettern(String pattern) {
		if (map == null) {
			initialize();
		}
		if (map.containsKey(pattern)) {
			map.remove(pattern);
		}
	}
}
