import {Component, EventEmitter, input, Input, Output, ChangeDetectionStrategy} from '@angular/core';
import { FormGroup, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatExpansionPanel} from '@angular/material/expansion';
import {DescriptionTemplate} from '@app/core/model/description-template/description-template';
import {PlanUser} from '@app/core/model/plan/plan';
import {BaseComponent} from '@common/base/base.component';
import {ValidationErrorModel} from '@common/forms/validation/error-model/validation-error-model';
import {Guid} from '@common/types/guid';
import {LinkToScroll} from '../table-of-contents/table-of-contents.component';
import {VisibilityRulesService} from './visibility-rules/visibility-rules.service';
import { PlanEditorForm } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { DescriptionEditorForm } from '../description-editor.model';
import { FormSectionComponent } from './components/form-section/form-section.component';

@Component({
    selector: 'app-description-form',
    templateUrl: './description-form.component.html',
    styleUrls: ['./description-form.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormSectionComponent, FormsModule, ReactiveFormsModule]
})
export class DescriptionFormComponent extends BaseComponent {
    descriptionId = input<Guid>(null);
    planId = input<Guid>(null);

	@Input() canAnnotate: boolean = false;
	@Input() canEdit: boolean = false;
	@Input() datasetDescription: string;
	@Input() linkToScroll: LinkToScroll;
	@Input() planUsers: PlanUser[] = [];
    @Input() ordinal: string;
    @Input() propertiesFormGroup: UntypedFormGroup;
    @Input() formGroup: FormGroup<PlanEditorForm | DescriptionEditorForm>;
	@Input() descriptionTemplate: DescriptionTemplate;
	@Input() visibilityRulesService: VisibilityRulesService;
    @Input() validationErrorModel: ValidationErrorModel;
	@Input() isNew: boolean = false;


    // selectedEntry = input<ToCEntry>(null);

    @Output() formChanged: EventEmitter<any> = new EventEmitter();

	constructor() {
		super();
	}

	onAskedToScroll(panel: MatExpansionPanel, id?: string) {
	}

    // selectedPage(pageId: string): boolean{
    //     return this.selectedEntry()?.id === pageId || (this.selectedEntry()?.pathToEntry?.length && this.selectedEntry().pathToEntry[0] === pageId)
    // }

    // public toggleExpand(selected: string){
    //     if(!selected){return;}
    //     const index = selected?.split('.');
    //     if(index?.length) {
    //         this.expansionPanels?.toArray()?.[Number(index[0])-1]?.open();
    //         if(index.length > 1){
    //             const sectionPath = index[0] + '.' + index[1];
    //             this.formSections?.find((component) => component.path === sectionPath)?.toggleExpand();
    //         }
    //     }
    // }
}
