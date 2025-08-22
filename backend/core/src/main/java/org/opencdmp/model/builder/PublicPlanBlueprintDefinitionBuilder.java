package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.PublicPlanBlueprintDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanBlueprintDefinitionBuilder extends BaseBuilder<PublicPlanBlueprintDefinition, DefinitionEntity>{

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicPlanBlueprintDefinitionBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanBlueprintDefinitionBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public PublicPlanBlueprintDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanBlueprintDefinition> build(FieldSet fields, List<DefinitionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet sectionsFields = fields.extractPrefixed(this.asPrefix(PublicPlanBlueprintDefinition._sections));

        List<PublicPlanBlueprintDefinition> models = new ArrayList<>();
        for (DefinitionEntity d : data) {
            PublicPlanBlueprintDefinition m = new PublicPlanBlueprintDefinition();

            if (!sectionsFields.isEmpty() && d.getSections() != null) m.setSections(this.builderFactory.builder(PublicPlanBlueprintSectionBuilder.class).authorize(this.authorize).build(sectionsFields, d.getSections()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
