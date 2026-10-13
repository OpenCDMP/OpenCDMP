package org.opencdmp.model.planupdaterequest.preview;

import java.util.List;
import java.util.UUID;

public class DescriptionChange {

    private UUID changeId;
    public static final String _changeId = "changeId";

    private DescriptionChangeKind kind;
    public static final String _kind = "kind";

    private String datasetId;
    public static final String _datasetId = "datasetId";

    private UUID descriptionId;
    public static final String _descriptionId = "descriptionId";

    private UUID templateId;
    public static final String _templateId = "templateId";

    private List<FieldChange> fieldChanges;
    public static final String _fieldChanges = "fieldChanges";

    private List<UUID> candidateTemplateIds;
    public static final String _candidateTemplateIds = "candidateTemplateIds";

    private List<IncomingField> incomingFields;
    public static final String _incomingFields = "incomingFields";

    private String currentLabel;
    public static final String _currentLabel = "currentLabel";

    public UUID getChangeId() {
        return changeId;
    }

    public void setChangeId(UUID changeId) {
        this.changeId = changeId;
    }

    public DescriptionChangeKind getKind() {
        return kind;
    }

    public void setKind(DescriptionChangeKind kind) {
        this.kind = kind;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public UUID getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(UUID descriptionId) {
        this.descriptionId = descriptionId;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public List<FieldChange> getFieldChanges() {
        return fieldChanges;
    }

    public void setFieldChanges(List<FieldChange> fieldChanges) {
        this.fieldChanges = fieldChanges;
    }

    public List<UUID> getCandidateTemplateIds() {
        return candidateTemplateIds;
    }

    public void setCandidateTemplateIds(List<UUID> candidateTemplateIds) {
        this.candidateTemplateIds = candidateTemplateIds;
    }

    public List<IncomingField> getIncomingFields() {
        return incomingFields;
    }

    public void setIncomingFields(List<IncomingField> incomingFields) {
        this.incomingFields = incomingFields;
    }

    public String getCurrentLabel() {
        return currentLabel;
    }

    public void setCurrentLabel(String currentLabel) {
        this.currentLabel = currentLabel;
    }
}
