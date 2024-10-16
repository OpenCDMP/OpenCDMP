package org.opencdmp.model.builder.descriptionworkflow;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.descriptionworkflow.DescriptionWorkflowDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionWorkflowEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptionworkflow.DescriptionWorkflow;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionWorkflowBuilder extends BaseBuilder<DescriptionWorkflow, DescriptionWorkflowEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final TenantScope tenantScope;
    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;

    public DescriptionWorkflowBuilder(ConventionService conventionService, TenantScope tenantScope, BuilderFactory builderFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionWorkflowBuilder.class)));
        this.tenantScope = tenantScope;
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;
    }

    public DescriptionWorkflowBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionWorkflow> build(FieldSet fields, List<DescriptionWorkflowEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || fields.isEmpty() || data == null)
            return new ArrayList<>();

        List<DescriptionWorkflow> models = new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(DescriptionWorkflow._definition));

        for (DescriptionWorkflowEntity d : data) {
            DescriptionWorkflow m = new DescriptionWorkflow();
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._name))) m.setName(d.getName());
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(DescriptionWorkflow._definition))) {
                DescriptionWorkflowDefinitionEntity definitionData = this.xmlHandlingService.fromXmlSafe(DescriptionWorkflowDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DescriptionWorkflowDefinitionBuilder.class).build(definitionFields, definitionData));
            }

            models.add(m);
        }

        return models;
    }
}
