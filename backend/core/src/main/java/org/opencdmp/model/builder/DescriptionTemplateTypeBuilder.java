package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.model.DescriptionTemplateType;
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
public class DescriptionTemplateTypeBuilder extends BaseBuilder<DescriptionTemplateType, DescriptionTemplateTypeEntity> {

    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DescriptionTemplateTypeBuilder(
		    ConventionService conventionService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionTemplateTypeBuilder.class)));
	    this.tenantScope = tenantScope;
    }

    public DescriptionTemplateTypeBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionTemplateType> build(FieldSet fields, List<DescriptionTemplateTypeEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DescriptionTemplateType> models = new ArrayList<>();
        for (DescriptionTemplateTypeEntity d : data) {
            DescriptionTemplateType m = new DescriptionTemplateType();
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._code)))
                m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._name)))
                m.setName(d.getName());
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._status)))
                m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(DescriptionTemplateType._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
