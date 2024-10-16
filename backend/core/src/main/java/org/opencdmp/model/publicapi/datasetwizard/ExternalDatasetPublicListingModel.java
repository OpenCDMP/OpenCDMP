package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.reference.Field;
import org.opencdmp.service.publicapi.PublicApiProperties;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class ExternalDatasetPublicListingModel {
    private UUID id;
    private String name;
    private String abbreviation;
    private String reference;
    private Date created;
    private Date modified;
    private String info;
    private ExternalDatasetType type;
    private String pid;
    private String uri;
    private String tag; // Api fetching the data
    private String source; // Actual harvested source

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
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

    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }
    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Integer getType() {
        return type != null ? type.getValue() : null;
    }
    public void setType(Integer type) {
        this.type = ExternalDatasetType.fromInteger(type);
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public String getPid() {
        return pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public static ExternalDatasetPublicListingModel fromDescriptionReference(DescriptionReference descriptionReference, PublicApiProperties.ReferenceTypeMapConfig config) {
        if (!Objects.equals(descriptionReference.getReference().getType().getId(), config.getDatasetTypeId()))
            return null;
        ExternalDatasetPublicListingModel model = new ExternalDatasetPublicListingModel();
        Reference reference = descriptionReference.getReference();
        model.setId(reference.getId());
        model.setName(reference.getLabel());
        model.setAbbreviation(reference.getAbbreviation());
        model.setReference(reference.getReference());
        model.setSource(reference.getSource());
        model.setPid(reference.getReference());
        if (reference.getDefinition() == null) return model;
        Field uri = reference.getDefinition().getFields().stream().filter(x -> x.getCode().equals("uri")).toList().get(0);
        if (uri != null) model.setUri(uri.getValue());

        return model;
    }
}
