import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { NgTemplateOutlet } from '@angular/common';
import { Component, OnInit, inject, input, ChangeDetectionStrategy } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PreviewFieldSource } from '@app/core/common/enum/preview-field-source';
import { PreviewFieldType } from '@app/core/common/enum/preview-field-type';
import { DepositPreviewModel, DepositPreviewResult, PreviewField, PreviewFieldGroup, PreviewFieldOption, PreviewFieldValue, PreviewGroupItem, PreviewGroupValues, StoredPreviewField } from '@app/core/model/deposit/deposit-preview';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import { RichTextEditorModule } from '@app/library/rich-text-editor/rich-text-editor.module';
import { Guid } from '@common/types/guid';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

export type DepositReviewSource = 'plan' | 'you' | 'auto' | 'missing';

@Component({
	selector: 'app-deposit-review',
	templateUrl: './deposit-review.component.html',
	styleUrls: ['./deposit-review.component.scss'],
	standalone: true,
	changeDetection: ChangeDetectionStrategy.Eager,
	imports: [
		ReactiveFormsModule,
		MatFormFieldModule,
		MatInputModule,
		MatSelectModule,
		MatAutocompleteModule,
		MatDatepickerModule,
		MatNativeDateModule,
		MatSlideToggleModule,
		MatChipsModule,
		MatButtonModule,
		MatIconModule,
		MatTooltipModule,
		RichTextEditorModule,
		TranslateModule,
		NgTemplateOutlet
	]
})
export class DepositReviewComponent implements OnInit {
	private depositService = inject(DepositService);
	private language = inject(TranslateService);

	readonly preview = input.required<DepositPreviewModel>();
	readonly repositoryId = input.required<string>();
	readonly planId = input.required<Guid>();
	readonly repositoryLabel = input('');

	previewFieldTypeEnum = PreviewFieldType;
	previewFieldSourceEnum = PreviewFieldSource;
	readonly tagSeparatorKeysCodes = [ENTER, COMMA] as const;

	groups: PreviewFieldGroup[] = [];
	formGroup: FormGroup = new FormGroup({});
	formInvalid = false;
	rowArrays: Map<string, FormArray<FormGroup>> = new Map();
	tagValues: Map<string, string[]> = new Map<string, string[]>();

	private structurallyChangedGroups: Set<string> = new Set();
	private vocabOptions: Map<string, PreviewFieldOption[]> = new Map();
	private vocabLabels: Map<string, Map<string, string>> = new Map();
	private vocabSearch$: Map<string, Subject<string>> = new Map();

	openGroups: Set<string> = new Set();
	moreOpen = false;
	private addedGroups: Set<string> = new Set();

	ngOnInit(): void {
		// the dialog lists the uploaded files itself, from what the plugin declares
		this.groups = (this.preview()?.groups ?? []).filter(group => group.code !== 'files');
		this.groups.forEach(group => {
			if (group.repeatable) {
				this.registerRepeatableGroup(group);
				(group.fields ?? []).forEach(field => {
					if (field.vocabSearch) this.registerVocabField(field);
				});
			}
			else (group.fields ?? []).forEach(field => this.registerField(field));
		});
	}

	asFormControl(code: string): FormControl {
		return this.formGroup.get(code) as FormControl;
	}

	isRichText(field: PreviewField): boolean {
		return field.widgetHint === 'richtext';
	}

	isMultiVocab(field: PreviewField): boolean {
		return field.type === PreviewFieldType.Select && field.multipleSelect;
	}

	allowsCustomValues(field: PreviewField): boolean {
		return field.widgetHint === 'allow-custom';
	}

	addCustomChip(field: PreviewField, event: MatChipInputEvent): void {
		if (!this.allowsCustomValues(field)) {
			return;
		}
		const value = (event.value || '').trim();
		if (value && !this.tags(field).includes(value)) {
			this.tagValues.set(field.code, [...this.tags(field), value]);
			this.markTagsChanged(field);
		}
		event.chipInput?.clear();
	}

	get scalarGroups(): PreviewFieldGroup[] {
		return this.groups.filter(x => !x.repeatable);
	}

	get repeatableGroups(): PreviewFieldGroup[] {
		return this.groups.filter(x => x.repeatable);
	}

	isEditable(field: PreviewField): boolean {
		return field.type !== PreviewFieldType.ReadOnly && field.source !== PreviewFieldSource.System;
	}

	groupLabel(group: PreviewFieldGroup): string {
		return this.translateOrFallback(`DEPOSIT-PREVIEW-DIALOG.GROUPS.${group.code}`, group.label);
	}

	fieldLabel(field: PreviewField): string {
		return this.translateOrFallback(`DEPOSIT-PREVIEW-DIALOG.FIELDS.${field.code}`, field.label);
	}

	optionLabel(option: PreviewFieldOption): string {
		return this.translateOrFallback(`DEPOSIT-PREVIEW-DIALOG.OPTIONS.${option.value}`, option.label);
	}

	private translateOrFallback(key: string, fallback: string): string {
		const translated = this.language.instant(key);
		return translated !== key ? translated : fallback;
	}

	readOnlyDisplay(field: PreviewField): string {
		if (field.value?.textListValue?.length) return field.value.textListValue.join(', ');
		if (!field.value?.textValue) return '-';
		return this.translateOrFallback(`DEPOSIT-PREVIEW-DIALOG.OPTIONS.${field.value.textValue}`, field.value.textValue);
	}

	previousValueDisplay(field: PreviewField): string {
		if (!field.previousValue) return null;
		const raw = field.previousValue.textListValue?.length ? field.previousValue.textListValue.join(', ')
			: field.previousValue.textValue ?? (field.previousValue.dateValue != null ? new Date(field.previousValue.dateValue).toLocaleDateString() : null);
		return raw != null ? this.language.instant('DEPOSIT-PREVIEW-DIALOG.PREVIOUS-VALUE-HINT', { value: raw }) : null;
	}

	// ---- tags ----

	tags(field: PreviewField): string[] {
		return this.tagValues.get(field.code) ?? [];
	}

	private markTagsChanged(field: PreviewField): void {
		const control = this.formGroup.get(field.code);
		control?.markAsDirty();
		control?.updateValueAndValidity();
	}

	addTag(field: PreviewField, event: MatChipInputEvent): void {
		const value = (event.value || '').trim();
		if (value) {
			this.tagValues.set(field.code, [...this.tags(field), value]);
			this.markTagsChanged(field);
		}
		event.chipInput?.clear();
	}

	removeTag(field: PreviewField, tag: string): void {
		this.tagValues.set(field.code, this.tags(field).filter(x => x !== tag));
		this.markTagsChanged(field);
	}

	// ---- vocabulary autocomplete ----

	vocabOptionsFor(field: PreviewField): PreviewFieldOption[] {
		return this.vocabOptions.get(field.code) ?? field.options ?? [];
	}

	vocabDisplayFn(field: PreviewField): (value: string) => string {
		return (value: string) => {
			if (!value) return '';
			const stored = this.vocabLabels.get(field.code)?.get(value) ?? value;
			return this.translateOrFallback(`DEPOSIT-PREVIEW-DIALOG.OPTIONS.${value}`, stored);
		};
	}

	onVocabInput(field: PreviewField, event: Event): void {
		const term = (event.target as HTMLInputElement).value;
		this.vocabSearch$.get(field.code)?.next(term ?? '');
	}

	onVocabSelected(field: PreviewField, event: MatAutocompleteSelectedEvent): void {
		this.formGroup.get(field.code)?.setValue(event.option.value);
		this.formGroup.get(field.code)?.markAsDirty();
	}

	chipDisplay(field: PreviewField, value: string): string {
		const stored = this.vocabLabels.get(field.code)?.get(value) ?? value;
		return this.translateOrFallback(`DEPOSIT-PREVIEW-DIALOG.OPTIONS.${value}`, stored);
	}

	onMultiOptionSelected(field: PreviewField, event: MatAutocompleteSelectedEvent, input: HTMLInputElement): void {
		const value = event.option.value as string;
		if (value && !this.tags(field).includes(value)) {
			this.tagValues.set(field.code, [...this.tags(field), value]);
			this.markTagsChanged(field);
		}
		input.value = '';
	}

	onTagOptionSelected(field: PreviewField, event: MatAutocompleteSelectedEvent, input: HTMLInputElement): void {
		const value = event.option.value as string;
		if (value && !this.tags(field).includes(value)) {
			this.tagValues.set(field.code, [...this.tags(field), value]);
			this.markTagsChanged(field);
		}
		input.value = '';
	}

	// ---- repeatable groups ----

	rows(group: PreviewFieldGroup): FormArray<FormGroup> {
		return this.rowArrays.get(group.code);
	}

	addRow(group: PreviewFieldGroup): void {
		this.rows(group).push(this.buildRow(group, null));
		this.structurallyChangedGroups.add(group.code);
	}

	removeRow(group: PreviewFieldGroup, index: number): void {
		this.rows(group).removeAt(index);
		this.structurallyChangedGroups.add(group.code);
	}

	// ---- summary ----

	get summaryGroups(): PreviewFieldGroup[] {
		return this.groups.filter(group => this.isListed(group));
	}

	get moreGroups(): PreviewFieldGroup[] {
		return this.groups.filter(group => !this.isListed(group));
	}

	get missingGroups(): PreviewFieldGroup[] {
		return this.groups.filter(group => this.isMissing(group));
	}

	private isListed(group: PreviewFieldGroup): boolean {
		return this.hasValue(group) || this.isRequired(group) || this.addedGroups.has(group.code) || this.openGroups.has(group.code);
	}

	isRequired(group: PreviewFieldGroup): boolean {
		return !!group.required || (group.fields ?? []).some(field => field.required && this.isEditable(field));
	}

	isMissing(group: PreviewFieldGroup): boolean {
		if (group.repeatable) return this.groupHasMissingRows(group);
		return (group.fields ?? []).some(field => this.isEditable(field) && this.formGroup.get(field.code)?.invalid);
	}

	isOpen(group: PreviewFieldGroup): boolean {
		return this.openGroups.has(group.code);
	}

	isFixed(group: PreviewFieldGroup): boolean {
		return !group.repeatable && (group.fields ?? []).every(field => !this.isEditable(field));
	}

	toggle(group: PreviewFieldGroup): void {
		if (this.openGroups.has(group.code)) this.openGroups.delete(group.code);
		else this.openGroups.add(group.code);
	}

	add(group: PreviewFieldGroup): void {
		this.addedGroups.add(group.code);
		this.openGroups.add(group.code);
		if (group.repeatable && !this.rows(group)?.length) this.addRow(group);
	}

	complete(group: PreviewFieldGroup): void {
		this.openGroups.add(group.code);
		this.formGroup.markAllAsTouched();
	}

	source(group: PreviewFieldGroup): DepositReviewSource {
		if (this.isMissing(group)) return 'missing';
		if (this.isFixed(group)) return 'auto';
		if (group.repeatable) {
			const rows = this.rows(group);
			return rows?.dirty || this.structurallyChangedGroups.has(group.code) || group.storedItemsApplied ? 'you' : 'plan';
		}
		const changed = (group.fields ?? []).some(field => field.storedValueApplied || this.formGroup.get(field.code)?.dirty);
		return changed ? 'you' : 'plan';
	}

	summary(group: PreviewFieldGroup): string {
		if (group.repeatable) {
			// a row reads best by its name or title, then any other text; selects (kind, role) only stand in when nothing else is filled
			const all = group.fields ?? [];
			const named = all.filter(field => field.type !== PreviewFieldType.Select && /name|title/i.test(field.code));
			const fields = [...named, ...all.filter(field => field.type !== PreviewFieldType.Select && !named.includes(field)), ...all.filter(field => field.type === PreviewFieldType.Select)];
			const given = all.find(field => /given/i.test(field.code));
			const family = all.find(field => /family/i.test(field.code));
			return (this.rows(group)?.controls ?? [])
				.map(row => {
					const person = [given, family].map(field => field ? row.get(field.code)?.value : null).filter(value => value != null && `${value}`.trim().length).join(' ');
					return person || fields.map(field => row.get(field.code)?.value).find(value => value != null && `${value}`.trim().length);
				})
				.filter(value => !!value).join(', ');
		}
		return (group.fields ?? []).map(field => this.fieldDisplay(field)).filter(value => !!value).join(' · ');
	}

	private hasValue(group: PreviewFieldGroup): boolean {
		if (group.repeatable) return (this.rows(group)?.length ?? 0) > 0;
		return (group.fields ?? []).some(field => !!this.fieldDisplay(field));
	}

	private fieldDisplay(field: PreviewField): string {
		if (!this.isEditable(field)) return field.value?.textListValue?.length || field.value?.textValue ? this.readOnlyDisplay(field) : null;
		if (this.isMultiVocab(field)) return this.tags(field).map(value => this.chipDisplay(field, value)).join(', ');
		if (field.type === PreviewFieldType.Tags) return this.tags(field).join(', ');
		const value = this.formGroup.get(field.code)?.value;
		if (value == null || `${value}`.trim() === '') return null;
		switch (field.type) {
			case PreviewFieldType.Date: return new Date(value).toLocaleDateString();
			case PreviewFieldType.Boolean: return value ? this.fieldLabel(field) : null;
			case PreviewFieldType.Select: {
				const option = (field.options ?? []).find(x => x.value === value);
				return option ? this.optionLabel(option) : this.vocabDisplayFn(field)(value);
			}
			case PreviewFieldType.TextArea: return this.plainText(`${value}`);
			default: return `${value}`;
		}
	}

	private plainText(value: string): string {
		return value.replace(/<[^>]*>/g, ' ').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim();
	}

	// ---- result ----

	/** The edits to send with the deposit, or null when a required value is missing. */
	result(): DepositPreviewResult | null {
		if (this.formGroup.invalid) {
			this.formGroup.markAllAsTouched();
			this.formInvalid = true;
			this.missingGroups.forEach(group => this.openGroups.add(group.code));
			return null;
		}
		this.formInvalid = false;
		const storedFields: StoredPreviewField[] = [];
		this.scalarGroups.forEach(group => (group.fields ?? []).forEach(field => {
			if (!this.isEditable(field)) return;
			const control = this.formGroup.get(field.code);
			if (control == null) return;
			if (!control.dirty && !field.storedValueApplied) return;
			storedFields.push({
				value: this.toFieldValue(field, control.value),
				base: field.baseValue ?? null
			});
		}));

		const storedGroups: PreviewGroupValues[] = [];
		this.repeatableGroups.forEach(group => {
			const rows = this.rows(group);
			if (rows == null) return;
			const touched = rows.dirty || this.structurallyChangedGroups.has(group.code) || group.storedItemsApplied;
			if (!touched) return;
			const items: PreviewGroupItem[] = rows.controls.map(row => ({
				values: (group.fields ?? []).map(field => this.toRowFieldValue(field, row.get(field.code)?.value))
			}));
			storedGroups.push({ groupCode: group.code, items: items, baseItems: group.baseItems ?? [] });
		});

		return { storedFields, storedGroups };
	}

	// ---- registration ----

	private registerField(field: PreviewField): void {
		if (!this.isEditable(field)) return;
		const validators = field.required ? [Validators.required] : [];
		if (this.isMultiVocab(field)) {
			this.tagValues.set(field.code, [...(field.value?.textListValue ?? [])]);
			this.formGroup.addControl(field.code, new FormControl(null, field.required ? [this.requireAtLeastOneTag(field)] : []));
			if (field.vocabSearch) this.registerVocabField(field);
			return;
		}
		switch (field.type) {
			case PreviewFieldType.Tags: {
				this.tagValues.set(field.code, [...(field.value?.textListValue ?? [])]);
				this.formGroup.addControl(field.code, new FormControl(null, field.required ? [this.requireAtLeastOneTag(field)] : []));
				break;
			}
			case PreviewFieldType.Date: {
				const initial = field.value?.dateValue != null ? new Date(field.value.dateValue) : null;
				this.formGroup.addControl(field.code, new FormControl(initial, validators));
				break;
			}
			case PreviewFieldType.Boolean: {
				this.formGroup.addControl(field.code, new FormControl(field.value?.booleanValue ?? false, validators));
				break;
			}
			case PreviewFieldType.Number: {
				this.formGroup.addControl(field.code, new FormControl(field.value?.numberValue ?? null, validators));
				break;
			}
			default: {
				this.formGroup.addControl(field.code, new FormControl(field.value?.textValue ?? null, validators));
			}
		}
		if (field.vocabSearch) this.registerVocabField(field);
	}

	private registerVocabField(field: PreviewField): void {
		const labels = new Map<string, string>();
		(field.options ?? []).forEach(option => labels.set(option.value, option.label));
		if (field.value?.textValue && !labels.has(field.value.textValue)) labels.set(field.value.textValue, field.value.textValue);
		(field.value?.textListValue ?? []).forEach(entry => { if (!labels.has(entry)) labels.set(entry, entry); });
		this.vocabLabels.set(field.code, labels);
		this.vocabOptions.set(field.code, field.options ?? []);

		const search$ = new Subject<string>();
		this.vocabSearch$.set(field.code, search$);
		search$.pipe(debounceTime(300), distinctUntilChanged()).subscribe(term => {
			this.depositService.searchVocabulary(this.repositoryId(), field.code, this.planId(), term)
				.subscribe(options => {
					this.vocabOptions.set(field.code, options ?? []);
					const map = this.vocabLabels.get(field.code);
					(options ?? []).forEach(option => map.set(option.value, option.label));
				});
		});
	}

	private registerRepeatableGroup(group: PreviewFieldGroup): void {
		const array = new FormArray<FormGroup>([]);
		(group.items ?? []).forEach(item => array.push(this.buildRow(group, item)));
		if (group.required) array.addValidators(this.requireAtLeastOneRow());
		array.updateValueAndValidity();
		this.rowArrays.set(group.code, array);
		this.formGroup.addControl(this.groupArrayControlName(group), array);
	}

	// Validators.minLength treats a zero-length array as "no value" and skips validation entirely,
	// so it never flags an empty required group; this validator explicitly requires at least one row.
	private requireAtLeastOneRow(): ValidatorFn {
		return (control: AbstractControl): ValidationErrors | null =>
			(control.value?.length ?? 0) > 0 ? null : { required: true };
	}

	// Tags/multi-vocab fields keep their actual data in tagValues, not the control's own value (which is
	// always null), so Validators.required can never see it; this reads the real chip list instead.
	private requireAtLeastOneTag(field: PreviewField): ValidatorFn {
		return (): ValidationErrors | null =>
			this.tags(field).length > 0 ? null : { required: true };
	}

	private groupArrayControlName(group: PreviewFieldGroup): string {
		return `__group__${group.code}`;
	}

	groupHasMissingRows(group: PreviewFieldGroup): boolean {
		return !!group.required && !this.rows(group)?.length;
	}

	private buildRow(group: PreviewFieldGroup, item: PreviewGroupItem): FormGroup {
		const row = new FormGroup({});
		(group.fields ?? []).forEach(field => {
			const value = item?.values?.find(x => x.code === field.code);
			row.addControl(field.code, new FormControl(value?.textValue ?? null));
		});
		return row;
	}

	// ---- value conversion ----

	private toFieldValue(field: PreviewField, controlValue: any): PreviewFieldValue {
		const value: PreviewFieldValue = { code: field.code };
		if (this.isMultiVocab(field)) {
			value.textListValue = this.tags(field);
			return value;
		}
		switch (field.type) {
			case PreviewFieldType.Tags:
				value.textListValue = this.tags(field);
				break;
			case PreviewFieldType.Date:
				value.dateValue = controlValue != null ? new Date(controlValue) : null;
				break;
			case PreviewFieldType.Boolean:
				value.booleanValue = controlValue;
				break;
			case PreviewFieldType.Number:
				value.numberValue = controlValue;
				break;
			default:
				value.textValue = controlValue;
		}
		return value;
	}

	private toRowFieldValue(field: PreviewField, controlValue: any): PreviewFieldValue {
		return { code: field.code, textValue: controlValue != null && `${controlValue}`.trim().length ? `${controlValue}` : null };
	}
}
