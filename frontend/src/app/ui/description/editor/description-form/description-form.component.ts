import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
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

@Component({
	selector: 'app-description-form',
	templateUrl: './description-form.component.html',
	styleUrls: ['./description-form.component.scss']
})
export class DescriptionFormComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() propertiesFormGroup: UntypedFormGroup;
	@Input() descriptionTemplate: DescriptionTemplate;
	@Input() visibilityRulesService: VisibilityRulesService;
	@Input() descriptionId: Guid;
	@Input() isNew: boolean = false;
	@Input() canAnnotate: boolean = false;
	@Input() path: string;
	@Input() datasetDescription: String;
	@Input() linkToScroll: LinkToScroll;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() planUsers: PlanUser[] = [];

	@Output() formChanged: EventEmitter<any> = new EventEmitter();

	constructor(
		public formAnnotationService: FormAnnotationService,
	) {
		super();

	}

	ngOnInit() {
		this.init();
	}

	ngOnChanges(changes: SimpleChanges) {
		this.init();
		if (this.descriptionId != null) {
			this.formAnnotationService.init(this.descriptionId, AnnotationEntityType.Description);
		}
	}

	init() {
	}

	onAskedToScroll(panel: MatExpansionPanel, id?: string) {
	}
}
