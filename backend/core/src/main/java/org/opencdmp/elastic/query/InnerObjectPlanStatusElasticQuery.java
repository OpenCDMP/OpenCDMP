package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticInnerObjectQuery;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.elastic.data.nested.NestedPlanStatusElasticEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InnerObjectPlanStatusElasticQuery  extends ElasticInnerObjectQuery<InnerObjectPlanStatusElasticQuery, NestedPlanStatusElasticEntity, UUID> {

    private Collection<UUID> ids;
    private Collection<UUID> excludedIds;
    private Collection<PlanStatus> internalStatus;
    private String innerPath;

    public InnerObjectPlanStatusElasticQuery(ElasticsearchTemplate elasticsearchRestTemplate, ElasticProperties elasticProperties) {
        super(elasticsearchRestTemplate, elasticProperties);
    }

    @Override
    protected String getInnerPath() {
        return this.innerPath;
    }

    public InnerObjectPlanStatusElasticQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public InnerObjectPlanStatusElasticQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanStatusElasticQuery ids(Collection<UUID> value) {
        this.ids = value;
        return this;
    }

    public InnerObjectPlanStatusElasticQuery internalStatuses(PlanStatus value) {
        this.internalStatus = List.of(value);
        return this;
    }

    public InnerObjectPlanStatusElasticQuery internalStatuses(PlanStatus... value) {
        this.internalStatus = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanStatusElasticQuery internalStatuses(Collection<PlanStatus> value) {
        this.internalStatus = value;
        return this;
    }

    public InnerObjectPlanStatusElasticQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public InnerObjectPlanStatusElasticQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public InnerObjectPlanStatusElasticQuery excludedIds(Collection<UUID> value) {
        this.excludedIds = value;
        return this;
    }


    @Override
    public InnerObjectPlanStatusElasticQuery innerPath(String value) {
        this.innerPath = value;
        return this;
    }

    @Override
    protected Boolean isFalseQuery() {
        return false;
    }

    @Override
    protected ElasticField fieldNameOf(FieldResolver item) {
        if (item.match(NestedPlanStatusElasticEntity._id)) return this.elasticFieldOf(NestedPlanStatusElasticEntity._id);
        else if (item.match(NestedPlanStatusElasticEntity._name)) return this.elasticFieldOf(NestedPlanStatusElasticEntity._name);
        else if (item.match(NestedPlanStatusElasticEntity._internalStatus)) return this.elasticFieldOf(NestedPlanStatusElasticEntity._internalStatus);
        else return null;
    }

    @Override
    protected Class<NestedPlanStatusElasticEntity> entityClass() {
        return NestedPlanStatusElasticEntity.class;
    }

    @Override
    protected UUID toKey(String key) {
        return  UUID.fromString(key);
    }

    @Override
    protected ElasticField getKeyField() {
        return this.elasticFieldOf(NestedPlanStatusElasticEntity._id);
    }

    @Override
    protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
        return null;
    }

    @Override
    protected Query applyFilters() {
        List<Query> predicates= new ArrayList<>();
        if (this.ids != null) {
            predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanStatusElasticEntity._id).disableInfer(true), this.ids)._toQuery());
        }
        if (this.excludedIds != null) {
            predicates.add(this.not(this.containsUUID(this.elasticFieldOf(NestedPlanStatusElasticEntity._id).disableInfer(true), this.ids)._toQuery())._toQuery());
        }
        if (this.internalStatus != null) {
                predicates.add(this.or(this.not(this.exists(this.elasticFieldOf(NestedPlanStatusElasticEntity._internalStatus).disableInfer(true))._toQuery())._toQuery(),
                        this.contains(this.elasticFieldOf(NestedPlanStatusElasticEntity._internalStatus).disableInfer(true), this.internalStatus.stream().map(PlanStatus::getValue).toList().toArray(new Short[this.internalStatus.size()]))._toQuery()
                        )._toQuery());
        }
        if (!predicates.isEmpty()) {
            return this.and(predicates);
        } else {
            return null;
        }
    }
    protected ExistsQuery exists(ElasticField field, Short... values) {
        return QueryBuilders.exists().field(field.getFieldNameWithPath()).build();

    }

    @Override
    public NestedPlanStatusElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
        NestedPlanStatusElasticEntity mocDoc = new NestedPlanStatusElasticEntity();
        if (columns.contains(NestedPlanStatusElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanStatusElasticEntity._id), UUID.class));
        if (columns.contains(NestedPlanStatusElasticEntity._name)) mocDoc.setName(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanStatusElasticEntity._name), String.class));
        if (columns.contains(NestedPlanStatusElasticEntity._internalStatus)) mocDoc.setInternalStatus(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanStatusElasticEntity._internalStatus), PlanStatus.class));
        return mocDoc;
    }


}


