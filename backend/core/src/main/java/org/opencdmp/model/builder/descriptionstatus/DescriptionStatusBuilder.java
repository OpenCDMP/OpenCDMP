package org.opencdmp.model.builder.descriptionstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusBuilder extends BaseBuilder<DescriptionStatus, DescriptionStatusEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final TenantScope tenantScope;
    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;

    public DescriptionStatusBuilder(ConventionService conventionService, TenantScope tenantScope, BuilderFactory builderFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionStatusBuilder.class)));
        this.tenantScope = tenantScope;
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;
    }

    public DescriptionStatusBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionStatus> build(FieldSet fields, List<DescriptionStatusEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DescriptionStatus> models = new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(DescriptionStatus._definition));
        for (DescriptionStatusEntity d : data) {
            DescriptionStatus m = new DescriptionStatus();
            if (fields.hasField(this.asIndexer(DescriptionStatus._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(DescriptionStatus._name))) m.setName(d.getName());
            if (fields.hasField(this.asIndexer(DescriptionStatus._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(DescriptionStatus._updatedAt))) m.setUpdateAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(DescriptionStatus._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(DescriptionStatus._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(DescriptionStatus._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(DescriptionStatus._internalStatus))) m.setInternalStatus(d.getInternalStatus());
            if (fields.hasField(this.asIndexer(DescriptionStatus._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));

            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                DescriptionStatusDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DescriptionStatusDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DescriptionStatusDefinitionBuilder.class).authorize(authorize).build(definitionFields, definition));
            }

            models.add(m);
        }
        return models;
    }
}
