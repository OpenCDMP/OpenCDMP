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
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.dashborad.RecentActivityItemEntity;
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
    private final UserScope userScope;
    private final TenantScope tenantScope;
    private final DashboardServiceProperties config;
    private final ElasticQueryHelperService elasticQueryHelperService;
    private final DashboardStatisticsCacheService dashboardStatisticsCacheService;
    @Autowired
    public DashboardServiceImpl(
		    ConventionService conventionService, AuthorizationService authorizationService,
		    BuilderFactory builderFactory,
		    QueryFactory queryFactory,
            UserScope userScope,
            TenantScope tenantScope,
		    DashboardServiceProperties config, ElasticQueryHelperService elasticQueryHelperService, DashboardStatisticsCacheService dashboardStatisticsCacheService) {
	    this.conventionService = conventionService;
	    this.authorizationService = authorizationService;
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.userScope = userScope;
        this.tenantScope = tenantScope;
	    this.config = config;
	    this.elasticQueryHelperService = elasticQueryHelperService;
        this.dashboardStatisticsCacheService = dashboardStatisticsCacheService;
    }

    @Override
    public List<RecentActivityItem> getMyRecentActivityItems(RecentActivityItemLookup model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting recent activity").And("model", model));
        model.setUserIds(List.of(this.userScope.getUserId()));
        
        List<RecentActivityItemEntity> recentActivityItemEntities = new ArrayList<>();
        DescriptionLookup descriptionLookup = model.asDescriptionLookup();
        if (descriptionLookup != null) {
            descriptionLookup.getPage().setOffset(0);
            descriptionLookup.getPage().setSize(model.getPage().getSize()+model.getPage().getOffset());

            QueryResult<Description> descriptions = this.elasticQueryHelperService.collect(descriptionLookup, AuthorizationFlags.AllExceptPublic, new BaseFieldSet().ensure(Description._id).ensure(Description._updatedAt).ensure(Description._status).ensure(Description._label));
            if (!this.conventionService.isListNullOrEmpty(descriptions.getItems())) {
                for (Description description : descriptions.getItems()) recentActivityItemEntities.add(new RecentActivityItemEntity(RecentActivityItemType.Description, description.getId(), description.getUpdatedAt(), description.getLabel(), description.getStatus().getValue()));
            }
        }
        
        PlanLookup planLookup = model.asPlanLookup();
        if (planLookup != null){
            planLookup.getPage().setOffset(0);
            planLookup.getPage().setSize(model.getPage().getSize()+model.getPage().getOffset());

            QueryResult<Plan> plans = this.elasticQueryHelperService.collect(planLookup, AuthorizationFlags.AllExceptPublic, new BaseFieldSet().ensure(Plan._id).ensure(Plan._updatedAt).ensure(Plan._label).ensure(Plan._status));
            if (!this.conventionService.isListNullOrEmpty(plans.getItems())) {
                for (Plan plan : plans.getItems()) recentActivityItemEntities.add(new RecentActivityItemEntity(RecentActivityItemType.Plan, plan.getId(), plan.getUpdatedAt(), plan.getLabel(), plan.getStatus().getValue()));
            }
        }

        Comparator<RecentActivityItemEntity> comparator = Comparator.comparing(RecentActivityItemEntity::getUpdatedAt);
        
        if (model.getOrderField() != null) {
            switch (model.getOrderField()){
                case Label -> comparator = Comparator.comparing(RecentActivityItemEntity::getLabel).thenComparing(RecentActivityItemEntity::getUpdatedAt);
                case UpdatedAt -> comparator = Comparator.comparing(RecentActivityItemEntity::getUpdatedAt);
                case Status -> comparator = Comparator.comparing(RecentActivityItemEntity::getStatusValue).thenComparing(RecentActivityItemEntity::getUpdatedAt);
                default -> throw  new IllegalArgumentException("Type not found" + model.getOrderField()) ;
            }
        }
        recentActivityItemEntities = recentActivityItemEntities.stream().sorted(comparator).toList().reversed();

        if (model.getPage() != null){
            recentActivityItemEntities = recentActivityItemEntities.stream().skip(model.getPage().getOffset()).limit(model.getPage().getSize()).toList();
        }
        return this.builderFactory.builder(RecentActivityItemBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(model.getProject()), recentActivityItemEntities);
    }

    @Override
    public DashboardStatistics getPublicDashboardStatistics(){
        this.authorizationService.authorizeForce(Permission.PublicBrowseDashboardStatistics);

        DashboardStatisticsCacheService.DashboardStatisticsCacheValue cacheValue = this.dashboardStatisticsCacheService.lookup(this.dashboardStatisticsCacheService.buildKey(DashboardStatisticsCacheService.publicKey));
        if (cacheValue == null || cacheValue.getDashboardStatistics() == null) {
            PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).versionStatuses(PlanVersionStatus.Current).statuses(PlanStatus.Finalized).accessTypes(PlanAccessType.Public);
            DashboardStatistics statistics = new DashboardStatistics();
            statistics.setPlanCount(planQuery.authorize(EnumSet.of(Public)).count());
            statistics.setDescriptionCount(this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).planSubQuery(planQuery).statuses(DescriptionStatus.Finalized).authorize(EnumSet.of(Public)).count());

            statistics.setReferenceTypeStatistics(new ArrayList<>());
            if (!this.conventionService.isListNullOrEmpty(this.config.getReferenceTypeCounters())){
                for (UUID typeId : this.config.getReferenceTypeCounters()){
                    DashboardReferenceTypeStatistics referenceTypeStatistics = new DashboardReferenceTypeStatistics();
                    referenceTypeStatistics.setCount(this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).typeIds(typeId).authorize(EnumSet.of(Public))
                            .planReferenceSubQuery(this.queryFactory.query(PlanReferenceQuery.class).disableTracking().isActives(IsActive.Active)
                                    .planSubQuery(planQuery)).count());
                    referenceTypeStatistics.setReferenceType(this.builderFactory.builder(PublicReferenceTypeBuilder.class).build(new BaseFieldSet().ensure(PublicReferenceType._id), this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(typeId).first()));
                    statistics.getReferenceTypeStatistics().add(referenceTypeStatistics);
                }
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
        this.authorizationService.authorizeAtLeastOneForce(this.userScope.getUserIdSafe() != null ? List.of(new OwnedResource(this.userScope.getUserIdSafe())) : null);

        DashboardStatisticsCacheService.DashboardStatisticsCacheValue cacheValue = this.dashboardStatisticsCacheService.lookup(this.dashboardStatisticsCacheService.buildKey(this.dashboardStatisticsCacheService.generateUserTenantCacheKey(this.userScope.getUserId(), this.tenantScope.getTenantCode())));
        if (cacheValue == null || cacheValue.getDashboardStatistics() == null) {
            PlanUserQuery planUserLookup = this.queryFactory.query(PlanUserQuery.class).disableTracking();
            planUserLookup.userIds(this.userScope.getUserId());
            planUserLookup.isActives(IsActive.Active);

            PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).planUserSubQuery(planUserLookup).versionStatuses(List.of(PlanVersionStatus.Current, PlanVersionStatus.NotFinalized));
            
            DashboardStatistics statistics = new DashboardStatistics();
            statistics.setPlanCount(planQuery.authorize(AuthorizationFlags.AllExceptPublic).count());
            statistics.setDescriptionCount(this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).planSubQuery(planQuery).authorize(AuthorizationFlags.AllExceptPublic).count());

            statistics.setReferenceTypeStatistics(new ArrayList<>());
            if (!this.conventionService.isListNullOrEmpty(this.config.getReferenceTypeCounters())){
                for (UUID typeId : this.config.getReferenceTypeCounters()){
                    DashboardReferenceTypeStatistics referenceTypeStatistics = new DashboardReferenceTypeStatistics();
                    referenceTypeStatistics.setCount(this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).typeIds(typeId).authorize(AuthorizationFlags.AllExceptPublic)
                            .planReferenceSubQuery(this.queryFactory.query(PlanReferenceQuery.class).disableTracking().isActives(IsActive.Active)
                                    .planSubQuery(planQuery)).count());
                    referenceTypeStatistics.setReferenceType(this.builderFactory.builder(PublicReferenceTypeBuilder.class).build(new BaseFieldSet().ensure(PublicReferenceType._id), this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(typeId).first()));
                    statistics.getReferenceTypeStatistics().add(referenceTypeStatistics);
                }
            }
            cacheValue = new DashboardStatisticsCacheService.DashboardStatisticsCacheValue(this.userScope.getUserId(), this.tenantScope.getTenantCode());
            cacheValue.setPublic(false);
            cacheValue.setDashboardStatistics(statistics);
            this.dashboardStatisticsCacheService.put(cacheValue);
        }
        return cacheValue.getDashboardStatistics();
    }
}
