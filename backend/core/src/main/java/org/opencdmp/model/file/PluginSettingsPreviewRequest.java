package org.opencdmp.model.file;

import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationFieldPersist;

import java.util.List;
import java.util.UUID;

/**
 * Renders a plan (or one of its assets, when {@code descriptionId} is set) as a PDF with a file transformer's
 * settings as they are being edited, instead of the tenant's saved ones. Blueprint and asset template overrides
 * still apply on top, as in a real export.
 */
public class PluginSettingsPreviewRequest {

    private String repositoryId;
    private UUID planId;
    private UUID descriptionId;
    /** The tenant-level settings of this plugin, as edited. */
    private List<PluginConfigurationFieldPersist> fields;

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public UUID getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(UUID descriptionId) {
        this.descriptionId = descriptionId;
    }

    public List<PluginConfigurationFieldPersist> getFields() {
        return fields;
    }

    public void setFields(List<PluginConfigurationFieldPersist> fields) {
        this.fields = fields;
    }
}
