package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.reference.Field;
import org.opencdmp.service.publicapi.PublicApiProperties;

import java.util.Objects;
import java.util.UUID;

public class RegistryPublicModel {
    private UUID id;
    private String label;
    private String abbreviation;
    private String reference;
    private String uri;
    private String definition;
    private String source;

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

    public String getAbbreviation() {
        return abbreviation;
    }

    public String generateLabel() {
        return getLabel();
    }
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public String getHint() {
        return null;
    }

    public static RegistryPublicModel fromDescriptionReference(DescriptionReference descriptionReference, PublicApiProperties.ReferenceTypeMapConfig config) {
        if (!Objects.equals(descriptionReference.getReference().getType().getId(), config.getRegistryTypeId()))
            return null;
        RegistryPublicModel model = new RegistryPublicModel();
        Reference reference = descriptionReference.getReference();
        model.setId(reference.getId());
        model.setLabel(reference.getLabel());
        model.setAbbreviation(reference.getAbbreviation());
        model.setReference(reference.getReference());
        model.setSource(reference.getSource());
        model.setDefinition(reference.getDefinition().toString());
        if (reference.getDefinition() == null) return model;
        Field uri = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("uri")).toList().get(0);
        if (uri != null) model.setUri(uri.getValue());

        return model;
    }
}
