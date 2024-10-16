package org.opencdmp.integrationevent.outbox.tenantdefaultlocaletouched;

import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class TenantDefaultLocaleTouchedIntegrationEvent extends TrackedEvent {

	private UUID tenantId;
	private String timezone;
	private String language;
	private String culture;


	public TenantDefaultLocaleTouchedIntegrationEvent() {
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}
}
