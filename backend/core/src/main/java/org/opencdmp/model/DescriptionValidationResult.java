package org.opencdmp.model;

import org.opencdmp.commons.enums.DescriptionValidationOutput;

import java.util.UUID;

public class DescriptionValidationResult {

    private UUID descriptionId;

    public static final String _id = "id";

    private DescriptionValidationOutput result;

    public static final String _result = "result";

    public DescriptionValidationResult() {
    }

    public DescriptionValidationResult(UUID descriptionId, DescriptionValidationOutput result) {
        this.descriptionId = descriptionId;
        this.result = result;
    }

    public UUID getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(UUID descriptionId) {
        this.descriptionId = descriptionId;
    }

    public DescriptionValidationOutput getResult() {
        return result;
    }

    public void setResult(DescriptionValidationOutput result) {
        this.result = result;
    }
}
