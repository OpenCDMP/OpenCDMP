import { Component, computed, effect, EventEmitter, input, Output, SecurityContext, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormControl, UntypedFormArray, UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AnnotationStatusPersist } from '@annotation-service/core/model/annotation-status.model';
import { Annotation, AnnotationPersist } from '@annotation-service/core/model/annotation.model';
import { Status } from '@annotation-service/core/model/status.model';
import { StatusLookup } from '@annotation-service/core/query/status.lookup';
import { AnnotationService } from '@annotation-service/services/http/annotation.service';
import { AnnotationProtectionType } from '@app/core/common/enum/annotation-protection-type.enum';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { AnnotationStatusArrayEditorModel } from './annotation-status-editor.model';
import { StatusService } from '@annotation-service/services/http/status.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { PlanUser } from '@app/core/model/plan/plan';
import { DomSanitizer } from '@angular/platform-browser';
import { Subject } from 'rxjs';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { AnnotationPermission } from '@annotation-service/core/enum/permission.enum';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';

interface AnnotationPayloadItem {
	isMention: boolean;
	payload: string;
}
import { EnqueueService } from '@app/core/services/enqueue.service';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { FallbackAvatarDirective } from '../../../../common/http/image/fallback-avatar.directive';
import { NgTemplateOutlet, NgClass } from '@angular/common';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { TextAreaWithMentionsComponent } from '../components/text-area-with-mentions.component';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { UserLogoPipe } from '../../../../common/http/image/user-logo.pipe';
import { RelativeTimeFormatPipe } from '../../../core/pipes/relative-time.pipe';

@Component({
    selector: 'app-annotation-dialog',
    templateUrl: './annotation-dialog.component.html',
    styleUrls: ['./annotation-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, MatTooltip, FallbackAvatarDirective, NgTemplateOutlet, FormsModule, ReactiveFormsModule, MatButton, NgClass, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, TextAreaWithMentionsComponent, MatButtonToggleGroup, MatButtonToggle, TranslatePipe, UserLogoPipe, RelativeTimeFormatPipe]
})
export class AnnotationDialogComponent extends BaseComponent implements OnDestroy {

	annotationProtectionTypeEnumValues = this.enumUtils.getEnumValues<AnnotationProtectionType>(AnnotationProtectionType);
	annotationProtectionTypeEnum = AnnotationProtectionType;

    annotations = input<Annotation[]>([]);
	planUserMap = input<Map<Guid, PlanUser[]>>(new Map([]));
    uniqueActivePlanUsers = computed(() => {
        if(!this.planUserMap()?.size){ return [] }
        const res = [];
        this.planUserMap()?.forEach((entry) => {
            if(entry?.length){
                res.push(entry[0])
            }
        })
        return res;
    })
    canAnnotate = input<boolean>(false);
    entityId = input<Guid>();
    entityType = input<string | AnnotationEntityType>();
    anchor = input<string>();
    generateLink = input<(anchor: string, entityId: Guid) => string>();
    canCreateComment = input<boolean>(false);

    @Output() onChanges = new EventEmitter<void>();


	public comments = new Array<Annotation>();
	public threads = new Set<Guid>();
	public annotationsCount: number = 0;
	public annotationsPerThread = {};
	public showRepliesPerThread = {};
	public replyEnabledPerThread = {};
	public parentAnnotationsPerThread = {};
	threadReplyTextsFG: Array<UntypedFormGroup>;
	threadFormGroup: UntypedFormGroup;
	private formBuilder: FormBuilder = new FormBuilder();
	public annotationStatusFormGroup: UntypedFormGroup;
	public listingStatuses: Status[] = [];

	MENTION_CLASS: string = "highlight-user-mention"

	clearCommentSubject: Subject<any> = new Subject<any>();
	ROOT_COMMENT: string = 'root';

	editingAnnotationId: Guid | null = null;
	editFormControl: FormControl | null = null;
	private rawPayloadPerAnnotation: { [annotationId: string]: string } = {};

	isLoading = this.enqueueService.exhaustPipelineBusy;

	constructor(
		public dialog: MatDialog,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
		private annotationService: AnnotationService,
		private formService: FormService,
		public enumUtils: EnumUtils,
		private statusService: StatusService,
		protected routerUtils: RouterUtilsService,
		private configurationService: ConfigurationService,
		private sanitizer: DomSanitizer,
		private enqueueService: EnqueueService,
	) {
		super();
        this.threadFormGroup = new UntypedFormGroup({
            text: new FormControl(null, [Validators.required]),
            protectionType: new FormControl(AnnotationProtectionType.EntityAccessors, [Validators.required])
        });
        effect(() => {
            if(!this.canAnnotate()){
                this.threadFormGroup.disable();
            }
        })
        effect(() => {
            if(this.annotations()){
                this.resetFormGroup();
                this.clearCommentSubject?.next(this.ROOT_COMMENT);
                this.prepareThreads(this.annotations(), this.uniqueActivePlanUsers());
            }
        })
        this.getStatuses();
	}

	createThread() {
		this.formService.removeAllBackEndErrors(this.threadFormGroup);
		this.formService.touchAllFormFields(this.threadFormGroup);
		if (!this.isFormValid(this.threadFormGroup)) {
			return;
		}
		const threadToCreate: AnnotationPersist = {
			threadId: Guid.create(),
			payload: this.threadFormGroup.get('text').value,
			protectionType: this.threadFormGroup.get('protectionType').value,
			entityId: this.entityId(),
			entityType: this.entityType(),
			anchor: this.anchor()
		};
		this.enqueueService.enqueueExhaustChannel(this.annotationService.persist(threadToCreate)).pipe(takeUntil(this._destroyed))
			.subscribe(
				complete => {
					this.clearCommentSubject.next(this.ROOT_COMMENT);
					this.onCallbackSuccess();
				},
				error => this.onCallbackError(error)
			);
	}

    cancelReply(threadId: Guid){
        this.replyEnabledPerThread[threadId.toString()] = false;
        this.clearCommentSubject.next(threadId);
    }

	replyThread(threadId: Guid) {
		this.formService.removeAllBackEndErrors(this.threadReplyTextsFG[threadId.toString()]);
		this.formService.touchAllFormFields(this.threadReplyTextsFG[threadId.toString()]);
		if (!this.isFormValid(this.threadReplyTextsFG[threadId.toString()])) {
			return;
		}
		const replyToCreate: AnnotationPersist = {
			threadId: threadId,
			payload: this.threadReplyTextsFG[threadId.toString()].get('replyText').value,
			protectionType: this.parentAnnotationsPerThread[threadId.toString()].protectionType,
			entityId: this.entityId(),
			entityType: this.entityType(),
			anchor: this.anchor(),
			parentId: this.parentAnnotationsPerThread[threadId.toString()].id
		};
		this.enqueueService.enqueueExhaustChannel(this.annotationService.persist(replyToCreate)).pipe(takeUntil(this._destroyed))
			.subscribe(
				complete => {
					this.clearCommentSubject.next(threadId);
					this.onCallbackSuccess()
				},
				error => this.onCallbackError(error)
			);
	}

	private refreshAnnotations() {
		this.threadReplyTextsFG.forEach(element => {
			element.reset();
		});
		this.onChanges.emit();
	}

	canEdit(annotation: Annotation): boolean {
		return !!annotation?.authorizationFlags?.includes(AnnotationPermission.EditAnnotation);
	}

	canDelete(annotation: Annotation): boolean {
		return !!annotation?.authorizationFlags?.includes(AnnotationPermission.DeleteAnnotation);
	}

	isEditing(annotation: Annotation): boolean {
		if (!annotation || !annotation.id || !this.editingAnnotationId) return false;
		return this.editingAnnotationId.toString() === annotation.id.toString();
	}

	startEdit(annotation: Annotation): void {
		if (!annotation || !annotation.id) return;
		const rawPayload = this.rawPayloadPerAnnotation[annotation.id.toString()] ?? '';
		this.editingAnnotationId = annotation.id;
		this.editFormControl = new FormControl(rawPayload, [Validators.required]);
	}

	cancelEdit(): void {
		this.editingAnnotationId = null;
		this.editFormControl = null;
	}

	saveEdit(annotation: Annotation): void {
		if (!annotation || !annotation.id || !this.editFormControl) return;
		this.formService.touchAllFormFields(this.editFormControl);
		if (!this.editFormControl.valid) return;

		const updatedPayload: string = this.editFormControl.value;
		const update: AnnotationPersist = {
			id: annotation.id,
			hash: annotation.hash,
			threadId: annotation.threadId,
			payload: updatedPayload,
			protectionType: annotation.protectionType,
			entityId: this.entityId(),
			entityType: this.entityType(),
			anchor: this.anchor(),
			parentId: annotation.parent ? annotation.parent.id : undefined
		};

		this.enqueueService.enqueueExhaustChannel(this.annotationService.persist(update)).pipe(takeUntil(this._destroyed))
			.subscribe(
				complete => {
					this.cancelEdit();
					this.onCallbackSuccess();
				},
				error => this.onCallbackError(error)
			);
	}

	deleteAnnotation(annotation: Annotation): void {
		if (!annotation || !annotation.id) return;
		const annotationId: Guid = annotation.id;
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: annotation?.parent?.id == null ? this.language.instant('ANNOTATION-DIALOG.DELETE-CONFIRMATION.THREAD-MESSAGE'): this.language.instant('ANNOTATION-DIALOG.DELETE-CONFIRMATION.MESSAGE'),
				confirmButton: this.language.instant('ANNOTATION-DIALOG.DELETE-CONFIRMATION.CONFIRM'),
				cancelButton: this.language.instant('ANNOTATION-DIALOG.DELETE-CONFIRMATION.CANCEL')
			},
			maxWidth: '35em'
		})
			.afterClosed()
			.pipe(takeUntil(this._destroyed))
			.subscribe(confirm => {
				if (!confirm) return;
				this.annotationService.delete(annotationId).pipe(takeUntil(this._destroyed))
					.subscribe({
						next: () => {
							if (this.isEditing(annotation)) this.cancelEdit();
							this.onCallbackSuccess();
						},
						error: error => this.onCallbackError(error)
					});
			});
	}

    protected loadingResults = false;
	private prepareThreads(threads: Annotation[], users: PlanUser[]) {
        // copies: the display HTML written into payload below must not reach the shared objects, which other renders read as raw payload
        const annotations = threads.map(x => ({ ...x }));
        this.annotationsCount = annotations.length;
        this.annotationsPerThread = {};
        this.parentAnnotationsPerThread = {};
        this.rawPayloadPerAnnotation = {};
        this.threads = new Set();
        this.threadReplyTextsFG = new Array<UntypedFormGroup>();
        this.cancelEdit();
        this.comments = annotations.sort((a1, a2) => new Date(a2.timeStamp).getTime() - new Date(a1.timeStamp).getTime());
        annotations.forEach(item => {
            if (item.id) this.rawPayloadPerAnnotation[item.id.toString()] = item.payload;
        });
        this.comments.forEach(element => {
            this.threadReplyTextsFG[element.threadId.toString()] = this.formBuilder.group({ replyText: new FormControl(null, [Validators.required]) });
            this.annotationsPerThread[element.threadId.toString()] = annotations.filter(x => x.threadId === element.threadId && x.id !== element.id).sort((a1, a2) => new Date(a1.timeStamp).getTime() - new Date(a2.timeStamp).getTime());

            const parentAnnotation: any = annotations.filter(x => x.threadId === element.threadId && x.id === element.id)[0];
            const annotationItems = this.parsePayload(parentAnnotation.payload, users);

            parentAnnotation.payload = annotationItems.map(item => {
                if (item.isMention) return `<span class="${this.MENTION_CLASS}">${item.payload}</span>`
                else return item.payload;
            }).join('');

            this.parentAnnotationsPerThread[element.threadId.toString()] = parentAnnotation;

            this.threads.add(element.threadId);
        });
        // create annotation status array to handle each annotation
        this.annotationStatusFormGroup = new AnnotationStatusArrayEditorModel().fromModel(this.comments, this.listingStatuses).buildForm();
        this.loadingResults = false;
	}

	getParentAnnotation(thread: Guid): Annotation {
		return this.parentAnnotationsPerThread[thread.toString()];
	}

	resetFormGroup() {
        this.threadFormGroup = new UntypedFormGroup({
            text: new FormControl(null, [Validators.required]),
            protectionType: new FormControl(AnnotationProtectionType.EntityAccessors, [Validators.required])
        });
	}

	getSelectedStatusIcon(threadId: Guid): string {
		const selectedStatusId = this.getAnnotationStatusControl(this.annotationStatusFormGroup.get('annotationsStatusArray') as UntypedFormArray, threadId)?.get('statusId')?.value;
		if (!selectedStatusId) return null;

		const selectedStatus = this.listingStatuses.find(s => s.id == selectedStatusId);
		if (!selectedStatus) return null;

		return this.getStatusIcon(selectedStatus);
	}

	getStatusIcon(status: Status): string {
		let icon = this.configurationService.defaultStatusIcon;

		if (status) {
			if (status.internalStatus != null) {
				const statusConfiguration = this.configurationService.statusIcons?.find(s => s.InternalStatus == status.internalStatus);
				if (statusConfiguration?.icon != null && statusConfiguration?.icon != '') icon = statusConfiguration?.icon;
			} else if (status.id) {
				const statusConfiguration = this.configurationService.statusIcons?.find(s => s.id == status.id.toString());
				if (statusConfiguration?.icon != null && statusConfiguration?.icon != '') icon = statusConfiguration?.icon;
			}
		}

		return icon
	}

	public isFormValid(value: any) {
		return value.valid;
	}

	private onCallbackSuccess() {
		// this.uiNotificationService.snackBarNotification(this.language.instant('ANNOTATION-DIALOG.SUCCESS'), SnackBarNotificationLevel.Success);
		this.refreshAnnotations();
	}

	private onCallbackError(error: any) {
		this.uiNotificationService.snackBarNotification(this.language.instant(error.message), SnackBarNotificationLevel.Error);
	}


	getAnnotationProtectionType(thread: Guid): string {
		return this.enumUtils.toAnnotationProtectionTypeString(this.parentAnnotationsPerThread[thread.toString()].protectionType);
	}

    toggleReplies(threadId: string) {
        this.showRepliesPerThread[threadId] = !this.showRepliesPerThread[threadId];
    }

	showReplies(threadId: string): void {
		this.showRepliesPerThread[threadId] = true;
	}

	enableReply(threadId: string): void {
		this.replyEnabledPerThread[threadId] = true;
	}

	copyLink() {
		const el = document.createElement('textarea');
		let domain = `${window.location.protocol}//${window.location.hostname}`;
		if (window.location.port && window.location.port != '') domain += `:${window.location.port}`
		const path = this.generateLink()?.(this.anchor(), this.entityId());
		// let currentPath = window.location.pathname;
        // const rgx = /\/f\/|\/d\/|\/annotation/;
        // currentPath = currentPath.split(rgx)?.[0]; //get base of route, remove any descriptionid, fieldId, or annotation it might contain

        // let sectionPath: string;
        // if(this.entityType === AnnotationEntityType.Description){
        //     sectionPath = this.routerUtils.generateUrl([currentPath, 'd', this.entityId, 'f', this.anchor, 'annotation'].join('/'));
        // } else {
        //     sectionPath = this.routerUtils.generateUrl([currentPath, 'f', this.anchor, 'annotation'].join('/'));
        // }
		el.value = domain + path;
		el.setAttribute('readonly', '');
		el.style.position = 'absolute';
		el.style.left = '-9999px';
		document.body.appendChild(el);
		el.select();
		document.execCommand('copy');
		document.body.removeChild(el);
		this.uiNotificationService.snackBarNotification(
			this.language.instant('DESCRIPTION-EDITOR.QUESTION.EXTENDED-DESCRIPTION.COPY-LINK-SUCCESSFUL'),
			SnackBarNotificationLevel.Success
		);
	}

	// status

	private getStatuses(){
		const lookup: StatusLookup = new StatusLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { size: 100, offset: 0 };
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<Status>(x => x.id),
				nameof<Status>(x => x.label),
				nameof<Status>(x => x.internalStatus)
			]
		};

		lookup.order = { items: [nameof<Status>(x => x.label)] };

		return this.statusService.query(lookup).pipe(takeUntil(this._destroyed))
				.subscribe(result => {
					if (result && result.items?.length > 0) this.listingStatuses = result.items;
				});
	}

	private getAnnotationStatusControl(formArray: UntypedFormArray, annotationId: Guid){
		const index = formArray.controls.findIndex(x => x.get('annotationId')?.value == annotationId);
		if (index < 0) return null;
		return formArray.at(index);
	}
	// getAnnotationStatusFormControl(formArray: UntypedFormArray, annotationId: Guid) {
	// 	const control = this.getAnnotationStatusControl(formArray, annotationId);
	// 	if (control == null) return;
	// 	return control;
	// }

	private persistAnnotationStatus(formArray: UntypedFormArray, annotationId: Guid){
		const control = this.getAnnotationStatusControl(formArray, annotationId);
		if (control && control.valid){
			const formData = this.formService.getValue(control.value) as AnnotationStatusPersist;
			this.enqueueService.enqueueExhaustChannel(this.annotationService.persistStatus(formData))
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => this.onCallbackAnnotationStatusSuccess(),
				error => this.onCallbackError(error)
			);
		}
	}

	setAnnotationStatus(formArray: UntypedFormArray, annotationId: Guid, selectedStatusId: Guid) {
		const annotationForm = this.getAnnotationStatusControl(formArray, annotationId);
        if(!annotationForm){ return; }
		annotationForm.get('statusId').setValue(selectedStatusId);
        this.persistAnnotationStatus(formArray, annotationId)
	}

	canSaveAnnotationStatus(formArray: UntypedFormArray, annotationId: Guid): boolean{
		const control = this.getAnnotationStatusControl(formArray, annotationId);
		if (control == null) return false;
		return control.valid;
	}

	parsePayload(payload: string, users: PlanUser[]): AnnotationPayloadItem[] {
		if (!users?.length) return [{ isMention: false, payload: payload}];

		let payloadItems: AnnotationPayloadItem[] = [];

		const mentionRegExp = new RegExp(/\@\{\{userid:[a-zA-Z0-9\-]*\}\}/g);
		payloadItems = payload.split(/(?=\@\{\{userid:[a-zA-Z0-9\-]*\}\})|(?<=\@\{\{userid:[a-zA-Z0-9\-]*\}\})/g)
			.filter( p => p!=null && p!='')
			.map((p)=> {
				const annotationItem: AnnotationPayloadItem = { isMention: false, payload: p};

				if (mentionRegExp.exec(p)) {
					annotationItem.isMention = true;
					annotationItem.payload = users.find(u => p.includes(u.id.toString()))?.user?.name;
				}

				annotationItem.payload = this.sanitizer.sanitize(SecurityContext.HTML, annotationItem.payload);

				return annotationItem;
			});

		return payloadItems;
	}

	private onCallbackAnnotationStatusSuccess() {
		this.uiNotificationService.snackBarNotification(this.language.instant('ANNOTATION-DIALOG.ANNOTATION-STATUS.SUCCESS'), SnackBarNotificationLevel.Success);
		this.refreshAnnotations();
	}

    ngOnDestroy(): void {
        this.clearCommentSubject?.unsubscribe();
        this.clearCommentSubject = null;
    }

    protected annotationStatuses(threadId: Guid) {
        const selectedStatusId = this.getAnnotationStatusControl(this.annotationStatusFormGroup.get('annotationsStatusArray') as UntypedFormArray, threadId)?.get('statusId')?.value;
		if (!selectedStatusId) return this.listingStatuses;
		return this.listingStatuses.filter(x => x.id !== selectedStatusId);
    }

    protected annotationStatus(threadId): Status {
        const selectedStatusId = this.getAnnotationStatusControl(this.annotationStatusFormGroup.get('annotationsStatusArray') as UntypedFormArray, threadId)?.get('statusId')?.value;
		if (!selectedStatusId) return null;
		return this.listingStatuses.find(x => x.id === selectedStatusId);
    }

    protected planUserRoles(userId: Guid): PlanUserRole[] {
        if(!this.planUserMap()?.size){ return [];}
        return this.planUserMap().get(userId)?.filter(x => x.isActive === IsActive.Active).map(x => x.role) ?? [];
    }
}
