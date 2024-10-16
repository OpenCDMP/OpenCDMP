package org.opencdmp.service.publicapi;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "public-api")
public class PublicApiProperties {
	private ReferenceTypeMapConfig referenceTypeMap;
	public ReferenceTypeMapConfig getReferenceTypeMap() {
		return referenceTypeMap;
	}

	public void setReferenceTypeMap(ReferenceTypeMapConfig referenceTypeMap) {
		this.referenceTypeMap = referenceTypeMap;
	}

	public static class ReferenceTypeMapConfig{
		private UUID funderTypeId;
		private UUID grantTypeId;
		private UUID registryTypeId;
		private UUID serviceTypeId;
		private UUID dataRepositoryTypeId;
		private UUID datasetTypeId;
		private UUID researcherTypeId;
		private UUID organizationTypeId;
		public UUID getFunderTypeId() {
			return funderTypeId;
		}

		public void setFunderTypeId(UUID funderTypeId) {
			this.funderTypeId = funderTypeId;
		}

		public UUID getGrantTypeId() {
			return grantTypeId;
		}

		public void setGrantTypeId(UUID grantTypeId) {
			this.grantTypeId = grantTypeId;
		}

		public UUID getRegistryTypeId() {
			return registryTypeId;
		}

		public void setRegistryTypeId(UUID registryTypeId) {
			this.registryTypeId = registryTypeId;
		}

		public UUID getServiceTypeId() {
			return serviceTypeId;
		}

		public void setServiceTypeId(UUID serviceTypeId) {
			this.serviceTypeId = serviceTypeId;
		}

		public UUID getDataRepositoryTypeId() {
			return dataRepositoryTypeId;
		}

		public void setDataRepositoryTypeId(UUID dataRepositoryTypeId) {
			this.dataRepositoryTypeId = dataRepositoryTypeId;
		}

		public UUID getDatasetTypeId() {
			return datasetTypeId;
		}

		public void setDatasetTypeId(UUID datasetTypeId) {
			this.datasetTypeId = datasetTypeId;
		}

		public UUID getResearcherTypeId() {
			return researcherTypeId;
		}

		public void setResearcherTypeId(UUID researcherTypeId) {
			this.researcherTypeId = researcherTypeId;
		}

		public UUID getOrganizationTypeId() {
			return organizationTypeId;
		}

		public void setOrganizationTypeId(UUID organizationTypeId) {
			this.organizationTypeId = organizationTypeId;
		}
	}
}

