package org.opencdmp.service.custompolicy;

import gr.cite.commons.web.authz.configuration.Permission;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinitionAuthorization;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.planstatus.PlanStatusDefinitionAuthorization;
import org.opencdmp.query.DescriptionStatusQuery;
import org.opencdmp.query.PlanStatusQuery;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.util.*;

@Service
public class CustomPolicyServiceImpl implements CustomPolicyService{
	private final QueryFactory queryFactory;
	private final XmlHandlingService xmlHandlingService;
	private final TenantScope tenantScope;
	private final CustomPolicyCacheService customPolicyCacheService;
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(CustomPolicyServiceImpl.class));

	public CustomPolicyServiceImpl(QueryFactory queryFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope, CustomPolicyCacheService customPolicyCacheService) {
		this.queryFactory = queryFactory;
		this.xmlHandlingService = xmlHandlingService;
        this.tenantScope = tenantScope;
        this.customPolicyCacheService = customPolicyCacheService;
    }

	@Override
	public HashMap<String, Permission> buildPlanStatusPolicies() {
		HashMap<String, Permission> policies = new HashMap<>();
		Map<UUID, PlanStatusDefinitionEntity> map = this.buildPlanStatusDefinitionMap();
		if (map == null) return policies;

		for (UUID statusId: map.keySet()) {
			PlanStatusDefinitionEntity definition = map.get(statusId);
			if (definition != null && definition.getAuthorization() != null && definition.getAuthorization().getEdit() != null) {
				policies.put(this.getPlanStatusCanEditStatusPermission(statusId), new Permission(new HashSet<>(definition.getAuthorization().getEdit().getRoles()), new ArrayList<>(), new HashSet<>(), definition.getAuthorization().getEdit().getAllowAnonymous(), definition.getAuthorization().getEdit().getAllowAuthenticated()));
			}
		}

		return policies;
	}

	@Override
	public HashMap<String, Permission> buildDescriptionStatusPolicies(){
		HashMap<String, Permission> policies = new HashMap<>();
		Map<UUID, DescriptionStatusDefinitionEntity> map = this.buildDescriptionStatusDefinitionMap();
		if (map == null) return policies;

		for (UUID statusId: map.keySet()) {
			DescriptionStatusDefinitionEntity definition = map.get(statusId);
			if (definition != null && definition.getAuthorization() != null && definition.getAuthorization().getEdit() != null) {
				policies.put(this.getDescriptionStatusCanEditStatusPermission(statusId), new Permission(new HashSet<>(definition.getAuthorization().getEdit().getRoles()), new ArrayList<>(), new HashSet<>(), definition.getAuthorization().getEdit().getAllowAnonymous(), definition.getAuthorization().getEdit().getAllowAuthenticated()));
			}
		}

		return policies;
	}

	@Override
	public Map<UUID, PlanStatusDefinitionEntity> buildPlanStatusDefinitionMap() {
		HashMap<String, Permission> policies = new HashMap<>();
		String tenantCode = null;
		try {
			tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : this.tenantScope.getDefaultTenantCode();
		} catch (InvalidApplicationException e) {
			throw new RuntimeException(e);
		}
		CustomPolicyCacheService.CustomPolicyCacheValue cacheValue = this.customPolicyCacheService.lookup(this.customPolicyCacheService.buildKey(tenantCode));
		if (cacheValue == null || cacheValue.getPlanStatusDefinitionMap() == null) {
			Map<UUID, PlanStatusDefinitionEntity> definitionStatusMap = new HashMap<>();
			List<PlanStatusEntity> entities = this.queryFactory.query(PlanStatusQuery.class).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanStatus._id).ensure(PlanStatus._definition));
			for (PlanStatusEntity entity: entities) {
				PlanStatusDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(PlanStatusDefinitionEntity.class, entity.getDefinition());
				if (definition != null) {
					definitionStatusMap.put(entity.getId(), definition);
				}
			}

			cacheValue = new CustomPolicyCacheService.CustomPolicyCacheValue(tenantCode, definitionStatusMap, cacheValue != null ? cacheValue.getDescriptionStatusDefinitionMap() : null);
			this.customPolicyCacheService.put(cacheValue);
		}

		return cacheValue.getPlanStatusDefinitionMap();
	}

	@Override
	public Map<UUID, DescriptionStatusDefinitionEntity> buildDescriptionStatusDefinitionMap(){
		HashMap<String, Permission> policies = new HashMap<>();
		String tenantCode = null;
		try {
			tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : this.tenantScope.getDefaultTenantCode();
		} catch (InvalidApplicationException e) {
			throw new RuntimeException(e);
		}
		CustomPolicyCacheService.CustomPolicyCacheValue cacheValue = this.customPolicyCacheService.lookup(this.customPolicyCacheService.buildKey(tenantCode));
		if (cacheValue == null || cacheValue.getDescriptionStatusDefinitionMap() == null) {
			Map<UUID, DescriptionStatusDefinitionEntity> definitionStatusMap = new HashMap<>();
			List<DescriptionStatusEntity> entities = this.queryFactory.query(DescriptionStatusQuery.class).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._definition));
			for (DescriptionStatusEntity entity : entities) {
				DescriptionStatusDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DescriptionStatusDefinitionEntity.class, entity.getDefinition());
				if (definition != null) {
					definitionStatusMap.put(entity.getId(), definition);
				}
			}

			cacheValue = new CustomPolicyCacheService.CustomPolicyCacheValue(tenantCode, cacheValue != null ? cacheValue.getPlanStatusDefinitionMap(): null, definitionStatusMap);
			this.customPolicyCacheService.put(cacheValue);
		}

		return cacheValue.getDescriptionStatusDefinitionMap();
	}

	@Override
	public String getPlanStatusCanEditStatusPermission(UUID id){
		return ("PlanStatus" + "_" + id + "_" + PlanStatusDefinitionAuthorization._edit).toLowerCase();
	}

	@Override
	public String getDescriptionStatusCanEditStatusPermission(UUID id){
		return ("DescriptionStatus" + "_" + id + "_" + DescriptionStatusDefinitionAuthorization._edit).toLowerCase();
	}
}
