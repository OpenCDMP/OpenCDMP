package org.opencdmp.integrationevent.outbox.userremoval;

import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.integrationevent.inbox.ConsistencyHandler;
import org.opencdmp.query.UserQuery;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("outboxuserremovalconsistencyhandler")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserRemovalConsistencyHandler implements ConsistencyHandler<UserRemovalConsistencyPredicates> {

	private final QueryFactory queryFactory;

	public UserRemovalConsistencyHandler(QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public Boolean isConsistent(UserRemovalConsistencyPredicates consistencyPredicates) {
		long count = this.queryFactory.query(UserQuery.class).disableTracking().ids(consistencyPredicates.getUserId()).count();
        return count != 0;
    }
}
