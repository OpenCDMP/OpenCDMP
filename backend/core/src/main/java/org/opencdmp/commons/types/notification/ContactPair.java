package org.opencdmp.commons.types.notification;


import org.opencdmp.commons.enums.ContactInfoType;

public class ContactPair {
	private ContactInfoType type;
	private String Contact;

	public ContactPair(ContactInfoType type, String contact) {
		this.type = type;
		Contact = contact;
	}

	public ContactPair() {
	}

	public ContactInfoType getType() {
		return type;
	}

	public void setType(ContactInfoType type) {
		this.type = type;
	}

	public String getContact() {
		return Contact;
	}

	public void setContact(String contact) {
		Contact = contact;
	}
}
