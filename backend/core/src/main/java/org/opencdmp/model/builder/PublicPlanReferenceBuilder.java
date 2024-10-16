package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanReferenceEntity;
import org.opencdmp.model.PublicPlan;
import org.opencdmp.model.PublicPlanReference;
import org.opencdmp.model.PublicReference;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.ReferenceQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanReferenceBuilder extends BaseBuilder<PublicPlanReference, PlanReferenceEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicPlanReferenceBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanReferenceBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
    }

    public PublicPlanReferenceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanReference> build(FieldSet fields, List<PlanReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet referenceFields = fields.extractPrefixed(this.asPrefix(PublicPlanReference._reference));
        Map<UUID, PublicReference> referenceItemsMap = this.collectReferences(referenceFields, data);

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(PublicPlanReference._plan));
        Map<UUID, PublicPlan> planItemsMap = this.collectPlans(planFields, data);

        List<PublicPlanReference> models = new ArrayList<>();
        for (PlanReferenceEntity d : data) {
            PublicPlanReference m = new PublicPlanReference();
            if (fields.hasField(this.asIndexer(PublicPlanReference._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicPlanReference._isActive))) m.setIsActive(d.getIsActive());
            if (!referenceFields.isEmpty() && referenceItemsMap != null && referenceItemsMap.containsKey(d.getReferenceId()))  m.setReference(referenceItemsMap.get(d.getReferenceId()));
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId())) m.setPlan(planItemsMap.get(d.getPlanId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, PublicReference> collectReferences(FieldSet fields, List<PlanReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicReference.class.getSimpleName());

        Map<UUID, PublicReference> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicReference._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicReference item = new PublicReference();
                        item.setId(x);
                        return item;
                    },
                    PublicReference::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicReference._id);
            ReferenceQuery q = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(this.authorize).isActive(IsActive.Active).ids(data.stream().map(PlanReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicReferenceBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicReference::getId);
        }
        if (!fields.hasField(PublicReference._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

    private Map<UUID, PublicPlan> collectPlans(FieldSet fields, List<PlanReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicPlan.class.getSimpleName());

        Map<UUID, PublicPlan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicPlan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanReferenceEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicPlan item = new PublicPlan();
                        item.setId(x);
                        return item;
                    },
                    PublicPlan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicPlan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(this.authorize).isActive(IsActive.Active).ids(data.stream().map(PlanReferenceEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicPlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicPlan::getId);
        }
        if (!fields.hasField(PublicPlan._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

}
