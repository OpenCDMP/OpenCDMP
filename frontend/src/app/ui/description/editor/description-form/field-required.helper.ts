import { FieldType } from '@app/core/common/enum/field-type';
import { FieldValidationType } from '@app/core/common/enum/field-validation-type';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { BaseField, BaseFieldSet, BaseSection, FieldSystemData } from '@app/core/model/description-template/description-template';

/**
 * Single source of truth for "is this field required?" as far as the UI is concerned.
 *
 * Every field — system or not — carries its requirement in `validations`; the backend passes the
 * blueprint/template author's choice through verbatim (`FieldPersist.validations`) and never injects
 * `Required` on its own. Title, Description, Access Rights, Language and Description Title are only
 * "always required" in practice because the plan / description editor models always persist them with
 * `Required` set; `validations` still reflects that. `PlanContact`, by contrast, is genuinely optional
 * unless the blueprint author checked "required" for it, so it must NOT be hardcoded here — this list
 * exists only to cover legacy blueprints saved before a field carried its own `validations`.
 *
 * The field heading, the inline control, the table of contents and the section legend all derive from
 * this function so they can no longer disagree.
 */
const REQUIRED_SYSTEM_FIELD_TYPES: readonly SystemFieldType[] = [
	SystemFieldType.PlanTitle,
	SystemFieldType.PlanDescription,
	SystemFieldType.PlanAccessRights,
	SystemFieldType.PlanLanguage,
	SystemFieldType.PlanDescriptionTitle,
];

export function isFieldRequired(field: BaseField | null | undefined): boolean {
	if (!field) { return false; }
	if (field.validations?.includes(FieldValidationType.Required)) { return true; }
	if (field.data?.fieldType === FieldType.SYSTEM) {
		return REQUIRED_SYSTEM_FIELD_TYPES.includes((field.data as FieldSystemData)?.systemFieldType);
	}
	return false;
}

export function isFieldSetRequired(fieldSet: BaseFieldSet | null | undefined): boolean {
	return (fieldSet?.fields ?? []).some(isFieldRequired);
}

/** True when the section, or any section nested under it, contains at least one required field. */
export function sectionHasRequiredFields(section: BaseSection | null | undefined): boolean {
	if (!section) { return false; }
	return (section.fieldSets ?? []).some(isFieldSetRequired) || (section.sections ?? []).some(sectionHasRequiredFields);
}
