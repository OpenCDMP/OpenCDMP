package org.opencdmp.service.externalfetcher;

import org.opencdmp.service.externalfetcher.config.entities.SourceBaseConfiguration;
import org.opencdmp.service.externalfetcher.criteria.ExternalReferenceCriteria;
import org.opencdmp.service.externalfetcher.models.ExternalDataResult;

import java.util.List;

public interface ExternalFetcherService {
	ExternalDataResult getExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key);
	Integer countExternalData(List<SourceBaseConfiguration> sources, ExternalReferenceCriteria externalReferenceCriteria, String key);
}
