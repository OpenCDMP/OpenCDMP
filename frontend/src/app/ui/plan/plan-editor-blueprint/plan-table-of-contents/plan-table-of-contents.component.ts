import {
  Component,
  computed,
  effect,
  input,
  model,
  output,
  QueryList,
  Signal,
  untracked,
  ViewChild,
  ViewChildren,
  ChangeDetectionStrategy,
  forwardRef
} from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { DescriptionTemplatesInSection, PlanBlueprint, RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan } from '@app/core/model/plan/plan';
import { BaseComponent } from '@common/base/base.component';
import { PlanEditorForm } from '../plan-editor.model';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { debounceTime, distinctUntilChanged, filter, mergeMap, Observable, skip, takeUntil, tap } from 'rxjs';
import { toObservable } from '@angular/core/rxjs-interop';
import { ToCEntry } from '@app/ui/description/editor/table-of-contents/models/toc-entry';
import { DescriptionInfo, PlanTempStorageService } from '../plan-temp-storage.service';
import { TableOfContentsComponent } from '@app/ui/description/editor/table-of-contents/table-of-contents.component';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { PlanWebSocketService } from '@app/core/services/websocket/plan-websocket.service';
import { UserActionPayload } from '@app/core/model/websocket/ws-message.model';
import { ToCEntryType } from '@app/ui/description/editor/table-of-contents/models/toc-entry-type.enum';
import { MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelContent } from '@angular/material/expansion';
import { PlanAnnotationService } from '../plan-annotation.service';
import { TableOfContentsHelper } from '@app/ui/description/editor/table-of-contents/helpers/table-of-contents.helper';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { EntityType } from '@app/core/common/enum/entity-type';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { AuthService } from '@app/core/services/auth/auth.service';
import { PlanLockService } from '../plan-lock.service';
import { EditorSessionStateService, statusContrastColor } from '../editor-session-state.service';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { MatButton } from '@angular/material/button';
import { NgTemplateOutlet, NgClass } from '@angular/common';
import { TableOfContentsInternal } from '../../../description/editor/table-of-contents/table-of-contents-internal/table-of-contents-internal';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { TableOfContentsComponent as TableOfContentsComponent_1 } from '../../../description/editor/table-of-contents/table-of-contents.component';
import { PlanFormProgressIndicationComponent } from '../form-progress-indication/plan-form-progress-indication.component';

@Component({
    selector: 'app-plan-table-of-contents',
    templateUrl: './plan-table-of-contents.component.html',
    styleUrl: './plan-table-of-contents.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButton, MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, NgTemplateOutlet, MatExpansionPanelContent, forwardRef(() => TableOfContentsInternal), MatTooltip, MatIcon, forwardRef(() => TableOfContentsComponent_1), NgClass, PlanFormProgressIndicationComponent, FormsModule, ReactiveFormsModule, TranslatePipe]
})
export class PlanTableOfContentsComponent extends BaseComponent{
    @ViewChild('sectionAccordion') sectionAccordion: MatAccordion;
    @ViewChildren('descriptionAccordion') descriptionAccordions: QueryList<MatAccordion>;
    @ViewChildren('descriptionToC') descriptionToCList: QueryList<TableOfContentsComponent>;
    @ViewChildren('expansionPanel') expansionPanels: QueryList<MatExpansionPanel>;

    //** INPUTS */
    protected plan = input<Plan>();
    protected selectedBlueprint = input<PlanBlueprint>();
    protected formGroup = input<FormGroup<PlanEditorForm>>();
    protected canEditPlan = input<boolean>();
    protected isNew = input<boolean>();
    protected isLocked = input<boolean>();
    protected permissionPerSection = input<Map<Guid, string[]>>();
    protected showDescriptionErrors = input<Set<string>>(new Set([]));
    protected showPlanErrors = input<boolean>(false);
    protected visibilityRulesService = input<VisibilityRulesService>();
    protected tocEntries = model<RootToCEntry[]>([]);

    //DESCRIPTION
    protected selectedDescription = model<Guid>(null);
    protected descriptionInfo = computed(() => this.selectedDescription()?  this.planTempStorage.descriptions().get(this.selectedDescription().toString()) : null)
    protected descriptionFormGroup = computed(() => this.descriptionInfo()?.formGroup);
    protected selectedDescriptionStatic = computed(() => this.descriptionInfo()?.lastPersist);

    //** OUTPUTS */

    protected onRemoveDescription = output<DescriptionInfo>();
    protected onAddDescription = output<RootSection>();
    protected onNavigate = output<ToCEntry>();

    
    //ToC step selection 
    // tocEntries: RootToCEntry[] = [];
    selected: SelectedEntry = null;

	protected descriptionStatusEnum = DescriptionStatusEnum;
    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;

    //annotations
	annotationsPerAnchor =  computed(() => this.annotationService.annotationCountAnchorMap());
	annotationsPerDescription = computed(() => this.annotationService.descriptionsAnnotationMap());

    descriptionAnnotationsPerSection = computed(() => {
        const map = new Map<string, number>();
        if(!this.annotationsPerDescription()?.size){ return map }
        this.selectedBlueprint()?.definition?.sections?.forEach((section) => {
            let count = 0;
            this.descriptionsInSection()?.get(section.id)?.forEach((x) => {
                count += this.annotationsPerDescription().get(x.lastPersist.id?.toString())?.length
            });
            map.set(section.id, count)
        })
        return map;
    })
	showAnnotations = output<Guid>();
    constructor(
        private planTempStorage: PlanTempStorageService,
        protected enumUtils: EnumUtils,
        private planWebSocketService: PlanWebSocketService,
		private annotationService: PlanAnnotationService,
        private storageFileService: StorageFileService,
        private sanitizer: DomSanitizer,
        private authService: AuthService,
        private planLockService: PlanLockService,
        private editorSessionState: EditorSessionStateService,
        private language: TranslateService,
    ) {
        super();
        effect(() => {
            if(this.selectedBlueprint()){
                this.reloadTocEntries();
                if(!this.selected || !TableOfContentsHelper.findTocEntryById(this.selected.entry.id, untracked(this.tocEntries))){
                    this.selected = {
                        entityType: EntityType.Plan,
                        entry: TableOfContentsHelper.getHeadField(untracked(this.tocEntries)?.[0])
                    };
                    setTimeout(() => {
                        this._resetObserver();
                        this.pauseIntersectionObserver = false;
                    });
                }
            }
        })
        // Publish the authoritative description numbering (computed here) so the description editor
        // reads the exact same value instead of recomputing it with a divergent formula.
        effect(() => {
            const entries = this.tocEntries();
            const map = new Map<string, string>();
            const collect = (list: ToCEntry[]) => {
                (list ?? []).forEach((e) => {
                    if(e?.type === ToCEntryType.Description && e?.id && e?.numbering){
                        map.set(e.id.toString(), e.numbering);
                    }
                    collect((e as any)?.descriptions);
                    collect(e?.subEntries);
                });
            };
            collect(entries);
            this.planTempStorage.setDescriptionNumbering(map);
        })
        effect(() => {
            const storageFiles = this.plan()?.planDescriptionTemplates?.filter(x =>
                !!x.currentDescriptionTemplate?.definition?.iconConfiguration?.storageFile
            );
            if(storageFiles?.length){
                this.downloadStorageFiles(storageFiles.map(x => x.id));
            }
        })
        // Re-acquire the lock for the current selection on subsequent planLockService.initialize()
        // bumps (e.g., save → refreshData → reset → initialize). skip(1) drops the initial bump —
        // the first lock is acquired via the navigation flow in plan-editor. filter(v > 0) ignores
        // the version=0 emission that reset() produces before the next bump. The synchronous
        // tocEntries() guard handles the rare case where version bumps before the ToC has built.
        toObservable(this.planLockService.initializeVersion).pipe(
            filter(v => v > 0),
            skip(1),
            takeUntil(this._destroyed),
        ).subscribe(() => {
            if(this.tocEntries()?.length){
                this.lockSelected();
            }
        });
    }


    maxSteps = computed(() => this.plan()?.blueprint?.definition?.sections?.length ?? 0);

    // initializeSectionInfo() {
    //     if(!this.selectedBlueprint()?.definition) { return;}
    //     this.sectionToFieldsMap = new Map([]);
    //     this.selectedBlueprint().definition.sections.forEach((section: PlanBlueprintDefinitionSection) => {
	// 		let value: PlanFieldIndicator = new PlanFieldIndicator(section);
	// 		this.sectionToFieldsMap.set(section.id, value);
	// 	});
    // }

    protected descriptionsInSection: Signal<Map<string, DescriptionInfo[]>> = computed(() => {
        const descriptionsMap = new Map<string,DescriptionInfo[]>([]);
        this.planTempStorage.descriptionIdsBySectionMap().forEach((value, key) => descriptionsMap.set(key, value?.map((id) => this.planTempStorage.descriptions().get(id.toString()))));
        return descriptionsMap;
    }); 

    descTemplatesTouched(entry: RootToCEntry): boolean {
        return !!this.formGroup()?.controls?.descriptionTemplates?.get(entry.id.toString())?.dirty;
    }

	hasValidMultiplicity(entry: RootToCEntry): boolean {
        if (!entry.hasTemplates){
            return false;
        }
        if(!entry.descriptionTemplates?.length){
            return true;
        }
        const descriptions = this.descriptionsInSection().get(entry.id) ?? [];
        const distinctTemplates = new Set(descriptions.map(x => x.formGroup?.value?.planDescriptionTemplateId));

        if (this.plan()?.planDescriptionTemplates?.filter(x => x.sectionId == entry.id).length > distinctTemplates.size) {
            return true;
        }

        const descriptionPlanDescriptionTemplates = descriptions.map((x) => this.planTempStorage.getPlanDescriptionTemplate(x.formGroup?.value?.planDescriptionTemplateId)).filter((x) => x);
        const descriptionTemplateGroupIds = descriptionPlanDescriptionTemplates.map((x) => x?.descriptionTemplateGroupId);
        return entry.descriptionTemplates.some(sectionDescriptionTemplate => {
            if (sectionDescriptionTemplate.maxMultiplicity != null) {
                const groupId = sectionDescriptionTemplate.descriptionTemplate?.groupId;
                const count = descriptionTemplateGroupIds.filter(x => x === groupId).length;
                return count < sectionDescriptionTemplate.maxMultiplicity;
            } else {
                return true;
            }
        })
	}

    descriptionOrdinal(entry: RootToCEntry, baseOrdinal: number): string{
        return `${entry.numbering}.${entry.subEntries?.length ? (entry.subEntries.length + 1 + '.') : ''}${baseOrdinal}`;
    }

    hasDescriptionTemplates(entry: RootToCEntry): boolean {
        return this.formGroup()?.controls?.descriptionTemplates?.get(entry.id)?.value?.length > 0;
	}

	showSectionErrors(entry: ToCEntry): boolean {
        if(!this.showPlanErrors()){ return; }
        return !TableOfContentsHelper.isTocEntryValid(entry);
		// const formControlBySection = this.sectionToFieldsMap?.get(sectionId);
		// if (formControlBySection == null || this.formGroup() == null || ((this.formGroup()?.disabled??false)==true || (!this.isNew() && !this.canEdit()))) return false;

		// for (let control of formControlBySection.fieldControls) {
		// 	if (this.formControlValid(control.formControlName) === false) {
		// 		return true;
		// 	}
        //     if (this.hasPlanOwnerRequired(control.formControlName)) {
        //         return true;
        //     }
		// }

		// return false;
	}
    // showFieldError(sectionId: string, fieldId: string): boolean{
    //     if((!this.isNew() && !this.canEdit()) || !this.formGroup() || ((this.formGroup()?.disabled??false) == true)){
    //         return false;
    //     }

    //     const formControlBySection = this.sectionToFieldsMap?.get(sectionId);
	// 	if (formControlBySection == null || formControlBySection == undefined){
    //         return false;
    //     }

    //     const control = formControlBySection.fieldControls.find((x) => x.id === fieldId);
    //     return control && control && (this.formControlValid(control.formControlName) === false || this.hasPlanOwnerRequired(control.formControlName) === true)
    // }

    hasPlanOwnerRequired(controlName: string) {
        if (controlName === 'users' && this.formGroup()?.get(controlName).hasError('planOwnerRequired')) {
            return true;
        }

        return false;
    }

    invalidDescription(descriptionMetadata: DescriptionInfo){
        return descriptionMetadata.lastPersist.status?.internalStatus != DescriptionStatusEnum.Finalized && (!descriptionMetadata.formGroup.disabled && !descriptionMetadata.formGroup.valid);
    }

    // FE-2 (TOC status dashboard): per-description facts read from the unified EditorSessionState
    // selector, so the navigator shows status / unsaved / lock at a glance.
    protected descriptionStatusInfo(id: Guid | string) {
        return this.editorSessionState.descriptionState(id)?.status;
    }
    protected isDescriptionDirty(id: Guid | string): boolean {
        return !!this.editorSessionState.descriptionState(id)?.dirty;
    }
    protected descriptionLockState(id: Guid | string): string {
        return this.editorSessionState.lockInfoFor(id)?.state;
    }
    // FE-2: readable text color for a status chip given its admin-configured background color.
    protected statusContrastColor = statusContrastColor;

    // FE-2: a section is "unsaved" if its own plan fields changed OR any description it contains is dirty.
    // Rolls the per-entity dirty state up to the section header in the TOC.
    protected isSectionDirty(entry: RootToCEntry): boolean {
        if(!entry?.id){ return false; }
        if(this.editorSessionState.planSectionDirty(entry.id)){ return true; }
        return (entry.descriptions ?? []).some(d => this.editorSessionState.descriptionState(d.id)?.dirty);
    }

    // FE-2: a section is "incomplete" (has pending required / invalid fields) if its own plan fields
    // are invalid OR any description it contains is invalid. Visibility-aware (hidden required fields
    // don't count). Proactive guidance before finalization.
    protected isSectionIncomplete(entry: RootToCEntry): boolean {
        if(!entry?.id){ return false; }
        if(this.editorSessionState.planSectionIncomplete(entry.id)){ return true; }
        return (entry.descriptions ?? []).some(d => this.isDescriptionIncomplete(d.id));
    }
    protected isDescriptionIncomplete(id: Guid | string): boolean {
        const state = this.editorSessionState.descriptionState(id);
        return !!state && !state.valid;
    }

    /**
     * What still needs input in a section, rolled up so the badges add up top-down: every visible plan
     * field whose control is invalid counts 1 (regardless of touched state, this is proactive guidance),
     * and every incomplete description contributes its own pending-question count. Feeds the section
     * chip's number and tooltip so the "!" says *which* rows are pending.
     */
    protected pendingItems(entry: RootToCEntry): PendingSummary {
        const fields = this.pendingFieldSetLabels(entry?.subEntries, this.visibilityRulesService());
        const summary: PendingSummary = { count: fields.length, labels: [...fields] };
        (entry?.descriptions ?? []).forEach(d => {
            if (!this.isDescriptionIncomplete(d.id)) { return; }
            // Description entries carry no label of their own; use the live form title, then the persisted one.
            const info = this.planTempStorage.descriptions()?.get(d.id?.toString());
            const name = info?.formGroup?.get('label')?.value || info?.lastPersist?.label || d.label
                || this.language.instant('PLAN-EDITOR.CONTEXT.DESCRIPTION');
            // An invalid description with no pending question (e.g. a missing title) still counts as one item.
            const questions = Math.max(1, this.pendingDescriptionItems(d).count);
            summary.count += questions;
            summary.labels.push(`${name} (${questions})`);
        });
        return summary;
    }

    /** Same as {@link pendingItems} for a single description row: its visible questions whose controls are invalid. */
    protected pendingDescriptionItems(descEntry: ToCEntry): PendingSummary {
        const info = this.planTempStorage.descriptions()?.get(descEntry?.id?.toString());
        const labels = this.pendingFieldSetLabels(descEntry?.subEntries, info?.visibilityRulesService);
        return { count: labels.length, labels };
    }

    /** Tooltip / aria text for an incomplete chip: the total plus the first few names, so a long template stays readable. */
    protected pendingTooltip(pending: PendingSummary): string {
        if (!pending?.count) { return this.language.instant('PLAN-EDITOR.SECTION-INCOMPLETE'); }
        const shown = pending.labels.slice(0, PlanTableOfContentsComponent.PENDING_NAMES_SHOWN);
        const listed = shown.join(', ') + (pending.labels.length > shown.length ? ', …' : '');
        return this.language.instant('PLAN-EDITOR.SECTION-INCOMPLETE-LIST', { count: pending.count, names: listed });
    }
    private static readonly PENDING_NAMES_SHOWN = 5;

    private pendingFieldSetLabels(entries: ToCEntry[] | null | undefined, visibilityRules: VisibilityRulesService | null | undefined): string[] {
        const names: string[] = [];
        const isVisible = (e: ToCEntry) => visibilityRules?.isVisibleMap?.[e.visibilityRuleKey] !== false;
        const walk = (e: ToCEntry) => {
            if (!e || !isVisible(e)) { return; }
            if (e.type === ToCEntryType.FieldSet) {
                if (e.validityAbstractControls?.some(c => c.enabled && c.invalid)) { names.push(e.label); }
                return;
            }
            e.subEntries?.forEach(walk);
        };
        entries?.forEach(walk);
        return names;
    }

	formControlValid(controlName: string): boolean {
		if (!this.showPlanErrors() || !this.formGroup()?.get(controlName)){
            return true
        }
		return this.formGroup()?.get(controlName).valid;
	}


    onEntrySelected(entry: ToCEntry){
        const prevDesc = this.selectedDescription();
        this.selectedDescription.set(null);
        this.pauseIntersectionObserver = true;
        const prevEntry = this.selected?.entry;
        this.selected = {
            entityType: EntityType.Plan,
            entry
        }
        this.onNavigate.emit(this.selected.entry);
        if(prevEntry != entry){
            setTimeout(() => {
                this.scrollToSelected();
                this._resetObserver();
                this.pauseIntersectionObserver = false;
            });
        }else {
            this.pauseIntersectionObserver = false;
        }
        this.publishAction();
        if(prevDesc?.toString() != this.selectedDescription()?.toString()){
            this.lockSelected();
        }
    }
    onDescriptionSelected(entry: ToCEntry){
        const prevDesc = this.selectedDescription();
        this.selectedDescription.set(Guid.parse(entry.id));
        this.pauseIntersectionObserver = true;
        const headField = TableOfContentsHelper.getHeadField(entry);
        this.selected = {
            entityType: EntityType.Description,
            entry: headField
        };
        this.onNavigate.emit(this.selected.entry);
        this.publishAction();
        if(prevDesc?.toString() != this.selectedDescription()?.toString()){
            this.lockSelected();
        }
    }
    onDescriptionEntrySelected(entry: ToCEntry, descriptionEntryId: string, scroll: boolean = true){
        const prevDesc = this.selectedDescription();
        this.selectedDescription.set(Guid.parse(descriptionEntryId));
        this.pauseIntersectionObserver = true;
        this.selected = {
            entityType: EntityType.Description,
            entry: entry
        };
        this.onNavigate.emit(this.selected.entry);
        this.publishAction();
        if(scroll){
            setTimeout(() => this.scrollToSelected())
        }
        if(prevDesc?.toString() != this.selectedDescription()?.toString()){
            this.lockSelected();
        }
    }
	// changePlanStep(params: {
    //     section: number, 
    //     sectionId?: Guid;
    //     fieldId?: Guid,
    //     descriptionId?: Guid,
    //     descriptionFieldId?: string
    // }) {
    //     const {section: index, sectionId, fieldId, descriptionId, descriptionFieldId} = params;
    //     this.pauseIntersectionObserver = true;

    //     if(index != this.step() && index >= 1 && index <= this.selectedBlueprint().definition.sections?.length){
    //         this.step.set(index);
    //         // this.expandChildrenMap.set(index, true);
    //     }

    //     this.selectedDescription.set(descriptionId ?? null);
    //     if(descriptionId){
    //         const descriptionToC = this.descriptionToCList?.find((item) => item.descriptionId() === descriptionId);
    //         setTimeout(() => descriptionToC?.selectField(descriptionFieldId ?? null)); //wait for next cycle so table of contents component is initiated
    //     } else {
    //         this.pauseIntersectionObserver = false;
    //     }

    //     this.selectedFieldId = fieldId?.toString() ?? null;
    //     if(this.selectedFieldId){
    //         setTimeout(() => {this.scrollToSelectedField(fieldId)}); //wait for next cycle so plan section is rendered
    //     } else {
    //         this.resetScroll('editor-form');
    //     }
    //     this.publishAction({sectionId, fieldId, descriptionId});
	// }

    
    // public changeDescriptionStep(section: PlanBlueprintDefinitionSection, descInfo: DescriptionInfo, selected: ToCEntry = null, execute: boolean = true) {
    //     let descriptionId = descInfo.lastPersist.id;
    //     let sectionIndex = (this.selectedBlueprint()?.definition?.sections?.findIndex((x) => x.id === section.id) ?? 0) + 1;
    //     if(sectionIndex != this.step() || descriptionId !== this.selectedDescription()){
    //         this.pauseIntersectionObserver = true;
    //         if(sectionIndex != this.step()){
    //             this.step.set(sectionIndex);
    //         }
    //         if(descriptionId !== this.selectedDescription()){
    //             this.selectedDescription.set(descriptionId);
    //         }
    //     }
    //     this.selectedFieldId = selected?.id;
	// 	if (execute) {
	// 		if (selected) {
	// 			this.reachedBase = false;
	// 			this.reachedFirst = selected.isFirstEntry;
	// 			this.reachedLast = selected.isLastEntry;
	// 			const element = document.getElementById(selected.id);
	// 			if (element) {
	// 				setTimeout(() => {
    //                     element.scrollIntoView({ behavior: 'smooth' });
    //                     this.pauseIntersectionObserver = false;
    //                 }); //wait for next cycle so desc form component is rendered
	// 			}
	// 		} else {
	// 			this.reachedBase = true;
	// 			this.reachedFirst = false;
	// 			this.reachedLast = false;

	// 			setTimeout(() => {
    //                 this.resetScroll('description-editor-form')
    //                 this.pauseIntersectionObserver = false;
    //             });  //wait for next cycle so desc base fields component is rendered
	// 		}
	// 	}
    // }

    publishAction(){
        if(this.isDeleted() || !this.selected?.entry){ return; }
        let fieldId;
        let descriptionId;
        let sectionId;
        if(this.selected.entry.type == ToCEntryType.FieldSet){
            fieldId = this.selected.entry.id;
        }else {
            sectionId = this.selected.entry.id;
        }
        if(this.selected?.entityType == EntityType.Description){
            descriptionId = this.selectedDescription();
        }
        if (fieldId || sectionId) {
            this.planWebSocketService.getClient$().pipe(takeUntil(this._destroyed)).subscribe(stompClient => {
                if (stompClient?.connected) {
                    const payload: UserActionPayload = {
                        fieldSetId: fieldId,
                        descriptionId: descriptionId ?? null,
                        sectionId: sectionId,
                    }
                    stompClient.publish({
                        destination: `/app/plan/${this.plan()?.id?.toString()}/user-action`,
                        body: JSON.stringify(payload)
                    });
                }
            });
        }
    }

    //publishDescriptionAction(entry: ToCEntry){
        //if (this.isDeleted()){ return; }
        // this.planWebSocketService.getClient$().pipe(takeUntil(this._destroyed)).subscribe(stompClient => {
		// 	if (stompClient?.connected) {
		// 		const payload: UserActionPayload = {
        //             blueprintSectionId: this.selectedBlueprint()?.definition?.sections?.[this.step()]?.id ? Guid.parse(this.selectedBlueprint().definition.sections[this.step()].id) : null,
		// 			descriptionId: this.selectedDescription(),
		// 			descriptionPageId: entry?.type === ToCEntryType.RootSection ? entry.id: null,
		// 			descriptionSectionId: entry?.type === ToCEntryType.Section ? entry.id: null,
		// 			descriptionFieldSetId: entry?.type === ToCEntryType.FieldSet ? entry.id: null
		// 		}
		// 		stompClient.publish({
		// 			destination: `/app/plan/${this.plan()?.id?.toString()}/user-action`,
		// 			body: JSON.stringify(payload)
		// 		});
		// 	}
		// });
    //}

    reachedBase: boolean = true;
	reachedLast: boolean = false;
	reachedFirst: boolean = false;


    // toggleExpand(index) {
    //     // this.expandChildrenMap.set(index, !this.expandChildrenMap.get(index));
	// }

	// private scrollToSelectedField(fieldId: Guid) {
	// 	if (!fieldId) return;

	// 	const element = document.getElementById(fieldId.toString());
	// 	if (element) {
	// 		element.scrollIntoView({ behavior: 'smooth' });
	// 	}
	// }

    private scrollToSelected(attempts: number = 12){
        const id = this.selected?.entry?.id;
        if (!id) { return; }
        const element = document.getElementById(id);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
            return;
        }
        // The target may not be in the DOM yet when switching to a different description (its form
        // re-renders). Retry a few times until it appears, then scroll.
        if (attempts > 0) {
            setTimeout(() => this.scrollToSelected(attempts - 1), 50);
        }
    }
    private resetScroll(id: string) {
		if (document.getElementById(id) != null) document.getElementById(id).scrollTop = 0;
	}

    

    /** PERMISSIONS */
    isDeleted = computed(() => this.plan()? this.plan().isActive === IsActive.Inactive : false)
    isFinalized = computed(() => this.plan()?.status?.internalStatus === PlanStatusEnum.Finalized);

    protected planIsEditable = computed(() => this.isNew() || (
        this.plan().belongsToCurrentTenant && !this.isDeleted() && !this.isFinalized()
    ))

    protected descriptionIsEditable(descriptionId: Guid): boolean {
        const descInfo = this.planTempStorage?.descriptions()?.get(descriptionId.toString());
        return descInfo?.isNew || (descInfo?.lastPersist?.status?.internalStatus != DescriptionStatusEnum.Finalized &&
            descInfo?.lastPersist?.isActive != IsActive.Inactive && descInfo?.lastPersist?.belongsToCurrentTenant);
    }

    protected canEditInSection(id: Guid | string): boolean {
		return  this.planIsEditable() && 
            (
                this.authService.hasAnyPermission([AppPermission.EditPlan, AppPermission.EditDescription]) || 
                this.plan()?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EditDescription || x === AppPermission.EditPlan) ||
                this.permissionPerSection()?.[id.toString()]?.some(x => x === AppPermission.EditDescription || x === AppPermission.EditPlan)
            )
        ;
	}

    // protected canEditDescriptionsInSection(id: Guid | string): boolean {
    //     return this.planIsEditable() && 
    //         (
    //             this.authService.hasAnyPermission([AppPermission.EditDescription]) || 
    //             this.plan()?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.EditDescription) ||
    //             this.permissionPerSection()?.[id.toString()]?.some(x => x === AppPermission.EditDescription)
    //         )
    //     ;
    // }

    protected canDeleteInSection(id: Guid, descId: Guid): boolean {
		return this.planIsEditable() && this.descriptionIsEditable(descId) &&
            !this.descriptionLocked(descId) && (
            this.authService.hasPermission(this.authService.permissionEnum.DeleteDescription) || 
            this.plan()?.authorizationInformation?.globalAuthorizationFlags?.some(x => x === AppPermission.DeleteDescription) ||
            this.permissionPerSection()?.[id.toString()]?.some(x => x === AppPermission.DeleteDescription)
        );
	}

    //////
    
    //area focus observer
    private _intersectionObserver: IntersectionObserver;
	private pauseIntersectionObserver: boolean = true;
    private fieldToParentMap = new Map<string, number>([]);


	private _resetObserver() {
		this._cleanUpObserver();

		new Observable(observer => {
			const options = {
				root: null,
				rootMargin: '20% 0px -30% 0px',
				threshold: [0, 0.2, 0.5, 1]
			}
			this._intersectionObserver = new IntersectionObserver(entries => {
				observer.next(entries);
			}, options);

			const fieldsetsEtries = TableOfContentsHelper.getAllFieldSets(this.tocEntries());

			fieldsetsEtries.forEach(e => {
				if (e.type === ToCEntryType.FieldSet) {
					try {
						const targetElement = document.getElementById(e.id);
						this._intersectionObserver.observe(targetElement);
					} catch {
						console.log('element not found');
					}
				}
			});

			return () => { this._intersectionObserver.disconnect(); };
		}).pipe(
			takeUntil(this._destroyed),
			mergeMap((entries: IntersectionObserverEntry[]) => entries),
			filter(entry => entry.isIntersecting),
			debounceTime(300),
			distinctUntilChanged(),
		).subscribe(x => {
			if (!this.pauseIntersectionObserver) {
				const target_id = x.target.id;
				if (this.visibilityRulesService().isVisibleMap[target_id] ?? true) {
                    const entry = TableOfContentsHelper.findTocEntryById(target_id, this.tocEntries());
                    if(entry){
                        this.selected = {
                            entityType: EntityType.Plan,
                            entry
                        }
                    }
                    this.publishAction();
				}
			}
		});
	}
    private _cleanUpObserver(){
        if (this._intersectionObserver) {
			this._intersectionObserver.disconnect();
			this._intersectionObserver = null;
		}
    }

    previousStep(){
        let previous: ToCEntry;

        if (!this.selected) previous = TableOfContentsHelper.getHeadField(this.tocEntries()[0]);
        // else if (this.selected?.entry?.subEntries && this.selected?.entry?.subEntries.length > 0) {
        //     previous = this.selected.entry;
        //     while (previous?.subEntries && previous?.subEntries?.length > 0 && !previous?.previousEntry) {
        //         previous = previous?.subEntries[0];
        //     }
        //     previous = previous.previousEntry;
        // }
        else previous = this.selected?.entry?.previousEntry;

        while (!this._isVisible(previous?.id) && !previous?.isFirstEntry) {
            previous = previous.previousEntry;
        }

        if (!previous || !this._isVisible(previous?.id)) {
            this.selected = null;
        } else {
            this.selected = {
                entityType: this._entryEntityType(previous),
                entry: previous
            };
        }
        const parentDescription = previous?.pathToEntry?.find((id) => this.planTempStorage.descriptions()?.has(id));
        if(parentDescription != this.selectedDescription()?.toString()){
            this.selectedDescription.set(parentDescription ? Guid.parse(parentDescription) : null);
        }

        this.onNavigate.emit(this.selected?.entry);
        setTimeout(() => {
            if(this.selected){
                this.scrollToSelected();
            } else {
                this.resetScroll('editor-form');
            }
        })
        this.publishAction();
    }

    nextStep(){
        let next: ToCEntry;

        if (!this.selected) next = TableOfContentsHelper.getHeadField(this.tocEntries()[0]);
        else if (this.selected?.entry?.subEntries && this.selected?.entry?.subEntries.length > 0) {
            next = this.selected.entry;
            while (next?.subEntries && next?.subEntries?.length > 0 && !next?.nextEntry) {
                next = next?.subEntries[0];
            }
        }
        else next = this.selected?.entry?.nextEntry;

        while (!this._isVisible(next?.id) && !next?.isLastEntry) {
            next = next?.nextEntry;
        }

        if (!next || !this._isVisible(next?.id)) {
            this.selected = null;
        } else {
            this.selected = {
                entityType: this._entryEntityType(next),
                entry: next
            };
        }

        const parentDescription = next.pathToEntry?.find((id) => this.planTempStorage.descriptions()?.has(id));
        if(parentDescription != this.selectedDescription()?.toString()){
            this.selectedDescription.set(parentDescription ? Guid.parse(parentDescription) : null);
        }

        this.onNavigate.emit(this.selected?.entry);
        setTimeout(() => {
            if(this.selected){
                this.scrollToSelected();
            } else {
                this.resetScroll('editor-form');
            }
        })
        this.publishAction();
    }

    private _entryEntityType(entry: ToCEntry): EntityType{
        let type: ToCEntryType;
        if(!entry.pathToEntry?.length){
            type = entry.type;
        } else {
            type = TableOfContentsHelper.findTocEntryById(entry.pathToEntry[0], this.tocEntries())?.type;
        }
        if(type != ToCEntryType.RootDescriptionSection && type != ToCEntryType.RootPlanSection){ 
            console.error(`Selecting entry ${entry.id}: could not establish entity context`);
            return; 
        }
        return type == ToCEntryType.RootDescriptionSection ? EntityType.Description : EntityType.Plan;
    }

    private _isVisible(entryId: string): boolean {
		return this.visibilityRulesService().isVisibleMap[entryId] ?? true;
	}

    // nextStep() {
    //     // if(this.reachedToCEnd){
    //     //     return;
    //     // }
    //     // if(this.selectedDescription() && !this.reachedLast){
    //     //     const descriptionToC = this.descriptionToCList?.find((item) => item.descriptionId() === this.selectedDescription());
    //     //     descriptionToC?.selectNext();   
    //     // } else {
    //     //     const descriptionsInSection = this.descriptionsInSection()?.get(this.selectedSection()?.id)?.map((x) => x.lastPersist.id) ?? []; 
    //     //     const index = this.selectedDescription() ? Array.from(descriptionsInSection).findIndex((x) => x === this.selectedDescription()) : -1;
    //     //     const section = this.selectedBlueprint()?.definition?.sections[this.step()] ?? null;
    //         // if(descriptionsInSection.length && index < descriptionsInSection.length - 1){
    //         //     this.changePlanStep({section: this.step() + 1, sectionId: section?.id, fieldId: this.getSectionFields(section)?.[0]?.id ?? null, descriptionId: descriptionsInSection[index + 1]});
    //         // } else {
    //         //     this.changePlanStep({section: this.step() + 1, sectionId: section?.id, fieldId: this.getSectionFields(section)?.[0]?.id ?? null});
    //         // }
    //     // }
	// 	this.resetScroll('editor-form');
	// }

	// previousStep() {
    //     if(this.selectedDescription() && !this.reachedBase){
    //         const descriptionToC = this.descriptionToCList?.find((item) => item.descriptionId() === this.selectedDescription());
    //         descriptionToC?.selectPrevious();
    //     } else {
    //         this.previousPlanStep()
    //     }
	// }
    
    // previousPlanStep(){
    //     this.resetScroll('editor-form');
    //     // const section = this.selectedBlueprint()?.definition?.sections[this.step()] ?? null;
    //     // if(this.selectedDescription()){
    //     //     const descriptionsInSection = this.descriptionsInSection()?.get(this.selectedSection()?.id) ?? []; 
    //     //     const index = descriptionsInSection.findIndex((x) => x.lastPersist.id === this.selectedDescription());
    //     //     const prevDescription = index > 0 ? descriptionsInSection[index - 1] : null;
    //     //     if(prevDescription){
    //     //         // this.changePlanStep({section: this.step(), sectionId: section?.id, fieldId: this.getSectionFields(section)?.[0]?.id ?? null, descriptionId: prevDescription?.lastPersist?.id});
    //     //         return;
    //     //     }
    //     // }
    //     // this.changePlanStep({section: this.step() - 1, sectionId: section?.id, fieldId: this.getSectionFields(section)?.[0]?.id ?? null,});
    // }



    get reachedToCBeginning(): boolean {
        return this.selected?.entry?.isFirstEntry;
    }

    get reachedToCEnd(): boolean{
        return this.selected?.entry?.isLastEntry;
        // const selectedSectionDescriptions = this.descriptionsInSection()?.get(this.selectedSection()?.id)?.map((x) => x.lastPersist.id) ?? []
        // const endOfDescription = this.selectedDescription() && this.reachedLast;
        // const lastDescriptionInSection = this.selectedDescription() && selectedSectionDescriptions.indexOf(this.selectedDescription()) === selectedSectionDescriptions.length -1;
        // return this.step() === this.selectedBlueprint().definition.sections?.length && (!selectedSectionDescriptions.length || (endOfDescription && lastDescriptionInSection));
    }

    protected getAddDescriptionText(entry: RootToCEntry): string{
        const sectionTemplates = this.plan().planDescriptionTemplates?.filter((x) => x.sectionId === entry.id)?.map((x) => x.currentDescriptionTemplate);
        const descTemplateTypes = new Set(sectionTemplates?.map((x) => x.type?.id) ?? []); 
        if(!entry.canEditDescriptionTemplates && descTemplateTypes.size === 1){
            const template = this.plan().planDescriptionTemplates.filter(x => x.sectionId == entry.id)?.[0]?.currentDescriptionTemplate;
            return template?.type?.name;
        }
        return ;
    }
    
	// getSectionFields(section: PlanBlueprintDefinitionSection): FieldInSection[] {
	// 	return (section?.fieldSets ?? []).flatMap(fs => fs.fields ?? []);
	// }

	countAnnotationsPerSection = computed(() => {
		const map = new 	Map<string, number> ();
		this.selectedBlueprint()?.definition?.sections.forEach(section  =>{
			let count = 0;
			const sectionAllFieldSets = (section?.fieldSets ?? []);
			for (const fieldSet of sectionAllFieldSets) {
				count += (this.annotationsPerAnchor()?.get(fieldSet.id)??0);
			}
			map.set(section.id, count);
		});
		return map;

	});

    expandL1(){
        this.sectionAccordion?.openAll();
        this.descriptionAccordions?.forEach((accordion) => accordion.openAll());
    }

    expandL2(){
        this.expandL1();
        this.descriptionToCList?.forEach((descriptionToC) => descriptionToC.expandL1());
    }

    expandAll(){
        this.expandL1();
        this.descriptionToCList?.forEach((descriptionToC) => descriptionToC.expandAll());
    }

    collapseAll(){
        this.sectionAccordion?.closeAll();
        this.descriptionAccordions?.forEach((accordion) => accordion.closeAll());
        this.descriptionToCList?.forEach((descriptionToC) => descriptionToC.collapseAll());
    }

    removeDescription(id: Guid, section: RootToCEntry){
        this.onRemoveDescription.emit(this.planTempStorage.descriptions()?.get(id.toString()));
    }

    ///PUBLIC ACTIONS

    public goToFirstStep(){
        const entry = TableOfContentsHelper.getHeadField(this.tocEntries()?.[0]);
        this.selected = {
            entityType: EntityType.Plan,
            entry
        }

        const parentDescription = entry?.pathToEntry?.find((id) => this.planTempStorage.descriptions()?.has(id));
        if(parentDescription != this.selectedDescription()?.toString()){
            this.selectedDescription.set(parentDescription ? Guid.parse(parentDescription) : null);
        }

        this.publishAction();
        this.lockSelected();
    }

    public goToDescription(id: Guid){
        const entry = TableOfContentsHelper.findTocEntryById(id.toString(), this.tocEntries());
        if(entry){
            this.onDescriptionSelected(entry);
        }
    }

    public goToEntry(id: string, descriptionId?: string){
        const entry = id ? TableOfContentsHelper.findTocEntryById(id, this.tocEntries()) : null;
        if(entry){
            if(descriptionId){
                this.onDescriptionEntrySelected(entry, descriptionId);
            }
            // if(entry.type == ToCEntryType.RootPlanSection){
            //     this.onEntrySelected(entry);
            //     return;
            // } 
            else if(entry.type == ToCEntryType.Description){
                this.onDescriptionSelected(entry);
            } else {
                this.onEntrySelected(entry);
                // let parentDescription: ToCEntry;
                // for(let i = 0; i < entry.pathToEntry?.length; i++){
                //     let parentEntry = TableOfContentsHelper.findTocEntryById(entry.pathToEntry[i], this.tocEntries);
                //     if(parentEntry?.type == ToCEntryType.Description){
                //         parentDescription = entry;
                //         break;
                //     }
                //     //if parent is not a plan root section
                //     //and will not find a description any further than this
                //     if(parentEntry.type != ToCEntryType.RootPlanSection){
                //         break;
                //     }
                // }
                // if(parentDescription){
                //     this.onDescriptionEntrySelected(entry, parentDescription.id);
                // }else {
                //     this.onEntrySelected(entry);
                // }
            }
            setTimeout(() => this.scrollToSelected())
        }else {
            this.onEntrySelected(this.tocEntries()?.[0])
        }
    }

    // public addDescriptionToToc(id: Guid, sectionId: string ){
    //     const entry = TableOfContentsHelper.findTocEntryById(sectionId, this.tocEntries) as RootToCEntry;
    //     const descInfo = this.planTempStorage.descriptions()?.get(id.toString());
    //     if(!descInfo || !entry || entry.type !== ToCEntryType.RootPlanSection){ return; }
    //     let descEntry = this.buildDescriptionEntry(descInfo, entry.descriptions.length, entry )
    //     entry.descriptions.push(descEntry);
    //     this.onDescriptionSelected(descEntry);
    // }
    
    // public removeDescriptionFromToC(descriptionId: Guid){
    //     const stringId = descriptionId.toString();
    //     const parentEntry = this.tocEntries.find((x) => x.subEntries?.some((x) => x.id == stringId));
    //     parentEntry.subEntries = parentEntry.subEntries.filter(x => x.id !== stringId);
    //     if(this.selectedDescription().toString() == stringId){
    //         this.selected = {
    //             entityType: EntityType.Plan,
    //             entry: parentEntry
    //         };
    //     }
    // }

    //////

    getTocEntries(planBlueprint: PlanBlueprint): RootToCEntry[] {
        if (!planBlueprint?.definition?.sections) { return []; }
        const result: RootToCEntry[] = [];

        const previousTocEntry: ToCEntry = null;

        //build parent sections
        planBlueprint.definition.sections.forEach((section, i) => {
            const tempResult = TableOfContentsHelper.buildRecursivelySection({
                item: section,
                previousTocEntry,
                path: [],
                fieldSetFormGroup: this.formGroup().controls.properties.controls.fieldSets,
                formGroup: this.formGroup()
            });
            const tocEntry: RootToCEntry = {
                ...tempResult,
                id: section.id,
                label: section.title,
                type: ToCEntryType.RootPlanSection,
                numbering: (i + 1).toString(),
                ordinal: section.ordinal,
                visibilityRuleKey: section.id,
                validityAbstractControls: null,
                pathToEntry: [],
                description: section.description,
                descriptionTemplates: section.descriptionTemplates,
                canEditDescriptionTemplates: section.canEditDescriptionTemplates,
                hasTemplates: section.hasTemplates,
                descriptions: [],
                isFirstEntry: i == 0,
            };
            const descriptions = this.descriptionsInSection()?.get(section.id);
            const descriptionEntries: ToCEntry[] = [];
            if(descriptions?.length){
                descriptions.forEach((desc, index) => {
                    const descEntry = this.buildDescriptionEntry(desc, index, tocEntry);
                    const headField = TableOfContentsHelper.getHeadField(descEntry);
                    if(descriptionEntries.length){
                        const prevLastField =  TableOfContentsHelper.getTailField(descriptionEntries[descriptionEntries.length - 1]) ?? null;
                        headField.previousEntry = prevLastField;
                        headField.isFirstEntry = false;
                        prevLastField.nextEntry = headField;
                        prevLastField.isLastEntry = false;
                    }else{
                        headField.previousEntry = TableOfContentsHelper.getTailField(tocEntry);
                        headField.isFirstEntry = false;
                    }
                    descriptionEntries.push(
                        descEntry
                    )
                });
                descriptionEntries[0].isFirstEntry = false;
                descriptionEntries[descriptionEntries.length - 1].isLastEntry = false;
                TableOfContentsHelper.getTailField(tocEntry).nextEntry = descriptionEntries[0];
                tocEntry.descriptions = descriptionEntries;
            }

            if(result.length){
                const headField = TableOfContentsHelper.getHeadField(tocEntry);
                const prevTailField = TableOfContentsHelper.getTailField(result[result.length - 1]);
                headField.previousEntry = prevTailField;
                headField.isFirstEntry = false;
                prevTailField.nextEntry = headField;
                prevTailField.isLastEntry = false;
            }

            // // explanation: previous-section[last-field].next = current-section[first-field] 
            // let firstTocentryOfCurrentSection = TableOfContentsHelper.getHeadField(tempResult);
            // if (tocEntry.subEntries.length > 0) {
            //     tocEntry.subEntries[tocEntry.subEntries.length - 1] = TableOfContentsHelper.setTocEntryNext(tocEntry.subEntries[tocEntry.subEntries.length - 1], firstTocentryOfCurrentSection);
            // }

            result.push(tocEntry)
        });
        if(result.length){
            TableOfContentsHelper.getHeadField(result[0]).isFirstEntry = true;
            TableOfContentsHelper.getTailField(result[result.length - 1]).isLastEntry = true;
        }
        TableOfContentsHelper.sortByOrdinal(result);
        //calculate numbering
        TableOfContentsHelper.calculateNumbering(result);
        // TableOfContentsHelper.calculatePlacement(result);
        return result;
    }

    buildDescriptionEntry(descriptionInfo: DescriptionInfo, index: number, parent: RootToCEntry): ToCEntry {
        return {
            id: descriptionInfo.lastPersist.id.toString(),
            ordinal: (index + 1),
            subEntriesType: ToCEntryType.Section,
            label: '', //label changes depending on formGroup
            numbering: this.descriptionOrdinal(parent, (index + 1)),
            pathToEntry: [parent.id],
            validityAbstractControls: [descriptionInfo.formGroup],
            visibilityRuleKey: null,
            type: ToCEntryType.Description,
            subEntries: TableOfContentsHelper.getDescriptionTocEntries({
                descriptionTemplate: descriptionInfo.lastPersist.descriptionTemplate,
                fieldSetFormGroup: descriptionInfo.formGroup?.controls?.properties?.controls?.fieldSets,
                pathToEntry: [parent.id, descriptionInfo.lastPersist.id.toString()],
                formGroup: descriptionInfo.formGroup
            })
        }
    }

    public reloadTocEntries(){
        this.tocEntries.set(this.getTocEntries(this.selectedBlueprint()));
    }

    public isOrContainsSelected(entryId: string): boolean {
        if(!this.selected){ return false; }
        return entryId === this.selected.entry?.id || this.selected.entry?.pathToEntry?.includes(entryId);
    }

    addDescription(entry: ToCEntry) {
        const section = this.selectedBlueprint()?.definition?.sections?.find(x => x.id == entry.id);
        if(section){
            this.onAddDescription.emit(section)
        }
    }

    storageFileMap = new Map<string, SafeUrl>([]);
    protected downloadStorageFiles(storageFileIds: Guid[]){
        const newIds = storageFileIds?.filter(x => !this.storageFileMap.has(x?.toString()));
        newIds?.forEach((id) =>
            this.storageFileService.download(id)
            .pipe(takeUntil(this._destroyed), tap((res) => {
                const url = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(res.body));
                this.storageFileMap.set(id?.toString(), url);
            }))
            .subscribe()
        )
    }

    //** LOCKS */
    descriptionLocked(id: Guid): boolean {
        return this.planLockService?.externalLockIds()?.includes(id?.toString());
    }
    
    private lockSelected(){
        this.planLockService.unlockTransient().subscribe(() => {
            if(this.selectedDescription()){
                const canEditDescription = !this.descriptionInfo().isNew &&
                    this.canEditInSection(this.descriptionInfo().lastPersist.planDescriptionTemplate?.sectionId) && 
                    this.descriptionIsEditable(this.selectedDescription());
                if(!canEditDescription){ return; }
                const formGroup = this.descriptionInfo().formGroup;
                this.planLockService.checkAndLock(this.selectedDescription(), LockTargetType.Description, formGroup);
            }else if(this.plan()?.id && this.canEditPlan()){
                this.planLockService.checkAndLock(this.plan().id, LockTargetType.Plan, this.formGroup());
            }
        })
    }
}

export interface RootToCEntry extends ToCEntry {
    description?: string;
    hasTemplates?: boolean;
    descriptionTemplates?: DescriptionTemplatesInSection[];
    canEditDescriptionTemplates?: boolean;
    descriptions: ToCEntry[];
}

/** Rolled-up "still to complete" figure for a ToC chip: the total and the names behind it. */
export interface PendingSummary {
    count: number;
    labels: string[];
}

export interface SelectedEntry {
    entityType: EntityType.Plan | EntityType.Description;
    entry: ToCEntry
}