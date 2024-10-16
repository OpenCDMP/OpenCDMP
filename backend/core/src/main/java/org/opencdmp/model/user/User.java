package org.opencdmp.model.user;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.TenantUser;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.model.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class User {

	private UUID id;
	public static final String _id = "id";

	private String name;
	public static final String _name = "name";

	private Instant createdAt;

	public static final String _createdAt = "createdAt";

	private Instant updatedAt;

	public static final String _updatedAt = "updatedAt";

	private IsActive isActive;

	public static final String _isActive = "isActive";

	private String hash;

	public static final String _hash = "hash";

	private UserAdditionalInfo additionalInfo;

	public static final String _additionalInfo = "additionalInfo";

	private List<UserContactInfo> contacts;

	public static final String _contacts = "contacts";

	private List<UserRole> globalRoles;

	public static final String _globalRoles = "globalRoles";

	private List<UserRole> tenantRoles;

	public static final String _tenantRoles = "tenantRoles";

	private List<UserCredential> credentials;

	public static final String _credentials = "credentials";

	public final static String _tenantUsers = "tenantUsers";
	private List<TenantUser> tenantUsers;

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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public IsActive getIsActive() {
		return isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public UserAdditionalInfo getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(UserAdditionalInfo additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public List<UserContactInfo> getContacts() {
		return contacts;
	}

	public void setContacts(List<UserContactInfo> contacts) {
		this.contacts = contacts;
	}

	public List<UserCredential> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<UserCredential> credentials) {
		this.credentials = credentials;
	}

	public List<TenantUser> getTenantUsers() {
		return tenantUsers;
	}

	public void setTenantUsers(List<TenantUser> tenantUsers) {
		this.tenantUsers = tenantUsers;
	}

	public List<UserRole> getTenantRoles() {
		return tenantRoles;
	}

	public void setTenantRoles(List<UserRole> tenantRoles) {
		this.tenantRoles = tenantRoles;
	}

	public List<UserRole> getGlobalRoles() {
		return globalRoles;
	}

	public void setGlobalRoles(List<UserRole> globalRoles) {
		this.globalRoles = globalRoles;
	}
}
