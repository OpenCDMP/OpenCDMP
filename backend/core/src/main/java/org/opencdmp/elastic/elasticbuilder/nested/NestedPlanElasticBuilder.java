package org.opencdmp.elastic.elasticbuilder.nested;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanReferenceEntity;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.elastic.data.nested.NestedCollaboratorElasticEntity;
import org.opencdmp.elastic.data.nested.NestedPlanElasticEntity;
import org.opencdmp.elastic.data.nested.NestedDoiElasticEntity;
import org.opencdmp.elastic.data.nested.NestedReferenceElasticEntity;
import org.opencdmp.elastic.elasticbuilder.BaseElasticBuilder;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.query.PlanReferenceQuery;
import org.opencdmp.query.PlanUserQuery;
import org.opencdmp.query.EntityDoiQuery;
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
public class NestedPlanElasticBuilder extends BaseElasticBuilder<NestedPlanElasticEntity, PlanEntity> {

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    
    @Autowired
    public NestedPlanElasticBuilder(
            ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedPlanElasticBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    @Override
    public List<NestedPlanElasticEntity> build(List<PlanEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        Map<UUID, List<NestedReferenceElasticEntity>> referenceElasticEntityMap = this.collectPlanReferences(data);
        Map<UUID, List<NestedCollaboratorElasticEntity>> collaboratorElasticEntityMap = this.collectCollaborators(data);
        Map<UUID, List<NestedDoiElasticEntity>> doiElasticEntityMap = this.collectDois(data);
        
        List<NestedPlanElasticEntity> models = new ArrayList<>();
        for (PlanEntity d : data) {
            NestedPlanElasticEntity m = new NestedPlanElasticEntity();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            m.setVersion(d.getVersion());
            m.setStatusId(d.getStatusId());
            m.setAccessType(d.getAccessType());
            m.setLanguage(d.getLanguage());
            m.setBlueprintId(d.getBlueprintId());
            m.setGroupId(d.getGroupId());
            m.setGroupId(d.getGroupId());
            m.setVersionStatus(d.getVersionStatus());
            if (d.getFinalizedAt() != null) {
                m.setFinalizedAt(Date.from(d.getFinalizedAt()));
            }
            if (referenceElasticEntityMap != null) m.setReferences(referenceElasticEntityMap.getOrDefault(d.getId(), null));
            if (collaboratorElasticEntityMap != null) m.setCollaborators(collaboratorElasticEntityMap.getOrDefault(d.getId(), null));
            if (doiElasticEntityMap != null) m.setDois(doiElasticEntityMap.getOrDefault(d.getId(), null));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, List<NestedReferenceElasticEntity>> collectPlanReferences(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PlanReference.class.getSimpleName());

        PlanReferenceQuery associationQuery = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().planIds(data.stream().map(PlanEntity::getId).collect(Collectors.toList())).isActives(IsActive.Active);
        List<PlanReferenceEntity> associationEntities = associationQuery.collect();
        
        ReferenceQuery query = this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).ids(associationEntities.stream().map(PlanReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()));
        Map<UUID, NestedReferenceElasticEntity> itemMapById = this.builderFactory.builder(NestedReferenceElasticBuilder.class).asForeignKey(query, NestedReferenceElasticEntity::getId);

        Map<UUID, List<NestedReferenceElasticEntity>> itemMap = new HashMap<>();
        for (PlanReferenceEntity associationEntity : associationEntities){
            if (!itemMap.containsKey(associationEntity.getPlanId())) itemMap.put(associationEntity.getPlanId(), new ArrayList<>());
            NestedReferenceElasticEntity item = itemMapById.getOrDefault(associationEntity.getReferenceId(), null);
            if (item != null) itemMap.get(associationEntity.getPlanId()).add(item);
        }

        return itemMap;
    }

    private Map<UUID, List<NestedCollaboratorElasticEntity>> collectCollaborators(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanUserEntity.class.getSimpleName());

        PlanUserQuery associationQuery = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(data.stream().map(PlanEntity::getId).collect(Collectors.toList())).isActives(IsActive.Active);
        List<PlanUserEntity> associationEntities = associationQuery.collect();

        Map<UUID, NestedCollaboratorElasticEntity> itemMapById = this.builderFactory.builder(NestedCollaboratorElasticBuilder.class).asForeignKey(associationEntities, NestedCollaboratorElasticEntity::getId);

        Map<UUID, List<NestedCollaboratorElasticEntity>> itemMap = new HashMap<>();
        for (PlanUserEntity associationEntity : associationEntities){
            if (!itemMap.containsKey(associationEntity.getId())) itemMap.put(associationEntity.getPlanId(), new ArrayList<>());
            NestedCollaboratorElasticEntity item = itemMapById.getOrDefault(associationEntity.getId(), null);
            if (item != null) itemMap.get(associationEntity.getPlanId()).add(item);
        }
        return itemMap;
    }
    
    private Map<UUID, List<NestedDoiElasticEntity>> collectDois(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", EntityDoiEntity.class.getSimpleName());

        EntityDoiQuery associationQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().entityIds(data.stream().map(PlanEntity::getId).collect(Collectors.toList())).isActive(IsActive.Active);
        List<EntityDoiEntity> associationEntities = associationQuery.collect();

        Map<UUID, NestedDoiElasticEntity> itemMapById = this.builderFactory.builder(NestedDoiElasticBuilder.class).asForeignKey(associationEntities, NestedDoiElasticEntity::getId);

        Map<UUID, List<NestedDoiElasticEntity>> itemMap = new HashMap<>();
        for (EntityDoiEntity associationEntity : associationEntities){
            if (!itemMap.containsKey(associationEntity.getId())) itemMap.put(associationEntity.getEntityId(), new ArrayList<>());
            NestedDoiElasticEntity item = itemMapById.getOrDefault(associationEntity.getId(), null);
            if (item != null) itemMap.get(associationEntity.getEntityId()).add(item);
        }
        return itemMap;
    }

}
