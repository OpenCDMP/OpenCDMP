package org.opencdmp.model;

import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class UserContactInfo {
	private UUID id;
	public static final String _id = "id";

	private String value;
	public static final String _value = "value";

	private ContactInfoType type;
	public static final String _type = "type";
	
	private int ordinal;
	public static final String _ordinal = "ordinal";

	private User user;
	public static final String _user = "user";

	private Instant createdAt;

	public static final String _createdAt = "createdAt";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ContactInfoType getType() {
		return type;
	}

	public void setType(ContactInfoType type) {
		this.type = type;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
