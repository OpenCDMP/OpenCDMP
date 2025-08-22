import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanBlueprint, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { BaseComponent } from '@common/base/base.component';
import { map, takeUntil } from 'rxjs/operators';
import { PlanImportRdaConfigEditorModel } from './plan-common-model-config.editor.model';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { FormService } from '@common/forms/form-service';
import { PlanCommonModelConfig } from '@app/core/model/plan/plan-import';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TranslateService } from '@ngx-translate/core';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { FileTransformerConfiguration } from '@app/core/model/file/file-transformer-configuration.model';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
@Component({
    selector: 'plan-upload-dialog',
    templateUrl: './plan-upload-dialog.component.html',
    styleUrls: ['./plan-upload-dialog.component.scss'],
    standalone: false
})
export class PlanUploadDialogComponent extends BaseComponent {
   

	planTitle: string;
	planBlueprints: any[] = [];
	file: File;
	selectedBlueprintSections: PlanBlueprintDefinitionSection[];
	formGroup: UntypedFormGroup;
	fileTransformerWithJson: FileTransformerConfiguration[] = [];
	loadSpiner = false;
	storageFile: StorageFile = null;

	descriptionTemplateSingleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildAutocompleteLookup(null, null, null, [DescriptionTemplateVersionStatus.Current], [DescriptionTemplateStatus.Finalized])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildAutocompleteLookup(searchQuery, null, null, [DescriptionTemplateVersionStatus.Current], [DescriptionTemplateStatus.Finalized])).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildAutocompleteLookup(null, null, [selectedItem], [DescriptionTemplateVersionStatus.Current], [DescriptionTemplateStatus.Finalized])).pipe(map(x => x.items[0])),
		displayFn: (item: DescriptionTemplate) => item.label,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: (item: DescriptionTemplate) => item.id,
		popupItemActionIcon: 'visibility'
	};


	constructor(
		public dialogRef: MatDialogRef<PlanUploadDialogComponent>,
		private dialog: MatDialog,
		private analyticsService: AnalyticsService,
		private formService: FormService,
		public descriptionTemplateService: DescriptionTemplateService,
		public planBlueprintService: PlanBlueprintService,
		private planService: PlanService,
		private storageFileStorage: StorageFileService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private fileTransformerService: FileTransformerService,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanUploadDialog);
	}

	cancel() {
		this.data.success = false;
		this.dialogRef.close(this.data);
	}

	close() {
		this.dialogRef.close(false);
	}

	confirm() {
		this.data.success = true;
		this.data.planTitle = this.planTitle;
		this.data.planBlueprint = this.planBlueprints;
        this.data.file = this.file;
        
		if (this.file?.type?.includes('/json') && this.formGroup){
			this.formService.removeAllBackEndErrors(this.formGroup);
			this.formService.touchAllFormFields(this.formGroup);
			if (this.formGroup.valid){
				this.data.planCommonModelConfig = this.formService.getValue(this.formGroup.value) as PlanCommonModelConfig;
				this.data.planCommonModelConfig.file = this.file;
			} else {
				return;
			}
		}
		this.dialogRef.close(this.data);
	}

	disableConfirmButton(){
		if (!this.file) return true;
		if ((this.file.type.includes('/json') && (this.formGroup == null || !this.formGroup.valid)) || (this.file.type.includes('/xml') && this.planTitle?.length == 0)) return true;
		return false;
	}

	uploadFile(event: File) {
    	this.file = event;
		this.planTitle = event.name

		if (this.file?.type?.includes('/json')){
			this.storageFileStorage.uploadTempFiles(this.file)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				(storageFile) => {
					if (storageFile.length > 0 ){
						this.storageFile = storageFile[0];
						this.fileTransformerWithJson = this.fileTransformerService.availableImportFormatsFor(FileTransformerEntityType.Plan)?.filter(x => x.importVariants?.filter(y => y.format?.toLowerCase() === 'json')) || [];
						if (this.fileTransformerWithJson?.length == 1) {
							this.preprocessingPlan(this.fileTransformerWithJson[0].fileTransformerId);
						}
					}
					
				},
				(error) => this.onCallbackEror(error.error)
			);
			
		}
	}

	repoChanged(repositoryId: string) {
		this.preprocessingPlan(repositoryId);
	}

	preprocessingPlan(repositoryId: string) {
		if (this.storageFile?.id && repositoryId?.length > 0) {
			this.loadSpiner = true;
			this.planService.preprocessingPlan(this.storageFile?.id, repositoryId)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				(preprocessingData) => {
					this.formGroup = new PlanImportRdaConfigEditorModel().fromModel(preprocessingData, this.storageFile?.id, repositoryId).buildForm();
					if (this.formGroup != null) this.loadSpiner = false;
				},
				(error) => {
					this.httpErrorHandlingService.handleBackedRequestError(error)
					this.close();
				}
			);
		}
	}

	onRemove() {
		this.file = null;
		this.planTitle = null;
		this.formGroup = null;
	}

	selectedBlueprintChanged(item: PlanBlueprint): void{
		this.selectedBlueprintSections = item.definition?.sections?.filter(x => x.hasTemplates) || null;
		if (this.formGroup){
			const descriptionsFormArray = this.formGroup.get('descriptions') as UntypedFormArray;
			descriptionsFormArray.controls.forEach( control =>{
				control.get('sectionId').patchValue(null);
			})
		}
	}

	onPreviewDescriptionTemplate(event, descriptionIndex: number) {
		const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
			width: '590px',
			minHeight: '200px',
			restoreFocus: false,
			data: {
				descriptionTemplateId: event.id
			},
			panelClass: 'custom-modalbox'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(descTemplate => {
			if (descTemplate) {
				(this.formGroup.get('descriptions') as UntypedFormArray).at(descriptionIndex).get('templateId').patchValue(event.id);
			}
		});
	}

	hasFile(): boolean {
		return !!this.file;
	}

	private onCallbackEror(error: any) {
		this.uiNotificationService.snackBarNotification(this.language.instant(error.error), SnackBarNotificationLevel.Error);
		this.close();
	}
}
