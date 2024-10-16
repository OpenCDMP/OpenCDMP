package org.opencdmp.controllers.publicapi.query.definition.helpers;

import io.swagger.annotations.ApiModelProperty;

import java.util.LinkedList;
import java.util.List;


public class ColumnOrderings {

    @ApiModelProperty(value = "fields", name = "fields", dataType = "List<String>", example = "[]")
    private List<String> fields;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<Ordering> getFieldOrderings()  {
        List<Ordering> orderings = new LinkedList<>();
        for (String field : fields) {
            orderings.add(this.orderingFromString(field));
        }
        return orderings;
    }

    private Ordering orderingFromString(String field) {
        Ordering ordering = new Ordering(field);
        if (ordering.getFieldName().contains("+"))
            ordering.fieldName(ordering.getFieldName().replace("+", "")).orderByType(Ordering.OrderByType.ASC);
        else if (ordering.getFieldName().startsWith("-"))
            ordering.fieldName(ordering.getFieldName().replace("-", "")).orderByType(Ordering.OrderByType.DESC);
        if (ordering.getFieldName().contains("|count|"))
            ordering.fieldName(ordering.getFieldName().replace("|count|", "")).columnType(Ordering.ColumnType.COUNT);
        else if (ordering.getFieldName().contains("|join|"))
            ordering.fieldName(ordering.getFieldName().replace("|join|", "")).columnType(Ordering.ColumnType.JOIN_COLUMN);
        else if (ordering.getFieldName().equals("asc"))
            ordering.fieldName("label").orderByType(Ordering.OrderByType.ASC);
        else if (ordering.getFieldName().equals("desc"))
            ordering.fieldName("label").orderByType(Ordering.OrderByType.DESC);
        return ordering;
    }
}
