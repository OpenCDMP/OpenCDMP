import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';

import { FormControl, ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { ReferenceType, ReferenceTypeDefinition, ReferenceTypeField } from '@app/core/model/reference-type/reference-type';
import { ExternalFetcherBaseSourceConfiguration } from '@app/core/model/external-fetcher/external-fetcher';
import { Reference, ReferenceDefinitionPersist, ReferenceFieldPersist, ReferencePersist } from '@app/core/model/reference/reference';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { MaterialModule } from '@common/material/material.module';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Guid } from '@common/types/guid';
import { takeUntil } from 'rxjs/operators';
import { ReferenceCardComponent } from '../../reference-card/reference-card.component';
import { nameof } from 'ts-simple-nameof';

// Presentational manual-create form for a reference, extracted from the old dialog so it can be
// hosted in either surface (the legacy dialog wrapper, or the search side-panel's "create" mode).
// Emits the created/selected reference instead of closing a dialog itself.
@Component({
	selector: 'app-reference-manual-form',
	standalone: true,
	imports: [ReactiveFormsModule, MaterialModule, MatDialogModule, TranslateModule, ReferenceCardComponent],
	templateUrl: './reference-manual-form.component.html',
	changeDetection: ChangeDetectionStrategy.Eager,
	styleUrls: ['./reference-manual-form.component.scss']
})
export class ReferenceManualFormComponent extends BaseComponent implements OnInit {
	@Input() referenceTypeId: Guid;
	@Input() label: string = null;
	// Set when the form is the whole dialog: it then renders the title and close button itself.
	@Input() title: string = null;
	// Prefills the form when editing; saving still emits a new reference and the original is never changed.
	@Input() reference: Reference = null;
	@Output() created = new EventEmitter<Reference>();
	@Output() cancelled = new EventEmitter<void>();

	formGroup: UntypedFormGroup;
	referenceType: ReferenceType;
	systemFields: string[];
	showDescription = false;
	preview: Reference = null;
	previewEmpty = true;
	sourceNames = '';
	logoUrl: string = null;
	labelTemplate: string = null;

	get hasReferenceTypeFields(): boolean {
		return !!(this.referenceType?.definition?.fields?.length);
	}

	constructor(
		private referenceTypeService: ReferenceTypeService,
		private configurationService: ConfigurationService,
		private language: TranslateService,
		private fb: UntypedFormBuilder,
		private formService: FormService,
	) {
		super();
		this.formGroup = this.fb.group({});
	}

	ngOnInit(): void {
		// Manual references get a generated identifier, so the form doesn't ask for one.
		this.systemFields = this.referenceTypeService.getSystemFields([]).filter(x => x !== 'reference_id');

		this.referenceTypeService.getSingle(this.referenceTypeId, this.lookupFields())
			.pipe(takeUntil(this._destroyed))
			.subscribe(referenceType => {
				this.referenceType = referenceType;
				this.sourceNames = referenceType.definition?.sources?.map(x => x.label).filter(x => !!x).join(', ') ?? '';
				const hasLogo = referenceType.definition?.sources?.some(x => !!x.storageFile?.id);
				this.logoUrl = hasLogo ? `${this.configurationService.server}reference-type/public/${referenceType.id}/logo` : null;
				this.showDescription = !!this.reference?.description;
				this.labelTemplate = referenceType.definition?.labelTemplate?.trim() || null;
				this.buildForm(referenceType.definition?.fields);
				this.preview = this.buildPreview();
				this.formGroup.valueChanges.pipe(takeUntil(this._destroyed)).subscribe(() => this.preview = this.buildPreview());
			});
	}

	buildForm(fields: ReferenceTypeField[]) {
		// With a name template the name is built from the fields, so the form doesn't ask for it.
		const templateCodes = this.templateCodes();
		this.systemFields.filter(x => !(this.labelTemplate && x === 'label')).forEach(systemField => {
			this.formGroup.setControl(systemField, new FormControl({ value: (systemField === 'label' ? this.reference?.label : this.reference?.description) ?? null, disabled: false }, systemField != 'description' ? Validators.required : null));
		});
		if (fields != null && fields.length >= 0) {
			fields.forEach(x => {
				const value = this.reference?.definition?.fields?.find(f => f.code === x.code)?.value ?? null;
				this.formGroup.setControl(x.code, new FormControl({ value, disabled: false }, x.required || templateCodes.includes(x.code) ? Validators.required : null));
			});
		}
	}

	send() {
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.formGroup.valid) { return; }
		this.created.emit(this.buildReferencePersist() as unknown as Reference);
	}

	buildReferencePersist(): ReferencePersist {
		return {
			label: this.labelTemplate ? this.applyLabelTemplate() : this.formGroup.get('label').value,
			description: this.formGroup.get('description').value,
			typeId: this.referenceTypeId,
			reference: Guid.create().toString(),
			abbreviation: "",
			source: "Internal",
			sourceType: ReferenceSourceType.Internal,
			definition: this.buildDefinitionPersist(this.referenceType.definition.fields),
		};
	}

	buildDefinitionPersist(fields: ReferenceTypeField[]): ReferenceDefinitionPersist {
		if (fields == null || fields.length == 0) return null;
		return { fields: fields.map(x => this.buildFieldPersist(x)) };
	}

	buildFieldPersist(field: ReferenceTypeField): ReferenceFieldPersist {
		return {
			code: field.code,
			dataType: field.dataType,
			value: this.formGroup.get(field.code).value,
		};
	}

	private buildPreview(): Reference {
		// Always a card, so the dialog doesn't grow on the first keystroke; an empty name shows a muted placeholder.
		const persist = this.buildReferencePersist();
		this.previewEmpty = !persist.label?.trim();
		const label = this.previewEmpty ? this.language.instant('REFERENCE-FIELD.REFERENCE-DIALOG-EDITOR.PREVIEW-PLACEHOLDER') : persist.label;
		return { ...persist, label, reference: null, type: this.referenceType } as unknown as Reference;
	}

	cancel() {
		this.cancelled.emit();
	}

	private templateCodes(): string[] {
		return Array.from(this.labelTemplate?.matchAll(/\{([\w-]+)\}/g) ?? []).map(m => m[1]);
	}

	private applyLabelTemplate(): string {
		return this.labelTemplate
			.replace(/\{([\w-]+)\}/g, (_m, code) => this.formGroup.get(code)?.value?.toString().trim() ?? '')
			.replace(/\s+/g, ' ')
			.trim();
	}

	// Many configured descriptions just repeat the label ("Pid", "Researchers First Name"); those add nothing as a hint.
	fieldHint(field: ReferenceTypeField): string {
		const hint = field.description?.trim();
		const label = field.label?.trim().toLowerCase();
		if (!hint || (label && hint.toLowerCase().endsWith(label))) return null;
		return hint;
	}

	isRequired(control: FormControl): boolean {
		return control?.hasValidator(Validators.required);
	}

	private lookupFields(): string[] {
		return [
			nameof<ReferenceType>(x => x.id),
			nameof<ReferenceType>(x => x.name),
			nameof<ReferenceType>(x => x.code),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.labelTemplate)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.code)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.label)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.dataType)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.required)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.fields), nameof<ReferenceTypeField>(x => x.description)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.label)].join('.'),
			[nameof<ReferenceType>(x => x.definition), nameof<ReferenceTypeDefinition>(x => x.sources), nameof<ExternalFetcherBaseSourceConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.')
		];
	}
}
