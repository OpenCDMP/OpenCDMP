import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from "@angular/material/dialog";
import { DescriptionTemplateFieldSet } from '@app/core/model/description-template/description-template';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';
import { ToCEntry } from '../../../table-of-contents/models/toc-entry';
import { VisibilityRulesService } from '../../visibility-rules/visibility-rules.service';
import { DescriptionPropertyDefinitionEditorModel, DescriptionPropertyDefinitionFieldSetEditorModel } from '../../../description-editor.model';
import { FormFieldSetEditorDialogComponent } from './dialog-editor/form-fieldset-editor-dialog.component';
import { cloneAbstractControl } from '@app/utilities/enhancers/utils';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { AnnotationDialogComponent } from '@app/ui/annotations/annotation-dialog-component/annotation-dialog.component';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { FormAnnotationService } from '../../../../../annotations/annotation-dialog-component/form-annotation.service';
import { DescriptionPropertyDefinitionFieldSet } from '@app/core/model/description/description';
import { DescriptionFormService } from '../services/description-form.service';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TranslateService } from '@ngx-translate/core';
import { PlanUser } from '@app/core/model/plan/plan';

@Component({
    selector: 'app-description-form-field-set',
    templateUrl: './form-field-set.component.html',
    styleUrls: ['./form-field-set.component.scss'],
    standalone: false
})
export class DescriptionFormFieldSetComponent extends BaseComponent {

	@Input() fieldSet: DescriptionTemplateFieldSet;
	@Input() propertiesFormGroup: UntypedFormGroup;
	@Input() descriptionId: Guid;
    @Input() planId: Guid;
	@Input() hideAnnotations: boolean = false;
	@Input() canAnnotate: boolean = false;
	@Input() isNew: boolean = false;
	@Input() planUsers: PlanUser[] = [];

	get isMultiplicityEnabled() {
		return this.fieldSet.hasMultiplicity && this.fieldSet.multiplicity != null;
	}

	isVisibleByVisibilityService: boolean = true;
	@Input() visibilityRulesService: VisibilityRulesService;
	@Input() path: String;
	@Input() descriptionTemplateId: String;
	@Input() isChild: Boolean = false;
	@Input() showDelete: Boolean = false;
	@Input() tocentry: ToCEntry;
	@Input() tableRow: boolean = false;
	@Input() showTitle: boolean = true;
	@Input() placeholderTitle: boolean = false;
	@Input() validationErrorModel: ValidationErrorModel = new ValidationErrorModel();

	annotationsCount: number = 0;
	descriptionTemplateFieldType = DescriptionTemplateFieldType;
	isAnchor: boolean = false;

	constructor(
		private routerUtils: RouterUtilsService,
		private dialog: MatDialog,
		private changeDetector: ChangeDetectorRef,
		private formAnnotationService: FormAnnotationService,
		private descriptionFormService: DescriptionFormService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
	) {
		super();
	}

	ngOnInit() {
		this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe( _ => this.changeDetector.detectChanges() );

		this.formAnnotationService.getAnnotationCountObservable().pipe(takeUntil(this._destroyed)).subscribe((annotationsPerAnchor: Map<string, number>) => {
			const newCount = annotationsPerAnchor?.has(this.fieldSet.id) ? annotationsPerAnchor.get(this.fieldSet.id) : 0;
			if (newCount != this.annotationsCount) {
				this.annotationsCount = newCount;
				this.changeDetector.markForCheck();
			}
		});

		this.descriptionFormService.getScrollingToAnchorObservable().pipe(takeUntil(this._destroyed)).subscribe((anchorFieldsetId: string) => {
			if (anchorFieldsetId && anchorFieldsetId == this.fieldSet.id) {
				this.isAnchor = true;
			}
		});
	}

    showAnnotations(fieldSetId: string){
        this.formAnnotationService.οpenAnnotationDialog(fieldSetId);
    }

	canAddMultiplicityField(): boolean{
		if (!this.fieldSet.hasMultiplicity) return false;
		if (this.fieldSet?.multiplicity?.max) return this.fieldSet.multiplicity.max > (this.propertiesFormGroup.get('items') as UntypedFormArray).length;
		return true;
	}

	addMultiplicityField() {
		const formArray = this.propertiesFormGroup?.get('items') as UntypedFormArray;
		if (formArray.disabled) {
			return;
		}
		const properties: DescriptionPropertyDefinitionFieldSet = this.propertiesFormGroup.value;
		let ordinal = 0;
		if (properties?.items && properties.items.map(x => x.ordinal).filter(val => !isNaN(val)).length > 0) {
			ordinal = Math.max(...properties.items.map(x => x.ordinal).filter(val => !isNaN(val))) + 1;
		}
		const item: DescriptionPropertyDefinitionFieldSetEditorModel = new DescriptionPropertyDefinitionEditorModel(this.validationErrorModel).calculateFieldSetProperties(this.fieldSet, ordinal, null, null);
		formArray.push((item.buildForm({ rootPath: `properties.fieldSets[${this.fieldSet.id}].`, visibilityRulesService: this.visibilityRulesService }).get('items') as UntypedFormArray).at(0));
		this.visibilityRulesService.reloadVisibility();
	}

	deleteMultiplicityField(fieldSetIndex: number): void {
		const formArray = this.propertiesFormGroup.get('items') as UntypedFormArray;
		formArray.removeAt(fieldSetIndex);
		formArray.controls.forEach((fieldSet, index) => {
			fieldSet.get('ordinal').setValue(index);
		});

		//Reapply validators
		DescriptionPropertyDefinitionFieldSetEditorModel.reapplyValidators(
			{
				formArray: formArray,
				validationErrorModel: this.validationErrorModel,
				rootPath: `properties.fieldSets[${this.fieldSet.id}].`,
				fieldSetDefinition: this.fieldSet,
				visibilityRulesService: this.visibilityRulesService
			}
		);
		formArray.markAsDirty();
		this.visibilityRulesService.reloadVisibility();

	}

	editTableMultiplicityFieldInDialog(fieldSetIndex: number) {
		const dialogRef = this.dialog.open(FormFieldSetEditorDialogComponent, {
			disableClose: true,
            minWidth: 'min(90vw, 750px)',
			data: {
				fieldSet: this.fieldSet,
				propertiesFormGroup: cloneAbstractControl((this.propertiesFormGroup?.get('items') as UntypedFormArray).at(fieldSetIndex)),
				numberingText: this.path,
				visibilityRulesService: this.visibilityRulesService
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(data => {
			if (data) {
				(this.propertiesFormGroup?.get('items') as UntypedFormArray).at(fieldSetIndex).patchValue(data.value);
				this.changeDetector.detectChanges();
			}
		});
	}

	copyLink(fieldsetId: string) {
		const el = document.createElement('textarea');
		let domain = `${window.location.protocol}//${window.location.hostname}`;
		if (window.location.port && window.location.port != '') domain += `:${window.location.port}`
		const descriptionSectionPath = this.routerUtils.generateUrl(['plans/edit', this.planId.toString(), 'd', this.descriptionId.toString(), 'f', fieldsetId].join('/'));
		el.value = domain + descriptionSectionPath;
		el.setAttribute('readonly', '');
		el.style.position = 'absolute';
		el.style.left = '-9999px';
		document.body.appendChild(el);
		el.select();
		document.execCommand('copy');
		document.body.removeChild(el);
		this.uiNotificationService.snackBarNotification(
			this.language.instant('DESCRIPTION-EDITOR.QUESTION.EXTENDED-DESCRIPTION.COPY-LINK-SUCCESSFUL'), 
			SnackBarNotificationLevel.Success
		);
	}
}
