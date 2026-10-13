import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { RuleEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BlueprintStructureHelper } from '../../structure.helper';
import { InputKind, kindHasOptions, kindOf, questionIndex } from '../../input-kinds';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatFormField, MatError } from '@angular/material/form-field';
import { MatSelect, MatOption, MatSelectTrigger, MatOptgroup } from '@angular/material/select';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

interface TargetOption { id: string, label: string, disabled: boolean, reason: string }

/**
 * The rules of one input, read as sentences: "When <answer> show <question>". The value control follows the
 * input's kind. Targets that cannot be picked stay in the list, greyed, with the reason, instead of vanishing.
 */
@Component({
    selector: 'app-bp2-rules-editor',
    templateUrl: './rules-editor.component.html',
    styleUrls: ['./rules-editor.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatFormField, MatSelect, ReactiveAsteriskDirective, FormsModule, ReactiveFormsModule, MatOption, MatSelectTrigger, MatOptgroup, MatError, MatIconButton, MatTooltip, MatIcon, MatButton, TranslatePipe]
})
export class Bp2RulesEditorComponent {
	@Input() fieldForm: UntypedFormGroup;
	@Input() entry: ToCEntry;
	@Input() rootPath = '';

	questionTargets: TargetOption[] = [];
	inputTargets: TargetOption[] = [];

	constructor(public editor: StructureEditorBase<any, any>) { }

	get readOnly(): boolean { return this.editor.readOnly; }
	get rules(): UntypedFormArray { return this.fieldForm?.get('visibilityRules') as UntypedFormArray; }
	get kind(): InputKind { return kindOf(this.fieldForm); }
	get hasOptions(): boolean { return kindHasOptions(this.kind); }
	get isBoolean(): boolean { return this.kind === 'yesno' || this.kind === 'checkbox'; }
	get isDate(): boolean { return this.kind === 'date'; }
	get isNumber(): boolean { return this.kind === 'number'; }
	get options(): { label: string, value: string }[] { return ((this.fieldForm?.get('data')?.get('options') as UntypedFormArray)?.value ?? []).filter((o: { value: string }) => o.value); }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }

	/** Recomputed whenever a target list opens, so it always reflects the current structure. */
	computeTargets(): void {
		const ownQuestionId: string = this.entry?.form?.get('id')?.value;
		const ownFieldId: string = this.fieldForm?.get('id')?.value;
		const questions = questionIndex(this.editor.toCEntries);
		// questions that already show this one: pointing back at them would make each depend on the other
		const showsThis = new Set<string>();
		questions.forEach(q => ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []).forEach(f => {
			((f.get('visibilityRules') as UntypedFormArray)?.controls ?? []).forEach(r => {
				const t = r.get('target')?.value;
				if (t && (t === ownQuestionId || t === ownFieldId)) showsThis.add(q.id);
			});
		}));
		this.questionTargets = []; this.inputTargets = [];
		questions.forEach(q => {
			if (BlueprintStructureHelper.isPresetQuestion(q.fieldSetForm)) return;
			const title = q.title || this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.UNTITLED-QUESTION');
			const own = q.id === ownQuestionId;
			const loop = showsThis.has(q.id);
			this.questionTargets.push({
				id: q.id, label: `${q.numbering} ${title}`, disabled: own || loop,
				reason: own ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.CONTAINS-THIS') : loop ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.SHOWS-THIS') : ''
			});
			const fields = ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
			if (fields.length > 1) fields.forEach((f, i) => {
				if (BlueprintStructureHelper.fieldType(f) === FieldType.SYSTEM) return;
				const label = f.get('data')?.get('label')?.value || this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.INPUT.INPUT-N', { n: i + 1 });
				this.inputTargets.push({
					id: f.get('id').value, label: `${q.numbering} › ${label}`, disabled: own || loop,
					reason: own ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.SAME-QUESTION') : loop ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.SHOWS-THIS') : ''
				});
			});
		});
	}

	targetLabel(id: string): string {
		if (!id) return null;
		if (!this.questionTargets.length && !this.inputTargets.length) this.computeTargets();
		return [...this.questionTargets, ...this.inputTargets].find(t => t.id === id)?.label ?? id;
	}

	addRule(): void {
		const index = this.rules.length;
		this.rules.push(new RuleEditorModel(this.editor.editorModel.validationErrorModel).buildForm({ rootPath: `${this.rootPath}visibilityRules[${index}].` }));
		this.rules.markAsDirty();
		this.computeTargets();
	}

	removeRule(index: number): void {
		this.rules.removeAt(index);
		this.rules.controls.forEach((control, i) => RuleEditorModel.reapplyValidators({ formGroup: control as UntypedFormGroup, rootPath: `${this.rootPath}visibilityRules[${i}].`, validationErrorModel: this.editor.editorModel.validationErrorModel }));
		this.rules.markAsDirty();
		this.editor.onFieldTypeChanged();
	}

	onTargetChange(): void { this.editor.onFieldTypeChanged(); }

	/** The date control stores a Date; the native date input speaks yyyy-mm-dd. */
	dateString(rule: UntypedFormGroup): string {
		const v = rule.get('dateValue')?.value;
		if (!v) return '';
		const d = new Date(v);
		return isNaN(d.getTime()) ? '' : d.toISOString().slice(0, 10);
	}
	setDate(rule: UntypedFormGroup, value: string): void {
		rule.get('dateValue').setValue(value ? new Date(value) : null);
		rule.get('dateValue').markAsDirty();
	}
}
