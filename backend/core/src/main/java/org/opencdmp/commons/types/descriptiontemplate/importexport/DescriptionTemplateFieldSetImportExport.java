package org.opencdmp.commons.types.descriptiontemplate.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateFieldSetImportExport {

    @XmlElement(name="id")
    private String id;
    @XmlElement(name="ordinal")
    private int ordinal;

    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    private List<DescriptionTemplateFieldImportExport> fields;
    @XmlElement(name="title")
    private String title;
    @XmlElement(name="description")
    private String description;
    @XmlElement(name="extendedDescription")
    private String extendedDescription;
    @XmlElement(name="additionalInformation")
    private String additionalInformation;
    @XmlElement(name="multiplicity")
    private DescriptionTemplateMultiplicityImportExport multiplicity;
    
    @XmlElement(name="hasMultiplicity")
    private boolean hasMultiplicity;
    @XmlElement(name="hasCommentField")
    private Boolean hasCommentField;

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

    public List<DescriptionTemplateFieldImportExport> getFields() {
        return this.fields;
    }

    public void setFields(List<DescriptionTemplateFieldImportExport> fields) {
        this.fields = fields;
    }

    public Boolean getHasCommentField() {
        return this.hasCommentField;
    }
    public void setHasCommentField(Boolean hasCommentField) {
        this.hasCommentField = hasCommentField;
    }

    public DescriptionTemplateMultiplicityImportExport getMultiplicity() {
        return this.multiplicity;
    }

    public void setMultiplicity(DescriptionTemplateMultiplicityImportExport multiplicity) {
        this.multiplicity = multiplicity;
    }

    public String getTitle() {
        return this.title;
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

    public String getAdditionalInformation() {
        return this.additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getHasMultiplicity() {
        return this.hasMultiplicity;
    }

    public void setHasMultiplicity(boolean hasMultiplicity) {
        this.hasMultiplicity = hasMultiplicity;
    }

    public List<DescriptionTemplateFieldImportExport> getAllField() {
        return this.getFields() == null ? new ArrayList<>() : this.getFields();
    }

    public List<DescriptionTemplateFieldImportExport> getFieldById(String id) {
        return this.getAllField().stream().filter(x-> id.equals(x.getId())).toList();
    }
}
