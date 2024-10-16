import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { InternalStatus } from '@annotation-service/core/enum/internal-status.enum';
import { AnnotationStatus, AnnotationStatusPersist } from '@annotation-service/core/model/annotation-status.model';
import { Annotation } from '@annotation-service/core/model/annotation.model';
import { Status } from '@annotation-service/core/model/status.model';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { BaseEditorModel } from '@common/base/base-form-editor-model';
import { BackendErrorValidator } from '@common/forms/validation/custom-validator';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Validation, ValidationContext } from '@common/forms/validation/validation-context';
import { Guid } from '@common/types/guid';

export class AnnotationStatusArrayEditorModel {
	annotationsStatusArray: AnnotationStatusEditorModel [] = [];

	public validationErrorModel: ValidationErrorModel = new ValidationErrorModel();
	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor() { }

	public fromModel(item: Annotation[], listingStatuses: Status[]): AnnotationStatusArrayEditorModel {
		if (item) {
			item.forEach(annotation => {
				if (annotation.annotationStatuses){
					annotation.annotationStatuses = annotation.annotationStatuses.filter(x => x.isActive === IsActive.Active);
					annotation.annotationStatuses.forEach( x => {
						this.annotationsStatusArray.push(new AnnotationStatusEditorModel(this.validationErrorModel).fromModel(x, listingStatuses))
					})
				} else {
					const tempAnnotationStatus = {
						annotation: annotation,
						status: null
					} as AnnotationStatus;
					this.annotationsStatusArray.push(new AnnotationStatusEditorModel(this.validationErrorModel).fromModel(tempAnnotationStatus, listingStatuses))
				}
				
			})
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			annotationsStatusArray: this.formBuilder.array(
				(this.annotationsStatusArray ?? []).map(
					(item, index) => item.buildForm()
				), context.getValidation('annotationsStatusArray').validators
			),
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'annotationsStatusArray', validators: [BackendErrorValidator(this.validationErrorModel, 'annotationsStatusArray')] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}

export class AnnotationStatusEditorModel implements AnnotationStatusPersist {
	annotationId: Guid;
	statusId: Guid;
	isCheckIcon: boolean = false;

	protected formBuilder: UntypedFormBuilder = new UntypedFormBuilder();

	constructor(public validationErrorModel: ValidationErrorModel = new ValidationErrorModel()) {}

	public fromModel(item: AnnotationStatus, listingStatuses: Status[]): AnnotationStatusEditorModel {
		if (item) {
			this.annotationId = item.annotation?.id;
			this.statusId = item.status?.id;
			if (this.statusId) {
				const status = listingStatuses.find(x => x.id === this.statusId) || null;
				if (status && status.internalStatus != null && status.internalStatus == InternalStatus.Resolved){
					this.isCheckIcon = true;
				} else {
					this.isCheckIcon = false;
				}
			}
		}
		return this;
	}

	buildForm(context: ValidationContext = null, disabled: boolean = false): UntypedFormGroup {
		if (context == null) { context = this.createValidationContext(); }

		return this.formBuilder.group({
			annotationId: [{ value: this.annotationId, disabled: disabled }, context.getValidation('annotationId').validators],
			statusId: [{ value: this.statusId, disabled: disabled }, context.getValidation('statusId').validators],
			isCheckIcon: [{ value: this.isCheckIcon, disabled: disabled }, context.getValidation('isCheckIcon').validators],
		});
	}

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'annotationId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'label')] });
		baseValidationArray.push({ key: 'statusId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'internalStatus')] });
		baseValidationArray.push({ key: 'isCheckIcon', validators: [] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}
}
