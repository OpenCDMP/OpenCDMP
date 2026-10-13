import { Component, computed, effect, input, output, Signal, untracked, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { PlanAnnotationService } from '../plan-annotation.service';
import { BaseComponent } from '@common/base/base.component';
import { Guid } from '@common/types/guid';
import { Plan, PlanUser } from '@app/core/model/plan/plan';
import { Annotation } from '@annotation-service/core/model/annotation.model';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { PlanTempStorageService } from '../plan-temp-storage.service';
import { ToCEntry } from '@app/ui/description/editor/table-of-contents/models/toc-entry';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { TableOfContentsHelper } from '@app/ui/description/editor/table-of-contents/helpers/table-of-contents.helper';
import { RootToCEntry } from '../plan-table-of-contents/plan-table-of-contents.component';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { FormsModule } from '@angular/forms';
import { AnnotationDialogComponent } from '../../../annotations/annotation-dialog-component/annotation-dialog.component';
import { NgTemplateOutlet, LowerCasePipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { InvokeMemoizedPipe } from '../../../../../common/formatting/pipes/invoke-memoized.pipe';

@Component({
    selector: 'app-plan-annotations-side-dialog',
    templateUrl: './plan-annotations-side-dialog.component.html',
    styleUrl: './plan-annotations-side-dialog.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, MatIconButton, MatButtonToggleGroup, FormsModule, MatButtonToggle, AnnotationDialogComponent, NgTemplateOutlet, LowerCasePipe, TranslatePipe, InvokeMemoizedPipe]
})
export class PlanAnnotationsSideDialogComponent extends BaseComponent implements OnInit{
    tocEntries = input<RootToCEntry[]>([]);
    plan = input<Plan>();
    planUsers = computed(() => this.plan()?.planUsers ?? []);
    planGroupedUsers: Signal<Map<Guid, PlanUser[]>> = computed(() => {
        const map = new Map<Guid, PlanUser[]>([]);
        if(!this.planUsers()?.length){ return map;}
        // before the role de-duplication below, or an inactive row can hide the active one with the same role
        this.planUsers().filter(x => x.isActive === IsActive.Active).forEach((planUser) => {
            if(!map.has(planUser.user.id)){
                map.set(planUser.user.id, [planUser])
            } else {
                const entries = map.get(planUser.user.id);
                if(entries.some(x => x.role == planUser.role)){
                    return;
                }
                if(planUser.role == PlanUserRole.Owner){
                    map.set(planUser.user.id, [planUser, ...entries]);
                }else{
                    map.set(planUser.user.id, [...entries, planUser]);
                }
            }
        })
        return map;
    })
    annotationCount = input<number>();
    canAnnotatePerFieldSet = input<Map<string, boolean>>(new Map([]));
    canAnnotatePerDescription = input<Map<string, boolean>>(new Map([]));
    onEditorClose = output<{changes?: boolean}>();
    changeStep = output<{descriptionId?: Guid, fieldId: Guid}>();
    
    selectedArea: string  = null;

    context = computed(() => {
        if(!this.planAnnotationService.context()){return null;}
        return {
            ...this.planAnnotationService.context(),
            label: this.getAnchorName(this.planAnnotationService.context().anchor, this.planAnnotationService.context().entityId, this.planAnnotationService.context().entityType)
        }
    })

    contextAnnotations = computed(() => {
        if(!this.context() || !this.annotationsByAnchor()?.length){ return []; }
        return this.annotationsByAnchor().find(x => x.anchor == this.context().anchor)?.annotations ?? [];
    })
    
    annotationsByAnchor: Signal<{anchor: string, label?:string, entityId: Guid, entityType: AnnotationEntityType, annotations: Annotation[]}[]> = computed(() => {
        if(!this.planAnnotationService.planAnnotations()?.length){ return []}
        const map = new Map<string, Annotation[]>([]);
        const res = [];
        this.planAnnotationService.annotationCountAnchorMap()?.forEach((val, key) => {
            map.set(key, [])
        });
        this.planAnnotationService.planAnnotations().forEach((annotation) => {
            map.set(annotation.anchor, [...map.get(annotation.anchor), annotation])
        })
        map.forEach((val, key) => {
            res.push({
                anchor: key,
                label: this.getAnchorName(key, val?.[0]?.entityId, val?.[0]?.entityType),
                entityType: val?.[0]?.entityType,
                entityId: val?.[0]?.entityId,
                annotations: val
            })
        })
        return res;
    })

    getAnchorName(anchor: string, entityId: Guid, entityType: AnnotationEntityType): string{
        if(entityType == AnnotationEntityType.Plan){
            const entry = TableOfContentsHelper.findTocEntryById(anchor, untracked(this.tocEntries));
            return ((entry?.numbering ?? '') + ' ' + (entry?.label ?? '')).trim();
        //     let label = '';
        //     this.plan()?.blueprint?.definition?.sections?.find((section) => {
        //         //TODO akis refactor with new structure
        //         // let fieldFound = section.fields?.find(x => x.id.toString() == anchor);
        //         // if(fieldFound){
        //         //     label = fieldFound.label;
        //         //     return true;
        //         // }
        //         // return false;
        //     })
        //     return label;
        } else {
            const parentEntry: ToCEntry = TableOfContentsHelper.findTocEntryById(entityId.toString(), untracked(this.tocEntries));
            const entries: ToCEntry[] = parentEntry?.subEntries ?? [];
            const entry = TableOfContentsHelper.findTocEntryById(anchor, entries);
            return ((parentEntry?.numbering ? parentEntry?.numbering + '.' : '' ) + (entry?.numbering ?? '') + ' ' + (entry?.label ?? '')).trim();
        }
    }


    constructor(
        private planAnnotationService: PlanAnnotationService,
        private planTempStorage: PlanTempStorageService

    ){
        super();
        effect(() => {
            if(this.planAnnotationService.context()){
                this.selectedArea = this.planAnnotationService.context().anchor;
            } else {
                this.selectedArea = null;
            }
        })
    }

    refreshAnnotations(){
        this.planAnnotationService.refreshAnnotations();
    }
    
    ngOnInit(): void {
	}

    canAnnotate: (anchor: string, entityId: Guid, entityType: AnnotationEntityType) => boolean = (anchor, entityId, entityType) => {
        if(!entityType){ return false;}
        if(entityType == AnnotationEntityType.Plan){
            return this.canAnnotatePerFieldSet()?.get(anchor);
        } else {
            return this.canAnnotatePerDescription()?.get(entityId.toString());
        }
    }

    navigateToItem(item: {anchor: string, label?:string, entityId: Guid, entityType: AnnotationEntityType, annotations: Annotation[]}){
        if(item.entityType == AnnotationEntityType.Description){
            this.changeStep.emit({descriptionId: item.entityId, fieldId: Guid.parse(item.anchor)})
        }else {
            this.changeStep.emit({fieldId: Guid.parse(item.anchor)})
        }
    }
}
