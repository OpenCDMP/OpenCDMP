package org.opencdmp.elastic.elasticbuilder.nested;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.DescriptionReferenceEntity;
import org.opencdmp.data.DescriptionTagEntity;
import org.opencdmp.elastic.data.nested.NestedDescriptionElasticEntity;
import org.opencdmp.elastic.data.nested.NestedDescriptionTemplateElasticEntity;
import org.opencdmp.elastic.data.nested.NestedReferenceElasticEntity;
import org.opencdmp.elastic.data.nested.NestedTagElasticEntity;
import org.opencdmp.elastic.elasticbuilder.BaseElasticBuilder;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
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
public class NestedDescriptionElasticBuilder extends BaseElasticBuilder<NestedDescriptionElasticEntity, DescriptionEntity> {

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory; 
    
    @Autowired
    public NestedDescriptionElasticBuilder(
            ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(NestedDescriptionElasticBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
    }

    @Override
    public List<NestedDescriptionElasticEntity> build(List<DescriptionEntity> data) throws MyApplicationException {
        if (data == null)
            return new ArrayList<>();

        Map<UUID, List<NestedReferenceElasticEntity>> referenceElasticEntityMap = this.collectDescriptionReferences(data);
        Map<UUID, List<NestedTagElasticEntity>> tagElasticEntityMap = this.collectDescriptionTags(data);
        Map<UUID, NestedDescriptionTemplateElasticEntity> descriptionTemplateElasticEntityMap = this.collectDescriptionTemplates(data);
        
        List<NestedDescriptionElasticEntity> models = new ArrayList<>();
        for (DescriptionEntity d : data) {
            NestedDescriptionElasticEntity m = new NestedDescriptionElasticEntity();
            m.setId(d.getId());
            m.setPlanId(d.getPlanId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            m.setStatus(d.getStatus());
            if (d.getFinalizedAt() != null) {
                m.setFinalizedAt(Date.from(d.getFinalizedAt()));
            }
            if (referenceElasticEntityMap != null) m.setReferences(referenceElasticEntityMap.getOrDefault(d.getId(), null));
            if (tagElasticEntityMap != null) m.setTags(tagElasticEntityMap.getOrDefault(d.getId(), null));
            if (descriptionTemplateElasticEntityMap != null) m.setDescriptionTemplate(descriptionTemplateElasticEntityMap.getOrDefault(d.getDescriptionTemplateId(), null));
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
        
        ReferenceQuery query = this.queryFactory.query(ReferenceQuery.class).disableTracking().isActive(IsActive.Active).ids(associationEntities.stream().map(DescriptionReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()));
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
