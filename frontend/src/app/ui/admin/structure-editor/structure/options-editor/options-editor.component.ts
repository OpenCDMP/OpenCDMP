import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { FieldRadioBoxDataEditorModel, FieldRadioBoxOptionEditorModel, FieldSelectDataEditorModel, FieldSelectOptionEditorModel, SemanticValueEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { shortKey, usagesOfKey } from '../../semantic-objects';
import { BlueprintStructureHelper } from '../../structure.helper';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton, MatButton } from '@angular/material/button';
import { Bp2SemanticsPickerComponent } from '../semantics-picker/semantics-picker.component';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

/**
 * Options of a single or multiple choice: what authors read and what is stored, side by side, with one
 * option markable as preselected. Renaming a stored value carries over to the default and to rules using it.
 */
@Component({
    selector: 'app-bp2-options-editor',
    templateUrl: './options-editor.component.html',
    styleUrls: ['./options-editor.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatSlideToggle, ReactiveAsteriskDirective, FormsModule, ReactiveFormsModule, MatTooltip, MatIcon, MatIconButton, Bp2SemanticsPickerComponent, MatButton, TranslatePipe, Bp2CountPipe]
})
export class Bp2OptionsEditorComponent {
	@Input() fieldForm: UntypedFormGroup;
	@Input() rootPath = '';
	/** Single choice offers the radio / dropdown switch; multiple choice is always a list. */
	@Input() single = true;
	/** The objects around the input, innermost first: answer keys under them are suggested first and shortened. */
	@Input() scope: string[] = [];
	/** Whether each option shows what it stands for; the question's Export row switches it on. */
	@Input() showKeys = true;
	@Output() dropdownChange = new EventEmitter<boolean>();

	/** Options whose answer keys are open. */
	openKeys = new Set<AbstractControl>();

	private valueOnFocus: string = null;

	constructor(public editor: StructureEditorBase<any, any>) { }

	get readOnly(): boolean { return this.editor.readOnly; }
	get data(): UntypedFormGroup { return this.fieldForm?.get('data') as UntypedFormGroup; }
	get options(): UntypedFormArray { return this.data?.get('options') as UntypedFormArray; }
	get isDropdown(): boolean { return BlueprintStructureHelper.fieldType(this.fieldForm) === FieldType.SELECT; }
	get defaultControl() { return this.fieldForm?.get('defaultValue')?.get('textValue'); }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }

	isDefault(option: UntypedFormGroup): boolean {
		const v = option.get('value').value;
		return v != null && v !== '' && this.defaultControl?.value === v;
	}

	toggleDefault(option: UntypedFormGroup): void {
		if (this.readOnly || !this.defaultControl) return;
		const v = option.get('value').value;
		if (!v) return;
		this.defaultControl.setValue(this.defaultControl.value === v ? null : v);
		this.defaultControl.markAsDirty();
	}

	addOption(): void {
		const index = this.options.length;
		const path = `${this.rootPath}data.options[${index}].`;
		const model = this.isDropdown ? new FieldSelectOptionEditorModel(this.editor.editorModel.validationErrorModel) : new FieldRadioBoxOptionEditorModel(this.editor.editorModel.validationErrorModel);
		this.options.push(model.buildForm({ rootPath: path }));
		this.options.markAsDirty();
		setTimeout(() => (document.getElementById(this.inputId(index, 'label')) as HTMLInputElement)?.focus());
	}

	removeOption(index: number): void {
		const option = this.options.at(index) as UntypedFormGroup;
		const value = option?.get('value').value;
		if (value != null && value !== '') this.replaceValueEverywhere(value, null);
		this.options.removeAt(index);
		const params = { formGroup: this.data, validationErrorModel: this.editor.editorModel.validationErrorModel, rootPath: `${this.rootPath}data.` };
		if (this.isDropdown) FieldSelectDataEditorModel.reapplySelectValidators(params); else FieldRadioBoxDataEditorModel.reapplyRadioBoxValidators(params);
		this.options.markAsDirty();
		this.options.updateValueAndValidity();
	}

	/** A label typed with no stored value yet gets one derived from it, so most admins never touch the value. */
	onLabelBlur(option: UntypedFormGroup): void {
		const label: string = option.get('label').value ?? '';
		const value = option.get('value');
		if (label.trim() && !value.value) { value.setValue(this.slug(label)); value.markAsDirty(); }
	}

	onValueFocus(option: UntypedFormGroup): void { this.valueOnFocus = option.get('value').value; }

	onValueChange(option: UntypedFormGroup): void {
		const next = option.get('value').value;
		if (this.valueOnFocus != null && this.valueOnFocus !== '' && next !== this.valueOnFocus) this.replaceValueEverywhere(this.valueOnFocus, next || null);
		this.valueOnFocus = next;
	}

	// ---- answer keys: what choosing an option stands for

	keysOf(option: AbstractControl): UntypedFormArray { return option.get('semantics') as UntypedFormArray; }
	keyCount(option: AbstractControl): number { return (this.keysOf(option)?.value ?? []).filter((x: any) => x?.key).length; }
	exportsNothing(option: AbstractControl): boolean { return !!option.get('exportsNothing')?.value; }
	/** The object the question (or its section) describes, by its last name, e.g. "distribution". */
	get objectName(): string { const o = this.scope?.[0]; return o ? o.slice(o.lastIndexOf('.') + 1) : null; }
	toggleKeys(option: AbstractControl): void { if (this.openKeys.has(option)) this.openKeys.delete(option); else this.openKeys.add(option); }

	addKey(option: AbstractControl, optionIndex: number): void {
		const rows = this.keysOf(option);
		if (!rows) return;
		rows.push(new SemanticValueEditorModel(this.editor.editorModel.validationErrorModel).buildForm({ rootPath: `${this.rootPath}data.options[${optionIndex}].semantics[${rows.length}].` }));
		rows.markAsDirty();
	}

	removeKey(option: AbstractControl, index: number): void {
		const rows = this.keysOf(option);
		rows?.removeAt(index);
		rows?.markAsDirty();
	}

	setExportsNothing(option: AbstractControl, on: boolean): void {
		option.get('exportsNothing')?.setValue(on);
		option.get('exportsNothing')?.markAsDirty();
	}

	/** The values a plugin accepts for the key (Choice format), for a dropdown; empty for free text. */
	valuesFor(key: string): string[] { return key ? [...new Set(usagesOfKey(this.editor.knownUsages(), key).flatMap(x => x.usage.values ?? []))] : []; }
	/** What the plugin calls the value, e.g. "Host › URL". */
	labelFor(key: string): string { return key ? usagesOfKey(this.editor.knownUsages(), key).find(x => x.usage.label)?.usage.label ?? null : null; }
	shown(key: string): string { return shortKey(key, this.scope); }

	inputId(index: number, part: 'label' | 'value'): string { return `bp2-opt-${this.fieldForm?.get('id')?.value}-${index}-${part}`; }

	/** Keeps the preselected answer and the rules of this input pointing at the renamed or removed value. */
	private replaceValueEverywhere(from: string, to: string): void {
		if (this.defaultControl?.value === from) { this.defaultControl.setValue(to); this.defaultControl.markAsDirty(); }
		((this.fieldForm.get('visibilityRules') as UntypedFormArray)?.controls ?? []).forEach(rule => {
			if (rule.get('textValue')?.value === from) { rule.get('textValue').setValue(to); rule.markAsDirty(); }
		});
	}

	private slug(label: string): string {
		return label.normalize('NFKD').replace(/[̀-ͯ]/g, '').toLowerCase().trim().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '') || label.trim();
	}
}
