import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { TranslateService } from '@ngx-translate/core';
import { BlueprintStructureHelper } from './structure.helper';

export type EditorTab = 'details' | 'structure' | 'review';

export interface StructureProblem {
	id: string;
	severity: 'blocker' | 'optional' | 'ok';
	/** Blockers that also stop a plain Save (form validity). Finalize-only rules leave this false. */
	blocksSave: boolean;
	message: string;
	where: string;
	tab: EditorTab;
	entryId?: string;
	questionId?: string;
	action?: 'add-presets';
}

/**
 * The problems both structure editors share: required presets, and every section, question and input in the
 * outline, read from the validators the form already runs. `term` maps a key to the editor's own wording.
 */
function compute(params: { sections: UntypedFormArray, entries: ToCEntry[], requiredPresets: SystemFieldType[], language: TranslateService, enumUtils: EnumUtils, term: (key: string) => string }): StructureProblem[] {
	const { sections, entries, requiredPresets, language, enumUtils, term } = params;
	const t = (key: string, args?: any) => language.instant(term(key), args);
	const problems: StructureProblem[] = [];
	const structure = t('PLAN-BLUEPRINT-EDITOR.TABS.STRUCTURE');

	// ---- presets
	const present = BlueprintStructureHelper.presetsIn(sections);
	const missing = requiredPresets.filter(p => !present.includes(p));
	if (missing.length) {
		problems.push({
			id: 'presets', severity: 'blocker', blocksSave: true, tab: 'structure', action: 'add-presets', where: structure,
			message: t(missing.length === 1 ? 'PLAN-BLUEPRINT-EDITOR.PROBLEMS.PRESET-MISSING' : 'PLAN-BLUEPRINT-EDITOR.PROBLEMS.PRESETS-MISSING', { names: missing.map(m => enumUtils.toSystemFieldTypeString(m)).join(', ') })
		});
	} else {
		problems.push({ id: 'presets', severity: 'ok', blocksSave: false, tab: 'structure', where: structure, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.PRESETS-OK') });
	}

	if (!sections?.length) problems.push({ id: 'no-sections', severity: 'blocker', blocksSave: true, tab: 'structure', where: structure, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NO-SECTIONS') });
	const backendSections = sections?.getError('backendError');
	if (backendSections) problems.push({ id: 'sections-be', severity: 'blocker', blocksSave: true, tab: 'structure', where: structure, message: backendSections.message });

	// ---- sections & questions
	let questionsWithoutInput = 0;
	const visit = (entry: ToCEntry, parentSection: ToCEntry) => {
		const where = `${structure} › ${entry.numbering} ${entry.label || t(entry.type === ToCEntryType.Section ? 'PLAN-BLUEPRINT-EDITOR.UNTITLED-SECTION' : 'PLAN-BLUEPRINT-EDITOR.UNTITLED-QUESTION')}`;
		const form = entry.form as UntypedFormGroup;
		if (entry.type === ToCEntryType.Section) {
			if (form.get('title')?.invalid) problems.push({ id: `title-${entry.id}`, severity: 'blocker', blocksSave: true, tab: 'structure', entryId: entry.id, where, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.SECTION-TITLE') });
			if (form.hasError('sectionMustHaveOneChildOrTemplate') || form.hasError('sectionMustHaveOneChild')) problems.push({ id: `empty-${entry.id}`, severity: 'blocker', blocksSave: true, tab: 'structure', entryId: entry.id, where, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.SECTION-EMPTY') });
			const templates = form.get('descriptionTemplates') as UntypedFormArray;
			if (form.get('hasTemplates')?.value && templates?.controls?.some(c => c.invalid)) problems.push({ id: `templates-${entry.id}`, severity: 'blocker', blocksSave: true, tab: 'structure', entryId: entry.id, where, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.TEMPLATE-ROW') });
			entry.subEntries?.forEach(child => visit(child, entry));
			return;
		}
		const sectionId = parentSection?.id;
		const at = { tab: 'structure' as EditorTab, entryId: sectionId, questionId: entry.id, where, severity: 'blocker' as const, blocksSave: true };
		if (form.get('title')?.invalid) problems.push({ ...at, id: `qtitle-${entry.id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.QUESTION-TITLE') });
		const fields = form.get('fields') as UntypedFormArray;
		if (!fields?.length) { questionsWithoutInput++; problems.push({ ...at, id: `noinput-${entry.id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.QUESTION-NO-INPUT') }); }
		fields?.controls?.forEach((field, index) => {
			const data = field.get('data') as UntypedFormGroup;
			const type = data?.get('fieldType')?.value as FieldType;
			const n = fields.length > 1 ? ` (${index + 1})` : '';
			const id = field.get('id').value;
			if (!type) problems.push({ ...at, id: `type-${id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.INPUT-NO-TYPE') + n });
			else if (type === FieldType.SYSTEM && data.get('systemFieldType')?.invalid) problems.push({ ...at, id: `preset-${id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.INPUT-NO-PRESET') + n });
			else if (type === FieldType.REFERENCE_TYPES && data.get('referenceTypeId')?.invalid) problems.push({ ...at, id: `ref-${id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.INPUT-NO-REFERENCE-TYPE') + n });
			else if ((type === FieldType.SELECT || type === FieldType.RADIO_BOX) && data.get('options')?.invalid) problems.push({ ...at, id: `opts-${id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.INPUT-OPTIONS') + n });
			else if (type === FieldType.UPLOAD && data.get('types')?.invalid) problems.push({ ...at, id: `upl-${id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.INPUT-UPLOAD-TYPES') + n });
			else if (field.invalid) problems.push({ ...at, id: `inv-${id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.INPUT-INVALID') + n });
		});
		if (form.get('hasMultiplicity')?.value && form.get('multiplicity')?.invalid) problems.push({ ...at, id: `mult-${entry.id}`, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.REPEATABLE') });
	};
	entries?.forEach(e => visit(e, null));
	if (entries?.length && !questionsWithoutInput && !problems.some(p => p.id.startsWith('qtitle-') || p.id.startsWith('type-') || p.id.startsWith('inv-'))) {
		problems.push({ id: 'questions-ok', severity: 'ok', blocksSave: false, tab: 'structure', where: structure, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.QUESTIONS-OK') });
	}
	return problems;
}

/** A backend validation message on a details control, as a blocker. */
function backendOn(problems: StructureProblem[], control: any, id: string, label: string, where: string): void {
	if (control?.hasError('backendError')) problems.push({ id, severity: 'blocker', blocksSave: true, message: `${label}: ${control.getError('backendError').message}`, where, tab: 'details' });
}

export const StructureProblemsHelper = {
	compute,
	backendOn
};
