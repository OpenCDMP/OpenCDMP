import { HttpClient } from '@angular/common/http';
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
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TranslateService } from '@ngx-translate/core';
import { Guid } from '@common/types/guid';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';

@Component({
	selector: 'plan-upload-dialog',
	templateUrl: './plan-upload-dialog.component.html',
	styleUrls: ['./plan-upload-dialog.component.scss']
})
export class PlanUploadDialogComponent extends BaseComponent {
	planTitle: string;
	planBlueprints: any[] = [];
	files: File[] = [];
	selectedBlueprintSections: PlanBlueprintDefinitionSection[];
	formGroup: UntypedFormGroup;

	descriptionTemplateSingleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active], searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTempalteGroupAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: DescriptionTemplate) => item.label,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: (item: DescriptionTemplate) => item.id,
		popupItemActionIcon: 'visibility'
	};


	constructor(
		public dialogRef: MatDialogRef<PlanUploadDialogComponent>,
		private _service: PlanService,
		private dialog: MatDialog,
		private httpClient: HttpClient,
		private analyticsService: AnalyticsService,
		private formService: FormService,
		public descriptionTemplateService: DescriptionTemplateService,
		public planBlueprintService: PlanBlueprintService,
		private planService: PlanService,
		private storageFileStorage: StorageFileService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,

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

		if (this.files.length > 0 && this.files[0].type.includes('/json') && this.formGroup){
			this.formService.removeAllBackEndErrors(this.formGroup);
			this.formService.touchAllFormFields(this.formGroup);
			if (this.formGroup.valid){
				this.data.planCommonModelConfig = this.formService.getValue(this.formGroup.value) as PlanCommonModelConfig;
				this.data.planCommonModelConfig.file = this.files[0];
			} else {
				return;
			}
		}
		this.dialogRef.close(this.data);
	}

	disableConfirmButton(){
		if (this.data.fileList.length === 0 || this.files.length === 0) return true;
		if (this.files.length > 0 && this.files[0].type.includes('/json') && this.formGroup == null) return true;
		return false;
	}

	uploadFile(event) {
		this.formGroup = null;
		const fileList: FileList = event.target.files
		this.data.fileList = fileList;
		if (this.data.fileList.length > 0) {
			this.planTitle = fileList.item(0).name;
		}
		if (this.files.length === 1) {
			this.files.splice(0, 1);
		}
		this.files.push(...event.target.files);

		if (this.files.length > 0 && this.files[0].type.includes('/json')){
			this.storageFileStorage.uploadTempFiles(fileList[0])
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				(storageFile) => {
					if (storageFile.length >0 ){
						this.planService.preprocessingPlan(storageFile[0].id, 'rda-file-transformer')
						.pipe(takeUntil(this._destroyed))
						.subscribe(
							(preprocessingData) => {
								this.formGroup = new PlanImportRdaConfigEditorModel().fromModel(preprocessingData, storageFile[0].id,).buildForm();
							},
							(error) => this.onCallbackEror(error.error)
						);
					}
					
				},
				(error) => this.onCallbackEror(error.error)
			);
			
		}
	}

	selectFile(event) {
		const fileList: FileList = event.addedFiles
		this.data.fileList = fileList;
		if (this.data.fileList.length > 0) {
			this.planTitle = fileList[0].name;
		}
		if (this.files.length === 1) {
			this.files.splice(0, 1);
		}
		this.files.push(...event.addedFiles);
	}

	onRemove(event) {
		this.files.splice(0, 1);
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
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(groupId => {
			if (groupId) {
				(this.formGroup.get('descriptions') as UntypedFormArray).at(descriptionIndex).get('templateId').patchValue(event.id);
			}
		});
	}

	hasFile(): boolean {
		return this.files && this.files.length > 0;
	}

	private onCallbackEror(error: any) {
		this.uiNotificationService.snackBarNotification(this.language.instant(error.error), SnackBarNotificationLevel.Error);
		this.close();
	}
}
