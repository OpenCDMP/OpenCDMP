package org.opencdmp.model;


import java.util.UUID;

public class PublicUser {

	public final static String _id = "id";
	private UUID id;

	public final static String _name = "name";
	private String name = null;

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
}
