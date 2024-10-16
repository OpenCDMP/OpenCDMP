package org.opencdmp.integrationevent.outbox.tenantremoval;

import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.integrationevent.inbox.ConsistencyHandler;
import org.opencdmp.query.TenantQuery;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantRemovalConsistencyHandler implements ConsistencyHandler<TenantRemovalConsistencyPredicates> {

    private final QueryFactory queryFactory;

    public TenantRemovalConsistencyHandler(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Boolean isConsistent(TenantRemovalConsistencyPredicates consistencyPredicates) {
        long count = this.queryFactory.query(TenantQuery.class).disableTracking().ids(consistencyPredicates.getTenantId()).count();
        return count > 0;
    }

}
