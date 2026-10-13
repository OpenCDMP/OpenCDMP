package org.opencdmp.model.plan.versiondiff;

import org.opencdmp.model.planupdaterequest.preview.DescriptionChangeKind;
import org.opencdmp.model.planupdaterequest.preview.FieldChange;

import java.util.List;
import java.util.UUID;

public class DescriptionVersionDiff {

    private DescriptionChangeKind kind;
    public static final String _kind = "kind";

    private UUID fromDescriptionId;
    public static final String _fromDescriptionId = "fromDescriptionId";

    private UUID toDescriptionId;
    public static final String _toDescriptionId = "toDescriptionId";

    private String fromLabel;
    public static final String _fromLabel = "fromLabel";

    private String toLabel;
    public static final String _toLabel = "toLabel";

    private String sectionId;
    public static final String _sectionId = "sectionId";

    private UUID fromTemplateId;
    public static final String _fromTemplateId = "fromTemplateId";

    private UUID toTemplateId;
    public static final String _toTemplateId = "toTemplateId";

    private Boolean templateChanged;
    public static final String _templateChanged = "templateChanged";

    private List<FieldChange> fieldChanges;
    public static final String _fieldChanges = "fieldChanges";

    public DescriptionChangeKind getKind() {
        return kind;
    }

    public void setKind(DescriptionChangeKind kind) {
        this.kind = kind;
    }

    public UUID getFromDescriptionId() {
        return fromDescriptionId;
    }

    public void setFromDescriptionId(UUID fromDescriptionId) {
        this.fromDescriptionId = fromDescriptionId;
    }

    public UUID getToDescriptionId() {
        return toDescriptionId;
    }

    public void setToDescriptionId(UUID toDescriptionId) {
        this.toDescriptionId = toDescriptionId;
    }

    public String getFromLabel() {
        return fromLabel;
    }

    public void setFromLabel(String fromLabel) {
        this.fromLabel = fromLabel;
    }

    public String getToLabel() {
        return toLabel;
    }

    public void setToLabel(String toLabel) {
        this.toLabel = toLabel;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public UUID getFromTemplateId() {
        return fromTemplateId;
    }

    public void setFromTemplateId(UUID fromTemplateId) {
        this.fromTemplateId = fromTemplateId;
    }

    public UUID getToTemplateId() {
        return toTemplateId;
    }

    public void setToTemplateId(UUID toTemplateId) {
        this.toTemplateId = toTemplateId;
    }

    public Boolean getTemplateChanged() {
        return templateChanged;
    }

    public void setTemplateChanged(Boolean templateChanged) {
        this.templateChanged = templateChanged;
    }

    public List<FieldChange> getFieldChanges() {
        return fieldChanges;
    }

    public void setFieldChanges(List<FieldChange> fieldChanges) {
        this.fieldChanges = fieldChanges;
    }
}
