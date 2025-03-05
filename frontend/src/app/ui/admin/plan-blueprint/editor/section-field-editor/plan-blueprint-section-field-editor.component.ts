import { Component, effect, input, model, output, signal } from '@angular/core';
import { FieldInSectionEditorModel, PlanBlueprintDefinitionSectionEditorModel, PlanBlueprintEditorModel, PlanBlueprintSectionFieldForm } from '../plan-blueprint-editor.model';
import { FormGroup } from '@angular/forms';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanBlueprintExtraFieldDataType } from '@app/core/common/enum/plan-blueprint-field-type';
import { PlanBlueprintSystemFieldType } from '@app/core/common/enum/plan-blueprint-system-field-type';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { SemanticsService } from '@app/core/services/semantic/semantics.service';
import { PlanEditorForm, PlanEditorModel } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { ReferenceType, ReferenceTypeDefinition } from '@app/core/model/reference-type/reference-type';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { map, merge, Subscription, takeUntil } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { ExternalFetcherBaseSourceConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { BaseComponent } from '@common/base/base.component';
import { ReferenceTypeFieldInSectionPersist } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan } from '@app/core/model/plan/plan';
import { Guid } from '@common/types/guid';

@Component({
  selector: 'app-plan-blueprint-field-editor',
  templateUrl: './plan-blueprint-section-field-editor.component.html',
  styleUrl: './plan-blueprint-section-field-editor.component.scss',
  standalone: false
})
export class PlanBlueprintSectionFieldEditorComponent extends BaseComponent{
    static nextId = 0;
    id: string = (PlanBlueprintSectionFieldEditorComponent.nextId++).toString();

    field = input.required<FormGroup<PlanBlueprintSectionFieldForm>>();

    disabledSystemFields = input<Set<PlanBlueprintSystemFieldType>>(new Set([]));
    previewMode = input<boolean>(true);
    reorderingMode = input<boolean>(false);

    referenceTypeMap = model<Map<Guid, ReferenceType>>(new Map([]));

    fieldCategoryChanged = output();
    stepChange = output();
    referenceTypeSelected = output<ReferenceType>();

    planBlueprintFieldCategory = PlanBlueprintFieldCategory;
    planBlueprintFieldCategoryEnum = this.enumUtils.getEnumValues<PlanBlueprintFieldCategory>(PlanBlueprintFieldCategory);
    planBlueprintExtraFieldDataTypeEnum = this.enumUtils.getEnumValues<PlanBlueprintExtraFieldDataType>(PlanBlueprintExtraFieldDataType);
    planBlueprintSystemFieldTypeEnum = this.enumUtils.getEnumValues<PlanBlueprintSystemFieldType>(PlanBlueprintSystemFieldType);

    previewModeForm: FormGroup<PlanEditorForm>;
    private previewModelEditorModel = new PlanEditorModel();

    readonly separatorKeysCodes: number[] = [ENTER, COMMA]; //TODO: doesnt work as separator keys

    private formSub: Subscription;
    constructor(
        protected enumUtils: EnumUtils,
        protected semanticsService: SemanticsService,
        private referenceTypeService: ReferenceTypeService
    ){
        super();
        effect(() => {
            const field = this.field();
            if(!field){ return; }
            if(this.hasBaseValues){
                this.generatePreviewForm(field.value);
            }
            if(this.formSub){
                this.formSub.unsubscribe();
            }
            this.formSub = merge(
                this.field().controls.dataType?.valueChanges,
                this.field().controls.systemFieldType?.valueChanges,
                this.field().controls.referenceTypeId?.valueChanges
            ).pipe(takeUntil(this._destroyed))
            .subscribe((res) => {
                this.generatePreviewForm(this.field().value);
            })
        })
    }

    previewEditorModel = new PlanEditorModel();

    get alwaysRequiredSystemFieldTypes(): PlanBlueprintSystemFieldType[] {
		return FieldInSectionEditorModel.alwaysRequiredSystemFieldTypes;
	}

    isMandatorySystemField(field: FieldInSectionEditorModel): boolean {
		return field != null &&
			field.category == this.planBlueprintFieldCategory.System &&
			this.alwaysRequiredSystemFieldTypes.includes(field.systemFieldType);
	}

    referenceTypeSingleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
        initialItems: (data?: any) => this.referenceTypeService.query({
            ...this.referenceTypeService.buildAutocompleteLookup(),
            project: {
                fields: [
                    ...this.referenceTypeService.buildAutocompleteLookup().project.fields,
                    [nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.referenceTypeDependencies),nameof<ReferenceType>(x => x.id)].join('.'),
                    [nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.referenceTypeDependencies),nameof<ReferenceType>(x => x.name)].join('.'),
                ]
            }
        }).pipe(map(x => x.items)),
        filterFn: (searchQuery: string, data?: any) => this.referenceTypeService.query(this.referenceTypeService.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
        getSelectedItem: (selectedItem: any) => this.referenceTypeService.query(this.referenceTypeService.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
        displayFn: (item: ReferenceType) => item.name,
        titleFn: (item: ReferenceType) => item.name,
        valueAssign: (item: ReferenceType) => {this.referenceTypeMap.update((map) => map.set(item.id, item)); this.referenceTypeSelected.emit(item); return item.id},
    }

    generatePreviewForm(field: any){
        const dummyPlan: Plan = {
            blueprint: {
                ...PlanBlueprintEditorModel.createEmptyBlueprint(),
                definition: {
                    sections: [{
                        ...PlanBlueprintDefinitionSectionEditorModel.createEmptySection(),
                        fields: [{
                            ...field,
                            referenceType: this.referenceTypeMap().get((field as ReferenceTypeFieldInSectionPersist).referenceTypeId)
                        }]
                    }]
                }
            }
        };
        this.previewModeForm = this.previewModelEditorModel.fromModel(dummyPlan).buildForm();
        // console.log(this.previewModeForm);
    }

    get hasBaseValues(): boolean{
        const field = this.field()?.getRawValue();
        return field && field.category != null && (
            (field.category === this.planBlueprintFieldCategory.System && field.systemFieldType != null ) ||
            (field.category === this.planBlueprintFieldCategory.Extra && field.dataType != null ) ||
            (field.category === this.planBlueprintFieldCategory.ReferenceType && field.referenceTypeId != null)
        )
    }
}
