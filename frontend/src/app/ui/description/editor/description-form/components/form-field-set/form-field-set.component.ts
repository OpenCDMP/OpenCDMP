import {computed, effect, input, NgZone, OnInit, ChangeDetectorRef, Component, Inject, Input, OnDestroy, Optional, Signal, ViewContainerRef, ChangeDetectionStrategy} from '@angular/core';
import { FormGroup, UntypedFormArray, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from "@angular/material/dialog";
import { BaseField, BaseFieldSet } from '@app/core/model/description-template/description-template';
import { isFieldSetRequired } from '../../field-required.helper';
import { BaseComponent } from '@common/base/base.component';
import { catchError, switchMap, takeUntil, tap } from 'rxjs/operators';
import { ToCEntry } from '../../../table-of-contents/models/toc-entry';
import { VisibilityRulesService } from '../../visibility-rules/visibility-rules.service';
import { DescriptionEditorForm, PropertyDefinitionEditorModel, PropertyDefinitionFieldSetEditorModel, FieldSetsFormGroup } from '../../../description-editor.model';
import { FormFieldSetEditorDialogComponent } from './dialog-editor/form-fieldset-editor-dialog.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { FormScrollService } from '../services/form-scroll.service';
import { FieldType } from '@app/core/common/enum/field-type';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { FieldValuePipe } from '@app/core/pipes/field-value.pipe';
import { forkJoin, Observable, of, Subscription } from 'rxjs';
import { FieldPersist } from '@app/core/model/description/description';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { PlanAnnotationService } from '@app/ui/plan/plan-editor-blueprint/plan-annotation.service';
import { AnnotationEntityType } from '@app/core/common/enum/annotation-entity-type';
import { PlanEditorForm } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { FormAnnotationService, MULTI_FORM_ANNOTATION_SERVICE_TOKEN } from '@app/ui/annotations/annotation-dialog-component/form-annotation.service';
import { FormFieldSetTitleComponent } from './field-set-title/field-set-title.component';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatBadge } from '@angular/material/badge';
import { FormFieldComponent } from '../form-field/form-field.component';
import { MatError } from '@angular/material/form-field';
import { NgClass } from '@angular/common';

@Component({
    selector: 'app-form-field-set',
    templateUrl: './form-field-set.component.html',
    styleUrls: ['./form-field-set.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [FormFieldSetTitleComponent, MatIconButton, MatTooltip, MatIcon, MatBadge, FormFieldComponent, FormsModule, ReactiveFormsModule, MatError, MatButton, NgClass, TranslatePipe]
})
export class FormFieldSetComponent extends BaseComponent implements OnInit{

	fieldSet = input<BaseFieldSet>();
	propertiesFormGroup = input<FieldSetsFormGroup>();
    @Input() formGroup: FormGroup<PlanEditorForm | DescriptionEditorForm>;

	@Input() descriptionId: Guid;
    @Input() planId: Guid;
	@Input() hideAnnotations: boolean = false;
	@Input() canAnnotate: boolean = false;
	@Input() isNew: boolean = false;

	get isMultiplicityEnabled() {
		return this.fieldSet().hasMultiplicity && this.fieldSet().multiplicity != null;
	}

	isVisibleByVisibilityService: boolean = true;
	@Input() visibilityRulesService: VisibilityRulesService;
	@Input() path: string;
	@Input() descriptionTemplateId: string;
	@Input() isChild: boolean = false;
	@Input() showDelete: boolean = false;
	@Input() tocentry: ToCEntry;
	@Input() tableRow: boolean = false;
	@Input() showTitle: boolean = true;
	@Input() placeholderTitle: boolean = false;
	@Input() validationErrorModel: ValidationErrorModel = new ValidationErrorModel();

	protected annotationsCount!: Signal<number>;
	descriptionTemplateFieldType = FieldType;
	isAnchor: boolean = false;

	/** The heading shows the asterisk when any field of the set is required. */
	get fieldSetRequired(): boolean {
		return isFieldSetRequired(this.fieldSet());
	}

	/**
	 * With a single field under a titled heading, the heading is the field's label: it carries the
	 * asterisk and the field's own label / floating label / placeholder marker must not repeat it.
	 */
	get headingCarriesRequiredMarker(): boolean {
		const fieldSet = this.fieldSet();
		return this.showTitle && !this.isChild && !!fieldSet?.title?.trim() && (fieldSet?.fields?.length ?? 0) === 1;
	}

	hideFieldLabel(field: BaseField): boolean {
		if (!this.headingCarriesRequiredMarker) { return false; }
		const label = field?.data?.label?.trim();
		if (!label) { return true; }
		return label.toLowerCase() === this.fieldSet().title.trim().toLowerCase();
	}

    fieldValueMapArray: Map<string, string>[] = [];

    formChanges: Subscription;

	constructor(
		private routerUtils: RouterUtilsService,
		private dialog: MatDialog,
		private changeDetector: ChangeDetectorRef,
		private viewContainerRef: ViewContainerRef,
		@Optional() @Inject(MULTI_FORM_ANNOTATION_SERVICE_TOKEN) private formAnnotationServices: FormAnnotationService[],
		private descriptionFormService: FormScrollService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
        private fieldValuePipe: FieldValuePipe,
        private logger: LoggingService,
        private planAnnotationService: PlanAnnotationService
	) {
		super();
        effect(() => {
            const fieldSet = this.fieldSet();
            const propertiesFormGroup = this.propertiesFormGroup();
            if(!this.propertiesFormGroup()?.controls?.items?.controls?.length || !this.fieldSet()?.fields?.length){ return; }
            this.initFieldSetValues();
        })
        this.annotationsCount = computed(() =>
            this.planAnnotationService.annotationCountAnchorMap()?.get(this.fieldSet()?.id) ?? 0);
	}

	ngOnInit() {
		this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe( _ => this.changeDetector.detectChanges() );
		// this.formAnnotationService = this.formAnnotationServices?.find(service => service.getEntityId() === this.descriptionId);
        // this.formAnnotationService?.getAnnotationCountObservable().pipe(takeUntil(this._destroyed)).subscribe((annotationsPerAnchor: Map<string, number>) => {
		// 	const newCount = annotationsPerAnchor?.has(this.fieldSet().id) ? annotationsPerAnchor.get(this.fieldSet().id) : 0;
		// 	if (newCount != this.annotationsCount) {
		// 		this.annotationsCount = newCount;
		// 		this.changeDetector.markForCheck();
		// 	}
		// });

		this.descriptionFormService.getScrollingToAnchorObservable().pipe(takeUntil(this._destroyed)).subscribe((anchorFieldsetId: string) => {
			if (anchorFieldsetId && anchorFieldsetId == this.fieldSet().id) {
				this.isAnchor = true;
			}
		});
	}

    initFieldSetValues(index?: number){
        const properties = this.propertiesFormGroup()?.getRawValue()?.items ?? [];
        if(!index){ 
            this.fieldValueMapArray = [];
            forkJoin(
                properties?.map((item) => {
                    return this.buildFieldMap(item.fields);
                })
            ).subscribe({
                next: (res) => {
                    this.fieldValueMapArray = [];
                    res?.forEach((itemMap) => this.fieldValueMapArray.push(itemMap));
                    this.changeDetector.detectChanges();
                },
                error: (error) => this.fieldValueMapArray = []
            })
        }else {
            const fieldValues = properties[index]?.fields;
            if(!fieldValues){ return; }
            this.buildFieldMap(fieldValues)
            .subscribe({
                next: (res) => {
                    try {
                        this.fieldValueMapArray[index] = res;
                        this.changeDetector.detectChanges();
                    } catch (err) {
                        this.logger.error('Could not add field values to row ' + index);
                    }
                },
                error: (error) => this.fieldValueMapArray[index] = null
            })
        }
    }

    buildFieldMap(fieldValues: Record<string, FieldPersist>): Observable<Map<string, string>>{
        const itemMap = new Map<string, string>([]);
        return forkJoin(
            Object.keys(fieldValues)?.map((key) => {
                const field = this.fieldSet().fields.find((x) => x.id === key);
                return this.fieldValuePipe.transform(fieldValues[key], field)
                .pipe(
                    takeUntil(this._destroyed), 
                    catchError(() => of(null)),
                    tap((res) => itemMap.set(key,res))
                )
            })
        ).pipe(switchMap(() => of(itemMap)), catchError((error) => of(new Map<string, string>([]))))
    }

    get properties() {
        return this.propertiesFormGroup()?.controls?.items?.controls;
    }

    showAnnotations(fieldSetId: string){
        this.planAnnotationService.setContext({
            anchor: fieldSetId,
            entityId: this.descriptionId ?? this.planId,
            entityType: this.descriptionId ? AnnotationEntityType.Description : AnnotationEntityType.Plan
        })
        // this.formAnnotationService?.openAnnotationDialog(fieldSetId);
    }

	canAddMultiplicityField(): boolean{
		if (!this.fieldSet().hasMultiplicity) return false;
		if (this.fieldSet()?.multiplicity?.max) return this.fieldSet().multiplicity.max > (this.propertiesFormGroup().get('items') as UntypedFormArray).length;
		return true;
	}

	addMultiplicityField() {
		const formArray = this.propertiesFormGroup()?.get('items') as UntypedFormArray;
		if (formArray.disabled) {
			return;
		}
		const properties = this.propertiesFormGroup().value;// as DescriptionPropertyDefinitionFieldSet;
		let ordinal = 0;
		if (properties?.items && properties.items.map(x => x.ordinal).filter(val => !isNaN(val)).length > 0) {
			ordinal = Math.max(...properties.items.map(x => x.ordinal).filter(val => !isNaN(val))) + 1;
		}
		const item: PropertyDefinitionFieldSetEditorModel = new PropertyDefinitionEditorModel(this.validationErrorModel).calculateFieldSetProperties(this.fieldSet(), ordinal, null, null);
		formArray.push((item.buildForm({ rootPath: `properties.fieldSets[${this.fieldSet().id}].`, visibilityRulesService: this.visibilityRulesService }).get('items') as UntypedFormArray).at(0));
		this.visibilityRulesService.reloadVisibility();
	}

	deleteMultiplicityField(fieldSetIndex: number): void {
        const formArray = this.propertiesFormGroup().get('items') as UntypedFormArray;
		formArray.removeAt(fieldSetIndex);
		formArray.controls.forEach((fieldSet, index) => {
			fieldSet.get('ordinal').setValue(index);
		});

		//Reapply validators
		PropertyDefinitionFieldSetEditorModel.reapplyValidators(
			{
				formArray: formArray,
				validationErrorModel: this.validationErrorModel,
				rootPath: `properties.fieldSets[${this.fieldSet().id}].`,
				fieldSetDefinition: this.fieldSet(),
				visibilityRulesService: this.visibilityRulesService
			}
		);
		formArray.markAsDirty();
		this.visibilityRulesService.reloadVisibility();
        this.fieldValueMapArray.splice(fieldSetIndex, 1);
        this.changeDetector.detectChanges();
	}

	editTableMultiplicityFieldInDialog(fieldSetIndex: number) {
		const dialogRef = this.dialog.open(FormFieldSetEditorDialogComponent, {
			disableClose: true,
            minWidth: 'min(90vw, 750px)',
			viewContainerRef: this.viewContainerRef,
			data: {
				fieldSet: this.fieldSet(),
                formGroup: this.formGroup,
				propertiesFormGroup: (this.propertiesFormGroup()?.get('items') as UntypedFormArray).at(fieldSetIndex),
				numberingText: this.path,
				visibilityRulesService: this.visibilityRulesService
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(data => {
			if (data) {
				(this.propertiesFormGroup()?.get('items') as UntypedFormArray).at(fieldSetIndex).patchValue(data.value);
                this.initFieldSetValues(fieldSetIndex);
				this.visibilityRulesService.reloadVisibility();
				this.changeDetector.detectChanges();
			}
		});
	}

	copyLink(fieldsetId: string) {
		const el = document.createElement('textarea');
		let domain = `${window.location.protocol}//${window.location.hostname}`;
		if (window.location.port && window.location.port != '') domain += `:${window.location.port}`
        let descriptionSectionPath;
        if(this.descriptionId){
            descriptionSectionPath = this.routerUtils.generateUrl(['plans/edit', this.planId.toString(), 'd', this.descriptionId.toString(), 'f', fieldsetId].join('/'));
        }else {
            descriptionSectionPath = this.routerUtils.generateUrl(['plans/edit', this.planId.toString(), 'f', fieldsetId].join('/'));
        }
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
