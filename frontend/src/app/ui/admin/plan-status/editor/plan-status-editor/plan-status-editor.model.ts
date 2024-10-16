import { FormArray, FormControl, FormGroup, Validators } from "@angular/forms";
import { AppRole } from "@app/core/common/enum/app-role";
import { AppPermission } from "@app/core/common/enum/permission.enum";
import { PlanStatusEnum } from "@app/core/common/enum/plan-status";
import { PlanUserRole } from "@app/core/common/enum/plan-user-role";
import { PlanStatus, PlanStatusDefinition, PlanStatusDefinitionAuthorizationItem } from "@app/core/model/plan-status/plan-status";
import { PlanStatusPersist } from "@app/core/model/plan-status/plan-status-persist";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class PlanStatusEditorModel extends BaseEditorModel implements PlanStatusPersist {
    name: string;
    description: string;
    internalStatus: PlanStatusEnum;
    definition: PlanStatusDefinition;

    public fromModel(item: PlanStatus): PlanStatusEditorModel {
		if (item) {
			super.fromModel(item);
			this.name = item?.name;
            this.description = item?.description;
            this.internalStatus = item?.internalStatus;
            this.definition = item?.definition;
		}
		return this;
	}

	buildForm(params: {context?: ValidationContext, disabled?: boolean}): FormGroup<PlanStatusForm> {
        const {context = this.createValidationContext(), disabled = false} = params;

		return this.formBuilder.group({
			id: [{ value: this.id, disabled }, context.getValidation('id').validators],
			name: [{value: this.name, disabled}, context.getValidation('name').validators],
			description: [{value: this.description, disabled}, context.getValidation('description').validators],
			internalStatus: [{value: this.internalStatus, disabled}, context.getValidation('internalStatus').validators],
			hash: [{ value: this.hash, disabled: disabled }, context.getValidation('hash').validators],
			definition: this.buildDefinitionForm({context, disabled}),
		});
	}

    buildDefinitionForm(params: {context: ValidationContext, disabled: boolean}): FormGroup<PlanStatusDefinitionForm> {
        const {context = this.createValidationContext(), disabled} = params;
        const definitionForm = new FormGroup<PlanStatusDefinitionForm>({
            authorization: new FormGroup<PlanStatusDefinitionAuthorizationForm>({
                edit: this.buildDefinitionAuthorizationItemForm({item: this.definition?.authorization?.edit, rootPath: 'edit', disabled})
            })
        });
        definitionForm.controls.authorization.addValidators(context.getValidation('authorization').validators);
        definitionForm.addValidators(context.getValidation('definition').validators);
        return definitionForm;
    }

    buildDefinitionAuthorizationItemForm(params: {item: PlanStatusDefinitionAuthorizationItem, rootPath: string, disabled: boolean}): FormGroup<PlanStatusDefinitionAuthorizationItemForm>{
        const {item, rootPath, disabled} = params;
        const context = this.createAuthorizationItemContext(rootPath);
        return new FormGroup<PlanStatusDefinitionAuthorizationItemForm>({
            allowAnonymous: new FormControl({value: item?.allowAnonymous ?? false, disabled}, context.getValidation('allowAnonymous').validators),
            allowAuthenticated: new FormControl({value: item?.allowAuthenticated ?? false, disabled}, context.getValidation('allowAuthenticated').validators),
            roles: new FormControl({value: item?.roles ?? [], disabled}, context.getValidation('roles').validators),
            planRoles: new FormControl({value: item?.planRoles ?? [], disabled}, context.getValidation('planRoles').validators),
        })
    }

	createValidationContext(): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, 'id')] });
		baseValidationArray.push({ key: 'name', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, 'name')] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, 'description')] });
		baseValidationArray.push({ key: 'internalStatus', validators: [BackendErrorValidator(this.validationErrorModel, 'internalStatus')] });
		baseValidationArray.push({ key: 'hash', validators: [] });
		baseValidationArray.push({ key: 'definition', validators: [BackendErrorValidator(this.validationErrorModel, 'definition')] });
		baseValidationArray.push({ key: 'authorization', validators: [BackendErrorValidator(this.validationErrorModel, 'definition.authorization')] });
		baseValidationArray.push({ key: 'edit', validators: [BackendErrorValidator(this.validationErrorModel, 'definition.authorization.edit')] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

    createAuthorizationItemContext(rootPath?: string): ValidationContext {
        const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
        baseValidationArray.push({ key: 'allowAnonymous', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}allowAnonymous`)] });
		baseValidationArray.push({ key: 'allowAuthenticated', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}allowAuthenticated`)] });
		baseValidationArray.push({ key: 'planRoles', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}planRoles`)] });
		baseValidationArray.push({ key: 'roles', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}roles`)] });

        baseContext.validation = baseValidationArray;
		return baseContext;
    }

}

export interface PlanStatusForm {
    id: FormControl<Guid>;
    hash: FormControl<string>;
    name: FormControl<string>;
    description: FormControl<string>;
    internalStatus: FormControl<PlanStatusEnum>;
    definition: FormGroup<PlanStatusDefinitionForm>;
}

export interface PlanStatusDefinitionForm {
    authorization: FormGroup<PlanStatusDefinitionAuthorizationForm>
}

export interface PlanStatusDefinitionAuthorizationForm {
    edit: FormGroup<PlanStatusDefinitionAuthorizationItemForm>;
}

export interface PlanStatusDefinitionAuthorizationItemForm {
    roles: FormControl<AppRole[]>;
    planRoles: FormControl<PlanUserRole[]>;
    allowAuthenticated: FormControl<boolean>;
    allowAnonymous: FormControl<boolean>;
}