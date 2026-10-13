package org.opencdmp.service.kpi;

public class DashboardDefaultsProperties {

	private String referenceTypeId;
	private String planStatusId;
	private String descriptionStatusId;
	private Info adminDashboardInfo;
	private Info tenantDashboardInfo;

	public static class Info {
		private String path;
		private String key;
		private String name;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public String getReferenceTypeId() {
		return referenceTypeId;
	}

	public void setReferenceTypeId(String referenceTypeId) {
		this.referenceTypeId = referenceTypeId;
	}

	public String getPlanStatusId() {
		return planStatusId;
	}

	public void setPlanStatusId(String planStatusId) {
		this.planStatusId = planStatusId;
	}

	public String getDescriptionStatusId() {
		return descriptionStatusId;
	}

	public void setDescriptionStatusId(String descriptionStatusId) {
		this.descriptionStatusId = descriptionStatusId;
	}

	public Info getAdminDashboardInfo() {
		return adminDashboardInfo;
	}

	public void setAdminDashboardInfo(Info adminDashboardInfo) {
		this.adminDashboardInfo = adminDashboardInfo;
	}

	public Info getTenantDashboardInfo() {
		return tenantDashboardInfo;
	}

	public void setTenantDashboardInfo(Info tenantDashboardInfo) {
		this.tenantDashboardInfo = tenantDashboardInfo;
	}
}
