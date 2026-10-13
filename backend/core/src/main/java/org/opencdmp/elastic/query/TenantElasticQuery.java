package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.query.ElasticQuery;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.elastic.data.DescriptionElasticEntity;
import org.opencdmp.model.Tenant;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.UUID;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)

public abstract class TenantElasticQuery<D,K> extends ElasticQuery<D, K> {

    private final TenantScopeFactory tenantScopeFactory;

    public TenantElasticQuery(ElasticsearchTemplate elasticsearchTemplate, ElasticProperties elasticProperties, TenantScopeFactory tenantScopeFactory) {
        super(elasticsearchTemplate, elasticProperties);
        this.tenantScopeFactory = tenantScopeFactory;
    }

    public Query applyTenant(List<Query> predicates){
        if (this.tenantScopeFactory.getInstance().isSet()){
            Query tenantQuery;
            if (this.tenantScopeFactory.getInstance().isDefaultTenant()){
                try {
                    tenantQuery = this.or(this.fieldNotExists(this.elasticFieldOf(DescriptionElasticEntity._tenantId))._toQuery(),
                            this.equals(this.elasticFieldOf(DescriptionElasticEntity._tenantId), this.tenantScopeFactory.getInstance().getTenant()))._toQuery();
                } catch (InvalidApplicationException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    tenantQuery = this.equals(this.elasticFieldOf(DescriptionElasticEntity._tenantId), this.tenantScopeFactory.getInstance().getTenant());
                } catch (InvalidApplicationException e) {
                    throw new RuntimeException(e);
                }
            }
            if (predicates == null) return tenantQuery;
            else return this.and(tenantQuery, this.or(predicates)._toQuery());
        } else {
            if (predicates != null) return this.or(predicates)._toQuery();
        }
        return null;
    }
}
