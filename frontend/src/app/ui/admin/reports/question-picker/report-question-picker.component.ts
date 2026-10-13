import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, input, output, signal } from '@angular/core';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ReportOutline, ReportOutlineField, ReportOutlineFieldSet, ReportOutlineSection, SelectedQuestion } from '@app/core/model/report/report';
import { TranslateModule } from '@ngx-translate/core';
import { defaultCountedFields, fieldTypeWord, isControlField } from '../report-state';

/**
 * Shows a template or blueprint as its numbered question tree and lets the user pick the
 * questions to count — the browse route into a report, for when the number of a question
 * is known but not its wording, or when several questions are wanted at once.
 *
 * <p>Ticking a question selects the fields that carry answers. Selection stays per field
 * underneath, because a question such as FCT 5.2.4 holds a reference field, the checkbox
 * that reveals a free-text fallback, and the fallback itself: counting all three would mix
 * "Yes" into a list of repositories.</p>
 */
@Component({
	selector: 'app-report-question-picker',
	standalone: true,
	imports: [NgTemplateOutlet, MatCheckboxModule, MatIconModule, MatTooltipModule, TranslateModule],
	templateUrl: './report-question-picker.component.html',
	styleUrl: './report-question-picker.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportQuestionPickerComponent {

	readonly outline = input<ReportOutline>(null);
	/** Questions already chosen — restored from a link, say — shown ticked. */
	readonly initialSelection = input<SelectedQuestion[]>([]);
	readonly selectionChanged = output<SelectedQuestion[]>();

	/** fieldSetId -> selected field ids, in the order the user ticked them. */
	protected readonly selection = signal<Map<string, string[]>>(new Map());
	protected readonly collapsed = signal<Set<string>>(new Set());
	protected readonly showFields = signal<Set<string>>(new Set());

	protected readonly sections = computed(() => this.outline()?.sections ?? []);

	protected readonly selectedCount = computed(() =>
		Array.from(this.selection().values()).filter(x => x.length > 0).length);

	constructor() {
		effect(() => {
			const initial = this.initialSelection();
			const next = new Map<string, string[]>();
			for (const question of initial ?? []) {
				if (question.fieldIds?.length) next.set(question.fieldSetId, [...question.fieldIds]);
			}
			this.selection.set(next);
		});
	}

	protected isSelected(fieldSetId: string, fieldId: string): boolean {
		return (this.selection().get(fieldSetId) ?? []).includes(fieldId);
	}

	protected toggleField(fieldSet: ReportOutlineFieldSet, fieldId: string, checked: boolean): void {
		const next = new Map(this.selection());
		const current = [...(next.get(fieldSet.id) ?? [])];
		const index = current.indexOf(fieldId);

		if (checked && index < 0) current.push(fieldId);
		if (!checked && index >= 0) current.splice(index, 1);

		if (current.length === 0) next.delete(fieldSet.id);
		else next.set(fieldSet.id, current);

		this.selection.set(next);
		this.emit();
	}

	/**
	 * Whether this particular field is the one left out of a select-all. A lone gating
	 * boolean is still the question its fieldset asks — "are there metadata?" both answers
	 * something and reveals a follow-up — so it is only set aside when siblings carry the
	 * actual answers.
	 */
	protected isExcludedControl(fieldSet: ReportOutlineFieldSet, field: ReportOutlineField): boolean {
		return isControlField(field) && !this.countableFields(fieldSet).some(x => x.id === field.id);
	}

	protected typeWord(field: ReportOutlineField): string {
		return 'REPORTS.FIELD-TYPES.' + fieldTypeWord(field.fieldType);
	}

	private countableFields(fieldSet: ReportOutlineFieldSet): ReportOutlineField[] {
		return defaultCountedFields(fieldSet.fields);
	}

	/** Ticking the question selects the fields that carry answers. */
	protected toggleFieldSet(fieldSet: ReportOutlineFieldSet, checked: boolean): void {
		const next = new Map(this.selection());
		if (checked) next.set(fieldSet.id, this.countableFields(fieldSet).map(x => x.id));
		else next.delete(fieldSet.id);

		this.selection.set(next);
		this.emit();
	}

	protected isFieldSetFullySelected(fieldSet: ReportOutlineFieldSet): boolean {
		const current = this.selection().get(fieldSet.id) ?? [];
		const countable = this.countableFields(fieldSet);
		return countable.length > 0 && countable.every(x => current.includes(x.id));
	}

	protected isFieldSetPartiallySelected(fieldSet: ReportOutlineFieldSet): boolean {
		const current = this.selection().get(fieldSet.id) ?? [];
		return current.length > 0 && !this.isFieldSetFullySelected(fieldSet);
	}

	protected isCollapsed(sectionId: string): boolean {
		return this.collapsed().has(sectionId);
	}

	protected toggleSection(sectionId: string): void {
		const next = new Set(this.collapsed());
		if (next.has(sectionId)) next.delete(sectionId);
		else next.add(sectionId);
		this.collapsed.set(next);
	}

	/** Fields stay folded under their question unless someone asks to choose among them. */
	protected fieldsShown(fieldSetId: string): boolean {
		return this.showFields().has(fieldSetId);
	}

	protected toggleFields(fieldSetId: string): void {
		const next = new Set(this.showFields());
		if (next.has(fieldSetId)) next.delete(fieldSetId);
		else next.add(fieldSetId);
		this.showFields.set(next);
	}

	clear(): void {
		this.selection.set(new Map());
		this.emit();
	}

	private emit(): void {
		const fieldSets = new Map<string, ReportOutlineFieldSet>();
		this.indexFieldSets(this.sections(), fieldSets);

		const questions: SelectedQuestion[] = [];
		for (const [fieldSetId, fieldIds] of this.selection().entries()) {
			if (fieldIds.length === 0) continue;
			const fieldSet = fieldSets.get(fieldSetId);
			questions.push({
				fieldSetId: fieldSetId,
				fieldIds: fieldIds,
				numbering: fieldSet?.numbering,
				label: [fieldSet?.numbering, fieldSet?.title].filter(x => !!x).join(' ')
			});
		}
		questions.sort((left, right) => (left.numbering ?? '').localeCompare(right.numbering ?? '', undefined, { numeric: true }));
		this.selectionChanged.emit(questions);
	}

	private indexFieldSets(sections: ReportOutlineSection[], into: Map<string, ReportOutlineFieldSet>): void {
		for (const section of sections ?? []) {
			for (const fieldSet of section.fieldSets ?? []) into.set(fieldSet.id, fieldSet);
			this.indexFieldSets(section.sections, into);
		}
	}
}
