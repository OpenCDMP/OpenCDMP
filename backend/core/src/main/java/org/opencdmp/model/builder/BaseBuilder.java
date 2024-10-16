package org.opencdmp.model.builder;

import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.PermissionNameProvider;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.Builder;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;

import javax.management.InvalidApplicationException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseBuilder<M, D> implements Builder {
    protected final LoggerService logger;
    protected final ConventionService conventionService;

    public BaseBuilder(
            ConventionService conventionService,
            LoggerService logger
    ) {
        this.conventionService = conventionService;
        this.logger = logger;
    }

    public M build(FieldSet directives, D data) throws MyApplicationException {
        if (data == null) {
            //this.logger.Debug(new MapLogEntry("requested build for null item requesting fields").And("fields", directives));
//			return default(M);
            M model = null;
            return null; //TODO
        }
        List<M> models = this.build(directives, Arrays.asList(data));
        return models.stream().findFirst().orElse(null); //TODO
    }

    public abstract List<M> build(FieldSet directives, List<D> data) throws MyApplicationException;

    public <K> Map<K, M> asForeignKey(QueryBase<D> query, FieldSet directives, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("Building references from query");
        List<D> data = query.collectAs(directives);
        this.logger.debug("collected {} items to build", Optional.ofNullable(data).map(List::size).orElse(0));
        return this.asForeignKey(data, directives, keySelector);
    }

    public <K> Map<K, M> asForeignKey(List<D> data, FieldSet directives, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("building references");
        List<M> models = this.build(directives, data);
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(List::size).orElse(0), Optional.ofNullable(data).map(List::size).orElse(0));
        Map<K, M> map = models.stream().collect(Collectors.toMap(keySelector, o -> o));
        return map;
    }

    public <K> Map<K, List<M>> asMasterKey(QueryBase<D> query, FieldSet directives, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("Building details from query");
        List<D> data = query.collectAs(directives);
        this.logger.debug("collected {} items to build", Optional.ofNullable(data).map(List::size).orElse(0));
        return this.asMasterKey(data, directives, keySelector);
    }

    public <K> Map<K, List<M>> asMasterKey(List<D> data, FieldSet directives, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("building details");
        List<M> models = this.build(directives, data);
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(List::size).orElse(0), Optional.ofNullable(data).map(List::size).orElse(0));
        Map<K, List<M>> map = new HashMap<>();
        for (M model : models) {
            K key = keySelector.apply(model);
            if (!map.containsKey(key)) map.put(key, new ArrayList<M>());
            map.get(key).add(model);
        }
        return map;
    }

    public <FK, FM> Map<FK, FM> asEmpty(List<FK> keys, Function<FK, FM> mapper, Function<FM, FK> keySelector) {
        this.logger.trace("building static references");
        List<FM> models = keys.stream().map(mapper).collect(Collectors.toList());
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(List::size).orElse(0), Optional.ofNullable(keys).map(List::size));
        Map<FK, FM> map = models.stream().collect(Collectors.toMap(keySelector, o -> o));
        return map;
    }

    protected String hashValue(Instant value) throws MyApplicationException {
        return this.conventionService.hashValue(value);
    }

    protected String asPrefix(String name) {
        return this.conventionService.asPrefix(name);
    }

    protected String asIndexer(String... names) {
        return this.conventionService.asIndexer(names);
    }


    protected Set<String> extractAuthorizationFlags(FieldSet fields, String propertyName, List<String> permissionNames){
        if (fields == null) return new HashSet<>();
        if (permissionNames == null) return new HashSet<>();
        
        FieldSet authorizationFlags = fields.extractPrefixed(this.asPrefix(propertyName));
        List<String> permissions = new ArrayList<>();
        for (String fieldValue : authorizationFlags.getFields()) permissions.addAll(permissionNames.stream().filter(x-> x.equalsIgnoreCase(fieldValue)).toList());
        return new HashSet<>(permissions);
    }

    protected List<String> evaluateAuthorizationFlags(AuthorizationService authorizationService, Set<String> authorizationFlags, AffiliatedResource affiliatedResource) {
        List<String> allowed = new ArrayList<>();
        if (authorizationFlags == null) return allowed;
        if (authorizationService == null) return allowed;

        for (String permission : authorizationFlags) {
            Boolean isAllowed = affiliatedResource == null ?  authorizationService.authorize(permission) : authorizationService.authorizeAtLeastOne(List.of(affiliatedResource), permission);
            if (isAllowed) allowed.add(permission);
        }
        return allowed;
    }

    protected boolean getBelongsToCurrentTenant(TenantScopedBaseEntity entity, TenantScope tenantScope){
        if (!tenantScope.isSet()) return true;
        try {
            if (entity.getTenantId() == null && tenantScope.getTenant() == null) return true;
            if (entity.getTenantId() == null || tenantScope.getTenant() == null) return false;

            return entity.getTenantId().equals(tenantScope.getTenant());
        } catch (InvalidApplicationException e) {
            return false;
        }
    }

}

