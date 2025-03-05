import { booleanAttribute, Component, computed, effect, EventEmitter, Injector, input, Input, Output } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatExpansionPanel } from '@angular/material/expansion';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanUser } from '@app/core/model/plan/plan';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { FormAnnotationService } from '../../../annotations/annotation-dialog-component/form-annotation.service';
import { LinkToScroll } from '../table-of-contents/table-of-contents.component';
import { VisibilityRulesService } from './visibility-rules/visibility-rules.service';
import { PlanTempStorageService } from '@app/ui/plan/plan-editor-blueprint/plan-temp-storage.service';

@Component({
    selector: 'app-description-form',
    templateUrl: './description-form.component.html',
    styleUrls: ['./description-form.component.scss'],
    standalone: false
})
export class DescriptionFormComponent extends BaseComponent {
    descriptionId = input<Guid>(null);
    planId = input<Guid>(null);

	@Input() canAnnotate: boolean = false;
	@Input() datasetDescription: String;
	@Input() linkToScroll: LinkToScroll;
	@Input() planUsers: PlanUser[] = [];
    @Input() ordinal: string;
    @Input() propertiesFormGroup: UntypedFormGroup;
	@Input() descriptionTemplate: DescriptionTemplate;
	@Input() visibilityRulesService: VisibilityRulesService;
    @Input() validationErrorModel: ValidationErrorModel;
	@Input() isNew: boolean = false;


    // selectedEntry = input<ToCEntry>(null);

    @Output() formChanged: EventEmitter<any> = new EventEmitter();

	constructor(
		public formAnnotationService: FormAnnotationService,
	) {
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
