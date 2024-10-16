package org.opencdmp.model.builder.prefillingsource;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.prefillingsource.PrefillingSourceDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PrefillingSourceEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrefillingSourceBuilder extends BaseBuilder<PrefillingSource, PrefillingSourceEntity> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PrefillingSourceBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PrefillingSourceBuilder.class)));
        this.builderFactory = builderFactory;
	    this.tenantScope = tenantScope;
        this.xmlHandlingService = xmlHandlingService;
    }

    public PrefillingSourceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PrefillingSource> build(FieldSet fields, List<PrefillingSourceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PrefillingSource._definition));

        List<PrefillingSource> models = new ArrayList<>();
        for (PrefillingSourceEntity d : data) {
            PrefillingSource m = new PrefillingSource();
            if (fields.hasField(this.asIndexer(PrefillingSource._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PrefillingSource._code))) m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(PrefillingSource._label))) m.setLabel(d.getLabel());
            if (!definitionFields.isEmpty() && d.getDefinition() != null){
                PrefillingSourceDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(PrefillingSourceDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(PrefillingSourceDefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }            if (fields.hasField(this.asIndexer(PrefillingSource._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PrefillingSource._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PrefillingSource._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PrefillingSource._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(PrefillingSource._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
