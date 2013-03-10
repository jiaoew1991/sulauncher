package com.sulauncher.search;

import java.util.LinkedList;

/**
 * A list with a tag indicating what type its content is
 * @author YuMS
 */
public class ListWithTag {
	public static enum ListTags{APP, CON, RECENTAPP};
	private LinkedList<? extends DataItem> list;
	private ListTags tag;
	private int numInList;
	public ListWithTag(LinkedList<? extends DataItem> list, ListTags tag) {
		this.list = list;
		this.tag = tag;
	}
	public void setTag(ListTags tag) {
		this.tag = tag;
	}
	public ListTags getTag() {
		return tag;
	}
	public void setList(LinkedList<? extends DataItem> list) {
		this.list = list;
	}
	public LinkedList<? extends DataItem> getList() {
		return list;
	}
	public int getNumInList() {
		return numInList;
	}
	public void setNumInList(int numInList) {
		this.numInList = numInList;
	}
	public void numInListInc(int inc) {
		this.numInList += inc;
	}
}