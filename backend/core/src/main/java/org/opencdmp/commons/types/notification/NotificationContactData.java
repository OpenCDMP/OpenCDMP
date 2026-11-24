package org.opencdmp.commons.types.notification;

import java.util.List;

public class NotificationContactData {
	private List<ContactPair> contacts;
	private List<ContactPair> bcc;
	private List<ContactPair> cc;

	public NotificationContactData(List<ContactPair> contacts, List<ContactPair> bcc, List<ContactPair> cc) {
		this.contacts = contacts;
		this.bcc = bcc;
		this.cc = cc;
	}

	public List<ContactPair> getContacts() {
		return contacts;
	}

	public void setContacts(List<ContactPair> contacts) {
		this.contacts = contacts;
	}

	public List<ContactPair> getBcc() {
		return bcc;
	}

	public void setBcc(List<ContactPair> bcc) {
		this.bcc = bcc;
	}

	public List<ContactPair> getCc() {
		return cc;
	}

	public void setCc(List<ContactPair> cc) {
		this.cc = cc;
	}
}
