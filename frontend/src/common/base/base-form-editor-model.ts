import { UntypedFormBuilder } from '@angular/forms';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { BaseEntity } from './base-entity.model';

export abstract class BaseEditorModel implements IEditorModel {
	id: Guid;
	isActive: IsActive;
	hash: string;
	createdAt: Date;
	updatedAt: Date;
	belongsToCurrentTenant?: boolean;

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	public fromModel(item: Partial<BaseEntity>): BaseEditorModel {
		if (item) {
			this.id = item.id;
			this.isActive = item.isActive;
			this.hash = item.hash;
			this.belongsToCurrentTenant = item.belongsToCurrentTenant;
			if (item.createdAt) { this.createdAt = item.createdAt; }
			if (item.updatedAt) { this.updatedAt = item.updatedAt; }
		}
		return this;
	}
}


export interface IEditorModel {
	validationErrorModel: ValidationErrorModel;
}
