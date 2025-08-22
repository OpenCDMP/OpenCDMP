package org.opencdmp.query;


import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantConfigurationQuery extends QueryBase<TenantConfigurationEntity> {

    private Collection<UUID> ids;
    private Collection<UUID> tenantIds;
    private Boolean tenantIsSet;
    private Collection<IsActive> isActives;
    private Collection<TenantConfigurationType> types;
    private Collection<UUID> excludedIds;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    private final TenantEntityManager tenantEntityManager;
	public TenantConfigurationQuery(TenantEntityManager tenantEntityManager) {
		this.tenantEntityManager = tenantEntityManager;
	}

    public TenantConfigurationQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public TenantConfigurationQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public TenantConfigurationQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public TenantConfigurationQuery tenantIds(UUID value) {
        this.tenantIds = List.of(value);
        return this;
    }

    public TenantConfigurationQuery tenantIds(UUID... value) {
        this.tenantIds = Arrays.asList(value);
        return this;
    }

    public TenantConfigurationQuery tenantIds(Collection<UUID> values) {
        this.tenantIds = values;
        return this;
    }

    public TenantConfigurationQuery clearTenantIds() {
        this.tenantIds = null;
        return this;
    }

    public TenantConfigurationQuery tenantIsSet(Boolean values) {
        this.tenantIsSet = values;
        return this;
    }

    public TenantConfigurationQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public TenantConfigurationQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public TenantConfigurationQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public TenantConfigurationQuery types(TenantConfigurationType value) {
        this.types = List.of(value);
        return this;
    }

    public TenantConfigurationQuery types(TenantConfigurationType... value) {
        this.types = Arrays.asList(value);
        return this;
    }

    public TenantConfigurationQuery types(Collection<TenantConfigurationType> values) {
        this.types = values;
        return this;
    }

    public TenantConfigurationQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public TenantConfigurationQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public TenantConfigurationQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public TenantConfigurationQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public TenantConfigurationQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public TenantConfigurationQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) ||this.isEmpty(this.isActives) ||this.isEmpty(this.types) || this.isEmpty(this.tenantIds);
    }

    @Override
    protected Class<TenantConfigurationEntity> entityClass() {
        return TenantConfigurationEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantConfigurationEntity._id));
            for (UUID item : this.ids) inClause.value(item);
            predicates.add(inClause);
        }
        
        if (this.tenantIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantConfigurationEntity._tenantId));
            for (UUID item : this.tenantIds) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantConfigurationEntity._isActive));
            for (IsActive item : this.isActives) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.tenantIsSet != null) {
            if (this.tenantIsSet) predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(TenantConfigurationEntity._tenantId)));
            else predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(TenantConfigurationEntity._tenantId)));
        }

        if (this.types != null) {
            CriteriaBuilder.In<TenantConfigurationType> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantConfigurationEntity._type));
            for (TenantConfigurationType item : this.types) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantConfigurationEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }

        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }

    }

    @Override
    protected TenantConfigurationEntity convert(Tuple tuple, Set<String> columns) {
        TenantConfigurationEntity item = new TenantConfigurationEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, TenantConfigurationEntity._id, UUID.class));
        item.setValue(QueryBase.convertSafe(tuple, columns, TenantConfigurationEntity._value, String.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, TenantConfigurationEntity._tenantId, UUID.class));
        item.setType(QueryBase.convertSafe(tuple, columns, TenantConfigurationEntity._type, TenantConfigurationType.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, TenantConfigurationEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, TenantConfigurationEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, TenantConfigurationEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(TenantConfiguration._id)) return TenantConfigurationEntity._id;
        else if (item.match(TenantConfiguration._type)) return TenantConfigurationEntity._type;
        else if (item.prefix(TenantConfiguration._cssColors)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._cssColors)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._depositPlugins)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._depositPlugins)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._defaultUserLocale)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._defaultUserLocale)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._evaluatorPlugins)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._evaluatorPlugins)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._fileTransformerPlugins)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._fileTransformerPlugins)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._logo)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._logo)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._featuredEntities)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._featuredEntities)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._defaultPlanBlueprint)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._defaultPlanBlueprint)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._pluginConfiguration)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._pluginConfiguration)) return TenantConfigurationEntity._value;
        else if (item.prefix(TenantConfiguration._viewPreferences)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._viewPreferences)) return TenantConfigurationEntity._value;
        else if (item.match(TenantConfiguration._createdAt)) return TenantConfigurationEntity._createdAt;
        else if (item.match(TenantConfiguration._updatedAt)) return TenantConfigurationEntity._updatedAt;
        else if (item.match(TenantConfiguration._isActive)) return TenantConfigurationEntity._isActive;
        else if (item.match(TenantConfiguration._belongsToCurrentTenant)) return TenantConfigurationEntity._tenantId;
        else if (item.match(TenantConfigurationEntity._tenantId)) return TenantConfigurationEntity._tenantId;
        else return null;
    }

}
