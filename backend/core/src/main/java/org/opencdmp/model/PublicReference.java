package org.opencdmp.model;

import org.opencdmp.commons.enums.ReferenceSourceType;

import java.util.UUID;

public class PublicReference {

    private UUID id;
    public static final String _id = "id";

    private String label;
    public static final String _label = "label";

    private PublicReferenceType type;
    public static final String _type = "type";

    private String description;
    public static final String _description = "description";

    private String reference;
    public static final String _reference = "reference";

    private String source;
    public static final String _source = "source";

    private ReferenceSourceType sourceType;
    public static final String _sourceType = "sourceType";


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

    public PublicReferenceType getType() {
        return type;
    }

    public void setType(PublicReferenceType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ReferenceSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ReferenceSourceType sourceType) {
        this.sourceType = sourceType;
    }
}


