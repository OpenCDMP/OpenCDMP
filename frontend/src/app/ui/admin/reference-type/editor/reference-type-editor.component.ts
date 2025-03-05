
import { Component, OnInit } from '@angular/core';
import { FormArray, FormGroup, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { ExternalFetcherApiHTTPMethodType } from '@app/core/common/enum/external-fetcher-api-http-method-type';
import { ExternalFetcherSourceType } from '@app/core/common/enum/external-fetcher-source-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { ReferenceFieldDataType } from '@app/core/common/enum/reference-field-data-type';
import { ReferenceType, ReferenceTypeField, ReferenceTypePersist } from '@app/core/model/reference-type/reference-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { ReferenceTypeEditorModel, ReferenceTypeFieldFormGroup } from './reference-type-editor.model';
import { ReferenceTypeEditorResolver } from './reference-type-editor.resolver';
import { ReferenceTypeEditorService } from './reference-type-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { FieldMappingFormGroup, ItemsFormGroup, StaticEditorModel, StaticOptionEditorModel } from '@app/ui/external-fetcher/external-fetcher-source-editor.model';
import { ReferenceTypeTestDialogComponent } from '../reference-type-test-dialog/reference-type-test-dialog.component';
import { ExternalFetcherBaseSourceConfigurationPersist } from '@app/core/model/external-fetcher/external-fetcher';

@Component({
    selector: 'app-reference-type-editor-component',
    templateUrl: 'reference-type-editor.component.html',
    styleUrls: ['./reference-type-editor.component.scss'],
    providers: [ReferenceTypeEditorService],
    standalone: false
})
export class ReferenceTypeEditorComponent extends BaseEditor<ReferenceTypeEditorModel, ReferenceType> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
	referenceTypeSourceType = ExternalFetcherSourceType;
	referenceTypeExternalApiHTTPMethodType = ExternalFetcherApiHTTPMethodType;
	public referenceTypeSourceTypeEnum = this.enumUtils.getEnumValues<ExternalFetcherSourceType>(ExternalFetcherSourceType);
	public referenceFieldDataTypeEnum = this.enumUtils.getEnumValues<ReferenceFieldDataType>(ReferenceFieldDataType);
	public referenceTypeExternalApiHTTPMethodTypeEnum = this.enumUtils.getEnumValues<ExternalFetcherApiHTTPMethodType>(ExternalFetcherApiHTTPMethodType);
	referenceTypes: ReferenceType[] = null;
	sourceKeysMap: Map<Guid, string[]> = new Map<Guid, string[]>();

    prevFields: ReferenceTypeField[] = [];

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeleteReferenceType) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditReferenceType) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canFinalize(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditReferenceType);
	}


	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.editorModel?.permissions?.includes(permission);
	}

	constructor(
		// BaseFormEditor injected dependencies
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
		// Rest dependencies. Inject any other needed deps here:
		public enumUtils: EnumUtils,
		public referenceTypeService: ReferenceTypeService,
		private logger: LoggingService,
		private referenceTypeEditorService: ReferenceTypeEditorService,
		private titleService: Title,
		protected routerUtils: RouterUtilsService,
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.name;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('REFERENCE-TYPE-EDITOR.TITLE-EDIT-REFERENCE-TYPE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		super.ngOnInit();
		if ((this.formGroup.get('definition').get('sources') as FormArray).length == 0) {
			this.addSource();
		}
	}

	getItem(itemId: Guid, successFunction: (item: ReferenceType) => void) {
		this.referenceTypeService.getSingle(itemId, ReferenceTypeEditorResolver.lookupFields())
			.pipe(map(data => data as ReferenceType), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: ReferenceType) {
		try {
			this.editorModel = data ? new ReferenceTypeEditorModel().fromModel(data) : new ReferenceTypeEditorModel();

			this.getReferenceTypes(this.editorModel.id);

			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse referenceType item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditReferenceType));
		this.referenceTypeEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
        this.prevFields = this.formGroup.controls.definition.get('fields')?.getRawValue() ?? [];
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: ReferenceType) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/reference-type')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.getRawValue()) as ReferenceTypePersist;
		this.referenceTypeService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
				error => this.onCallbackError(error)
			);
	}

	formSubmit(): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.isFormValid()) {
			return;
		}

		this.persistEntity();
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
					this.referenceTypeService.delete(value.id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackDeleteSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

	//
	//
	// fields
	//
	//
	addField(): void {
		(this.formGroup.get('definition').get('fields') as FormArray).push(this.editorModel.createChildField((this.formGroup.get('definition').get('fields') as FormArray).length));
	}

	removeField(fieldIndex: number): void {
		const fieldForm = this.formGroup.get('definition').get('fields') as FormArray;
		const fieldCode = fieldForm.at(fieldIndex).get('code').value;

		fieldForm.removeAt(fieldIndex);

		//Reapply validators
		ReferenceTypeEditorModel.reApplyDefinitionFieldsValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		)
		fieldForm.markAsDirty();

		const sourceFormArray = (this.formGroup.get('definition').get('sources') as FormArray);
		if (sourceFormArray) {
			for (let i = 0; i < sourceFormArray.length; i++) {
				const fieldMappingFormArray = (sourceFormArray.at(i).get('results').get('fieldsMapping') as FormArray);
				for (let j = 0; j < fieldMappingFormArray.length; j++) {
					// remove code from fields mapping
					if (fieldCode == fieldMappingFormArray.at(j).get('code').getRawValue()) {
						this.removeFieldMapping(i, j);
					}
				}

				const staticFormArray = (this.formGroup.get('definition').get('sources') as FormArray).at(i).get('items') as FormArray;
				for (let k = 0; k < staticFormArray.length; k++) {
					const optionsFormArray = staticFormArray.at(k).get('options') as FormArray;
					for (let p = 0; p < optionsFormArray.length; p++) {
						// remove code from static options
						if (fieldCode == optionsFormArray.at(p).get('code').getRawValue()) {
							this.removeOption(i, k, p);
						}
					}
				}
			}
		}
	}

	submitFields(): void {
		const fieldsFormArray = (this.formGroup.get('definition').get('fields') as FormArray<FormGroup<ReferenceTypeFieldFormGroup>>);
        this.prevFields = fieldsFormArray?.getRawValue() ?? [];

		if (fieldsFormArray?.valid) {
			const sourcesFormArray = (this.formGroup.get('definition').get('sources') as FormArray);
            const staticFieldsLength = this.STATIC_FIELD_MAPPINGS.length;

			if (fieldsFormArray.length > 0) {
                sourcesFormArray.controls.forEach((sourceConfig, sourceIndex) => {
                    // PATCH + ADD FIELD MAPPINGS
                    const fieldMappings = sourceConfig.get('results').get('fieldsMapping') as FormArray<FormGroup<FieldMappingFormGroup>>;
                    fieldsFormArray.controls.forEach((field, index) => {
                        const positionInArray = index + staticFieldsLength
                        const fieldMappingsLength = fieldMappings?.controls?.length;
                        const code = field.value.code;
                        if(positionInArray < fieldMappingsLength){
                            fieldMappings.controls[positionInArray].controls.code.setValue(code)
                        } else {
                            this.addFieldMapping(sourceIndex, field.value.code);
                        }
                    })
                    // // PATCH + ADD STATIC ITEM OPTIONS
                    const staticItems = sourceConfig?.get('items') as FormArray<FormGroup<ItemsFormGroup>>;
                    staticItems?.controls?.forEach((item) => {
                        if(!item.controls?.options){
                            return;
                        }
                        fieldsFormArray.controls.forEach((field, index) => {
                            const positionInArray = index + staticFieldsLength
                            const staticItemLength = item.controls?.options?.length;
                            if(positionInArray < staticItemLength){
                                item.controls.options.controls[positionInArray].controls.code.setValue(field.value.code)
                            } else {
                                this.addOption(sourceIndex, staticItemLength, field.value.code);
                            }
                        })
                    })

                })
			}
		}
        // if (fieldsFormArray.valid) {
		// 	const sourcesFormArray = (this.formGroup.get('definition').get('sources') as FormArray);

		// 	if (fieldsFormArray.length > 0) {
		// 		for (let j = 0; j < sourcesFormArray.length; j++) {
		// 			for (let i = 0; i < fieldsFormArray.length; i++) {
		// 				this.addFieldMapping(j, fieldsFormArray.at(i).get('code').value);
		// 				const staticFormArray = (this.formGroup.get('definition').get('sources') as FormArray).at(j).get('items') as FormArray;
		// 				for (let k = 0; k < staticFormArray.length; k++) {
		// 					this.addOption(j, k, fieldsFormArray.at(i).get('code').value);
		// 				}
		// 			}
		// 		}
		// 	}
		// }
	}


    private STATIC_FIELD_MAPPINGS = ["reference_id", "label", "description"]
	addSource(): void {
		(this.formGroup.get('definition').get('sources') as FormArray).push(this.editorModel.createChildSource((this.formGroup.get('definition').get('sources') as FormArray).length));
		const sourceIndex = (this.formGroup.get('definition').get('sources') as FormArray).length - 1;

        this.STATIC_FIELD_MAPPINGS.forEach((code) => this.addFieldMapping(sourceIndex, code))

		const fieldsFormArray = (this.formGroup.get('definition').get('fields') as FormArray);
		if (fieldsFormArray && fieldsFormArray.length > 0) {
			for (let i = 0; i < fieldsFormArray.length; i++) {
				this.addFieldMapping(sourceIndex, fieldsFormArray.at(i).get('code').value);
			}
		}
		this.addStaticItem(sourceIndex);
	}

	removeSource(sourceIndex: number): void {
		const formArray = (this.formGroup.get('definition').get('sources') as FormArray);
		formArray.removeAt(sourceIndex);

		ReferenceTypeEditorModel.reApplyDefinitionSourcesValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		)
		formArray.markAsDirty();
	}

	//
	//
	// resultFieldsMapping
	//
	//
	addFieldMapping(sourceIndex: number, code: string): void {
		const fieldMappingSize = ((this.formGroup.get('definition').get('sources') as FormArray).controls[sourceIndex]?.get('results')?.get('fieldsMapping') as FormArray)?.length;

		if (fieldMappingSize > 0) {
			for (let i = 0; i < fieldMappingSize; i++) {
				if (((this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('results').get('fieldsMapping') as FormArray).at(i).get('code').getRawValue() == code) {
					return;
				}
			}
		}

		((this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('results').get('fieldsMapping') as FormArray).push(this.editorModel.createFieldsMapping(sourceIndex, fieldMappingSize));
		((this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('results').get('fieldsMapping') as FormArray).at(fieldMappingSize).get('code').patchValue(code);
	}

	removeFieldMapping(sourceIndex: number, fieldMappingIndex: number): void {
		const formArray = ((this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex) as FormArray);
		(formArray.get('results').get('fieldsMapping') as FormArray).removeAt(fieldMappingIndex);

		ReferenceTypeEditorModel.reApplyDefinitionSourcesValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		);
		(this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('results').get('fieldsMapping').markAsDirty();

	}

	private getReferenceTypes(excludedId?: Guid): void {
		let sourceKeys: string[] = [];

		const lookup = ReferenceTypeService.DefaultReferenceTypeLookup();
		if (excludedId) lookup.excludedIds = [excludedId];

		this.referenceTypeService.query(lookup)
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => {
				this.referenceTypes = response.items as ReferenceType[];
				this.referenceTypes.forEach(referenceType => {
					sourceKeys = referenceType.definition.sources.map(x => x.key);
					this.sourceKeysMap.set(referenceType.id, sourceKeys);
				})
			},
				error => {
					this.router.navigate([this.routerUtils.generateUrl('/reference-type')]);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				});
	}

	selectedReferenceTypeChanged(id: Guid): void {
		let sourceKeys: string[] = [];

		this.referenceTypeService.getSingle(id, ReferenceTypeEditorResolver.lookupFields())
			.pipe(takeUntil(this._destroyed))
			.subscribe(data => {
				const referenceType = data as ReferenceType;

				// source keys
				referenceType.definition.sources.forEach(source => {
					if (!sourceKeys.includes(source.key)) sourceKeys.push(source.key)
				});

				if (this.sourceKeysMap.has(referenceType.id) && this.sourceKeysMap.get(referenceType.id).length == 0) {
					this.sourceKeysMap.set(referenceType.id, sourceKeys);
				}
			});
	}

	// static item

	addStaticItem(sourceIndex: number): void {
		const formArray = (this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('items') as FormArray;
		const staticItem = new StaticEditorModel(this.editorModel.validationErrorModel);
		formArray.push(staticItem.buildForm({rootPath: 'definition.sources[' + sourceIndex + '].items[' + formArray.length + '].'}));

        this.STATIC_FIELD_MAPPINGS.forEach((code) => this.addOption(sourceIndex, formArray.length -1 , code))

		const fieldsFormArray = (this.formGroup.get('definition').get('fields') as FormArray);
		if (fieldsFormArray && fieldsFormArray.length > 0) {
			for (let i = 0; i < fieldsFormArray.length; i++) {
				this.addOption(sourceIndex, formArray.length - 1, fieldsFormArray.at(i).get('code').value);
			}
		}
	}

	removeStaticItem(sourceIndex: number, staticIndex: number): void {
		const formArray = (this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('items') as FormArray;
		formArray.removeAt(staticIndex);

		ReferenceTypeEditorModel.reApplyDefinitionSourcesValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		);
		formArray.markAsDirty();
	}


	// Options

	addOption(sourceIndex: number, staticIndex: number, code: string): void {
		const formArray = ((this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('items') as FormArray).at(staticIndex).get('options') as FormArray;

		if (formArray && formArray.length > 0) {
			for (let i = 0; i < formArray.length; i++) {
				if (formArray.at(i).get('code').getRawValue() == code) {
					return;
				}
			}
		}

		const option = new StaticOptionEditorModel(this.editorModel.validationErrorModel);
		formArray.push(option.buildForm({rootPath: 'definition.sources[' + sourceIndex + 'items[' + staticIndex + '].options[' + formArray.length + '].'}));
		formArray.at(formArray.length -1 ).get('code').patchValue(code);
	}

	removeOption(sourceIndex: number, staticIndex: number, optionIndex: number): void {
		const formArray = ((this.formGroup.get('definition').get('sources') as FormArray).at(sourceIndex).get('items') as FormArray).at(staticIndex).get('options') as FormArray;
		formArray.removeAt(optionIndex);

		ReferenceTypeEditorModel.reApplyDefinitionSourcesValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		);		
		formArray.markAsDirty();
	}

	 hasSources() : boolean{
		return (this.formGroup.get('definition').get('sources') as UntypedFormArray).length > 0;
	}

	openReferenceTestDialog(params?: {key?: string, index?: number}): void {
        const {key = null, index = null} = params ?? {};
		const sourceArray = (this.formGroup.get('definition').get('sources') as FormArray<UntypedFormGroup>)?.getRawValue();
        const sources = (index != null && index != undefined && sourceArray?.length >= index) ? [sourceArray[index]] : sourceArray;
		
		this.dialog.open(ReferenceTypeTestDialogComponent, {
		  data: {
			sources: JSON.parse(JSON.stringify(sources)) as ExternalFetcherBaseSourceConfigurationPersist[],
			key: key,
			label: this.formGroup.get('name').value
		  },
          maxHeight: 'min(650px, 100vh)',
		});
	  }
}
