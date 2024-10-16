package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.ActionConfirmationStatus;
import org.opencdmp.commons.enums.ActionConfirmationType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.ActionConfirmationEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.actionconfirmation.ActionConfirmation;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ActionConfirmationQuery extends QueryBase<ActionConfirmationEntity> {

    private String like;

    private Collection<UUID> ids;

    private Collection<UUID> excludedIds;

    private Collection<IsActive> isActives;

    private Collection<UUID> createdByIds;

    private Collection<ActionConfirmationType> types;

    private Collection<ActionConfirmationStatus> statuses;

    private Collection<String> tokens;


    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public ActionConfirmationQuery(QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public ActionConfirmationQuery like(String value) {
        this.like = value;
        return this;
    }

    public ActionConfirmationQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public ActionConfirmationQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public ActionConfirmationQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public ActionConfirmationQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public ActionConfirmationQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public ActionConfirmationQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public ActionConfirmationQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public ActionConfirmationQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public ActionConfirmationQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }
    
    public ActionConfirmationQuery createdByIds(UUID value) {
        this.createdByIds = List.of(value);
        return this;
    }

    public ActionConfirmationQuery createdByIds(UUID... value) {
        this.createdByIds = Arrays.asList(value);
        return this;
    }

    public ActionConfirmationQuery createdByIds(Collection<UUID> values) {
        this.createdByIds = values;
        return this;
    }

    public ActionConfirmationQuery types(ActionConfirmationType value) {
        this.types = List.of(value);
        return this;
    }

    public ActionConfirmationQuery types(ActionConfirmationType... value) {
        this.types = Arrays.asList(value);
        return this;
    }

    public ActionConfirmationQuery types(Collection<ActionConfirmationType> values) {
        this.types = values;
        return this;
    }

    public ActionConfirmationQuery statuses(ActionConfirmationStatus value) {
        this.statuses = List.of(value);
        return this;
    }

    public ActionConfirmationQuery statuses(ActionConfirmationStatus... value) {
        this.statuses = Arrays.asList(value);
        return this;
    }

    public ActionConfirmationQuery statuses(Collection<ActionConfirmationStatus> values) {
        this.statuses = values;
        return this;
    }

    public ActionConfirmationQuery tokens(String value) {
        this.tokens = List.of(value);
        return this;
    }

    public ActionConfirmationQuery tokens(String... value) {
        this.tokens = Arrays.asList(value);
        return this;
    }

    public ActionConfirmationQuery tokens(Collection<String> values) {
        this.tokens = values;
        return this;
    }

    public ActionConfirmationQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public ActionConfirmationQuery disableTracking() {
        this.noTracking = true;
        return this;
    }


    public ActionConfirmationQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
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
                        this.isEmpty(this.excludedIds) ||
                        this.isEmpty(this.statuses) ||
                        this.isEmpty(this.types) ||
                        this.isEmpty(this.tokens) ||
                        this.isEmpty(this.createdByIds);
    }

    @Override
    protected Class<ActionConfirmationEntity> entityClass() {
        return ActionConfirmationEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(ActionConfirmationEntity._token), this.like));
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ActionConfirmationEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.types != null) {
            CriteriaBuilder.In<ActionConfirmationType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ActionConfirmationEntity._type));
            for (ActionConfirmationType item : this.types)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ActionConfirmationEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ActionConfirmationEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.createdByIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ActionConfirmationEntity._createdById));
            for (UUID item : this.createdByIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tokens != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(ActionConfirmationEntity._token));
            for (String item : this.tokens)
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
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(ActionConfirmation._id)) return ActionConfirmationEntity._id;
        else if (item.match(ActionConfirmation._type)) return ActionConfirmationEntity._type;
        else if (item.match(ActionConfirmation._status)) return ActionConfirmationEntity._status;
        else if (item.match(ActionConfirmationEntity._token)) return ActionConfirmationEntity._token;
        else if (item.match(ActionConfirmation._expiresAt)) return ActionConfirmationEntity._expiresAt;
        else if (item.prefix(ActionConfirmation._mergeAccountConfirmation)) return ActionConfirmationEntity._data;
        else if (item.prefix(ActionConfirmation._removeCredentialRequest)) return ActionConfirmationEntity._data;
        else if (item.prefix(ActionConfirmation._planInvitation)) return ActionConfirmationEntity._data;
        else if (item.prefix(ActionConfirmation._createdBy)) return ActionConfirmationEntity._createdById;
        else if (item.match(ActionConfirmation._createdBy)) return ActionConfirmationEntity._createdById;
        else if (item.match(ActionConfirmation._createdAt)) return ActionConfirmationEntity._createdAt;
        else if (item.match(ActionConfirmation._updatedAt)) return ActionConfirmationEntity._updatedAt;
        else if (item.match(ActionConfirmation._hash)) return ActionConfirmationEntity._updatedAt;
        else if (item.match(ActionConfirmation._isActive)) return ActionConfirmationEntity._isActive;
        else if (item.match(ActionConfirmation._belongsToCurrentTenant)) return ActionConfirmationEntity._tenantId;
        else return null;
    }

    @Override
    protected ActionConfirmationEntity convert(Tuple tuple, Set<String> columns) {
        ActionConfirmationEntity item = new ActionConfirmationEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._tenantId, UUID.class));
        item.setType(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._type, ActionConfirmationType.class));
        item.setStatus(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._status, ActionConfirmationStatus.class));
        item.setToken(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._token, String.class));
        item.setData(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._data, String.class));
        item.setExpiresAt(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._expiresAt, Instant.class));
        item.setCreatedById(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._createdById, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._isActive, IsActive.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, ActionConfirmationEntity._tenantId, UUID.class));
        return item;
    }

}
