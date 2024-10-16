package org.opencdmp.model.builder.referencetype;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.referencetype.ReferenceTypeDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.referencetype.ReferenceType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeBuilder extends BaseBuilder<ReferenceType, ReferenceTypeEntity> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceTypeBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeBuilder.class)));
        this.builderFactory = builderFactory;
	    this.tenantScope = tenantScope;
        this.xmlHandlingService = xmlHandlingService;
    }

    public ReferenceTypeBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<ReferenceType> build(FieldSet fields, List<ReferenceTypeEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(ReferenceType._definition));

        List<ReferenceType> models = new ArrayList<>();
        for (ReferenceTypeEntity d : data) {
            ReferenceType m = new ReferenceType();
            if (fields.hasField(this.asIndexer(ReferenceType._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(ReferenceType._name))) m.setName(d.getName());
            if (fields.hasField(this.asIndexer(ReferenceType._code))) m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(ReferenceType._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(ReferenceType._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(ReferenceType._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(ReferenceType._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(ReferenceType._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!definitionFields.isEmpty() && d.getDefinition() != null){
                ReferenceTypeDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(ReferenceTypeDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(ReferenceTypeDefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
