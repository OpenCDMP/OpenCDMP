package org.opencdmp.model;


import java.util.UUID;

public class PublicDescriptionStatus {

    public final static String _id = "id";
    private UUID id;

    public final static String _name = "name";
    private String name;

    public final static String _internalStatus = "internalStatus";
    private org.opencdmp.commons.enums.DescriptionStatus internalStatus;

    public final static String _definition = "definition";
    private PublicDescriptionStatusDefinition definition;

    public UUID getId() { return this.id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public org.opencdmp.commons.enums.DescriptionStatus getInternalStatus() { return this.internalStatus; }
    public void setInternalStatus(org.opencdmp.commons.enums.DescriptionStatus internalStatus) { this.internalStatus = internalStatus; }

    public PublicDescriptionStatusDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(PublicDescriptionStatusDefinition definition) {
        this.definition = definition;
    }
}
