package org.opencdmp.service.semantic;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Semantic {

    @JsonProperty("category")
    private String category;
    @JsonProperty("name")
    private String name;

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
