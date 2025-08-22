import { Component, computed, Input, input, model, output, QueryList, ViewChildren } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { PlanBlueprintForm } from '../plan-blueprint-editor.model';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { CdkDragDrop, CdkDropList } from '@angular/cdk/drag-drop';
import { Guid } from '@common/types/guid';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { startWith, takeUntil } from 'rxjs';
import { BaseComponent } from '@common/base/base.component';

@Component({
  selector: 'app-plan-blueprint-toc',
  templateUrl: './plan-blueprint-toc.component.html',
  styleUrl: './plan-blueprint-toc.component.scss',
  standalone: false
})
export class PlanBlueprintTocComponent extends BaseComponent{
    @ViewChildren('fieldList') dragAndDropQueryList: QueryList<CdkDropList>;
    dragAndDropListArray: CdkDropList[] = [];

    private _blueprintFormGroup: FormGroup<PlanBlueprintForm>;
    @Input() set blueprint(val: FormGroup<PlanBlueprintForm>){
        if(val){
            this._blueprintFormGroup = val;
        }
    }
    get blueprint(): FormGroup<PlanBlueprintForm> {
        return this._blueprintFormGroup;
    }
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
    fieldsDropped = output<{
        previous: {
            sectionIndex: number,
            index: number
        },
        current: {
            sectionIndex: number,
            index: number
        },
    }>();

    get sections(){
        return this.blueprint?.controls?.definition?.controls?.sections
    }

    planBlueprintSectionFieldCategoryEnum = PlanBlueprintFieldCategory;

    cdkDropListConnectedTo(id: string): any[] {
        return this.dragAndDropListArray.filter((x) => x.id !== id);
    }

    constructor(protected enumUtils: EnumUtils){
        super();
    }

    ngAfterViewInit(){
        this.dragAndDropQueryList.changes.pipe(takeUntil(this._destroyed), startWith(this.dragAndDropQueryList))
        .subscribe((list) => {
            setTimeout(() => this.dragAndDropListArray = list?.toArray() ?? []);
        })
    }
    
    generateFieldListId(index: string): string{
        return index + '-fields';
    }
    parseFieldListIndex(id: string): number {
        return id ? Number(id.split('-')?.[0]) : null;
    }

    dropFields(event: CdkDragDrop<string[]>){
        let previousListIndex = this.parseFieldListIndex(event.previousContainer?.id);
        let currentListIndex = this.parseFieldListIndex(event.container?.id);
        this.fieldsDropped.emit({
            previous: {
                sectionIndex: previousListIndex,
                index: event.previousIndex
            },
            current: {
                sectionIndex: currentListIndex,
                index: event.currentIndex
            }
        })
    }

}
