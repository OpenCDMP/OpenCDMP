package org.opencdmp.controllers.publicapi.query.definition.helpers;


public class Ordering {
    public enum OrderByType {
        ASC, DESC
    }

    public enum ColumnType {
        COUNT, COLUMN, JOIN_COLUMN
    }

    private String fieldName;
    private OrderByType orderByType;
    private ColumnType columnType;

    public Ordering(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public OrderByType getOrderByType() {
        return orderByType;
    }

    public void setOrderByType(OrderByType orderByType) {
        this.orderByType = orderByType;
    }

    public Ordering fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public Ordering orderByType(OrderByType orderByType) {
        this.orderByType = orderByType;
        return this;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public Ordering columnType(ColumnType columnType) {
        this.columnType = columnType;
        return this;
    }
}
