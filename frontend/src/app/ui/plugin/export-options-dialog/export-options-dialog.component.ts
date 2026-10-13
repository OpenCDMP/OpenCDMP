import { Component, Inject, inject, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { PluginDataType } from '@app/core/common/enum/plugin-data-type';
import { ConfigurationField } from '@app/core/model/plugin-configuration/plugin-configuration';
import { MaterialModule } from '@common/material/material.module';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NgxColorsModule } from 'ngx-colors';

export interface ExportSelectiveItem {
	id: string;
	label: string;
}

export interface ExportOptionsDialogData {
	transformerId: string;
	fields: ConfigurationField[];
	sections?: ExportSelectiveItem[];
	descriptions?: ExportSelectiveItem[];
}

const INCLUDE_SECTIONS_CODE = 'include-sections';
const EXCLUDE_DESCRIPTIONS_CODE = 'exclude-descriptions';
const SELECTIVE_CODES = [INCLUDE_SECTIONS_CODE, EXCLUDE_DESCRIPTIONS_CODE];

/**
 * Collects per-export option values for a file transformer, built dynamically from the
 * transformer's user configuration fields. Empty values are omitted, so the exporting user's
 * stored preferences (or the plugin defaults) still apply for anything left blank. The last
 * choices are remembered per transformer.
 */
@Component({
	selector: 'app-export-options-dialog',
	standalone: true,
	imports: [ReactiveFormsModule, MatDialogModule, MaterialModule, NgxColorsModule, TranslateModule],
	template: `
		<h1 mat-dialog-title>{{ 'EXPORT-OPTIONS-DIALOG.TITLE' | translate }}</h1>
		<div mat-dialog-content>
			<form [formGroup]="formGroup" class="export-options-form">
				@for (field of optionFields; track field.code) {
					@switch (field.type) {
						@case (pluginDataTypeEnum.Select) {
							<mat-form-field class="w-100">
								<mat-label>{{ field.label }}</mat-label>
								<mat-select [formControlName]="field.code">
									<mat-option [value]="null">{{ defaultOptionLabel(field) }}</mat-option>
									@for (option of field.options ?? []; track option.value) {
										<mat-option [value]="option.value">{{ option.label }}</mat-option>
									}
								</mat-select>
							</mat-form-field>
						}
						@case (pluginDataTypeEnum.Boolean) {
							<div class="export-options-toggle">
								<mat-slide-toggle [checked]="isChecked(field.code)" (change)="setBoolean(field.code, $event.checked)">
									{{ field.label }}
								</mat-slide-toggle>
							</div>
						}
						@case (pluginDataTypeEnum.Color) {
							<mat-form-field class="w-100">
								<mat-label>{{ field.label }}</mat-label>
								<input matInput [formControlName]="field.code" />
								<ngx-colors class="suffix" matSuffix ngx-colors-trigger [formControlName]="field.code"></ngx-colors>
							</mat-form-field>
						}
						@case (pluginDataTypeEnum.Number) {
							<mat-form-field class="w-100">
								<mat-label>{{ field.label }}</mat-label>
								<input matInput type="number" [formControlName]="field.code">
							</mat-form-field>
						}
						@case (pluginDataTypeEnum.String) {
							<mat-form-field class="w-100">
								<mat-label>{{ field.label }}</mat-label>
								<input matInput [formControlName]="field.code">
								@if (field.defaultValue) {
									<mat-hint>{{ 'PLUGIN-EDITOR.DEFAULT-VALUE-HINT' | translate: { value: field.defaultValue } }}</mat-hint>
								}
							</mat-form-field>
						}
						@case (pluginDataTypeEnum.TextArea) {
							<mat-form-field class="w-100">
								<mat-label>{{ field.label }}</mat-label>
								<textarea matInput rows="3" [formControlName]="field.code"></textarea>
								@if (field.defaultValue) {
									<mat-hint>{{ 'PLUGIN-EDITOR.DEFAULT-VALUE-HINT' | translate: { value: field.defaultValue } }}</mat-hint>
								}
							</mat-form-field>
						}
					}
				}
			</form>
			@if (showSections) {
				<div class="selective-group">
					<div class="selective-title">{{ 'EXPORT-OPTIONS-DIALOG.SECTIONS' | translate }}</div>
					@for (section of data.sections; track section.id) {
						<mat-checkbox [checked]="sectionChecked[section.id]" (change)="sectionChecked[section.id] = $event.checked">{{ section.label }}</mat-checkbox>
					}
				</div>
			}
			@if (showDescriptions) {
				<div class="selective-group">
					<div class="selective-title">{{ 'EXPORT-OPTIONS-DIALOG.DESCRIPTIONS' | translate }}</div>
					@for (description of data.descriptions; track description.id) {
						<mat-checkbox [checked]="descriptionChecked[description.id]" (change)="descriptionChecked[description.id] = $event.checked">{{ description.label }}</mat-checkbox>
					}
				</div>
			}
		</div>
		<div mat-dialog-actions align="end">
			<button mat-button (click)="cancel()">{{ 'EXPORT-OPTIONS-DIALOG.CANCEL' | translate }}</button>
			<button mat-raised-button color="primary" [disabled]="exportDisabled" (click)="confirm()">{{ 'EXPORT-OPTIONS-DIALOG.EXPORT' | translate }}</button>
		</div>
	`,
	changeDetection: ChangeDetectionStrategy.Eager,
	styles: [`
		.export-options-form { display: flex; flex-direction: column; padding-top: 0.5rem; }
		.export-options-toggle { padding: 0.5rem 0 1rem 0; }
		.w-100 { width: 100%; }
		.selective-group { display: flex; flex-direction: column; padding-bottom: 0.5rem; }
		.selective-title { font-weight: 600; padding: 0.5rem 0 0.25rem 0; }
	`]
})
export class ExportOptionsDialogComponent {

	pluginDataTypeEnum = PluginDataType;
	private language = inject(TranslateService);

	/** "Default", or "Default (A4)" when the plugin declares which option applies to an empty field. */
	defaultOptionLabel(field: ConfigurationField): string {
		const fallback = field.defaultValue != null && field.defaultValue !== ''
			? (field.options?.find(x => x.value === field.defaultValue)?.label ?? field.defaultValue)
			: null;
		return fallback != null
			? this.language.instant('PLUGIN-EDITOR.DEFAULT-OPTION-WITH-VALUE', { value: fallback })
			: this.language.instant('EXPORT-OPTIONS-DIALOG.DEFAULT-OPTION');
	}
	formGroup: FormGroup;
	optionFields: ConfigurationField[] = [];
	showSections = false;
	showDescriptions = false;
	sectionChecked: { [id: string]: boolean } = {};
	descriptionChecked: { [id: string]: boolean } = {};

	constructor(
		private dialogRef: MatDialogRef<ExportOptionsDialogComponent, { [code: string]: string }>,
		@Inject(MAT_DIALOG_DATA) public data: ExportOptionsDialogData,
		formBuilder: FormBuilder
	) {
		// the selective codes get entity-aware checklist rendering; without context they are hidden
		this.optionFields = (this.data.fields ?? []).filter(x => !SELECTIVE_CODES.includes(x.code));
		this.showSections = (this.data.sections?.length ?? 0) > 0 && (this.data.fields ?? []).some(x => x.code === INCLUDE_SECTIONS_CODE);
		this.showDescriptions = (this.data.descriptions?.length ?? 0) > 0 && (this.data.fields ?? []).some(x => x.code === EXCLUDE_DESCRIPTIONS_CODE);
		this.data.sections?.forEach(x => this.sectionChecked[x.id] = true);
		this.data.descriptions?.forEach(x => this.descriptionChecked[x.id] = true);

		const stored = this.readStored();
		const controls: { [code: string]: any } = {};
		for (const field of this.optionFields) {
			controls[field.code] = formBuilder.control(stored[field.code] ?? null);
		}
		this.formGroup = formBuilder.group(controls);
	}

	get exportDisabled(): boolean {
		return this.showSections && this.data.sections.every(x => !this.sectionChecked[x.id]);
	}

	isChecked(code: string): boolean {
		return this.formGroup.get(code)?.value === 'true';
	}

	setBoolean(code: string, checked: boolean): void {
		this.formGroup.get(code)?.setValue(checked ? 'true' : 'false');
	}

	cancel(): void {
		this.dialogRef.close(null);
	}

	confirm(): void {
		const value: { [code: string]: string } = {};
		for (const field of this.optionFields) {
			const controlValue = this.formGroup.get(field.code)?.value;
			if (controlValue !== null && controlValue !== undefined && ('' + controlValue).length > 0) value[field.code] = '' + controlValue;
		}
		try {
			// selective choices are entity-specific and deliberately not remembered
			localStorage.setItem(this.storageKey(), JSON.stringify(value));
		} catch { }

		if (this.showSections) {
			const included = this.data.sections.filter(x => this.sectionChecked[x.id]);
			if (included.length < this.data.sections.length) value[INCLUDE_SECTIONS_CODE] = included.map(x => x.id).join(',');
		}
		if (this.showDescriptions) {
			const excluded = this.data.descriptions.filter(x => !this.descriptionChecked[x.id]);
			if (excluded.length > 0) value[EXCLUDE_DESCRIPTIONS_CODE] = excluded.map(x => x.id).join(',');
		}
		this.dialogRef.close(value);
	}

	private storageKey(): string {
		return `export-options-${this.data.transformerId}`;
	}

	private readStored(): { [code: string]: string } {
		try {
			return JSON.parse(localStorage.getItem(this.storageKey())) ?? {};
		} catch {
			return {};
		}
	}
}
