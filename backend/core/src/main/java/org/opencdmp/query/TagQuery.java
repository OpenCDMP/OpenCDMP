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
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.DescriptionTagEntity;
import org.opencdmp.data.TagEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.Tag;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TagQuery extends QueryBase<TagEntity> {

    private String like;
    private Collection<String> tags;
    private Collection<String> excludedTags;

    private Collection<UUID> ids;

    private Collection<UUID> excludedIds;

    private Collection<IsActive> isActives;

    private Collection<UUID> createdByIds;

    private DescriptionTagQuery descriptionTagQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    
    private final UserScope userScope;
    private final AuthorizationService authService;
    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;

    public TagQuery(UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
	    this.userScope = userScope;
	    this.authService = authService;
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public TagQuery like(String value) {
        this.like = value;
        return this;
    }

    public TagQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public TagQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public TagQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public TagQuery tags(String value) {
        this.tags = List.of(value);
        return this;
    }

    public TagQuery tags(String... value) {
        this.tags = Arrays.asList(value);
        return this;
    }

    public TagQuery tags(Collection<String> values) {
        this.tags = values;
        return this;
    }

    public TagQuery excludedTags(String value) {
        this.excludedTags = List.of(value);
        return this;
    }

    public TagQuery excludedTags(String... value) {
        this.excludedTags = Arrays.asList(value);
        return this;
    }

    public TagQuery excludedTags(Collection<String> values) {
        this.excludedTags = values;
        return this;
    }

    public TagQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public TagQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public TagQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public TagQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public TagQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public TagQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }
    
    public TagQuery createdByIds(UUID value) {
        this.createdByIds = List.of(value);
        return this;
    }

    public TagQuery createdByIds(UUID... value) {
        this.createdByIds = Arrays.asList(value);
        return this;
    }

    public TagQuery createdByIds(Collection<UUID> values) {
        this.createdByIds = values;
        return this;
    }

    public TagQuery descriptionTagSubQuery(DescriptionTagQuery subQuery) {
        this.descriptionTagQuery = subQuery;
        return this;
    }

    public TagQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public TagQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public TagQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return
                this.isEmpty(this.ids) ||
                        this.isEmpty(this.isActives) ||
                        this.isEmpty(this.tags) ||
                        this.isEmpty(this.excludedTags) ||
                        this.isEmpty(this.excludedIds) ||
                        this.isEmpty(this.createdByIds) ||
                        this.isFalseQuery(this.descriptionTagQuery);
    }

    @Override
    protected Class<TagEntity> entityClass() {
        return TagEntity.class;
    }
    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseTag)) return null;
        UUID userId;
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated)) userId = this.userScope.getUserIdSafe();
        else  userId = null;

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null || usePublic ) {
            predicates.add(queryContext.CriteriaBuilder.or(
                    queryContext.CriteriaBuilder.isNull(queryContext.Root.get(TagEntity._createdById)),
                    userId != null ?  queryContext.CriteriaBuilder.equal(queryContext.Root.get(TagEntity._createdById), userId) : queryContext.CriteriaBuilder.or(),  //Creates a false query
                    queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._id)).value(this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(DescriptionTagEntity.class, UUID.class)
                            .query(queryContext.Query)
                            .criteriaBuilder(queryContext.CriteriaBuilder)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(DescriptionTagEntity._tagId))
                            .filterFunc((subQueryRoot, cb) ->
                                    cb.in(subQueryRoot.get(DescriptionTagEntity._descriptionId)).value(this.queryUtilsService.buildDescriptionAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, usePublic))
                            )
                    )))  //Creates a false query
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
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(TagEntity._label), this.like));
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tags != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._label));
            for (String item : this.tags)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedTags != null) {
            CriteriaBuilder.In<String> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._label));
            for (String item : this.excludedTags)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.createdByIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._createdById));
            for (UUID item : this.createdByIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.descriptionTagQuery != null) {
            QueryContext<DescriptionTagEntity, UUID> subQuery = this.applySubQuery(this.descriptionTagQuery, queryContext, UUID.class, descriptionTagEntityRoot -> descriptionTagEntityRoot.get(DescriptionTagEntity._tagId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(TagEntity._id)).value(subQuery.Query));
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(Tag._id)) return TagEntity._id;
        else if (item.match(Tag._label)) return TagEntity._label;
        else if (item.prefix(Tag._createdBy)) return TagEntity._createdById;
        else if (item.match(Tag._createdBy)) return TagEntity._createdById;
        else if (item.match(Tag._createdAt)) return TagEntity._createdAt;
        else if (item.match(Tag._updatedAt)) return TagEntity._updatedAt;
        else if (item.match(Tag._hash)) return TagEntity._updatedAt;
        else if (item.match(Tag._isActive)) return TagEntity._isActive;
        else if (item.match(Tag._belongsToCurrentTenant)) return TagEntity._tenantId;
        else return null;
    }

    @Override
    protected TagEntity convert(Tuple tuple, Set<String> columns) {
        TagEntity item = new TagEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, TagEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, TagEntity._tenantId, UUID.class));
        item.setLabel(QueryBase.convertSafe(tuple, columns, TagEntity._label, String.class));
        item.setCreatedById(QueryBase.convertSafe(tuple, columns, TagEntity._createdById, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, TagEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, TagEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, TagEntity._isActive, IsActive.class));
        return item;
    }

}
