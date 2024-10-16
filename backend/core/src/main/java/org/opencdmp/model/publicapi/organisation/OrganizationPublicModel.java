package org.opencdmp.model.publicapi.organisation;

import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.service.publicapi.PublicApiProperties;

import java.util.Objects;

public class OrganizationPublicModel {
    private String label;
    private String name;
    private String id;
    private String reference;
    private String key;

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public static OrganizationPublicModel fromPlanReference(PlanReference planReference, PublicApiProperties.ReferenceTypeMapConfig config) {
        if (!Objects.equals(planReference.getReference().getType().getId(), config.getOrganizationTypeId()))
            return null;
        OrganizationPublicModel model = new OrganizationPublicModel();
        Reference reference = planReference.getReference();
        model.setId(reference.getId().toString());
        model.setReference(reference.getReference());
        model.setLabel(reference.getLabel());
        model.setName(reference.getLabel());
        if (reference.getReference() != null)
            model.setKey(reference.getReference().substring(0, reference.getReference().indexOf(":")));

        return model;
    }
}
