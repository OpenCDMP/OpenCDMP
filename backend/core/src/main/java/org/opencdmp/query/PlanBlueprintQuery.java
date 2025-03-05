package org.opencdmp.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.PlanBlueprintStatus;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBlueprintQuery extends QueryBase<PlanBlueprintEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<UUID> excludedIds;

    private Collection<IsActive> isActives;

    private Collection<PlanBlueprintStatus> statuses;

    private Collection<UUID> groupIds;

    private Collection<UUID> excludedGroupIds;

    private Collection<Short> versions;

    private Collection<String> codes;

    private Collection<PlanBlueprintVersionStatus> versionStatuses;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanBlueprintQuery like(String value) {
        this.like = value;
        return this;
    }

    public PlanBlueprintQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public PlanBlueprintQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public PlanBlueprintQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public PlanBlueprintQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public PlanBlueprintQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery excludedGroupIds(Collection<UUID> values) {
        this.excludedGroupIds = values;
        return this;
    }

    public PlanBlueprintQuery excludedGroupIds(UUID value) {
        this.excludedGroupIds = List.of(value);
        return this;
    }

    public PlanBlueprintQuery excludedGroupIds(UUID... value) {
        this.excludedGroupIds = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public PlanBlueprintQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public PlanBlueprintQuery statuses(PlanBlueprintStatus value) {
        this.statuses = List.of(value);
        return this;
    }

    public PlanBlueprintQuery statuses(PlanBlueprintStatus... value) {
        this.statuses = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery statuses(Collection<PlanBlueprintStatus> values) {
        this.statuses = values;
        return this;
    }

    public PlanBlueprintQuery groupIds(UUID value) {
        this.groupIds = List.of(value);
        return this;
    }

    public PlanBlueprintQuery groupIds(UUID... value) {
        this.groupIds = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery groupIds(Collection<UUID> values) {
        this.groupIds = values;
        return this;
    }

    public PlanBlueprintQuery versions(Short value) {
        this.versions = List.of(value);
        return this;
    }

    public PlanBlueprintQuery versions(Short... value) {
        this.versions = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery versions(Collection<Short> values) {
        this.versions = values;
        return this;
    }

    public PlanBlueprintQuery versionStatuses(PlanBlueprintVersionStatus value) {
        this.versionStatuses = List.of(value);
        return this;
    }

    public PlanBlueprintQuery versionStatuses(PlanBlueprintVersionStatus... value) {
        this.versionStatuses = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery versionStatuses(Collection<PlanBlueprintVersionStatus> values) {
        this.versionStatuses = values;
        return this;
    }

    public PlanBlueprintQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public PlanBlueprintQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public PlanBlueprintQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public PlanBlueprintQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanBlueprintQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public PlanBlueprintQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public PlanBlueprintQuery(
		    AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager
    ) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected Class<PlanBlueprintEntity> entityClass() {
        return PlanBlueprintEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.statuses) || this.isEmpty(this.groupIds) || this.isEmpty(this.excludedGroupIds);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();

        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }

        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanBlueprintEntity._label), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanBlueprintEntity._code), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(PlanBlueprintEntity._description), this.like)
            ));
        }

        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.statuses != null) {
            CriteriaBuilder.In<PlanBlueprintStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._status));
            for (PlanBlueprintStatus item : this.statuses)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.groupIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._groupId));
            for (UUID item : this.groupIds)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.excludedGroupIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._groupId));
            for (UUID item : this.excludedGroupIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }

        if (this.versions != null) {
            CriteriaBuilder.In<Short> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._version));
            for (Short item : this.versions)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.versionStatuses != null) {
            CriteriaBuilder.In<PlanBlueprintVersionStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._versionStatus));
            for (PlanBlueprintVersionStatus item : this.versionStatuses)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanBlueprintEntity._code));
            for (String item : this.codes)
                inClause.value(item);
            predicates.add(inClause);
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected PlanBlueprintEntity convert(Tuple tuple, Set<String> columns) {
        PlanBlueprintEntity item = new PlanBlueprintEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._label, String.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._code, String.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._definition, String.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._status, PlanBlueprintStatus.class));
        item.setGroupId(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._groupId, UUID.class));
        item.setVersion(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._version, Short.class));
        item.setVersionStatus(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._versionStatus, PlanBlueprintVersionStatus.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._isActive, IsActive.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, PlanBlueprintEntity._description, String.class));

        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(PlanBlueprint._id))
            return PlanBlueprintEntity._id;
        else if (item.match(PlanBlueprint._label))
            return PlanBlueprintEntity._label;
        else if (item.match(PlanBlueprint._code))
            return PlanBlueprintEntity._code;
        else if (item.match(PlanBlueprint._definition))
            return PlanBlueprintEntity._definition;
        else if (item.prefix(PlanBlueprint._definition))
            return PlanBlueprintEntity._definition;
        else if (item.match(PlanBlueprint._status))
            return PlanBlueprintEntity._status;
        else if (item.match(PlanBlueprint._groupId))
            return PlanBlueprintEntity._groupId;
        else if (item.match(PlanBlueprint._version))
            return PlanBlueprintEntity._version;
        else if (item.match(PlanBlueprint._versionStatus))
            return PlanBlueprintEntity._versionStatus;
        else if (item.match(PlanBlueprint._createdAt))
            return PlanBlueprintEntity._createdAt;
        else if (item.match(PlanBlueprint._updatedAt))
            return PlanBlueprintEntity._updatedAt;
        else if (item.match(PlanBlueprint._isActive))
            return PlanBlueprintEntity._isActive;
        else if (item.match(PlanBlueprint._hash))
            return PlanBlueprintEntity._updatedAt;
        else if (item.match(PlanBlueprint._belongsToCurrentTenant))
            return PlanBlueprintEntity._tenantId;
        else if (item.match(PlanBlueprint._description))
            return PlanBlueprintEntity._description;
        else
            return null;
    }

}
