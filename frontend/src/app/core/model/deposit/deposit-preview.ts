import { PreviewFieldSource } from "@app/core/common/enum/preview-field-source";
import { PreviewFieldType } from "@app/core/common/enum/preview-field-type";
import { Guid } from "@common/types/guid";

export interface DepositPreviewModel {
	groups: PreviewFieldGroup[];
}

export interface PreviewFieldGroup {
	code: string;
	label: string;
	repeatable: boolean;
	required?: boolean;
	fields: PreviewField[];
	groups?: PreviewFieldGroup[];
	items?: PreviewGroupItem[];
	baseItems?: PreviewGroupItem[];
	storedItemsApplied?: boolean;
	widgetHint?: string;
}

export interface PreviewField {
	code: string;
	label: string;
	description?: string;
	type: PreviewFieldType;
	source: PreviewFieldSource;
	required: boolean;
	multipleSelect: boolean;
	options?: PreviewFieldOption[];
	value?: PreviewFieldValue;
	baseValue?: PreviewFieldValue;
	previousValue?: PreviewFieldValue;
	storedValueApplied?: boolean;
	vocabSearch?: boolean;
	widgetHint?: string;
}

export interface PreviewFieldOption {
	label: string;
	value: string;
}

export interface PreviewFieldValue {
	code: string;
	textValue?: string;
	textListValue?: string[];
	booleanValue?: boolean;
	numberValue?: number;
	dateValue?: Date;
}

export interface PreviewGroupItem {
	values: PreviewFieldValue[];
}

export interface PreviewGroupValues {
	groupCode: string;
	items: PreviewGroupItem[];
	baseItems?: PreviewGroupItem[];
}

export interface StoredPreviewField {
	value: PreviewFieldValue;
	base?: PreviewFieldValue;
}

export interface DepositPreviewResult {
	storedFields: StoredPreviewField[];
	storedGroups: PreviewGroupValues[];
}

export class DepositPreviewRequest {
	repositoryId: string;
	planId: Guid;
}
