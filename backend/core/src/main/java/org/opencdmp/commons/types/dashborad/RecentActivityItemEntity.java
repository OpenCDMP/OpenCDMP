package org.opencdmp.commons.types.dashborad;

import org.opencdmp.commons.enums.RecentActivityItemType;

import java.time.Instant;
import java.util.UUID;

public class RecentActivityItemEntity {
	private RecentActivityItemType type;
	private UUID id;
	private Instant updatedAt;
	private String label;
	private Short statusValue;

	public RecentActivityItemEntity(RecentActivityItemType type, UUID id, Instant updatedAt, String label, Short statusValue) {
		this.type = type;
		this.id = id;
		this.updatedAt = updatedAt;
		this.label = label;
		this.statusValue = statusValue;
	}

	public RecentActivityItemType getType() {
		return type;
	}

	public void setType(RecentActivityItemType type) {
		this.type = type;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Short getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(Short statusValue) {
		this.statusValue = statusValue;
	}
}
