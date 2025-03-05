package org.opencdmp.commons.types.dashborad;

import org.opencdmp.commons.enums.RecentActivityItemType;

import java.time.Instant;
import java.util.UUID;

public class RecentActivityItemEntity {
	private RecentActivityItemType type;
	private UUID id;
	private Instant updatedAt;
	private String label;
	private UUID statusId;

	public RecentActivityItemEntity(RecentActivityItemType type, UUID id, Instant updatedAt, String label, UUID statusId) {
		this.type = type;
		this.id = id;
		this.updatedAt = updatedAt;
		this.label = label;
		this.statusId = statusId;
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

	public UUID getStatusId() {
		return statusId;
	}

	public void setStatusId(UUID statusId) {
		this.statusId = statusId;
	}
}
