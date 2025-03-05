package org.opencdmp.model.builder.tenantconfiguration;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.types.featured.DescriptionTemplateEntity;
import org.opencdmp.commons.types.featured.PlanBlueprintEntity;
import org.opencdmp.commons.types.tenantconfiguration.FeaturedEntitiesEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder;
import org.opencdmp.model.builder.planblueprint.PlanBlueprintBuilder;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.tenantconfiguration.FeaturedEntities;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.PlanBlueprintQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FeaturedEntitiesBuilder extends BaseBuilder<FeaturedEntities, FeaturedEntitiesEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;

    @Autowired
    public FeaturedEntitiesBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory){
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FeaturedEntitiesBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public FeaturedEntitiesBuilder authorize(EnumSet<AuthorizationFlags> values){
        this.authorize = values;
        return this;
    }

    @Override
    public List<FeaturedEntities> build(FieldSet fields, List<FeaturedEntitiesEntity> data) throws MyApplicationException {

        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet planBlueprintsFields = fields.extractPrefixed(this.asPrefix(FeaturedEntities._planBlueprints));
        FieldSet descriptionTemplatesFields = fields.extractPrefixed(this.asPrefix(FeaturedEntities._descriptionTemplates));

        List<FeaturedEntities> models = new ArrayList<>();
        for(FeaturedEntitiesEntity d : data){
            FeaturedEntities m = new FeaturedEntities();
            if (!planBlueprintsFields.isEmpty() && !this.conventionService.isListNullOrEmpty(d.getPlanBlueprints())) {
                Map<UUID, Integer> ordinalMap = new HashMap<>();
                for (PlanBlueprintEntity entity: d.getPlanBlueprints()) {
                    ordinalMap.put(entity.getGroupId(), entity.getOrdinal());
                }
                PlanBlueprintQuery query = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(this.authorize).groupIds(d.getPlanBlueprints().stream().map(PlanBlueprintEntity::getGroupId).distinct().collect(Collectors.toList())).versionStatuses(PlanBlueprintVersionStatus.Current).statuses(PlanBlueprintStatus.Finalized).isActive(IsActive.Active);
                m.setPlanBlueprints(this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(this.authorize).featuredOrdinalMap(ordinalMap).build(planBlueprintsFields, query.collectAs(planBlueprintsFields)));
                m.getPlanBlueprints().sort(Comparator.comparing(PlanBlueprint::getOrdinal));
            }
            if (!descriptionTemplatesFields.isEmpty() && !this.conventionService.isListNullOrEmpty(d.getDescriptionTemplates())) {
                Map<UUID, Integer> ordinalMap = new HashMap<>();
                for (DescriptionTemplateEntity entity: d.getDescriptionTemplates()) {
                    ordinalMap.put(entity.getGroupId(), entity.getOrdinal());
                }
                DescriptionTemplateQuery query = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).groupIds(d.getDescriptionTemplates().stream().map(DescriptionTemplateEntity::getGroupId).distinct().collect(Collectors.toList())).versionStatuses(DescriptionTemplateVersionStatus.Current).statuses(DescriptionTemplateStatus.Finalized).isActive(IsActive.Active);
                m.setDescriptionTemplates(this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(this.authorize).featuredOrdinalMap(ordinalMap).build(descriptionTemplatesFields, query.collectAs(descriptionTemplatesFields)));
                m.getDescriptionTemplates().sort(Comparator.comparing(DescriptionTemplate::getOrdinal));
            }

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
