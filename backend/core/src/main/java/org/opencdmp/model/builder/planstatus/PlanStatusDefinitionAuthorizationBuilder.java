package org.opencdmp.model.builder.planstatus;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionAuthorizationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planstatus.PlanStatusDefinitionAuthorization;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanStatusDefinitionAuthorizationBuilder extends BaseBuilder<PlanStatusDefinitionAuthorization, PlanStatusDefinitionAuthorizationEntity> {

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanStatusDefinitionAuthorizationBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanStatusDefinitionAuthorizationItemBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PlanStatusDefinitionAuthorizationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanStatusDefinitionAuthorization> build(FieldSet fields, List<PlanStatusDefinitionAuthorizationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PlanStatusDefinitionAuthorization> models = new ArrayList<>();

        FieldSet editFields = fields.extractPrefixed(this.asPrefix(PlanStatusDefinitionAuthorization._edit));

        for (PlanStatusDefinitionAuthorizationEntity d : data) {
            PlanStatusDefinitionAuthorization m = new PlanStatusDefinitionAuthorization();

            if (!editFields.isEmpty() && d.getEdit() != null) {
                m.setEdit(this.builderFactory.builder(PlanStatusDefinitionAuthorizationItemBuilder.class).authorize(this.authorize).build(editFields, d.getEdit()));
            }

            models.add(m);
        }

        return models;
    }
}
