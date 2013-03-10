package com.sulauncher.search;

import com.sulauncher.db.DatabaseManager;

/**
 * A contact item
 * @author YuMS
 */
public class ContactItem extends DataItem{
	public int contactID;
	public ContactItem(String itemName, int contactId) {
		super(itemName);
		this.contactID = contactId;
		this.visitTimes = DatabaseManager.getRecentConTimes(contactId);
	}
}
