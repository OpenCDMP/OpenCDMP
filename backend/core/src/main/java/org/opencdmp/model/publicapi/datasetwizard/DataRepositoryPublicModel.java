package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.reference.Field;
import org.opencdmp.service.publicapi.PublicApiProperties;

import java.util.Objects;


public class DataRepositoryPublicModel {
    private String id;
    private String pid;
    private String name;
    private String uri;
    private String info;
    private String reference;
    private String abbreviation;
    private String tag;
    private String source;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
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

    public String generateLabel() {
        return this.getName();
    }

    public String getHint() {
        return null;
    }

    public static DataRepositoryPublicModel fromDescriptionReference(DescriptionReference descriptionReference, PublicApiProperties.ReferenceTypeMapConfig config) {
        if (!Objects.equals(descriptionReference.getReference().getType().getId(), config.getDataRepositoryTypeId()))
            return null;
        DataRepositoryPublicModel model = new DataRepositoryPublicModel();
        Reference reference = descriptionReference.getReference();
        model.setId(reference.getId().toString());
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
