package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.RecentActivityItemType;
import org.opencdmp.commons.types.dashborad.RecentActivityItemEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.RecentActivityItem;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.DescriptionQuery;
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
public class RecentActivityItemBuilder extends BaseBuilder<RecentActivityItem, RecentActivityItemEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public RecentActivityItemBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RecentActivityItemBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public RecentActivityItemBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<RecentActivityItem> build(FieldSet fields, List<RecentActivityItemEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet descriptionFields = fields.extractPrefixed(this.asPrefix(RecentActivityItem._description));
        Map<UUID, Description> descriptionItemsMap = this.collectDescriptions(descriptionFields, data);

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(RecentActivityItem._plan));
        Map<UUID, Plan> planItemsMap = this.collectPlans(planFields, data);

        List<RecentActivityItem> models = new ArrayList<>();
        for (RecentActivityItemEntity d : data) {
            RecentActivityItem m = new RecentActivityItem();
            if (fields.hasField(this.asIndexer(RecentActivityItem._type))) m.setType(d.getType());
            if (!descriptionFields.isEmpty() && descriptionItemsMap != null && descriptionItemsMap.containsKey(d.getId())) m.setDescription(descriptionItemsMap.get(d.getId()));
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getId())) m.setPlan(planItemsMap.get(d.getId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }


    private Map<UUID, Plan> collectPlans(FieldSet fields, List<RecentActivityItemEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Plan.class.getSimpleName());

        Map<UUID, Plan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Plan._id))) {
            itemMap = this.asEmpty(
                    data.stream().filter(x-> x.getType().equals(RecentActivityItemType.Plan)).map(RecentActivityItemEntity::getId).distinct().collect(Collectors.toList()),
                    x -> {
                        Plan item = new Plan();
                        item.setId(x);
                        return item;
                    },
                    Plan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Plan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().filter(x-> x.getType().equals(RecentActivityItemType.Plan)).map(RecentActivityItemEntity::getId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Plan::getId);
        }
        if (!fields.hasField(Plan._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

    private Map<UUID, Description> collectDescriptions(FieldSet fields, List<RecentActivityItemEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Description.class.getSimpleName());

        Map<UUID, Description> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Description._id))) {
            itemMap = this.asEmpty(
                    data.stream().filter(x-> x.getType().equals(RecentActivityItemType.Description)).map(RecentActivityItemEntity::getId).distinct().collect(Collectors.toList()),
                    x -> {
                        Description item = new Description();
                        item.setId(x);
                        return item;
                    },
                    Description::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Description._id);
            DescriptionQuery q = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().filter(x-> x.getType().equals(RecentActivityItemType.Description)).map(RecentActivityItemEntity::getId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Description::getId);
        }
        if (!fields.hasField(Description._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

}
