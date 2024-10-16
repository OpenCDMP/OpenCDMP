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
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.model.PublicDescription;
import org.opencdmp.model.PublicDescriptionTemplate;
import org.opencdmp.model.PublicPlan;
import org.opencdmp.model.PublicPlanDescriptionTemplate;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.PlanDescriptionTemplateQuery;
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
public class PublicDescriptionBuilder extends BaseBuilder<PublicDescription, DescriptionEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicDescriptionBuilder(
            ConventionService conventionService,
            QueryFactory queryFactory,
            BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicDescriptionBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public PublicDescriptionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicDescription> build(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet planDescriptionTemplateFields = fields.extractPrefixed(this.asPrefix(PublicDescription._planDescriptionTemplate));
        Map<UUID, PublicPlanDescriptionTemplate> planDescriptionTemplateItemsMap = this.collectPlanDescriptionTemplates(planDescriptionTemplateFields, data);

        FieldSet descriptionTemplateFields = fields.extractPrefixed(this.asPrefix(PublicDescription._descriptionTemplate));
        Map<UUID, PublicDescriptionTemplate> descriptionTemplateItemsMap = this.collectDescriptionTemplates(descriptionTemplateFields, data);

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(PublicDescription._plan));
        Map<UUID, PublicPlan> planItemsMap = this.collectPlans(planFields, data);

        List<PublicDescription> models = new ArrayList<>();
        for (DescriptionEntity d : data) {
            PublicDescription m = new PublicDescription();
            if (fields.hasField(this.asIndexer(PublicDescription._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicDescription._label)))  m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(PublicDescription._status))) m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(PublicDescription._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(PublicDescription._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PublicDescription._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PublicDescription._finalizedAt))) m.setFinalizedAt(d.getFinalizedAt());
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId()))  m.setPlan(planItemsMap.get(d.getPlanId()));
            if (!planDescriptionTemplateFields.isEmpty() && planDescriptionTemplateItemsMap != null && planDescriptionTemplateItemsMap.containsKey(d.getPlanDescriptionTemplateId()))  m.setPlanDescriptionTemplate(planDescriptionTemplateItemsMap.get(d.getPlanDescriptionTemplateId()));
            if (!descriptionTemplateFields.isEmpty() && descriptionTemplateItemsMap != null && descriptionTemplateItemsMap.containsKey(d.getDescriptionTemplateId()))  m.setDescriptionTemplate(descriptionTemplateItemsMap.get(d.getDescriptionTemplateId()));
            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, PublicPlanDescriptionTemplate> collectPlanDescriptionTemplates(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicPlanDescriptionTemplate.class.getSimpleName());

        Map<UUID, PublicPlanDescriptionTemplate> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicPlanDescriptionTemplate._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getPlanDescriptionTemplateId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicPlanDescriptionTemplate item = new PublicPlanDescriptionTemplate();
                        item.setId(x);
                        return item;
                    },
                    PublicPlanDescriptionTemplate::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicPlanDescriptionTemplate._id);
            PlanDescriptionTemplateQuery q = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getPlanDescriptionTemplateId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicPlanDescriptionTemplateBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicPlanDescriptionTemplate::getId);
        }
        if (!fields.hasField(PublicPlanDescriptionTemplate._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, PublicDescriptionTemplate> collectDescriptionTemplates(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicDescriptionTemplate.class.getSimpleName());

        Map<UUID, PublicDescriptionTemplate> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicDescriptionTemplate._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicDescriptionTemplate item = new PublicDescriptionTemplate();
                        item.setId(x);
                        return item;
                    },
                    PublicDescriptionTemplate::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicDescriptionTemplate._id);
            DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicDescriptionTemplateBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicDescriptionTemplate::getId);
        }
        if (!fields.hasField(PublicDescriptionTemplate._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, PublicPlan> collectPlans(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicPlan.class.getSimpleName());

        Map<UUID, PublicPlan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicPlan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicPlan item = new PublicPlan();
                        item.setId(x);
                        return item;
                    },
                    PublicPlan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicPlan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(DescriptionEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicPlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicPlan::getId);
        }
        if (!fields.hasField(PublicPlan._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

}
