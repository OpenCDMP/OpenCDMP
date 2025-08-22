
import { CdkStep, StepperSelectionEvent } from '@angular/cdk/stepper';
import { ChangeDetectorRef, Component, OnInit, QueryList, ViewChild } from '@angular/core';
import { FormArray, FormControl, FormGroup, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatStepper } from '@angular/material/stepper';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { UserDescriptionTemplateRole } from '@app/core/common/enum/user-description-template-role';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplatePersist, NewVersionDescriptionTemplatePersist } from '@app/core/model/description-template/description-template-persist';
import { LanguageInfo } from '@app/core/model/language-info';
import { User } from '@app/core/model/user/user';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageInfoService } from '@app/core/services/culture/language-info-service';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { DescriptionTemplateEditorModel, DescriptionTemplateFieldEditorModel, DescriptionTemplateFieldSetEditorModel, DescriptionTemplateForm, DescriptionTemplatePageEditorModel, DescriptionTemplateSectionEditorModel, UserDescriptionTemplateEditorModel } from './description-template-editor.model';
import { DescriptionTemplateEditorResolver } from './description-template-editor.resolver';
import { DescriptionTemplateEditorService } from './description-template-editor.service';
import { NewEntryType, ToCEntry, ToCEntryType } from './table-of-contents/description-template-table-of-contents-entry';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { DescriptionFormService } from '@app/ui/description/editor/description-form/components/services/description-form.service';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { toObservable } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { PluginRepositoryConfiguration } from '@app/core/model/plugin-configuration/plugin-configuration';
import { GENERAL_ANIMATIONS, STEPPER_ANIMATIONS } from '@app/library/animations/animations';


@Component({
    selector: 'app-description-template-editor-component',
    templateUrl: 'description-template-editor.component.html',
    styleUrls: ['./description-template-editor.component.scss'],
    providers: [DescriptionTemplateEditorService, DescriptionFormService],
    animations: [...STEPPER_ANIMATIONS, ...GENERAL_ANIMATIONS],
    standalone: false
})
export class DescriptionTemplateEditorComponent extends BaseEditor<DescriptionTemplateEditorModel, DescriptionTemplate> implements OnInit {

	@ViewChild('stepper') stepper: MatStepper;

	formGroup: FormGroup<DescriptionTemplateForm> = null;
	item: DescriptionTemplate;
	showInactiveDetails = false;
	finalized: DescriptionTemplateStatus.Finalized;

	availableLanguages: LanguageInfo[] = this.languageInfoService.getLanguageInfoValues();

	showErrors: boolean = false;
	toCEntries: ToCEntry[];
	selectedTocEntry: ToCEntry;
	colorizeInvalid: boolean = false;
	tocEntryEnumValues = ToCEntryType;

	usersMap: Map<Guid, User> = new Map<Guid, User>();
	userFormControl = new FormControl();

	singleAutocompleteDescriptionTemplateTypeConfiguration: SingleAutoCompleteConfiguration;

	isNew = true;
	isClone = false;
	isNewVersion = false;
	isDeleted = false;

	//Preview
	previewFieldSet: DescriptionTemplate = null;
	previewPropertiesFormGroup: UntypedFormGroup = null;
	finalPreviewDescriptionTemplatePersist: DescriptionTemplatePersist;
	availableReferenceTypes: ReferenceType[] = [];

	initFile: StorageFile = null;
    fileMap = new Map<Guid, StorageFile>([]);
    pluginsInitialized: Observable<PluginRepositoryConfiguration[]>;

	get generalInfoStepperLabel(): string {
		return '1 ' + this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.GENERAL-INFO.TITLE');
	}

	get fromStepperLabel(): string {
		return '2 ' + this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.TITLE');
	}

	get previewLabel(): string {
		const label = this.isFinalized ? this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.ACTIONS.PREVIEW') : this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.ACTIONS.PREVIEW-AND-FINALIZE');
		return '3 ' + label;
	}

    protected get belongsToCurrentTenant(): boolean {
        return this.isNew || this.editorModel?.belongsToCurrentTenant;
    }

    protected get isTransient(): boolean {
        return this.isNew || this.isClone || this.isNewVersion;
    }

    protected get viewOnly(): boolean {
        return (this.isDeleted || this.isFinalized || !this.hasPermission(AppPermission.EditDescriptionTemplate) || !this.belongsToCurrentTenant) && !this.isNewVersion;;
    }

	protected get canDelete(): boolean {
		return  !this.isDeleted && !this.isTransient && this.belongsToCurrentTenant &&  (this.hasPermission(this.authService.permissionEnum.DeleteDescriptionTemplate) || this.item?.authorizationFlags?.some(x => x === AppPermission.DeleteDescriptionTemplate));
	}

	protected get canFinalize(): boolean {
		return !this.isTransient && !this.isFinalized;
	}

	protected get isFinalized(): boolean {
		return this.editorModel?.status == DescriptionTemplateStatus.Finalized;
	}


	private hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || this.editorModel?.permissions?.includes(permission) || this.item?.authorizationFlags?.some(x => x === permission);
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
		private descriptionTemplateService: DescriptionTemplateService,
		public descriptionTemplateTypeService: DescriptionTemplateTypeService,
		private logger: LoggingService,
		private descriptionTemplateEditorService: DescriptionTemplateEditorService,
		private languageInfoService: LanguageInfoService,
		public userService: UserService,
		public titleService: Title,
		private analyticsService: AnalyticsService,
		private routerUtils: RouterUtilsService,
		private referenceTypeService: ReferenceTypeService,
		private storageFileService: StorageFileService,
		private cdr: ChangeDetectorRef,
		private fileUtils: FileUtils,
		private pluginConfigurationService: PluginConfigurationService
		
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.label;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('DESCRIPTION-TEMPLATE-EDITOR.TITLE-EDIT-DESCRIPTION-TEMPLATE');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
        this.pluginsInitialized = toObservable(this.pluginConfigurationService.pluginRepositoryConfiguration);
    }

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionTemplateEditor);
		this.initModelFlags(this.route.snapshot.data['action']);
        this.pluginsInitialized.pipe(takeUntil(this._destroyed))
        .subscribe((initialized) => {
            if(initialized){
                super.ngOnInit();
            }
        })
		this.singleAutocompleteDescriptionTemplateTypeConfiguration = this.descriptionTemplateTypeService.getSingleAutocompleteConfiguration([DescriptionTemplateTypeStatus.Finalized]);
		this.referenceTypeService.query(ReferenceTypeService.DefaultReferenceTypeLookup()).subscribe(referenceTypes => this.availableReferenceTypes = referenceTypes.items as ReferenceType[]);	}

	private initModelFlags(action: string): void {
		if (action == 'clone') {
			this.isNew = false;
			this.isClone = true;
			this.isNewVersion = false;
		} else if (action == 'new-version') {
			this.isNew = false;
			this.isClone = false;
			this.isNewVersion = true;
		} else {
			this.isClone = false;
			this.isNewVersion = false;
		}
	}

	getItem(itemId: Guid, successFunction: (item: DescriptionTemplate) => void) {
		this.descriptionTemplateService.getSingle(itemId, DescriptionTemplateEditorResolver.lookupFields())
			.pipe(map(data => data as DescriptionTemplate), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: DescriptionTemplate) {
		try {
			let dataCopy = data ? JSON.parse(JSON.stringify(data)) : null;

			this.fileMap.clear();
			if (dataCopy?.definition?.pluginConfigurations) {
                dataCopy.definition.pluginConfigurations?.forEach((config) => {
					config.fields?.forEach((field) => {
						if(field.fileValue){
							this.fileMap.set(field.fileValue.id, field.fileValue)
						}
					})
				})
				
			} else {
                const pluginConfigurations = this.pluginConfigurationService.getAvailablePluginsFor(PluginType.FileTransformer, [PluginEntityType.Description]);
				if (dataCopy == null) {
					dataCopy = {
						...dataCopy,
						status: DescriptionTemplateStatus.Draft,
						definition : {
							pluginConfigurations: pluginConfigurations
						}
					}
				} else if (!dataCopy?.definition?.pluginConfigurations){
					dataCopy.definition.pluginConfigurations = pluginConfigurations;
				}
			}

			if(dataCopy && this.isClone) {
				dataCopy.belongsToCurrentTenant = true;
			}

			this.editorModel = dataCopy ? new DescriptionTemplateEditorModel().fromModel(dataCopy, this.pluginConfigurationService.getPluginRepositoryConfigurationFor(PluginType.FileTransformer, [PluginEntityType.Description])) : new DescriptionTemplateEditorModel();
			this.item = dataCopy;
			// Add user info to Map, to present them.
			(this.item?.users ?? []).forEach(obj => { this.usersMap.set(obj.user.id, obj.user); });
			this.isDeleted = data?.isActive === IsActive.Inactive;
			this.buildForm();

			if (data && data.id) this.checkLock(data.id, LockTargetType.DescriptionTemplate, 'DESCRIPTION-TEMPLATE-EDITOR.LOCKED-DIALOG.TITLE', 'DESCRIPTION-TEMPLATE-EDITOR.LOCKED-DIALOG.MESSAGE');

			this._initializeToCEntries();

		} catch (error) {
			console.error(error);
			this.logger.error('Could not parse descriptionTemplate item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.viewOnly);
		this.descriptionTemplateEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);

		const action = this.route.snapshot.data['action'];
		if (action && action == 'new-version') {
			this.formGroup.get('code').disable();
			this.formGroup.get('status').setValue(DescriptionTemplateStatus.Draft);
		}
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: DescriptionTemplate) => this.prepareForm(data));
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
		let route = [];

		if (id === null) {
			route.push(this.routerUtils.generateUrl('/description-templates/'));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else if (this.isNew || this.isNewVersion || this.isClone) {
			route.push(this.routerUtils.generateUrl('/description-templates/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}

	}

	persistEntity(onSuccess?: (response) => void): void {
		if (!this.isNewVersion) {
			const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as DescriptionTemplatePersist;
			formData.code = this.formGroup.get('code').getRawValue();

			this.descriptionTemplateService.persist(formData)
				.pipe(takeUntil(this._destroyed)).subscribe(
					complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
					error => {
						this.onCallbackError(error);
						if (error.status == 400) this.showErrors = true;
					}
				);
		} else if (this.isNewVersion == true && this.isNew == false && this.isClone == false) {
			const formData = JSON.parse(JSON.stringify(this.formGroup.value)) as NewVersionDescriptionTemplatePersist;

			this.descriptionTemplateService.newVersion(formData)
				.pipe(takeUntil(this._destroyed)).subscribe(
					complete => onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete),
					error => this.onCallbackError(error)
				);
		}
	}

	formSubmit(onSuccess?: (response) => void): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);

		if (!this.isFormValid()) {
			this.showErrors = true;
			this.uiNotificationService.snackBarNotification(
  	  	this.language.instant("GENERAL.BACKEND-ERRORS.MODEL-VALIDATION"), SnackBarNotificationLevel.Error);
			return;
		}

		this.persistEntity(onSuccess);
	}

	saveWithClose(close: boolean) {
		this.formSubmit((data) => {
			this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
			if (close) {
				this.formGroup = null;
				this.router.navigate([this.routerUtils.generateUrl('/description-templates')]);
			} else {
				this.refreshOnNavigateToData(data ? data.id : null);
			}
		});
	}

	public delete() {
		const id = this.formGroup.controls.id.value;
		if (id) {
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
					this.descriptionTemplateService.delete(id).pipe(takeUntil(this._destroyed))
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

	finalize() {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.TITLE'),
				confirmButton: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.CONFIRM'),
				cancelButton: this.language.instant('DESCRIPTION-OVERVIEW.FINALIZE-DIALOG.NEGATIVE'),
				isDeleteConfirmation: false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.formGroup.get('status').setValue(DescriptionTemplateStatus.Finalized);
				this.persistEntity();
			}
		});
	}

	//
	//
	// Description Template User
	//
	//
	addUser(user: User) {
		const userArray = (this.formGroup.get('users') as FormArray)
		const newUser: UserDescriptionTemplateEditorModel = new UserDescriptionTemplateEditorModel(this.editorModel.validationErrorModel);
		newUser.userId = user.id;
		newUser.role = UserDescriptionTemplateRole.Owner;
		this.usersMap.set(user.id, user);
		(this.formGroup.get('users') as FormArray).push(newUser.buildForm({ rootPath: 'users[' + userArray.length + '].' }));
		this.userFormControl.reset();
	}

	removeUser(index: number) {
		(this.formGroup.get('users') as FormArray).controls.splice(index, 1);
		this.formGroup.get('users').updateValueAndValidity();
		this.reaplyValidators();
		this.formGroup.get('users').markAsDirty();
	}

	verifyAndRemoveUser(index: number) {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(approve => {
			if (approve) {
				this.removeUser(index);
			}
		});
	}

	//
	//
	// Stepper
	//
	//
	onMatStepperSelectionChange(event: StepperSelectionEvent) {

		if (event.selectedIndex === (this.stepper?.steps.length - 1)) {//preview selected
			this.generatePreviewForm();
		}
		this.formGroup.markAsUntouched();
	}

	validateStep(selectedIndex) {

        if (this.formGroup.invalid) {
            this.formService.touchAllFormFields(this.formGroup);
            this.checkFormValidation(selectedIndex);
        }
	}


	//
	//
	// Preview
	//
	//
	generatePreviewForm() {
		this.finalPreviewDescriptionTemplatePersist = this.formGroup.getRawValue();
	}

	//
	//
	// Table of Contents
	//
	//
	private _initializeToCEntries() {
		const tocentries = this.refreshToCEntries();//tocentries are sorted based on their ordinal value

		this._updateOrdinals(tocentries);

		if (tocentries && tocentries.length && !this.selectedTocEntry) {
			this.selectedTocEntry = tocentries[0];
		}

		//Checking invalid visibilty RULES
		const fieldsetEntries = this._getAllFieldSets(this.toCEntries);
		const fieldSetHavingInvalidVisibilityRules: ToCEntry[] = fieldsetEntries
			.filter(entry => {
				const fieldsFormGroup = entry.form.get('fields');
				const invalid = (fieldsFormGroup as UntypedFormArray).controls.filter(field => {
					return this.hasInvalidVisibilityRule(field as UntypedFormGroup);

				});
				if (invalid && invalid.length) {
					return true;
				}
				return false;
			});


		if (fieldSetHavingInvalidVisibilityRules.length) {
			const occurences = fieldSetHavingInvalidVisibilityRules.map(record => record.numbering).join(' , ');
			this.dialog.open(ConfirmationDialogComponent, {
				data: {
					message: this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.ERRORS.INVALID-VISIBILITY-RULES.MESSAGE-START') + occurences + this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.ERRORS.INVALID-VISIBILITY-RULES.MESSAGE-END'),
					confirmButton: this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.ERRORS.INVALID-VISIBILITY-RULES.CONFIRM-YES'),
					cancelButton: this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.ERRORS.INVALID-VISIBILITY-RULES.CONFIRM-NO')
				},
				maxWidth: '30em'
			})
				.afterClosed()
				.subscribe(confirm => {
					if (confirm) {
						this.removeFieldSetVisibilityRules(fieldSetHavingInvalidVisibilityRules);
						this.uiNotificationService.snackBarNotification(this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.ERRORS.INVALID-VISIBILITY-RULES.REMOVE-SUCCESS'), SnackBarNotificationLevel.Success);

					} else {
						console.log('User not confirmed');
					}
				})
		}
	}

	private refreshToCEntries(): ToCEntry[] {
		this.toCEntries = this.getTocEntries();
		//update selected tocentry
		if (this.selectedTocEntry) {
			this.selectedTocEntry = this._findTocEntryById(this.selectedTocEntry.id, this.toCEntries);
		}
		return this.toCEntries;
	}

	/**
	 * 	Updates entries ordinal form value
	 * based on the index they have on the tocentry array.
	 * Tocentries that are on the same level have distinct ordinal value
	 *
	 * @param tocentries
	 *
	 */
	private _updateOrdinals(tocentries: ToCEntry[]) {

		if (!tocentries || !tocentries.length) return;
		tocentries.forEach((e, idx) => {
			const ordinalControl = e.form.get('ordinal');
			if (ordinalControl) {
				ordinalControl.setValue(idx);
				ordinalControl.updateValueAndValidity();
			}
			this._updateOrdinals(e.subEntries);
		});
	}

	getTocEntries(): ToCEntry[] {
		if (this.formGroup == null) { return []; }
		const result: ToCEntry[] = [];

		//build parent pages
		(this.formGroup.get('definition').get('pages') as UntypedFormArray).controls.forEach((pageElement, i) => {

			const page = {
				id: pageElement.get('id').value,
				label: pageElement.get('title').value,
				type: ToCEntryType.Page,
				form: pageElement,
				numbering: (i + 1).toString(),
				subEntriesType: ToCEntryType.Section,
				subEntries: [],
				validationRootPath: 'definition.pages[' + i + ']'
			} as ToCEntry;

			const subEntries = [];
			(pageElement.get('sections') as UntypedFormArray).controls.forEach((sectionElement, i) => {

				const item = {
					id: sectionElement.get('id').value,
					label: sectionElement.get('title').value,
					type: ToCEntryType.Section,
					form: sectionElement,
					numbering: page.numbering + '.' + (subEntries.length + 1),
					validationRootPath: page.validationRootPath + '.sections[' + i + ']'
				} as ToCEntry;
				const sectionItems = this.populateSections(sectionElement.get('sections') as UntypedFormArray, item.numbering, item.validationRootPath);
				const fieldSetItems = this.populateFieldSets(sectionElement.get('fieldSets') as UntypedFormArray, item.numbering, item.validationRootPath);

				if (sectionItems != null) {
					item.subEntries = sectionItems;
					item.subEntriesType = ToCEntryType.Section;
				}

				if (fieldSetItems != null) {
					if (item.subEntries == null) {
						item.subEntries = fieldSetItems;
					} else {
						item.subEntries.push(...fieldSetItems);
					}
					item.subEntriesType = ToCEntryType.FieldSet;

				}
				subEntries.push(item);
			});

			page.subEntries = subEntries;
			result.push(page);
		});

		this._sortToCentries(result);//ordeby ordinal
		this._updateNumbering(result, '');//update nubering if needed
		return result;
	}

	private getNextFieldAfterDeletingTocentry(tce: ToCEntry): any {
		const sameLevelFields: UntypedFormArray = tce.form?.parent?.parent?.get('fieldSets')?.value;

		let tceIndex = -1;

		//find tce index
		for (let i = 0; i < sameLevelFields.length; i++) {
			let section: any = sameLevelFields?.at(i);
			let sectionId = section?.id;
			if (sectionId == tce.id) {
				tceIndex = i;
				break;
			}
		}

		return sameLevelFields.at(tceIndex > 0 ? tceIndex - 1 : tceIndex + 1); //if deleting the first field, find the next one, else find the previous
	}

	private populateSections(sections: UntypedFormArray, existingNumbering: string, validationRootPath: string): ToCEntry[] {
		if (sections == null || sections.controls == null || sections.controls.length == 0) { return null; }

		const result: ToCEntry[] = [];
		sections.controls.forEach((sectionElement, i) => {

			const item = {
				id: sectionElement.get('id').value,
				label: sectionElement.get('title').value,
				type: ToCEntryType.Section,
				form: sectionElement,
				numbering: existingNumbering + '.' + (i + 1),
				validationRootPath: validationRootPath + '.sections[' + i + ']'
			} as ToCEntry;
			const sectionItems = this.populateSections(sectionElement.get('sections') as UntypedFormArray, item.numbering, item.validationRootPath);
			const fieldSetItems = this.populateFieldSets(sectionElement.get('fieldSets') as UntypedFormArray, item.numbering, item.validationRootPath);
			if (sectionItems != null) {
				item.subEntries = sectionItems;
				item.subEntriesType = ToCEntryType.Section;
			}
			if (fieldSetItems != null) {
				if (item.subEntries == null) {
					item.subEntries = fieldSetItems;
				} else {
					item.subEntries.push(...fieldSetItems);
				}
				item.subEntriesType = ToCEntryType.FieldSet;
			}
			result.push(item);
		});

		return result;
	}

	private populateFieldSets(fieldSets: UntypedFormArray, existingNumbering: string, validationRootPath: string): ToCEntry[] {
		if (fieldSets == null || fieldSets.controls == null || fieldSets.controls.length == 0) { return null; }

		const result: ToCEntry[] = [];
		fieldSets.controls.forEach((fieldSetElement, i) => {

			result.push({
				id: fieldSetElement.get('id').value,
				label: fieldSetElement.get('title').value,
				type: ToCEntryType.FieldSet,
				//subEntries: this.populateSections((fieldSetElement.get('fieldSets') as FormArray), existingNumbering + '.' + i),
				form: fieldSetElement,
				numbering: existingNumbering + '.' + (i + 1),
				validationRootPath: validationRootPath
			} as ToCEntry)

		});

		return result;
	}



	private _findTocEntryById(id: string, tocentries: ToCEntry[]): ToCEntry {
		if (!tocentries || !tocentries.length) {
			return null;
		}

		let tocEntryFound = tocentries.find(entry => entry.id === id);

		if (tocEntryFound) {
			return tocEntryFound;
		}

		for (let entry of tocentries) {
			const result = this._findTocEntryById(id, entry.subEntries);
			if (result) {
				tocEntryFound = result;
				break;
			}
		}

		return tocEntryFound ? tocEntryFound : null;
	}

	private reaplyValidators() {
		DescriptionTemplateEditorModel.reApplyDefinitionValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.editorModel.validationErrorModel
			}
		);
	}

	addNewEntry(tce: NewEntryType) {
		const parent = tce.parent;

		const pages = this.formGroup.get('definition').get('pages') as FormArray;
		let pageIndex = -1;

		// define entry type
		switch (tce.childType) {
			case ToCEntryType.Page:
				const page: DescriptionTemplatePageEditorModel = new DescriptionTemplatePageEditorModel(this.editorModel.validationErrorModel);
				page.id = Guid.create().toString();
				if (isNaN(pages.length)) { page.ordinal = 0; } else { page.ordinal = pages.length; }
				const pageForm = page.buildForm({ rootPath: 'definition.pages[' + pages.length + '].' });

				pages.push(pageForm);
				this.refreshToCEntries();
				this.selectedTocEntry = this._findTocEntryById(pageForm.get('id').value, this.toCEntries);

				break;
			case ToCEntryType.Section:

				const section: DescriptionTemplateSectionEditorModel = new DescriptionTemplateSectionEditorModel(this.editorModel.validationErrorModel);
				section.id = Guid.create().toString();
				let sectionsArray: UntypedFormArray;

				if (parent.type === ToCEntryType.Page) {//FIRST LEVEL SECTION
					for (let i = 0; i < pages?.length; i++) {
						let page = pages.at(i);
						let pageId = page.get('id').value;

						if (pageId == parent.id) {
							pageIndex = i;
							break;
						}
					}
					sectionsArray = pages.controls.find(x => x.get('id')?.value === parent.id).get('sections') as UntypedFormArray;
					try {
						const max = sectionsArray.controls.map(control => control.get('ordinal').value)
							.reduce((a, b) => Math.max(a, b));

						section.ordinal = max + 1;
					} catch {
						section.ordinal = sectionsArray.length;

					}

					sectionsArray.push(section.buildForm({ rootPath: 'definition.pages[' + pageIndex + '].sections[' + sectionsArray.length + '].' }));

				} else if (parent.type == ToCEntryType.Section) { //SUBSECTION OF SECTION
					let sectionIndexes: number[] = [];
					for (let j = 0; j < pages.length; j++) {
						const parentSections = pages.at(j).get('sections') as UntypedFormArray;
						sectionIndexes = this.findSectionIndex(parentSections, parent.id);
						if (sectionIndexes && sectionIndexes.length > 0) {
							pageIndex = j;
							break;
						}
					}
					let parentSectionRootPath = '';
					if (sectionIndexes.length > 0) {
						sectionIndexes.forEach(index => {
							parentSectionRootPath = parentSectionRootPath + 'sections[' + index + '].'
						});

						sectionsArray = parent.form.get('sections') as UntypedFormArray;

						//adding page parent MAYBE NOT NEEDED
						try {
							const maxOrdinal = sectionsArray.controls.map(control => control.get('ordinal').value).reduce((a, b) => Math.max(a, b));
							section.ordinal = maxOrdinal + 1;
						} catch {
							section.ordinal = sectionsArray.length;
						}

						sectionsArray.push(section.buildForm({ rootPath: 'definition.pages[' + pageIndex + '].' + parentSectionRootPath + 'sections[' + sectionsArray.length + '].' }));

					}
				} else {
					console.error('Section can only be child of a page or another section');
				}


				const sectionAdded = sectionsArray.at(sectionsArray.length - 1) as UntypedFormGroup;


				this.refreshToCEntries();
				this.selectedTocEntry = this._findTocEntryById(sectionAdded.get('id').value, this.toCEntries);

				break;
			case ToCEntryType.FieldSet:

				let sectionIndexes: number[] = [];
				for (let j = 0; j < pages.length; j++) {
					const parentSections = pages.at(j).get('sections') as UntypedFormArray;
					sectionIndexes = this.findSectionIndex(parentSections, parent.id);
					if (sectionIndexes && sectionIndexes.length > 0) {
						pageIndex = j;
						break;
					}
				}
				let parentSectionRootPath = '';
				if (sectionIndexes.length > 0) {
					sectionIndexes.forEach(index => {
						parentSectionRootPath = parentSectionRootPath + 'sections[' + index + '].'
					});
				}
				if (sectionIndexes.length > 0) {
					//create one field form fieldset
					const field: DescriptionTemplateFieldEditorModel = new DescriptionTemplateFieldEditorModel(this.editorModel.validationErrorModel);
					field.id = Guid.create().toString();
					field.ordinal = 0;//first filed in the fields list

					const fieldSetsArray = parent.form.get('fieldSets') as UntypedFormArray
					const fieldForm = field.buildForm({ rootPath: 'definition.pages[' + pageIndex + '].' + parentSectionRootPath + 'fieldSets[' + fieldSetsArray.length + '].' + 'fields[' + 0 + '].' });

					//give fieldset id and ordinal
					const fieldSet: DescriptionTemplateFieldSetEditorModel = new DescriptionTemplateFieldSetEditorModel(this.editorModel.validationErrorModel);
					const fieldSetId = Guid.create().toString();
					fieldSet.id = fieldSetId;

					try {
						const maxOrdinal = fieldSetsArray.controls.map(control => control.get('ordinal').value).reduce((a, b) => Math.max(a, b));
						fieldSet.ordinal = maxOrdinal + 1;
					} catch {
						fieldSet.ordinal = fieldSetsArray.length;
					}
					const fieldsetForm = fieldSet.buildForm({ rootPath: 'definition.pages[' + pageIndex + '].' + parentSectionRootPath + 'fieldSets[' + fieldSetsArray.length + '].' });

					(fieldsetForm.get('fields') as UntypedFormArray).push(fieldForm);
					fieldSetsArray.push(fieldsetForm);

					this.refreshToCEntries();
					this.selectedTocEntry = this._findTocEntryById(fieldSetId.toString(), this.toCEntries);

					break;
				}

			default:
				break;
		}

		this.formGroup.updateValueAndValidity();
	}

	private findSectionIndex(sectionFormArray: UntypedFormArray, parentId: string): number[] {
		for (let i = 0; i < sectionFormArray?.length; i++) {
			let sectionFormGroup = sectionFormArray.at(i);
			let sectionId = sectionFormGroup.get('id').value;

			const parentSections = sectionFormGroup.get('sections') as UntypedFormArray;

			if (sectionId == parentId) {
				return [i];
			} else if (parentSections && parentSections.length > 0) {
				const indexes: number[] = this.findSectionIndex(parentSections, parentId);
				if (indexes && indexes.length > 0) {
					indexes.unshift(i);
					return indexes;
				}
			}
		}

		return null;
	}

	private getUpdatedSectionFormArray(sectionFormArray: UntypedFormArray, tceId: string): UntypedFormArray {
		for (let i = 0; i < sectionFormArray?.length; i++) {
			let sectionFormGroup = sectionFormArray.at(i);
			let sectionId = sectionFormGroup.get('id').value;

			const parentSections = sectionFormGroup.get('sections') as UntypedFormArray;

			if (sectionId == tceId) {
				sectionFormArray.removeAt(i);
				return sectionFormArray;
				// sectionFormArray.at(i).get('ordinal').patchValue(i);
			} else if (parentSections && parentSections.length > 0) {
				const currentSectionFormArray = this.getUpdatedSectionFormArray(parentSections, tceId);
				if (currentSectionFormArray != null || currentSectionFormArray != undefined) {
					return currentSectionFormArray;
				}
			}
		}

		return null;
	}

	onRemoveEntry(tce: ToCEntry) {

		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this._deleteEntry(tce);
			}
		});
	}

	private _deleteEntry(tce: ToCEntry) {
		const pages = this.formGroup.get('definition').get('pages') as UntypedFormArray;
		let pageIndex = -1;

		//define entry type
		switch (tce.type) {
			case ToCEntryType.Page:
				//get the index
				for (let i = 0; i < pages.length; i++) {
					let page = pages.at(i) as UntypedFormGroup;

					if (page.controls.id.value === tce.id) {
						pageIndex = i;
						break;
					}
				}

				if (pageIndex >= 0) {
					//remove page
					this._updateSelectedItem(tce);
					pages.removeAt(pageIndex);

					//update page ordinals
					for (let i = 0; i < pages.length; i++) {
						pages.at(i).get('ordinal').patchValue(i);
					}

					this.reaplyValidators();
				}
				break;

			case ToCEntryType.Section:

				//FIRST LEVEL SECTION CASE
				let sectionIndex = -1;
				for (let j = 0; j < pages.length; j++) {
					const sections = pages.at(j).get('sections') as UntypedFormArray;
					for (let i = 0; i < sections?.length; i++) {
						let section = sections.at(i);
						let sectionId = section.get('id').value;

						if (sectionId == tce.id) {
							sectionIndex = i;
							pageIndex = j;
							break;
						}
					}
				}


				if (sectionIndex >= 0) { //section found

					const sections = pages.at(pageIndex).get('sections') as UntypedFormArray;

					//remove section
					sections.removeAt(sectionIndex);

					//update ordinal
					for (let i = 0; i < sections.length; i++) {
						sections.at(i).get('ordinal').patchValue(i);
					}
				} else {//NOT FOUND IN FIRST LEVEL CASE

					//LOOK FOR SUBSECTION CASE

					for (let j = 0; j < pages.length; j++) {
						const parentSections = pages.at(j).get('sections') as UntypedFormArray;
						const sectionFormArray = this.getUpdatedSectionFormArray(parentSections, tce.id);

						if (sectionFormArray) {
							//update ordinal
							for (let i = 0; i < sectionFormArray.length; i++) {
								sectionFormArray.at(i).get('ordinal').patchValue(i);
							}
							break;
						}
					}

				}
				this.reaplyValidators();

				break;
			case ToCEntryType.FieldSet:
				const parentFormArray = tce.form.parent as UntypedFormArray;


				let idx = -1;

				for (let i = 0; i < parentFormArray.length; i++) {
					let inspectingField = parentFormArray.at(i);

					if (inspectingField.get('id').value === tce.id) {
						//fieldset found
						idx = i;
						break;
					}
				}

				if (idx >= 0) {//fieldset found
					this._updateSelectedItem(tce);
					parentFormArray.removeAt(idx);

					//patching order
					for (let i = 0; i < parentFormArray.length; i++) {
						parentFormArray.at(i).get('ordinal').patchValue(i);
					}
					this.reaplyValidators();
				}
				break;
			default:
				break;
		}

		//in case selectedtocentrhy is child of the removed element

		this.onDataNeedsRefresh();
		this.formGroup.updateValueAndValidity();

	}

	cloneFieldSet(fieldset: FormGroup, validationRootPath: string) {
		const values = fieldset.getRawValue();
		const parentArray = fieldset.parent as FormArray;

		values.id = Guid.create().toString();
		values.ordinal = parentArray.length;

		values.fields.forEach(element => {
			element.id = Guid.create().toString()
		});


		const clonedModel = new DescriptionTemplateFieldSetEditorModel(this.editorModel.validationErrorModel).fromModel(values);
		const clonedForm = clonedModel.buildForm({ rootPath: validationRootPath + '.fieldSets[' + parentArray.length + ']' + '.fields[' + 0 + '].' });
		parentArray.push(clonedForm);

		//update tocentries and make selected tocentry the cloedn
		let entries = this.refreshToCEntries();

		const entryfound = this._findTocEntryById(clonedForm.get('id').value, entries);
		if (entryfound) {
			this.selectedTocEntry = entryfound;
		}
	}

	private _updateSelectedItem(tce: ToCEntry) {

		if (this.selectedTocEntry) {

			if (this.tocEntryIsChildOf(this.selectedTocEntry, tce)) {
				if (this.selectedTocEntry.type == ToCEntryType.Page) {
					this.selectedTocEntry = null;
				} else {

					const pages = this.formGroup.get('definition').get('pages') as UntypedFormArray;
					//if first level section

					let isFirstLevel: boolean = false;
					for (let j = 0; j < pages.length; j++) {
						const sections = pages.at(j).get('sections') as UntypedFormArray;
						for (let i = 0; i < sections?.length; i++) {
							let section = sections.at(i);
							let sectionId = section.get('id').value;

							if (sectionId == tce.id) {
								isFirstLevel = true;
								break;
							}
						}
					}

					let sameLevelFields: UntypedFormArray = tce.form?.parent?.parent?.get('fieldSets')?.value;
					if (sameLevelFields?.length > 1) {

						const previousField = this.getNextFieldAfterDeletingTocentry(tce);
						const tocentries = this.getTocEntries();
						const previousFieldTocEntry = this._findTocEntryById(previousField?.id, tocentries);

						this.selectedTocEntry = previousFieldTocEntry;

					} else if (sameLevelFields?.length == 1) {
						//scroll to parent

						let parentId = null;
						if (isFirstLevel) {
							parentId = tce.form.get('page').value;
						} else {
							parentId = tce.form.parent.parent.get('id').value
						}

						if (parentId) {
							const tocentries = this.getTocEntries();
							const parent = this._findTocEntryById(parentId, tocentries);

							if (parent) {
								this.selectedTocEntry = parent;
							} else {
								this.selectedTocEntry = null;
							}
						} else {
							this.selectedTocEntry = null;
						}
					} else {
						this.selectedTocEntry = null;
					}
				}
			}
		}
	}

	tocEntryIsChildOf(testingChild: ToCEntry, parent: ToCEntry): boolean {

		if (!testingChild || !parent) return false;

		if (testingChild.id == parent.id) { return true; }

		if (parent.subEntries) {
			let childFound: boolean = false;

			parent.subEntries.forEach(subEntry => {
				if (this.tocEntryIsChildOf(testingChild, subEntry)) {
					childFound = true;
					return true;
				}
			})

			return childFound;
		}
		return false;
	}

	onDataNeedsRefresh(params?) {

		const tocentries = this.refreshToCEntries();

		if (params && params.draggedItemId) {
			if (params.draggedItemId) {
				this.displayItem(this._findTocEntryById(params.draggedItemId, tocentries));
			}
		}
		this.formGroup.markAsDirty();
	}

	displayItem(entry: ToCEntry): void {
		this.selectedTocEntry = entry;
	}

	/**
	 * Get all filedsets in a tocentry array;
	 * @param entries Tocentries to search in
	 * @returns The tocentries that are Fieldsets provided in the entries
	 */
	private _getAllFieldSets(entries: ToCEntry[]): ToCEntry[] {

		const fieldsets: ToCEntry[] = [];
		if (!entries || !entries.length) return fieldsets;


		entries.forEach(e => {
			if (e.type === ToCEntryType.FieldSet) {
				fieldsets.push(e);
			} else {
				fieldsets.push(...this._getAllFieldSets(e.subEntries));
			}
		});
		return fieldsets;
	}

	private _sortToCentries(entries: ToCEntry[]) {
		if (!entries || !entries.length) return;
		entries.sort(this._compareOrdinals);
		entries.forEach(e => {
			this._sortToCentries(e.subEntries)
		});
	}
	private _compareOrdinals(a, b) {

		const aValue = a.form.get('ordinal').value as number;
		const bValue = b.form.get('ordinal').value as number;

		return aValue - bValue;
	}
	private _updateNumbering(entries: ToCEntry[], parentNumbering: string) {
		if (!entries || !entries.length) return;
		let prefix = '';
		if (parentNumbering.length) {
			prefix = parentNumbering + '.';
		}
		entries.forEach((entry, index) => {
			const numbering = prefix + (index + 1);
			entry.numbering = numbering;
			this._updateNumbering(entry.subEntries, numbering);
		})
	}

	//
	//
	// Visibility Rules
	//
	//
	private hasInvalidVisibilityRule(field: UntypedFormGroup): boolean {
		return false;
	}

	private removeFieldSetVisibilityRules(fieldsets: ToCEntry[]) {

		if (!fieldsets || !fieldsets.length) return;

		fieldsets.forEach(fieldset => {
			if (fieldset.type != ToCEntryType.FieldSet) {
				return;
			}
			const fields = fieldset.form.get('fields') as UntypedFormArray;

			fields.controls.forEach(fieldControl => {
				if (this.hasInvalidVisibilityRule(fieldControl as UntypedFormGroup)) {
					try {
						(fieldControl.get('visible').get('rules') as UntypedFormArray).clear();
					} catch { }
				}
			})

		})

	}

	//
	//
	// Other
	//
	//
	scrollOnTop() {
		try {
			const topPage = document.getElementById(this.stepper.selectedIndex + '-step-top');
			topPage?.scrollIntoView({ behavior: 'smooth' });
		} catch (e) {
			console.log(e);
			console.log('coulnd not scroll');
		}
	}

	get numOfPages() {
		return (<UntypedFormArray>this.formGroup.get('definition').get('pages'))?.length;
	}

	checkFormValidation(index: number) {
        switch(index){
            case 0: {
                this.colorizeInvalid = true;
                break;
            }
            case 1: {
                this.showErrors = true;
                break;
            }
        }
	}

	public cancel(): void {
		this.router.navigate([this.routerUtils.generateUrl('/description-templates')]);
	}
}
