package org.opencdmp.model.plan.versiondiff;

import org.opencdmp.model.planupdaterequest.preview.FieldChange;

import java.util.List;
import java.util.UUID;

public class PlanVersionDiff {

    private UUID groupId;
    public static final String _groupId = "groupId";

    private UUID fromPlanId;
    public static final String _fromPlanId = "fromPlanId";

    private UUID toPlanId;
    public static final String _toPlanId = "toPlanId";

    private Short fromVersion;
    public static final String _fromVersion = "fromVersion";

    private Short toVersion;
    public static final String _toVersion = "toVersion";

    private Boolean blueprintChanged;
    public static final String _blueprintChanged = "blueprintChanged";

    private List<FieldChange> planFieldChanges;
    public static final String _planFieldChanges = "planFieldChanges";

    private List<DescriptionVersionDiff> descriptionChanges;
    public static final String _descriptionChanges = "descriptionChanges";

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getFromPlanId() {
        return fromPlanId;
    }

    public void setFromPlanId(UUID fromPlanId) {
        this.fromPlanId = fromPlanId;
    }

    public UUID getToPlanId() {
        return toPlanId;
    }

    public void setToPlanId(UUID toPlanId) {
        this.toPlanId = toPlanId;
    }

    public Short getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(Short fromVersion) {
        this.fromVersion = fromVersion;
    }

    public Short getToVersion() {
        return toVersion;
    }

    public void setToVersion(Short toVersion) {
        this.toVersion = toVersion;
    }

    public Boolean getBlueprintChanged() {
        return blueprintChanged;
    }

    public void setBlueprintChanged(Boolean blueprintChanged) {
        this.blueprintChanged = blueprintChanged;
    }

    public List<FieldChange> getPlanFieldChanges() {
        return planFieldChanges;
    }

    public void setPlanFieldChanges(List<FieldChange> planFieldChanges) {
        this.planFieldChanges = planFieldChanges;
    }

    public List<DescriptionVersionDiff> getDescriptionChanges() {
        return descriptionChanges;
    }

    public void setDescriptionChanges(List<DescriptionVersionDiff> descriptionChanges) {
        this.descriptionChanges = descriptionChanges;
    }
}
