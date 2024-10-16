package org.opencdmp.model.builder.planreference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.planreference.PlanReferenceDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanReferenceEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.builder.reference.ReferenceBuilder;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.reference.Reference;
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
public class PlanReferenceBuilder extends BaseBuilder<PlanReference, PlanReferenceEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final TenantScope tenantScope;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanReferenceBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, JsonHandlingService jsonHandlingService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanReferenceBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
	    this.jsonHandlingService = jsonHandlingService;
	    this.tenantScope = tenantScope;
    }

    public PlanReferenceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanReference> build(FieldSet fields, List<PlanReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet referenceFields = fields.extractPrefixed(this.asPrefix(PlanReference._reference));
        Map<UUID, Reference> referenceItemsMap = this.collectReferences(referenceFields, data);

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(PlanReference._plan));
        Map<UUID, Plan> planItemsMap = this.collectPlans(planFields, data);

        FieldSet dataFields = fields.extractPrefixed(this.asPrefix(PlanReference._data));
        List<PlanReference> models = new ArrayList<>();
        for (PlanReferenceEntity d : data) {
            PlanReference m = new PlanReference();
            if (fields.hasField(this.asIndexer(PlanReference._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PlanReference._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PlanReference._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PlanReference._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanReference._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(PlanReference._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!referenceFields.isEmpty() && referenceItemsMap != null && referenceItemsMap.containsKey(d.getReferenceId())) m.setReference(referenceItemsMap.get(d.getReferenceId()));
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId())) m.setPlan(planItemsMap.get(d.getPlanId()));
            if (!dataFields.isEmpty() && d.getData() != null){
                PlanReferenceDataEntity propertyDefinition = this.jsonHandlingService.fromJsonSafe(PlanReferenceDataEntity.class, d.getData());
                m.setData(this.builderFactory.builder(PlanReferenceDataBuilder.class).authorize(this.authorize).build(dataFields, propertyDefinition));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, Reference> collectReferences(FieldSet fields, List<PlanReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Reference.class.getSimpleName());

        Map<UUID, Reference> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Reference._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()),
                    x -> {
                        Reference item = new Reference();
                        item.setId(x);
                        return item;
                    },
                    Reference::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Reference._id);
            ReferenceQuery q = this.queryFactory.query(ReferenceQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(PlanReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(ReferenceBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Reference::getId);
        }
        if (!fields.hasField(Reference._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

    private Map<UUID, Plan> collectPlans(FieldSet fields, List<PlanReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Plan.class.getSimpleName());

        Map<UUID, Plan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Plan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanReferenceEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        Plan item = new Plan();
                        item.setId(x);
                        return item;
                    },
                    Plan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Plan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(PlanReferenceEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Plan::getId);
        }
        if (!fields.hasField(Plan._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }

}
