package org.opencdmp.model.builder.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import gr.cite.tools.data.builder.BuilderFactory;
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
public class PlanBlueprintBuilder extends BaseBuilder<PlanBlueprint, PlanBlueprintEntity> {

    private final BuilderFactory builderFactory;

    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanBlueprintBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBlueprintBuilder.class)));
        this.builderFactory = builderFactory;
        this.xmlHandlingService = xmlHandlingService;
	    this.tenantScope = tenantScope;
    }

    public PlanBlueprintBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanBlueprint> build(FieldSet fields, List<PlanBlueprintEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PlanBlueprint._definition));
        List<PlanBlueprint> models = new ArrayList<>();
        for (PlanBlueprintEntity d : data) {
            PlanBlueprint m = new PlanBlueprint();
            if (fields.hasField(this.asIndexer(PlanBlueprint._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PlanBlueprint._label)))
                m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(PlanBlueprint._code)))
                m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(PlanBlueprint._status)))
                m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(PlanBlueprint._groupId)))
                m.setGroupId(d.getGroupId());
            if (fields.hasField(this.asIndexer(PlanBlueprint._version)))
                m.setVersion(d.getVersion());
            if (fields.hasField(this.asIndexer(PlanBlueprint._versionStatus)))
                m.setVersionStatus(d.getVersionStatus());
            if (fields.hasField(this.asIndexer(PlanBlueprint._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PlanBlueprint._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanBlueprint._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PlanBlueprint._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(PlanBlueprint._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
