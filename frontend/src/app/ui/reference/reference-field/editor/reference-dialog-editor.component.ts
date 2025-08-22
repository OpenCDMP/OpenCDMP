import { Component, computed, HostBinding, Inject, OnInit } from '@angular/core';
import { FormControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { ReferenceType, ReferenceTypeDefinition, ReferenceTypeField } from '@app/core/model/reference-type/reference-type';
import { DefinitionPersist, FieldPersist, ReferencePersist } from '@app/core/model/reference/reference';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Component({
    templateUrl: 'reference-dialog-editor.component.html',
    styleUrls: ['./reference-dialog-editor.component.scss'],
    standalone: false
})
export class ReferenceDialogEditorComponent extends BaseComponent implements OnInit {
	formGroup: UntypedFormGroup;
	referenceType: ReferenceType;
	systemFields: string[];
	label: string = null
	referenceExists: Boolean;

	get hasReferenceTypeFields(): boolean {
		return this.referenceType && this.referenceType.definition && this.referenceType.definition.fields && this.referenceType.definition.fields.length > 0;
	}

	constructor(
		private referenceTypeService: ReferenceTypeService,
		private referenceService: ReferenceService,
		private fb: UntypedFormBuilder,
		public dialogRef: MatDialogRef<ReferenceDialogEditorComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private formService: FormService,
	) { 
		super(); 
		this.label = data.label;
		this.formGroup = this.fb.group({});
	}

	ngOnInit(): void {
		this.systemFields = this.referenceTypeService.getSystemFields([]);

		this.referenceTypeService.getSingle(this.data.referenceTypeId, this.lookupFields())
		.pipe(takeUntil(this._destroyed))
		.subscribe( //TODO HANDLE-ERRORS
			referenceType => {
				this.referenceType = referenceType;
				this.buildForm(referenceType.definition?.fields);
				this.formGroup.get(this.systemFields[0]).valueChanges
				.pipe(takeUntil(this._destroyed), debounceTime(500))
				.subscribe(x => this.findReferenceIfExist());
				
			}
		);
	}

	findReferenceIfExist(): void {
		this.referenceExists = null;
		this.referenceService.findReference(this.formGroup.get(this.systemFields[0]).value, this.data.referenceTypeId)
		.pipe(takeUntil(this._destroyed))
		.subscribe( //TODO HANDLE-ERRORS
			result => {
				this.referenceExists = result;
			}
		);
	}

	buildForm(fields: ReferenceTypeField[]) {
	
		this.systemFields.forEach(systemField => {
			this.formGroup.setControl(systemField, new FormControl({ value: null, disabled: false }, Validators.required));
		})

		if (fields != null && fields.length >= 0){
			fields.forEach(x => {
				this.formGroup.setControl(x.code, new FormControl({ value: null, disabled: false }, Validators.required));
			})
		}

	}

	isFormValid() {
		return this.formGroup.valid && this.referenceExists != null && !this.referenceExists;
	}

	send() {
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.formGroup.valid) { return; }
		
		this.dialogRef.close(this.buildReferencePersist());

	}

	buildReferencePersist() : ReferencePersist{
		return {
			label: this.formGroup.get(this.systemFields[1]).value,
			description: this.formGroup.get(this.systemFields[2]).value,
			typeId: this.data.referenceTypeId,
			reference: this.formGroup.get(this.systemFields[0]).value,
			abbreviation: "",
			source: "Internal",
			sourceType: ReferenceSourceType.Internal,
			definition: this.buildDefinitionPersist(this.referenceType.definition.fields),
		}
		
	}

	buildDefinitionPersist(fields: ReferenceTypeField[]): DefinitionPersist{
		if(fields == null || fields.length == 0) return null;
		return {
			fields: fields.map(x => this.buildFieldPersist(x))
		} 
	}

	buildFieldPersist(field: ReferenceTypeField): FieldPersist{
		return {
			code: field.code,
			dataType: field.dataType,
			value: this.formGroup.get(field.code).value,
		}
	}

	close() {
		this.dialogRef.close(false);
	}

	private lookupFields(): string[] {
		return [
			nameof<ReferenceType>(x => x.id),
			nameof<ReferenceType>(x => x.name),
			nameof<ReferenceType>(x => x.code),

			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.code)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.label)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.dataType)].join('.')
		]
	}

    isRequired(control: FormControl): boolean{
        return control?.hasValidator(Validators.required);
    }
}
