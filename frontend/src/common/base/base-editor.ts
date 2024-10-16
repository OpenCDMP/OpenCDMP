import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { ResponseErrorCode } from '@app/core/common/enum/respone-error-code';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { BaseEntity } from '@common/base/base-entity.model';
import { BasePendingChangesComponent } from '@common/base/base-pending-changes.component';
import { FormService } from '@common/forms/form-service';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { TranslateService } from '@ngx-translate/core';
import { isNullOrUndefined } from '@swimlane/ngx-datatable';
import { Observable, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { Guid } from '../types/guid';
import { BaseEditorModel } from './base-form-editor-model';

@Component({
	selector: 'app-base-editor-component',
	template: ''
})
export abstract class BaseEditor<EditorModelType extends BaseEditorModel, EntityType> extends BasePendingChangesComponent implements OnInit {

	isNew = true;
	isDeleted = false;
	isLocked: Boolean = false;
	isLockedByUser: Boolean = false;
	formGroup: UntypedFormGroup = null;
	lookupParams: any;

	// Getter Setter for editorModel. We use it to notify when the editor model is changed.
	get editorModel(): EditorModelType { return this._editorModel; }
	set editorModel(value: EditorModelType) { this._editorModel = value; }
	private _editorModel: EditorModelType;
	protected lv = 0;


	abstract getItem(itemId: Guid, successFunction: (item: EntityType) => void): void;
	abstract prepareForm(data: EntityType): void;
	abstract formSubmit(): void;
	abstract delete(): void;
	abstract refreshData(): void;
	abstract refreshOnNavigateToData(id?: Guid): void;
	abstract persistEntity(onSuccess?: (response) => void): void;
	abstract buildForm(): void;

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected router: Router,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected filterService: FilterService,
		protected route: ActivatedRoute,
		protected queryParamsService: QueryParamsService,
		protected lockService: LockService,
		protected authService: AuthService,
		protected configurationService: ConfigurationService,
	) { super(); }

	public ngOnInit(): void {
		const entity = this.route.snapshot.data['entity'] as EntityType;
		if (entity) {
			this.isNew = false;
			this.prepareForm(entity);
			if (this.formGroup && this.editorModel.belongsToCurrentTenant == false) {
				this.formGroup.disable();
			}
		} else {
			this.prepareForm(null);
		}

		this.route.queryParamMap.pipe(takeUntil(this._destroyed)).subscribe((params: ParamMap) => {
			// If lookup is on the query params load it
			if (params.keys.length > 0 && params.has('lookup')) {
				this.lookupParams = this.queryParamsService.deSerializeLookup(params.get('lookup'));
			}
		});
	}

	public isFormValid() {
		return this.formGroup.valid;
	}

	public isFormDisabled() {
		return this.formGroup.disabled;
	}

	public save() {
		this.clearErrorModel();
	}

	public cancel(): void {
		this.router.navigate(['..'], { relativeTo: this.route, queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true });// ! lv is always zero . replaceUrl?
	}



	onCallbackSuccess(data?: any): void {
		this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
		this.refreshOnNavigateToData(data ? data.id : null);
	}

	onCallbackDeleteSuccess(data?: any): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.cancel();
	}

	onCallbackError(errorResponse: HttpErrorResponse) {
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse);

		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
		if (error.statusCode === 400) {
			this.editorModel.validationErrorModel.fromJSONObject(errorResponse.error);

			if (errorResponse.error.code === ResponseErrorCode.PlanDescriptionTemplateCanNotRemove) {
				this.refreshOnNavigateToData(null);
			}

			this.formService.validateAllFormFields(this.formGroup);
		}
	}

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

	internalRefreshData(): void {
		this.refreshData();
	}



	canDeactivate(): boolean | Observable<boolean> {
		return this.formGroup ? !this.formGroup.dirty : true;
	}

	public static commonFormFieldNames(): string[] {
		return [
			nameof<BaseEntity>(x => x.id),
			nameof<BaseEntity>(x => x.isActive),
			nameof<BaseEntity>(x => x.createdAt),
			nameof<BaseEntity>(x => x.updatedAt),
			nameof<BaseEntity>(x => x.hash),
			nameof<BaseEntity>(x => x.belongsToCurrentTenant),
		];
	}

	//
	//
	// Lock
	//
	//
	protected checkLock(itemId: Guid, targetType: LockTargetType, title: string, message: string) {
		if (itemId != null) {
			this.isNew = false;
			// check if locked.
			this.lockService.checkLockStatus(itemId).pipe(takeUntil(this._destroyed)).subscribe(lockStatus => { //TODO HANDLE-ERRORS
				this.isLocked = lockStatus.status;
				if (this.isLocked) {
					this.formGroup.disable();
					this.dialog.open(PopupNotificationDialogComponent, {
						data: {
							title: this.language.instant(title),
							message: this.language.instant(message)
						}, maxWidth: '30em'
					});
				}

				if (!this.isLocked && !isNullOrUndefined(this.authService.currentAccountIsAuthenticated())) {
					// lock it.
					this.lockService.lock(itemId, targetType).pipe(takeUntil(this._destroyed)).subscribe(async result => { //TODO HANDLE-ERRORS
						this.isLockedByUser = true;
						interval(this.configurationService.lockInterval).pipe(takeUntil(this._destroyed)).subscribe(() => this.touchLock(itemId));
					});
				}
			});
		}
	}

	private unlockTarget(targetId: Guid) {
		this.lockService.unlockTarget(targetId).pipe(takeUntil(this._destroyed)).subscribe(async result => { }); //TODO HANDLE-ERRORS
	}

	private touchLock(targetId: Guid) {
		this.lockService.touchLock(targetId).pipe(takeUntil(this._destroyed)).subscribe(async result => { }); //TODO HANDLE-ERRORS
	}

	ngOnDestroy(): void {
		super.ngOnDestroy();
		if (this.isLockedByUser) this.unlockTarget(this.editorModel.id);
	}
}
