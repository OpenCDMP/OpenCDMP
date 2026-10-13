import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { FieldValidationType } from '@app/core/common/enum/field-validation-type';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { TranslateService } from '@ngx-translate/core';
import { BlueprintStructureHelper } from './structure.helper';

/**
 * What an admin picks, named the way authors would call it. Several kinds sit on one stored field type or pick
 * between two, so the saved data keeps the stored field types:
 * - single choice is a radio box, or a select with one answer when "Show as a dropdown" is on
 * - multiple choice is a select with `multipleSelect`
 * - "plan or asset in OpenCDMP" is either internal-entries type, picked by "Link to"
 * - dataset identifier is the identifier type, or the validator type when "Check it" is on
 */
export type InputKind = 'freetext' | 'textarea' | 'richtext' | 'number' | 'yesno' | 'single' | 'multiple' | 'checkbox' | 'date'
	| 'reference' | 'internal' | 'tags' | 'identifier' | 'upload' | 'preset';

export const KIND_GROUPS: { key: string, kinds: InputKind[] }[] = [
	{ key: 'TEXT', kinds: ['freetext', 'textarea', 'richtext', 'number'] },
	{ key: 'CHOICES', kinds: ['yesno', 'single', 'multiple', 'checkbox', 'date'] },
	{ key: 'LINKED', kinds: ['reference', 'internal', 'tags', 'identifier', 'upload'] },
];

const KIND_ICON: Record<InputKind, string> = {
	freetext: 'short_text', textarea: 'notes', richtext: 'format_paint', number: '123',
	yesno: 'toggle_on', single: 'radio_button_checked', multiple: 'checklist', checkbox: 'check_box', date: 'event',
	reference: 'public', internal: 'link', tags: 'sell', identifier: 'fingerprint', upload: 'upload_file', preset: 'bookmark'
};

export function kindIcon(kind: InputKind): string { return KIND_ICON[kind] ?? 'short_text'; }
export function kindLabelKey(kind: InputKind): string { return `PLAN-BLUEPRINT-EDITOR.KINDS.${kind.toUpperCase()}`; }
export function kindHintKey(kind: InputKind): string { return `PLAN-BLUEPRINT-EDITOR.KINDS.${kind.toUpperCase()}-HINT`; }

/** The kind a stored field shows as. */
export function kindOf(fieldForm: UntypedFormGroup): InputKind {
	const type = BlueprintStructureHelper.fieldType(fieldForm);
	const multiple = !!fieldForm?.get('data')?.get('multipleSelect')?.value;
	switch (type) {
		case FieldType.FREE_TEXT: return 'freetext';
		case FieldType.TEXT_AREA: return 'textarea';
		case FieldType.RICH_TEXT_AREA: return 'richtext';
		case FieldType.NUMBER: return 'number';
		case FieldType.BOOLEAN_DECISION: return 'yesno';
		case FieldType.RADIO_BOX: return 'single';
		case FieldType.SELECT: return multiple ? 'multiple' : 'single';
		case FieldType.CHECK_BOX: return 'checkbox';
		case FieldType.DATE_PICKER: return 'date';
		case FieldType.REFERENCE_TYPES: return 'reference';
		case FieldType.INTERNAL_ENTRIES_PLANS:
		case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS: return 'internal';
		case FieldType.TAGS: return 'tags';
		case FieldType.DATASET_IDENTIFIER:
		case FieldType.VALIDATION: return 'identifier';
		case FieldType.UPLOAD: return 'upload';
		case FieldType.SYSTEM: return 'preset';
		default: return null;
	}
}

/** The stored type a newly picked kind starts as. */
export function defaultTypeFor(kind: InputKind): FieldType {
	switch (kind) {
		case 'freetext': return FieldType.FREE_TEXT;
		case 'textarea': return FieldType.TEXT_AREA;
		case 'richtext': return FieldType.RICH_TEXT_AREA;
		case 'number': return FieldType.NUMBER;
		case 'yesno': return FieldType.BOOLEAN_DECISION;
		case 'single': return FieldType.RADIO_BOX;
		case 'multiple': return FieldType.SELECT;
		case 'checkbox': return FieldType.CHECK_BOX;
		case 'date': return FieldType.DATE_PICKER;
		case 'reference': return FieldType.REFERENCE_TYPES;
		case 'internal': return FieldType.INTERNAL_ENTRIES_PLANS;
		case 'tags': return FieldType.TAGS;
		case 'identifier': return FieldType.DATASET_IDENTIFIER;
		case 'upload': return FieldType.UPLOAD;
		case 'preset': return FieldType.SYSTEM;
	}
}

/** Kinds whose answers can drive a rule, matching what the old rule editor accepted. */
export function kindHasOptions(kind: InputKind): boolean { return kind === 'single' || kind === 'multiple'; }

// ------------------------------------------------------------------ describing inputs in words

export interface RuleLink { sourceFieldId: string, sourceNumbering: string, sourceTitle: string, targetId: string, targetNumbering: string, targetTitle: string, value: string }

/** Everything under the sections array as a flat list of questions with numbering, for rules and summaries. */
export interface QuestionRef { id: string, numbering: string, title: string, fieldSetForm: UntypedFormGroup, sectionIds: string[] }

export function questionIndex(entries: ToCEntry[]): QuestionRef[] {
	const result: QuestionRef[] = [];
	const visit = (list: ToCEntry[], sectionIds: string[]) => list?.forEach(e => {
		if (e.type === ToCEntryType.FieldSet) result.push({ id: e.id, numbering: e.numbering, title: e.label, fieldSetForm: e.form as UntypedFormGroup, sectionIds });
		else visit(e.subEntries, [...sectionIds, e.id]);
	});
	visit(entries, []);
	return result;
}

function fieldsOf(fieldSetForm: UntypedFormGroup): UntypedFormGroup[] {
	return ((fieldSetForm?.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
}

function optionLabel(fieldForm: UntypedFormGroup, value: string): string {
	const options = ((fieldForm?.get('data')?.get('options') as UntypedFormArray)?.controls ?? []).map(o => o.value);
	return options.find(o => o.value == value)?.label || value;
}

/** A rule's value, as the author would pick it. */
export function ruleValueText(fieldForm: UntypedFormGroup, rule: UntypedFormGroup, language: TranslateService): string {
	const kind = kindOf(fieldForm);
	const v = rule.value ?? {};
	if (kind === 'yesno' || kind === 'checkbox') return v.booleanValue === true ? language.instant('PLAN-BLUEPRINT-EDITOR.RULES.YES') : v.booleanValue === false ? language.instant('PLAN-BLUEPRINT-EDITOR.RULES.NO') : '';
	if (kind === 'date') return v.dateValue ? new Date(v.dateValue).toLocaleDateString() : '';
	if (kind === 'number') return v.numberValue != null ? String(v.numberValue) : '';
	if (kindHasOptions(kind)) return optionLabel(fieldForm, v.textValue) ?? '';
	return v.textValue ?? '';
}

/** Every rule in the blueprint, resolved to numbered questions on both ends. */
export function ruleLinks(entries: ToCEntry[], language: TranslateService): RuleLink[] {
	const questions = questionIndex(entries);
	const byId = new Map<string, QuestionRef>();
	questions.forEach(q => { byId.set(q.id, q); fieldsOf(q.fieldSetForm).forEach(f => byId.set(f.get('id').value, q)); });
	const links: RuleLink[] = [];
	questions.forEach(q => fieldsOf(q.fieldSetForm).forEach(f => {
		((f.get('visibilityRules') as UntypedFormArray)?.controls ?? []).forEach(r => {
			const targetId = r.get('target')?.value;
			const target = targetId ? byId.get(targetId) : null;
			links.push({
				sourceFieldId: f.get('id').value, sourceNumbering: q.numbering, sourceTitle: q.title,
				targetId, targetNumbering: target?.numbering ?? '?', targetTitle: target?.title ?? '',
				value: ruleValueText(f, r as UntypedFormGroup, language)
			});
		});
	}));
	return links;
}

/** The default value, as the author would see it prefilled. */
export function defaultValueText(fieldForm: UntypedFormGroup, language: TranslateService): string {
	const type = BlueprintStructureHelper.fieldType(fieldForm);
	if (!BlueprintStructureHelper.canSetDefaultValue(type)) return null;
	const control = fieldForm.get('defaultValue')?.get(BlueprintStructureHelper.defaultValueControlName(type));
	const v = control?.value;
	if (v === null || v === undefined || v === '') return null;
	const kind = kindOf(fieldForm);
	if (kind === 'yesno' || kind === 'checkbox') return language.instant(v ? 'PLAN-BLUEPRINT-EDITOR.RULES.YES' : 'PLAN-BLUEPRINT-EDITOR.RULES.NO');
	if (kind === 'date') return new Date(v).toLocaleDateString();
	if (kindHasOptions(kind)) return optionLabel(fieldForm, v);
	return String(v).replace(/<[^>]+>/g, ' ').trim();
}

export function isRequired(fieldForm: UntypedFormGroup): boolean { return BlueprintStructureHelper.hasValidation(fieldForm, FieldValidationType.Required); }
