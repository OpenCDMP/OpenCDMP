package org.opencdmp.service.accounting;

import org.opencdmp.model.accounting.AccountingAggregateResults;
import org.opencdmp.query.lookup.accounting.AccountingInfoLookup;

public interface AccountingClient {

    AccountingAggregateResults calculate(AccountingInfoLookup lookup) throws Exception;

}
