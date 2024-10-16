package org.opencdmp.model;

import org.opencdmp.commons.enums.PlanValidationOutput;

import java.util.List;
import java.util.UUID;

public class PlanValidationResult {

    private UUID id;

    public static final String _id = "id";

    private PlanValidationOutput result;

    private List<String> errors;

    public static final String _result = "result";

    public PlanValidationResult() {
    }

    public PlanValidationResult(UUID id, PlanValidationOutput result) {
        this.id = id;
        this.result = result;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PlanValidationOutput getResult() {
        return result;
    }

    public void setResult(PlanValidationOutput result) {
        this.result = result;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
