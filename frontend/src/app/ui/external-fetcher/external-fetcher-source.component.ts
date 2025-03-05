import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormArray, UntypedFormGroup } from '@angular/forms';
import { ExternalFetcherApiHTTPMethodType } from '@app/core/common/enum/external-fetcher-api-http-method-type';
import { ExternalFetcherSourceType } from '@app/core/common/enum/external-fetcher-source-type';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { ExternalFetcherBaseSourceConfigurationEditorModel, HeaderConfigEditorModel, QueryCaseConfigEditorModel, QueryConfigEditorModel, StaticEditorModel, StaticOptionEditorModel } from './external-fetcher-source-editor.model';
import { Guid } from '@common/types/guid';
import { ExternalFetcherApiHeaderType } from '@app/core/common/enum/ExternalFetcherApiHeader.enum';

@Component({
    selector: 'app-external-fetcher-source-component',
    templateUrl: 'external-fetcher-source.component.html',
    styleUrls: ['./external-fetcher-source.component.scss'],
    standalone: false
})
export class ExternalFetcherSourceComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() formGroup: UntypedFormGroup = null;
	@Input() fieldsForm: any;
	@Input() validationErrorModel: ValidationErrorModel = null;
	@Input() validationRootPath: string = null;
	@Input() referenceTypeSourceIndex: number = null;
	@Input() referenceTypes: ReferenceType[] = null;
	@Input() sourceKeysMap: Map<Guid, string[]> = new Map<Guid, string[]>();

	externalFetcherSourceType = ExternalFetcherSourceType;
	externalFetcherApiHTTPMethodType = ExternalFetcherApiHTTPMethodType;
	externalFetcherSourceTypeEnum = this.enumUtils.getEnumValues<ExternalFetcherSourceType>(ExternalFetcherSourceType);
	externalFetcherApiHTTPMethodTypeEnum = this.enumUtils.getEnumValues<ExternalFetcherApiHTTPMethodType>(ExternalFetcherApiHTTPMethodType);
	externalFetcherApiHeaderType = ExternalFetcherApiHeaderType;
	externalFetcherApiHeaderTypeEnum = this.enumUtils.getEnumValues<ExternalFetcherApiHeaderType>(ExternalFetcherApiHeaderType);
	referenceTypeDependenciesMap: Map<number, ReferenceType[]> = new Map<number, ReferenceType[]>();

	constructor(
		public enumUtils: EnumUtils,
	) { super(); }

	ngOnInit() {
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['referenceTypes'] || changes['formGroup']) {
			if (this.referenceTypes != null && this.referenceTypes.length > 0) {
				const referenceTypeDependencyIds: Guid[] = this.formGroup.get('referenceTypeDependencyIds')?.value;
				if (referenceTypeDependencyIds && referenceTypeDependencyIds.length > 0 && this.referenceTypeSourceIndex != null) this.setReferenceTypeDependenciesMap(referenceTypeDependencyIds, this.referenceTypeSourceIndex);
			}
		}

	}

	private reApplyValidators(){
		ExternalFetcherBaseSourceConfigurationEditorModel.reapplyValidators(
			{
				formGroup: this.formGroup,
				validationErrorModel: this.validationErrorModel,
				rootPath: this.validationRootPath
			}
		)
	}

	//
	//
	// headers
	//
	//
	addHeader(): void {
		const formArray= this.formGroup.get('headers') as FormArray;
		const header: HeaderConfigEditorModel = new HeaderConfigEditorModel(this.validationErrorModel);
		formArray.push(header.buildForm({rootPath: this.validationRootPath + 'headers[' + formArray.length + '].'}));
	}

	removeHeader(headerIndex: number): void {
		const formArray = (this.formGroup.get('headers') as FormArray);
		formArray.removeAt(headerIndex);
		this.reApplyValidators();
		formArray.markAsDirty();
	}

	headerFieldDisabled(keyMethod: ExternalFetcherApiHeaderType) {
		return (this.formGroup.get('headers') as FormArray)?.controls.some(x => (x.get('key') as FormArray)?.value === keyMethod);
	}

	//
	//
	// queries
	//
	//
	addQuery(): void {
		const formArray= this.formGroup.get('queries') as FormArray;
		const query: QueryConfigEditorModel = new QueryConfigEditorModel(this.validationErrorModel);
		formArray.push(query.buildForm({rootPath: this.validationRootPath + 'queries[' + formArray.length + '].'}));
	}

	removeQuery(queryIndex: number): void {
		const formArray = (this.formGroup.get('queries') as FormArray);
		formArray.removeAt(queryIndex);
		this.reApplyValidators();
		formArray.markAsDirty();
	}

	// cases

	addCase(queryIndex: number): void {
		const formArray = (this.formGroup.get('queries') as FormArray).at(queryIndex).get('cases') as FormArray;
		const queryCase: QueryCaseConfigEditorModel = new QueryCaseConfigEditorModel(this.validationErrorModel);
		formArray.push(queryCase.buildForm({rootPath: this.validationRootPath + 'queries[' + queryIndex + '].cases[' + formArray.length + '].'}));

	}

	removeCase(queryIndex: number, index: number): void {
		const formArray = (this.formGroup.get('queries') as FormArray).at(queryIndex).get('cases') as FormArray;
		formArray.removeAt(index);
		this.reApplyValidators();
		formArray.markAsDirty();
	}

	// static item

	addStaticItem(): void {
		const formArray = this.formGroup.get('items') as FormArray;
		const staticItem = new StaticEditorModel(this.validationErrorModel);
		formArray.push(staticItem.buildForm({rootPath: this.validationRootPath + 'items[' + formArray.length + '].'}));

		this.addOption(formArray.length -1 , "reference_id");
		this.addOption(formArray.length -1, "label");
		this.addOption(formArray.length -1, "description");

		const fieldsFormArray = (this.fieldsForm as FormArray);
		if (fieldsFormArray && fieldsFormArray.length > 0) {
			for (let i = 0; i < fieldsFormArray.length; i++) {
				this.addOption(formArray.length - 1, fieldsFormArray.at(i).get('code').value);
			}
		}
	}

	removeStaticItem(staticIndex: number): void {
		const formArray = this.formGroup.get('items') as FormArray;
		formArray.removeAt(staticIndex);
		this.reApplyValidators();
		formArray.markAsDirty();
	}

	// Options

	addOption(staticIndex: number, code: string): void {
		const optionsFormArray = (this.formGroup.get('items') as FormArray).at(staticIndex).get('options') as FormArray;

		if (optionsFormArray && optionsFormArray.length > 0) {
			for (let i = 0; i < optionsFormArray.length; i++) {
				if (optionsFormArray.at(i).get('code').getRawValue() == code) {
					return;
				}
			}
		}

		const option = new StaticOptionEditorModel(this.validationErrorModel);
		optionsFormArray.push(option.buildForm({rootPath: this.validationRootPath + 'items[' + staticIndex + '].options[' + optionsFormArray.length + '].'}));
		optionsFormArray.at(optionsFormArray.length -1 ).get('code').patchValue(code);
	}

	removeOption(staticIndex: number, optionIndex: number): void {
		const formArray = (this.formGroup.get('items') as FormArray).at(staticIndex).get('options') as FormArray;
		formArray.removeAt(optionIndex);
		this.reApplyValidators();
		formArray.markAsDirty();
	}

	setReferenceTypeDependenciesMap(ids: Guid[], index: number){
		let mapValues :ReferenceType[] = [];
		this.referenceTypes.forEach(x => {
			if(ids.includes(x.id)){
				mapValues.push(x);
			}
		})
		this.referenceTypeDependenciesMap.set(index, mapValues);
	}

}
