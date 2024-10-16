package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.model.*;
import org.opencdmp.query.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanBuilder extends BaseBuilder<PublicPlan, PlanEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicPlanBuilder(ConventionService conventionService,
                             QueryFactory queryFactory,
                             BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    public PublicPlanBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlan> build(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<PublicPlan> models = new ArrayList<>();

        FieldSet planReferencesFields = fields.extractPrefixed(this.asPrefix(PublicPlan._planReferences));
        Map<UUID, List<PublicPlanReference>> planReferenceMap = this.collectPlanReferences(planReferencesFields, data);

        FieldSet planUsersFields = fields.extractPrefixed(this.asPrefix(PublicPlan._planUsers));
        Map<UUID, List<PublicPlanUser>> planUsersMap = this.collectPlanUsers(planUsersFields, data);

        FieldSet descriptionsFields = fields.extractPrefixed(this.asPrefix(PublicPlan._descriptions));
        Map<UUID, List<PublicDescription>> descriptionsMap = this.collectPlanDescriptions(descriptionsFields, data);

        FieldSet entityDoisFields = fields.extractPrefixed(this.asPrefix(PublicPlan._entityDois));
        Map<UUID, List<PublicEntityDoi>> entityDoisMap = this.collectEntityDois(entityDoisFields, data);

        FieldSet otherPlanVersionsFields = fields.extractPrefixed(this.asPrefix(PublicPlan._otherPlanVersions));
        Map<UUID, List<PublicPlan>> otherPlanVersionsMap = this.collectOtherPlanVersions(otherPlanVersionsFields, data);

        for (PlanEntity d : data) {
            PublicPlan m = new PublicPlan();
            if (fields.hasField(this.asIndexer(PublicPlan._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicPlan._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(PublicPlan._version))) m.setVersion(d.getVersion());
            if (fields.hasField(this.asIndexer(PublicPlan._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(PublicPlan._finalizedAt))) m.setFinalizedAt(d.getFinalizedAt());
            if (fields.hasField(this.asIndexer(PublicPlan._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PublicPlan._accessType))) m.setAccessType(d.getAccessType());
            if (fields.hasField(this.asIndexer(PublicPlan._status))) m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(PublicPlan._groupId))) m.setGroupId(d.getGroupId());
            if (fields.hasField(this.asIndexer(PublicPlan._accessType))) m.setAccessType(d.getAccessType());

            if (planReferenceMap != null && !planReferenceMap.isEmpty() && planReferenceMap.containsKey(d.getId())) m.setPlanReferences(planReferenceMap.get(d.getId()));
            if (planUsersMap != null && !planUsersMap.isEmpty() && planUsersMap.containsKey(d.getId())) m.setPlanUsers(planUsersMap.get(d.getId()));
            if (descriptionsMap != null && !descriptionsMap.isEmpty() && descriptionsMap.containsKey(d.getId())) m.setDescriptions(descriptionsMap.get(d.getId()));
            if (entityDoisMap != null && !entityDoisMap.isEmpty() && entityDoisMap.containsKey(d.getId())) m.setEntityDois(entityDoisMap.get(d.getId()));
            if (otherPlanVersionsMap != null && !otherPlanVersionsMap.isEmpty() && otherPlanVersionsMap.containsKey(d.getGroupId())){
                m.setOtherPlanVersions(otherPlanVersionsMap.get(d.getGroupId()));
                m.getOtherPlanVersions().sort(Comparator.comparing(PublicPlan::getVersion));
            }

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, List<PublicPlanReference>> collectPlanReferences(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PublicPlanReference.class.getSimpleName());

        Map<UUID, List<PublicPlanReference>> itemMap = null;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PublicPlanReference._plan, PublicPlan._id));
        PlanReferenceQuery query = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PublicPlanReferenceBuilder.class).authorize(this.authorize).authorize(this.authorize).asMasterKey(query, clone, x -> x.getPlan().getId());

        if (!fields.hasField(this.asIndexer(PublicPlanReference._plan, PublicPlan._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getPlan() != null).forEach(x -> {
                x.getPlan().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PublicPlanUser>> collectPlanUsers(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PublicPlanUser.class.getSimpleName());

        Map<UUID, List<PublicPlanUser>> itemMap = null;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PublicPlanUser._plan, PublicPlan._id));
        PlanUserQuery query = this.queryFactory.query(PlanUserQuery.class).disableTracking().authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PublicPlanUserBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getPlan().getId());

        if (!fields.hasField(this.asIndexer(PublicPlanUser._plan, PublicPlan._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getPlan() != null).forEach(x -> {
                x.getPlan().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PublicDescription>> collectPlanDescriptions(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PublicDescription.class.getSimpleName());

        Map<UUID, List<PublicDescription>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PublicDescription._plan, PublicDescription._id));
        DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PublicDescriptionBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getPlan().getId());

        if (!fields.hasField(this.asIndexer(PublicDescription._plan, PublicDescription._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getPlan() != null).forEach(x -> {
                x.getPlan().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PublicEntityDoi>> collectEntityDois(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PublicEntityDoi.class.getSimpleName());

        Map<UUID, List<PublicEntityDoi>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PublicEntityDoi._entityId));
        EntityDoiQuery query = this.queryFactory.query(EntityDoiQuery.class).disableTracking().authorize(this.authorize).types(EntityType.Plan).entityIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PublicEntityDoiBuilder.class).authorize(this.authorize).asMasterKey(query, clone, PublicEntityDoi::getEntityId);

        if (!fields.hasField(this.asIndexer(PublicEntityDoi._entityId))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getEntityId() != null).forEach(x -> {
                x.setEntityId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PublicPlan>> collectOtherPlanVersions(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PublicPlan.class.getSimpleName());

        Map<UUID, List<PublicPlan>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicPlan._id);
        PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(this.authorize).groupIds(data.stream().map(PlanEntity::getGroupId).distinct().collect(Collectors.toList())).isActive(IsActive.Active);
        itemMap = this.builderFactory.builder(PublicPlanBuilder.class).authorize(this.authorize).asMasterKey(query, clone, PublicPlan::getGroupId);

        if (!fields.hasField(PublicPlan._id)) {
            itemMap.values().stream().flatMap(List::stream).filter(Objects::nonNull).forEach(x -> {
                x.setId(null);
            });
        }

        return itemMap;
    }

}
