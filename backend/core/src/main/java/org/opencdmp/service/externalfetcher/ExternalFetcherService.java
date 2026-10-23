package org.opencdmp.service.externalfetcher;

import org.opencdmp.commons.types.externalfetcher.AuthenticationConfigurationEntity;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherBaseSourceConfigurationEntity;
import org.opencdmp.model.persist.externalfetcher.AuthenticationConfigurationPersist;
import org.opencdmp.model.persist.externalfetcher.ExternalFetcherBaseSourceConfigurationPersist;
import org.opencdmp.service.externalfetcher.config.entities.SourceBaseConfiguration;
import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import org.opencdmp.service.externalfetcher.models.ExternalDataResult;
import org.opencdmp.service.externalfetcher.models.ExternalFetcherTestResult;

import java.util.List;

public interface ExternalFetcherService {
	ExternalDataResult getExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key ,boolean isTestMode);
	Integer countExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key, boolean isTestMode);
	ExternalFetcherTestResult testSource(SourceBaseConfiguration source, ExternalReferenceCriteria externalReferenceCriteria);
	String authRequestBodyToStore(AuthenticationConfigurationPersist persist, AuthenticationConfigurationEntity old);
	void useStoredAuthRequestBodies(List<ExternalFetcherBaseSourceConfigurationPersist> sources, List<ExternalFetcherBaseSourceConfigurationEntity> stored);
}
