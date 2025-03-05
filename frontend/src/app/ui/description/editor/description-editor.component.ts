import { AfterViewInit, ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplatePage, DescriptionTemplateSection } from '@app/core/model/description-template/description-template';
import { Description, DescriptionPersist, DescriptionStatusPersist } from '@app/core/model/description/description';
import { PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PlanDescriptionTemplate } from '@app/core/model/plan/plan';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseEditor } from '@common/base/base-editor';
import { FormService } from '@common/forms/form-service';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { FormAnnotationService } from '../../annotations/annotation-dialog-component/form-annotation.service';
import { DescriptionEditorModel, DescriptionFieldIndicator, DescriptionPropertyDefinitionEditorModel } from './description-editor.model';
import { DescriptionEditorService } from './description-editor.service';
import { DescriptionFormService } from './description-form/components/services/description-form.service';
import { NewDescriptionDialogComponent, NewDescriptionDialogComponentResult } from './new-description/new-description.component';
import { DescriptionEditorEntityResolver } from './resolvers/description-editor-entity.resolver';
import { ToCEntry } from './table-of-contents/models/toc-entry';
import { TableOfContentsService } from './table-of-contents/services/table-of-contents-service';
import { TableOfContentsComponent } from './table-of-contents/table-of-contents.component';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { DescriptionStatusAvailableActionType } from '@app/core/common/enum/description-status-available-action-type';
import { DescriptionStatusPermission } from '@app/core/common/enum/description-status-permission.enum';

@Component({
    selector: 'app-description-editor-component',
    templateUrl: 'description-editor.component.html',
    styleUrls: ['./description-editor.component.scss'],
    providers: [DescriptionEditorService],
    standalone: false
})
export class DescriptionEditorComponent extends BaseEditor<DescriptionEditorModel, Description> implements OnInit, AfterViewInit {
	isNew = true;
	isDeleted = false;
	isCopy = false;
	canEdit = false;
	canAnnotate = false;
	canExport = false;
	item: Description;
	fileTransformerEntityTypeEnum = FileTransformerEntityType;
	showDescriptionTemplateLoader = false;

	viewOnly = false;
	lockStatus: Boolean;
	descriptionIsOnceSaved = false;
	isFinalized = false;
	showTocEntriesErrors = false;
	@ViewChild('table0fContents') table0fContents: TableOfContentsComponent;

	reachedBase: boolean = true;
	reachedLast: boolean = false;
	reachedFirst: boolean = false;
	isNewDescriptionDialogOpen: boolean = false;

	anchorFieldsetId: string;
	scrollToField: boolean = false;
	openAnnotation: boolean = false;

	private initialTemplateId: string = Guid.EMPTY;
	private permissionPerSection: Map<Guid, string[]>;

	oldStatusId: Guid;

	constructor(
		// BaseFormEditor injected dependencies
		public routerUtils: RouterUtilsService,
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
		private descriptionService: DescriptionService,
		private logger: LoggingService,
		private descriptionEditorService: DescriptionEditorService,
		private descriptionTemplateService: DescriptionTemplateService,
		public visibilityRulesService: VisibilityRulesService,
		public fileTransformerService: FileTransformerService,
		public titleService: Title,
		private analyticsService: AnalyticsService,
		private changeDetectorRef: ChangeDetectorRef,
		private tableOfContentsService: TableOfContentsService,
		private descriptionFormService: DescriptionFormService,
		private formAnnotationService: FormAnnotationService,
	) {
		const descriptionLabel: string = route.snapshot.data['entity']?.label;
		if (descriptionLabel) {
			titleService.setTitle(descriptionLabel);
		} else {
			titleService.setTitle('DESCRIPTION-EDITOR.TITLE-EDIT-DESCRIPTION');
		}
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
	}

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionEditor);

		this.permissionPerSection = this.route.snapshot.data['permissions'] as Map<Guid, string[]> ?? new Map<Guid, string[]>();
		this.permissionPerSection = this.route.snapshot.data['permissions'] as Map<Guid, string[]> ?? new Map<Guid, string[]>();

		super.ngOnInit();


		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe((params: Params) => {

				const itemId = params['id'];
				const planId = params['planId'];
				const copyPlanId = params['copyPlanId'];
				const planSectionId = params['planSectionId'];

				const isPublicDescription = params['public'];
				const newPlanId = params['newPlanId'];

				this.scrollToField = this.route.snapshot.data['scrollToField'] ?? false; this.descriptionFormService.queryIncludesField = this.scrollToField;
				this.anchorFieldsetId = params['fieldsetId'] ?? null;
				this.openAnnotation = this.route.snapshot.data['openAnnotation'] ?? false; this.descriptionFormService.queryIncludesAnnotation = this.openAnnotation;

				if (copyPlanId && !planId && planSectionId) this.isCopy = true;

				this.viewOnly = this.viewOnly || isPublicDescription;

				//Regular Editor case
				if (itemId != null && newPlanId == null && this.item.isActive == IsActive.Active) {
					this.checkLock(this.item.id, LockTargetType.Description, 'DESCRIPTION-EDITOR.LOCKED-DIALOG.TITLE', 'DESCRIPTION-EDITOR.LOCKED-DIALOG.MESSAGE');
				}
				else if (planId != null && planSectionId != null) {
					this.isNew = true;
					this.isNewDescriptionDialogOpen = true;
					const dialogRef = this.dialog.open(NewDescriptionDialogComponent, {
						width: '590px',
						minHeight: '200px',
						restoreFocus: false,
						data: {
							plan: this.item.plan,
							planSectionId: planSectionId
						},
						panelClass: 'custom-modalbox'
					});
					dialogRef.afterClosed().subscribe((result: NewDescriptionDialogComponentResult) => {
						if (result) {
							if (result.description != null) {
								this.titleService.setTitle(result.description.label);

								result.description.plan = this.item.plan;
								result.description.planDescriptionTemplate = this.item.planDescriptionTemplate;

								const sectionId = this.item.planDescriptionTemplate.sectionId;
								result.description.planDescriptionTemplate = this.item.plan.planDescriptionTemplates.find(x => x.sectionId == sectionId && x.descriptionTemplateGroupId == result.description.descriptionTemplate.groupId);

								this.prepareForm(result.description);
								this.changeDetectorRef.markForCheck(); // when prefilling a description the "prepareForm" has already being executed from the base-editor and we need to trigger the angular's change-detector manually
								this.descriptionFormService.detectChanges(true);
							} else if (result.description?.descriptionTemplate?.id != null) {
								this.formGroup.get('descriptionTemplateId').setValue(result.description.descriptionTemplate.id);
							}
						}
						this.isNewDescriptionDialogOpen = false;
					})
				}
				if (this.route.snapshot.url[1] && this.route.snapshot.url[1].path == 'finalize' && !this.lockStatus && !this.viewOnly) {
					setTimeout(() => {
						const finalizedStatus = this.item.availableStatuses?.find(x => x.internalStatus === DescriptionStatusEnum.Finalized) || null;
						if (finalizedStatus) this.finalize(finalizedStatus.id);
					}, 0);
				}
			});
	}

	ngAfterViewInit(): void {
		if (this.scrollToField && this.anchorFieldsetId && this.anchorFieldsetId != '') {
			this.descriptionFormService.scrollingToAnchor(this.anchorFieldsetId);
			if (this.openAnnotation) this.formAnnotationService.οpenAnnotationDialog(this.anchorFieldsetId);
		}
	}

	getItem(itemId: Guid, successFunction: (item: Description) => void) {
		this.descriptionService.getSingle(itemId, DescriptionEditorEntityResolver.lookupFields())
			.pipe(map(data => data as Description), takeUntil(this._destroyed))
			.subscribe(
				data => successFunction(data),
				error => this.onCallbackError(error)
			);
	}

	prepareForm(data: Description) {
		try {
			this.editorModel = data ? new DescriptionEditorModel().fromModel(data, data.descriptionTemplate) : new DescriptionEditorModel();
			if (data) {
				if (data.status?.id) this.oldStatusId = data.status.id
				if (data.status?.definition?.availableActions?.filter(x => x === DescriptionStatusAvailableActionType.Export).length > 0) this.canExport = true;
			}
			if (data && data?.plan?.planUsers) data.plan.planUsers = data.plan.planUsers.filter(x => x.isActive === IsActive.Active);
			this.item = data;
			this.initialTemplateId = data?.descriptionTemplate?.id?.toString();
			if (data && data.planDescriptionTemplate?.sectionId && data.plan?.blueprint?.definition?.sections?.length > 0 && data.plan?.descriptions?.length > 0) {
				const section = data.plan?.blueprint?.definition?.sections.find(x => x.id == data.planDescriptionTemplate?.sectionId);
				if (section?.hasTemplates) {
					const notAvailableDescriptionTemplates = this.calculateMultiplicityRejectedPlanDescriptionTemplates(section, data.plan.descriptions.filter(x => x.isActive == IsActive.Active));
					this.item.plan.planDescriptionTemplates = data.plan.planDescriptionTemplates.filter(x => !notAvailableDescriptionTemplates.map(y => y.id).includes(x.id))
				}
			}
			this.isDeleted = data ? data.isActive === IsActive.Inactive : false;
			this.buildForm();
			if (data?.status?.internalStatus == DescriptionStatusEnum.Finalized || this.isDeleted || !this.canEdit) {
				this.viewOnly = true;
				this.isFinalized = true;
				this.formGroup.disable();
			} else {
				this.viewOnly = false;
			}
			if (this.isDeleted || (this.formGroup && this.editorModel.belongsToCurrentTenant == false)) {
				this.formGroup.disable();
				this.canEdit = false;
			}
		} catch (error) {
			this.logger.error('Could not parse Description item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.canEdit = !this.isDeleted && this.permissionPerSection && this.permissionPerSection[this.item.planDescriptionTemplate.sectionId.toString()] && this.permissionPerSection[this.item.planDescriptionTemplate.sectionId.toString()].some(x => x === AppPermission.EditDescription);
		this.canAnnotate = !this.isDeleted && this.permissionPerSection && this.permissionPerSection[this.item.planDescriptionTemplate.sectionId.toString()] && this.permissionPerSection[this.item.planDescriptionTemplate.sectionId.toString()].some(x => x === AppPermission.AnnotateDescription);
		this.formGroup = this.editorModel.buildForm(null, this.isDeleted || !this.canEdit, this.visibilityRulesService);
		if (this.item.descriptionTemplate?.definition) this.visibilityRulesService.setContext(this.item.descriptionTemplate.definition, this.formGroup.get('properties'));

		// this.selectedSystemFields = this.selectedSystemFieldDisabled();
		this.descriptionEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);

		this.registerFormListeners();
	}

	calculateMultiplicityRejectedPlanDescriptionTemplates(section: PlanBlueprintDefinitionSection, descriptions: Description[]): PlanDescriptionTemplate[] {
		if (section.descriptionTemplates?.length > 0) {
			descriptions = descriptions?.filter(x => x?.planDescriptionTemplate?.sectionId === section.id) || [];

			let rejectedPlanDescriptionTemplates: PlanDescriptionTemplate[] = [];
			section.descriptionTemplates.forEach(sectionDescriptionTemplate => {
				if (sectionDescriptionTemplate.maxMultiplicity != null) {
					const commonDescriptions = descriptions.filter(x => x.planDescriptionTemplate.descriptionTemplateGroupId == sectionDescriptionTemplate.descriptionTemplate?.groupId);

					if (commonDescriptions && commonDescriptions.length > sectionDescriptionTemplate.maxMultiplicity) {
						rejectedPlanDescriptionTemplates.push.apply(rejectedPlanDescriptionTemplates, commonDescriptions.map(x => x.planDescriptionTemplate));
					}
				}
			})
			return rejectedPlanDescriptionTemplates;
		} else {
			return [];
		}
	}

	fireRefreshDataEvent(event: boolean): void {
		if (event) {
			this.refreshData();
		}
	}

	refreshData(): void {
		this.getItem(this.editorModel.id, (data: Description) => {
			this.prepareForm(data)
		});
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();

		if (this.isNew || this.isCopy) {
			let route = [];
			route.push(this.routerUtils.generateUrl('/descriptions/edit/' + id));
			this.router.navigate(route, { queryParams: { 'lookup': this.queryParamsService.serializeLookup(this.lookupParams), 'lv': ++this.lv }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData();
		}
	}

	persistEntity(onSuccess?: (response) => void): void {
		const formData = this.formService.getValue(this.formGroup.value) as DescriptionPersist;
		const finalizedStatus = this.item.availableStatuses?.find(x => x.internalStatus === DescriptionStatusEnum.Finalized) || null;

		this.descriptionService.persist(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => {
					onSuccess ? onSuccess(complete) : this.onCallbackSuccess(complete);
					this.descriptionIsOnceSaved = true;
					if (finalizedStatus && this.formGroup.get('statusId').value == finalizedStatus.id) {
						this.isFinalized = true;
						this.formGroup.disable();
					}
				},
				error => {
					if (finalizedStatus && this.formGroup.get('statusId').value == finalizedStatus.id) {
						this.formGroup.get('statusId').setValue(this.oldStatusId);
						this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-FINALIZE'), SnackBarNotificationLevel.Error);
					} else {
						this.onCallbackError(error);
					}
				}
			);
	}

	formSubmit(onSuccess?: (response) => void): void {
		this.formService.removeAllBackEndErrors(this.formGroup);
		if (this.formGroup.get('label').valid && this.formGroup.get('planId').valid && this.formGroup.get('planDescriptionTemplateId').valid
			&& this.formGroup.get('descriptionTemplateId').valid) {// && this.formGroup.get('statusId').valid) {
			this.persistEntity(onSuccess);
		} else {
			const errorMessages = this._buildSemiFormErrorMessages();
			this.showSaveStateValidationErrorsDialog(undefined, errorMessages);
		}
	}

	saveAndClose() {
		this.formSubmit((data) => {
			this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
			const planId = this.formGroup.get('planId').value;
			this.formGroup = null;
			this.backToPlan(planId);
		});
	}

	saveAndContinue() {
		this.formSubmit((data) => {
			this.uiNotificationService.snackBarNotification(this.isNew ? this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-CREATION') : this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
			this.refreshOnNavigateToData(data ? data.id : null);
		});
	}

	discardChanges() {
		let messageText = "";
		let confirmButtonText = "";
		let cancelButtonText = "";
		let isDeleteConfirmation = false;

		if (this.isNew && !this.descriptionIsOnceSaved) {

			messageText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-MESSAGE');
			confirmButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-CONFIRM');
			cancelButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-DENY');
			isDeleteConfirmation = true;
		} else {

			messageText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-MESSAGE');
			confirmButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-CONFIRM');
			cancelButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-DENY');
			isDeleteConfirmation = false;
		}


		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: messageText,
				confirmButton: confirmButtonText,
				cancelButton: cancelButtonText,
				isDeleteConfirmation: true
			},
			maxWidth: '40em'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				setTimeout(x => {
					if (this.isNew) this._resetFields();
					else this.refreshOnNavigateToData(this.editorModel?.id);
				});
			}
		});

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
					this.descriptionService.delete(value.id).pipe(takeUntil(this._destroyed))
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

	backToPlan(planId) {
		this.router.navigate([this.routerUtils.generateUrl(['/plans/', 'edit/', planId])]);
	}

	private showSaveStateValidationErrorsDialog(projectOnly?: boolean, errmess?: string[]) {
		if (errmess) {
			const dialogRef = this.dialog.open(FormValidationErrorsDialogComponent, {
				disableClose: true,
				restoreFocus: false,
				data: {
					errorMessages: errmess,
					projectOnly: projectOnly
				},
			});
		} else {

			const dialogRef = this.dialog.open(FormValidationErrorsDialogComponent, {
				disableClose: true,
				restoreFocus: false,
				data: {
					formGroup: this.formGroup,
					projectOnly: projectOnly
				},
			});
		}
	}

	private _buildSemiFormErrorMessages(): string[] {
		const errmess: string[] = [];
		Object.keys(this.formGroup.controls).forEach(controlName => {
			if (controlName != 'properties' && this.formGroup.get(controlName).invalid) {
				errmess.push(...this._buildErrorMessagesForAbstractControl(this.formGroup.get(controlName), controlName));
			}
		})

		return errmess;
	}

	// takes as an input an abstract control and gets its error messages[]
	private _buildErrorMessagesForAbstractControl(aControl: AbstractControl, controlName: string): string[] {
		const errmess: string[] = [];

		if (aControl.invalid) {

			if (aControl.errors) {
				//check if has placeholder
				if ((<any>aControl).nativeElement !== undefined && (<any>aControl).nativeElement !== null) {
					const placeholder = this._getPlaceHolder(aControl);
					if (placeholder) {
						controlName = placeholder;
					}
				}
				const errorMessage = this._getErrorMessage(aControl, controlName);

				errmess.push(...errorMessage);
			}

			/*in case the aControl is FormControl then the it should have provided its error messages above.
			No need to check case of FormControl below*/

			if (aControl instanceof UntypedFormGroup) {

				const fg = aControl as UntypedFormGroup;
				//check children
				Object.keys(fg.controls).forEach(controlName => {
					errmess.push(...this._buildErrorMessagesForAbstractControl(fg.get(controlName), controlName));
				});
			} else if (aControl instanceof UntypedFormArray) {

				const fa = aControl as UntypedFormArray;

				fa.controls.forEach((control, index) => {
					errmess.push(...this._buildErrorMessagesForAbstractControl(control, `${controlName} --> ${index + 1}`));
				});

			}
		}

		return errmess;
	}

	private _getErrorMessage(formControl: AbstractControl, name: string): string[] {
		const errors: string[] = [];
		Object.keys(formControl.errors).forEach(key => {
			if (key === 'required') {
				if (name == 'label') errors.push(this.language.instant(this.language.instant('DESCRIPTION-EDITOR.BASE-INFO.FIELDS.TITLE') + ": " + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.REQUIRED')));
				else if (name == 'descriptionTemplateId') errors.push(this.language.instant(this.language.instant('DESCRIPTION-EDITOR.BASE-INFO.FIELDS.DESCRIPTION-TEMPLATE') + ": " + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.REQUIRED')));
			}
			else if (key === 'email') {
				errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + name + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.EMAIL'));
			} else if (key === 'min') {
				errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + name + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.MIN-VALUE', { 'min': formControl.getError('min').min }));
			} else if (key === 'max') {
				errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + name + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.MAX-VALUE', { 'max': formControl.getError('max').max }));
			} else {
				errors.push(this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.THIS-FIELD') + ' "' + name + '" ' + this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.HAS-ERROR') + ', ' + formControl.errors[key].message);
			}
		});
		return errors;
	}

	private _getPlaceHolder(formControl: any): string {
		if (formControl.nativeElement.localName === 'input' || formControl.nativeElement.localName === 'textarea'
			|| formControl.nativeElement.localName === 'richTextarea') {
			return formControl.nativeElement.getAttribute('placeholder');
		} else if (formControl.nativeElement.localName === 'mat-select') {
			return formControl.nativeElement.getAttribute('placeholder');
		} else if (formControl.nativeElement.localName === 'app-single-auto-complete') {
			return (Array.from(formControl.nativeElement.firstChild.children).filter((x: any) => x.localName === 'input')[0] as any).getAttribute('placeholder');
		} else if (formControl.nativeElement.localName === 'app-multiple-auto-complete') {
			return (Array.from(formControl.nativeElement.firstChild.firstChild.firstChild.children).filter((x: any) => x.localName === 'input')[0] as any).getAttribute('placeholder');
		}
	}


	//
	//
	// Misc
	//
	//

	isDirty() {
		return this.formGroup.dirty;
	}

	isPristine() {
		return this.formGroup.pristine; //use for discard
	}

	hasReversableStatus(): boolean {
		if (this.item?.plan) {
			return (this.item.plan.status?.internalStatus == PlanStatusEnum.Draft && this.isFinalized);
		} else {
			return false;
		}
	}

	isNotFinalizedPlan(): boolean {
		return this.item.plan?.status?.internalStatus != PlanStatusEnum.Finalized;
	}

	descriptionInfoValid(): boolean {
		return this.formGroup.get('label') && this.formGroup.get('label').valid && this.formGroup.get('descriptionTemplateId') && this.formGroup.get('descriptionTemplateId').valid;
	}


	//
	//
	// Table of Contents
	//
	//
	public changeStep(selected: ToCEntry = null, execute: boolean = true) {
		if (execute) {
			if (selected) {
				this.reachedBase = false;
				this.reachedFirst = selected.isFirstEntry;
				this.reachedLast = selected.isLastEntry;

				const element = document.getElementById(selected.id);
				if (element) {
					setTimeout(() => element.scrollIntoView({ behavior: 'smooth' }), );
				}
			} else {
				this.reachedBase = true;
				this.reachedFirst = false;
				this.reachedLast = false;

				this.resetScroll();
			}
		}
	}

	public nextStep() {
		this.tableOfContentsService.nextClicked();
	}

	public previousStep() {
		this.tableOfContentsService.previousClicked();
	}

	private scroll(entry: ToCEntry) {
		document.getElementById(entry.id).scrollIntoView();
	}

	private resetScroll() {
		document.getElementById('description-editor-form').scrollTop = 0;
	}

	get countErrorsOfBaseInfoPage(): number {
		if (this.formGroup == null) return 0;

		let errorsCount: number = 0;

		const baseInfoControlNames: string[] = [nameof<DescriptionEditorModel>(x => x.label), nameof<DescriptionEditorModel>(x => x.descriptionTemplateId)];
		baseInfoControlNames.forEach((name: string) => {
			if (this.formGroup.get(name)?.touched && !this.formGroup.get(name)?.valid) errorsCount += 1;
		});

		return errorsCount;
	}

	get canEditStatus(): boolean{
        return this.item.statusAuthorizationFlags?.some(x => x.toLowerCase() === DescriptionStatusPermission.Edit.toLowerCase())
    }

	registerFormListeners() {

		this.formGroup.get('descriptionTemplateId').valueChanges
			.pipe(takeUntil(this._destroyed))
			.subscribe(descriptionTemplateId => {
				if (descriptionTemplateId) {
					if (this.initialTemplateId && this.initialTemplateId != '' && descriptionTemplateId === this.initialTemplateId) return;
					this.descriptionTemplateValueChanged(descriptionTemplateId);
				}
			});
	}

	descriptionTemplateValueChanged(descriptionTemplateId: Guid) {
		if (descriptionTemplateId != null) {

			this.descriptionTemplateService.getSingle(descriptionTemplateId, DescriptionEditorEntityResolver.descriptionTemplateLookupFields()).pipe(takeUntil(this._destroyed))
				.subscribe(descriptionTemplate => {

					this.initialTemplateId = descriptionTemplateId.toString();
					this.editorModel.properties = new DescriptionPropertyDefinitionEditorModel(this.editorModel.validationErrorModel).fromModel(null, descriptionTemplate, null);
					this.formGroup.setControl('properties', this.editorModel.buildProperties(this.visibilityRulesService));
					this.item.descriptionTemplate = descriptionTemplate;

					const sectionId = this.item.planDescriptionTemplate.sectionId;
					this.item.planDescriptionTemplate = this.item.plan.planDescriptionTemplates.find(x => x.sectionId == sectionId && x.descriptionTemplateGroupId == descriptionTemplate.groupId && x.isActive === IsActive.Active);
					this.formGroup.get('planDescriptionTemplateId').setValue(this.item.planDescriptionTemplate.id);
					if (descriptionTemplate.definition) this.visibilityRulesService.setContext(this.item.descriptionTemplate.definition, this.formGroup.get('properties'));

					if (this.formGroup.get('label').value == null || this.formGroup.get('label').value.length == 0) {
						this.formGroup.get('label').setValue(descriptionTemplate.label);
					}
					this.registerFormListeners();
				},
					error => {
						this.formGroup.get('descriptionTemplateId').setValue(this.initialTemplateId && this.initialTemplateId != '' ? this.initialTemplateId : null);
						this.httpErrorHandlingService.handleBackedRequestError(error);
					});
		}
	}

	getFieldsetsOfPage(page: DescriptionTemplatePage): DescriptionFieldIndicator[] {
		const fieldsByPage: DescriptionFieldIndicator[] = []

		page.sections?.forEach((section: DescriptionTemplateSection) => {
			let fieldsets = this.getNestedSectionFieldsets(section);
			let fieldsBySection: DescriptionFieldIndicator[] = fieldsets?.flatMap((fieldset: DescriptionTemplateFieldSet) =>
				fieldset.fields?.flatMap((field: DescriptionTemplateField) => {
					let sectionIds = this.getNestedSectionIdsByField(section, fieldset.id);
					return new DescriptionFieldIndicator(page.id, sectionIds, fieldset.id, field.id, field.data.fieldType, field.data.multipleSelect)
				}
				));

			fieldsByPage.push(...fieldsBySection);
		});

		return fieldsByPage;
	}

	getNestedSectionFieldsets(section: DescriptionTemplateSection): DescriptionTemplateFieldSet[] {
		if (section.sections) {
			return section.sections.flatMap((subsection: DescriptionTemplateSection) => this.getNestedSectionFieldsets(subsection));
		}

		else return section.fieldSets;
	}

	getNestedSectionIdsByField(section: DescriptionTemplateSection, fieldSetId: string): string[] {
		if (section.sections) {
			const subNestedSectionIds: string[] = section.sections.flatMap((subsection: DescriptionTemplateSection) => this.getNestedSectionIdsByField(subsection, fieldSetId))
			if (subNestedSectionIds.length > 0) return [section.id, ...subNestedSectionIds];
		}

		else if (section.fieldSets.find(fieldSet => fieldSet.id == fieldSetId)) return [section.id];

		return [];
	}

	persistStatus(status: DescriptionStatus) {
		if (status.internalStatus != null && status.internalStatus === DescriptionStatusEnum.Finalized) {
			this.finalize(status.id);
		} else if (status.internalStatus != null && this.item.status.internalStatus === DescriptionStatusEnum.Finalized){
			this.reverse(status.id);
		} else {
			// other statuses
			this.formGroup.get('statusId').setValue(status.id);
			this.persistEntity();
		}
	}

	finalize(statusId: Guid) {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		this.formService.validateAllFormFields(this.formGroup);

		this.descriptionFormService.detectChanges(true);

		this.showTocEntriesErrors = true;
		if (!this.isFormValid()) {
			this.dialog.open(FormValidationErrorsDialogComponent, {
				data: {
					errorMessages: [this.language.instant('DESCRIPTION-EDITOR.MESSAGES.MISSING-FIELDS')]
				}
			})

			this.formService.touchAllFormFields(this.formGroup);
			return;
		}
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
				this.formGroup.get('statusId').setValue(statusId);
				this.persistEntity();
			}
		});
	}

	reverse(statusId: Guid) {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('DESCRIPTION-EDITOR.ACTIONS.UNDO-FINALIZATION-QUESTION'),
				confirmButton: this.language.instant('DESCRIPTION-EDITOR.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('DESCRIPTION-EDITOR.ACTIONS.REJECT'),
				isDeleteConfirmation: false
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				const planUserRemovePersist: DescriptionStatusPersist = {
					id: this.formGroup.get('id').value,
					statusId: statusId,
					hash: this.formGroup.get('hash').value
				};
				this.descriptionService.persistStatus(planUserRemovePersist, DescriptionEditorEntityResolver.lookupFields()).pipe(takeUntil(this._destroyed))
					.subscribe(data => {
						this.isFinalized = false;
						this.refreshData();
						this.onCallbackSuccess();
					}, (error: any) => {
						this.onCallbackError(error)
					});
			}
		});
	}

	private _resetFields(): void {
		this.formGroup.get('label').reset();
		this.formGroup.get('description').reset();
		this.formGroup.get('tags').reset();
		this.formGroup.get('properties').reset();
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.validateAllFormFields(this.formGroup);
		this.descriptionFormService.detectChanges(true);
		this.formGroup.markAsPristine();
	}
}
