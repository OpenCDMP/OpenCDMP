package org.opencdmp.service.accounting;

import org.opencdmp.commons.types.accounting.AccountingSourceEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.util.List;

@ConfigurationProperties(prefix = "accounting")
public class AccountingProperties {
	private Boolean enabled;
	private String serviceId;
	private Instant fromInstant;
	private String subjectId;
	private List<String> projectFields;

	private List<AccountingSourceEntity> sources;

	public String getServiceId() {
		return serviceId;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Instant getFromInstant() {
		return fromInstant;
	}

	public void setFromInstant(Instant fromInstant) {
		this.fromInstant = fromInstant;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public List<String> getProjectFields() {
		return projectFields;
	}

	public void setProjectFields(List<String> projectFields) {
		this.projectFields = projectFields;
	}

	public List<AccountingSourceEntity> getSources() {
		return sources;
	}

	public void setSources(List<AccountingSourceEntity> sources) {
		this.sources = sources;
	}
}
