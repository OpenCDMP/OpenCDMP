import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { FieldValidationType } from '@app/core/common/enum/field-validation-type';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BlueprintStructureHelper } from '../../structure.helper';
import { defaultTypeFor, defaultValueText, InputKind, KIND_GROUPS, kindHintKey, kindIcon, kindLabelKey, kindOf, RuleLink } from '../../input-kinds';
import { StructureEditorBase } from '../../structure-editor-base';
import { objectContext, shortKey } from '../../semantic-objects';
import { SemanticCheck } from '../../semantic-checks';
import { MatFormField, MatLabel, MatHint, MatError } from '@angular/material/form-field';
import { MatSelect, MatSelectTrigger, MatOption, MatOptgroup } from '@angular/material/select';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MatIcon } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIconButton } from '@angular/material/button';
import { Bp2OptionsEditorComponent } from '../options-editor/options-editor.component';
import { SingleAutoCompleteComponent } from '../../../../../library/auto-complete/single/single-auto-complete.component';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { Bp2UploadSettingsComponent } from '../upload-settings/upload-settings.component';
import { MatInput } from '@angular/material/input';
import { Bp2SemanticsPickerComponent } from '../semantics-picker/semantics-picker.component';
import { DescriptionTemplateEditorDefaultValueComponent } from '../../../description-template/editor/components/default-value/description-template-editor-default-value.component';
import { Bp2RulesEditorComponent } from '../rules-editor/rules-editor.component';
import { TranslatePipe } from '@ngx-translate/core';

/**
 * One input of a question: its kind, its validations as pills, only the settings that kind needs, and the
 * cross-cutting settings as chips that open inline (semantics, download, default value, conditions).
 */
@Component({
    selector: 'app-bp2-input-row',
    templateUrl: './input-row.component.html',
    styleUrls: ['./input-row.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatFormField, MatSelect, ReactiveAsteriskDirective, MatSelectTrigger, MatIcon, MatOption, MatOptgroup, MatTooltip, MatIconButton, Bp2OptionsEditorComponent, MatLabel, SingleAutoCompleteComponent, FormsModule, ReactiveFormsModule, MatHint, MatError, MatSlideToggle, MatButtonToggleGroup, MatButtonToggle, Bp2UploadSettingsComponent, MatInput, Bp2SemanticsPickerComponent, DescriptionTemplateEditorDefaultValueComponent, Bp2RulesEditorComponent, TranslatePipe]
})
export class Bp2InputRowComponent {
	@Input() fieldForm: UntypedFormGroup;
	@Input() entry: ToCEntry;
	@Input() index = 0;
	@Input() count = 1;
	@Input() rootPath = '';
	@Output() changed = new EventEmitter<void>();
	@Output() remove = new EventEmitter<void>();
	@Output() move = new EventEmitter<-1 | 1>();

	showSemantics = false;
	showDefault = false;
	showRules = false;

	readonly FieldType = FieldType;
	readonly FieldValidationType = FieldValidationType;
	readonly kindGroups = KIND_GROUPS;
	readonly kindIcon = kindIcon;
	readonly kindLabelKey = kindLabelKey;
	readonly kindHintKey = kindHintKey;

	constructor(public editor: StructureEditorBase<any, any>, public referenceTypeService: ReferenceTypeService) { }

	get readOnly(): boolean { return this.editor.readOnly; }
	get data(): UntypedFormGroup { return this.fieldForm?.get('data') as UntypedFormGroup; }
	get type(): FieldType { return BlueprintStructureHelper.fieldType(this.fieldForm); }
	get kind(): InputKind { return kindOf(this.fieldForm); }
	get isPreset(): boolean { return this.type === FieldType.SYSTEM; }
	get preset(): SystemFieldType { return BlueprintStructureHelper.systemFieldType(this.fieldForm); }
	get presetOptions(): SystemFieldType[] {
		const current = this.preset;
		const options = [...this.editor.requiredPresets, ...this.editor.optionalPresets];
		const used = this.editor.presentPresets.filter(p => p !== current);
		return options.filter(p => p === current || !used.includes(p));
	}
	get forcedRequired(): boolean { return this.isPreset && this.preset != null && BlueprintStructureHelper.ALWAYS_REQUIRED_PRESETS.includes(this.preset); }
	get isRequired(): boolean { return BlueprintStructureHelper.hasValidation(this.fieldForm, FieldValidationType.Required); }
	get isUrl(): boolean { return BlueprintStructureHelper.hasValidation(this.fieldForm, FieldValidationType.Url); }
	get isEmail(): boolean { return BlueprintStructureHelper.hasValidation(this.fieldForm, FieldValidationType.Email); }
	get canUrlEmail(): boolean { return this.type === FieldType.FREE_TEXT; }
	get canDefault(): boolean { return BlueprintStructureHelper.canSetDefaultValue(this.type); }
	get canRules(): boolean { return BlueprintStructureHelper.canApplyVisibility(this.type); }
	get semantics(): string[] { return BlueprintStructureHelper.semanticsOf(this.fieldForm); }
	/** The objects around this input (its question's, then its sections'), innermost first. */
	get scope(): string[] { return objectContext(this.editor.toCEntries, this.entry?.form as UntypedFormGroup); }
	/** A key as shown: shortened under an object around the input; the full key is stored. */
	shown(key: string): string { return shortKey(key, this.scope); }
	/** What the plugins say about this input's keys. */
	get checks(): SemanticCheck[] { return this.editor.checksFor(this.fieldForm); }
	get problems(): SemanticCheck[] { return this.checks.filter(c => !c.notice); }
	get includeInExport(): boolean { return !!this.fieldForm?.get('includeInExport')?.value; }
	get rules(): UntypedFormArray { return this.fieldForm?.get('visibilityRules') as UntypedFormArray; }
	get ruleCount(): number { return this.rules?.length ?? 0; }
	get links(): RuleLink[] { return this.editor.rulesFromField(this.fieldForm); }
	get defaultControlName(): string { return BlueprintStructureHelper.defaultValueControlName(this.type); }
	get defaultText(): string { return defaultValueText(this.fieldForm, this.editor.language); }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }
	get fieldSetId(): string { return this.entry?.form?.get('id')?.value; }
	get multiple(): boolean { return !!this.data?.get('multipleSelect')?.value; }
	get isDropdown(): boolean { return this.type === FieldType.SELECT; }
	get linksAssets(): boolean { return this.type === FieldType.INTERNAL_ENTRIES_DESCRIPTIONS; }
	get validatesIdentifier(): boolean { return this.type === FieldType.VALIDATION; }
	get inputLabel(): string { return this.entry?.label; }
	/** What the plain `label` means for this kind, as authors meet it. */
	get labelKey(): string {
		if (this.kind === 'checkbox') return 'PLAN-BLUEPRINT-EDITOR.INPUT.CHECKBOX-TEXT';
		if (this.kind === 'yesno' || (this.kind === 'single' && !this.isDropdown)) return 'PLAN-BLUEPRINT-EDITOR.INPUT.LABEL-AUTHORS-SEE';
		return 'PLAN-BLUEPRINT-EDITOR.INPUT.PLACEHOLDER';
	}

	/** The rules chip reads as a sentence once there is one: "Shows 2.2 when “Other”". */
	get rulesChipText(): string {
		const links = this.links.filter(l => l.targetId);
		if (!links.length) return null;
		const first = links[0];
		const text = first.value
			? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.SHOWS-WHEN', { target: first.targetNumbering, value: first.value })
			: this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.SHOWS', { target: first.targetNumbering });
		return links.length > 1 ? `${text} +${links.length - 1}` : text;
	}

	onKindChange(kind: InputKind): void {
		if (!kind || kind === this.kind) return;
		const multiple = kind === 'multiple' ? true : (kind === 'single' ? false : undefined);
		// single and multiple choice share the select type: switching between them only flips "more than one"
		if ((kind === 'single' || kind === 'multiple') && this.type === FieldType.SELECT) {
			this.data.get('multipleSelect').setValue(kind === 'multiple');
			this.data.get('multipleSelect').markAsDirty();
			this.changed.emit();
			return;
		}
		const carry = (kind === 'multiple' && this.type === FieldType.RADIO_BOX);
		this.rebuild(kind === 'multiple' ? FieldType.SELECT : defaultTypeFor(kind), carry, multiple);
		this.showDefault = false; this.showRules = false;
	}

	/** Radio buttons or a dropdown: two stored types, one kind. Options, the default and the rules carry over. */
	setDropdown(on: boolean): void { this.rebuild(on ? FieldType.SELECT : FieldType.RADIO_BOX, true, on ? false : undefined); }
	setLinkTo(assets: boolean): void { this.rebuild(assets ? FieldType.INTERNAL_ENTRIES_DESCRIPTIONS : FieldType.INTERNAL_ENTRIES_PLANS, true); }
	setValidateIdentifier(on: boolean): void { this.rebuild(on ? FieldType.VALIDATION : FieldType.DATASET_IDENTIFIER, true); }

	setMultiple(on: boolean): void {
		this.data.get('multipleSelect').setValue(on);
		this.data.get('multipleSelect').markAsDirty();
	}

	private rebuild(type: FieldType, carry: boolean, multipleSelect?: boolean): void {
		if (type === this.type && multipleSelect == null) return;
		this.fieldForm = BlueprintStructureHelper.rebuildFieldForType({ fieldForm: this.fieldForm, type, validationErrorModel: this.editor.editorModel.validationErrorModel, rootPath: this.rootPath, maxFileSizeInMB: this.editor.configurationService.maxFileSizeInMB, carry, multipleSelect });
		this.fieldForm.markAsDirty();
		this.changed.emit();
	}

	onPresetChange(preset: SystemFieldType): void {
		this.data.get('systemFieldType').setValue(preset);
		this.data.get('systemFieldType').markAsDirty();
		if (BlueprintStructureHelper.ALWAYS_REQUIRED_PRESETS.includes(preset)) BlueprintStructureHelper.setValidation(this.fieldForm, FieldValidationType.Required, true);
		const label = this.data.get('label');
		if (!label.value) label.setValue(this.editor.enumUtils.toSystemFieldTypeString(preset));
		const title = this.entry?.form?.get('title');
		if (title && !title.value) title.setValue(this.editor.enumUtils.toSystemFieldTypeString(preset));
		this.changed.emit();
	}

	togglePill(validation: FieldValidationType): void {
		if (this.readOnly || (validation === FieldValidationType.Required && this.forcedRequired)) return;
		BlueprintStructureHelper.setValidation(this.fieldForm, validation, !BlueprintStructureHelper.hasValidation(this.fieldForm, validation));
	}

	toggleExport(): void {
		if (this.readOnly) return;
		const c = this.fieldForm.get('includeInExport');
		c.setValue(!c.value); c.markAsDirty();
	}

	openRules(): void {
		this.showRules = !this.showRules;
	}

	presetName(p: SystemFieldType): string { return this.editor.enumUtils.toSystemFieldTypeString(p); }
	prefix(key: string): string { return key.includes('.') ? key.split('.')[0] : key; }
	rest(key: string): string { return key.includes('.') ? key.slice(key.indexOf('.') + 1) : ''; }
}
