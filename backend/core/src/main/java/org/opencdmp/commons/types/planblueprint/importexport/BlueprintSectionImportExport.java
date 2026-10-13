package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateSectionImportExport;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintSectionImportExport extends DescriptionTemplateSectionImportExport {

    @XmlAttribute(name = "hasTemplates")
    private boolean hasTemplates;
    @XmlElementWrapper(name = "descriptionTemplates")
    @XmlElement(name = "descriptionTemplate")
    private List<BlueprintDescriptionTemplateImportExport> descriptionTemplates;
    @XmlAttribute(name = "prefillingSourcesEnabled")
    private Boolean prefillingSourcesEnabled;
    @XmlAttribute(name = "canEditDescriptionTemplates")
    private Boolean canEditDescriptionTemplates;
    @XmlElementWrapper(name = "prefillingSources")
    @XmlElement(name = "prefillingSource")
    private List<BlueprintPrefillingSourceImportExport> prefillingSources;

    public boolean isHasTemplates() { return this.hasTemplates; }
    public void setHasTemplates(boolean hasTemplates) { this.hasTemplates = hasTemplates; }

    public List<BlueprintDescriptionTemplateImportExport> getDescriptionTemplates() { return this.descriptionTemplates; }
    public void setDescriptionTemplates(List<BlueprintDescriptionTemplateImportExport> descriptionTemplates) { this.descriptionTemplates = descriptionTemplates; }

    public Boolean getPrefillingSourcesEnabled() { return this.prefillingSourcesEnabled; }
    public void setPrefillingSourcesEnabled(Boolean prefillingSourcesEnabled) { this.prefillingSourcesEnabled = prefillingSourcesEnabled; }

    public Boolean getCanEditDescriptionTemplates() { return canEditDescriptionTemplates; }
    public void setCanEditDescriptionTemplates(Boolean canEditDescriptionTemplates) { this.canEditDescriptionTemplates = canEditDescriptionTemplates; }

    public List<BlueprintPrefillingSourceImportExport> getPrefillingSources() { return this.prefillingSources; }
    public void setPrefillingSources(List<BlueprintPrefillingSourceImportExport> prefillingSources) { this.prefillingSources = prefillingSources; }
}
