import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { Plan } from '@app/core/model/plan/plan';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { takeUntil } from 'rxjs/operators';


@Component({
	selector: 'app-plan-finalize-dialog-component',
	templateUrl: 'plan-finalize-dialog.component.html',
	styleUrls: ['./plan-finalize-dialog.component.scss']
})
export class PlanFinalizeDialogComponent extends BaseComponent implements OnInit {

	plan: Plan;
	isPlanValid: boolean;
	planErrors: string[];
	planAccessTypeEnum = PlanAccessType;
	descriptionStatusEnum = DescriptionStatusEnum;
	descriptionValidationOutputEnum = DescriptionValidationOutput;
	validationResults: DescriptionValidationResult[] = [];
	descriptionsToBeFinalized: Guid[] = [];
	descriptionValidationOutputMap = new Map<Guid, DescriptionValidationOutput>();

	constructor(
		public router: Router,
		public dialogRef: MatDialogRef<PlanFinalizeDialogComponent>,
		public descriptionService: DescriptionService,
		private planService: PlanService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		super();
		this.plan = data.plan;
	}

	ngOnInit(): void {
		this.planService.validate(this.plan.id).pipe(takeUntil(this._destroyed),)
		.subscribe(result => {
			if (result.result === PlanValidationOutput.Valid){
				this.validateDescriptions(this.plan);
				this.isPlanValid = true;
			}else{
				this.isPlanValid = false;
				this.planErrors = result.errors;
			}
		},
		error => {
			this.dialogRef.close();
			this.httpErrorHandlingService.handleBackedRequestError(error)
		});
	}

	isDescriptionValid(descriptionId: Guid): boolean {
		const result = this.validationResults.find(x => x.descriptionId == descriptionId)?.result === DescriptionValidationOutput.Valid;
		if (result){
			this.descriptionValidationOutputMap.set(descriptionId, DescriptionValidationOutput.Valid);
		} else{
			this.descriptionValidationOutputMap.set(descriptionId, DescriptionValidationOutput.Invalid);
		}
		return result;
	}

	onSubmit() {
		this.dialogRef.close({ descriptionsToBeFinalized: this.descriptionsToBeFinalized } as PlanFinalizeDialogOutput);
	}

	getFinalizedDescriptions() {
		if (!this.plan.descriptions) return [];
		const finalizedDescriptions = this.plan.descriptions.filter(x => x.status === DescriptionStatusEnum.Finalized);
		if (finalizedDescriptions?.length > 0){
			finalizedDescriptions.forEach(finalize => {
				this.descriptionValidationOutputMap.set(finalize.id, DescriptionValidationOutput.Valid);
			})
		}
		return finalizedDescriptions;
	}

	close() {
		this.dialogRef.close({ cancelled: true } as PlanFinalizeDialogOutput);
	}

	validateDescriptions(plan: Plan) {
		if (!plan.descriptions?.some(x => x.status == DescriptionStatusEnum.Draft)) return;

		const draftDescriptions = this.plan.descriptions.filter(x => x.status == DescriptionStatusEnum.Draft) || [];
		if ( draftDescriptions.length > 0){
			draftDescriptions.forEach(draft => {
				this.descriptionValidationOutputMap.set(draft.id, DescriptionValidationOutput.Pending);
			});
		}

		this.descriptionService.validate(plan.descriptions.filter(x => x.status == DescriptionStatusEnum.Draft).map(x => x.id)).pipe(takeUntil(this._destroyed),)
		.subscribe(result => {
			this.validationResults = result;
		},
		error => {
			this.dialogRef.close();
			this.httpErrorHandlingService.handleBackedRequestError(error)
		});
	}

	get validDraftDescriptions() {
		if (!this.plan.descriptions) return [];
		return this.plan.descriptions.filter(x => this.validationResults.some(y => y.descriptionId == x.id && y.result == DescriptionValidationOutput.Valid));
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
	descriptionsToBeFinalized?: Guid[];
}
