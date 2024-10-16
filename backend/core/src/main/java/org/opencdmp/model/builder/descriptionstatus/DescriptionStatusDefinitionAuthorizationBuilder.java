package org.opencdmp.model.builder.descriptionstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionAuthorizationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinitionAuthorization;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusDefinitionAuthorizationBuilder extends BaseBuilder<DescriptionStatusDefinitionAuthorization, DescriptionStatusDefinitionAuthorizationEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final BuilderFactory builderFactory;
    public DescriptionStatusDefinitionAuthorizationBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionStatusDefinitionAuthorizationBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public DescriptionStatusDefinitionAuthorizationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionStatusDefinitionAuthorization> build(FieldSet fields, List<DescriptionStatusDefinitionAuthorizationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DescriptionStatusDefinitionAuthorization> models = new ArrayList<>();

        FieldSet editFields = fields.extractPrefixed(this.asPrefix(DescriptionStatusDefinitionAuthorization._edit));
        for (DescriptionStatusDefinitionAuthorizationEntity d : data) {
            DescriptionStatusDefinitionAuthorization m = new DescriptionStatusDefinitionAuthorization();
            if (!editFields.isEmpty() && d.getEdit() != null) {
                m.setEdit(this.builderFactory.builder(DescriptionStatusDefinitionAuthorizationItemBuilder.class).authorize(this.authorize).build(editFields, d.getEdit()));
            }

            models.add(m);
        }

        return models;
    }
}
