import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { FieldValidationType } from '@app/core/common/enum/field-validation-type';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BlueprintStructureHelper } from '../../structure.helper';
import { defaultValueText, InputKind, kindIcon, kindLabelKey, kindOf, RuleLink } from '../../input-kinds';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

/** A question as plain text, for blueprints that cannot be edited: what it asks and how, with no form controls. */
@Component({
    selector: 'app-bp2-question-read',
    templateUrl: './question-read.component.html',
    styleUrls: ['./question-read.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, TranslatePipe]
})
export class Bp2QuestionReadComponent {
	@Input() entry: ToCEntry;

	readonly kindIcon = kindIcon;
	readonly kindLabelKey = kindLabelKey;

	constructor(public editor: StructureEditorBase<any, any>) { }

	get form(): UntypedFormGroup { return this.entry?.form as UntypedFormGroup; }
	get fields(): UntypedFormGroup[] { return ((this.form?.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[]; }
	get isPreset(): boolean { return BlueprintStructureHelper.isPresetQuestion(this.form); }
	get multiplicity(): UntypedFormGroup { return this.form?.get('multiplicity') as UntypedFormGroup; }
	get repeatable(): boolean { return !!this.form?.get('hasMultiplicity')?.value; }
	get into(): RuleLink[] { return this.editor.rulesInto(this.form); }

	kind(f: UntypedFormGroup): InputKind { return kindOf(f); }
	data(f: UntypedFormGroup): UntypedFormGroup { return f.get('data') as UntypedFormGroup; }
	type(f: UntypedFormGroup): FieldType { return BlueprintStructureHelper.fieldType(f); }
	required(f: UntypedFormGroup): boolean { return BlueprintStructureHelper.hasValidation(f, FieldValidationType.Required); }
	url(f: UntypedFormGroup): boolean { return BlueprintStructureHelper.hasValidation(f, FieldValidationType.Url); }
	email(f: UntypedFormGroup): boolean { return BlueprintStructureHelper.hasValidation(f, FieldValidationType.Email); }
	options(f: UntypedFormGroup): { label: string, value: string }[] { return (this.data(f)?.get('options') as UntypedFormArray)?.getRawValue() ?? []; }
	types(f: UntypedFormGroup): { label: string, value: string }[] { return (this.data(f)?.get('types') as UntypedFormArray)?.getRawValue() ?? []; }
	semantics(f: UntypedFormGroup): string[] { return BlueprintStructureHelper.semanticsOf(f); }
	defaultText(f: UntypedFormGroup): string { return defaultValueText(f, this.editor.language); }
	rules(f: UntypedFormGroup): RuleLink[] { return this.editor.rulesFromField(f); }
	isDefault(f: UntypedFormGroup, value: string): boolean { return value != null && f.get('defaultValue')?.get('textValue')?.value === value; }
	presetName(f: UntypedFormGroup): string { return this.editor.enumUtils.toSystemFieldTypeString(BlueprintStructureHelper.systemFieldType(f)); }
	referenceName(f: UntypedFormGroup): string {
		const id = this.data(f)?.get('referenceTypeId')?.value;
		return this.editor.availableReferenceTypes.find(r => r.id?.toString() === id?.toString())?.name ?? null;
	}
	prefix(key: string): string { return key.includes('.') ? key.split('.')[0] : key; }
	rest(key: string): string { return key.includes('.') ? key.slice(key.indexOf('.') + 1) : ''; }
}
