package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;


import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.model.PublicDescriptionStatus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicDescriptionStatusBuilder extends BaseBuilder<PublicDescriptionStatus, DescriptionStatusEntity> {

    private final XmlHandlingService xmlHandlingService;
    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PublicDescriptionStatusBuilder(
            ConventionService conventionService, XmlHandlingService xmlHandlingService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicDescriptionStatusBuilder.class)));
        this.xmlHandlingService = xmlHandlingService;
        this.builderFactory = builderFactory;
    }

    public PublicDescriptionStatusBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicDescriptionStatus> build(FieldSet fields, List<DescriptionStatusEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        List<PublicDescriptionStatus> models = new ArrayList<>();
        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PublicDescriptionStatus._definition));

        for (DescriptionStatusEntity d : data) {
            PublicDescriptionStatus m = new PublicDescriptionStatus();
            if (fields.hasField(this.asIndexer(PublicDescriptionStatus._id)))  m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicDescriptionStatus._name)))  m.setName(d.getName());
            if (fields.hasField(this.asIndexer(PublicDescriptionStatus._internalStatus)))  m.setInternalStatus(d.getInternalStatus());
            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                DescriptionStatusDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DescriptionStatusDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(PublicDescriptionStatusDefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
