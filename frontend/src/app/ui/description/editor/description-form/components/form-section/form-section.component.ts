import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { DescriptionTemplateSection } from '@app/core/model/description-template/description-template';
import { PlanUser } from '@app/core/model/plan/plan';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { ToCEntry } from '../../../table-of-contents/models/toc-entry';
import { ToCEntryType } from '../../../table-of-contents/models/toc-entry-type.enum';
import { LinkToScroll } from '../../../table-of-contents/table-of-contents.component';
import { VisibilityRulesService } from '../../visibility-rules/visibility-rules.service';
import { DescriptionFormService } from '../services/description-form.service';
import { takeUntil } from 'rxjs';


@Component({
	selector: 'app-description-form-section',
	templateUrl: './form-section.component.html',
	styleUrls: ['./form-section.component.scss'],
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class DescriptionFormSectionComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() isNew: boolean = false;
	@Input() canAnnotate: boolean = false;
	@Input() section: DescriptionTemplateSection;
	@Input() propertiesFormGroup: UntypedFormGroup;
	@Input() visibilityRulesService: VisibilityRulesService;
	@Input() path: string;
	@Input() descriptionId: Guid;
	@Input() planUsers: PlanUser[] = [];
	@Input() tocentry: ToCEntry;
	@Input() pathName: string;
	@Input() linkToScroll: LinkToScroll;
	@Input() hiddenEntriesIds: string[] = [];
	panelExpanded = true;
	subsectionLinkToScroll: LinkToScroll;
	@Output() askedToScroll = new EventEmitter<string>();
	tocentriesType = ToCEntryType;
	@Input() validationErrorModel: ValidationErrorModel;

	constructor(
		private descriptionFormService: DescriptionFormService,
		private changeDetector: ChangeDetectorRef
	) {
		super();

	}

	ngOnInit() {
		this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe( _ => this.changeDetector.markForCheck() );
	}

	ngOnChanges(changes: SimpleChanges) {

		if (changes['linkToScroll']) {
			if (changes['linkToScroll'].currentValue && changes['linkToScroll'].currentValue.section) {

				if (this.pathName === changes['linkToScroll'].currentValue.section) {
					this.panelExpanded = true;
				} else if (changes['linkToScroll'].currentValue.section.includes(this.pathName)) {
					this.subsectionLinkToScroll = changes['linkToScroll'].currentValue;
					this.panelExpanded = true;
				}
			}
		}
	}

	onAskedToScroll(event: MouseEvent, id: string) {
		event?.stopPropagation();
		this.panelExpanded = true;
		this.askedToScroll.emit(id);
	}
}
