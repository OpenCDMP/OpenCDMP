package org.opencdmp.model.builder.planworkflow;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanWorkflowEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planworkflow.PlanWorkflow;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanWorkflowBuilder extends BaseBuilder<PlanWorkflow, PlanWorkflowEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final XmlHandlingService xmlHandlingService;
    private final BuilderFactory builderFactory;
    private final TenantScope tenantScope;


    public PlanWorkflowBuilder(ConventionService conventionService, XmlHandlingService xmlHandlingService, BuilderFactory builderFactory, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanWorkflowBuilder.class)));
        this.xmlHandlingService = xmlHandlingService;
        this.builderFactory = builderFactory;
        this.tenantScope = tenantScope;
    }

    public PlanWorkflowBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanWorkflow> build(FieldSet fields, List<PlanWorkflowEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || fields.isEmpty() || data == null)
            return new ArrayList<>();

        List<PlanWorkflow> models = new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PlanWorkflow._definition));

        for (PlanWorkflowEntity d : data) {
            PlanWorkflow m = new PlanWorkflow();
            if (fields.hasField(this.asIndexer(PlanWorkflow._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PlanWorkflow._name))) m.setName(d.getName());
            if (fields.hasField(this.asIndexer(PlanWorkflow._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(PlanWorkflow._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PlanWorkflow._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanWorkflow._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PlanWorkflow._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(PlanWorkflow._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));

            if (definitionFields != null && d.getDefinition() != null && !d.getDefinition().isBlank()) {
                PlanWorkflowDefinitionEntity definitionData = this.xmlHandlingService.fromXmlSafe(PlanWorkflowDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(PlanWorkflowDefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definitionData));
            }

            models.add(m);
        }

        return models;
    }
}
