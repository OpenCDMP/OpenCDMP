package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticInnerObjectQuery;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.elastic.data.nested.NestedPlanBlueprintElasticEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

public class InnerObjectPlanBlueprintElasticQuery extends ElasticInnerObjectQuery<InnerObjectPlanBlueprintElasticQuery, NestedPlanBlueprintElasticEntity, UUID> {
    private Collection<UUID> groupIds;
    private Collection<UUID> ids;
    private Collection<UUID> excludedIds;
    private Collection<PlanBlueprintVersionStatus> versionStatuses;
    private Collection<UUID> excludedGroupIds;
    private Collection<UUID> typeIds;
    private String innerPath;

    @Override
    public InnerObjectPlanBlueprintElasticQuery innerPath(String value) {
        this.innerPath = value;
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery groupIds(UUID value) {
        this.groupIds = List.of(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery groupIds(UUID... value) {
        this.groupIds = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery groupIds(Collection<UUID> values) {
        this.groupIds = values;
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery versionStatuses(PlanBlueprintVersionStatus value) {
        this.versionStatuses = List.of(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery versionStatuses(PlanBlueprintVersionStatus... value) {
        this.versionStatuses = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery versionStatuses(Collection<PlanBlueprintVersionStatus> values) {
        this.versionStatuses = values;
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery excludedGroupIds(UUID value) {
        this.excludedGroupIds = List.of(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery excludedGroupIds(UUID... value) {
        this.excludedGroupIds = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery excludedGroupIds(Collection<UUID> values) {
        this.excludedGroupIds = values;
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery typeIds(UUID value) {
        this.typeIds = List.of(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery typeIds(UUID... value) {
        this.typeIds = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanBlueprintElasticQuery typeIds(Collection<UUID> values) {
        this.typeIds = values;
        return this;
    }


    public InnerObjectPlanBlueprintElasticQuery(
            ElasticsearchTemplate elasticsearchRestTemplate,
            ElasticProperties elasticProperties
    ) {
        super(elasticsearchRestTemplate, elasticProperties);
    }

    @Override
    protected Class<NestedPlanBlueprintElasticEntity> entityClass() {
        return NestedPlanBlueprintElasticEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.groupIds);
    }

    @Override
    protected Query applyAuthZ() {
        return null;
    }

    @Override
    protected Query applyFilters() {
        List<Query> predicates = new ArrayList<>();
        if (this.groupIds != null) {
            predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanBlueprintElasticEntity._groupId).disableInfer(true), this.groupIds)._toQuery());
        }
        if (this.ids != null) {
            predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanBlueprintElasticEntity._id).disableInfer(true), this.ids)._toQuery());
        }
        if (this.excludedIds != null) {
            predicates.add(this.not(this.containsUUID(this.elasticFieldOf(NestedPlanBlueprintElasticEntity._id).disableInfer(true), this.excludedIds)._toQuery())._toQuery());
        }
        if (this.versionStatuses != null) {
            predicates.add(this.contains(this.elasticFieldOf(NestedPlanBlueprintElasticEntity._versionStatus).disableInfer(true), this.versionStatuses.stream().map(PlanBlueprintVersionStatus::getValue).toList().toArray(new Short[this.versionStatuses.size()]))._toQuery());
        }
        if (this.excludedGroupIds != null) {
            predicates.add(this.not(this.containsUUID(this.elasticFieldOf(NestedPlanBlueprintElasticEntity._groupId).disableInfer(true), this.excludedGroupIds)._toQuery())._toQuery());
        }
        if (this.typeIds != null) {
            predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanBlueprintElasticEntity._typeId).disableInfer(true), this.typeIds)._toQuery());
        }

        if (!predicates.isEmpty()) {
            return this.and(predicates);
        } else {
            return null;
        }
    }

    @Override
    public NestedPlanBlueprintElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
        NestedPlanBlueprintElasticEntity mocDoc = new NestedPlanBlueprintElasticEntity();
        if (columns.contains(NestedPlanBlueprintElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanBlueprintElasticEntity._id), UUID.class));
        if (columns.contains(NestedPlanBlueprintElasticEntity._label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanBlueprintElasticEntity._label), String.class));
        if (columns.contains(NestedPlanBlueprintElasticEntity._groupId)) mocDoc.setGroupId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanBlueprintElasticEntity._groupId), UUID.class));
        if (columns.contains(NestedPlanBlueprintElasticEntity._versionStatus)) mocDoc.setVersionStatus(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanBlueprintElasticEntity._versionStatus), PlanBlueprintVersionStatus.class));
        if (columns.contains(NestedPlanBlueprintElasticEntity._typeId)) mocDoc.setTypeId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanBlueprintElasticEntity._typeId), UUID.class));
        return mocDoc;
    }

    @Override
    protected ElasticField fieldNameOf(FieldResolver item) {
        if (item.match(NestedPlanBlueprintElasticEntity._id)) return this.elasticFieldOf(NestedPlanBlueprintElasticEntity._id).disableInfer(true);
        else if (item.match(NestedPlanBlueprintElasticEntity._label)) return this.elasticFieldOf(NestedPlanBlueprintElasticEntity._label).disableInfer(true);
        else if (item.match(NestedPlanBlueprintElasticEntity._groupId)) return this.elasticFieldOf(NestedPlanBlueprintElasticEntity._groupId).disableInfer(true);
        else if (item.match(NestedPlanBlueprintElasticEntity._versionStatus)) return this.elasticFieldOf(NestedPlanBlueprintElasticEntity._versionStatus).disableInfer(true);
        else if (item.match(NestedPlanBlueprintElasticEntity._typeId)) return this.elasticFieldOf(NestedPlanBlueprintElasticEntity._typeId).disableInfer(true);
        else return null;
    }

    @Override
    protected String getInnerPath() {
        return this.innerPath;
    }

    @Override
    protected UUID toKey(String key) {
        return UUID.fromString(key);
    }

    @Override
    protected ElasticField getKeyField() {
        return this.elasticFieldOf(NestedPlanBlueprintElasticEntity._id);
    }

    @Override
    protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
        return null;
    }
}
