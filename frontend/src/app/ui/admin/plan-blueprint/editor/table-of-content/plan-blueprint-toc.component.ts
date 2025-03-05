import { Component, computed, Input, input, model, output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { PlanBlueprintForm } from '../plan-blueprint-editor.model';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Guid } from '@common/types/guid';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';

@Component({
  selector: 'app-plan-blueprint-toc',
  templateUrl: './plan-blueprint-toc.component.html',
  styleUrl: './plan-blueprint-toc.component.scss',
  standalone: false
})
export class PlanBlueprintTocComponent {
    
    @Input() blueprint: FormGroup<PlanBlueprintForm>;
    hideEditActions = input<boolean>(false);
    selectedSection = input<number>();
    selectedField = input<number>();
    referenceTypeMap = input<Map<Guid, ReferenceType>>(new Map([]));

    addSection = output();
    removeSection = output<number>();
    addField = output<number>();
    removeField = output<{section: number, field: number}>();
    stepChange = output<{section: number, field: number}>();
    sectionsDropped = output<CdkDragDrop<string[]>>();
    fieldsDropped = output<{event: CdkDragDrop<string[]>, sectionIndex: number}>();

    get sections(){
        return this.blueprint?.controls?.definition?.controls?.sections
    }

    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;

    constructor(protected enumUtils: EnumUtils){
    }

}
