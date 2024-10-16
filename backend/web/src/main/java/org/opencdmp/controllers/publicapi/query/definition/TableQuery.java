package org.opencdmp.controllers.publicapi.query.definition;

import org.opencdmp.controllers.publicapi.QueryableList;
import org.opencdmp.controllers.publicapi.criteria.Criteria;
import org.opencdmp.controllers.publicapi.query.definition.helpers.ColumnOrderings;
import org.opencdmp.controllers.publicapi.query.definition.helpers.SelectionFields;
import io.swagger.annotations.ApiModelProperty;


public abstract class TableQuery<C extends Criteria<T>, T, K> extends Query<C, T> implements TableCriteriaQuery<C, T> {
    private ColumnOrderings orderings;
    @ApiModelProperty(hidden = true)
    private SelectionFields selection;
    @ApiModelProperty(value = "length", name = "length", dataType = "Integer", example = "2")
    private Integer length;
    @ApiModelProperty(value = "offset", name = "offset", dataType = "Integer", example = "0")
    private Integer offset;

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public ColumnOrderings getOrderings() {
        return orderings;
    }

    public void setOrderings(ColumnOrderings orderings) {
        this.orderings = orderings;
    }

    public SelectionFields getSelection() {
        return selection;
    }

    public void setSelection(SelectionFields selection) {
        this.selection = selection;
    }

    @Override
    public QueryableList<T> collect() {
        return this.applyPaging(super.collect());
    }

    @Override
    public QueryableList<T> collect(QueryableList<T> repo) {
        return this.applyPaging(super.collect(repo));
    }
}
