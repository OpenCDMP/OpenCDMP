import { Component, computed, HostBinding, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { Description } from '@app/core/model/description/description';
import { Plan } from '@app/core/model/plan/plan';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { takeUntil } from 'rxjs/operators';
import { DescriptionInfo } from '../plan-editor-blueprint/plan-temp-storage.service';
import { DescriptionEditorHelper } from '../plan-editor-blueprint/plan-description-editor/plan-description-editor-helper';
import { FormService } from '@common/forms/form-service';


@Component({
    selector: 'app-plan-finalize-dialog-component',
    templateUrl: 'plan-finalize-dialog.component.html',
    styleUrls: ['./plan-finalize-dialog.component.scss'],
    standalone: false
})
export class PlanFinalizeDialogComponent extends BaseComponent implements OnInit {
   

	plan: Plan;
    descriptions: Description[];
    descriptionMetadata: DescriptionInfo[];
	isPlanValid: boolean;
	planErrors: string[];
	planAccessTypeEnum = PlanAccessType;
	descriptionStatusEnum = DescriptionStatusEnum;
	descriptionValidationOutputEnum = DescriptionValidationOutput;
	validationResults: DescriptionValidationResult[] = [];
	descriptionsToBeFinalized: Guid[] = [];
	draftDescriptionValidationOutputMap = new Map<Guid, DescriptionValidationOutput>();
    draftDescriptionErrorMap = new Map<Guid, string[]>();

	constructor(
		public router: Router,
		public dialogRef: MatDialogRef<PlanFinalizeDialogComponent>,
		public descriptionService: DescriptionService,
		private planService: PlanService,
		private httpErrorHandlingService: HttpErrorHandlingService,
        private formService: FormService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		super();
		this.plan = data.plan;
        this.descriptionMetadata = data.descriptionMetadata;
        this.descriptions = this.descriptionMetadata ? this.descriptionMetadata.map((x) => x.lastPersist) : this.plan.descriptions;
        this.dialogRef.backdropClick().subscribe(() => this.dialogRef.close({
            planValid: this.isPlanValid,
            cancelled: true
        } as PlanFinalizeDialogOutput))
	}

	ngOnInit(): void {
		this.planService.validate(this.plan.id).pipe(takeUntil(this._destroyed),)
		.subscribe(result => {
			if (result.result === PlanValidationOutput.Valid){
				this.isPlanValid = true;
			}else{
				this.isPlanValid = false;
				this.planErrors = result.errors;
			}
            this.validateDescriptions();
		},
		error => {
			this.dialogRef.close({planValid: this.isPlanValid});
			this.httpErrorHandlingService.handleBackedRequestError(error)
		});
	}

	isDescriptionValid(descriptionId: Guid): boolean {
		const result = this.validationResults.find(x => x.descriptionId == descriptionId)?.result === DescriptionValidationOutput.Valid;
		if (result){
			this.draftDescriptionValidationOutputMap.set(descriptionId, DescriptionValidationOutput.Valid);
		} else{
			this.draftDescriptionValidationOutputMap.set(descriptionId, DescriptionValidationOutput.Invalid);
		}
		return result;
	}

	onSubmit() {
		this.dialogRef.close({ planValid: true, descriptionsToBeFinalized: this.descriptionsToBeFinalized } as PlanFinalizeDialogOutput);
	}

	getFinalizedDescriptions() {
		if (!this.descriptions) return [];
		const finalizedDescriptions = this.descriptions.filter(x => x.status.internalStatus === DescriptionStatusEnum.Finalized);
		if (finalizedDescriptions?.length > 0){
			finalizedDescriptions.forEach(finalize => {
				this.draftDescriptionValidationOutputMap.set(finalize.id, DescriptionValidationOutput.Valid);
			})
		}
		return finalizedDescriptions;
	}

	close() {
		this.dialogRef.close({ cancelled: true, planValid: this.isPlanValid } as PlanFinalizeDialogOutput);
	}

	validateDescriptions() {
		if (!this.descriptions?.some(x => x.status.internalStatus == DescriptionStatusEnum.Draft)) return;
        if(this.descriptionMetadata){
            const draftDescriptions = this.descriptionMetadata.filter(x => x.lastPersist.status.internalStatus == DescriptionStatusEnum.Draft) || [];
            draftDescriptions.forEach(draft => {
                this.formService.removeAllBackEndErrors(draft.formGroup);
                this.formService.touchAllFormFields(draft.formGroup);
                this.formService.validateAllFormFields(draft.formGroup);

                const isValid = draft.formGroup.valid
                this.draftDescriptionValidationOutputMap.set(draft.lastPersist.id, isValid ? DescriptionValidationOutput.Valid : DescriptionValidationOutput.Invalid);

                if(!isValid){
                    this.draftDescriptionErrorMap.set(draft.lastPersist.id, DescriptionEditorHelper.getDescriptionErrors({
                        formGroup: draft.formGroup, 
                        htmlMapping: (id: string) => `${id}-label`
                    }));
                }
            });
        }else {
            const draftDescriptions = this.descriptions.filter(x => x.status.internalStatus == DescriptionStatusEnum.Draft) || [];
            if ( draftDescriptions.length > 0){
                draftDescriptions.forEach(draft => {
                    this.draftDescriptionValidationOutputMap.set(draft.id, DescriptionValidationOutput.Pending);
                });
            }
    
            this.descriptionService.validate(this.plan.descriptions.filter(x => x.status.internalStatus == DescriptionStatusEnum.Draft).map(x => x.id)).pipe(takeUntil(this._destroyed),)
            .subscribe({
                next: (result) => {
                    this.validationResults = result;
                    this.validationResults.forEach((res) => this.draftDescriptionValidationOutputMap.set(res.descriptionId, res.result))
                },
                error: (error) => {
                    this.dialogRef.close();
                    this.httpErrorHandlingService.handleBackedRequestError(error)
                }
            });
        }

	}

	get validDraftDescriptions() {
		if (!this.descriptions) return [];
		return this.descriptions.filter(x => x.status.internalStatus === DescriptionStatusEnum.Draft && this.draftDescriptionValidationOutputMap.get(x.id) === DescriptionValidationOutput.Valid);
	}
}

export interface DescriptionValidationResult {
	descriptionId: Guid;
	result: DescriptionValidationOutput;
}

export enum DescriptionValidationOutput {
	Valid = 1,
	Invalid = 2,
	Pending = 3
}

export interface PlanValidationResult {
	id: Guid;
	result: PlanValidationOutput;
	errors: string[];
}

export enum PlanValidationOutput {
	Valid = 1,
	Invalid = 2
}


export interface PlanFinalizeDialogOutput {
	cancelled?: boolean;
    planValid?: boolean
	descriptionsToBeFinalized?: Guid[];
}
