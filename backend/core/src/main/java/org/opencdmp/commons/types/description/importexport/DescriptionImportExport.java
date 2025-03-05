package org.opencdmp.commons.types.description.importexport;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateImportExport;
import org.opencdmp.commons.xmladapter.InstantXmlAdapter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "description")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionImportExport {

    @XmlElement(name = "id")
    private UUID id;

    @XmlElement(name = "description")
    private String  description;

    @XmlElement(name = "label")
    private String  label;

    @XmlElement(name = "status")
    private DescriptionStatusImportExport status;

    @XmlElement(name = "finalizedAt")
    @XmlJavaTypeAdapter(InstantXmlAdapter.class)
    private Instant finalizedAt;

    @XmlElement(name = "sectionId")
    private UUID  sectionId;

    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    private List<String> tags;

    @XmlElement(name = "descriptionTemplate")
    private DescriptionTemplateImportExport descriptionTemplate;

    @XmlElementWrapper(name = "references")
    @XmlElement(name = "reference")
    private List<DescriptionReferenceImportExport> references;

    @XmlElement(name = "properties")
    private DescriptionPropertyDefinitionImportExport properties;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DescriptionStatusImportExport getStatus() {
        return status;
    }

    public void setStatus(DescriptionStatusImportExport status) {
        this.status = status;
    }

    public Instant getFinalizedAt() {
        return this.finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public UUID getSectionId() {
        return this.sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public DescriptionTemplateImportExport getDescriptionTemplate() {
        return this.descriptionTemplate;
    }

    public void setDescriptionTemplate(DescriptionTemplateImportExport descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public List<DescriptionReferenceImportExport> getReferences() {
        return this.references;
    }

    public void setReferences(List<DescriptionReferenceImportExport> references) {
        this.references = references;
    }

    public DescriptionPropertyDefinitionImportExport getProperties() {
        return this.properties;
    }

    public void setProperties(DescriptionPropertyDefinitionImportExport properties) {
        this.properties = properties;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

