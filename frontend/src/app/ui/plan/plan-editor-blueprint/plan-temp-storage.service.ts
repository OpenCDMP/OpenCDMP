import { Injectable, signal } from "@angular/core";
import { FormGroup } from "@angular/forms";
import { DescriptionTemplate } from "@app/core/model/description-template/description-template";
import { Description } from "@app/core/model/description/description";
import { PlanDescriptionTemplate } from "@app/core/model/plan/plan";
import { DescriptionEditorForm } from "@app/ui/description/editor/description-editor.model";
import { VisibilityRulesService } from "@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service";
import { ValidationErrorModel } from "@common/forms/validation/error-model/validation-error-model";
import { Guid } from "@common/types/guid";
@Injectable()
export class PlanTempStorageService {
    private _descriptions = signal<Map<string, DescriptionInfo>>(new Map<string, DescriptionInfo>([])); //desc id to description info
    public descriptions = this._descriptions.asReadonly();
    
    private _descriptionIdsBySectionMap =  signal<Map<string, Guid[]>>(new Map([])); //section id to Description Ids
    public descriptionIdsBySectionMap = this._descriptionIdsBySectionMap.asReadonly();

    // Single source for description numbering: computed once by the table-of-contents and read by the
    // description editor, so both always show the same number (no divergent per-component formulas).
    private _descriptionNumbering = signal<Map<string, string>>(new Map<string, string>([])); //desc id to numbering label
    public descriptionNumbering = this._descriptionNumbering.asReadonly();
    public setDescriptionNumbering(map: Map<string, string>) {
        this._descriptionNumbering.set(map ?? new Map<string, string>([]));
    }

    private _descriptionTemplates = new Map<string, DescriptionTemplate>([]); //templateId to template info
    private _planDescriptionTemplates = new Map<string, PlanDescriptionTemplate>([]); //planDescriptionTemplateId to plan description template info
    public get planDescriptionTemplates() { return this._planDescriptionTemplates }
    public getPlanDescriptionTemplate(id: Guid | string): PlanDescriptionTemplate {
        return this._planDescriptionTemplates.get(id?.toString());
    }

    public clearDescriptions(){
        this._descriptions.set(new Map([]));
        this._descriptionIdsBySectionMap.set(new Map([]));
    }
    public setDescription(params: {
        lastPersist: Description, 
        formGroup: FormGroup<DescriptionEditorForm>, 
        visibilityRulesService: VisibilityRulesService,
        validationErrorModel: ValidationErrorModel,
        isNew?: boolean,
    }){
        const {lastPersist, formGroup, isNew = false, visibilityRulesService, validationErrorModel} = params;
        if(!lastPersist){ return;}
        const sectionId = lastPersist.planDescriptionTemplate?.sectionId;
        const sectionDescriptions = this._descriptionIdsBySectionMap().get(sectionId);
        const existingData = this._descriptions().get(lastPersist.id.toString());

        this._descriptions.update((oldMap) => {
            const newMap = new Map(oldMap);
            newMap.set(lastPersist.id.toString(), {
                lastPersist: lastPersist,
                formGroup,
                isNew,
                visibilityRulesService,
                validationErrorModel,
                ordinal: existingData?.ordinal ?? ((sectionDescriptions?.length ?? 0) + 1).toString()
            });
            return newMap;
        })
        // FIX #7: guard against falsy sectionId so we don't dereference undefined `sectionDescriptions` below
        if(!sectionId){ return; }
        const idStr = lastPersist.id.toString();
        if(!sectionDescriptions){
            this._descriptionIdsBySectionMap.update((map) => {
                const newMap = new Map(map);
                newMap.set(sectionId, [lastPersist.id]);
                return newMap;
            })
        } else if(!sectionDescriptions.some(x => x.toString() === idStr)) { // FIX #1: Guids aren't reference-equal, compare on string value
            this._descriptionIdsBySectionMap.update((map) => {
                const newMap = new Map(map);
                // FIX #12: clone the inner array so we don't mutate the previous signal snapshot
                const updatedIds = [...(newMap.get(sectionId) ?? []), lastPersist.id];
                newMap.set(sectionId, updatedIds);
                return newMap;
            })
        }
        if(lastPersist.descriptionTemplate){
            this.setDescriptionTemplate(lastPersist.descriptionTemplate);
        }

    }

    // public getNewDescriptions(): DescriptionInfo[]{
    //     return Array.from(this._descriptions.values()).filter((x) => x.isNew);
    // }


    public removeDescription(id: Guid) {
        if(!id){ return; }

        const idStr = id.toString();
        const sectionId = this._descriptions().get(idStr)?.lastPersist?.planDescriptionTemplate?.sectionId;
        // FIX #1: Guids aren't reference-equal, compare on string value
        const idx = this._descriptionIdsBySectionMap().get(sectionId)?.findIndex((x) => x.toString() === idStr);
        if(idx >= 0){
            this._descriptionIdsBySectionMap.update((oldMap) => {
                const newMap = new Map(oldMap);
                // FIX #12: clone the inner array instead of splicing the shared snapshot
                const updatedIds = [...(newMap.get(sectionId) ?? [])];
                updatedIds.splice(idx, 1);
                newMap.set(sectionId, updatedIds);
                return newMap;
            })
        }
        this.reCalculateOrdinals(sectionId);

        this._descriptions.update((oldMap) => {
            const newMap = new Map(oldMap);
            newMap.delete(idStr);
            return newMap;
        })

    }

    public refreshDescriptions(){
        this._descriptions.update((map) => new Map(map));
    }

    private reCalculateOrdinals(sectionId){
        const ids = this._descriptionIdsBySectionMap().get(sectionId);
        if(!ids?.length){ return; }
        // FIX #2: signal updates must go through .update() with a new Map; mutating the
        // asReadonly map directly leaves the reference unchanged, so dependent computeds never re-run
        this._descriptions.update((oldMap) => {
            const newMap = new Map(oldMap);
            ids.forEach((id, index) => {
                const description = newMap.get(id.toString());
                if(!description){ return; }
                newMap.set(id.toString(), {
                    ...description,
                    ordinal: (index + 1).toString()
                });
            });
            return newMap;
        });
    }



    public getDescriptionTemplate(id: Guid | string): DescriptionTemplate {
        return this._descriptionTemplates.get(id?.toString());
    }
    public setDescriptionTemplate(template: DescriptionTemplate) {
        if(!template){return;}
        this._descriptionTemplates.set(template.id?.toString(), template);
        //! to make it possible to also find templates when looking through plan form group, which only has knowledge of groupId
        this._descriptionTemplates.set(template.groupId?.toString(), template);
    }
    public clearDescriptionTemplates(){
        this._descriptionTemplates.clear();
    }

    public setPlanDescriptionTemplate(template: PlanDescriptionTemplate) {
        if(!template){return;}
        this._planDescriptionTemplates.set(template.id?.toString(), template)
    }
    public clearPlanDescriptionTemplates(){
        this._planDescriptionTemplates.clear();
    }

    reset(){
        this._descriptions.set(new Map<string, DescriptionInfo>([]));
        this._descriptionIdsBySectionMap.set(new Map([]));
        this._descriptionTemplates = new Map<string, DescriptionTemplate>([]);
        this._planDescriptionTemplates = new Map<string, PlanDescriptionTemplate>([]);
    }

    /**
     * Marks every stored description as no longer new and marks its form pristine. Used after a save
     * that does NOT refetch the plan (e.g. plan-editor's Save & Close), so a description created and
     * saved in the same session doesn't keep reporting itself dirty (a fresh `isNew` description is
     * always considered dirty regardless of its form state — see EditorSessionStateService.descriptionState).
     * The "Save & Continue" path avoids this instead by rebuilding every description from freshly
     * fetched data, which naturally clears `isNew`.
     */
    public markAllDescriptionsPersisted(){
        this._descriptions.update((oldMap) => {
            const newMap = new Map(oldMap);
            newMap.forEach((info, id) => {
                info.formGroup?.markAsPristine();
                if (info.isNew) { newMap.set(id, { ...info, isNew: false }); }
            });
            return newMap;
        });
    }
}

//Description metadata
export interface DescriptionInfo {
    lastPersist: Description, //static info only changed after persist
    formGroup: FormGroup<DescriptionEditorForm>,
    visibilityRulesService: VisibilityRulesService,
    validationErrorModel: ValidationErrorModel
    isNew?: boolean,
    ordinal?: string
}