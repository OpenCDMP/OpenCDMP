import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { BaseFormEditorModel } from '@common/base/base-form-editor-model';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@common/modules/notification/ui-notification-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export abstract class BaseFormEditor<FormModel, EditorModelType extends BaseFormEditorModel> extends BasePendingChangesComponent {

	isNew = true;
	isDeleted = false;
	editorModel: EditorModelType;
	serverData: FormModel; //To be used only for presentation purposes
	formGroup: FormGroup = null;

	abstract deleteItem(id: Guid);
	abstract refreshData(): void;
	abstract persistForm(onSuccess?: (response) => void): void;
	abstract buildForm(): void;

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
	) { super(); }

	public isFormValid() {
		return this.formGroup.valid;
	}

	public delete() {
		const value = this.formGroup.value;
		if (value.id) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '300px',
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.deleteItem(value.id);
				}
			});
		}
	}

	public cancel(): void {
		this.router.navigate(['/folders']);
	}

	onCallbackSuccess(data?: any): void {
		this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.refreshOnNavigateToData(data.id);
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);
			this.formService.validateAllFormFields(this.formGroup);
		} else {
			this.uiNotificationService.snackBarNotification(error.getMessagesString(), SnackBarNotificationLevel.Warning);
		}
	}

	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	formSubmit(): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			return;
		}

		this.persistForm(complete => {
			this.refreshOnNavigateToData(complete.id);
		});
	}

	private refreshOnNavigateToData(id: Guid): void {
		if (this.isNew) {
			this.formGroup.markAsPristine();
			this.router.navigate(['../' + (id)], { relativeTo: this.route });
		} else { this.internalRefreshData(); }
	}

	private internalRefreshData(): void {
		setTimeout(() => {
			this.formGroup = null;
			this.editorModel = null;
		});
		this.refreshData();
	}
}
