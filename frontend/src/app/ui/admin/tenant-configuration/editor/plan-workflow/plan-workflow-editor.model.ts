import { FormArray, FormControl, FormGroup, ValidatorFn, Validators } from "@angular/forms";
import { PlanWorkflow } from "@app/core/model/workflow/plan-workflow";
import { PlanWorkflowDefinitionPersist, PlanWorkflowPersist } from "@app/core/model/workflow/plan-workflow-persist";
import { BaseEditorModel } from "@common/base/base-form-editor-model";
import { BackendErrorValidator } from "@common/forms/validation/custom-validator";
import { Validation, ValidationContext } from "@common/forms/validation/validation-context";
import { Guid } from "@common/types/guid";

export class PlanWorkflowEditorModel extends BaseEditorModel implements PlanWorkflowPersist {
    name: string = 'default';
    description: string;
    definition: PlanWorkflowDefinitionPersist;
    
    public fromModel(item: PlanWorkflow): PlanWorkflowEditorModel {
        if(item){
            super.fromModel(item);
            this.description = item.description;
            this.definition = {
                startingStatusId: item.definition?.startingStatus?.id ?? null,
                statusTransitions: []
            };
            item.definition?.statusTransitions?.forEach((st) => 
                this.definition.statusTransitions.push({
                    fromStatusId: st.fromStatus?.id,
                    toStatusId: st.toStatus?.id
                })
            )
        }

        return this;
    }

    buildForm(params: {context?: ValidationContext, disabled?: boolean}): FormGroup<PlanWorkflowForm> {
        const {context, disabled = false} = params;
        const mainContext = context ?? this.createValidationContext();
        const formGroup = new FormGroup<PlanWorkflowForm>({
            id: new FormControl({value: this.id, disabled }, mainContext.getValidation('id').validators),
            hash: new FormControl({value: this.hash, disabled }, mainContext.getValidation('hash').validators),
            name: new FormControl({value: this.name, disabled}, mainContext.getValidation('name').validators),
            description: new FormControl({value: this.description, disabled}, mainContext.getValidation('description').validators),
            definition: new FormGroup<PlanWorkflowDefinitionForm>({
                startingStatusId: new FormControl({value: this.definition?.startingStatusId, disabled}, mainContext.getValidation('startingStatusId').validators),
                statusTransitions: new FormArray<FormGroup<PlanWorkflowDefinitionTransitionForm>>([], mainContext.getValidation('statusTransitions').validators)
            }, mainContext.getValidation('definition').validators)
        })

        this.definition?.statusTransitions?.forEach((st, index) => {
            const itemContext = context ?? this.createValidationContext(`StatusTransition[${index}]`)
            formGroup.controls.definition.controls.statusTransitions.push(new FormGroup({
                fromStatusId: new FormControl({value: st.fromStatusId,disabled}, itemContext.getValidation('fromStatusId').validators),
                toStatusId: new FormControl({value: st.toStatusId,disabled}, itemContext.getValidation('fromStatusId').validators)
            }, this.differentStatusId()))
        })

        return formGroup;
    }

    buildStatusTransitionForm(index: number): FormGroup<PlanWorkflowDefinitionTransitionForm> {
        const itemContext = this.createValidationContext(`StatusTransition[${index}]`)
        return new FormGroup({
            fromStatusId: new FormControl(null, itemContext.getValidation('fromStatusId').validators),
            toStatusId: new FormControl(null, itemContext.getValidation('toStatusId').validators)
        }, this.differentStatusId())
    }

    reApplyDefinitionValidators(formGroup: FormGroup<PlanWorkflowDefinitionForm>) {
        if(!formGroup?.controls?.statusTransitions?.length) { return; }
        formGroup.controls.statusTransitions.controls.forEach((st, index) => {
            const context = this.createValidationContext(`StatusTransition[${index}]`);
            st.clearValidators();
            st.addValidators(this.differentStatusId());
            st.controls.fromStatusId.clearValidators();
            st.controls.fromStatusId.addValidators(context.getValidation('fromStatusId').validators);
            st.controls.toStatusId.clearValidators();
            st.controls.toStatusId.addValidators(context.getValidation('toStatusId').validators);
        })
    }

    createValidationContext(rootPath?: string): ValidationContext {
		const baseContext: ValidationContext = new ValidationContext();
		const baseValidationArray: Validation[] = new Array<Validation>();
		baseValidationArray.push({ key: 'id', validators: [BackendErrorValidator(this.validationErrorModel, `${rootPath}id`)] });
		baseValidationArray.push({ key: 'name', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}name`)] });
		baseValidationArray.push({ key: 'description', validators: [BackendErrorValidator(this.validationErrorModel, `${rootPath}description`)] });
		baseValidationArray.push({ key: 'hash', validators: [] });
		baseValidationArray.push({ key: 'definition', validators: [BackendErrorValidator(this.validationErrorModel, `${rootPath}definition`)] });
		baseValidationArray.push({ key: 'statusTransitions', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}statusTransitions`)] });
		baseValidationArray.push({ key: 'startingStatusId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}startingStatusId`)] });
		baseValidationArray.push({ key: 'fromStatusId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}fromStatusId`)] });
		baseValidationArray.push({ key: 'toStatusId', validators: [Validators.required, BackendErrorValidator(this.validationErrorModel, `${rootPath}toStatusId`)] });

		baseContext.validation = baseValidationArray;
		return baseContext;
	}

    differentStatusId(): ValidatorFn {
        return (formGroup: FormGroup<PlanWorkflowDefinitionTransitionForm>): { [key: string]: any } => {
            const fromStatusId = formGroup?.controls?.fromStatusId?.value;
            const toStatusId = formGroup?.controls?.toStatusId?.value;
            if(!fromStatusId || !toStatusId){
                return null;
            }
            return fromStatusId?.toString().toLowerCase() == toStatusId?.toString()?.toLowerCase() ? 
            {'differentStatusId': true} : null
        }
    }
}

export interface PlanWorkflowForm {
    id: FormControl<Guid>;
    hash: FormControl<string>;
    name: FormControl<string>;
    description: FormControl<string>;
    definition: FormGroup<PlanWorkflowDefinitionForm>;
}

export interface PlanWorkflowDefinitionForm {
    startingStatusId: FormControl<Guid>;
    statusTransitions: FormArray<FormGroup<PlanWorkflowDefinitionTransitionForm>>
}

export interface PlanWorkflowDefinitionTransitionForm {
    fromStatusId: FormControl<Guid>;
    toStatusId: FormControl<Guid>;
}