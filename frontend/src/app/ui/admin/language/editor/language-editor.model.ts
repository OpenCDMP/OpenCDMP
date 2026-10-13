import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { Language, LanguagePersist } from "@app/core/model/language/language";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";

export interface TranslationText {
	path: string[];
	value: unknown;
}

// Paths are kept as segment arrays because some keys contain dots (e.g. licence ids like "cc-by-4.0").
export function flattenTranslations(source: object, path: string[] = [], result: TranslationText[] = []): TranslationText[] {
	for (const [key, value] of Object.entries(source ?? {})) {
		if (value != null && typeof value === 'object' && !Array.isArray(value)) flattenTranslations(value, [...path, key], result);
		else result.push({ path: [...path, key], value: value });
	}
	return result;
}

export function unflattenTranslations(texts: TranslationText[]): object {
	const result = {};
	for (const text of texts) {
		let node = result;
		for (const key of text.path.slice(0, -1)) {
			if (node[key] == null) node[key] = {};
			node = node[key];
		}
		node[text.path[text.path.length - 1]] = text.value;
	}
	return result;
}

export class LanguageEditorModel extends BaseEditorModel implements LanguagePersist {
	code: string;
	payload: string;
	ordinal: number;

	permissions: string[];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { super(); }

	public fromModel(item: Language): LanguageEditorModel {
		if (item) {
			super.fromModel(item);
			this.code = item.code;
			this.payload = item.payload;
			this.ordinal = item.ordinal;
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			id: [{ value: this.id, disabled: disabled }, context.getValidation('id').validators],
			code: [{ value: this.code, disabled: disabled }, context.getValidation('code').validators],
			payload: [{ value: this.payload, disabled: disabled }, context.getValidation('payload').validators],
			ordinal: [{ value: this.ordinal, disabled: disabled }, context.getValidation('ordinal').validators],				
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators]
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'code', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'code')] });
		baseValidationArray.push({ key: 'payload', validators: [BackendErrorValidator(this.validationErrorModel, 'payload')] });
		baseValidationArray.push({ key: 'ordinal', validators: [BackendErrorValidator(this.validationErrorModel, 'ordinal')] });
		baseValidationArray.push({ key: 'hash', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
