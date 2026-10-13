package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;

import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class RootSectionEntity extends SectionEntity {

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


    public Boolean getHasTemplates() {
        return hasTemplates;
    }

    public void setHasTemplates(Boolean hasTemplates) {
        this.hasTemplates = hasTemplates;
    }

    public Boolean getPrefillingSourcesEnabled() {
        return prefillingSourcesEnabled;
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

    public List<BlueprintDescriptionTemplateEntity> getDescriptionTemplates() {
        return descriptionTemplates;
    }

    public void setDescriptionTemplates(List<BlueprintDescriptionTemplateEntity> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public List<UUID> getPrefillingSourcesIds() {
        return prefillingSourcesIds;
    }

    public void setPrefillingSourcesIds(List<UUID> prefillingSourcesIds) {
        this.prefillingSourcesIds = prefillingSourcesIds;
    }

}
