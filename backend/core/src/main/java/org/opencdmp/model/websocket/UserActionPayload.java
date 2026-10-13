package org.opencdmp.model.websocket;

import java.util.UUID;

public class UserActionPayload {

    private UUID descriptionId;

    private String sectionId;

    private String fieldSetId;

    public UUID getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(UUID descriptionId) {
        this.descriptionId = descriptionId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getFieldSetId() {
        return fieldSetId;
    }

    public void setFieldSetId(String fieldSetId) {
        this.fieldSetId = fieldSetId;
    }
}
