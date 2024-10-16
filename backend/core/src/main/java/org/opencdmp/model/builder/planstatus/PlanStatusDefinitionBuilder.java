package org.opencdmp.model.builder.planstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planstatus.PlanStatusDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanStatusDefinitionBuilder extends BaseBuilder<PlanStatusDefinition, PlanStatusDefinitionEntity> {

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanStatusDefinitionBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanStatusDefinitionAuthorizationItemBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PlanStatusDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanStatusDefinition> build(FieldSet fields, List<PlanStatusDefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanStatusDefinition> models = new ArrayList<>();

        FieldSet authorizationFields = fields.extractPrefixed(this.asPrefix(PlanStatusDefinition._authorization));

        for (PlanStatusDefinitionEntity d : data) {
            PlanStatusDefinition m = new PlanStatusDefinition();

            if (!authorizationFields.isEmpty() && d.getAuthorization() != null) {
                m.setAuthorization(this.builderFactory.builder(PlanStatusDefinitionAuthorizationBuilder.class).authorize(this.authorize).build(authorizationFields, d.getAuthorization()));
            }

            models.add(m);
        }

        return models;
    }
}
