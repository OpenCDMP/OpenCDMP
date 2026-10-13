import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, OnChanges, OnInit, Output, SimpleChanges, computed, effect, input } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BaseField, BaseFieldSet, BaseSection } from '@app/core/model/description-template/description-template';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { ToCEntry } from '../../../table-of-contents/models/toc-entry';
import { ToCEntryType } from '../../../table-of-contents/models/toc-entry-type.enum';
import { LinkToScroll } from '../../../table-of-contents/table-of-contents.component';
import { VisibilityRulesService } from '../../visibility-rules/visibility-rules.service';
import { sectionHasRequiredFields } from '../../field-required.helper';
import { FormScrollService } from '../services/form-scroll.service';
import { takeUntil } from 'rxjs';
import { DescriptionEditorForm, PropertiesFormGroup } from '../../../description-editor.model';
import { PlanEditorForm } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { FieldType } from '@app/core/common/enum/field-type';
import { TemplateFieldEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { FormFieldSetComponent } from '../form-field-set/form-field-set.component';
import { TranslatePipe } from '@ngx-translate/core';


@Component({
    selector: 'app-form-section',
    templateUrl: './form-section.component.html',
    styleUrls: ['./form-section.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [FormFieldSetComponent, FormsModule, ReactiveFormsModule, TranslatePipe]
})
export class FormSectionComponent extends BaseComponent implements OnInit, OnChanges {

	isNew = input<boolean>(false);
    isChild = input<boolean>(false);
	canAnnotate = input<boolean>(false);
	canEdit = input<boolean>(false);
	section = input<BaseSection>();
	propertiesFormGroup = input<FormGroup<PropertiesFormGroup>>();
	formGroup = input<FormGroup<PlanEditorForm> | FormGroup<DescriptionEditorForm>>();
	visibilityRulesService = input<VisibilityRulesService>();
	path = input<string>();
	descriptionId = input<Guid>();
	planId = input<Guid>();
	tocentry = input<ToCEntry>();
	pathName = input<string>();
	linkToScroll = input<LinkToScroll>();
	hiddenEntriesIds = input<string[]>([]);
	subsectionLinkToScroll: LinkToScroll;
	@Output() askedToScroll = new EventEmitter<string>();
	tocentriesType = ToCEntryType;
	validationErrorModel = input<ValidationErrorModel>();
	/** Drives the "Fields marked with * are mandatory" legend under the section title. */
	hasRequiredFields = computed(() => sectionHasRequiredFields(this.section()));

	constructor(
		private descriptionFormService: FormScrollService,
		private changeDetector: ChangeDetectorRef
	) {
		super();
        effect(() => {
            if(!this.canEdit()){
                this.disableFields(this.section())
            }else {
                this.enableFields(this.section())
            }
        })
	}

	ngOnInit() {
		this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe( _ => this.changeDetector.markForCheck() );
	}

	ngOnChanges(changes: SimpleChanges) {
        if (changes['linkToScroll']?.currentValue?.section?.includes(this.pathName())) {
            this.subsectionLinkToScroll = changes['linkToScroll'].currentValue;
        }
	}

	onAskedToScroll(event: MouseEvent, id: string) {
		event?.stopPropagation();
		this.askedToScroll.emit(id);
	}

    getFieldSets(section: BaseSection): BaseFieldSet[] {
        const res = [];
        if(section.sections?.length){
            section.sections.forEach((subSection) => res.push(...this.getFieldSets(subSection)));
        }
        if(section.fieldSets?.length){
            section.fieldSets.forEach((fieldSet) => {
                res.push(fieldSet)
            })
        }
        return res;
    }

    disableFields(section: BaseSection) {
        const sectionFieldSets = this.getFieldSets(section);
        const sectionFields: BaseField[] = [];

        sectionFieldSets?.forEach((fieldSet) => {
            sectionFields.push(...(fieldSet.fields ?? []));
            this.propertiesFormGroup().controls.fieldSets.get(fieldSet.id).disable({emitEvent: false});
        })
        const systemFields = sectionFields?.filter(x => x.data?.fieldType == FieldType.SYSTEM);
        if(!systemFields?.length){return}
        systemFields.forEach((field) => 
            TemplateFieldEditorModel.getSystemFieldControl(field, this.formGroup())?.disable({emitEvent: false})
        )
    }
    enableFields(section: BaseSection) {
        const sectionFieldSets = this.getFieldSets(section);
        const sectionFields: BaseField[] = [];

        sectionFieldSets?.forEach((fieldSet) => {
            sectionFields.push(...(fieldSet.fields ?? []));
            this.propertiesFormGroup().get('fieldSets').get(fieldSet.id).enable({emitEvent: false});
        })
        const systemFields = sectionFields?.filter(x => x.data?.fieldType == FieldType.SYSTEM);
        if(!systemFields?.length){return}
        systemFields.forEach((field) => 
            TemplateFieldEditorModel.getSystemFieldControl(field, this.formGroup())?.enable({emitEvent: false})
        )
    }
}
