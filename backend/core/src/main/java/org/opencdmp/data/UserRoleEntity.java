package org.opencdmp.data;

import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UserRole\"")
public class UserRoleEntity extends TenantScopedBaseEntity {
	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public static final String _id = "id";

	@Column(name = "role", length = UserRoleEntity._roleLength, nullable = false)
	private String role;
	public static final String _role = "role";
	public static final int _roleLength = 512;

	@Column(name = "\"user\"", nullable = false)
	private UUID userId;
	public static final String _userId = "userId";


	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public static final String _createdAt = "createdAt";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

}
