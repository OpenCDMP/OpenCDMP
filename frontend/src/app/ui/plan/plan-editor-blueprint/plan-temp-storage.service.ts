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
    
    private _descriptionIdsBySectionMap =  signal<Map<Guid, Guid[]>>(new Map([])); //section id to Description Ids
    public descriptionIdsBySectionMap = this._descriptionIdsBySectionMap.asReadonly();

    private _descriptionTemplates = new Map<Guid, DescriptionTemplate>([]); //templateId to template info
    private _planDescriptionTemplates = new Map<Guid, PlanDescriptionTemplate>([]); //planDescriptionTemplateId to plan description template info
    public get planDescriptionTemplates() { return this._planDescriptionTemplates }

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
        if(!sectionDescriptions && sectionId){
            this._descriptionIdsBySectionMap.update((map) => {
                const newMap = new Map(map);
                newMap.set(sectionId, [lastPersist.id]);
                return newMap;
            })
        } else if(!sectionDescriptions.includes(lastPersist.id)) {
            this._descriptionIdsBySectionMap.update((map) => {
                const newMap = new Map(map);
                newMap.get(lastPersist.planDescriptionTemplate?.sectionId)?.push(lastPersist.id);
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
                
        const sectionId = this._descriptions().get(id.toString())?.lastPersist?.planDescriptionTemplate?.sectionId;
        const idx = this._descriptionIdsBySectionMap().get(sectionId)?.findIndex((x) => x === id);
        if(idx >= 0){
            this._descriptionIdsBySectionMap.update((oldMap) => {
                const newMap = new Map(oldMap);
                newMap.get(sectionId)?.splice(idx, 1);
                return newMap;
            })
        }
        this.reCalculateOrdinals(sectionId);

        this._descriptions.update((oldMap) => {
            const newMap = new Map(oldMap);
            newMap.delete(id.toString());
            return newMap;
        })

    }

    public refreshDescriptions(){
        this._descriptions.update((map) => new Map(map));
    }

    private reCalculateOrdinals(sectionId){
        this._descriptionIdsBySectionMap().get(sectionId)?.forEach((id, index) => {
            const description = this.descriptions().get(id.toString());
            this.descriptions().set(id.toString(), {
                ...description,
                ordinal: (index + 1).toString()
            })
        });
    }



    public getDescriptionTemplate(id: Guid): DescriptionTemplate {
        return this._descriptionTemplates.get(id);
    }
    public setDescriptionTemplate(template: DescriptionTemplate) {
        if(!template){return;}
        this._descriptionTemplates.set(template.id, template);
    }
    public clearDescriptionTemplates(){
        this._descriptionTemplates.clear();
    }

    public setPlanDescriptionTemplate(template: PlanDescriptionTemplate) {
        if(!template){return;}
        this._planDescriptionTemplates.set(template.id, template)
    }
    public clearPlanDescriptionTemplates(){
        this._planDescriptionTemplates.clear();
    }

    reset(){
        this._descriptions.set(new Map<string, DescriptionInfo>([]));
        this._descriptionIdsBySectionMap.set(new Map([]));
        this._descriptionTemplates = new Map<Guid, DescriptionTemplate>([]);
        this._planDescriptionTemplates = new Map<Guid, PlanDescriptionTemplate>([]);
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