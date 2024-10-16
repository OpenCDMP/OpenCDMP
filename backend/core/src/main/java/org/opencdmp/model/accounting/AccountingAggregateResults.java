package org.opencdmp.model.accounting;

import java.util.List;

public class AccountingAggregateResults {

    private List<AccountingAggregateResultItem> items;

    private long count;

    public List<AccountingAggregateResultItem> getItems() {
        return items;
    }

    public void setItems(List<AccountingAggregateResultItem> items) {
        this.items = items;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
