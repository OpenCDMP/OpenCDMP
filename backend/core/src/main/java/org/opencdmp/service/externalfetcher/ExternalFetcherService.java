package org.opencdmp.service.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.SourceBaseConfiguration;
import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import org.opencdmp.service.externalfetcher.models.ExternalDataResult;
import org.opencdmp.service.externalfetcher.models.ExternalFetcherTestResult;

import java.util.List;

public interface ExternalFetcherService {
	ExternalDataResult getExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key ,boolean isTestMode);
	Integer countExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key, boolean isTestMode);
	ExternalFetcherTestResult testSource(SourceBaseConfiguration source, ExternalReferenceCriteria externalReferenceCriteria);
}
