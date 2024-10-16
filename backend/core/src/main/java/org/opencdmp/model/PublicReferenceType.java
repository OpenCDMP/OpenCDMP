package org.opencdmp.model;

import java.util.UUID;

public class PublicReferenceType {

    private UUID id;
    public static final String _id = "id";


    private String name;
    public static final String _name = "name";


    private String code;
    public static final String _code = "code";


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
