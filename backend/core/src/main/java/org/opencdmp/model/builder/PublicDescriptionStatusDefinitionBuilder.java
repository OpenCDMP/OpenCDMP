package org.opencdmp.model.builder;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;

import org.opencdmp.model.PublicDescriptionStatusDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicDescriptionStatusDefinitionBuilder extends BaseBuilder<PublicDescriptionStatusDefinition, DescriptionStatusDefinitionEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PublicDescriptionStatusDefinitionBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicDescriptionStatusDefinitionBuilder.class)));
    }

    public PublicDescriptionStatusDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicDescriptionStatusDefinition> build(FieldSet fields, List<DescriptionStatusDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        List<PublicDescriptionStatusDefinition> models = new ArrayList<>();
        for (DescriptionStatusDefinitionEntity d : data) {
            PublicDescriptionStatusDefinition m = new PublicDescriptionStatusDefinition();
            if (fields.hasField(this.asIndexer(PublicDescriptionStatusDefinition._availableActions)))  m.setAvailableActions(d.getAvailableActions());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
