package org.opencdmp.elastic.elasticbuilder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.DescriptionReferenceEntity;
import org.opencdmp.data.DescriptionTagEntity;
import org.opencdmp.elastic.data.DescriptionElasticEntity;
import org.opencdmp.elastic.data.nested.NestedDescriptionTemplateElasticEntity;
import org.opencdmp.elastic.data.nested.NestedPlanElasticEntity;
import org.opencdmp.elastic.data.nested.NestedReferenceElasticEntity;
import org.opencdmp.elastic.data.nested.NestedTagElasticEntity;
import org.opencdmp.elastic.elasticbuilder.nested.NestedDescriptionTemplateElasticBuilder;
import org.opencdmp.elastic.elasticbuilder.nested.NestedPlanElasticBuilder;
import org.opencdmp.elastic.elasticbuilder.nested.NestedReferenceElasticBuilder;
import org.opencdmp.elastic.elasticbuilder.nested.NestedTagElasticBuilder;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
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
public class DescriptionElasticBuilder extends BaseElasticBuilder<DescriptionElasticEntity, DescriptionEntity> {

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    
    @Autowired
    public DescriptionElasticBuilder(
            ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionElasticBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    @Override
    public List<DescriptionElasticEntity> build(List<DescriptionEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        Map<UUID, List<NestedReferenceElasticEntity>> referenceElasticEntityMap = this.collectDescriptionReferences(data);
        Map<UUID, List<NestedTagElasticEntity>> tagElasticEntityMap = this.collectDescriptionTags(data);
        Map<UUID, NestedPlanElasticEntity> planElasticEntityMap = this.collectPlans(data);
        Map<UUID, NestedDescriptionTemplateElasticEntity> descriptionTemplateElasticEntityMap = this.collectDescriptionTemplates(data);
        
        List<DescriptionElasticEntity> models = new ArrayList<>();
        for (DescriptionEntity d : data) {
            DescriptionElasticEntity m = new DescriptionElasticEntity();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            m.setStatus(d.getStatus());
            m.setCreatedAt(Date.from(d.getCreatedAt()));
            m.setUpdatedAt(Date.from(d.getUpdatedAt()));
            if (d.getFinalizedAt() != null) {
                m.setFinalizedAt(Date.from(d.getFinalizedAt()));
            }
            if (d.getTenantId() != null) {
                m.setTenantId(d.getTenantId());
            }
            if (referenceElasticEntityMap != null) m.setReferences(referenceElasticEntityMap.getOrDefault(d.getId(), null));
            if (tagElasticEntityMap != null) m.setTags(tagElasticEntityMap.getOrDefault(d.getId(), null));
            if (planElasticEntityMap != null) m.setPlan(planElasticEntityMap.getOrDefault(d.getPlanId(), null));
            if (descriptionTemplateElasticEntityMap != null) m.setDescriptionTemplate(descriptionTemplateElasticEntityMap.getOrDefault(d.getPlanId(), null));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, List<NestedReferenceElasticEntity>> collectDescriptionReferences(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty()) return null;
        this.logger.debug("checking related - {}", DescriptionReference.class.getSimpleName());

        DescriptionReferenceQuery associationQuery = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().descriptionIds(data.stream().map(DescriptionEntity::getId).collect(Collectors.toList())).isActive(IsActive.Active);
        List<DescriptionReferenceEntity> associationEntities = associationQuery.collect();
        
        ReferenceQuery query = this.queryFactory.query(ReferenceQuery.class).isActive(IsActive.Active).disableTracking().ids(associationEntities.stream().map(DescriptionReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()));
        Map<UUID, NestedReferenceElasticEntity> itemMapById = this.builderFactory.builder(NestedReferenceElasticBuilder.class).asForeignKey(query, NestedReferenceElasticEntity::getId);

        Map<UUID, List<NestedReferenceElasticEntity>> itemMap = new HashMap<>();
        for (DescriptionReferenceEntity associationEntity : associationEntities){
            if (!itemMap.containsKey(associationEntity.getDescriptionId())) itemMap.put(associationEntity.getDescriptionId(), new ArrayList<>());
            NestedReferenceElasticEntity item = itemMapById.getOrDefault(associationEntity.getReferenceId(), null);
            if (item != null) itemMap.get(associationEntity.getDescriptionId()).add(item);
        }

        return itemMap;
    }

    private Map<UUID, List<NestedTagElasticEntity>> collectDescriptionTags(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty()) return null;
        this.logger.debug("checking related - {}", DescriptionTag.class.getSimpleName());

        DescriptionTagQuery associationQuery = this.queryFactory.query(DescriptionTagQuery.class).disableTracking().descriptionIds(data.stream().map(DescriptionEntity::getId).collect(Collectors.toList())).isActive(IsActive.Active);
        List<DescriptionTagEntity> associationEntities = associationQuery.collect();

        TagQuery query = this.queryFactory.query(TagQuery.class).disableTracking().isActive(IsActive.Active).ids(associationEntities.stream().map(DescriptionTagEntity::getTagId).distinct().collect(Collectors.toList()));
        Map<UUID, NestedTagElasticEntity> itemMapById = this.builderFactory.builder(NestedTagElasticBuilder.class).asForeignKey(query, NestedTagElasticEntity::getId);

        Map<UUID, List<NestedTagElasticEntity>> itemMap = new HashMap<>();
        for (DescriptionTagEntity associationEntity : associationEntities){
            if (!itemMap.containsKey(associationEntity.getDescriptionId())) itemMap.put(associationEntity.getDescriptionId(), new ArrayList<>());
            NestedTagElasticEntity item = itemMapById.getOrDefault(associationEntity.getTagId(), null);
            if (item != null) itemMap.get(associationEntity.getDescriptionId()).add(item);
        }

        return itemMap;
    }

    private Map<UUID, NestedPlanElasticEntity> collectPlans(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Plan.class.getSimpleName());

        Map<UUID, NestedPlanElasticEntity> itemMap;
        PlanQuery q = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).ids(data.stream().map(DescriptionEntity::getPlanId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(NestedPlanElasticBuilder.class).asForeignKey(q, NestedPlanElasticEntity::getId);

        return itemMap;
    }

    private Map<UUID, NestedDescriptionTemplateElasticEntity> collectDescriptionTemplates(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionTemplate.class.getSimpleName());

        Map<UUID, NestedDescriptionTemplateElasticEntity> itemMap;
        DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).ids(data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(NestedDescriptionTemplateElasticBuilder.class).asForeignKey(q, NestedDescriptionTemplateElasticEntity::getId);

        return itemMap;
    }
}
