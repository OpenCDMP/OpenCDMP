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
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReferenceSourceType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.*;
import org.opencdmp.model.PublicReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceQuery extends QueryBase<ReferenceEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<IsActive> isActives;

    private Collection<ReferenceSourceType> referenceSourceTypes;

    private Collection<UUID> typeIds;

    private Collection<String> references;

    private Collection<String> sources;

    private Collection<UUID> excludedIds;

    private Instant after;

    private PlanReferenceQuery planReferenceQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public ReferenceQuery like(String value) {
        this.like = value;
        return this;
    }

    public ReferenceQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public ReferenceQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public ReferenceQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public ReferenceQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public ReferenceQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public ReferenceQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public ReferenceQuery typeIds(UUID value) {
        this.typeIds = List.of(value);
        return this;
    }

    public ReferenceQuery typeIds(UUID... value) {
        this.typeIds = Arrays.asList(value);
        return this;
    }

    public ReferenceQuery typeIds(Collection<UUID> values) {
        this.typeIds = values;
        return this;
    }

    public ReferenceQuery references(String value) {
        this.references = List.of(value);
        return this;
    }

    public ReferenceQuery references(String... value) {
        this.references = Arrays.asList(value);
        return this;
    }

    public ReferenceQuery references(Collection<String> values) {
        this.references = values;
        return this;
    }

    public ReferenceQuery sources(String value) {
        this.sources = List.of(value);
        return this;
    }

    public ReferenceQuery sources(String... value) {
        this.sources = Arrays.asList(value);
        return this;
    }

    public ReferenceQuery sources(Collection<String> values) {
        this.sources = values;
        return this;
    }

    public ReferenceQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public ReferenceQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public ReferenceQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public ReferenceQuery sourceTypes(ReferenceSourceType value) {
        this.referenceSourceTypes = List.of(value);
        return this;
    }

    public ReferenceQuery sourceTypes(ReferenceSourceType... value) {
        this.referenceSourceTypes = Arrays.asList(value);
        return this;
    }

    public ReferenceQuery sourceTypes(Collection<ReferenceSourceType> values) {
        this.referenceSourceTypes = values;
        return this;
    }

    public ReferenceQuery after(Instant value) {
        this.after = value;
        return this;
    }

    public ReferenceQuery planReferenceSubQuery(PlanReferenceQuery value) {
        this.planReferenceQuery = value;
        return this;
    }

    public ReferenceQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public ReferenceQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public ReferenceQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    private final UserScope userScope;

    private final AuthorizationService authService;

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public ReferenceQuery(
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
    protected Class<ReferenceEntity> entityClass() {
        return ReferenceEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.sources) || this.isEmpty(this.excludedIds) || this.isEmpty(this.typeIds) || this.isEmpty(this.referenceSourceTypes) || this.isFalseQuery(this.planReferenceQuery);
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None))
            return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseReference))
            return null;
        UUID userId;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated))
            userId = this.userScope.getUserIdSafe();
        else
            userId = null;

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    this.authService.authorize(Permission.BrowseExternalReference) ? queryContext.CriteriaBuilder.equal(queryContext.Root.get(ReferenceEntity._sourceType), ReferenceSourceType.External) : queryContext.CriteriaBuilder.or(),
                    userId != null ? queryContext.CriteriaBuilder.equal(queryContext.Root.get(ReferenceEntity._createdById), userId) : queryContext.CriteriaBuilder.or(),  //Creates a false query
                    queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._id)).value(this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(PlanReferenceEntity.class, UUID.class)
                            .query(queryContext.Query)
                            .criteriaBuilder(queryContext.CriteriaBuilder)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanReferenceEntity._referenceId))
                            .filterFunc((subQueryRoot, cb) ->
                                    cb.in(subQueryRoot.get(PlanReferenceEntity._planId)).value(this.queryUtilsService.buildPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic))
                            )
                    ))),
                    queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._id)).value(this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(DescriptionReferenceEntity.class, UUID.class)
                            .query(queryContext.Query)
                            .criteriaBuilder(queryContext.CriteriaBuilder)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(DescriptionReferenceEntity._referenceId))
                            .filterFunc((subQueryRoot, cb) ->
                                    cb.in(subQueryRoot.get(DescriptionReferenceEntity._descriptionId)).value(this.queryUtilsService.buildDescriptionAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic))
                            )
                    )))
            ));
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
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(queryContext.CriteriaBuilder.or(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(ReferenceEntity._label), this.like),
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(ReferenceEntity._description), this.like),
		            this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(ReferenceEntity._reference), this.like)
            ));
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.referenceSourceTypes != null) {
            CriteriaBuilder.In<ReferenceSourceType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._sourceType));
            for (ReferenceSourceType item : this.referenceSourceTypes)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.typeIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._typeId));
            for (UUID item : this.typeIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.references != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._reference));
            for (String item : this.references)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.sources != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._source));
            for (String item : this.sources)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ReferenceEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.after != null) {
            Predicate afterClause = queryContext.CriteriaBuilder.greaterThanOrEqualTo(queryContext.Root.get(PlanEntity._createdAt), this.after);
            predicates.add(afterClause);
        }

        if (this.planReferenceQuery != null) {
            QueryContext<PlanReferenceEntity, UUID> subQuery = this.applySubQuery(this.planReferenceQuery, queryContext, UUID.class, root -> root.get(PlanReferenceEntity._referenceId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(PlanReferenceEntity._id)).value(subQuery.Query));
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected ReferenceEntity convert(Tuple tuple, Set<String> columns) {
        ReferenceEntity item = new ReferenceEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, ReferenceEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, ReferenceEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, ReferenceEntity._label, String.class));
        item.setDescription(QueryBase.convertSafe(tuple, columns, ReferenceEntity._description, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, ReferenceEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, ReferenceEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, ReferenceEntity._isActive, IsActive.class));
        item.setDefinition(QueryBase.convertSafe(tuple, columns, ReferenceEntity._definition, String.class));
        item.setAbbreviation(QueryBase.convertSafe(tuple, columns, ReferenceEntity._abbreviation, String.class));
        item.setReference(QueryBase.convertSafe(tuple, columns, ReferenceEntity._reference, String.class));
        item.setSource(QueryBase.convertSafe(tuple, columns, ReferenceEntity._source, String.class));
        item.setSourceType(QueryBase.convertSafe(tuple, columns, ReferenceEntity._sourceType, ReferenceSourceType.class));
        item.setTypeId(QueryBase.convertSafe(tuple, columns, ReferenceEntity._typeId, UUID.class));
        item.setCreatedById(QueryBase.convertSafe(tuple, columns, ReferenceEntity._createdById, UUID.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(Reference._id) || item.match(PublicReference._id))
            return ReferenceEntity._id;
        else if (item.match(Reference._label) || item.match(PublicReference._label))
            return ReferenceEntity._label;
        else if (item.match(Reference._description) || item.match(PublicReference._description))
            return ReferenceEntity._description;
        else if (item.match(Reference._createdAt))
            return ReferenceEntity._createdAt;
        else if (item.match(Reference._updatedAt))
            return ReferenceEntity._updatedAt;
        else if (item.match(Reference._hash))
            return ReferenceEntity._updatedAt;
        else if (item.match(Reference._isActive))
            return ReferenceEntity._isActive;
        else if (item.prefix(Reference._definition))
            return ReferenceEntity._definition;
        else if (item.match(Reference._abbreviation))
            return ReferenceEntity._abbreviation;
        else if (item.match(Reference._reference) || item.match(PublicReference._reference))
            return ReferenceEntity._reference;
        else if (item.match(Reference._source))
            return ReferenceEntity._source;
        else if (item.match(Reference._sourceType))
            return ReferenceEntity._sourceType;
        else if (item.match(Reference._type) || item.match(PublicReference._type))
            return ReferenceEntity._typeId;
        else if (item.prefix(Reference._type) || item.prefix(PublicReference._type))
            return ReferenceEntity._typeId;
        else if (item.prefix(Reference._createdBy))
            return ReferenceEntity._createdById;
        else if (item.match(Reference._belongsToCurrentTenant))
            return ReferenceEntity._tenantId;
        else
            return null;
    }

}
