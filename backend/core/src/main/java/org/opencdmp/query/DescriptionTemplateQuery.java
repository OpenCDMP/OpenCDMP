package org.opencdmp.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.DescriptionTemplateStatus;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.*;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateQuery extends QueryBase<DescriptionTemplateEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<UUID> groupIds;

    private Collection<IsActive> isActives;

    private Collection<Short> versions;

    private Collection<DescriptionTemplateVersionStatus> versionStatuses;

    private Collection<DescriptionTemplateStatus> statuses;

    private Collection<UUID> excludedIds;

    private Collection<UUID> excludedGroupIds;

    private Collection<UUID> typeIds;

    private Instant after;
    private Boolean onlyCanEdit;

    private PlanDescriptionTemplateQuery planDescriptionTemplateQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private Collection<String> codes;

    private Collection<String> languages;

    public DescriptionTemplateQuery like(String value) {
        this.like = value;
        return this;
    }

    public DescriptionTemplateQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public DescriptionTemplateQuery typeIds(UUID value) {
        this.typeIds = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery typeIds(UUID... value) {
        this.typeIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery typeIds(Collection<UUID> values) {
        this.typeIds = values;
        return this;
    }

    public DescriptionTemplateQuery groupIds(UUID value) {
        this.groupIds = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery groupIds(UUID... value) {
        this.groupIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery groupIds(Collection<UUID> values) {
        this.groupIds = values;
        return this;
    }

    public DescriptionTemplateQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public DescriptionTemplateQuery versions(Short value) {
        this.versions = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery versions(Short... value) {
        this.versions = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery versions(Collection<Short> values) {
        this.versions = values;
        return this;
    }

    public DescriptionTemplateQuery statuses(DescriptionTemplateStatus value) {
        this.statuses = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery statuses(DescriptionTemplateStatus... value) {
        this.statuses = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery statuses(Collection<DescriptionTemplateStatus> values) {
        this.statuses = values;
        return this;
    }

    public DescriptionTemplateQuery versionStatuses(DescriptionTemplateVersionStatus value) {
        this.versionStatuses = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery versionStatuses(DescriptionTemplateVersionStatus... value) {
        this.versionStatuses = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery versionStatuses(Collection<DescriptionTemplateVersionStatus> values) {
        this.versionStatuses = values;
        return this;
    }

    public DescriptionTemplateQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public DescriptionTemplateQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery excludedGroupIds(Collection<UUID> values) {
        this.excludedGroupIds = values;
        return this;
    }

    public DescriptionTemplateQuery excludedGroupIds(UUID value) {
        this.excludedGroupIds = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery excludedGroupIds(UUID... value) {
        this.excludedGroupIds = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery after(Instant value) {
        this.after = value;
        return this;
    }

    public DescriptionTemplateQuery onlyCanEdit(Boolean onlyCanEdit) {
        this.onlyCanEdit = onlyCanEdit;
        return this;
    }

    public DescriptionTemplateQuery planDescriptionTemplateSubQuery(PlanDescriptionTemplateQuery value) {
        this.planDescriptionTemplateQuery = value;
        return this;
    }

    public DescriptionTemplateQuery codes(String value) {
        this.codes = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery codes(String... value) {
        this.codes = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery codes(Collection<String> values) {
        this.codes = values;
        return this;
    }

    public DescriptionTemplateQuery languages(String value) {
        this.languages = List.of(value);
        return this;
    }

    public DescriptionTemplateQuery languages(String... value) {
        this.languages = Arrays.asList(value);
        return this;
    }

    public DescriptionTemplateQuery languages(Collection<String> values) {
        this.languages = values;
        return this;
    }

    public DescriptionTemplateQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionTemplateQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public DescriptionTemplateQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public DescriptionTemplateQuery(
		    UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }    
    
    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Class<DescriptionTemplateEntity> entityClass() {
        return DescriptionTemplateEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.typeIds) || this.isEmpty(this.versionStatuses) || this.isEmpty(this.groupIds) || this.isEmpty(this.isActives) || this.isEmpty(this.excludedIds) || this.isEmpty(this.excludedGroupIds) || this.isEmpty(this.statuses);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseDescriptionTemplate))
            return null;
        UUID userId;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated))
            userId = this.userScope.getUserIdSafe();
        else
            userId = null;

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic) {
            Subquery<UUID> planDescriptionTemplateSubquery = this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(
                    new BuildSubQueryInput.Builder<>(PlanDescriptionTemplateEntity.class, UUID.class, queryContext)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanDescriptionTemplateEntity._descriptionTemplateGroupId))
                            .filterFunc((subQueryRoot, cb) ->
                                    cb.in(subQueryRoot.get(PlanDescriptionTemplateEntity._planId)).value(this.queryUtilsService.buildPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic))
                            )
            ));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._groupId)).value(planDescriptionTemplateSubquery));
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return queryContext.CriteriaBuilder.or(); //Creates a false query
        }
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.typeIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._typeId));
            for (UUID item : this.typeIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.groupIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._groupId));
            for (UUID item : this.groupIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionTemplateEntity._label), this.like),
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionTemplateEntity._description), this.like),
                    this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(DescriptionTemplateEntity._code), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.versions != null) {
            CriteriaBuilder.In<Short> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._version));
            for (Short item : this.versions)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.versionStatuses != null) {
            CriteriaBuilder.In<DescriptionTemplateVersionStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._versionStatus));
            for (DescriptionTemplateVersionStatus item : this.versionStatuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.statuses != null) {
            CriteriaBuilder.In<DescriptionTemplateStatus> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._status));
            for (DescriptionTemplateStatus item : this.statuses)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.excludedGroupIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._groupId));
            for (UUID item : this.excludedGroupIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.after != null) {
            Predicate afterClause = queryContext.CriteriaBuilder.greaterThanOrEqualTo(queryContext.Root.get(PlanEntity._createdAt), this.after);
            predicates.add(afterClause);
        }
        if (this.codes != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._code));
            for (String item : this.codes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.languages != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._language));
            for (String item : this.languages)
                inClause.value(item);
            predicates.add(inClause);
        }
        
        if (this.onlyCanEdit != null) {
            boolean canEdit = this.authService.authorize(Permission.EditDescriptionTemplate);
            if (!canEdit){
                UUID userId = this.userScope.getUserIdSafe();
                if (userId == null){
                    predicates.add(queryContext.CriteriaBuilder.or()); //Creates a false query
                } else {
                    Subquery<UUID> subquery = this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(
                            new BuildSubQueryInput.Builder<>(UserDescriptionTemplateEntity.class, UUID.class, queryContext)
                                    .keyPathFunc((subQueryRoot) -> subQueryRoot.get(UserDescriptionTemplateEntity._descriptionTemplateId))
                                    .filterFunc((subQueryRoot, cb) ->
                                            cb.in(subQueryRoot.get(UserDescriptionTemplateEntity._userId)).value(userId)
                                    )
                    ));
                    predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._id)).value(subquery));
                }
            }
        }

        if (this.planDescriptionTemplateQuery != null) {
            QueryContext<PlanDescriptionTemplateEntity, UUID> subQuery = this.applySubQuery(this.planDescriptionTemplateQuery, queryContext, UUID.class, planDescriptionTemplateEntityRoot -> planDescriptionTemplateEntityRoot.get(PlanDescriptionTemplateEntity._descriptionTemplateGroupId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(DescriptionTemplateEntity._groupId)).value(subQuery.Query));
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected DescriptionTemplateEntity convert(Tuple tuple, Set<String> columns) {
        DescriptionTemplateEntity item = new DescriptionTemplateEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._label, String.class));
        item.setCode(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._code, String.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._definition, String.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._description, String.class));
        item.setGroupId(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._groupId, UUID.class));
        item.setVersion(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._version, Short.class));
        item.setLanguage(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._language, String.class));
        item.setTypeId(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._typeId, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._isActive, IsActive.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._status, DescriptionTemplateStatus.class));
        item.setVersionStatus(QueryBase.convertSafe(tuple, columns, DescriptionTemplateEntity._versionStatus, DescriptionTemplateVersionStatus.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(DescriptionTemplate._id))
            return DescriptionTemplateEntity._id;
        else if (item.match(DescriptionTemplate._label))
            return DescriptionTemplateEntity._label;
        else if (item.match(DescriptionTemplate._code))
            return DescriptionTemplateEntity._code;
        else if (item.prefix(DescriptionTemplate._definition))
            return DescriptionTemplateEntity._definition;
        else if (item.match(DescriptionTemplate._definition))
            return DescriptionTemplateEntity._definition;
        else if (item.prefix(DescriptionTemplate._users))
            return DescriptionTemplateEntity._id;
        else if (item.match(DescriptionTemplate._description))
            return DescriptionTemplateEntity._description;
        else if (item.match(DescriptionTemplate._groupId))
            return DescriptionTemplateEntity._groupId;
        else if (item.match(DescriptionTemplate._version))
            return DescriptionTemplateEntity._version;
        else if (item.match(DescriptionTemplate._language))
            return DescriptionTemplateEntity._language;
        else if (item.prefix(DescriptionTemplate._type))
            return DescriptionTemplateEntity._typeId;
        else if (item.match(DescriptionTemplate._createdAt))
            return DescriptionTemplateEntity._createdAt;
        else if (item.match(DescriptionTemplate._updatedAt))
            return DescriptionTemplateEntity._updatedAt;
        else if (item.match(DescriptionTemplate._hash))
            return DescriptionTemplateEntity._updatedAt;
        else if (item.match(DescriptionTemplate._isActive))
            return DescriptionTemplateEntity._isActive;
        else if (item.match(DescriptionTemplate._status))
            return DescriptionTemplateEntity._status;
        else if (item.match(DescriptionTemplate._versionStatus))
            return DescriptionTemplateEntity._versionStatus;
        else if (item.match(DescriptionTemplate._belongsToCurrentTenant))
            return DescriptionTemplateEntity._tenantId;
        else
            return null;
    }

}
