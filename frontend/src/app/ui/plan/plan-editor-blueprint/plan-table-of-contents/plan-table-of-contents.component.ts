import { Component, computed, effect, input, model, output, QueryList, Signal, ViewChildren } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { PlanBlueprint, PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan } from '@app/core/model/plan/plan';
import { BaseComponent } from '@common/base/base.component';
import { PlanFieldIndicator, PlanEditorForm } from '../plan-editor.model';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { debounceTime, distinctUntilChanged, filter, mergeMap, Observable, takeUntil } from 'rxjs';
import { ToCEntry } from '@app/ui/description/editor/table-of-contents/models/toc-entry';
import { DescriptionInfo, PlanTempStorageService } from '../plan-temp-storage.service';
import { TableOfContentsComponent } from '@app/ui/description/editor/table-of-contents/table-of-contents.component';
import { TableOfContentsService } from '@app/ui/description/editor/table-of-contents/services/table-of-contents-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';

@Component({
    selector: 'app-plan-table-of-contents',
    templateUrl: './plan-table-of-contents.component.html',
    styleUrl: './plan-table-of-contents.component.scss',
    standalone: false
})
export class PlanTableOfContentsComponent extends BaseComponent{
    @ViewChildren('descriptionToC') descriptionToCList: QueryList<TableOfContentsComponent>;

    protected plan = input<Plan>();
    protected selectedBlueprint = input<PlanBlueprint>();
    protected formGroup = input<FormGroup<PlanEditorForm>>();
    protected canEdit = input<Boolean>();
    protected isNew = input<Boolean>();
    protected permissionPerSection = input<Map<Guid, string[]>>();
    protected showDescriptionErrors = input<Set<Guid>>(new Set([]));
    protected showPlanErrors = input<boolean>(false);


    //ToC step selection 
    protected step = model<number>();
    protected selectedFieldId: string;
    protected selectedSection = computed(() => this.step() ? this.plan()?.blueprint?.definition?.sections?.[this.step() - 1] : null)

    //DESCRIPTION
    protected selectedDescription = model<Guid>(null);
    protected descriptionInfo = computed(() => this.planTempStorage.descriptions().get(this.selectedDescription().toString()))
    protected descriptionFormGroup = computed(() => this.descriptionInfo()?.formGroup);
    protected selectedDescriptionStatic = computed(() => this.descriptionInfo()?.lastPersist);
    protected visibilityRulesService = computed(() => this.descriptionInfo()?.visibilityRulesService ?? null);

    protected onRemoveDescription = output<DescriptionInfo>();
    protected onAddDescription = output<Guid>();

	protected descriptionStatusEnum = DescriptionStatusEnum;

    protected sectionToFieldsMap: Map<Guid, PlanFieldIndicator> = new Map<Guid, PlanFieldIndicator>();


    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;
    
    constructor(
        private planTempStorage: PlanTempStorageService,
        private descriptionToCService: TableOfContentsService,
        protected enumUtils: EnumUtils
    ) {
        super();
        effect(() => {
            const formGroup = this.formGroup();
            const selectedBlueprint = this.selectedBlueprint();
            if(selectedBlueprint){
                this.initializeSectionInfo();
                setTimeout(() => this._resetObserver());
            }
        });
    }

    isDeleted = computed(() => this.plan()? this.plan().isActive === IsActive.Inactive : false)
    isFinalized = computed(() => this.plan()?.status?.internalStatus === PlanStatusEnum.Finalized);

    maxSteps = computed(() => this.plan()?.blueprint?.definition?.sections?.length ?? 0);

    initializeSectionInfo() {
        if(!this.selectedBlueprint()?.definition) { return;}
        this.sectionToFieldsMap = new Map([]);
        this.selectedBlueprint().definition.sections.forEach((section: PlanBlueprintDefinitionSection) => {
			let value: PlanFieldIndicator = new PlanFieldIndicator(section);
			this.sectionToFieldsMap.set(section.id, value);
		});
    }

    protected descriptionsInSection: Signal<Map<Guid, DescriptionInfo[]>> = computed(() => {
        const descriptionsMap = new Map<Guid,DescriptionInfo[]>([]);
        this.planTempStorage.descriptionIdsBySectionMap().forEach((value, key) => descriptionsMap.set(key, value?.map((id) => this.planTempStorage.descriptions().get(id.toString()))));
        return descriptionsMap;
    }); 

    descTemplatesTouched(section: PlanBlueprintDefinitionSection): boolean {
        return this.formGroup()?.controls?.descriptionTemplates?.get(section.id.toString())?.dirty;
    }

	hasValidMultiplicity(section: PlanBlueprintDefinitionSection): boolean {
        if (!section.hasTemplates){
            return false;
        }
        if(!section.descriptionTemplates?.length){
            return true;
        }
        const descriptions = this.descriptionsInSection().get(section.id) ?? [];
        const distinctTemplates = new Set(descriptions.map(x => x.formGroup?.value?.planDescriptionTemplateId));

        if (this.plan().planDescriptionTemplates.filter(x => x.sectionId == section.id).length > distinctTemplates.size) {
            return true;
        }

        const descriptionPlanDescriptionTemplates = descriptions.map((x) => this.planTempStorage.planDescriptionTemplates.get(x.formGroup?.value?.planDescriptionTemplateId)).filter((x) => x);
        const descriptionTemplateGroupIds = descriptionPlanDescriptionTemplates.map((x) => x?.descriptionTemplateGroupId);
        return section.descriptionTemplates.some(sectionDescriptionTemplate => {
            if (sectionDescriptionTemplate.maxMultiplicity != null) {
                const groupId = sectionDescriptionTemplate.descriptionTemplate?.groupId;
                const count = descriptionTemplateGroupIds.filter(x => x === groupId).length;
                return count < sectionDescriptionTemplate.maxMultiplicity;
            } else {
                return true;
            }
        })
	}

    descriptionOrdinal(section: PlanBlueprintDefinitionSection, baseOrdinal: number): string{
        return `${section.ordinal}.${section.fields?.length ? (section.fields?.length + 1 + '.') : ''}${baseOrdinal}`;
    }

    hasDescriptionTemplates(section: PlanBlueprintDefinitionSection): boolean {
		if (this.plan().planDescriptionTemplates?.filter(x => x.sectionId == section.id).length > 0) return true;
		return false;
	}

	showSectionErrors(sectionId: Guid): boolean {
		const formControlBySection = this.sectionToFieldsMap?.get(sectionId);
		if (formControlBySection == null || this.formGroup() == null || ((this.formGroup()?.disabled??false)==true || (!this.isNew() && !this.canEdit()))) return false;

		for (let control of formControlBySection.fieldControls) {
			if (this.formControlValid(control.formControlName) === false) {
				return true;
			}
		}

		return false;
	}
    showFieldError(sectionId: Guid, fieldId: Guid): boolean{
        if((!this.isNew() && !this.canEdit()) || !this.formGroup() || ((this.formGroup()?.disabled??false) == true)){
            return false;
        }

        const formControlBySection = this.sectionToFieldsMap?.get(sectionId);
		if (formControlBySection == null || formControlBySection == undefined){
            return false;
        }

        const control = formControlBySection.fieldControls.find((x) => x.id === fieldId);
        return control && control && this.formControlValid(control.formControlName) === false
    }

    invalidDescription(descriptionMetadata: DescriptionInfo){
        return descriptionMetadata && descriptionMetadata.lastPersist.status.internalStatus === DescriptionStatusEnum.Draft && !descriptionMetadata.formGroup.valid;
    }

	formControlValid(controlName: string): boolean {
		if (!this.formGroup()?.get(controlName)){
            return true
        }
		return this.formGroup()?.get(controlName).valid;
	}

	changePlanStep(params: {
        section: number, 
        fieldId?: Guid,
        descriptionId?: Guid,
        descriptionFieldId?: string
    }) {
        const {section: index, fieldId, descriptionId, descriptionFieldId} = params;
        this.pauseIntersectionObserver = true;

        if(index != this.step() && index >= 1 && index <= this.selectedBlueprint().definition.sections?.length){
            this.step.set(index);
            // this.expandChildrenMap.set(index, true);
        }

        this.selectedDescription.set(descriptionId ?? null);
        if(descriptionId){
            const descriptionToC = this.descriptionToCList?.find((item) => item.descriptionId() === descriptionId);
            setTimeout(() => descriptionToC?.selectField(descriptionFieldId ?? null)); //wait for next cycle so table of contents component is initiated
        } else {
            this.pauseIntersectionObserver = false;
        }

        this.selectedFieldId = fieldId?.toString() ?? null;
        if(this.selectedFieldId){
            setTimeout(() => {this.scrollToSelectedField(fieldId)}); //wait for next cycle so plan section is rendered
        } else {
            this.resetScroll('editor-form');
        }
	}

    reachedBase: boolean = true;
	reachedLast: boolean = false;
	reachedFirst: boolean = false;

    public changeDescriptionStep(selected: ToCEntry = null, execute: boolean = true) {
        this.selectedFieldId = selected?.id;
		if (execute) {
			if (selected) {
				this.reachedBase = false;
				this.reachedFirst = selected.isFirstEntry;
				this.reachedLast = selected.isLastEntry;
				const element = document.getElementById(selected.id);
				if (element) {
					setTimeout(() => element.scrollIntoView({ behavior: 'smooth' })); //wait for next cycle so desc form component is rendered
				}
			} else {
				this.reachedBase = true;
				this.reachedFirst = false;
				this.reachedLast = false;

				setTimeout(() => this.resetScroll('description-editor-form'));  //wait for next cycle so desc base fields component is rendered
			}
		}
    }

    toggleExpand(index) {
        // this.expandChildrenMap.set(index, !this.expandChildrenMap.get(index));
	}

	public scrollToSelectedField(fieldId: Guid) {
		if (!fieldId) return;

		const element = document.getElementById(fieldId.toString());
		if (element) {
			element.scrollIntoView({ behavior: 'smooth' });
		}
	}

    private resetScroll(id: string) {
		if (document.getElementById(id) != null) document.getElementById(id).scrollTop = 0;
	}


    protected canEditInSection(id: Guid): boolean {
		return  this.plan().belongsToCurrentTenant && !this.isDeleted() && !this.isFinalized() && this.permissionPerSection()?.[id.toString()]?.some(x => x === AppPermission.EditDescription);
	}

    protected canDeleteInSection(id: Guid): boolean {
		return this.plan().belongsToCurrentTenant && !this.isDeleted() && !this.isFinalized() && this.permissionPerSection()?.[id.toString()]?.some(x => x === AppPermission.DeleteDescription);
	}

    
    //area focus observer
    private _intersectionObserver: IntersectionObserver;
	private pauseIntersectionObserver: boolean = true;
    private fieldToParentMap = new Map<string, number>([]);


	private _resetObserver() {
		if (this._intersectionObserver) {//clean up
			this._intersectionObserver.disconnect();
			this._intersectionObserver = null;
		}

		new Observable(observer => {
			const options = {
				root: null,
				rootMargin: '0px 0px 5% 0px',
				threshold: 1.0
			}
			this._intersectionObserver = new IntersectionObserver(entries => {
				observer.next(entries);
			}, options);

			const fields = []; 
            this.selectedBlueprint()?.definition?.sections?.forEach((section) => {
                section.fields?.forEach(
                    (field) => this.fieldToParentMap.set(field.id.toString(), section.ordinal)
                )
                fields.push(...(section.fields ?? []));
            });

			fields.forEach(e => {
                try {
                    const targetElement = document.getElementById(e.id.toString());
                    this._intersectionObserver.observe(targetElement);
                } catch {
                    console.log('element not found');
                }
			});
			return () => { this._intersectionObserver.disconnect(); };
		}).pipe(
			takeUntil(this._destroyed),
			mergeMap((entries: IntersectionObserverEntry[]) => entries),
			filter(entry => entry.isIntersecting),
			debounceTime(200),
			distinctUntilChanged(),
		).subscribe(x => {
			if (!this.pauseIntersectionObserver) {
				const target_id = x.target.id;
                try {
                    this.selectedFieldId = target_id;
                } catch {
                    console.log('field with target id not found');
                }
			}
		});
	}

    nextStep() {
        if(this.reachedToCEnd){
            return;
        }
        if(this.selectedDescription() && !this.reachedLast){
            const descriptionToC = this.descriptionToCList?.find((item) => item.descriptionId() === this.selectedDescription());
            descriptionToC?.selectNext();   
        } else {
            const descriptionsInSection = this.descriptionsInSection()?.get(this.selectedSection()?.id)?.map((x) => x.lastPersist.id) ?? []; 
            const index = this.selectedDescription() ? Array.from(descriptionsInSection).findIndex((x) => x === this.selectedDescription()) : -1;
            if(descriptionsInSection.length && index < descriptionsInSection.length - 1){
                this.changePlanStep({section: this.step(), descriptionId: descriptionsInSection[index + 1]})
            } else {
                this.changePlanStep({section: this.step() + 1})
            }
        }
		this.resetScroll('editor-form');
	}

	previousStep() {
        if(this.selectedDescription() && !this.reachedBase){
            const descriptionToC = this.descriptionToCList?.find((item) => item.descriptionId() === this.selectedDescription());
            descriptionToC?.selectPrevious();
        } else {
            this.previousPlanStep()
        }
	}
    
    previousPlanStep(){
        this.resetScroll('editor-form');
        if(this.selectedDescription()){
            const descriptionsInSection = this.descriptionsInSection()?.get(this.selectedSection()?.id) ?? []; 
            const index = descriptionsInSection.findIndex((x) => x.lastPersist.id === this.selectedDescription());
            const prevDescription = index > 0 ? descriptionsInSection[index - 1] : null;
            if(prevDescription){
                this.changePlanStep({section: this.step(), descriptionId: prevDescription?.lastPersist?.id});
                return;
            }
        }
        this.changePlanStep({section: this.step() - 1});
    }

    get reachedToCEnd(): boolean{
        const selectedSectionDescriptions = this.descriptionsInSection()?.get(this.selectedSection()?.id)?.map((x) => x.lastPersist.id) ?? []
        const endOfDescription = this.selectedDescription() && this.reachedLast;
        const lastDescriptionInSection = this.selectedDescription() && selectedSectionDescriptions.indexOf(this.selectedDescription()) === selectedSectionDescriptions.length -1;
        return this.step() === this.selectedBlueprint().definition.sections?.length && (!selectedSectionDescriptions.length || (endOfDescription && lastDescriptionInSection));
    }
}

