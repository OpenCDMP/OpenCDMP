package org.opencdmp.data;

import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.data.converters.enums.ContactInfoTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UserContactInfo\"")
public class UserContactInfoEntity {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public static final String _id = "id";

	@Column(name = "\"user\"", nullable = false)
	private UUID userId;
	public static final String _userId = "userId";

	@Column(name = "\"ordinal\"", nullable = false)
	private Integer ordinal;
	public static final String _ordinal = "ordinal";

	@Column(name = "type", nullable = false)
	@Convert(converter = ContactInfoTypeConverter.class)
	private ContactInfoType type;
	public static final String _type = "type";

	@Column(name = "value", length = UserContactInfoEntity._valueLength, nullable = false)
	private String value;
	public static final int _valueLength = 512;

	public static final String _value = "value";

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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public ContactInfoType getType() {
		return type;
	}

	public void setType(ContactInfoType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}
}
