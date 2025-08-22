package org.opencdmp.model;


import java.util.UUID;

public class PublicPlanBlueprint {

    private UUID id;

    public static final String _id = "id";

    private String label;

    public static final String _label = "label";

    private PublicPlanBlueprintDefinition definition;

    public static final String _definition = "definition";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PublicPlanBlueprintDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(PublicPlanBlueprintDefinition definition) {
        this.definition = definition;
    }
}
