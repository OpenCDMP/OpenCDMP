package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanDescriptionTemplateEntity;
import org.opencdmp.model.PublicPlan;
import org.opencdmp.model.PublicPlanDescriptionTemplate;
import org.opencdmp.query.PlanQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanDescriptionTemplateBuilder extends BaseBuilder<PublicPlanDescriptionTemplate, PlanDescriptionTemplateEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicPlanDescriptionTemplateBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanDescriptionTemplateBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public PublicPlanDescriptionTemplateBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanDescriptionTemplate> build(FieldSet fields, List<PlanDescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        FieldSet planFields = fields.extractPrefixed(this.asPrefix(PublicPlanDescriptionTemplate._plan));
        Map<UUID, PublicPlan> planItemsMap = this.collectPlans(planFields, data);

        List<PublicPlanDescriptionTemplate> models = new ArrayList<>();
        for (PlanDescriptionTemplateEntity d : data) {
            PublicPlanDescriptionTemplate m = new PublicPlanDescriptionTemplate();
            if (fields.hasField(this.asIndexer(PublicPlanDescriptionTemplate._id)))  m.setId(d.getId());
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId())) m.setPlan(planItemsMap.get(d.getPlanId()));
            if (fields.hasField(this.asIndexer(PublicPlanDescriptionTemplate._sectionId)))  m.setSectionId(d.getSectionId());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, PublicPlan> collectPlans(FieldSet fields, List<PlanDescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicPlan.class.getSimpleName());

        Map<UUID, PublicPlan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicPlan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanDescriptionTemplateEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicPlan item = new PublicPlan();
                        item.setId(x);
                        return item;
                    },
                    PublicPlan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicPlan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanDescriptionTemplateEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicPlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicPlan::getId);
        }
        if (!fields.hasField(PublicPlan._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

}
