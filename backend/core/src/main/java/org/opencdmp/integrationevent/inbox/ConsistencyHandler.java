package org.opencdmp.integrationevent.inbox;

public interface ConsistencyHandler<T extends ConsistencyPredicates> {
	Boolean isConsistent(T consistencyPredicates);
}
