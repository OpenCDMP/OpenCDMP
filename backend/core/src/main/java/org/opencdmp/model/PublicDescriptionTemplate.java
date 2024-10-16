package org.opencdmp.model;

import java.util.UUID;

public class PublicDescriptionTemplate {

    public final static String _id = "id";
    private UUID id;

    public final static String _groupId = "groupId";
    private UUID groupId;

    public final static String _label = "label";
    private String label;

    public final static String _description = "description";
    private String description;
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupId() { return this.groupId; }

    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
