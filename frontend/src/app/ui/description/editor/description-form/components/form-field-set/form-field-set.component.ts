import {effect, input, NgZone, OnInit, ChangeDetectorRef, Component, Inject, Input, OnDestroy, Optional} from '@angular/core';
import { UntypedFormArray } from '@angular/forms';
import { MatDialog } from "@angular/material/dialog";
import { DescriptionTemplateFieldSet } from '@app/core/model/description-template/description-template';
import { BaseComponent } from '@common/base/base.component';
import { catchError, startWith, switchMap, takeUntil, tap } from 'rxjs/operators';
import { ToCEntry } from '../../../table-of-contents/models/toc-entry';
import { VisibilityRulesService } from '../../visibility-rules/visibility-rules.service';
import { DescriptionPropertyDefinitionEditorModel, DescriptionPropertyDefinitionFieldSetEditorModel, FieldSetsFormGroup } from '../../../description-editor.model';
import { FormFieldSetEditorDialogComponent } from './dialog-editor/form-fieldset-editor-dialog.component';
import { cloneAbstractControl } from '@app/utilities/enhancers/utils';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import {
	FormAnnotationService,
	MULTI_FORM_ANNOTATION_SERVICE_TOKEN
} from '../../../../../annotations/annotation-dialog-component/form-annotation.service';
import { DescriptionFormService } from '../services/description-form.service';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { TranslateService } from '@ngx-translate/core';
import { PlanUser } from '@app/core/model/plan/plan';
import { FieldValuePipe } from '@app/core/pipes/field-value.pipe';
import { forkJoin, Observable, of, Subscription } from 'rxjs';
import { DescriptionFieldPersist } from '@app/core/model/description/description';
import { LoggingService } from '@app/core/services/logging/logging-service';

@Component({
    selector: 'app-description-form-field-set',
    templateUrl: './form-field-set.component.html',
    styleUrls: ['./form-field-set.component.scss'],
    standalone: false
})
export class DescriptionFormFieldSetComponent extends BaseComponent implements OnInit{

	fieldSet = input<DescriptionTemplateFieldSet>();
	propertiesFormGroup = input<FieldSetsFormGroup>();

	@Input() descriptionId: Guid;
    @Input() planId: Guid;
	@Input() hideAnnotations: boolean = false;
	@Input() canAnnotate: boolean = false;
	@Input() isNew: boolean = false;
	@Input() planUsers: PlanUser[] = [];

	get isMultiplicityEnabled() {
		return this.fieldSet().hasMultiplicity && this.fieldSet().multiplicity != null;
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

    fieldValueMapArray: Map<string, string>[] = [];

    formChanges: Subscription;

	private formAnnotationService: FormAnnotationService;
	constructor(
		private routerUtils: RouterUtilsService,
		private dialog: MatDialog,
		private changeDetector: ChangeDetectorRef,
		@Optional() @Inject(MULTI_FORM_ANNOTATION_SERVICE_TOKEN) private formAnnotationServices: FormAnnotationService[],
		private descriptionFormService: DescriptionFormService,
		private uiNotificationService: UiNotificationService,
		private language: TranslateService,
        private fieldValuePipe: FieldValuePipe,
        private logger: LoggingService
	) {
		super();
        effect(() => {
            const fieldSet = this.fieldSet();
            const propertiesFormGroup = this.propertiesFormGroup();
            if(!this.propertiesFormGroup()?.controls?.items?.controls?.length || !this.fieldSet()?.fields?.length){ return; }
            this.initFieldSetValues();
        })
	}

	ngOnInit() {
		this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe( _ => this.changeDetector.detectChanges() );
		this.formAnnotationService = this.formAnnotationServices?.find(service => service.getEntityId() === this.descriptionId);
        this.formAnnotationService?.getAnnotationCountObservable().pipe(takeUntil(this._destroyed)).subscribe((annotationsPerAnchor: Map<string, number>) => {
			const newCount = annotationsPerAnchor?.has(this.fieldSet().id) ? annotationsPerAnchor.get(this.fieldSet().id) : 0;
			if (newCount != this.annotationsCount) {
				this.annotationsCount = newCount;
				this.changeDetector.markForCheck();
			}
		});

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

    buildFieldMap(fieldValues: Record<string, DescriptionFieldPersist>): Observable<Map<string, string>>{
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
        this.formAnnotationService?.οpenAnnotationDialog(fieldSetId);
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
		const item: DescriptionPropertyDefinitionFieldSetEditorModel = new DescriptionPropertyDefinitionEditorModel(this.validationErrorModel).calculateFieldSetProperties(this.fieldSet(), ordinal, null, null);
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
		DescriptionPropertyDefinitionFieldSetEditorModel.reapplyValidators(
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
			data: {
				fieldSet: this.fieldSet(),
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
