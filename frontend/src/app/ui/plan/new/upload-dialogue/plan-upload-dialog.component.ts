import { Component, output, OnInit, AfterViewInit, ChangeDetectionStrategy } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanBlueprint, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { BaseComponent } from '@common/base/base.component';
import { catchError, map, takeUntil, tap } from 'rxjs/operators';
import { PlanImportRdaConfigEditorModel } from './plan-common-model-config.editor.model';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { FormService } from '@common/forms/form-service';
import { PlanCommonModelConfig } from '@app/core/model/plan/plan-import';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { FileTransformerConfiguration } from '@app/core/model/file/file-transformer-configuration.model';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { Guid } from '@common/types/guid';
import { EnqueueService } from '@app/core/services/enqueue.service';
import { throwError } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatSelect, MatOption } from '@angular/material/select';
import { SingleAutoCompleteComponent } from '../../../../library/auto-complete/single/single-auto-complete.component';
import { FileUploadComponent } from '../../../../library/file-uploader/file-uploader.component';
@Component({
    selector: 'plan-upload-dialog',
    templateUrl: './plan-upload-dialog.component.html',
    styleUrls: ['./plan-upload-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, MatIconButton, MatFormField, MatLabel, ReactiveAsteriskDirective, MatInput, FormsModule, MatSelect, MatOption, ReactiveFormsModule, SingleAutoCompleteComponent, MatError, FileUploadComponent, MatButton, TranslatePipe]
})
export class PlanUploadDialogComponent extends BaseComponent implements OnInit, AfterViewInit {
   

	planTitle: string;
	planBlueprints: any[] = [];
	file: File;
	selectedBlueprintSections: RootSection[];
	formGroup: UntypedFormGroup;
	fileTransformerWithJson: FileTransformerConfiguration[] = [];
	loadSpiner = false;
	storageFile: StorageFile = null;

    onClose = output<{
        success: boolean,
        planId?: Guid
    }>();

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
        private enqueueService: EnqueueService
	) {
		super();
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanUploadDialog);
		this.fileTransformerWithJson = this.fileTransformerService.availableImportFormatsFor(FileTransformerEntityType.Plan)?.filter(x => x.importVariants?.filter(y => y.format?.toLowerCase() === 'json')) || [];
	}
    ngAfterViewInit(){
        document.getElementById('plan-upload-close-button')?.focus();
    }

	cancel() {
		this.onClose.emit({success: false});
	}

	close() {
		this.onClose.emit({success: false});
	}

	confirm() {
		let planCommonModelConfig;
        
		if (this.file?.type?.includes('/json') && this.formGroup){
			this.formService.removeAllBackEndErrors(this.formGroup);
			this.formService.touchAllFormFields(this.formGroup);
			if (this.formGroup.valid){
				planCommonModelConfig = this.formService.getValue(this.formGroup.value) as PlanCommonModelConfig;
				planCommonModelConfig.file = this.file;
			} else {
				return;
			}
		}

        const file = this.file;
        if (file?.type.includes('/xml')){
            this.enqueueService.enqueueExhaustChannel(
                this.planService.uploadXml(this.file, this.planTitle)
                .pipe(
                    takeUntil(this._destroyed),
                    catchError((error) => {
                        this.httpErrorHandlingService.handleBackedRequestError(error);
                        return throwError(() => error)
                    }),
                    tap((res) => {
                        this.onCallbackImportComplete(res?.id);
                    })
                )
            )
        } else if (file?.type.includes('/json') && planCommonModelConfig){
            this.enqueueService.enqueueExhaustChannel(
                this.planService.uploadJson(planCommonModelConfig)
                .pipe(
                    takeUntil(this._destroyed),
                    catchError((error) => {
                        this.httpErrorHandlingService.handleBackedRequestError(error);
                        return throwError(() => error)
                    }),
                    tap((res) => {
                        this.onCallbackImportComplete(res?.id);
                    })
                )
            )
        }
	}
    
    private onCallbackImportComplete(planId: Guid) {
        this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-UPLOAD.UPLOAD-SUCCESS'), SnackBarNotificationLevel.Success);
        setTimeout(() => {
            this.onClose.emit({
                success:true,
                planId
            })
        })
    }

    private onCallbackImportFail(error: any) {
        this.httpErrorHandlingService.handleBackedRequestError(error, null, SnackBarNotificationLevel.Error);
    }

	disableConfirmButton(){
		if (!this.file || this.enqueueService.exhaustPipelineBusy()) return true;
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

	get hasJsonFormats(): boolean {
  		return true;
	}

	get hasRDAMadmpJsonFormat(): boolean {
  		if (!this.hasJsonFormats) return false;

		return this.fileTransformerWithJson?.filter(x => x.fileTransformerId?.toUpperCase() === 'RDA-FILE-TRANSFORMER')?.length > 0;
	}

	get supportedFormatsMessage(): string {
		const formats: string[] = [];

		if (this.hasRDAMadmpJsonFormat) {
			formats.push('<b>RDA maDMP JSON</b>');
		}

		if (this.hasJsonFormats && !this.hasRDAMadmpJsonFormat) {
			formats.push('<b>JSON</b>');
		}

		formats.push('<b>XML</b>');

		return `${formats.join(', ')}`;
	}

	get availableJsonFormatsText(): string {
		return this.fileTransformerWithJson?.flatMap(fileTransformer =>
			fileTransformer.importVariants?.map(fileFormat =>
				this.language.instant('GENERAL.FILE-TRANSFORMER.' + fileTransformer?.fileTransformerId?.toUpperCase() + '-' +fileFormat?.format?.toUpperCase()
				)) || []
			).filter(Boolean).join(', ');
	}
}
