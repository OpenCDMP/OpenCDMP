package org.opencdmp.commons.types.notification;

import java.util.List;

public class NotificationContactData {
	private List<ContactPair> contacts;
	private List<ContactPair> BCC;
	private List<ContactPair> CC;

	public NotificationContactData(List<ContactPair> contacts, List<ContactPair> BCC, List<ContactPair> CC) {
		this.contacts = contacts;
		this.BCC = BCC;
		this.CC = CC;
	}

	public List<ContactPair> getContacts() {
		return contacts;
	}

	public void setContacts(List<ContactPair> contacts) {
		this.contacts = contacts;
	}

	public List<ContactPair> getBCC() {
		return BCC;
	}

	public void setBCC(List<ContactPair> BCC) {
		this.BCC = BCC;
	}

	public List<ContactPair> getCC() {
		return CC;
	}

	public void setCC(List<ContactPair> CC) {
		this.CC = CC;
	}
}
