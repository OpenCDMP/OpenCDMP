import { ReportAnswerState } from '@app/core/common/enum/report-answer-state.enum';
import { ReportDimensionType } from '@app/core/common/enum/report-dimension-type.enum';
import { ReportSubjectType } from '@app/core/common/enum/report-subject-type.enum';
import { Guid } from '@common/types/guid';

/**
 * The question tree of one template or blueprint lineage, numbered the way the editor's
 * table of contents numbers it, so a report refers to a question by the number its authors
 * already use.
 */
export interface ReportOutline {
	subject: ReportSubjectType;
	groupId: Guid;
	latestVersionId: Guid;
	label: string;
	latestVersion: number;
	versionCount: number;
	sections: ReportOutlineSection[];
}

export interface ReportOutlineSection {
	id: string;
	numbering: string;
	title: string;
	ordinal: number;
	sections: ReportOutlineSection[];
	fieldSets: ReportOutlineFieldSet[];
}

export interface ReportOutlineFieldSet {
	id: string;
	numbering: string;
	title: string;
	hasMultiplicity: boolean;
	fields: ReportOutlineField[];
}

export interface ReportOutlineField {
	id: string;
	label: string;
	fieldType: string;
	ordinal: number;
	semantics: string[];
	/** The field gates its siblings — a control, not an answer. */
	controlsOtherFields: boolean;
}

export interface Report {
	subject: ReportSubjectType;
	sourceGroupId: Guid;
	sourceLabel: string;
	sourceVersionCount: number;
	/** True when every template or blueprint of the tenant was read rather than one lineage. */
	sourceSpansAllGroups: boolean;
	generatedAt: Date;
	subjects: number;
	distinctPlans: number;
	dimensions: ReportDimension[];
	questions: ReportQuestionResult[];
	crossTab: ReportCrossTabRow[];
}

export interface ReportDimension {
	type: ReportDimensionType;
	label: string;
	questionKey: string;
}

export interface ReportAnswerStates {
	answered: number;
	notAnswered: number;
	notApplicable: number;
	notMigrated: number;
}

export interface ReportDistributionEntry {
	value: string;
	count: number;
	percentOfAnswered: number;
}

export interface ReportNumberSummary {
	sum: number;
	average: number;
	min: number;
	max: number;
	count: number;
}

export interface ReportQuestionResult {
	key: string;
	label: string;
	numbering: string;
	fieldType: string;
	multiValue: boolean;
	states: ReportAnswerStates;
	distribution: ReportDistributionEntry[];
	numberSummary: ReportNumberSummary;
}

export interface ReportQuestionCell {
	key: string;
	states: ReportAnswerStates;
	distribution: ReportDistributionEntry[];
}

export interface ReportCrossTabRow {
	dimensionValues: string[];
	subjects: number;
	distinctPlans: number;
	questions: ReportQuestionCell[];
}

/** Convenience for the picker: a question the user has selected, before it is sent. */
export interface SelectedQuestion {
	fieldSetId: string;
	fieldIds: string[];
	label: string;
	numbering: string;
}

/** Questions found across every template and blueprint, by number, title, label or semantic. */
export interface ReportQuestionSearch {
	questions: ReportQuestionHit[];
	semantics: ReportSemanticHit[];
	/** Only when the lookup asks for them. */
	sources?: ReportSourceHit[];
}

/** One asset template or plan blueprint lineage, by its current version. */
export interface ReportSourceHit {
	subject: ReportSubjectType;
	groupId: Guid;
	label: string;
	/** Set only on an installation-wide search. */
	tenantCode?: string;
	versionCount: number;
}

/** One fieldset of one template or blueprint lineage. */
export interface ReportQuestionHit {
	subject: ReportSubjectType;
	groupId: Guid;
	groupLabel: string;
	/** Set only on an installation-wide search. */
	tenantCode?: string;
	versionCount: number;
	fieldSetId: string;
	numbering: string;
	title: string;
	hasMultiplicity: boolean;
	/** The fields that carry answers. */
	fields: ReportOutlineField[];
	/** Every field of the fieldset. */
	allFields: ReportOutlineField[];
}

/** A semantic tag shared by fields of several templates, reportable across all of them. */
export interface ReportSemanticHit {
	subject: ReportSubjectType;
	semantic: string;
	/** How templates most often word the question; the tag itself is its name. */
	label: string;
	/** Every distinct wording, most common first. */
	titles: string[];
	groupCount: number;
	groupLabels: string[];
	fieldTypes: string[];
}

export { ReportAnswerState };
