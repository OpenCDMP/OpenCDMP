
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from "@angular/material/dialog";
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { DescriptionTemplateFieldValidationType } from '@app/core/common/enum/description-template-field-validation-type';
import { DescriptionTemplateField, DescriptionTemplateFieldSet, DescriptionTemplateLabelAndMultiplicityData, DescriptionTemplateUploadData } from '@app/core/model/description-template/description-template';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DescriptionService } from '@app/core/services/description/description.service';
import { PlanService } from '@app/core/services/plan/plan.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { TagService } from '@app/core/services/tag/tag.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { BaseComponent } from '@common/base/base.component';
import { Observable } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { DescriptionFormService } from '../services/description-form.service';

@Component({
    selector: 'app-description-form-field',
    templateUrl: './form-field.component.html',
    styleUrls: ['./form-field.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class DescriptionFormFieldComponent extends BaseComponent implements OnInit {

	@Input() field: DescriptionTemplateField;
	@Input() fieldSet: DescriptionTemplateFieldSet;
	@Input() propertiesFormGroup: UntypedFormGroup;
	@Input() visibilityRulesService: VisibilityRulesService;

	@Input() descriptionTemplateId: any;
	@Input() isChild: Boolean = false;

	@Input() detectChangesObservable: Observable<any>;
    @Input() path: string;
	visible: boolean = true;
	descriptionTemplateFieldTypeEnum = DescriptionTemplateFieldType;



	public singleAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	public multipleAutoCompleteConfiguration: MultipleAutoCompleteConfiguration;
	tagsAutoCompleteConfiguration: SingleAutoCompleteConfiguration;
	multipleReferenceAutoCompleteConfiguration: MultipleAutoCompleteConfiguration;


	readonly separatorKeysCodes: number[] = [ENTER, COMMA];

	validationIcon;

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

	initFile: StorageFile = null;

	constructor(
		public planService: PlanService,
		public descriptionService: DescriptionService,
		public tagService: TagService,
		public dialog: MatDialog,
		private descriptionFormService: DescriptionFormService,
        private changeDetectorRef: ChangeDetectorRef
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
		this.descriptionFormService.getDetectChangesObservable().pipe(takeUntil(this._destroyed)).subscribe(x => this.changeDetectorRef.markForCheck());
	}

    get isRequired() {
        return this.field.validations?.includes(DescriptionTemplateFieldValidationType.Required)
    }

	private applyFieldType() {

		this.propertiesFormGroup.get(this.field.id).valueChanges
			.pipe(
				takeUntil(this._destroyed),
				distinctUntilChanged()
			)
			.subscribe(item => {
				this.visibilityRulesService.updateVisibilityForSource(this.field?.id);
				this.descriptionFormService.detectChanges(true);
			});
	}

	makeAutocompleteConfiguration(myfunc: Function, title: string, subtitle?: string): void {
		if (!((this.field.data as DescriptionTemplateLabelAndMultiplicityData).multipleSelect)) {
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
				const tempArray = new Array();
				for (let stringTag of stringValue) {
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
        const data = this.field.data as DescriptionTemplateUploadData;
		return data ? data.maxFileSizeInMB * 1048576 : null
    }

	typesToString() {
		return (this.field.data as DescriptionTemplateUploadData).types.map(type => type.value).toString();
	}
}
