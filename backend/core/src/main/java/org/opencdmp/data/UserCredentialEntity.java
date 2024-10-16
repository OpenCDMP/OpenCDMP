package org.opencdmp.data;

import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UserCredential\"")
public class UserCredentialEntity {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public final static String _id = "id";

	@Column(name = "\"user\"", columnDefinition = "uuid", nullable = false)
	private UUID userId;
	public final static String _userId = "userId";

	@Column(name = "data", nullable = true)
	private String data;
	public final static String _data = "data";

	@Column(name = "\"external_id\"", length = UserCredentialEntity._externalIdLength, nullable = false)
	private String externalId;
	public final static String _externalId = "externalId";
	public final static int _externalIdLength = 512;
	
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public static final String _createdAt = "createdAt";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
