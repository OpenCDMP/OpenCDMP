package org.opencdmp.service.dashborad;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.commons.types.dashborad.RecentActivityItemEntity;
import org.opencdmp.commons.types.referencetype.ReferenceTypeDefinitionEntity;
import org.opencdmp.commons.types.tenantconfiguration.ViewPreferencesConfigurationEntity;
import org.opencdmp.commons.types.viewpreference.ViewPreferenceEntity;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.*;
import org.opencdmp.model.builder.PublicReferenceTypeBuilder;
import org.opencdmp.model.builder.RecentActivityItemBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.*;
import org.opencdmp.query.lookup.DescriptionLookup;
import org.opencdmp.query.lookup.PlanLookup;
import org.opencdmp.service.elastic.ElasticQueryHelperService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.util.*;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DashboardServiceImpl.class));

    private final ConventionService conventionService;
    private final AuthorizationService authorizationService;
    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final UserScopeFactory userScopeFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final JsonHandlingService jsonHandlingService;
    private final XmlHandlingService xmlHandlingService;
    private final ElasticQueryHelperService elasticQueryHelperService;
    private final DashboardStatisticsCacheService dashboardStatisticsCacheService;
    @Autowired
    public DashboardServiceImpl(
		    ConventionService conventionService, AuthorizationService authorizationService,
		    BuilderFactory builderFactory,
		    QueryFactory queryFactory,
            UserScopeFactory userScopeFactory,
            TenantScopeFactory tenantScopeFactory,
		    JsonHandlingService jsonHandlingService, XmlHandlingService xmlHandlingService, ElasticQueryHelperService elasticQueryHelperService, DashboardStatisticsCacheService dashboardStatisticsCacheService) {
	    this.conventionService = conventionService;
	    this.authorizationService = authorizationService;
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.userScopeFactory = userScopeFactory;
        this.tenantScopeFactory = tenantScopeFactory;
	    this.jsonHandlingService = jsonHandlingService;
	    this.xmlHandlingService = xmlHandlingService;
	    this.elasticQueryHelperService = elasticQueryHelperService;
        this.dashboardStatisticsCacheService = dashboardStatisticsCacheService;
    }

    @Override
    public QueryResult<RecentActivityItem> getMyRecentActivityItems(RecentActivityItemLookup model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting recent activity").And("model", model));
        model.setUserIds(List.of(this.userScopeFactory.getInstance().getUserId()));

        long count = 0;
        List<RecentActivityItemEntity> recentActivityItemEntities = new ArrayList<>();
        DescriptionLookup descriptionLookup = model.asDescriptionLookup();
        if (descriptionLookup != null) {
            descriptionLookup.getPage().setOffset(0);
            descriptionLookup.getPage().setSize(model.getPage().getSize()+model.getPage().getOffset());

            QueryResult<Description> descriptions = this.elasticQueryHelperService.collect(descriptionLookup, AuthorizationFlags.AllExceptPublic, new BaseFieldSet().ensure(Description._id).ensure(Description._updatedAt).ensure(String.join(".",Description._status, org.opencdmp.model.descriptionstatus.DescriptionStatus._id)).ensure(Description._label));
            if (!this.conventionService.isListNullOrEmpty(descriptions.getItems())) {
                for (Description description : descriptions.getItems()) recentActivityItemEntities.add(new RecentActivityItemEntity(RecentActivityItemType.Description, description.getId(), description.getUpdatedAt(), description.getLabel(), description.getStatus().getId()));
            }
            count = count + this.elasticQueryHelperService.count(descriptionLookup, AuthorizationFlags.AllExceptPublic);
        }
        
        PlanLookup planLookup = model.asPlanLookup();
        if (planLookup != null){
            planLookup.getPage().setOffset(0);
            planLookup.getPage().setSize(model.getPage().getSize()+model.getPage().getOffset());

            QueryResult<Plan> plans = this.elasticQueryHelperService.collect(planLookup, AuthorizationFlags.AllExceptPublic, new BaseFieldSet().ensure(Plan._id).ensure(Plan._updatedAt).ensure(Plan._label).ensure(String.join(".",Plan._status, org.opencdmp.model.planstatus.PlanStatus._id)));
            if (!this.conventionService.isListNullOrEmpty(plans.getItems())) {
                for (Plan plan : plans.getItems()) recentActivityItemEntities.add(new RecentActivityItemEntity(RecentActivityItemType.Plan, plan.getId(), plan.getUpdatedAt(), plan.getLabel(), plan.getStatus().getId()));
            }
            count = count + this.elasticQueryHelperService.count(planLookup, AuthorizationFlags.AllExceptPublic);
        }

        Comparator<RecentActivityItemEntity> comparator = Comparator.comparing(RecentActivityItemEntity::getUpdatedAt);
        
        if (model.getOrder() != null && model.getOrder().getOrderField() != null) {
            switch (model.getOrder().getOrderField()){
                case Label -> comparator = Comparator.comparing(RecentActivityItemEntity::getLabel).thenComparing(RecentActivityItemEntity::getUpdatedAt);
                case UpdatedAt -> comparator = Comparator.comparing(RecentActivityItemEntity::getUpdatedAt);
                case Status -> comparator = Comparator.comparing(RecentActivityItemEntity::getStatusId).thenComparing(RecentActivityItemEntity::getUpdatedAt);
                default -> throw  new IllegalArgumentException("Type not found" + model.getOrder().getOrderField()) ;
            }
        }

        // Dynamic ASC/DESC
        if (model.getOrder() != null && model.getOrder().getSortDirection() != null && model.getOrder().getSortDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }
        recentActivityItemEntities = recentActivityItemEntities.stream().sorted(comparator).toList();

        if (model.getPage() != null){
            recentActivityItemEntities = recentActivityItemEntities.stream().skip(model.getPage().getOffset()).limit(model.getPage().getSize()).toList();
        }
        return new QueryResult<>(this.builderFactory.builder(RecentActivityItemBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(model.getProject()), recentActivityItemEntities), count);
    }

    @Override
    public DashboardStatistics getPublicDashboardStatistics(){
        this.authorizationService.authorizeForce(Permission.PublicBrowseDashboardStatistics);

        DashboardStatisticsCacheService.DashboardStatisticsCacheValue cacheValue = this.dashboardStatisticsCacheService.lookup(this.dashboardStatisticsCacheService.buildKey(DashboardStatisticsCacheService.publicKey));
        if (cacheValue == null || cacheValue.getDashboardStatistics() == null) {
            PlanStatusQuery planStatusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
            PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).planStatusSubQuery(planStatusQuery).accessTypes(PlanAccessType.Public);
            DashboardStatistics statistics = new DashboardStatistics();
            statistics.setPlanCount(planQuery.authorize(EnumSet.of(Public)).count());
            DescriptionStatusQuery descriptionStatusQuery = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().internalStatuses(DescriptionStatus.Finalized).isActive(IsActive.Active);
            statistics.setDescriptionCount(this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).planSubQuery(planQuery).descriptionStatusSubQuery(descriptionStatusQuery).authorize(EnumSet.of(Public)).count());

            // One counter per featured plan reference type of the default tenant (its view preferences),
            // the installation baseline, since these statistics span every tenant's public plans.
            statistics.setReferenceTypeStatistics(new ArrayList<>());
            for (ReferenceTypeEntity referenceType : this.featuredPlanReferenceTypes()) {
                DashboardReferenceTypeStatistics referenceTypeStatistics = new DashboardReferenceTypeStatistics();
                referenceTypeStatistics.setCount(this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).typeIds(referenceType.getId()).authorize(EnumSet.of(Public))
                        .planReferenceSubQuery(this.queryFactory.query(PlanReferenceQuery.class).disableTracking().isActives(IsActive.Active)
                                .planSubQuery(planQuery)).count());
                referenceTypeStatistics.setReferenceType(this.builderFactory.builder(PublicReferenceTypeBuilder.class).build(new BaseFieldSet().ensure(PublicReferenceType._id).ensure(PublicReferenceType._name).ensure(PublicReferenceType._code), referenceType));
                referenceTypeStatistics.setHasLogo(this.hasLogo(referenceType));
                statistics.getReferenceTypeStatistics().add(referenceTypeStatistics);
            }
            
            cacheValue = new DashboardStatisticsCacheService.DashboardStatisticsCacheValue();
            cacheValue.setPublic(true);
            cacheValue.setDashboardStatistics(statistics);
            this.dashboardStatisticsCacheService.put(cacheValue);
        }
        return cacheValue.getDashboardStatistics();
    }

    @Override
    public DashboardStatistics getMyDashboardStatistics() throws InvalidApplicationException {
        this.authorizationService.authorizeAtLeastOneForce(this.userScopeFactory.getInstance().getUserIdSafe() != null ? List.of(new OwnedResource(this.userScopeFactory.getInstance().getUserIdSafe())) : null);

        DashboardStatisticsCacheService.DashboardStatisticsCacheValue cacheValue = this.dashboardStatisticsCacheService.lookup(this.dashboardStatisticsCacheService.buildKey(this.dashboardStatisticsCacheService.generateUserTenantCacheKey(this.userScopeFactory.getInstance().getUserId(), this.tenantScopeFactory.getInstance().getTenantCode())));
        if (cacheValue == null || cacheValue.getDashboardStatistics() == null) {
            PlanUserQuery planUserLookup = this.queryFactory.query(PlanUserQuery.class).disableTracking();
            planUserLookup.userIds(this.userScopeFactory.getInstance().getUserId());
            planUserLookup.isActives(IsActive.Active);

            PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).planUserSubQuery(planUserLookup);
            
            DashboardStatistics statistics = new DashboardStatistics();
            statistics.setPlanCount(planQuery.authorize(AuthorizationFlags.AllExceptPublic).count());
            statistics.setDescriptionCount(this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).planSubQuery(planQuery).authorize(AuthorizationFlags.AllExceptPublic).count());

            // Signed-in users count their featured reference types through POST api/reference/usage instead.
            statistics.setReferenceTypeStatistics(new ArrayList<>());

            statistics.setPlanBlueprintsCount(this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().isActive(IsActive.Active).statuses(PlanBlueprintStatus.Finalized).versionStatuses(List.of(PlanBlueprintVersionStatus.Current)).authorize(AuthorizationFlags.AllExceptPublic).count());
            statistics.setDescriptionTemplatesCount(this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).statuses(DescriptionTemplateStatus.Finalized).versionStatuses(List.of(DescriptionTemplateVersionStatus.Current)).authorize(AuthorizationFlags.AllExceptPublic).count());

            cacheValue = new DashboardStatisticsCacheService.DashboardStatisticsCacheValue(this.userScopeFactory.getInstance().getUserId(), this.tenantScopeFactory.getInstance().getTenantCode());
            cacheValue.setPublic(false);
            cacheValue.setDashboardStatistics(statistics);
            this.dashboardStatisticsCacheService.put(cacheValue);
        }
        return cacheValue.getDashboardStatistics();
    }

    /** The active reference types the default tenant features on plans (view preferences), in their configured order. */
    private List<ReferenceTypeEntity> featuredPlanReferenceTypes() {
        TenantConfigurationEntity configuration = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking()
                .isActive(IsActive.Active).types(TenantConfigurationType.ViewPreferences).tenantIsSet(false).first();
        if (configuration == null || this.conventionService.isNullOrEmpty(configuration.getValue())) return List.of();

        ViewPreferencesConfigurationEntity preferences = this.jsonHandlingService.fromJsonSafe(ViewPreferencesConfigurationEntity.class, configuration.getValue());
        if (preferences == null || this.conventionService.isListNullOrEmpty(preferences.getPlanPreferences())) return List.of();

        List<UUID> typeIds = preferences.getPlanPreferences().stream()
                .filter(x -> x.getReferenceTypeId() != null)
                .sorted(Comparator.comparing(ViewPreferenceEntity::getOrdinal, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ViewPreferenceEntity::getReferenceTypeId).distinct().toList();
        if (typeIds.isEmpty()) return List.of();

        Map<UUID, ReferenceTypeEntity> types = new HashMap<>();
        for (ReferenceTypeEntity type : this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(typeIds).isActive(IsActive.Active).collect()) types.put(type.getId(), type);
        return typeIds.stream().filter(types::containsKey).map(types::get).toList();
    }

    private boolean hasLogo(ReferenceTypeEntity referenceType) {
        ReferenceTypeDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(ReferenceTypeDefinitionEntity.class, referenceType.getDefinition());
        return definition != null && definition.getSources() != null && definition.getSources().stream().anyMatch(x -> x != null && x.getStorageFileId() != null);
    }
}
