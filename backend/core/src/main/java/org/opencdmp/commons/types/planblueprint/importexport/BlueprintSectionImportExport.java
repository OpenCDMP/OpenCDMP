package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintSectionImportExport {

    @XmlElement(name = "id")
    private UUID id;
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "ordinal")
    private int ordinal;
    @XmlElementWrapper(name = "systemFields")
    @XmlElement(name = "systemField")
    private List<BlueprintSystemFieldImportExport> systemFields;
    @XmlElementWrapper(name = "extraFields")
    @XmlElement(name = "extraField")
    private List<BlueprintExtraFieldImportExport> extraFields;
    @XmlElementWrapper(name = "referenceFields")
    @XmlElement(name = "referenceField")
    private List<BlueprintReferenceTypeFieldImportExport> referenceFields;
    @XmlAttribute(name = "hasTemplates")
    private boolean hasTemplates;
    @XmlElementWrapper(name = "descriptionTemplates")
    @XmlElement(name = "descriptionTemplate")
    private List<BlueprintDescriptionTemplateImportExport> descriptionTemplates;

    @XmlAttribute(name = "prefillingSourcesEnabled")
    private Boolean prefillingSourcesEnabled;

    @XmlElementWrapper(name = "prefillingSources")
    @XmlElement(name = "prefillingSource")
    private List<BlueprintPrefillingSourceImportExport> prefillingSources;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public List<BlueprintSystemFieldImportExport> getSystemFields() {
        return this.systemFields;
    }

    public void setSystemFields(List<BlueprintSystemFieldImportExport> systemFields) {
        this.systemFields = systemFields;
    }

    public List<BlueprintExtraFieldImportExport> getExtraFields() {
        return this.extraFields;
    }

    public void setExtraFields(List<BlueprintExtraFieldImportExport> extraFields) {
        this.extraFields = extraFields;
    }

    public List<BlueprintReferenceTypeFieldImportExport> getReferenceFields() {
        return this.referenceFields;
    }

    public void setReferenceFields(List<BlueprintReferenceTypeFieldImportExport> referenceFields) {
        this.referenceFields = referenceFields;
    }

    public boolean isHasTemplates() {
        return this.hasTemplates;
    }

    public void setHasTemplates(boolean hasTemplates) {
        this.hasTemplates = hasTemplates;
    }

    public List<BlueprintDescriptionTemplateImportExport> getDescriptionTemplates() {
        return this.descriptionTemplates;
    }

    public void setDescriptionTemplates(List<BlueprintDescriptionTemplateImportExport> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public Boolean getPrefillingSourcesEnabled() {
        return this.prefillingSourcesEnabled;
    }

    public void setPrefillingSourcesEnabled(Boolean prefillingSourcesEnabled) {
        this.prefillingSourcesEnabled = prefillingSourcesEnabled;
    }

    public List<BlueprintPrefillingSourceImportExport> getPrefillingSources() {
        return this.prefillingSources;
    }

    public void setPrefillingSources(List<BlueprintPrefillingSourceImportExport> prefillingSources) {
        this.prefillingSources = prefillingSources;
    }
}
