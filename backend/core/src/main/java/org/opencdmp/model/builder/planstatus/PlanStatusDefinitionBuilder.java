package org.opencdmp.model.builder.planstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.IconConfigurationBuilder;
import org.opencdmp.model.planstatus.PlanStatusDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanStatusDefinitionBuilder extends BaseBuilder<PlanStatusDefinition, PlanStatusDefinitionEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private boolean isPublic;

    public PlanStatusDefinitionBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanStatusDefinitionAuthorizationItemBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public PlanStatusDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanStatusDefinitionBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    @Override
    public List<PlanStatusDefinition> build(FieldSet fields, List<PlanStatusDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanStatusDefinition> models = new ArrayList<>();

        FieldSet iconConfigurationFields = fields.extractPrefixed(this.asPrefix(PlanStatusDefinition._iconConfiguration));

        FieldSet authorizationFields = fields.extractPrefixed(this.asPrefix(PlanStatusDefinition._authorization));

        for (PlanStatusDefinitionEntity d : data) {
            PlanStatusDefinition m = new PlanStatusDefinition();

            if (!this.isPublic && !authorizationFields.isEmpty() && d.getAuthorization() != null) {
                m.setAuthorization(this.builderFactory.builder(PlanStatusDefinitionAuthorizationBuilder.class).authorize(this.authorize).build(authorizationFields, d.getAuthorization()));
            }

            if (fields.hasField(this.asIndexer(PlanStatusDefinition._availableActions))) m.setAvailableActions(d.getAvailableActions());

            if(!this.isPublic && fields.hasField(this.asIndexer(PlanStatusDefinition._statusColor))) m.setStatusColor(d.getStatusColor());

            if (!this.isPublic && !iconConfigurationFields.isEmpty() && d.getIconConfiguration() != null) m.setIconConfiguration(this.builderFactory.builder(IconConfigurationBuilder.class).authorize(this.authorize).build(iconConfigurationFields, d.getIconConfiguration()));

            models.add(m);
        }

        return models;
    }

}
