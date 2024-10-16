package org.opencdmp.controllers.publicapi.criteria;

import io.swagger.annotations.ApiModelProperty;

public abstract class Criteria<T> {
    @ApiModelProperty(value = "like", name = "like", dataType = "String", allowEmptyValue = true, example = "\"\"")
    private String like;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }


}
