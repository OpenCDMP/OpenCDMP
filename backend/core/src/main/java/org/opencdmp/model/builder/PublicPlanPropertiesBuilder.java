package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.PublicPlanProperties;
import org.opencdmp.model.plan.PlanProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("publicplanpropertiesbuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanPropertiesBuilder extends BaseBuilder<PublicPlanProperties, PlanPropertiesEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private DefinitionEntity definition;

    @Autowired
    public PublicPlanPropertiesBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanPropertiesBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PublicPlanPropertiesBuilder withDefinition(DefinitionEntity definition) {
        this.definition = definition;
        return this;
    }

    public PublicPlanPropertiesBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanProperties> build(FieldSet fields, List<PlanPropertiesEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //Not Bulk Build because is XML no interaction with db
        FieldSet planBlueprintValuesFields = fields.extractPrefixed(this.asPrefix(PlanProperties._planBlueprintValues));
        FieldSet contactsFields = fields.extractPrefixed(this.asPrefix(PlanProperties._contacts));

        List<PublicPlanProperties> models = new ArrayList<>();
        for (PlanPropertiesEntity d : data) {
            PublicPlanProperties m = new PublicPlanProperties();
            if (!planBlueprintValuesFields.isEmpty() && d.getPlanBlueprintValues() != null) m.setPlanBlueprintValues(this.builderFactory.builder(PublicPlanBlueprintValueBuilder.class).withDefinition(definition).authorize(this.authorize).build(planBlueprintValuesFields, d.getPlanBlueprintValues()));
            if (!contactsFields.isEmpty() && d.getContacts() != null) m.setContacts(this.builderFactory.builder(PublicPlanContactBuilder.class).authorize(this.authorize).build(contactsFields, d.getContacts()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
