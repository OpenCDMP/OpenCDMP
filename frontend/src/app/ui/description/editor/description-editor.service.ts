import { Injectable } from "@angular/core";
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Guid } from "@common/types/guid";

@Injectable()
export class DescriptionEditorService {

	private validationErrorModel: ValidationErrorModel;

	public setValidationErrorModel(validationErrorModel: ValidationErrorModel): void {
		this.validationErrorModel = validationErrorModel;
	}

	public getValidationErrorModel(): ValidationErrorModel {
		return this.validationErrorModel;
	}
}
