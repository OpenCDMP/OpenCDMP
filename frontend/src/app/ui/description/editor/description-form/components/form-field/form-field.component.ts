
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from "@angular/material/dialog";
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { DescriptionTemplateFieldValidationType } from '@app/core/common/enum/description-template-field-validation-type';
import { DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplateLabelAndMultiplicityData, DescriptionTemplateUploadData } from '@app/core/model/description-template/description-template';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DescriptionService } from '@app/core/services/description/description.service';
import { SnackBarNotificationLevel, UiNotificationService } from "@app/core/services/notification/ui-notification-service";
import { PlanService } from '@app/core/services/plan/plan.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { TagService } from '@app/core/services/tag/tag.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { Observable } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { DescriptionFormService } from '../services/description-form.service';

@Component({
	selector: 'app-description-form-field',
	templateUrl: './form-field.component.html',
	styleUrls: ['./form-field.component.scss'],
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class DescriptionFormFieldComponent extends BaseComponent implements OnInit {

	@Input() field: DescriptionTemplateField;
	@Input() fieldSet: DescriptionTemplateFieldSet;
	@Input() propertiesFormGroup: UntypedFormGroup;
	@Input() visibilityRulesService: VisibilityRulesService;
	isRequired: boolean = false;

	@Input() descriptionTemplateId: any;
	@Input() isChild: Boolean = false;

	@Input() detectChangesObservable: Observable<any>;

	visible: boolean = true;
	descriptionTemplateFieldTypeEnum = DescriptionTemplateFieldType;



	public singleAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	public multipleAutoCompleteConfiguration: MultipleAutoCompleteConfiguration;
	tagsAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	multipleReferenceAutoCompleteConfiguration: MultipleAutoCompleteConfiguration;


	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	validationIcon;

	readonly datasetIdTypes: any[] = [
		{ name: 'Handle', value: 'handle' },
		{ name: 'DOI', value: 'doi' },
		{ name: 'Ark', value: 'ark' },
		{ name: 'Url', value: 'url' },
		{ name: 'Other', value: 'other' }
	];


	readonly validationTypes: any[] = [
		{ name: 'Zenodo', value: 'zenodo' }
	];

	filesToUpload: FileList;
	fileNameDisplay: string = null;

	constructor(
		private language: TranslateService,
		public planService: PlanService,
		public descriptionService: DescriptionService,
		public tagService: TagService,
		private changeDetectorRef: ChangeDetectorRef,
		private uiNotificationService: UiNotificationService,
		public dialog: MatDialog,
		private fileUtils: FileUtils,
		private storageFileService: StorageFileService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private descriptionFormService: DescriptionFormService,
	) {
		super();
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['form']) {
		}
	}

	ngOnInit() {

		if (this.field?.data?.fieldType == DescriptionTemplateFieldType.UPLOAD && this.field && this.field.id && (this.propertiesFormGroup?.get(this.field.id).get('textValue').value != undefined) && !this.fileNameDisplay) {
			const id = Guid.parse((this.propertiesFormGroup?.get(this.field.id).get('textValue').value as string));

			const fields = [
				nameof<StorageFile>(x => x.id),
				nameof<StorageFile>(x => x.name),
				nameof<StorageFile>(x => x.extension),
			]
			this.storageFileService.getSingle(id, fields).pipe(takeUntil(this._destroyed)).subscribe(storageFile => {
				this.createFileNameDisplay(storageFile.name, storageFile.extension);
				this.applyFieldType();
			});
		} else {
			this.applyFieldType();
		}

		this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe(x => this.changeDetectorRef.markForCheck());
	}

	private applyFieldType() {
		this.isRequired = this.field.validations?.includes(DescriptionTemplateFieldValidationType.Required);

		this.propertiesFormGroup.get(this.field.id).valueChanges
			.pipe(
				takeUntil(this._destroyed),
				distinctUntilChanged()
			)
			.subscribe(item => {
				this.visibilityRulesService.updateVisibilityForSource(this.field?.id);
				this.descriptionFormService.detectChanges(true);
			});
	}

	makeAutocompleteConfiguration(myfunc: Function, title: string, subtitle?: string): void {
		if (!((this.field.data as DescriptionTemplateLabelAndMultiplicityData).multipleSelect)) {
			this.singleAutoCompleteConfiguration = {
				filterFn: myfunc.bind(this),
				initialItems: (extraData) => myfunc(''),
				displayFn: (item) => { try { return (item != null && item.length > 1) ? JSON.parse(item)[title] : item[title] } catch { return '' } },
				titleFn: (item) => { try { return item[title] } catch { return '' } },
				valueAssign: (item) => JSON.stringify(item),
				subtitleFn: (item) => { try { return item[subtitle] } catch { return '' } }
			};
		}
		else {
			this.multipleAutoCompleteConfiguration = {
				filterFn: myfunc.bind(this),
				initialItems: (extraData) => myfunc(''),
				displayFn: (item) => { try { return typeof (item) == 'string' ? JSON.parse(item)[title] : item[title] } catch { return '' } },
				titleFn: (item) => { try { return typeof (item) == 'string' ? JSON.parse(item)[title] : item[title] } catch { return '' } },
				valueAssign: (item) => { try { return typeof (item) == 'string' ? item : JSON.stringify(item) } catch { return '' } },
				subtitleFn: (item) => { try { return item[subtitle] } catch { return '' } }
			}
		}
	}

	parseTags() {
		try {


			let stringValue = this.propertiesFormGroup.get(this.field.id).get('value').value;
			if (typeof stringValue === 'string') {
				stringValue = (<string>stringValue).replace(new RegExp('{', 'g'), '{"').replace(new RegExp('=', 'g'), '":"').replace(new RegExp(',', 'g'), '",').replace(new RegExp(', ', 'g'), ', "').replace(new RegExp('}', 'g'), '"}');
				stringValue = stringValue.replace(new RegExp('}"', 'g'), '}').replace(new RegExp('"{', 'g'), '{');
			} else if (stringValue instanceof Array) {
				const tempArray = new Array();
				for (let stringTag of stringValue) {
					tempArray.push(JSON.parse(stringTag));
				}
				stringValue = JSON.stringify(tempArray);
			}
			const tagArray = JSON.parse(stringValue);
			this.propertiesFormGroup.get(this.field.id).get('value').patchValue(tagArray);
		} catch (e) {
			console.warn('Could not parse tags');
		}
	}

	showTag(ev: any) {
		if (typeof ev === 'string') {
			return ev;
		} else {
			return ev.name;
		}
	}

	getDatasetIdControl(name: string): UntypedFormControl {
		return this.propertiesFormGroup.get(this.field.id).get(name) as UntypedFormControl;
	}

	validateId() {
		//TODO refactor
		return null;
		// const identifier = this.getDatasetIdControl('identifier').value;
		// const type = this.getDatasetIdControl('type').value;
		// this.validationIcon = 'loading';
		// this.externalSourcesService.validateIdentifier(identifier, type).pipe(takeUntil(this._destroyed)).subscribe(result => {
		// 	this.validationIcon = result === true ? 'done' : 'clear';
		// });

	}


	public upload() {
		this.storageFileService.uploadTempFiles(this.filesToUpload[0])
			.pipe(takeUntil(this._destroyed)).subscribe((response) => {
				this.propertiesFormGroup?.get(this.field.id).get('textValue').patchValue(response[0].id.toString());
				this.createFileNameDisplay(response[0].name, response[0].extension);
				this.changeDetectorRef.detectChanges();
			}, error => {
				this.onCallbackUploadFail(error.error);
			})


	}

	private createFileNameDisplay(name: string, extension: string) {
		if (extension.startsWith('.')) this.fileNameDisplay = name + extension;
		else this.fileNameDisplay = name + '.' + extension;
		this.changeDetectorRef.markForCheck();
	}


	private onCallbackUploadFail(error: any) {
		this.makeFilesNull();
		this.uiNotificationService.snackBarNotification(this.language.instant(error.message), SnackBarNotificationLevel.Error);
	}

	fileChangeEvent(fileInput: any, dropped: boolean = false) {

		if (dropped) {
			this.filesToUpload = fileInput.addedFiles;
		} else {
			this.filesToUpload = fileInput.target.files;
		}


		let messages: string[] = [];
		if (this.filesToUpload.length == 0) {
			messages.push(this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.FIELDS.NO-FILES-SELECTED'));
			return;
		} else {
			let fileToUpload = this.filesToUpload[0];
			const data = this.field.data as DescriptionTemplateUploadData;
			if (data && data.types
				&& data.types.map(type => type.value).includes(fileToUpload.type)
				&& data.maxFileSizeInMB
				&& data.maxFileSizeInMB * 1048576 >= fileToUpload.size) {
				this.upload();
			} else {
				this.filesToUpload = null;
				messages.push(this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.FIELDS.LARGE-FILE-OR-UNACCEPTED-TYPE'));
				messages.push(this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.FIELDS.FIELD-UPLOAD-MAX-FILE-SIZE', { 'maxfilesize': data.maxFileSizeInMB }));
				messages.push(this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.FIELDS.ACCEPTED-FILE-TRANSFOMER') + data.types.map(type => type.value).join(", "));
			}

			if (messages && messages.length > 0) {
				this.dialog.open(FormValidationErrorsDialogComponent, {
					data: {
						errorMessages: messages
					}
				})
			}
		}
	}

	onRemove(makeFilesNull: boolean = true) {
		this.makeFilesNull()
		this.changeDetectorRef.detectChanges();
	}

	makeFilesNull() {
		this.filesToUpload = null;
		this.propertiesFormGroup?.get(this.field.id).get('textValue').patchValue(null);
		this.fileNameDisplay = null;
	}

	typesToString() {
		return (this.field.data as DescriptionTemplateUploadData).types.map(type => type.value).toString();
	}

	download(): void {

		if (this.propertiesFormGroup?.get(this.field.id).get('textValue').value) {
			const id = Guid.parse((this.propertiesFormGroup?.get(this.field.id).get('textValue').value as string));

			this.storageFileService.download(id).pipe(takeUntil(this._destroyed))
				.subscribe(response => {
					const blob = new Blob([response.body]);
					const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
					FileSaver.saveAs(blob, filename);
				},
					error => this.httpErrorHandlingService.handleBackedRequestError(error));
		}

	}
}
