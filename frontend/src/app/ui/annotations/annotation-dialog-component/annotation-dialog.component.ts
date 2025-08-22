import { Component, computed, HostBinding, Inject, SecurityContext, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, UntypedFormArray, UntypedFormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AnnotationStatus, AnnotationStatusPersist } from '@annotation-service/core/model/annotation-status.model';
import { Annotation, AnnotationPersist } from '@annotation-service/core/model/annotation.model';
import { Status } from '@annotation-service/core/model/status.model';
import { AnnotationLookup } from '@annotation-service/core/query/annotation.lookup';
import { StatusLookup } from '@annotation-service/core/query/status.lookup';
import { AnnotationService } from '@annotation-service/services/http/annotation.service';
import { AnnotationProtectionType } from '@app/core/common/enum/annotation-protection-type.enum';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { isNullOrUndefined } from '@app/utilities/enhancers/utils';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { AnnotationStatusArrayEditorModel } from './annotation-status-editor.model';
import { StatusService } from '@annotation-service/services/http/status.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { MatSelectionList } from '@angular/material/list';
import { PlanUser } from '@app/core/model/plan/plan';
import { DomSanitizer } from '@angular/platform-browser';
import { Subject } from 'rxjs';

interface AnnotationPayloadItem {
	isMention: boolean;
	payload: string;
}

@Component({
    selector: 'app-annotation-dialog',
    templateUrl: './annotation-dialog.component.html',
    styleUrls: ['./annotation-dialog.component.scss'],
    standalone: false
})
export class AnnotationDialogComponent extends BaseComponent {
   
	annotationProtectionTypeEnumValues = this.enumUtils.getEnumValues<AnnotationProtectionType>(AnnotationProtectionType);
	annotationProtectionTypeEnum = AnnotationProtectionType;

	private entityId: Guid;
	private anchor: string;
	private entityType: string;

	private changesMade: boolean = false;

    protected canAnnotate: boolean = false;
    protected generateLink: (anchor: string, entityId: Guid) => string;

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
	public planUsers: PlanUser[] = [];
	
	MENTION_CLASS: string = "highlight-user-mention" 

	@ViewChild('annotationStatus') annotationStatus: MatSelectionList;

	clearCommentSubject: Subject<any> = new Subject<any>();
	ROOT_COMMENT: string = 'root';

	constructor(
		public dialogRef: MatDialogRef<AnnotationDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
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
	) {
		super();
		this.entityId = data.entityId;
		this.anchor = data.anchor;
		this.entityType = data.entityType;
		this.planUsers = data.planUsers;
        this.canAnnotate = data.canAnnotate;
        this.generateLink = data.generateLink;
		dialogRef.backdropClick().pipe(takeUntil(this._destroyed)).subscribe(() => dialogRef.close(this.changesMade));
	}

	ngOnInit(): void {
		this.threadFormGroup = new UntypedFormGroup({
			text: new FormControl(null, [Validators.required]),
			protectionType: new FormControl(AnnotationProtectionType.EntityAccessors, [Validators.required])
		});
        if(!this.canAnnotate){
            this.threadFormGroup.disable();
        }
		if (this.entityId != null) {
			this.loadThreads();
		}
		
		this.planUsers = this.uniqueActivePlanUsers() || [];
		this.getStatuses();
	}

	private uniqueActivePlanUsers() {
		const seenUserIds = new Set<Guid>();

		return this.planUsers?.filter(x => {
				if (x.isActive === IsActive.Active && x.user?.id && !seenUserIds.has(x.user?.id)) {
					seenUserIds.add(x.user?.id);
					return true;
				}
				return false;
		});
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
			entityId: this.entityId,
			entityType: this.entityType,
			anchor: this.anchor
		};
		this.annotationService.persist(threadToCreate).pipe(takeUntil(this._destroyed))
			.subscribe(
				complete => {
					this.clearCommentSubject.next(this.ROOT_COMMENT);
					this.onCallbackSuccess();
				},
				error => this.onCallbackError(error)
			);
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
			entityId: this.entityId,
			entityType: this.entityType,
			anchor: this.anchor,
			parentId: this.parentAnnotationsPerThread[threadId.toString()].id
		};
		this.annotationService.persist(replyToCreate).pipe(takeUntil(this._destroyed))
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
		this.loadThreads();
	}

    protected loadingResults = false;
	private loadThreads() {
		const lookup: AnnotationLookup = new AnnotationLookup();
		lookup.entityIds = [this.entityId];
		lookup.anchors = [this.anchor];
		lookup.entityTypes = [this.entityType];
		lookup.project = {
			fields: [
				nameof<Annotation>(x => x.id),
				nameof<Annotation>(x => x.threadId),
				nameof<Annotation>(x => x.parent.id),
				nameof<Annotation>(x => x.timeStamp),
				nameof<Annotation>(x => x.author.name),
				nameof<Annotation>(x => x.payload),
				nameof<Annotation>(x => x.protectionType),
				[nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.id)].join('.'),
				[nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.annotation), nameof<Annotation>(x => x.id)].join('.'),
				[nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.status), nameof<Status>(x => x.id)].join('.'),
				[nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.createdAt)].join('.'),
				[nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.updatedAt)].join('.'),
				[nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.isActive)].join('.'),
				[nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.hash)].join('.'),
			]
		};
        this.loadingResults = true;
		this.annotationService.query(lookup)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				data => {
					this.annotationsCount = data.count;
					this.annotationsPerThread = {};
					this.parentAnnotationsPerThread = {};
					this.threads = new Set();
					this.threadReplyTextsFG = new Array<UntypedFormGroup>();
					this.resetFormGroup();
					this.comments = data.items.sort((a1, a2) => new Date(a2.timeStamp).getTime() - new Date(a1.timeStamp).getTime());
					this.comments.forEach(element => {
						this.threadReplyTextsFG[element.threadId.toString()] = this.formBuilder.group({ replyText: new FormControl(null, [Validators.required]) });
						this.annotationsPerThread[element.threadId.toString()] = data.items.filter(x => x.threadId === element.threadId && x.id !== element.id).sort((a1, a2) => new Date(a1.timeStamp).getTime() - new Date(a2.timeStamp).getTime());
						
						const parentAnnotation: any = data.items.filter(x => x.threadId === element.threadId && x.id === element.id)[0];
						const annotationItems = this.parsePayload(parentAnnotation.payload);

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
				},
				error => {this.onCallbackError(error); this.loadingResults = false;},
			);
	}

	getParentAnnotation(thread: Guid): Annotation {
		return this.parentAnnotationsPerThread[thread.toString()];
	}

	resetFormGroup() {
		this.threadFormGroup.reset();
		this.threadFormGroup.get('protectionType').setValue(AnnotationProtectionType.EntityAccessors);
	}

	isValidText(text: string): boolean {
		return !isNullOrUndefined(text) && text.length !== 0 && text !== '';
	}

	getSelectedStatusIcon(threadId: Guid): string {
		const selectedStatusId = this.getAnnotationStatusFormControl(this.annotationStatusFormGroup.get('annotationsStatusArray') as UntypedFormArray, threadId).get('statusId')?.value;
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
		this.uiNotificationService.snackBarNotification(this.language.instant('ANNOTATION-DIALOG.SUCCESS'), SnackBarNotificationLevel.Success);
		this.refreshAnnotations();
		this.changesMade = true;
	}

	private onCallbackError(error: any) {
		this.uiNotificationService.snackBarNotification(this.language.instant(error.message), SnackBarNotificationLevel.Error);
	}


	getAnnotationProtectionType(thread: Guid): string {
		return this.enumUtils.toAnnotationProtectionTypeString(this.parentAnnotationsPerThread[thread.toString()].protectionType);
	}
	
	cancel() {
		this.dialogRef.close(this.changesMade);
	}

	close() {
		this.dialogRef.close(this.changesMade);
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
		const path = this.generateLink(this.anchor, this.entityId);
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

	getAnnotationStatusFormControl(formArray: UntypedFormArray, annotationId: Guid) {
		const control = this.getAnnotationStatusControl(formArray, annotationId);
		if (control == null) return;
		return control;
	}

	persistAnnotationStatus(formArray: UntypedFormArray, annotationId: Guid){
		const control = this.getAnnotationStatusControl(formArray, annotationId);
		if (control && control.valid){
			const formData = this.formService.getValue(control.value) as AnnotationStatusPersist;
			this.annotationService.persistStatus(formData)
			.pipe(takeUntil(this._destroyed)).subscribe(
				complete => this.onCallbackAnnotationStatusSuccess(),
				error => this.onCallbackError(error)
			);
		}
	}

	setAnnotationStatus(formArray: UntypedFormArray, annotationId: Guid, selectedStatusId: Guid) {
		const annotationForm = this.getAnnotationStatusFormControl(formArray, annotationId);
		annotationForm.get('statusId').setValue(selectedStatusId);
	}
	
	canSaveAnnotationStatus(formArray: UntypedFormArray, annotationId: Guid): boolean{
		const control = this.getAnnotationStatusControl(formArray, annotationId);
		if (control == null) return false;
		return control.valid;
	}

	parsePayload(payload: string): AnnotationPayloadItem[] {

		if (!this.planUsers) return [{ isMention: false, payload: payload}];
    if (this.planUsers.length == 0) [{ isMention: false, payload: payload}];

		let payloadItems: AnnotationPayloadItem[] = [];

		const mentionRegExp = new RegExp(/\@\{\{userid:[a-zA-Z0-9\-]*\}\}/g);
		payloadItems = payload.split(/(?=\@\{\{userid:[a-zA-Z0-9\-]*\}\})|(?<=\@\{\{userid:[a-zA-Z0-9\-]*\}\})/g)
			.filter( p => p!=null && p!='')
			.map((p)=> {
				let annotationItem: AnnotationPayloadItem = { isMention: false, payload: p};
				
				if (mentionRegExp.exec(p)) {
					annotationItem.isMention = true;
					annotationItem.payload = this.planUsers.find(u => p.includes(u.id.toString()))?.user?.name;
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
}
