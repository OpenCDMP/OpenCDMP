import { Guid } from "@common/types/guid";
import { PreviewKind } from "@app/core/common/enum/preview-kind.enum";
import { DescriptionChangeKind } from "@app/core/common/enum/description-change-kind.enum";

export interface PlanUpdatePreview {
	planUpdateRequestId: Guid;
	planId?: Guid;
	planVersion?: string;
	kind: PreviewKind;
	planFieldChanges?: FieldChange[];
	descriptionChanges?: DescriptionChange[];
}

/**
 * Typed representation of a field value as returned by the backend preview endpoint.
 * Mirrors org.opencdmp.model.persist.descriptionproperties.FieldPersist.
 */
export interface FieldValuePreview {
	textValue?: string;
	textListValue?: string[];
	dateValue?: string;
	booleanValue?: boolean;
	numberValue?: number;
	reference?: ReferenceValuePreview;
	references?: ReferenceValuePreview[];
	tags?: string[];
	externalIdentifier?: ExternalIdentifierValuePreview;
}

export interface ReferenceValuePreview {
	id?: string;
	label?: string;
	reference?: string;
	source?: string;
	typeId?: string;
	description?: string;
	abbreviation?: string;
	sourceType?: string;
}

export interface ExternalIdentifierValuePreview {
	identifier?: string;
	type?: string;
}

export interface FieldChange {
	changeId: Guid;
	fieldId: string;
	fieldLabel?: string;
	currentValue?: FieldValuePreview;
	incomingValue?: FieldValuePreview;
}

export interface DescriptionChange {
	changeId: Guid;
	kind: DescriptionChangeKind;
	datasetId?: string;
	descriptionId?: Guid;
	templateId?: Guid;
	fieldChanges?: FieldChange[];
	candidateTemplateIds?: Guid[];
	incomingFields?: IncomingField[];
	currentLabel?: string;
}

export interface IncomingField {
	key: string;
	label?: string;
	value?: any;
}

/**
 * Returns a short, human-readable display string for a FieldValuePreview.
 * Extracts the typed sub-value rather than dumping the whole object as JSON.
 */
export function fieldValueDisplayText(value: FieldValuePreview | null | undefined, maxLength = 60): string {
	if (value == null) return '—';

	let text: string;

	if (value.textListValue != null) {
		text = value.textListValue.join(', ');
	} else if (value.textValue != null) {
		text = value.textValue;
	} else if (value.dateValue != null) {
		const d = new Date(value.dateValue);
		text = isNaN(d.getTime()) ? value.dateValue : d.toLocaleDateString();
	} else if (value.booleanValue != null) {
		text = String(value.booleanValue);
	} else if (value.numberValue != null) {
		text = String(value.numberValue);
	} else if (value.reference != null) {
		text = value.reference.label ?? value.reference.reference ?? value.reference.id ?? '—';
	} else if (value.references != null && value.references.length > 0) {
		text = value.references
			.map(r => r.label ?? r.reference ?? r.id ?? '?')
			.join(', ');
	} else if (value.tags != null) {
		text = value.tags.join(', ');
	} else if (value.externalIdentifier != null) {
		const ei = value.externalIdentifier;
		text = ei.type ? `${ei.type}: ${ei.identifier ?? ''}` : (ei.identifier ?? '—');
	} else {
		return '—';
	}

	return text.length > maxLength ? text.slice(0, maxLength) + '…' : text;
}
