package org.opencdmp.model.usercredential;

import org.opencdmp.model.user.User;
import org.opencdmp.model.usercredential.UserCredentialData;

import java.time.Instant;
import java.util.UUID;

public class UserCredential {
	private UUID id;
	public static final String _id = "id";

	private String externalId;
	public static final String _externalId = "externalId";

	private User user;
	public static final String _user = "user";

	private Instant createdAt;

	public static final String _createdAt = "createdAt";
	private UserCredentialData data;
	public static final String _data = "data";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
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

	public UserCredentialData getData() {
		return data;
	}

	public void setData(UserCredentialData data) {
		this.data = data;
	}
}
