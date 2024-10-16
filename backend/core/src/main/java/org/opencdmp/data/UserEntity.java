package org.opencdmp.data;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"User\"")
public class UserEntity {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public final static String _id = "id";

	@Column(name = "name", length = UserEntity._nameLength, nullable = true)
	private String name = null;
	public final static String _name = "name";
	public final static int _nameLength = 250;


	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public static final String _createdAt = "createdAt";

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public static final String _updatedAt = "updatedAt";

	@Column(name = "is_active", nullable = false)
	@Convert(converter = IsActiveConverter.class)
	private IsActive isActive;
	public static final String _isActive = "isActive";

	@Column(name = "additional_info", nullable = true)
	private String additionalInfo;
	public final static String _additionalInfo = "additionalInfo";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public IsActive getIsActive() {
		return isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
