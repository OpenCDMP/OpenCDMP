package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class SectionEntity {

    @XmlAttribute(name="id")
    private UUID id;

    @XmlAttribute(name="label")
    private String label;

    @XmlAttribute(name="description")
    private String description;

    @XmlAttribute(name="ordinal")
    private Integer ordinal;

    @XmlElementWrapper(name = "fields")
    @XmlElements({
            @XmlElement(name = PlanBlueprintFieldCategory.Names.Extra, type = ExtraFieldEntity.class),
            @XmlElement(name = PlanBlueprintFieldCategory.Names.System, type = SystemFieldEntity.class),
            @XmlElement(name = PlanBlueprintFieldCategory.Names.ReferenceType, type = ReferenceTypeFieldEntity.class),
            @XmlElement(name = PlanBlueprintFieldCategory.Names.Upload, type = UploadFieldEntity.class),
    })
    private List<FieldEntity> fields;
    
    @XmlAttribute(name="hasTemplates")
    private Boolean hasTemplates;

    @XmlAttribute(name = "prefillingSourcesEnabled")
    private Boolean prefillingSourcesEnabled;

    @XmlAttribute(name = "canEditDescriptionTemplates")
    private Boolean canEditDescriptionTemplates;

    @XmlElementWrapper(name = "descriptionTemplates")
    @XmlElement(name = "descriptionTemplate")
    private List<BlueprintDescriptionTemplateEntity> descriptionTemplates;

    @XmlElementWrapper(name = "prefillingSourcesIds")
    @XmlElement(name = "id")
    private List<UUID> prefillingSourcesIds;

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

    public Integer getOrdinal() {
        return this.ordinal;
    }
    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public List<FieldEntity> getFields() {
        return this.fields;
    }
    public void setFields(List<FieldEntity> fields) {
        this.fields = fields;
    }

    public Boolean getHasTemplates() {
        return this.hasTemplates;
    }
    public void setHasTemplates(Boolean hasTemplates) {
        this.hasTemplates = hasTemplates;
    }

    public List<BlueprintDescriptionTemplateEntity> getDescriptionTemplates() {
        return this.descriptionTemplates;
    }
    public void setDescriptionTemplates(List<BlueprintDescriptionTemplateEntity> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public Boolean getPrefillingSourcesEnabled() {
        return this.prefillingSourcesEnabled;
    }

    public void setPrefillingSourcesEnabled(Boolean prefillingSourcesEnabled) {
        this.prefillingSourcesEnabled = prefillingSourcesEnabled;
    }

    public Boolean getCanEditDescriptionTemplates() {
        return canEditDescriptionTemplates;
    }

    public void setCanEditDescriptionTemplates(Boolean canEditDescriptionTemplates) {
        this.canEditDescriptionTemplates = canEditDescriptionTemplates;
    }

    public List<UUID> getPrefillingSourcesIds() {
        return this.prefillingSourcesIds;
    }

    public void setPrefillingSourcesIds(List<UUID> prefillingSourcesIds) {
        this.prefillingSourcesIds = prefillingSourcesIds;
    }

    public List<FieldEntity> getAllField(){
        return this.getFields() != null ? this.getFields() : new ArrayList<>();
    }

}
