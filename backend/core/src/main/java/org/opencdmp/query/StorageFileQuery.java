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
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.StorageFile;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StorageFileQuery extends QueryBase<StorageFileEntity> {
    private String like;
    private Collection<UUID> ids;
    private Collection<UUID> ownerIds;
    private Boolean canPurge;
    private Boolean isPurged;
    private Instant createdAfter;
    private Collection<UUID> excludedIds;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final UserScope userScope;
    private final AuthorizationService authService;
    private final QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public StorageFileQuery(UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
	    this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public StorageFileQuery like(String value) {
        this.like = value;
        return this;
    }

    public StorageFileQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public StorageFileQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public StorageFileQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public StorageFileQuery ownerIds(UUID value) {
        this.ownerIds = List.of(value);
        return this;
    }

    public StorageFileQuery ownerIds(UUID... value) {
        this.ownerIds = Arrays.asList(value);
        return this;
    }

    public StorageFileQuery ownerIds(Collection<UUID> values) {
        this.ownerIds = values;
        return this;
    }

    public StorageFileQuery createdAfter(Instant value) {
        this.createdAfter = value;
        return this;
    }

    public StorageFileQuery canPurge(Boolean value) {
        this.canPurge = value;
        return this;
    }

    public StorageFileQuery isPurged(Boolean value) {
        this.isPurged = value;
        return this;
    }

    public StorageFileQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public StorageFileQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public StorageFileQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public StorageFileQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public StorageFileQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public StorageFileQuery disableTracking() {
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
                        this.isEmpty(this.ownerIds) ||
                        this.isEmpty(this.excludedIds) ;
    }

    @Override
    protected Class<StorageFileEntity> entityClass() {
        return StorageFileEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseStorageFile)) return null;
        UUID userId = null;
        if (this.authorize.contains(AuthorizationFlags.Owner)) userId = this.userScope.getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(StorageFileEntity._ownerId)).value(userId));
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
            predicates.add(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(StorageFileEntity._name), this.like));
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(StorageFileEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.ownerIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(StorageFileEntity._ownerId));
            for (UUID item : this.ownerIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.createdAfter != null) {
            predicates.add(queryContext.CriteriaBuilder.greaterThan(queryContext.Root.get(StorageFileEntity._createdAt), this.createdAfter));
        }
        if (this.canPurge != null) {
            if (this.canPurge) {
                predicates.add(
                        queryContext.CriteriaBuilder.and(
                                queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(StorageFileEntity._purgeAt)),
                                queryContext.CriteriaBuilder.lessThan(queryContext.Root.get(StorageFileEntity._purgeAt), Instant.now())
                        )
                );
            } else {
                predicates.add(
                        queryContext.CriteriaBuilder.or(
                                queryContext.CriteriaBuilder.isNull(queryContext.Root.get(StorageFileEntity._purgeAt)),
                                queryContext.CriteriaBuilder.greaterThan(queryContext.Root.get(StorageFileEntity._purgeAt), Instant.now())
                        )
                );
            }
        }
        if (this.isPurged != null) {
            if (this.isPurged){
                predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(StorageFileEntity._purgedAt)));
            } else {
                predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(StorageFileEntity._purgedAt)));
            }
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(StorageFileEntity._id));
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
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(StorageFile._id)) return StorageFileEntity._id;
        else if (item.match(StorageFile._name)) return StorageFileEntity._name;
        else if (item.match(StorageFile._fileRef)) return StorageFileEntity._fileRef;
        else if (item.match(StorageFile._fullName)) return StorageFileEntity._name;
        else if (item.match(StorageFile._extension)) return StorageFileEntity._extension;
        else if (item.match(StorageFile._mimeType)) return StorageFileEntity._mimeType;
        else if (item.match(StorageFile._storageType)) return StorageFileEntity._storageType;
        else if (item.match(StorageFile._createdAt)) return StorageFileEntity._createdAt;
        else if (item.match(StorageFile._purgeAt)) return StorageFileEntity._purgeAt;
        else if (item.match(StorageFile._purgedAt)) return StorageFileEntity._purgedAt;
        else if (item.match(StorageFile._owner)) return StorageFileEntity._ownerId;
        else if (item.prefix(StorageFile._owner)) return StorageFileEntity._ownerId;
        else if (item.match(StorageFile._belongsToCurrentTenant)) return StorageFileEntity._tenantId;
        else return null;
    }

    @Override
    protected StorageFileEntity convert(Tuple tuple, Set<String> columns) {
        StorageFileEntity item = new StorageFileEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, StorageFileEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, StorageFileEntity._tenantId, UUID.class));
        item.setName(QueryBase.convertSafe(tuple, columns, StorageFileEntity._name, String.class));
        item.setFileRef(QueryBase.convertSafe(tuple, columns, StorageFileEntity._fileRef, String.class));
        item.setExtension(QueryBase.convertSafe(tuple, columns, StorageFileEntity._extension, String.class));
        item.setMimeType(QueryBase.convertSafe(tuple, columns, StorageFileEntity._mimeType, String.class));
        item.setStorageType(QueryBase.convertSafe(tuple, columns, StorageFileEntity._storageType, StorageType.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, StorageFileEntity._createdAt, Instant.class));
        item.setPurgeAt(QueryBase.convertSafe(tuple, columns, StorageFileEntity._purgeAt, Instant.class));
        item.setPurgedAt(QueryBase.convertSafe(tuple, columns, StorageFileEntity._purgedAt, Instant.class));
        item.setOwnerId(QueryBase.convertSafe(tuple, columns, StorageFileEntity._ownerId, UUID.class));
        return item;
    }

}
