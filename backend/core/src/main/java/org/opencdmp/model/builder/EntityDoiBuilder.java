package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.model.EntityDoi;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityDoiBuilder extends BaseBuilder<EntityDoi, EntityDoiEntity> {

    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public EntityDoiBuilder(
		    ConventionService conventionService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(EntityDoiBuilder.class)));
	    this.tenantScope = tenantScope;
    }

    public EntityDoiBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<EntityDoi> build(FieldSet fields, List<EntityDoiEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<EntityDoi> models = new ArrayList<>();
        for (EntityDoiEntity d : data) {
            EntityDoi m = new EntityDoi();
            if (fields.hasField(this.asIndexer(EntityDoi._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(EntityDoi._doi)))
                m.setDoi(d.getDoi());
            if (fields.hasField(this.asIndexer(EntityDoi._entityId)))
                m.setEntityId(d.getEntityId());
            if (fields.hasField(this.asIndexer(EntityDoi._entityType)))
                m.setEntityType(d.getEntityType());
            if (fields.hasField(this.asIndexer(EntityDoi._repositoryId)))
                m.setRepositoryId(d.getRepositoryId());
            if (fields.hasField(this.asIndexer(EntityDoi._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(EntityDoi._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(EntityDoi._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(EntityDoi._hash)))
                m.setHash(hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(EntityDoi._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }



}