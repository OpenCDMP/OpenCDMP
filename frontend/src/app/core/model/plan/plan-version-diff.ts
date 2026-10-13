import { Guid } from '@common/types/guid';
import { DescriptionChangeKind } from '@app/core/common/enum/description-change-kind.enum';
import { FieldChange } from '@app/core/model/plan-update-request/plan-update-preview';

/**
 * Field-level differences between two versions of the same plan group,
 * as returned by GET plan/{id}/version-diff/{otherId}. "from" is always
 * the older version and "to" the newer one, regardless of argument order.
 */
export interface PlanVersionDiff {
	groupId: Guid;
	fromPlanId: Guid;
	toPlanId: Guid;
	fromVersion: number;
	toVersion: number;
	blueprintChanged?: boolean;
	planFieldChanges?: FieldChange[];
	descriptionChanges?: DescriptionVersionDiff[];
}

export interface DescriptionVersionDiff {
	kind: DescriptionChangeKind;
	fromDescriptionId?: Guid;
	toDescriptionId?: Guid;
	fromLabel?: string;
	toLabel?: string;
	sectionId?: string;
	fromTemplateId?: Guid;
	toTemplateId?: Guid;
	templateChanged?: boolean;
	fieldChanges?: FieldChange[];
}
