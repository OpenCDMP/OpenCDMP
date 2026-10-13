package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.TenantUserEntity;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.TenantUser;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.TenantUserQuery;
import org.opencdmp.query.UserQuery;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantBuilder extends BaseBuilder<Tenant, TenantEntity> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize  = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public TenantBuilder(ConventionService conventionService, BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantEntityManagerFactory tenantEntityManagerFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(TenantBuilder.class)));
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.queryFactory = queryFactory;
    }

    public TenantBuilder authorize(EnumSet<AuthorizationFlags> values){
        this.authorize = values;
        return this;
    }
    @Override
    public List<Tenant> build(FieldSet fields, List<TenantEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0),Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size) .orElse(0));
        this.logger.trace(new DataLogEntry("requested fields",fields));
        if(fields == null || data == null || fields.isEmpty()) return new ArrayList<>();


        List<UUID> ids = data.stream().map(TenantEntity::getId).toList();
        Map<UUID, Long> memberCounts = fields.hasField(this.asIndexer(Tenant._memberCount)) ? this.withoutTenantFilters(() -> this.countMembers(ids)) : null;
        Map<UUID, Long> planCounts = fields.hasField(this.asIndexer(Tenant._planCount)) ? this.withoutTenantFilters(() -> this.countPlans(ids)) : null;

        List<Tenant> models =  new ArrayList<>();
        for(TenantEntity d : data){
            Tenant m = new Tenant();
            if(fields.hasField(this.asIndexer(Tenant._id))) m.setId(d.getId());
            if(fields.hasField(this.asIndexer(Tenant._code))) m.setCode(d.getCode());
            if(fields.hasField(this.asIndexer(Tenant._name))) m.setName(d.getName());
            if(fields.hasField(this.asIndexer(Tenant._description))) m.setDescription(d.getDescription());
            if(fields.hasField(this.asIndexer(Tenant._isActive))) m.setIsActive(d.getIsActive());
            if(fields.hasField(this.asIndexer(Tenant._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if(fields.hasField(this.asIndexer(Tenant._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if(fields.hasField(this.asIndexer(Tenant._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if(memberCounts != null) m.setMemberCount(memberCounts.getOrDefault(d.getId(), 0L));
            if(planCounts != null) m.setPlanCount(planCounts.getOrDefault(d.getId(), 0L));
            models.add(m);
        }
        this.logger.debug("build {} items",Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, Long> countMembers(List<UUID> ids) {
        if (ids.isEmpty()) return new HashMap<>();
        UserQuery userSubQuery = this.queryFactory.query(UserQuery.class).disableTracking().isActive(IsActive.Active);
        return this.queryFactory.query(TenantUserQuery.class).disableTracking().tenantIds(ids).isActive(IsActive.Active).userSubQuery(userSubQuery)
                .collectAs(new BaseFieldSet(this.asIndexer(TenantUser._tenant, Tenant._id))).stream()
                .collect(Collectors.groupingBy(TenantUserEntity::getTenantId, Collectors.counting()));
    }

    private Map<UUID, Long> countPlans(List<UUID> ids) {
        if (ids.isEmpty()) return new HashMap<>();
        // a plan counts once however many versions it has
        TenantQuery tenantSubQuery = this.queryFactory.query(TenantQuery.class).disableTracking().ids(ids);
        return this.queryFactory.query(PlanQuery.class).disableTracking().tenantSubQuery(tenantSubQuery).isActive(IsActive.Active)
                .collectAs(new BaseFieldSet(Plan._tenantId, Plan._groupId)).stream()
                .collect(Collectors.groupingBy(PlanEntity::getTenantId, Collectors.collectingAndThen(Collectors.mapping(PlanEntity::getGroupId, Collectors.toSet()), x -> (long) x.size())));
    }

    private Map<UUID, Long> withoutTenantFilters(Supplier<Map<UUID, Long>> count) {
        TenantEntityManager entityManager = this.tenantEntityManagerFactory.getInstance();
        // The counts span organisations, so the tenant filter of the current request is lifted for these two queries.
        boolean wasDisabled = entityManager.isTenantFiltersDisabled();
        if (!wasDisabled) entityManager.disableTenantFilters();
        try {
            return count.get();
        } finally {
            if (!wasDisabled) {
                try {
                    entityManager.reloadTenantFilters();
                } catch (InvalidApplicationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
