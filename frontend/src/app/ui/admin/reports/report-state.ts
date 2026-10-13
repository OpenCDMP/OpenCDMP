import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { ReportOutlineField, ReportQuestionHit, ReportSemanticHit } from '@app/core/model/report/report';
import { Guid } from '@common/types/guid';

/**
 * A question the page is reporting on, in either of the two ways the engine accepts it:
 * one fieldset of one template or blueprint lineage, or a semantic tag matched across every
 * lineage of the tenant.
 */
export interface ReportQuestionRef {
	kind: 'fieldset' | 'semantic';
	subject: ReportSubjectType;
	title: string;
	numbering?: string;
	// fieldset kind
	groupId?: Guid;
	groupLabel?: string;
	versionCount?: number;
	fieldSetId?: string;
	/** The fields counted; a subset of allFields. */
	fieldIds?: string[];
	allFields?: ReportOutlineField[];
	// semantic kind — the tag is the title; how templates word it is the subtitle
	semantic?: string;
	subtitle?: string;
	titles?: string[];
	groupCount?: number;
	groupLabels?: string[];
}

export interface ReportFilters {
	descriptionStatusIds: Guid[];
	planStatusIds: Guid[];
	accessTypes: PlanAccessType[];
	planBlueprintGroupId: Guid | null;
	createdAfter: Date | null;
	createdBefore: Date | null;
	planReferenceIds: Guid[];
	descriptionReferenceIds: Guid[];
}

export function emptyFilters(): ReportFilters {
	return {
		descriptionStatusIds: [],
		planStatusIds: [],
		accessTypes: [],
		planBlueprintGroupId: null,
		createdAfter: null,
		createdBefore: null,
		planReferenceIds: [],
		descriptionReferenceIds: []
	};
}

/** A breakdown the user can choose, encoded so question breakdowns keep their identity. */
export interface BreakdownOption {
	token: string;
	label: string;
}

export function questionLabel(question: { numbering?: string, title?: string }): string {
	return [question.numbering, question.title].filter(x => !!x).join(' ');
}

export function fromQuestionHit(hit: ReportQuestionHit): ReportQuestionRef {
	return {
		kind: 'fieldset',
		subject: hit.subject,
		title: hit.title,
		numbering: hit.numbering,
		groupId: hit.groupId,
		groupLabel: hit.groupLabel,
		versionCount: hit.versionCount,
		fieldSetId: hit.fieldSetId,
		fieldIds: (hit.fields ?? []).map(x => x.id),
		allFields: hit.allFields ?? hit.fields ?? []
	};
}

export function fromSemanticHit(hit: ReportSemanticHit): ReportQuestionRef {
	return {
		kind: 'semantic',
		subject: hit.subject,
		title: hit.semantic,
		subtitle: hit.label,
		semantic: hit.semantic,
		titles: hit.titles,
		groupCount: hit.groupCount,
		groupLabels: hit.groupLabels
	};
}

/**
 * The i18n key suffix that names a field type in plain words: a report reader should see
 * "choice" and "free text", never <code>referenceTypes</code>.
 */
export function fieldTypeWord(fieldType: string): string {
	switch (fieldType) {
		case 'select':
		case 'radiobox':
			return 'CHOICE';
		case 'referenceTypes':
			return 'REFERENCE';
		case 'freetext':
		case 'textarea':
		case 'richTextarea':
			return 'FREE-TEXT';
		case 'booleanDecision':
		case 'checkBox':
			return 'YES-NO';
		case 'datePicker':
			return 'DATE';
		case 'number':
			return 'NUMBER';
		case 'tags':
			return 'TAGS';
		case 'upload':
			return 'FILE';
		case 'datasetIdentifier':
		case 'validation':
			return 'IDENTIFIER';
		case 'internalEntitiesPlans':
			return 'PLANS';
		case 'internalEntitiesDescriptions':
			return 'ASSETS';
		default:
			return 'OTHER';
	}
}

/** A tick box that only reveals a sibling is a control, not an answer. */
export function isControlField(field: ReportOutlineField): boolean {
	return !!field.controlsOtherFields && field.fieldType === 'checkBox';
}

/**
 * The fields counted when a whole question is picked; mirrors the engine's countableFields. A
 * yes/no beside other fields is the question itself, so it alone is counted.
 */
export function defaultCountedFields(fields: ReportOutlineField[]): ReportOutlineField[] {
	const all = fields ?? [];
	const decisions = all.filter(x => x.fieldType === 'booleanDecision');
	if (decisions.length > 0) return decisions;
	const countable = all.filter(x => !isControlField(x));
	// A fieldset that is nothing but a control still has to be selectable.
	return countable.length > 0 ? countable : all;
}
