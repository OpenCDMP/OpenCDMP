
import { Component, OnInit } from '@angular/core';
import { FormArray, FormGroup, UntypedFormGroup } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PrefillingSource, PrefillingSourcePersist } from '@app/core/model/prefilling-source/prefilling-source';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PrefillingSourceService } from '@app/core/services/prefilling-source/prefilling-source.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { FieldMappingFormGroup, ResultFieldsMappingConfigurationEditorModel } from '@app/ui/external-fetcher/external-fetcher-source-editor.model';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { PrefillingSourceDefinitionEditorModel, PrefillingSourceEditorModel } from './prefilling-source-editor.model';
import { PrefillingSourceEditorResolver } from './prefilling-source-editor.resolver';
import { PrefillingSourceEditorService } from './prefilling-source-editor.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';

@Component({
    selector: 'app-prefilling-source-editor-component',
    templateUrl: 'prefilling-source-editor.component.html',
    styleUrls: ['./prefilling-source-editor.component.scss'],
    providers: [PrefillingSourceEditorService],
    standalone: false
})
export class PrefillingSourceEditorComponent extends BaseEditor<PrefillingSourceEditorModel, PrefillingSource> implements OnInit {

	isNew = true;
	isDeleted = false;
	formGroup: UntypedFormGroup = null;
	showInactiveDetails = false;
	private STATIC_FIELD_MAPPINGS = ["reference_id", "label", "description"]

	protected get canDelete(): boolean {
		return !this.isDeleted && !this.isNew && this.hasPermission(this.authService.permissionEnum.DeletePrefillingSource) && this.editorModel.belongsToCurrentTenant != false;
	}

	protected get canSave(): boolean {
		return !this.isDeleted && this.hasPermission(this.authService.permissionEnum.EditPrefillingSource) && this.editorModel.belongsToCurrentTenant != false;
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
		private prefillingSourceService: PrefillingSourceService,
		private logger: LoggingService,
		private prefillingSourceEditorService: PrefillingSourceEditorService,
		private titleService: Title,
		private analyticsService: AnalyticsService,
		private routerUtils: RouterUtilsService
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.label;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('PREFILLING-SOURCE-EDITOR.TITLE-EDIT-PREFILLING-SOURCE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.PrefillingSourcesEditor);
		super.ngOnInit();
	}

	getItem(itemId: Guid, successFunction: (item: PrefillingSource) => void) {
		this.prefillingSourceService.getSingle(itemId, PrefillingSourceEditorResolver.lookupFields())
			.pipe(map(data => data as PrefillingSource), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: PrefillingSource) {
		try {
			this.editorModel = data ? new PrefillingSourceEditorModel().fromModel(data) : new PrefillingSourceEditorModel();
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
		} catch (error) {
			this.logger.error('Could not parse PrefillingSource item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.authService.hasPermission(AppPermission.EditPrefillingSource));
		if (this.isDeleted || !this.authService.hasPermission(AppPermission.EditPrefillingSource)) this.formGroup.disable();
		this.prefillingSourceEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		this.STATIC_FIELD_MAPPINGS.forEach((code) => this.addFieldMapping(code, "searchConfiguration"));

	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: PrefillingSource) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		this.router.navigate([this.routerUtils.generateUrl('/prefilling-sources')], { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as PrefillingSourcePersist;

		this.prefillingSourceService.persist(formData)
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
					this.prefillingSourceService.delete(value.id).pipe(takeUntil(this._destroyed))
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

	getEnabledChanged(event: MatCheckboxChange) {
		if (event.checked == true) {
			const definition = new PrefillingSourceDefinitionEditorModel(this.editorModel.validationErrorModel);
			definition.buildGetConfiguration(this.formGroup.get('definition') as UntypedFormGroup, "definition.");

			this.submitFields();
		} else {
			const definition = this.formGroup.get('definition') as UntypedFormGroup;
			if (definition.get('getConfiguration')) definition.removeControl('getConfiguration');
			this.submitFields();
		}
	}

	//
	//
	// fixed value fields
	//
	//
	addFixedValiueField(): void {
		(this.formGroup.get('definition').get('fixedValueFields') as FormArray).push(this.editorModel.createChildFixedValueField((this.formGroup.get('definition').get('fixedValueFields') as FormArray).length));
	}

	removeFixedValueField(fieldIndex: number): void {
		const fieldForm = this.formGroup.get('definition').get('fixedValueFields') as FormArray;

		fieldForm.removeAt(fieldIndex);

		//Reapply validators
		PrefillingSourceEditorModel.reApplyDefinitionValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		)
		fieldForm.markAsDirty();
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
		PrefillingSourceEditorModel.reApplyDefinitionValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		)
		fieldForm.markAsDirty();

		if (this.formGroup.get('definition').get('getEnabled').value == true) this.removeFieldMapping((this.formGroup.get('definition').get('getConfiguration') as FormGroup), fieldCode);
		else this.removeFieldMapping((this.formGroup.get('definition').get('searchConfiguration') as FormGroup), fieldCode);
	}

	submitFields(): void {
		const fieldsFormArray = (this.formGroup.get('definition').get('fields') as FormArray);

		if (fieldsFormArray.valid) {

			if (this.formGroup.get('definition').get('getEnabled').value == true) this.updateFieldsMapping(fieldsFormArray, "getConfiguration");
			else this.updateFieldsMapping(fieldsFormArray, "searchConfiguration");
		}
	}

	private updateFieldsMapping(fieldsFormArray: FormArray, controlName: string){
		// PATCH + ADD FIELD MAPPINGS
		const fieldMappings = this.formGroup.get('definition').get(controlName).get('results').get('fieldsMapping') as FormArray<FormGroup<FieldMappingFormGroup>>;
		fieldsFormArray.controls.forEach((field, index) => {
			let positionInArray = index;
			if (controlName === "searchConfiguration") positionInArray = positionInArray + this.STATIC_FIELD_MAPPINGS.length;
			const fieldMappingsLength = fieldMappings?.controls?.length;
			const code = field.value.code;
			if(positionInArray < fieldMappingsLength){
				fieldMappings.controls[positionInArray].controls.code.setValue(code)
			} else {
				this.addFieldMapping(code, controlName);
				const controlNameToRemove = controlName == "searchConfiguration" ? "getConfiguration": "searchConfiguration";
				this.removeFieldMapping((this.formGroup.get('definition').get(controlNameToRemove) as FormGroup), code);
			}
		})
	}


	//
	//
	// resultFieldsMapping
	//
	//
	addFieldMapping(code: string, controlName: string): void {
		const formArray = (this.formGroup.get('definition').get(controlName).get('results').get('fieldsMapping') as FormArray);
		const fieldMappingSize = formArray.length;

		if (fieldMappingSize > 0) {
			for (let i = 0; i < fieldMappingSize; i++) {
				if (formArray.at(i).get('code').getRawValue() == code) {
					return;
				}
			}
		}
		const fieldsMapping = new ResultFieldsMappingConfigurationEditorModel(this.editorModel.validationErrorModel);
		fieldsMapping.code = code;
		formArray.push(fieldsMapping.buildForm({ rootPath: "definition." + controlName + ".results.fieldsMapping[" + fieldMappingSize + "]." }));
	}

	removeFieldMapping(baseFormGroup: any, fieldCode: string) {
		if (baseFormGroup) {
			const fieldMappingFormArray = (baseFormGroup.get('results').get('fieldsMapping') as FormArray);
			for (let j = 0; j < fieldMappingFormArray.length; j++) {
				if (fieldCode == fieldMappingFormArray.at(j).get('code').getRawValue()) {
					fieldMappingFormArray.removeAt(j);

					PrefillingSourceEditorModel.reApplyDefinitionValidators({
						formGroup: this.formGroup,
						validationErrorModel: this.editorModel.validationErrorModel
					}
					);
				}
			}
		}
	}


}
