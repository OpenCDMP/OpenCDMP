package org.opencdmp.controllers.publicapi.query.definition.helpers;


import io.swagger.annotations.ApiModelProperty;

public class SelectionFields {

    @ApiModelProperty(value = "fields", name = "fields", dataType = "String[]", example = "[]")
    private String[] fields;

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }
}
