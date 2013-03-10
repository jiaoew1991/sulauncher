package com.sulauncher.search;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

/**
 * A base item
 * @author YuMS
 */
public class DataItem {
	/**
	 * Name of item
	 */
	public String itemName;
	/**
	 * Pinyin of item
	 */
	public String itemKey;
	/**
	 * Every initial of item' s Pinyin tokens
	 */
	public String importantKey;
	ArrayList<Integer> importantPos;
	public Drawable itemIcon;
	/**
	 * How the item matches the index
	 */
	public int match;
	/**
	 * Position of last match of itemKey
	 */
	public int lastMatch;
	/**
	 * Position of last match of itemKey
	 */
	public int lastMatchImportant;
	/**
	 * Visit times
	 */
	public int visitTimes;
	
	public DataItem(String itemName) {
		super();
		importantPos = new ArrayList<Integer>();
		this.match = 0;
		this.lastMatchImportant = -1;
		this.lastMatch = -1;
		this.itemName = itemName;
		this.itemKey = KeyGenerator.generate(itemName);
		this.importantKey = KeyGenerator.generateImportant(itemKey, importantPos);
		this.visitTimes = 0;
	}
}
