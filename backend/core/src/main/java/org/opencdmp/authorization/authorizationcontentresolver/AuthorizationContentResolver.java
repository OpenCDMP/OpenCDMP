package org.opencdmp.authorization.authorizationcontentresolver;

import org.opencdmp.authorization.AffiliatedResource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AuthorizationContentResolver {
	List<String> getPermissionNames();

	AffiliatedResource planAffiliation(UUID id);

	Map<UUID, AffiliatedResource> plansAffiliation(List<UUID> ids);

	AffiliatedResource descriptionTemplateAffiliation(UUID id);

	Map<UUID, AffiliatedResource> descriptionTemplateAffiliation(List<UUID> ids);

	boolean hasAtLeastOneDescriptionTemplateAffiliation();

	AffiliatedResource descriptionAffiliation(UUID id);

	Map<UUID, AffiliatedResource> descriptionsAffiliation(List<UUID> ids);

	AffiliatedResource descriptionsAffiliationBySection(UUID planId, String sectionId);

	Map<String, AffiliatedResource> descriptionsAffiliationBySections(UUID planId, List<String> sectionIds);
}
