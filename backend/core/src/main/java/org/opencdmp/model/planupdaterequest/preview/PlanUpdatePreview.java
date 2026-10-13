package org.opencdmp.model.planupdaterequest.preview;

import java.util.List;
import java.util.UUID;

public class PlanUpdatePreview {

    private UUID planUpdateRequestId;
    public static final String _planUpdateRequestId = "planUpdateRequestId";

    private UUID planId;
    public static final String _planId = "planId";

    private String planVersion;
    public static final String _planVersion = "planVersion";

    private PreviewKind kind;
    public static final String _kind = "kind";

    private List<FieldChange> planFieldChanges;
    public static final String _planFieldChanges = "planFieldChanges";

    private List<DescriptionChange> descriptionChanges;
    public static final String _descriptionChanges = "descriptionChanges";

    public UUID getPlanUpdateRequestId() {
        return planUpdateRequestId;
    }

    public void setPlanUpdateRequestId(UUID planUpdateRequestId) {
        this.planUpdateRequestId = planUpdateRequestId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(String planVersion) {
        this.planVersion = planVersion;
    }

    public PreviewKind getKind() {
        return kind;
    }

    public void setKind(PreviewKind kind) {
        this.kind = kind;
    }

    public List<FieldChange> getPlanFieldChanges() {
        return planFieldChanges;
    }

    public void setPlanFieldChanges(List<FieldChange> planFieldChanges) {
        this.planFieldChanges = planFieldChanges;
    }

    public List<DescriptionChange> getDescriptionChanges() {
        return descriptionChanges;
    }

    public void setDescriptionChanges(List<DescriptionChange> descriptionChanges) {
        this.descriptionChanges = descriptionChanges;
    }
}
