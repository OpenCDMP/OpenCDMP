package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.SupportiveMaterialEntity;
import org.opencdmp.model.*;
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
public class SupportiveMaterialBuilder extends BaseBuilder<SupportiveMaterial, SupportiveMaterialEntity>{

    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public SupportiveMaterialBuilder(
		    ConventionService conventionService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SupportiveMaterialBuilder.class)));
	    this.tenantScope = tenantScope;
    }

    public SupportiveMaterialBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<SupportiveMaterial> build(FieldSet fields, List<SupportiveMaterialEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<SupportiveMaterial> models = new ArrayList<>();
        for (SupportiveMaterialEntity d : data) {
            SupportiveMaterial m = new SupportiveMaterial();
            if (fields.hasField(this.asIndexer(SupportiveMaterial._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(SupportiveMaterial._type))) m.setType(d.getType());
            if (fields.hasField(this.asIndexer(SupportiveMaterial._languageCode))) m.setLanguageCode(d.getLanguageCode());
            if (fields.hasField(this.asIndexer(SupportiveMaterial._payload))) m.setPayload(d.getPayload());
            if (fields.hasField(this.asIndexer(SupportiveMaterial._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(SupportiveMaterial._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(SupportiveMaterial._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(SupportiveMaterial._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(SupportiveMaterial._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
