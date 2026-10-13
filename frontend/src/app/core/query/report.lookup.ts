import { ReportDateBucket } from '@app/core/common/enum/report-date-bucket.enum';
import { ReportDimensionType } from '@app/core/common/enum/report-dimension-type.enum';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { Guid } from '@common/types/guid';
import { DescriptionLookup } from './description.lookup';
import { PlanLookup } from './plan.lookup';

/**
 * An answer-report request. The structural filters are the listing lookups themselves, so
 * anything the asset or plan listing can filter by is available here; their paging and
 * ordering are ignored, because a report always reads the whole matching set.
 */
export class ReportLookup {
	subject: ReportSubjectType;
	descriptionTemplateGroupId?: Guid;
	planBlueprintGroupId?: Guid;
	questions: ReportQuestionSelection[];
	planQuestions?: ReportQuestionSelection[];
	groupBy?: ReportDimensionSelection[];
	descriptionSubQuery?: DescriptionLookup;
	planSubQuery?: PlanLookup;
	latestVersionOnly?: boolean;
	includeInactive?: boolean;
	dateBucket?: ReportDateBucket;
	textTopN?: number;
	installationWide?: boolean;
}

/** A question by fieldset and fields, or by the semantic tag its fields carry. */
export interface ReportQuestionSelection {
	fieldSetId?: string;
	fieldIds?: string[];
	semantic?: string;
	label?: string;
}

export class ReportQuestionSearchLookup {
	like?: string;
	subject?: ReportSubjectType;
	limit?: number;
	installationWide?: boolean;
	includeSources?: boolean;
}

export interface ReportDimensionSelection {
	type: ReportDimensionType;
	questionKey?: string;
}
