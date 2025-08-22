package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.model.PublicPlanBlueprint;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanBlueprintBuilder extends BaseBuilder<PublicPlanBlueprint, PlanBlueprintEntity>{

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private final XmlHandlingService xmlHandlingService;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicPlanBlueprintBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, QueryFactory queryFactory, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanBlueprintBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.xmlHandlingService = xmlHandlingService;
    }

    public PublicPlanBlueprintBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanBlueprint> build(FieldSet fields, List<PlanBlueprintEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PublicPlanBlueprint._definition));

        List<PublicPlanBlueprint> models = new ArrayList<>();
        for (PlanBlueprintEntity d : data) {
            PublicPlanBlueprint m = new PublicPlanBlueprint();
            if (fields.hasField(this.asIndexer(PublicPlanBlueprint._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicPlanBlueprint._label))) m.setLabel(d.getLabel());

            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(PublicPlanBlueprintDefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
