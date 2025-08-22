import {
	AfterViewInit,
	Component,
	computed,
	effect,
	Inject,
	input,
	OnDestroy,
	OnInit,
	output,
	Signal,
	ViewChild
} from '@angular/core';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Description, DescriptionPersist, DescriptionStatusPersist } from '@app/core/model/description/description';
import { DescriptionService } from '@app/core/services/description/description.service';
import { Guid } from '@common/types/guid';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpError, HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { catchError, EMPTY, interval, Observable, of, Subscription, take, takeUntil, tap} from 'rxjs';
import { BaseComponent } from '@common/base/base.component';
import { DescriptionEditorForm, DescriptionEditorModel } from '@app/ui/description/editor/description-editor.model';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { TranslateService } from '@ngx-translate/core';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { AbstractControl, FormGroup, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { Plan } from '@app/core/model/plan/plan';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { DescriptionFormComponent } from '@app/ui/description/editor/description-form/description-form.component';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { FormService } from '@common/forms/form-service';
import { MatDialog } from '@angular/material/dialog';
import { DescriptionFormService } from '@app/ui/description/editor/description-form/components/services/description-form.service';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { DescriptionStatusPermission } from '@app/core/common/enum/description-status-permission.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { LockService } from '@app/core/services/lock/lock.service';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { isNullOrUndefined } from '@swimlane/ngx-datatable';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionEditorHelper } from './plan-description-editor-helper';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { PlanTempStorageService } from '../plan-temp-storage.service';
import { UnlockMultipleTargetsPersist } from '@app/core/model/lock/lock.model';
import { ActivatedRoute, Params } from '@angular/router';
import {
	FormAnnotationService,
	MULTI_FORM_ANNOTATION_SERVICE_TOKEN
} from '@app/ui/annotations/annotation-dialog-component/form-annotation.service';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionFinalizeDialogOutput, FinalizeDescriptionDialogComponent } from './finalize-description-dialog/finalize-description-dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AnnotationDialogComponent } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.component';
import { EnqueueService } from '@app/core/services/enqueue.service';
@Component({
    selector: 'app-plan-description-editor',
    templateUrl: './plan-description-editor.component.html',
    styleUrl: './plan-description-editor.component.scss',
    providers: [FormAnnotationService],
    standalone: false
})
export class PlanDescriptionEditorComponent extends BaseComponent implements AfterViewInit, OnDestroy{
    @ViewChild("descriptionForm") descriptionEditorForm: DescriptionFormComponent;
    plan = input<Plan>();
    id = input<Guid>();
    atBaseEditor = input<boolean>();
    descriptionChanged = output<{description: Description, isNew?: boolean}>();
    showErrors = output<Guid>();

    descriptionInfo = computed(() => this.id()? this.planTempStorage.descriptions().get(this.id().toString()) : null);
    section = computed(() => this.plan()?.blueprint?.definition?.sections?.find((x) => x.id === this.descriptionInfo().lastPersist?.planDescriptionTemplate?.sectionId)); 
    ordinal = computed(() => `${this.section().ordinal}.${this.section().fields?.length ? (this.section().fields?.length + 1 + '.') : ''}${this.descriptionInfo().ordinal}`)

    protected planNotFinalized = computed(() => {
		return this.plan()?.status?.internalStatus != PlanStatusEnum.Finalized;
	}) 

    protected formGroup: Signal<FormGroup<DescriptionEditorForm>> = computed(() => this.descriptionInfo()?.formGroup);

    protected description: Signal<Description > = computed(() => this.descriptionInfo()?.lastPersist);
    protected visibilityRulesService: Signal<VisibilityRulesService > = computed(() => this.descriptionInfo()?.visibilityRulesService);
    protected validationErrorModel: Signal<ValidationErrorModel > = computed(() => this.descriptionInfo()?.validationErrorModel);
    
    protected isNew: Signal<boolean> = computed(() => {
        return this.descriptionInfo()?.isNew;
    });
    protected isDeleted: Signal<boolean> = computed(() => {
        return this.descriptionInfo()?.lastPersist?.isActive === IsActive.Inactive;
    });
    protected isFinalized: Signal<boolean> = computed(() => {
        return this.descriptionInfo()?.lastPersist?.status?.internalStatus === DescriptionStatusEnum.Finalized;
    });
	protected descriptionInfoValid: Signal<boolean> = computed(() => {
		return this.descriptionInfo()?.formGroup?.get('label')?.valid && this.formGroup()?.get('descriptionTemplateId')?.valid;

    });
    protected viewOnly: Signal<boolean> = computed(() => {
        const hasEditPermission = this.descriptionInfo()?.lastPersist?.authorizationFlags?.some(x => x === AppPermission.EditDescription);
        return this.isDeleted() || !this.description()?.belongsToCurrentTenant || !hasEditPermission;
    });
    protected canAnnotate: Signal<boolean> = computed(() => {
        const hasAnnotatePermission = this.descriptionInfo()?.lastPersist?.authorizationFlags?.some(x => x === AppPermission.AnnotateDescription);
        return this.description().belongsToCurrentTenant && !this.isNew() && !this.isDeleted() && hasAnnotatePermission;
    });
    protected canEditStatus: Signal<boolean> = computed(() => {
        return this.description().belongsToCurrentTenant && this.descriptionInfo()?.lastPersist?.statusAuthorizationFlags?.some(x => x.toLowerCase() === DescriptionStatusPermission.Edit.toLowerCase());
    });
    protected descriptionTemplate: Signal<DescriptionTemplate> = computed (() => this.planTempStorage.getDescriptionTemplate(this.formGroup()?.controls?.descriptionTemplateId?.getRawValue()))

    private oldStatusId: Guid;
    
    isLocked: boolean = false;
    
    isLoading = computed(() => this.enqueueService.exhaustPipelineBusy());

    private openAnnotation$: Subscription;
	private formAnnotationService: FormAnnotationService;
    constructor(
        private route: ActivatedRoute,
        private descriptionService: DescriptionService,
        private httpErrorHandlingService: HttpErrorHandlingService,
        private language: TranslateService,
        private uiNotificationService: UiNotificationService,
        private formService: FormService,
        private dialog: MatDialog,
        private descriptionFormService: DescriptionFormService,
        private lockService: LockService,
        private authService: AuthService,
        private configurationService: ConfigurationService,
        private planTempStorage: PlanTempStorageService,
        private routerUtils: RouterUtilsService,
        private enqueueService: EnqueueService,
	    @Inject(MULTI_FORM_ANNOTATION_SERVICE_TOKEN) private formAnnotationServices: FormAnnotationService[],
    ){
        super();
        effect(() => {
            const descInfo = this.descriptionInfo();
            if(!descInfo || descInfo.isNew){return;}
            const id = descInfo.lastPersist.id;
            this.openAnnotation$?.unsubscribe();
            
            this.formAnnotationService = this.formAnnotationServices.find(service => service.getEntityId() === id);
            this.openAnnotation$ = this.formAnnotationService?.getOpenAnnotationSubjectObservable().pipe(takeUntil(this._destroyed)).subscribe((anchorFieldsetId: string) => {
                if (anchorFieldsetId){
                    this.showAnnotations(anchorFieldsetId);
                }
            });
        
            this.visibilityRulesService().reloadVisibility();
            if(!this.lockSubscriptionMap.has(id) && !this.isFinalized() && !this.viewOnly()){
                this.lockService.checkLockStatus(id).pipe(takeUntil(this._destroyed))
                .subscribe((lockStatus) => {
                   this.isLocked = lockStatus.status;
                   if(this.isLocked){
                        this.onItemLocked(id);
                   } else {
                       this.lockOnFormChange();
                   }
                })
            }
            if(this.anchorFieldId){
                if(this.openDescAnnotation){
                    this.openAnnotation(this.anchorFieldId);
                } 
                this.anchorFieldId = null;
            }
            this.oldStatusId = this.formGroup().get('statusId').value;
            if(this.finalizeDescription){
                this.finalize();
                this.finalizeDescription = false;
            }
        })
    }
    
    anchorFieldId: string;
    openDescAnnotation: boolean;
    scrollToDescField: boolean;
    finalizeDescription: boolean;
    ngAfterViewInit(): void {
        this.route.params
        .pipe(takeUntil(this._destroyed))
        .subscribe((params: Params) => {
            this.anchorFieldId = params['fieldId'];
            if (this.anchorFieldId) {
                this.openDescAnnotation = this.route.snapshot.data['openDescriptionAnnotation'];
                this.scrollToDescField = this.openDescAnnotation || this.route.snapshot.data['scrollToDescriptionField'];
            }
            this.finalizeDescription = this.route.snapshot.data['finalizeDescription'];
        });
    }
// **** ANNOTATIONS ****
    public openAnnotation(anchorFieldsetId: string){
        this.descriptionFormService.queryIncludesAnnotation = true;
        this.showAnnotations(anchorFieldsetId);
    }

    showAnnotations(fieldSetId: string) {
        const dialogRef = this.dialog.open(AnnotationDialogComponent, {
            width: '40rem',
            maxWidth: '90vw',
            maxHeight: '90vh',
            data: {
                entityId: this.id(),
                anchor: fieldSetId,
                entityType: AnnotationEntityType.Description,
                planUsers: this.plan().planUsers,
                canAnnotate: this.canAnnotate(),
                generateLink: (anchor: string, entityId: Guid) => {
                    let currentPath = window.location.pathname;
                    const rgx = /\/f\/|\/d\/|\/annotation/;
                    currentPath = currentPath.split(rgx)?.[0]; //get base of route, remove any descriptionid, fieldId, or annotation it might contain
                    return this.routerUtils.generateUrl([currentPath, 'd', entityId, 'f', anchor, 'annotation'].join('/'));
                }
            }
        });
        dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(changesMade => {
            if (changesMade) {
                this.formAnnotationService.refreshAnnotations();
            }
        });
    }



// **** LOCK AND UNLOCK ****

    private lockSubscriptionMap = new Map<Guid, Subscription>([]);
    public get lockedIds(): Guid[] {
        return Array.from(this.lockSubscriptionMap.keys());
    }

    public lockedDescriptionIds(): Guid[] {
        return Array.from(this.lockSubscriptionMap.keys() ?? []);
    }

    protected checkAndLock(targetId: Guid, targetType: LockTargetType) {
		if (targetId && !isNullOrUndefined(this.authService.currentAccountIsAuthenticated())) {
			this.lockService.checkAndLock(targetId, targetType).pipe(takeUntil(this._destroyed))
            .subscribe((lockedByUser) => {
                this.isLocked = !lockedByUser;
                if(this.isLocked){
                    this.onItemLocked(targetId);
                } else {
                    this.onItemLockedByUser(targetId);
                }
            })
		}
	}

    private touchLock(targetId: Guid) {
		this.lockService.touchLock(targetId).pipe(takeUntil(this._destroyed)).subscribe(async result => { console.log() }); //TODO HANDLE-ERRORS
	}

    public unlock(id: Guid, relock: boolean = false){
        const subscription = this.lockSubscriptionMap.get(id);
        if(!subscription){
            return;
        }
        subscription.unsubscribe();
        this.lockService.unlockTarget(id).pipe(takeUntil(this._destroyed))
        .subscribe({
            complete: () => {
                this.lockSubscriptionMap.delete(id);
                if(relock){
                    this.lockOnFormChange();
                }
            }
        })
    }

    public unlockAll(): Observable<boolean>{
        if(!this.lockSubscriptionMap?.size){ return of(null); }
        const data: UnlockMultipleTargetsPersist = {
            targetIds: Array.from(this.lockSubscriptionMap.keys())
        }
        return this.lockService.unlockTargetMultiple(data).pipe(
            takeUntil(this._destroyed),
            tap((result) => {
                this.lockSubscriptionMap.forEach((sub) => sub.unsubscribe());
                this.lockSubscriptionMap.clear();
                return result;
            }),
            catchError((error) => {
                this.onCallbackError(error); 
                throw(error)
            })
        )
    }

    private lockOnFormChange(){
        const id = this.id();
        this.formGroup()?.valueChanges
        .pipe(take(1)).subscribe(() => 
            this.checkAndLock(id, LockTargetType.Description)
        )
    }

    private onItemLockedByUser(id: Guid){
        if(id !== this.id()) { return; }
        if(this.formGroup().disabled){
            this.enableForm();
        }
        const touchLock$ = interval(this.configurationService.lockInterval).pipe(takeUntil(this._destroyed)).subscribe(() => {console.log('touch' + id); this.touchLock(id)});
        this.lockSubscriptionMap.set(id, touchLock$);
    }

    private onItemLocked(id: Guid){
        if(id !== this.id()) { return; }
        this.formGroup().disable();
        this.lockSubscriptionMap.get(id)?.unsubscribe();
        this.lockSubscriptionMap.delete(id);
        this.dialog.open(PopupNotificationDialogComponent, {
            data: {
                title: this.language.instant('DESCRIPTION-EDITOR.LOCKED-DIALOG.TITLE'),
                message: this.language.instant('DESCRIPTION-EDITOR.LOCKED-DIALOG.MESSAGE')
            }, maxWidth: '30em'
        });
    }

    enableForm(){
        const description = this.descriptionInfo()?.lastPersist;
        const canEdit = description.isActive === IsActive.Active && description.status?.internalStatus != DescriptionStatusEnum.Finalized && description.authorizationFlags?.includes(AppPermission.EditDescription);
        if(canEdit){
            this.formGroup().enable;
            if(!this.descriptionInfo().isNew){
                this.formGroup().controls.descriptionTemplateId.disable();
            }
        }
    }
////// *****


//// **** BACK END CHANGES ****   
    changeStatus(status: DescriptionStatus) {
		if (status?.internalStatus === DescriptionStatusEnum.Finalized) {
			this.finalize(status.id);
		} else if (status?.internalStatus != null && this.description().status.internalStatus === DescriptionStatusEnum.Finalized){
			this.reverse(status.id);
		} else {
			// other statuses
			this.formGroup().get('statusId').setValue(status.id);
			this.attemptStatusPersist();
		}
	}

    private finalize(statusId?: Guid) {
		this.formService.removeAllBackEndErrors(this.formGroup());
		this.formService.touchAllFormFields(this.formGroup());
		this.formService.validateAllFormFields(this.formGroup());

		this.descriptionFormService.detectChanges(true);

		const dialogRef = this.dialog.open(FinalizeDescriptionDialogComponent, {
			restoreFocus: false,
			data: {
				formGroup: this.formGroup(),
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((result: DescriptionFinalizeDialogOutput) => {
			if (result) {
                if(!result.isValid){
                    this.showErrors.emit(this.id());
        
                    this.formService.touchAllFormFields(this.formGroup());
                    return;
                }
                else if (!result.cancelled){
                    const status = statusId ?? (this.description().availableStatuses?.find(x => x.internalStatus === DescriptionStatusEnum.Finalized)?.id || null);
                    this.formGroup().get('statusId').setValue(status);
                    this.persistStatus();
                }
			}
		});
	}

	private reverse(statusId: Guid) {
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
					id: this.formGroup().get('id').value,
					statusId: statusId,
					hash: this.formGroup().get('hash').value
				};
                this.enqueueService.enqueueExhaustChannel(
                    this.descriptionService.persistStatus(planUserRemovePersist, DescriptionEditorHelper.DescriptionLookupFields())
                    .pipe(
                        takeUntil(this._destroyed),
                        tap((desc) => this.onPersistSuccess(desc)),
                        catchError((error) => of(this.onCallbackError(error)))
                    )
                )
			}
		});
	}

    protected attemptStatusPersist(){
        this.formService.removeAllBackEndErrors(this.formGroup());
		if (!DescriptionEditorModel.baseFieldsAreInvalid(this.formGroup())) 
        {
			this.persistStatus();
		} else {
			const errorMessages = this._buildSemiFormErrorMessages();
			this.showSaveStateValidationErrorsDialog(errorMessages);
		}
    }

    private persistStatus() {
		const formData = this.formGroup()?.getRawValue() as DescriptionPersist;
		const finalizedStatus = this.description()?.availableStatuses?.find(x => x.internalStatus === DescriptionStatusEnum.Finalized) || null;
        this.enqueueService.enqueueExhaustChannel(
            this.descriptionService.persist(formData, DescriptionEditorHelper.DescriptionLookupFields())
            .pipe(
                takeUntil(this._destroyed),
                tap((desc) => this.onPersistSuccess(desc)),
                catchError((error) => {
                    if (finalizedStatus && this.formGroup().get('statusId').value == finalizedStatus.id) {
						this.formGroup().get('statusId').setValue(this.oldStatusId);
						this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.UNSUCCESSFUL-FINALIZE'), SnackBarNotificationLevel.Error);
					} else {
						this.onCallbackError(error);
					}
                    return EMPTY;
                })
            )
        )
    }

    private onPersistSuccess(desc: Description){
        this.unlock(this.id(), true);
        // const newFormGroup = new DescriptionEditorModel().fromModel(desc, desc.descriptionTemplate).buildForm(
        //     null, 
        //     DescriptionEditorModel.isViewOnlyDescription(desc),
        //     this.descriptionInfo().visibilityRulesService
        // );
        // const newDescriptionInfo = {
        //     ...this.descriptionInfo(),
        //     formGroup: newFormGroup,
        //     lastPersist: desc,
        // }
        this.descriptionChanged.emit({description: desc});
    }

    private onCallbackError(errorResponse: HttpErrorResponse) {
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse)
		const error: HttpError = this.httpErrorHandlingService.getError(errorResponse);
        console.log(error);
	}

    
    private _buildSemiFormErrorMessages(): string[] {
		const errmess: string[] = [];
		Object.keys(this.formGroup().controls).forEach(controlName => {
			if (controlName != 'properties' && this.formGroup().get(controlName).invalid) {
				errmess.push(...this._buildErrorMessagesForAbstractControl(this.formGroup().get(controlName), controlName));
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

    private showSaveStateValidationErrorsDialog(errmess?: string[]) {
		if (errmess) {
			const dialogRef = this.dialog.open(FormValidationErrorsDialogComponent, {
				disableClose: true,
				restoreFocus: false,
				data: {
					errorMessages: errmess,
				},
			});
		} else {
			const dialogRef = this.dialog.open(FormValidationErrorsDialogComponent, {
				disableClose: true,
				restoreFocus: false,
				data: {
					formGroup: this.formGroup,
				},
			});
		}
	}

////// ******

    onDescriptionTemplateChange(descriptionTemplateId?: Guid){
        if(descriptionTemplateId){ //change from UI selector
            const descriptionTemplate = this.planTempStorage.getDescriptionTemplate(descriptionTemplateId);
            const planDescriptionTemplate = Array.from(this.planTempStorage.planDescriptionTemplates?.values() ?? []).find((x) => x.currentDescriptionTemplate?.id === descriptionTemplateId);
            this.descriptionChanged.emit({
                description: {
                    ...this.description(),
                    label: this.formGroup()?.value?.label,
                    description: this.formGroup()?.value?.description,
                    properties: null,
                    descriptionTemplate,
                    planDescriptionTemplate
                }, 
                isNew: true
            });
        } else { // update to latest version
            this.descriptionService.getSingle(this.id(), [...DescriptionEditorHelper.BaseDescriptionLookupFields(), ...DescriptionEditorHelper.DescriptionTemplateInDescriptionLookupFields()])
            .pipe(takeUntil(this._destroyed))
            .subscribe({
                next: (description) => {
                    description.plan = this.plan(); 
                    this.descriptionChanged.emit({description});
                },
                error: (error) => this.onCallbackError(error)
            })
        }
    }

    discardChanges() {
		let messageText = "";
		let confirmButtonText = "";
		let cancelButtonText = "";

		if (this.isNew()) {

			messageText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-MESSAGE');
			confirmButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-CONFIRM');
			cancelButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-NEW-DENY');
		} else {

			messageText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-MESSAGE');
			confirmButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-CONFIRM');
			cancelButtonText = this.language.instant('DESCRIPTION-EDITOR.ACTIONS.DISCARD.DISCARD-EDITED-DENY');
		}


		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			restoreFocus: false,
			data: {
				message: messageText,
				confirmButton: confirmButtonText,
				cancelButton: cancelButtonText,
			},
			maxWidth: '40em'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				setTimeout(x => {
                    this._reverseForm();
                    if(!this.isNew()){
                        this.unlock(this.id(), true);
                    }
				});
			}
		});

	}

    private _reverseForm(){
        // const editorModel = new DescriptionEditorModel()
        // const formGroup = editorModel.fromModel(this.description(), this.description().descriptionTemplate)
        //     .buildForm(null, DescriptionEditorModel.isViewOnlyDescription(this.description()), this.visibilityRulesService());
        // const newDescriptionInfo: DescriptionInfo = {
        //     ...this.descriptionInfo(),
        //     formGroup,
        //     validationErrorModel: editorModel.validationErrorModel
        // }
        this.descriptionChanged.emit({description:this.description()});
    }

    private _resetFields(): void {
        const formGroup = this.formGroup;
		formGroup().get('label').reset();
		formGroup().get('description').reset();
		formGroup().get('tags').reset();
		formGroup().get('properties').reset();
		this.formService.removeAllBackEndErrors(formGroup());
		this.formService.validateAllFormFields(formGroup());
		this.descriptionFormService.detectChanges(true);
		formGroup().markAsPristine();
	}

    ngOnDestroy(): void {
        this.unlockAll();
        this.openAnnotation$?.unsubscribe();
        this.openAnnotation$ = null;
    }

}
