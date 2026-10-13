
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Optional, Output, computed, effect, signal } from '@angular/core';
import { PlanReviewService } from '@app/ui/plan/plan-editor-blueprint/plan-review.service';
import { DescriptionContextService } from '@app/ui/plan/plan-editor-blueprint/plan-description-editor/description-context.service';
import { FieldChange, FieldValuePreview } from '@app/core/model/plan-update-request/plan-update-preview';

function isFieldValuePreview(v: unknown): v is FieldValuePreview {
    if (v == null || typeof v !== 'object' || Array.isArray(v)) return false;
    const keys = Object.keys(v as object);
    // An empty object {} is a valid "clear" signal from the backend (incomingValue with no value).
    if (keys.length === 0) return true;
    return keys.some(k => ['textValue', 'textListValue', 'dateValue', 'booleanValue', 'numberValue', 'reference', 'references', 'tags', 'externalIdentifier'].includes(k));
}
import { FormArray, FormGroup, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { PlanContactEditorModel } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { MatDialog } from "@angular/material/dialog";
import { FieldType } from '@app/core/common/enum/field-type';
import { BaseField, BaseFieldSet, FieldLabelAndMultiplicityData, FieldSystemData, FieldUploadData } from '@app/core/model/description-template/description-template';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { TagService } from '@app/core/services/tag/tag.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { isFieldRequired } from '@app/ui/description/editor/description-form/field-required.helper';
import { BaseComponent } from '@common/base/base.component';
import { Observable } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { LanguageInfo } from '@app/core/model/language-info';
import { LanguageInfoService } from '@app/core/services/culture/language-info-service';
import { DescriptionEditorForm, FieldFormGroup } from '../../../description-editor.model';
import { PlanEditorForm } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { NgTemplateOutlet } from '@angular/common';
import { MaterialModule } from '@common/material/material.module';
import { TranslateModule } from '@ngx-translate/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { FormFocusDirective } from '@common/forms/form-focus.directive';
import { TagsFieldModule } from '@app/ui/tag/tags-field/tags-field.module';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { ReferenceFieldModule } from '@app/ui/reference/reference-field/reference-field.module';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { PlanContactModule } from '@app/ui/plan/plan-contact/plan-contact.module';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { FieldChangeBadgeComponent } from '../../../../../plan/plan-editor-blueprint/field-change-badge/field-change-badge.component';

@Component({
    selector: 'app-form-field',
    templateUrl: './form-field.component.html',
    styleUrls: ['./form-field.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
    MaterialModule,
    TranslateModule,
    CommonFormsModule,
    FormFocusDirective,
    AutoCompleteModule,
    TagsFieldModule,
    FileUploadComponent,
    ReferenceFieldModule,
    RichTextEditorModule,
    PlanContactModule,
    FieldChangeBadgeComponent,
    NgTemplateOutlet
]
})
export class FormFieldComponent extends BaseComponent implements OnInit {

	@Input() set field(value: BaseField) {
		this._field.set(value);
	}
	get field(): BaseField { return this._field(); }
	private readonly _field = signal<BaseField | null>(null);

	@Input() set fieldSet(value: BaseFieldSet) {
		this._fieldSet.set(value);
	}
	get fieldSet(): BaseFieldSet { return this._fieldSet(); }
	private readonly _fieldSet = signal<BaseFieldSet | null>(null);

	@Input() set propertiesFormGroup(value: FieldFormGroup) {
		this._propertiesFormGroup.set(value);
	}
	get propertiesFormGroup(): FieldFormGroup { return this._propertiesFormGroup(); }
	private readonly _propertiesFormGroup = signal<FieldFormGroup | null>(null);

    @Input() formGroup: FormGroup<PlanEditorForm> | FormGroup<DescriptionEditorForm>;
	@Input() visibilityRulesService: VisibilityRulesService;

	@Input() descriptionTemplateId: any;
	@Input() isChild: boolean = false;
	/** The field-set heading already names this field, so don't render the field's own label under it. */
	@Input() hideLabel: boolean = false;
	/** The field-set heading already carries the required asterisk, so the control must not repeat it. */
	@Input() hideRequiredMarker: boolean = false;

	@Input() detectChangesObservable: Observable<any>;
    @Input() path: string;

    @Output() reApplyPropertiesValidators = new EventEmitter<void>();

	visible: boolean = true;

	FieldTypeEnum = FieldType;
    SystemFieldTypeEnum = SystemFieldType;
    planAccessTypeEnumValues = this.enumUtils.getEnumValues<PlanAccessType>(PlanAccessType);

	readonly fieldChange = computed<FieldChange | null>(() => {
		if (!this.planReviewService) return null;
		const field = this._field();
		if (!field?.id) return null;

		// Plan context: no descriptionContextService
		if (!this.descriptionContextService) {
			return this.planFieldChange(field);
		}

		// Description context
		const descriptionId = this.descriptionContextService.descriptionId();
		if (!descriptionId) return null;

		// Description template system fields use static keys, not fieldSet-qualified ids
		if (field.data?.fieldType === FieldType.SYSTEM) {
			const systemType = (field.data as FieldSystemData).systemFieldType;
			let staticFieldId: string | null = null;
			if (systemType === SystemFieldType.PlanDescriptionTitle) staticFieldId = 'system.label';
			else if (systemType === SystemFieldType.PlanDescriptionDescription) staticFieldId = 'system.description';
			else if (systemType === SystemFieldType.PlanDescriptionTag) staticFieldId = 'system.tags';
			if (staticFieldId) {
				return this.planReviewService.descriptionFieldChangeByKey().get(`${descriptionId.toString()}:${staticFieldId}`) ?? null;
			}
			return null;
		}

		const fieldSet = this._fieldSet();
		const propsGroup = this._propertiesFormGroup();
		if (!propsGroup) return null;

		// fieldSet may have been bound as a signal-getter rather than its value (existing
		// template quirk). Read .id defensively.
		const rawFieldSet: any = fieldSet;
		const fieldSetId: string | undefined = typeof rawFieldSet === 'function'
			? rawFieldSet()?.id
			: rawFieldSet?.id;
		if (!fieldSetId) return null;

		// propertiesFormGroup is the "fields" group of an item; its parent is the item
		// group which holds the ordinal.
		const ordinal = propsGroup.parent?.get?.('ordinal')?.value;
		if (ordinal == null) return null;

		const qualifiedId = `${fieldSetId}[${ordinal}].${field.id}`;
		const key = `${descriptionId.toString()}:${qualifiedId}`;
		return this.planReviewService.descriptionFieldChangeByKey().get(key) ?? null;
	});

	private planFieldChange(field: BaseField): FieldChange | null {
		if (!this.planReviewService) return null;
		const fieldData = field.data;
		if (fieldData?.fieldType === FieldType.SYSTEM) {
			// Plan system fields: backend uses the blueprint field entity UUID as fieldId
			return this.planReviewService.planFieldChangeByFieldId().get(field.id.toString()) ?? null;
		}
		// Plan blueprint extra/reference fields: backend uses fieldSetId[ordinal].fieldId
		const fieldSet = this._fieldSet();
		const propsGroup = this._propertiesFormGroup();
		if (!propsGroup) return null;
		const rawFieldSet: any = fieldSet;
		const fieldSetId: string | undefined = typeof rawFieldSet === 'function'
			? rawFieldSet()?.id
			: rawFieldSet?.id;
		if (!fieldSetId) return null;
		const ordinal = propsGroup.parent?.get?.('ordinal')?.value;
		if (ordinal == null) return null;
		const qualifiedId = `${fieldSetId}[${ordinal}].${field.id}`;
		return this.planReviewService.planFieldChangeByFieldId().get(qualifiedId) ?? null;
	}

	private readonly registerLabelEffect = effect(() => {
		if (!this.planReviewService) return;
		const change = this.fieldChange();
		if (!change?.changeId) return;
		const field = this._field();
		this.planReviewService.registerDisplayMeta(change.changeId, {
			ordinal: this.path ?? null,
			label: field?.data?.label ?? null,
			containerLabel: this.descriptionContextService?.descriptionLabel() ?? (this.formGroup as FormGroup<PlanEditorForm>)?.get('label')?.value ?? null,
		});
	}, { allowSignalWrites: true });


	protected singleAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	protected multipleAutoCompleteConfiguration: MultipleAutoCompleteConfiguration;
	protected tagsAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	protected multipleReferenceAutoCompleteConfiguration: MultipleAutoCompleteConfiguration;

	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	protected validationIcon: string;

	readonly datasetIdTypes: any[] = [
		{ name: 'Handle', value: 'handle' },
		{ name: 'DOI', value: 'doi' },
		{ name: 'Ark', value: 'ark' },
		{ name: 'Url', value: 'url' },
		{ name: 'Other', value: 'other' }
	];


	readonly validationTypes: any[] = [
		{ name: 'Zenodo', value: 'zenodo' }
	];

	protected initFile: StorageFile = null;

	/**
	 * Resolves the form control (or form group for structured types) inside this field's
	 * properties group that holds the value the backend's preview diff targets.
	 */
	private resolveAnchorControl(): { control: any; controlName: string; mode: 'scalar' | 'date' | 'list' | 'boolean' | 'reference' | 'references' | 'externalIdentifier' | 'tags' | 'contacts' | 'number' | 'unsupported' } {
		const f = this._field();
		const group = this._propertiesFormGroup();
		if (!f?.id) return { control: null, controlName: null, mode: 'unsupported' };

		// System fields bind to top-level controls on the root formGroup.
		// Cast to UntypedFormGroup to avoid TS2349 on the union of strongly-typed FormGroups.
		if (f.data?.fieldType === FieldType.SYSTEM) {
			const systemType = (f.data as FieldSystemData).systemFieldType;
			const fg = this.formGroup as UntypedFormGroup | undefined;
			switch (systemType) {
				// Description template system fields — bind to description form controls
				case SystemFieldType.PlanDescriptionTitle:
					return { control: fg?.get('label'), controlName: 'label', mode: 'scalar' };
				case SystemFieldType.PlanDescriptionDescription:
					return { control: fg?.get('description'), controlName: 'description', mode: 'scalar' };
				case SystemFieldType.PlanDescriptionTag: {
					const tagsControl = fg?.get('tags');
					// Backend serializes description tags as textListValue, so use 'list' mode
					return tagsControl
						? { control: tagsControl, controlName: 'tags', mode: 'list' }
						: { control: null, controlName: null, mode: 'unsupported' };
				}
				// Plan blueprint system fields — bind to plan form controls
				case SystemFieldType.PlanTitle:
					return { control: fg?.get('label'), controlName: 'label', mode: 'scalar' };
				case SystemFieldType.PlanDescription:
					return { control: fg?.get('description'), controlName: 'description', mode: 'scalar' };
				case SystemFieldType.PlanLanguage:
					return { control: fg?.get('language'), controlName: 'language', mode: 'scalar' };
				case SystemFieldType.PlanAccessRights:
					return { control: fg?.get('accessType'), controlName: 'accessType', mode: 'scalar' };
				case SystemFieldType.PlanContact:
					return { control: fg?.get('properties')?.get('contacts') ?? null, controlName: 'contacts', mode: 'contacts' };
				default:
					return { control: null, controlName: null, mode: 'unsupported' };
			}
		}

		if (!group) return { control: null, controlName: null, mode: 'unsupported' };
		const valueGroup = group.get(f.id);
		if (!valueGroup) return { control: null, controlName: null, mode: 'unsupported' };
		const type = f.data?.fieldType;
		switch (type) {
			case FieldType.FREE_TEXT:
			case FieldType.TEXT_AREA:
			case FieldType.RICH_TEXT_AREA:
			case FieldType.UPLOAD:
				return { control: valueGroup.get('textValue'), controlName: 'textValue', mode: 'scalar' };
			case FieldType.BOOLEAN_DECISION:
			case FieldType.CHECK_BOX:
				return { control: valueGroup.get('booleanValue'), controlName: 'booleanValue', mode: 'boolean' };
			case FieldType.RADIO_BOX:
			case FieldType.SELECT: {
				const isMulti = !!(f.data as any)?.multipleSelect;
				return isMulti
					? { control: valueGroup.get('textListValue'), controlName: 'textListValue', mode: 'list' }
					: { control: valueGroup.get('textValue'), controlName: 'textValue', mode: 'scalar' };
			}
			case FieldType.INTERNAL_ENTRIES_PLANS:
			case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS: {
				const isMulti = !!(f.data as any)?.multipleSelect;
				return isMulti
					? { control: valueGroup.get('textListValue'), controlName: 'textListValue', mode: 'list' }
					: { control: valueGroup.get('textValue'), controlName: 'textValue', mode: 'scalar' };
			}
			case FieldType.DATE_PICKER:
				return { control: valueGroup.get('dateValue'), controlName: 'dateValue', mode: 'date' };
			case FieldType.TAGS:
				return { control: valueGroup.get('tags'), controlName: 'tags', mode: 'tags' };
			case FieldType.REFERENCE_TYPES: {
				const isMulti = !!(f.data as any)?.multipleSelect;
				return isMulti
					? { control: valueGroup.get('references'), controlName: 'references', mode: 'references' }
					: { control: valueGroup.get('reference'), controlName: 'reference', mode: 'reference' };
			}
			case FieldType.DATASET_IDENTIFIER:
			case FieldType.VALIDATION:
				return { control: valueGroup.get('externalIdentifier'), controlName: 'externalIdentifier', mode: 'externalIdentifier' };
			case FieldType.NUMBER:
				return { control: valueGroup.get('numberValue'), controlName: 'numberValue', mode: 'number' };
			default:
				return { control: null, controlName: null, mode: 'unsupported' };
		}
	}

	readFieldValue = (): unknown => {
		const { control } = this.resolveAnchorControl();
		return control?.value ?? null;
	};

	writeFieldValue = (value: unknown): void => {
		const { control, mode } = this.resolveAnchorControl();
		if (!control) return;

		if (mode === 'contacts') {
			const contactsArray = control as FormArray;
			let contacts: Array<{ firstName: string; lastName: string; email: string }> = [];
			if (isFieldValuePreview(value) && value.textValue) {
				try { contacts = JSON.parse(value.textValue); } catch { contacts = []; }
			} else if (Array.isArray(value)) {
				contacts = value;
			}
			const validationErrorModel = new ValidationErrorModel();
			while (contactsArray.length > 0) contactsArray.removeAt(0);
			contacts.forEach((c, i) => {
				const model = new PlanContactEditorModel(validationErrorModel);
				model.firstName = c.firstName;
				model.lastName = c.lastName;
				model.email = c.email;
				contactsArray.push(model.buildForm({ rootPath: `properties.contacts[${i}].` }));
			});
			contactsArray.markAsDirty();
			return;
		}

		let normalized: any = null;

		if (isFieldValuePreview(value)) {
			// Incoming value from backend preview — extract the typed sub-value.
			switch (mode) {
				case 'scalar':
					// Single-select SELECT and single INTERNAL_ENTRIES fields are stored by the
					// backend as textListValue (FieldType.isTextListType includes SELECT), so fall
					// back to textListValue[0] when textValue is absent.
					normalized = value.textValue ?? (value.textListValue?.length ? value.textListValue[0] : null);
					break;
				case 'boolean':
					normalized = value.booleanValue ?? null;
					break;
				case 'number':
					normalized = value.numberValue ?? null;
					break;
				case 'date': {
					const raw = value.dateValue;
					if (raw != null) {
						const parsed = new Date(raw);
						normalized = isNaN(parsed.getTime()) ? null : parsed;
					}
					break;
				}
				case 'list':
					normalized = value.textListValue ?? null;
					break;
				case 'tags':
					normalized = value.tags ?? null;
					break;
				case 'reference':
					normalized = value.reference ?? null;
					break;
				case 'references':
					normalized = value.references ?? null;
					break;
				case 'externalIdentifier':
					normalized = value.externalIdentifier ?? null;
					break;
				default:
					return;
			}
		} else {
			// Restored captured value (raw scalar/object from readFieldValue).
			normalized = value ?? null;
		}

		control.patchValue(normalized);
		control.markAsDirty();
	};

	constructor(
		protected planService: PlanService,
		protected descriptionService: DescriptionService,
		protected tagService: TagService,
		protected dialog: MatDialog,
        protected enumUtils: EnumUtils,
		// private descriptionFormService: FormScrollService,
        //private changeDetectorRef: ChangeDetectorRef,
        private languageInfoService: LanguageInfoService,
        @Optional() public planReviewService: PlanReviewService | null,
        @Optional() private descriptionContextService: DescriptionContextService | null,
	) {
		super();
	}

	ngOnInit() {
		// if (this.field?.data?.fieldType == DescriptionTemplateFieldType.UPLOAD && this.field && this.field.id && (this.propertiesFormGroup?.get(this.field.id).get('textValue').value != undefined) && !this.fileNameDisplay) {
		// 	const id = Guid.parse((this.propertiesFormGroup?.get(this.field.id).get('textValue').value as string));

		// 	const fields = [
		// 		nameof<StorageFile>(x => x.id),
		// 		nameof<StorageFile>(x => x.name),
		// 		nameof<StorageFile>(x => x.extension),
		// 	]
		// 	this.storageFileService.getSingle(id, fields).pipe(takeUntil(this._destroyed)).subscribe(storageFile => {
		// 		this.initFile = storageFile;
		// 		this.applyFieldType();
		// 	});
		// } else {
		// 	this.applyFieldType();
		// }
        this.applyFieldType();
		// this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe(x => this.changeDetectorRef.markForCheck());
	}

    get isRequired(): boolean {
        return isFieldRequired(this.field);
    }

	private applyFieldType() {

		this.propertiesFormGroup.get(this.field.id)?.valueChanges
			.pipe(
				takeUntil(this._destroyed),
				distinctUntilChanged()
			)
			.subscribe(item => {
				this.visibilityRulesService.updateVisibilityForSource(this.field?.id);
				// this.descriptionFormService.detectChanges(true);
			});
	}

	makeAutocompleteConfiguration(myfunc: (...args: any[]) => any, title: string, subtitle?: string): void {
		if (!((this.field.data as FieldLabelAndMultiplicityData).multipleSelect)) {
			this.singleAutoCompleteConfiguration = {
				filterFn: myfunc.bind(this),
				initialItems: (extraData) => myfunc(''),
				displayFn: (item) => { try { return (item != null && item.length > 1) ? JSON.parse(item)[title] : item[title] } catch { return '' } },
				titleFn: (item) => { try { return item[title] } catch { return '' } },
				valueAssign: (item) => JSON.stringify(item),
				subtitleFn: (item) => { try { return item[subtitle] } catch { return '' } }
			};
		}
		else {
			this.multipleAutoCompleteConfiguration = {
				filterFn: myfunc.bind(this),
				initialItems: (extraData) => myfunc(''),
				displayFn: (item) => { try { return typeof (item) == 'string' ? JSON.parse(item)[title] : item[title] } catch { return '' } },
				titleFn: (item) => { try { return typeof (item) == 'string' ? JSON.parse(item)[title] : item[title] } catch { return '' } },
				valueAssign: (item) => { try { return typeof (item) == 'string' ? item : JSON.stringify(item) } catch { return '' } },
				subtitleFn: (item) => { try { return item[subtitle] } catch { return '' } }
			}
		}
	}

	parseTags() {
		try {


			let stringValue = this.propertiesFormGroup.get(this.field.id).get('value').value;
			if (typeof stringValue === 'string') {
				stringValue = (<string>stringValue).replace(new RegExp('{', 'g'), '{"').replace(new RegExp('=', 'g'), '":"').replace(new RegExp(',', 'g'), '",').replace(new RegExp(', ', 'g'), ', "').replace(new RegExp('}', 'g'), '"}');
				stringValue = stringValue.replace(new RegExp('}"', 'g'), '}').replace(new RegExp('"{', 'g'), '{');
			} else if (stringValue instanceof Array) {
				const tempArray = [];
				for (const stringTag of stringValue) {
					tempArray.push(JSON.parse(stringTag));
				}
				stringValue = JSON.stringify(tempArray);
			}
			const tagArray = JSON.parse(stringValue);
			this.propertiesFormGroup.get(this.field.id).get('value').patchValue(tagArray);
		} catch (e) {
			console.warn('Could not parse tags');
		}
	}

	showTag(ev: any) {
		if (typeof ev === 'string') {
			return ev;
		} else {
			return ev.name;
		}
	}

	getDatasetIdControl(name: string): UntypedFormControl {
		return this.propertiesFormGroup.get(this.field.id).get(name) as UntypedFormControl;
	}

	validateId() {
		//TODO refactor
		return null;
		// const identifier = this.getDatasetIdControl('identifier').value;
		// const type = this.getDatasetIdControl('type').value;
		// this.validationIcon = 'loading';
		// this.externalSourcesService.validateIdentifier(identifier, type).pipe(takeUntil(this._destroyed)).subscribe(result => {
		// 	this.validationIcon = result === true ? 'done' : 'clear';
		// });

	}

    get maxFileSize(): number {
        const data = this.field.data as FieldUploadData;
		return data ? data.maxFileSizeInMB * 1048576 : null
    }

	typesToString() {
		return (this.field.data as FieldUploadData).types.map(type => type.value).join(', ');
	}

    getLanguageInfos(): LanguageInfo[] {
        return this.languageInfoService.getLanguageInfoValues();
    }
}
