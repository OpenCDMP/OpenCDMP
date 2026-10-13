import { Injectable, signal } from "@angular/core";
import { AnnotationService } from "@annotation-service/services/http/annotation.service";
import { SnackBarNotificationLevel, UiNotificationService } from "@app/core/services/notification/ui-notification-service";
import { BaseService } from "@common/base/base.service";
import { Guid } from "@common/types/guid";
import { TranslateService } from "@ngx-translate/core";
import { PlanTempStorageService } from "./plan-temp-storage.service";
import { Plan } from "@app/core/model/plan/plan";
import { AnnotationLookup } from "@annotation-service/core/query/annotation.lookup";
import { takeUntil } from "rxjs";
import { nameof } from "ts-simple-nameof";
import { AnnotationEntityType } from "@app/core/common/enum/annotation-entity-type";
import { Annotation } from "@annotation-service/core/model/annotation.model";
import { IsActive } from "@app/core/common/enum/is-active.enum";
import { AnnotationStatus } from "@annotation-service/core/model/annotation-status.model";
import { Status } from "@annotation-service/core/model/status.model";
import { AnnotationPermission } from "@annotation-service/core/enum/permission.enum";

@Injectable({
    providedIn: 'any',
})
export class PlanAnnotationService extends BaseService {
    plan: Plan;
    planTempStorage: PlanTempStorageService;
    
    private _planAnnotations = signal<Annotation[]>([]);
    private _descriptionsAnnotationMap = signal<Map<string, Annotation[]>>(new Map([]));
    private _annotationsAnchorMap = signal<Map<string, number>>(new Map([]));

    public planAnnotations = this._planAnnotations.asReadonly();
    public descriptionsAnnotationMap = this._descriptionsAnnotationMap.asReadonly();
    public annotationCountAnchorMap = this._annotationsAnchorMap.asReadonly();

    private _context = signal<AnnotationAreaContext>(null);
    public context = this._context.asReadonly();
    public setContext(val: AnnotationAreaContext){
        this._context.set(val);
        if(val && !this._annotationDialogOpen()){
            this.toggleAnnotationDialog();
        }
    }

    private _annotationDialogOpen = signal<boolean>(false);
    public annotationDialogOpen = this._annotationDialogOpen.asReadonly();
    public toggleAnnotationDialog(){
        this._annotationDialogOpen.update((x) => !x);
    }
    public closeAnnotationDialog(){
        this._annotationDialogOpen.set(false);
    }

    constructor(
		private annotationService: AnnotationService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService
	) {
		super();
	}

    init(plan: Plan, planTempStorage: PlanTempStorageService){
        if(!plan){ return; }
        this.plan = plan;
        this.planTempStorage = planTempStorage;
        this.refreshAnnotations();
    }

    public refreshAnnotations(){
        const annotationsAnchorMap = new Map<string, number>([]);
        const descriptionsAnnotationMap = new Map<string, Annotation[]>([]);
        const lookup: AnnotationLookup = new AnnotationLookup();
        const descriptionIds = [];
        this.planTempStorage?.descriptions?.()?.forEach((desc) => descriptionIds.push(desc.lastPersist.id));
        lookup.entityIds = [this.plan.id, ...descriptionIds];
        lookup.entityTypes = [AnnotationEntityType.Plan, (descriptionIds?.length ? AnnotationEntityType.Description : null)].filter(x => !!x);
        lookup.isActive = [IsActive.Active];
        lookup.project = {
            fields: this.annotationLookupFields
        };
        lookup.order = { items: ['-' + (nameof<Annotation>(x => x.updatedAt))] }; 

        this.annotationService.query(lookup)
        .pipe(takeUntil(this._destroyed))
        .subscribe({
            next: (data) => {
                for (const item of data.items) {
                    annotationsAnchorMap.set(item.anchor, (annotationsAnchorMap.get(item.anchor) ?? 0) + 1);
                    if(item.entityType == AnnotationEntityType.Description){
                        const key = item.entityId?.toString();
                        descriptionsAnnotationMap.set(key, [...(descriptionsAnnotationMap.get(key) ?? []), item])
                    }
                }
                //trigger update
                this._annotationsAnchorMap.set(annotationsAnchorMap);
                this._planAnnotations.set(data.items);
                this._descriptionsAnnotationMap.set(descriptionsAnnotationMap);
            },
            error: (error) => this.onCallbackError(error)
        });
    }
    private onCallbackError(error: any) {
        this.uiNotificationService.snackBarNotification(this.language.instant(error.message), SnackBarNotificationLevel.Error);
    }

    private get annotationLookupFields(): string[] {
        return [
            nameof<Annotation>(x => x.id),
            nameof<Annotation>(x => x.entityId),
            nameof<Annotation>(x => x.entityType),
            nameof<Annotation>(x => x.anchor),
            nameof<Annotation>(x => x.updatedAt),
            nameof<Annotation>(x => x.threadId),
            nameof<Annotation>(x => x.parent.id),
            nameof<Annotation>(x => x.timeStamp),
            nameof<Annotation>(x => x.author.id),
            nameof<Annotation>(x => x.author.name),
            nameof<Annotation>(x => x.payload),
            nameof<Annotation>(x => x.protectionType),
            nameof<Annotation>(x => x.hash),
            [nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.id)].join('.'),
            [nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.annotation), nameof<Annotation>(x => x.id)].join('.'),
            [nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.status), nameof<Status>(x => x.id)].join('.'),
            [nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.createdAt)].join('.'),
            [nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.updatedAt)].join('.'),
            [nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.isActive)].join('.'),
            [nameof<Annotation>(x => x.annotationStatuses), nameof<AnnotationStatus>(x => x.hash)].join('.'),
            [nameof<Annotation>(x => x.authorizationFlags), AnnotationPermission.EditAnnotation].join('.'),
			[nameof<Annotation>(x => x.authorizationFlags), AnnotationPermission.DeleteAnnotation].join('.')
        ]
    }
}
export interface AnnotationAreaContext {anchor: string, entityId: Guid, entityType: AnnotationEntityType}