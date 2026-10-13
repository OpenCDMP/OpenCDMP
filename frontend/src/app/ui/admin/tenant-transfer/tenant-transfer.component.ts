import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { TenantCopyRequest, TenantPurgeReport, TenantPurgeRequest, TransferExportRequest, TransferImportReport } from '@app/core/model/tenant-transfer/tenant-transfer';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { TenantTransferService } from '@app/core/services/tenant-transfer/tenant-transfer.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { takeUntil } from 'rxjs/operators';
import { AdminPageHeaderComponent } from '../../../../common/ui/admin-page-header/admin-page-header.component';
import { MatTabGroup, MatTab } from '@angular/material/tabs';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatFormField, MatLabel, MatHint } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../common/forms/reactive-asterisk-directive';
import { MultipleAutoCompleteComponent } from '../../../library/auto-complete/multiple/multiple-auto-complete.component';
import { MatButton } from '@angular/material/button';
import { FileUploadComponent } from '../../../library/file-uploader/file-uploader.component';
import { MatInput } from '@angular/material/input';

@Component({
    selector: 'app-tenant-transfer',
    templateUrl: './tenant-transfer.component.html',
    styleUrls: ['./tenant-transfer.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [AdminPageHeaderComponent, MatTabGroup, MatTab, FormsModule, ReactiveFormsModule, MatCheckbox, MatFormField, MatLabel, ReactiveAsteriskDirective, MultipleAutoCompleteComponent, MatButton, FileUploadComponent, MatInput, MatHint]
})
export class TenantTransferComponent extends BaseComponent {

	exportForm: UntypedFormGroup;
	exportInProgress = false;

	importFile: File = null;
	report: TransferImportReport = null;
	analyzeInProgress = false;
	importInProgress = false;

	purgeForm: UntypedFormGroup;
	purgeReport: TenantPurgeReport = null;
	purgeConfirmationText = '';
	purgeAnalyzeInProgress = false;
	purgeInProgress = false;

	copyForm: UntypedFormGroup;
	copyReport: TransferImportReport = null;
	copyAnalyzeInProgress = false;
	copyInProgress = false;

	constructor(
		protected dialog: MatDialog,
		public language: TranslateService,
		private formBuilder: UntypedFormBuilder,
		private tenantTransferService: TenantTransferService,
		private uiNotificationService: UiNotificationService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private fileUtils: FileUtils,
		public authService: AuthService,
		public planService: PlanService,
		public planBlueprintService: PlanBlueprintService,
		public descriptionTemplateService: DescriptionTemplateService,
	) {
		super();
		this.exportForm = this.formBuilder.group({
			includeAllReferenceTypes: [true],
			includeAllPrefillingSources: [true],
			includeAllDescriptionTemplateTypes: [true],
			includeAllPlanBlueprintTypes: [true],
			includeAllStatusesAndWorkflows: [true],
			includeAllLanguages: [false],
			includeSupportiveMaterial: [false],
			includeAllDescriptionTemplates: [false],
			descriptionTemplateIds: [[]],
			includeAllPlanBlueprints: [false],
			planBlueprintIds: [[]],
			includeAllPlans: [false],
			planIds: [[]],
		});
		this.purgeForm = this.formBuilder.group({
			deletePlans: [false],
			deleteReferences: [false],
			deleteTags: [false],
			deleteDescriptionTemplates: [false],
			deletePlanBlueprints: [false],
			deleteReferenceTypes: [false],
			deletePrefillingSources: [false],
			deleteStatusesAndWorkflows: [false],
			deleteBlueprintTypes: [false],
			deleteTemplateTypes: [false],
			deleteStorageFiles: [false],
		});
		this.purgeForm.valueChanges.pipe(takeUntil(this._destroyed)).subscribe(() => this.enforcePurgeCascade());
		this.copyForm = this.formBuilder.group({
			sourceTenantCode: [''],
			includeAllReferenceTypes: [true],
			includeAllPrefillingSources: [true],
			includeAllDescriptionTemplateTypes: [true],
			includeAllPlanBlueprintTypes: [true],
			includeAllStatusesAndWorkflows: [true],
			includeAllLanguages: [false],
			includeSupportiveMaterial: [false],
			includeAllDescriptionTemplates: [false],
			descriptionTemplateIds: [[]],
			includeAllPlanBlueprints: [false],
			planBlueprintIds: [[]],
			includeAllPlans: [true],
			planIds: [[]],
		});
	}

	get currentTenantCode(): string {
		return this.authService.selectedTenant() || 'default';
	}

	get currentTenantName(): string {
		return this.authService.getSelectedTenantName() || this.currentTenantCode;
	}

	get canPurge(): boolean {
		return this.authService.hasPermission(AppPermission.PurgeTenantData);
	}

	get purgeSelectionEmpty(): boolean {
		const value = this.purgeForm.value;
		return !Object.keys(value).some(key => value[key] === true);
	}

	/** deleting a dependency forces deletion of everything that references it, ending at plans */
	private enforcePurgeCascade() {
		const value = { ...this.purgeForm.value };
		let changed = false;
		const enable = (key: string) => {
			if (value[key] !== true) { value[key] = true; changed = true; }
		};
		if (value.deleteBlueprintTypes) enable('deletePlanBlueprints');
		if (value.deleteTemplateTypes) enable('deleteDescriptionTemplates');
		if (value.deleteReferenceTypes) { enable('deleteReferences'); enable('deleteDescriptionTemplates'); enable('deletePlanBlueprints'); enable('deletePrefillingSources'); }
		if (value.deletePrefillingSources) enable('deletePlanBlueprints');
		if (value.deletePlanBlueprints || value.deleteDescriptionTemplates || value.deleteStatusesAndWorkflows || value.deleteReferences || value.deleteTags || value.deleteStorageFiles) enable('deletePlans');
		if (changed) this.purgeForm.patchValue(value, { emitEvent: false });
	}

	get copySourceValid(): boolean {
		const source = (this.copyForm.get('sourceTenantCode').value || '').trim();
		return source.length > 0 && source.toLowerCase() !== (this.currentTenantCode || '').toLowerCase();
	}

	private buildCopyRequest(): TenantCopyRequest {
		const request: TenantCopyRequest = { ...this.copyForm.value };
		request.sourceTenantCode = (request.sourceTenantCode || '').trim();
		request.planIds = null;
		request.planBlueprintIds = null;
		request.descriptionTemplateIds = null;
		return request;
	}

	analyzeCopy(ev: Event) {
		if (!this.copySourceValid) return;
		this.copyAnalyzeInProgress = true;
		this.tenantTransferService.analyzeCopy(this.buildCopyRequest()).pipe(takeUntil(this._destroyed)).subscribe({
			next: report => {
				this.copyAnalyzeInProgress = false;
				this.copyReport = report;
			},
			error: error => {
				this.copyAnalyzeInProgress = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	copy(ev: Event) {
		if (!this.copySourceValid) return;
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('TENANT-TRANSFER.SECTIONS.COPY.CONFIRMATION', { sourceTenantCode: this.copyForm.get('sourceTenantCode').value, tenantCode: this.currentTenantCode }),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) this.doCopy();
			});
	}

	private doCopy() {
		this.copyInProgress = true;
		this.tenantTransferService.copyTenant(this.buildCopyRequest()).pipe(takeUntil(this._destroyed)).subscribe({
			next: report => {
				this.copyInProgress = false;
				this.copyReport = report;
				this.uiNotificationService.snackBarNotification(this.language.instant('TENANT-TRANSFER.MESSAGES.COPY-SUCCESS'), SnackBarNotificationLevel.Success);
			},
			error: error => {
				this.copyInProgress = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	copyReportHasMessages(): boolean {
		return this.copyReport != null && this.copyReport.types != null && this.copyReport.types.some(x => x.messages != null && x.messages.length > 0);
	}

	analyzePurge(ev: Event) {
		this.purgeAnalyzeInProgress = true;
		this.tenantTransferService.analyzePurge(this.purgeForm.value as TenantPurgeRequest).pipe(takeUntil(this._destroyed)).subscribe({
			next: report => {
				this.purgeAnalyzeInProgress = false;
				this.purgeReport = report;
			},
			error: error => {
				this.purgeAnalyzeInProgress = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	purge(ev: Event) {
		if (this.purgeConfirmationText !== this.currentTenantCode) return;
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('TENANT-TRANSFER.SECTIONS.PURGE.CONFIRMATION', { tenantCode: this.currentTenantCode }),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) this.doPurge();
			});
	}

	private doPurge() {
		this.purgeInProgress = true;
		this.tenantTransferService.purge(this.purgeForm.value as TenantPurgeRequest).pipe(takeUntil(this._destroyed)).subscribe({
			next: report => {
				this.purgeInProgress = false;
				this.purgeReport = report;
				this.purgeConfirmationText = '';
				this.uiNotificationService.snackBarNotification(this.language.instant('TENANT-TRANSFER.MESSAGES.PURGE-SUCCESS'), SnackBarNotificationLevel.Success);
			},
			error: error => {
				this.purgeInProgress = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	export(ev: Event) {
		const request: TransferExportRequest = { ...this.exportForm.value };
		if (request.includeAllPlans) request.planIds = null;
		if (request.includeAllPlanBlueprints) request.planBlueprintIds = null;
		if (request.includeAllDescriptionTemplates) request.descriptionTemplateIds = null;

		this.exportInProgress = true;
		this.tenantTransferService.exportBundle(request).pipe(takeUntil(this._destroyed)).subscribe({
			next: response => {
				this.exportInProgress = false;
				const blob = new Blob([response.body], { type: 'application/octet-stream' });
				const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
				FileSaver.saveAs(blob, filename);
				this.uiNotificationService.snackBarNotification(this.language.instant('TENANT-TRANSFER.MESSAGES.EXPORT-SUCCESS'), SnackBarNotificationLevel.Success);
			},
			error: error => {
				this.exportInProgress = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	selectFile(event: File) {
		if (!event) return;
		this.importFile = event;
		this.report = null;
	}

	onRemoveFile() {
		this.importFile = null;
		this.report = null;
	}

	analyze(ev: Event) {
		if (!this.importFile) return;
		this.analyzeInProgress = true;
		this.tenantTransferService.analyzeImport(this.importFile).pipe(takeUntil(this._destroyed)).subscribe({
			next: report => {
				this.analyzeInProgress = false;
				this.report = report;
			},
			error: error => {
				this.analyzeInProgress = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	import(ev: Event) {
		if (!this.importFile) return;
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('TENANT-TRANSFER.CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
			},
			maxWidth: '30em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) this.doImport();
			});
	}

	private doImport() {
		this.importInProgress = true;
		this.tenantTransferService.importBundle(this.importFile).pipe(takeUntil(this._destroyed)).subscribe({
			next: report => {
				this.importInProgress = false;
				this.report = report;
				this.uiNotificationService.snackBarNotification(this.language.instant('TENANT-TRANSFER.MESSAGES.IMPORT-SUCCESS'), SnackBarNotificationLevel.Success);
			},
			error: error => {
				this.importInProgress = false;
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	reportHasMessages(): boolean {
		return this.report != null && this.report.types != null && this.report.types.some(x => x.messages != null && x.messages.length > 0);
	}
}
