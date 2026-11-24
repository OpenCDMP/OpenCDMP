import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { RuleEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BlueprintStructureHelper } from '../../structure.helper';
import { InputKind, kindHasOptions, kindOf, questionIndex } from '../../input-kinds';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatFormField, MatError, MatHint } from '@angular/material/form-field';
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
    imports: [MatFormField, MatSelect, ReactiveAsteriskDirective, FormsModule, ReactiveFormsModule, MatOption, MatSelectTrigger, MatOptgroup, MatError, MatHint, MatIconButton, MatTooltip, MatIcon, MatButton, TranslatePipe]
})
export class Bp2RulesEditorComponent {
	@Input() fieldForm: UntypedFormGroup;
	@Input() entry: ToCEntry;
	@Input() rootPath = '';

	questionTargets: TargetOption[] = [];
	inputTargets: TargetOption[] = [];
	/** Only sections a rule already targets: the old editor offered sections, this one doesn't. */
	sectionTargets: TargetOption[] = [];

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
		// inputs that already show this one: pointing back at them would make each depend on the other
		const showsThis = new Set<string>();
		questions.forEach(q => ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []).forEach(f => {
			((f.get('visibilityRules') as UntypedFormArray)?.controls ?? []).forEach(r => {
				const t = r.get('target')?.value;
				if (t && (t === ownQuestionId || t === ownFieldId)) showsThis.add(f.get('id').value);
			});
		}));
		const targeted = new Set<string>((this.rules?.controls ?? []).map(r => r.get('target').value).filter(t => !!t));
		this.questionTargets = []; this.inputTargets = []; this.sectionTargets = [];
		const visitSections = (list: ToCEntry[]) => list?.forEach(e => {
			if (e.type !== ToCEntryType.Section) return;
			if (targeted.has(e.id)) this.sectionTargets.push({ id: e.id, label: `${e.numbering} ${e.label || this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.UNTITLED-SECTION')}`, disabled: false, reason: '' });
			visitSections(e.subEntries);
		});
		visitSections(this.editor.toCEntries);
		questions.forEach(q => {
			if (BlueprintStructureHelper.isPresetQuestion(q.fieldSetForm)) return;
			const title = q.title || this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.UNTITLED-QUESTION');
			const fields = ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
			const own = q.id === ownQuestionId;
			const loop = fields.some(f => showsThis.has(f.get('id').value));
			this.questionTargets.push({
				id: q.id, label: `${q.numbering} ${title}`, disabled: own || loop,
				reason: own ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.CONTAINS-THIS') : loop ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.SHOWS-THIS') : ''
			});
			// an input of the same question is a valid target (the backend and the runtime only reject the input itself);
			// the input of a single-input question is listed only when a rule already targets it, so that rule still shows its target
			if (fields.length > 1 || fields.some(f => targeted.has(f.get('id').value))) fields.forEach((f, i) => {
				if (BlueprintStructureHelper.fieldType(f) === FieldType.SYSTEM) return;
				const id: string = f.get('id').value;
				const label = f.get('data')?.get('label')?.value || this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.INPUT.INPUT-N', { n: i + 1 });
				const self = id === ownFieldId;
				const inputLoop = showsThis.has(id);
				this.inputTargets.push({
					id, label: `${q.numbering} › ${label}`, disabled: self || inputLoop,
					reason: self ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.THIS-INPUT') : inputLoop ? this.editor.language.instant('PLAN-BLUEPRINT-EDITOR.RULES.SHOWS-THIS') : ''
				});
			});
		});
	}

	/** The target id is set but no question or input has it any more, e.g. the question was deleted. */
	targetMissing(rule: UntypedFormGroup): boolean {
		const id: string = rule.get('target').value;
		const ownFieldId: string = this.fieldForm?.get('id')?.value;
		return !!id && this.editor.ruleLinks.some(l => l.sourceFieldId === ownFieldId && l.targetId === id && l.targetMissing);
	}

	targetLabel(id: string): string {
		if (!id) return null;
		if (!this.questionTargets.length && !this.inputTargets.length) this.computeTargets();
		return [...this.questionTargets, ...this.inputTargets, ...this.sectionTargets].find(t => t.id === id)?.label ?? id;
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
