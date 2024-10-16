package org.opencdmp.controllers.publicapi.response;

import java.util.List;

public class DataTableData<T> {
    private Long totalCount;
    private List<T> data;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
