import { FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import { IconConfiguration, IconConfigurationPersist } from "@app/core/model/icon-configuration/icon-configuration";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export interface IconConfigurationForm extends FormGroup{
    matIconName: FormControl<string>;
	storageFileId: FormControl<Guid>;
}
export class IconConfigurationEditorModel implements IconConfigurationPersist {
	matIconName: string;
	storageFileId: Guid;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(
		public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()
	) { }

	public fromModel(item: IconConfiguration): IconConfigurationEditorModel {
		if (item) {
			this.matIconName = item.matIconName;
			this.storageFileId = item.storageFile?.id;
		}
		return this;
	}

	buildForm(params?: {
		context?: ValidationContext,
		disabled?: boolean,
		rootPath?: string
	}): UntypedFormGroup {
		let {context = null, disabled = false, rootPath } = params ?? {}
		if (context == null) {
			context = IconConfigurationEditorModel.createValidationContext({
				validationErrorModel: this.validationErrorModel,
				rootPath
			});
		}

		return this.formBuilder.group({
			matIconName: [{ value: this.matIconName, disabled: disabled }, context.getValidation('matIconName').validators],
			storageFileId: [{ value: this.storageFileId, disabled: disabled }, context.getValidation('storageFileId').validators],
		});
	}

	static createValidationContext(params: {
		rootPath?: string,
		validationErrorModel: ValidationErrorModel
	}): ValidationContext {
		const { rootPath = '', validationErrorModel } = params;

		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'matIconName', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}matIconName`)] });
		baseValidationArray.push({ key: 'storageFileId', validators: [BackendErrorValidator(validationErrorModel, `${rootPath}storageFileId`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

	static reapplyValidators(params: {
		formGroup: UntypedFormGroup,
		validationErrorModel: ValidationErrorModel,
		rootPath: string
	}): void {
		const { formGroup, validationErrorModel, rootPath } = params;
		const context = IconConfigurationEditorModel.createValidationContext({
			rootPath,
			validationErrorModel
		});

		['matIconName', 'storageFileId'].forEach(keyField => {
			const control = formGroup?.get(keyField);
			control?.clearValidators();
			control?.addValidators(context.getValidation(keyField).validators);
		});
	}

}