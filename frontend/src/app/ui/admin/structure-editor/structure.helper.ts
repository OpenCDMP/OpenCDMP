import { FormArray, FormGroup, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { FieldValidationType } from '@app/core/common/enum/field-validation-type';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { BaseField, FieldLabelAndMultiplicityData, FieldLabelData, FieldNumberData, FieldRadioBoxData, FieldReferenceTypeData, FieldSelectData, FieldSelectOption, FieldSystemData, FieldUploadData } from '@app/core/model/description-template/description-template';
import { BaseFieldPersist } from '@app/core/model/description-template/description-template-persist';
import { TemplateFieldEditorModel, TemplateFieldSetEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';

/**
 * Pure helpers for the plan blueprint and description template editors. Everything here works on the same
 * reactive forms the shared editor models build.
 */

/** Preset fields a blueprint must carry before it can be finalized, in the order the bar shows them. */
const REQUIRED_PRESETS: SystemFieldType[] = [SystemFieldType.PlanTitle, SystemFieldType.PlanDescription, SystemFieldType.PlanLanguage, SystemFieldType.PlanAccessRights];
/** Preset fields that may be added but are not required. */
const OPTIONAL_PRESETS: SystemFieldType[] = [SystemFieldType.PlanContact];
/** Presets a description template must carry, and the ones it may. */
const TEMPLATE_REQUIRED_PRESETS: SystemFieldType[] = [SystemFieldType.PlanDescriptionTitle];
const TEMPLATE_OPTIONAL_PRESETS: SystemFieldType[] = [SystemFieldType.PlanDescriptionDescription, SystemFieldType.PlanDescriptionTag];
/** Presets that belong to asset templates, never to a blueprint. */
const SKIPPED_PRESETS: SystemFieldType[] = [SystemFieldType.PlanDescriptionTitle, SystemFieldType.PlanDescriptionDescription, SystemFieldType.PlanDescriptionTag];

/** Presets whose answer authors must always give. A preset only ever sits in its own kind of structure, so one list serves both. */
const ALWAYS_REQUIRED_PRESETS: SystemFieldType[] = [SystemFieldType.PlanTitle, SystemFieldType.PlanDescription, SystemFieldType.PlanLanguage, SystemFieldType.PlanAccessRights, SystemFieldType.PlanDescriptionTitle];

/** Input types offered in the "Add question" menu and the type picker, grouped the way the picker shows them. */
const TYPE_GROUPS: { key: string, types: FieldType[] }[] = [
	{ key: 'TEXT', types: [FieldType.FREE_TEXT, FieldType.TEXT_AREA, FieldType.RICH_TEXT_AREA, FieldType.NUMBER] },
	{ key: 'CHOICES', types: [FieldType.BOOLEAN_DECISION, FieldType.RADIO_BOX, FieldType.SELECT, FieldType.CHECK_BOX, FieldType.DATE_PICKER] },
	{ key: 'LINKED', types: [FieldType.REFERENCE_TYPES, FieldType.INTERNAL_ENTRIES_PLANS, FieldType.INTERNAL_ENTRIES_DESCRIPTIONS, FieldType.TAGS, FieldType.DATASET_IDENTIFIER, FieldType.VALIDATION, FieldType.UPLOAD] },
];

function typeIcon(type: FieldType): string {
	switch (type) {
		case FieldType.SYSTEM: return 'bookmark';
		case FieldType.FREE_TEXT: return 'short_text';
		case FieldType.TEXT_AREA: return 'notes';
		case FieldType.RICH_TEXT_AREA: return 'format_paint';
		case FieldType.NUMBER: return '123';
		case FieldType.BOOLEAN_DECISION: return 'toggle_on';
		case FieldType.RADIO_BOX: return 'radio_button_checked';
		case FieldType.SELECT: return 'arrow_drop_down_circle';
		case FieldType.CHECK_BOX: return 'check_box';
		case FieldType.DATE_PICKER: return 'event';
		case FieldType.REFERENCE_TYPES: return 'public';
		case FieldType.INTERNAL_ENTRIES_PLANS: return 'description';
		case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS: return 'inventory_2';
		case FieldType.TAGS: return 'sell';
		case FieldType.DATASET_IDENTIFIER: return 'fingerprint';
		case FieldType.VALIDATION: return 'verified';
		case FieldType.UPLOAD: return 'upload_file';
		default: return 'short_text';
	}
}

function canApplyVisibility(type: FieldType): boolean {
	return [FieldType.TEXT_AREA, FieldType.RICH_TEXT_AREA, FieldType.FREE_TEXT, FieldType.BOOLEAN_DECISION, FieldType.RADIO_BOX, FieldType.SELECT, FieldType.CHECK_BOX, FieldType.DATE_PICKER, FieldType.NUMBER].includes(type);
}

function canSetDefaultValue(type: FieldType): boolean {
	return [FieldType.FREE_TEXT, FieldType.TEXT_AREA, FieldType.RICH_TEXT_AREA, FieldType.RADIO_BOX, FieldType.SELECT, FieldType.DATE_PICKER, FieldType.CHECK_BOX, FieldType.BOOLEAN_DECISION, FieldType.NUMBER].includes(type);
}

function defaultValueControlName(type: FieldType): 'textValue' | 'dateValue' | 'booleanValue' | 'numberValue' {
	if (type == FieldType.DATE_PICKER) return 'dateValue';
	if (type == FieldType.CHECK_BOX || type == FieldType.BOOLEAN_DECISION) return 'booleanValue';
	if (type == FieldType.NUMBER) return 'numberValue';
	return 'textValue';
}

/** The data block a brand-new field of the given type starts with. Mirrors the old field editor. */
function emptyFieldData(type: FieldType, maxFileSizeInMB: number, systemFieldType?: SystemFieldType): any {
	switch (type) {
		case FieldType.REFERENCE_TYPES: return { label: '', multipleSelect: false, fieldType: type } as FieldReferenceTypeData;
		case FieldType.RADIO_BOX: return { label: '', options: [{ label: '', value: '' }], fieldType: type } as FieldRadioBoxData;
		case FieldType.SELECT: return { label: '', multipleSelect: false, options: [{ label: '', value: '' } as FieldSelectOption], fieldType: type } as FieldSelectData;
		case FieldType.INTERNAL_ENTRIES_PLANS:
		case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS: return { label: '', multipleSelect: false, fieldType: type } as FieldLabelAndMultiplicityData;
		case FieldType.UPLOAD: return { label: '', types: [], maxFileSizeInMB, fieldType: type } as FieldUploadData;
		case FieldType.NUMBER: return { label: '', fieldType: type } as FieldNumberData;
		case FieldType.SYSTEM: return { label: '', fieldType: type, systemFieldType: systemFieldType ?? null } as FieldSystemData;
		default: return { label: '', fieldType: type } as FieldLabelData;
	}
}

function newField(params: { type: FieldType, ordinal: number, maxFileSizeInMB: number, systemFieldType?: SystemFieldType, required?: boolean }): BaseField {
	const { type, ordinal, maxFileSizeInMB, systemFieldType, required } = params;
	const validations: FieldValidationType[] = [];
	if (required || (type == FieldType.SYSTEM && systemFieldType != null && ALWAYS_REQUIRED_PRESETS.includes(systemFieldType))) validations.push(FieldValidationType.Required);
	return {
		id: Guid.create().toString(),
		ordinal,
		validations,
		includeInExport: true,
		semantics: [],
		data: emptyFieldData(type, maxFileSizeInMB, systemFieldType)
	} as BaseField;
}

/** Builds a question (field set) holding exactly one input. */
function newFieldSetForm(params: { validationErrorModel: ValidationErrorModel, ordinal: number, title: string, field: BaseField, rootPath: string }): UntypedFormGroup {
	const { validationErrorModel, ordinal, title, field, rootPath } = params;
	const fieldSet = new TemplateFieldSetEditorModel(validationErrorModel);
	fieldSet.id = Guid.create().toString();
	fieldSet.ordinal = ordinal;
	fieldSet.title = title;
	const fieldSetForm = fieldSet.buildForm({ rootPath });
	const fieldForm = new TemplateFieldEditorModel(validationErrorModel).fromModel(field).buildForm({ rootPath: `${rootPath}fields[0].` });
	(fieldSetForm.get('fields') as UntypedFormArray).push(fieldForm);
	return fieldSetForm;
}

/**
 * Replaces a field's form with one rebuilt for another type. Options, default values and rule values that no
 * longer make sense are dropped on a type change.
 */
function rebuildFieldForType(params: { fieldForm: UntypedFormGroup, type: FieldType, validationErrorModel: ValidationErrorModel, rootPath: string, maxFileSizeInMB: number, carry?: boolean, multipleSelect?: boolean }): UntypedFormGroup {
	const { fieldForm, type, validationErrorModel, rootPath, maxFileSizeInMB, carry = false, multipleSelect } = params;
	const field: BaseFieldPersist = fieldForm.getRawValue();
	const previousData = (field.data as any) ?? {};
	const previousDefault = field.defaultValue;
	field.defaultValue = { booleanValue: null, numberValue: null, dateValue: null, textValue: null };
	if (!canApplyVisibility(type)) field.visibilityRules = [];
	const previousLabel = previousData.label ?? '';
	field.data = emptyFieldData(type, maxFileSizeInMB);
	(field.data as any).label = previousLabel;
	// switching between variants of one kind (radio and dropdown, plans and assets, identifier and validator)
	// keeps what the admin already set up: options, the default answer, the rules and "more than one".
	if (carry) {
		if (Array.isArray(previousData.options) && 'options' in (field.data as any)) (field.data as any).options = previousData.options;
		if ('multipleSelect' in (field.data as any) && previousData.multipleSelect != null) (field.data as any).multipleSelect = previousData.multipleSelect;
		if (previousDefault?.textValue != null) field.defaultValue.textValue = previousDefault.textValue;
	}
	if (multipleSelect != null && 'multipleSelect' in (field.data as any)) (field.data as any).multipleSelect = multipleSelect;
	if (![FieldType.FREE_TEXT].includes(type)) {
		field.validations = (field.validations ?? []).filter(v => v != FieldValidationType.Url && v != FieldValidationType.Email);
	}
	if (type == FieldType.SYSTEM) field.validations = (field.validations ?? []);
	if (!canSetDefaultValue(type)) field.defaultValue = null;
	const rebuilt = new TemplateFieldEditorModel(validationErrorModel).fromModel(field).buildForm({ rootPath });
	const parent = fieldForm.parent as UntypedFormArray;
	const index = parent?.controls.findIndex(c => c.get('id').value === fieldForm.get('id').value) ?? -1;
	if (index >= 0) {
		parent.removeAt(index);
		parent.insert(index, rebuilt);
	}
	return rebuilt;
}

function fieldType(fieldForm: UntypedFormGroup | FormGroup<any>): FieldType {
	return fieldForm?.get('data')?.get('fieldType')?.value;
}

function systemFieldType(fieldForm: UntypedFormGroup | FormGroup<any>): SystemFieldType {
	return fieldType(fieldForm) == FieldType.SYSTEM ? fieldForm?.get('data')?.get('systemFieldType')?.value : null;
}

function isPresetQuestion(fieldSetForm: UntypedFormGroup): boolean {
	const fields = fieldSetForm?.get('fields') as UntypedFormArray;
	return fields?.controls?.some(f => fieldType(f as UntypedFormGroup) == FieldType.SYSTEM) ?? false;
}

function hasValidation(fieldForm: UntypedFormGroup, validation: FieldValidationType): boolean {
	const validations: FieldValidationType[] = fieldForm?.get('validations')?.value ?? [];
	return validations.includes(validation);
}

function setValidation(fieldForm: UntypedFormGroup, validation: FieldValidationType, on: boolean): void {
	const control = fieldForm.get('validations');
	const current: FieldValidationType[] = [...(control.value ?? [])];
	const next = on ? (current.includes(validation) ? current : [...current, validation]) : current.filter(v => v != validation);
	control.setValue(next);
	control.markAsDirty();
	control.updateValueAndValidity();
}

/** Every preset field type present anywhere under a section form (or the whole sections array). */
function presetsIn(form: UntypedFormGroup | UntypedFormArray): SystemFieldType[] {
	const result: SystemFieldType[] = [];
	const visitSection = (section: UntypedFormGroup) => {
		(section.get('sections') as UntypedFormArray)?.controls?.forEach(s => visitSection(s as UntypedFormGroup));
		(section.get('fieldSets') as UntypedFormArray)?.controls?.forEach(fs => {
			(fs.get('fields') as UntypedFormArray)?.controls?.forEach(f => {
				const t = systemFieldType(f as UntypedFormGroup);
				if (t != null) result.push(t);
			});
		});
	};
	if (form instanceof UntypedFormArray || form instanceof FormArray) { (form as UntypedFormArray).controls.forEach(s => visitSection(s as UntypedFormGroup)); }
	else if (form) { visitSection(form as UntypedFormGroup); }
	return result;
}

function countQuestions(entry: ToCEntry): number {
	if (!entry?.subEntries?.length) return 0;
	return entry.subEntries.reduce((sum, e) => sum + (e.type == ToCEntryType.FieldSet ? 1 : countQuestions(e)), 0);
}

function hasSubsections(entry: ToCEntry): boolean {
	return entry?.subEntries?.some(e => e.type == ToCEntryType.Section) ?? false;
}

function hasQuestions(entry: ToCEntry): boolean {
	return entry?.subEntries?.some(e => e.type == ToCEntryType.FieldSet) ?? false;
}

/** Path of entries from a root section down to the given entry, for breadcrumbs. */
function pathTo(id: string, entries: ToCEntry[], acc: ToCEntry[] = []): ToCEntry[] {
	for (const entry of entries ?? []) {
		if (entry.id === id) return [...acc, entry];
		const deeper = pathTo(id, entry.subEntries ?? [], [...acc, entry]);
		if (deeper) return deeper;
	}
	return null;
}

/** True when any control under the form is invalid (touched or not). */
function isInvalid(form: UntypedFormGroup | UntypedFormArray): boolean {
	return form?.invalid ?? false;
}

function sectionsArrayOf(parentEntry: ToCEntry, rootSections: UntypedFormArray): UntypedFormArray {
	return parentEntry ? (parentEntry.form.get('sections') as UntypedFormArray) : rootSections;
}

function renumber(array: UntypedFormArray): void {
	array?.controls?.forEach((control, index) => { control.get('ordinal')?.setValue(index); });
}

/** Display label for the first input of a question, used on collapsed rows. */
function firstFieldForm(fieldSetForm: UntypedFormGroup): UntypedFormGroup {
	const fields = fieldSetForm?.get('fields') as UntypedFormArray;
	return (fields?.at(0) as UntypedFormGroup) ?? null;
}

function semanticsOf(fieldForm: UntypedFormGroup): string[] {
	return fieldForm?.get('semantics')?.value ?? [];
}

export const BlueprintStructureHelper = {
	REQUIRED_PRESETS,
	OPTIONAL_PRESETS,
	TEMPLATE_REQUIRED_PRESETS,
	TEMPLATE_OPTIONAL_PRESETS,
	SKIPPED_PRESETS,
	ALWAYS_REQUIRED_PRESETS,
	TYPE_GROUPS,
	typeIcon,
	canApplyVisibility,
	canSetDefaultValue,
	defaultValueControlName,
	emptyFieldData,
	newField,
	newFieldSetForm,
	rebuildFieldForType,
	fieldType,
	systemFieldType,
	isPresetQuestion,
	hasValidation,
	setValidation,
	presetsIn,
	countQuestions,
	hasSubsections,
	hasQuestions,
	pathTo,
	isInvalid,
	sectionsArrayOf,
	renumber,
	firstFieldForm,
	semanticsOf
};
