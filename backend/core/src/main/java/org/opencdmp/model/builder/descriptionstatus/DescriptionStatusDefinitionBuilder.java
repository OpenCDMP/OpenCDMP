package org.opencdmp.model.builder.descriptionstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;

import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.IconConfigurationBuilder;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusDefinitionBuilder extends BaseBuilder<DescriptionStatusDefinition, DescriptionStatusDefinitionEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private boolean isPublic;

    public DescriptionStatusDefinitionBuilder(ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionStatusDefinitionBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public DescriptionStatusDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public DescriptionStatusDefinitionBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    @Override
    public List<DescriptionStatusDefinition> build(FieldSet fields, List<DescriptionStatusDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DescriptionStatusDefinition> models = new ArrayList<>();

        FieldSet iconConfigurationFields = fields.extractPrefixed(this.asPrefix(DescriptionStatusDefinition._iconConfiguration));

        FieldSet authorizationFields = fields.extractPrefixed(this.asPrefix(DescriptionStatusDefinition._authorization));
        for (DescriptionStatusDefinitionEntity d : data) {
            DescriptionStatusDefinition m = new DescriptionStatusDefinition();
            if (!this.isPublic && !authorizationFields.isEmpty() && d.getAuthorization() != null) {
                m.setAuthorization(this.builderFactory.builder(DescriptionStatusDefinitionAuthorizationBuilder.class).authorize(authorize).build(authorizationFields, d.getAuthorization()));
            }
            if (fields.hasField(this.asIndexer(DescriptionStatusDefinition._availableActions))) m.setAvailableActions(d.getAvailableActions());

            if(!this.isPublic && fields.hasField(this.asIndexer(DescriptionStatusDefinition._statusColor))) m.setStatusColor(d.getStatusColor());

            if (!this.isPublic && !iconConfigurationFields.isEmpty() && d.getIconConfiguration() != null) m.setIconConfiguration(this.builderFactory.builder(IconConfigurationBuilder.class).authorize(this.authorize).build(iconConfigurationFields, d.getIconConfiguration()));

            models.add(m);
        }

        return models;
    }

}
