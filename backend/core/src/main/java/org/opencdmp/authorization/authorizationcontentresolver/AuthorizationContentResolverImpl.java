package org.opencdmp.authorization.authorizationcontentresolver;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.PermissionNameProvider;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.*;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.UserDescriptionTemplate;
import org.opencdmp.model.description.Description;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanDescriptionTemplateQuery;
import org.opencdmp.query.PlanUserQuery;
import org.opencdmp.query.UserDescriptionTemplateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequestScope
public class AuthorizationContentResolverImpl implements AuthorizationContentResolver {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(AuthorizationContentResolverImpl.class));
	private static final Logger log = LoggerFactory.getLogger(AuthorizationContentResolverImpl.class);
	private final QueryFactory queryFactory;
	private final UserScope userScope;
	private final TenantScope tenantScope;
	private final AffiliationCacheService affiliationCacheService;
	private final PermissionNameProvider permissionNameProvider;
	private final TenantEntityManager tenantEntityManager;
	public AuthorizationContentResolverImpl(QueryFactory queryFactory, UserScope userScope, TenantScope tenantScope, AffiliationCacheService affiliationCacheService, PermissionNameProvider permissionNameProvider, TenantEntityManager tenantEntityManager) {
		this.queryFactory = queryFactory;
		this.userScope = userScope;
		this.tenantScope = tenantScope;
		this.affiliationCacheService = affiliationCacheService;
		this.permissionNameProvider = permissionNameProvider;
		this.tenantEntityManager = tenantEntityManager;
	}
	
	@Override
	public List<String> getPermissionNames() {
		return this.permissionNameProvider.getPermissions();
	}

	@Override
	public AffiliatedResource planAffiliation(UUID id) {
		return this.plansAffiliation(List.of(id)).getOrDefault(id, new AffiliatedResource());
	}
	@Override
	public Map<UUID, AffiliatedResource> plansAffiliation(List<UUID> ids){
		UUID userId = this.userScope.getUserIdSafe();
		Map<UUID, AffiliatedResource> affiliatedResources = new HashMap<>();
		for (UUID id : ids){
			affiliatedResources.put(id, new AffiliatedResource());
		}
		if (userId == null || !this.userScope.isSet()) return affiliatedResources;

		List<UUID> idsToResolve = this.getAffiliatedFromCache(ids, userId, affiliatedResources, PlanEntity.class.getSimpleName());
		if (idsToResolve.isEmpty()) return affiliatedResources;
		List<PlanUserEntity> planUsers;
		try {
			this.tenantEntityManager.loadExplicitTenantFilters();
			planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(ids).sectionIsEmpty(true).userIds(userId).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanUser._role).ensure(PlanUser._plan));
		} catch (InvalidApplicationException e) {
			log.error(e.getMessage(), e);
			throw new MyApplicationException(e.getMessage());
		} finally {
			try {
				this.tenantEntityManager.reloadTenantFilters();
			} catch (InvalidApplicationException e) {
				log.error(e.getMessage(), e);
				throw new MyApplicationException(e.getMessage());
			}
		}
		for (PlanUserEntity planUser : planUsers){
			affiliatedResources.get(planUser.getPlanId()).getPlanUserRoles().add(planUser.getRole());
		}

		this.ensureAffiliatedInCache(idsToResolve, userId, affiliatedResources, PlanEntity.class.getSimpleName());
		return affiliatedResources;
	}

	@Override
	public AffiliatedResource descriptionTemplateAffiliation(UUID id) {
		return this.descriptionTemplateAffiliation(List.of(id)).getOrDefault(id, new AffiliatedResource());
	}

	@Override
	public Map<UUID, AffiliatedResource> descriptionTemplateAffiliation(List<UUID> ids){
		UUID userId = this.userScope.getUserIdSafe();
		Map<UUID, AffiliatedResource> affiliatedResources = new HashMap<>();
		for (UUID id : ids){
			affiliatedResources.put(id, new AffiliatedResource());
		}
		if (userId == null || !this.userScope.isSet()) return affiliatedResources;

		List<UUID> idsToResolve = this.getAffiliatedFromCache(ids, userId, affiliatedResources, DescriptionTemplateEntity.class.getSimpleName());
		if (idsToResolve.isEmpty()) return affiliatedResources;

		List<UserDescriptionTemplateEntity> userDescriptionTemplates;
		try {
			this.tenantEntityManager.loadExplicitTenantFilters();
			userDescriptionTemplates = this.queryFactory.query(UserDescriptionTemplateQuery.class).disableTracking().descriptionTemplateIds(ids).userIds(userId).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(UserDescriptionTemplate._role).ensure(UserDescriptionTemplate._descriptionTemplate));
		} catch (InvalidApplicationException e) {
			log.error(e.getMessage(), e);
			throw new MyApplicationException(e.getMessage());
		} finally {
			try {
				this.tenantEntityManager.reloadTenantFilters();
			} catch (InvalidApplicationException e) {
				log.error(e.getMessage(), e);
				throw new MyApplicationException(e.getMessage());
			}
		}

		for (UserDescriptionTemplateEntity planUser : userDescriptionTemplates){
			affiliatedResources.get(planUser.getDescriptionTemplateId()).getUserDescriptionTemplateRoles().add(planUser.getRole());
		}

		this.ensureAffiliatedInCache(idsToResolve, userId, affiliatedResources, DescriptionTemplateEntity.class.getSimpleName());
		return affiliatedResources;
	}

	@Override
	public boolean hasAtLeastOneDescriptionTemplateAffiliation(){
		UUID userId = this.userScope.getUserIdSafe();
		if (userId == null || !this.userScope.isSet()) return false;

		//TODO: investigate if we want to use cache 
		boolean hasAny;
		try {
			this.tenantEntityManager.loadExplicitTenantFilters();
			hasAny = this.queryFactory.query(UserDescriptionTemplateQuery.class).disableTracking().userIds(userId).isActive(IsActive.Active).count() > 0;
		} catch (InvalidApplicationException e) {
			log.error(e.getMessage(), e);
			throw new MyApplicationException(e.getMessage());
		} finally {
			try {
				this.tenantEntityManager.reloadTenantFilters();
			} catch (InvalidApplicationException e) {
				log.error(e.getMessage(), e);
				throw new MyApplicationException(e.getMessage());
			}
		}
		
		return hasAny;
	}

	@Override
	public AffiliatedResource descriptionAffiliation(UUID id) {
		return this.descriptionsAffiliation(List.of(id)).getOrDefault(id, new AffiliatedResource());
	}
	@Override
	public Map<UUID, AffiliatedResource> descriptionsAffiliation(List<UUID> ids){
		UUID userId = this.userScope.getUserIdSafe();
		Map<UUID, AffiliatedResource> affiliatedResources = new HashMap<>();
		for (UUID id : ids){
			affiliatedResources.put(id, new AffiliatedResource());
		}
		if (userId == null || !this.userScope.isSet()) return affiliatedResources;

		List<UUID> idsToResolve = this.getAffiliatedFromCache(ids, userId, affiliatedResources, DescriptionEntity.class.getSimpleName());
		if (idsToResolve.isEmpty()) return affiliatedResources;

		List<PlanDescriptionTemplateEntity> planDescriptionTemplateEntities;
		List<PlanUserEntity> planUsers;
		List<DescriptionEntity> descriptionEntities;
		try {
			this.tenantEntityManager.loadExplicitTenantFilters();
			descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(ids).collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._planDescriptionTemplate).ensure(Description._plan));
			planDescriptionTemplateEntities = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().ids(descriptionEntities.stream().map(DescriptionEntity::getPlanDescriptionTemplateId).distinct().toList()).collectAs(new BaseFieldSet().ensure(PlanDescriptionTemplate._id).ensure(PlanDescriptionTemplate._sectionId));
			planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().descriptionIds(ids).userIds(userId).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanUser._role).ensure(PlanUser._sectionId).ensure(PlanUser._plan));

		} catch (InvalidApplicationException e) {
			log.error(e.getMessage(), e);
			throw new MyApplicationException(e.getMessage());
		} finally {
			try {
				this.tenantEntityManager.reloadTenantFilters();
			} catch (InvalidApplicationException e) {
				log.error(e.getMessage(), e);
				throw new MyApplicationException(e.getMessage());
			}
		}



		Map<UUID, PlanDescriptionTemplateEntity> planDescriptionTemplateEntityMap = planDescriptionTemplateEntities == null ? new HashMap<>() : planDescriptionTemplateEntities.stream().collect(Collectors.toMap(PlanDescriptionTemplateEntity::getId, x-> x));
		Map<UUID, List<PlanUserEntity>> planUsersMap = planUsers.stream().collect(Collectors.groupingBy(PlanUserEntity::getPlanId));
		
		for (DescriptionEntity description : descriptionEntities){
			List<PlanUserEntity> planDescriptionUsers = planUsersMap.getOrDefault(description.getPlanId(), new ArrayList<>());
			for (PlanUserEntity planUser : planDescriptionUsers) {
				if (planUser.getSectionId() == null) affiliatedResources.get(description.getId()).getPlanUserRoles().add(planUser.getRole());
				else {
					PlanDescriptionTemplateEntity planDescriptionTemplateEntity = planDescriptionTemplateEntityMap.getOrDefault(description.getPlanDescriptionTemplateId(), null);
					if (planDescriptionTemplateEntity != null && planUser.getSectionId().equals(planDescriptionTemplateEntity.getSectionId())){
						affiliatedResources.get(description.getId()).getPlanUserRoles().add(planUser.getRole());
					}
				}
			}
		}

		this.ensureAffiliatedInCache(idsToResolve, userId, affiliatedResources, DescriptionEntity.class.getSimpleName());
		return affiliatedResources;
	}

	@Override
	public AffiliatedResource descriptionsAffiliationBySection(UUID planId, UUID sectionId){
		return this.descriptionsAffiliationBySections(planId, List.of(sectionId)).getOrDefault(sectionId, new AffiliatedResource());
	}

	@Override
	public Map<UUID, AffiliatedResource> descriptionsAffiliationBySections(UUID planId, List<UUID> sectionIds){
		UUID userId = this.userScope.getUserIdSafe();
		Map<UUID, AffiliatedResource> affiliatedResources = new HashMap<>();
		for (UUID id : sectionIds){
			affiliatedResources.put(id, new AffiliatedResource());
		}
		if (userId == null || !this.userScope.isSet()) return affiliatedResources;


		List<PlanUserEntity> planUsers;
		try {
			this.tenantEntityManager.loadExplicitTenantFilters();
			planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(planId).userIds(userId).isActives(IsActive.Active).collectAs(new BaseFieldSet().ensure(PlanUser._role).ensure(PlanUser._sectionId).ensure(PlanUser._plan));
		} catch (InvalidApplicationException e) {
			log.error(e.getMessage(), e);
			throw new MyApplicationException(e.getMessage());
		} finally {
			try {
				this.tenantEntityManager.reloadTenantFilters();
			} catch (InvalidApplicationException e) {
				log.error(e.getMessage(), e);
				throw new MyApplicationException(e.getMessage());
			}
		}

		for (UUID sectionId : sectionIds.stream().distinct().toList()){
			List<PlanUserEntity> planSectionUsers = planUsers.stream().filter(x-> x.getSectionId() == null || x.getSectionId().equals(sectionId)).toList();
			for (PlanUserEntity planUser : planSectionUsers) {
				if (planUser.getSectionId() == null) affiliatedResources.get(sectionId).getPlanUserRoles().add(planUser.getRole());
				else {
					if (planUser.getSectionId().equals(sectionId)){
						affiliatedResources.get(sectionId).getPlanUserRoles().add(planUser.getRole());
					}
				}
			}
		}

		return affiliatedResources;
	}

	private List<UUID> getAffiliatedFromCache(List<UUID> ids, UUID userId, Map<UUID, AffiliatedResource> affiliatedResources, String entityType)  {
		List<UUID> idsToResolve = new ArrayList<>();
		for (UUID id : ids){
			AffiliationCacheService.AffiliationCacheValue cacheValue = null;
			try {
				cacheValue = this.affiliationCacheService.lookup(this.affiliationCacheService.buildKey(this.tenantScope.isSet() ? this.tenantScope.getTenant(): null, userId, id, entityType));
			} catch (InvalidApplicationException e) {
				throw new RuntimeException(e);
			}
			if (cacheValue != null) affiliatedResources.put(id, cacheValue.getAffiliatedResource());
			else  idsToResolve.add(id);
		}
		return idsToResolve;
	}
	
	private void ensureAffiliatedInCache(List<UUID> idsToResolve, UUID userId, Map<UUID, AffiliatedResource> affiliatedResources, String entityType)  {
		for (UUID id : idsToResolve){
			AffiliatedResource affiliatedResource = affiliatedResources.getOrDefault(id, null);
			if (affiliatedResource != null) {
				AffiliationCacheService.AffiliationCacheValue cacheValue = null;
				try {
					cacheValue = new AffiliationCacheService.AffiliationCacheValue(this.tenantScope.isSet() ? this.tenantScope.getTenant(): null, userId, id, entityType, affiliatedResource);
				} catch (InvalidApplicationException e) {
					throw new RuntimeException(e);
				}
				this.affiliationCacheService.put(cacheValue);
			}
		}
	}
}
