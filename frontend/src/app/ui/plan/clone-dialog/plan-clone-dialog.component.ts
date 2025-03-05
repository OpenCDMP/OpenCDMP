import { Component, computed, HostBinding, Inject } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ClonePlanPersist, Plan } from '@app/core/model/plan/plan';
import { PlanService } from '@app/core/services/plan/plan.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';
import { catchError, exhaustMap, takeUntil, tap } from 'rxjs/operators';
import { PlanCloneDialogEditorModel } from './plan-clone-dialog.editor.model';
import { PlanEditorEntityResolver } from '../plan-editor-blueprint/resolvers/plan-editor-enitity.resolver';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, of, Subject } from 'rxjs';

@Component({
    selector: 'app-plan-clone-dialog',
    templateUrl: './plan-clone-dialog.component.html',
    styleUrls: ['./plan-clone-dialog.component.scss'],
    standalone: false
})
export class ClonePlanDialogComponent extends BaseComponent {
   

	plan: Plan;
	editorModel: PlanCloneDialogEditorModel;
	formGroup: UntypedFormGroup;
	isPublic: boolean = false;

	constructor(
		public dialogRef: MatDialogRef<ClonePlanDialogComponent>,
		private planService: PlanService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private language: TranslateService,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		super();
		this.plan = data.plan;
		this.isPublic = data.isPublic;
        this.clonePlan$.pipe(
            exhaustMap(() => this.confirm())
        ).subscribe(res => {});
	}


	get allDescriptionsNo(): number{
		return this.plan.descriptions?.length ?? 0;
	}

	get checkedDescrionsNo(): number {
		return this.formGroup.get('descriptions')?.value?.length ?? 0;
	}

	get allDescriptionsCompleted(): boolean {
		return this.allDescriptionsNo === this.checkedDescrionsNo;
	}

	get someDescriptionsCompleted(): boolean {
		return this.checkedDescrionsNo > 0 && this.checkedDescrionsNo < this.allDescriptionsNo;
	}

	ngOnInit() {
		this.editorModel = new PlanCloneDialogEditorModel().fromModel(this.data.plan);
		this.formGroup = this.editorModel.buildForm();
	}

	hasDescriptions() {
		return this.plan.descriptions?.length > 0;
	}

	close() {
		this.dialogRef.close(null);
	}

	cancel() {
		this.dialogRef.close(null);
	}

    clonePlan$: Subject<void> = new Subject();
	confirm(): Observable<Plan> {
		if (!this.formGroup.valid) { return; }
		const value: ClonePlanPersist = this.formGroup.value;

		if (this.isPublic) {
			return this.planService.publicClone(value, PlanEditorEntityResolver.lookupFields())
            .pipe(takeUntil(this._destroyed),
                catchError((error) => { this.onCallbackError(error); return of(null)}),
                tap((plan) => this.dialogRef.close(plan))
			);
		} else {
			return this.planService.clone(value, PlanEditorEntityResolver.lookupFields())
            .pipe(takeUntil(this._destroyed),
                catchError((error) => { this.onCallbackError(error); return of(null)}),
                tap((plan) => this.dialogRef.close(plan))
			);
		}
	}

	toggleAllDescriptions(event: any) {
		if (event === true) {
			this.formGroup.get('descriptions')?.setValue(this.plan.descriptions?.map(d=> d.id));
		} else {
			this.formGroup.get('descriptions')?.setValue([]);
		}
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		let errorOverrides = new Map<number, string>();
		errorOverrides.set(-1, errorResponse.error.message ? errorResponse.error.message : this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-UPDATE'));
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse, errorOverrides, SnackBarNotificationLevel.Error);
	}
}
