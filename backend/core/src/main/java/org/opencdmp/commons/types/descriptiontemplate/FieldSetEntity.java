package org.opencdmp.commons.types.descriptiontemplate;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class FieldSetEntity {
    @XmlAttribute(name="id")
    private String id;
    @XmlAttribute(name="ordinal")
    private int ordinal;

    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    private List<FieldEntity> fields;
    @XmlAttribute(name="title")
    private String title;
    @XmlAttribute(name="description")
    private String description;
    @XmlAttribute(name="extendedDescription")
    private String extendedDescription;
    @XmlAttribute(name="additionalInformation")
    private String additionalInformation;
    @XmlElement(name="multiplicity")
    private MultiplicityEntity multiplicity;
    @XmlAttribute(name="hasMultiplicity")
    private boolean hasMultiplicity;
    @XmlAttribute(name="hasCommentField")
    private boolean hasCommentField;

    public List<FieldEntity> getFields() {
        return this.fields;
    }
    public void setFields(List<FieldEntity> fieldEntities) {
        this.fields = fieldEntities;
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return this.ordinal;
    }
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtendedDescription() {
        return this.extendedDescription;
    }
    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public MultiplicityEntity getMultiplicity() {
        return this.multiplicity;
    }
    public void setMultiplicity(MultiplicityEntity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public boolean getHasCommentField() {
        return this.hasCommentField;
    }
    public void setHasCommentField(boolean hasCommentField) {
        this.hasCommentField = hasCommentField;
    }

    public String getAdditionalInformation() {
        return this.additionalInformation;
    }
    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }


    public List<FieldEntity> getAllField() {
        return this.getFields() == null ? new ArrayList<>() : this.getFields();
    }

    public List<FieldEntity> getFieldById(String id) {
        return this.getAllField().stream().filter(x-> id.equals(x.getId())).toList();
    }

    public boolean getHasMultiplicity() {
        return this.hasMultiplicity;
    }

    public void setHasMultiplicity(boolean hasMultiplicity) {
        this.hasMultiplicity = hasMultiplicity;
    }
}
