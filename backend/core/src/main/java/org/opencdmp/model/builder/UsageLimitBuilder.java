package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.usagelimit.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UsageLimitEntity;
import org.opencdmp.model.UsageLimit;
import org.opencdmp.model.builder.usagelimit.DefinitionBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UsageLimitBuilder extends BaseBuilder<UsageLimit, UsageLimitEntity> {

    private final TenantScope tenantScope;
    private final XmlHandlingService xmlHandlingService;
    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public UsageLimitBuilder(
            ConventionService conventionService,
            TenantScope tenantScope, XmlHandlingService xmlHandlingService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UsageLimitBuilder.class)));
	    this.tenantScope = tenantScope;
        this.xmlHandlingService = xmlHandlingService;
        this.builderFactory = builderFactory;
    }

    public UsageLimitBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<UsageLimit> build(FieldSet fields, List<UsageLimitEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(UsageLimit._definition));

        List<UsageLimit> models = new ArrayList<>();
        for (UsageLimitEntity d : data) {
            UsageLimit m = new UsageLimit();
            if (fields.hasField(this.asIndexer(UsageLimit._id)))
                m.setId(d.getId());
            if (fields.hasField(this.asIndexer(UsageLimit._label)))
                m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(UsageLimit._targetMetric)))
                m.setTargetMetric(d.getTargetMetric());
            if (fields.hasField(this.asIndexer(UsageLimit._value)))
                m.setValue(d.getValue());
            if (fields.hasField(this.asIndexer(UsageLimit._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(UsageLimit._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(UsageLimit._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(UsageLimit._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            if (fields.hasField(this.asIndexer(UsageLimit._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
