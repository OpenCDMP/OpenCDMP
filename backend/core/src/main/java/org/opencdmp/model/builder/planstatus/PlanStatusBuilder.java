package org.opencdmp.model.builder.planstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planstatus.PlanStatus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanStatusBuilder extends BaseBuilder<PlanStatus, PlanStatusEntity> {

    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanStatusBuilder(ConventionService conventionService, XmlHandlingService xmlHandlingService, TenantScope tenantScope, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanStatusDefinitionAuthorizationItemBuilder.class)));
        this.xmlHandlingService = xmlHandlingService;
        this.tenantScope = tenantScope;
        this.builderFactory = builderFactory;
    }

    public PlanStatusBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanStatus> build(FieldSet fields, List<PlanStatusEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PlanStatus._definition));
        List<PlanStatus> models = new ArrayList<>();

        for (PlanStatusEntity d : data) {
            PlanStatus m = new PlanStatus();

            if (fields.hasField(this.asIndexer(PlanStatus._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PlanStatus._name)))
                m.setName(d.getName());
            if (fields.hasField(this.asIndexer(PlanStatus._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PlanStatus._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanStatus._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PlanStatus._description)))
                m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(PlanStatus._internalStatus)))
                m.setInternalStatus(d.getInternalStatus());
            if (fields.hasField(this.asIndexer(PlanStatus._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(PlanStatus._belongsToCurrentTenant)))
                m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));

            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                PlanStatusDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(PlanStatusDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(PlanStatusDefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            models.add(m);
        }

        return models;
    }
}
