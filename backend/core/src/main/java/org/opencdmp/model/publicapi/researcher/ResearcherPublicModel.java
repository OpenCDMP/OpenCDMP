package org.opencdmp.model.publicapi.researcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.service.publicapi.PublicApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResearcherPublicModel {
    private static final Logger logger = LoggerFactory.getLogger(ResearcherPublicModel.class);
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

    public static ResearcherPublicModel fromDmpReference(PlanReference planReference, PublicApiProperties.ReferenceTypeMapConfig config) {
        if (!Objects.equals(planReference.getReference().getType().getId(), config.getRegistryTypeId()))
            return null;
        ResearcherPublicModel model = new ResearcherPublicModel();
        Reference reference = planReference.getReference();
        model.setId(reference.getId().toString());
        model.setReference(reference.getReference());
        model.setLabel(reference.getLabel());
        model.setName(reference.getLabel());
        String[] refParts = planReference.getReference().getReference().split(":");
        String source = refParts[0];
        if (source.equals("dmp"))
            model.setKey("Internal");
        else
            model.setKey(source);

        return model;
    }
}
