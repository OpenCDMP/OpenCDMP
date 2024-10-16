package org.opencdmp.model;

import java.util.UUID;

public class PlanAssociatedUser {

	private UUID id;
	public static final String _id = "id";

	private String name;
	public static final String _name = "name";

	private String email;
	public static final String _email = "email";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
