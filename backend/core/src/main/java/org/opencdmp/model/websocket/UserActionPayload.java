package org.opencdmp.model.websocket;

import java.util.UUID;

public class UserActionPayload {

    private UUID blueprintSectionId;

    private UUID blueprintFieldId;

    private UUID descriptionId;

    private String descriptionPageId;

    private String descriptionSectionId;

    private String descriptionFieldSetId;

    public UUID getBlueprintSectionId() {
        return blueprintSectionId;
    }

    public void setBlueprintSectionId(UUID blueprintSectionId) {
        this.blueprintSectionId = blueprintSectionId;
    }

    public UUID getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(UUID descriptionId) {
        this.descriptionId = descriptionId;
    }

    public String getDescriptionSectionId() {
        return descriptionSectionId;
    }

    public void setDescriptionSectionId(String descriptionSectionId) {
        this.descriptionSectionId = descriptionSectionId;
    }

    public UUID getBlueprintFieldId() {
        return blueprintFieldId;
    }

    public void setBlueprintFieldId(UUID blueprintFieldId) {
        this.blueprintFieldId = blueprintFieldId;
    }

    public String getDescriptionPageId() {
        return descriptionPageId;
    }

    public void setDescriptionPageId(String descriptionPageId) {
        this.descriptionPageId = descriptionPageId;
    }

    public String getDescriptionFieldSetId() {
        return descriptionFieldSetId;
    }

    public void setDescriptionFieldSetId(String descriptionFieldSetId) {
        this.descriptionFieldSetId = descriptionFieldSetId;
    }
}
