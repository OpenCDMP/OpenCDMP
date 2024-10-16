import { Injectable } from "@angular/core";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";

@Injectable()
export class UserRoleEditorService {
	private validationErrorModel: ValidationErrorModel;

	public setValidationErrorModel(validationErrorModel: ValidationErrorModel): void {
		this.validationErrorModel = validationErrorModel;
	}

	public getValidationErrorModel(): ValidationErrorModel {
		return this.validationErrorModel;
	}
}
