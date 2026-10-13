package org.opencdmp.service.report;

import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * One row of a report before aggregation: the answers of a single asset or plan, together
 * with the structural properties any dimension or export column may need. Plan fields are
 * the subject's own when the subject is a plan.
 */
record ReportSubject(
        UUID id,
        String label,
        UUID tenantId,
        UUID statusId,
        UUID sourceVersionId,
        PropertyDefinitionEntity properties,
        /** True when the answers are still in the pre-1.4 shape and cannot be read. */
        boolean legacyProperties,
        UUID planId,
        String planLabel,
        Short planVersion,
        PlanVersionStatus planVersionStatus,
        UUID planStatusId,
        PlanAccessType planAccessType,
        UUID planBlueprintId,
        Instant createdAt,
        Instant finalizedAt) {

    /** The same subject once its answers have been read: the report only needs the rest. */
    ReportSubject withoutAnswers() {
        return new ReportSubject(this.id, this.label, this.tenantId, this.statusId, this.sourceVersionId, null, this.legacyProperties,
                this.planId, this.planLabel, this.planVersion, this.planVersionStatus, this.planStatusId, this.planAccessType,
                this.planBlueprintId, this.createdAt, this.finalizedAt);
    }
}
