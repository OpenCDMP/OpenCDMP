package org.opencdmp.data;

import gr.cite.queueoutbox.entity.QueueOutbox;
import gr.cite.queueoutbox.entity.QueueOutboxNotifyStatus;
import jakarta.persistence.*;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.QueueOutboxNotifyStatusConverter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"QueueOutbox\"")
public class QueueOutboxEntity implements QueueOutbox {
	@Id
	@Column(name = "\"id\"", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public final static String _id = "id";

	@Column(name = "\"exchange\"", nullable = false, length = 200)
	private String exchange;
	public final static String _exchange = "exchange";

	@Column(name = "\"route\"", length = 200)
	private String route;
	public final static String _route = "route";

	@Column(name = "\"message_id\"", columnDefinition = "uuid", nullable = false)
	private UUID messageId;
	public final static String _messageId = "messageId";

	@Column(name = "\"message\"", columnDefinition = "json", nullable = false)
	private String message;
	public final static String _message = "message";

	@Column(name = "\"notify_status\"", nullable = false)
	@Convert(converter = QueueOutboxNotifyStatusConverter.class)
	private QueueOutboxNotifyStatus notifyStatus;
	public final static String _notifyStatus = "notifyStatus";

	@Column(name = "\"retry_count\"", nullable = false)
	private int retryCount;
	public final static String _retryCount = "retryCount";

	@Column(name = "\"published_at\"", nullable = true)
	private Instant publishedAt;
	public final static String _publishedAt = "publishedAt";

	@Column(name = "\"confirmed_at\"", nullable = true)
	private Instant confirmedAt;
	public final static String _confirmedAt = "confirmedAt";

	@Column(name = "\"tenant\"", columnDefinition = "uuid", nullable = true)
	private UUID tenantId;
	public final static String _tenantId = "tenantId";

	@Column(name = "\"is_active\"", nullable = false)
	@Convert(converter = IsActiveConverter.class)
	private IsActive isActive;
	public final static String _isActive = "isActive";

	@Column(name = "\"created_at\"", nullable = false)
	private Instant createdAt;
	public final static String _createdAt = "createdAt";

	@Column(name = "\"updated_at\"", nullable = false)
	@Version
	private Instant updatedAt;
	public final static String _updatedAt = "updatedAt";

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getExchange() {
		return this.exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getRoute() {
		return this.route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public UUID getMessageId() {
		return this.messageId;
	}

	public void setMessageId(UUID messageId) {
		this.messageId = messageId;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public QueueOutboxNotifyStatus getNotifyStatus() {
		return this.notifyStatus;
	}

	public void setNotifyStatus(QueueOutboxNotifyStatus notifyStatus) {
		this.notifyStatus = notifyStatus;
	}

	public Integer getRetryCount() {
		return this.retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public Instant getPublishedAt() {
		return this.publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}

	public Instant getConfirmedAt() {
		return this.confirmedAt;
	}

	public void setConfirmedAt(Instant confirmedAt) {
		this.confirmedAt = confirmedAt;
	}

	public UUID getTenantId() {
		return this.tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public IsActive getIsActive() {
		return this.isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}

