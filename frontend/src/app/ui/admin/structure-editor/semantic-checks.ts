import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { SemanticValueFormat } from '@app/core/model/semantic/plugin-semantics';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { InputKind, kindOf, questionIndex } from './input-kinds';
import { KnownObject, KnownUsage, objectContext, objectsOf, usagesOfKey } from './semantic-objects';

/**
 * Mapped, but won't come out right: checks of the structure's keys against what the plugins say they read. Only keys
 * a plugin's manifest lists are checked, never an organisation's own keys, and nothing here blocks saving.
 */

export type SemanticCheckKind = 'format' | 'choice-values' | 'answer-value' | 'reference' | 'spread' | 'shared-object' | 'not-imported';

export interface SemanticCheck {
	kind: SemanticCheckKind;
	/** A notice rather than a problem: allowed, just worth knowing. */
	notice: boolean;
	/** The key or object the check is about. */
	key: string;
	/** i18n key under `PLAN-BLUEPRINT-EDITOR.CHECKS.` and its parameters. */
	message: string;
	params: Record<string, string>;
	/** The input the check is about, when it is about one. */
	fieldForm?: UntypedFormGroup;
	/** Where to go: the questions (or sections) involved, numbered as the outline numbers them. */
	targets: { id: string, numbering: string, title: string, sectionId: string, isSection: boolean }[];
}

const FREE_TEXT: InputKind[] = ['freetext', 'textarea', 'richtext'];
const CHOICE_INPUTS: InputKind[] = ['single', 'multiple'];
const YES_NO = ['yes', 'no', 'unknown'];

function formatOf(usages: KnownUsage[]): { format: SemanticValueFormat, values: string[] } {
	const withFormat = usages.find(x => x.usage.format != null);
	const values = usages.flatMap(x => x.usage.values ?? []);
	return { format: withFormat?.usage.format ?? null, values: [...new Set(values)] };
}

/** What the input type cannot give: null when it fits. */
function formatProblem(kind: InputKind, format: SemanticValueFormat, values: string[]): string {
	if ((format === SemanticValueFormat.Integer || format === SemanticValueFormat.Number) && FREE_TEXT.includes(kind)) return format === SemanticValueFormat.Integer ? 'FORMAT-INTEGER' : 'FORMAT-NUMBER';
	if ((format === SemanticValueFormat.Date || format === SemanticValueFormat.DateTime) && FREE_TEXT.includes(kind)) return 'FORMAT-DATE-TEXT';
	if (format === SemanticValueFormat.DateTime && kind === 'date') return 'FORMAT-DATETIME';
	// the RDA export maps Yes/No itself for keys that take yes, no or unknown
	if (format === SemanticValueFormat.Choice && (kind === 'yesno' || kind === 'checkbox') && values.length && !values.every(v => YES_NO.includes(v.toLowerCase()))) return 'FORMAT-CHOICE-YESNO';
	return null;
}

function optionsOf(fieldForm: UntypedFormGroup): UntypedFormGroup[] {
	return ((fieldForm?.get('data')?.get('options') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
}

export function semanticChecks(entries: ToCEntry[], usages: KnownUsage[], objects: KnownObject[]): SemanticCheck[] {
	const result: SemanticCheck[] = [];
	if (!usages.length) return result;
	const questions = questionIndex(entries);
	const sections: { id: string, numbering: string, title: string, form: UntypedFormGroup }[] = [];
	const visit = (list: ToCEntry[]) => list?.forEach(e => { if (e.type !== ToCEntryType.FieldSet) { sections.push({ id: e.id, numbering: e.numbering, title: e.label, form: e.form as UntypedFormGroup }); visit(e.subEntries); } });
	visit(entries);
	const objectMap = new Map(objects.map(o => [o.key, o] as [string, KnownObject]));
	const notImported = new Set<string>();

	// the list object a value ends up in: the outermost object on its parent chain that the output holds a list of
	const listOf = (object: string): string => {
		let list: string = null;
		for (let o = objectMap.get(object); o; o = o.parent ? objectMap.get(o.parent) : null) if (o.multiple) list = o.key;
		return list;
	};
	// per list object, the questions filling it outside any object, with the values each fills
	const spread = new Map<string, Map<string, { q: typeof questions[number], values: Set<string> }>>();

	questions.forEach(q => {
		const target = { id: q.id, numbering: q.numbering, title: q.title, sectionId: q.sectionIds[q.sectionIds.length - 1] ?? null, isSection: false };
		const context = objectContext(entries, q.fieldSetForm);
		const fields = ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
		/** The object and the value each known key of the question fills. */
		const valueObjects: { object: string, value: string }[] = [];
		fields.forEach(field => {
			const kind = kindOf(field);
			((field.get('semantics')?.value ?? []) as string[]).filter(Boolean).forEach(key => {
				const known = usagesOfKey(usages, key);
				if (!known.length) return;
				known.forEach(x => { if (x.usage.object && !(x.usage.keys ?? []).includes(x.usage.object)) valueObjects.push({ object: x.usage.object, value: x.usage.target || x.usage.keys?.[0] }); });
				const { format, values } = formatOf(known);
				const problem = formatProblem(kind, format, values);
				// a date where a date and time is wanted is read as midnight, so it is worth knowing but works
				if (problem) result.push({ kind: 'format', notice: problem === 'FORMAT-DATETIME', key, message: problem, params: { key, values: values.join(', ') }, fieldForm: field, targets: [target] });
				if (format === SemanticValueFormat.Reference && kind !== 'reference') result.push({ kind: 'reference', notice: false, key, message: 'REFERENCE', params: { key }, fieldForm: field, targets: [target] });
				if (CHOICE_INPUTS.includes(kind) && values.length) {
					const outside = optionsOf(field).map(o => o.get('value')?.value as string).filter(v => v != null && v !== '' && !values.includes(v));
					if (outside.length) result.push({ kind: 'choice-values', notice: false, key, message: 'CHOICE-VALUES', params: { key, outside: outside.join(', '), values: values.join(', ') }, fieldForm: field, targets: [target] });
				}
				if (known.some(x => x.usage.imported === false) && !notImported.has(key)) {
					notImported.add(key);
					result.push({ kind: 'not-imported', notice: true, key, message: 'NOT-IMPORTED', params: { key }, fieldForm: field, targets: [target] });
				}
			});
			optionsOf(field).forEach(option => ((option.get('semantics')?.value ?? []) as { key: string, value: string }[]).filter(x => x?.key).forEach(row => {
				const known = usagesOfKey(usages, row.key);
				if (!known.length) return;
				known.forEach(x => { if (x.usage.object && !(x.usage.keys ?? []).includes(x.usage.object)) valueObjects.push({ object: x.usage.object, value: x.usage.target || x.usage.keys?.[0] }); });
				const { values } = formatOf(known);
				if (values.length && row.value != null && row.value !== '' && !values.includes(row.value)) {
					result.push({ kind: 'answer-value', notice: false, key: row.key, message: 'ANSWER-VALUE', params: { key: row.key, value: row.value, answer: option.get('label')?.value || option.get('value')?.value || '', values: values.join(', ') }, fieldForm: field, targets: [target] });
				}
			}));
		});
		// values of a list object outside any object that holds them together
		valueObjects.forEach(({ object, value }) => {
			const list = listOf(object);
			if (!list || context.some(c => c === list || list.startsWith(c + '.'))) return;
			if (!spread.has(list)) spread.set(list, new Map());
			const byQuestion = spread.get(list);
			if (!byQuestion.has(q.id)) byQuestion.set(q.id, { q, values: new Set() });
			byQuestion.get(q.id).values.add(value);
		});
	});

	// questions sharing a value each give a whole object on purpose (two related identifiers, each with its
	// identifier); questions with nothing in common split one object into half-empty ones (funder, dates, summary)
	spread.forEach((qs, list) => {
		if (qs.size < 2) return;
		const sets = [...qs.values()].map(x => x.values);
		if ([...sets[0]].some(v => sets.every(set => set.has(v)))) return;
		const all = [...qs.values()].map(x => x.q);
		result.push({
			kind: 'spread', notice: false, key: list, message: 'SPREAD',
			params: { object: list, questions: all.map(q => q.numbering).join(', ') },
			targets: all.map(q => ({ id: q.id, numbering: q.numbering, title: q.title, sectionId: q.sectionIds[q.sectionIds.length - 1] ?? null, isSection: false }))
		});
	});

	// the same object on two sections or questions
	const describers = new Map<string, SemanticCheck['targets']>();
	sections.forEach(s => objectsOf(s.form).forEach(k => { if (!describers.has(k)) describers.set(k, []); describers.get(k).push({ id: s.id, numbering: s.numbering, title: s.title, sectionId: s.id, isSection: true }); }));
	questions.forEach(q => objectsOf(q.fieldSetForm).forEach(k => { if (!describers.has(k)) describers.set(k, []); describers.get(k).push({ id: q.id, numbering: q.numbering, title: q.title, sectionId: q.sectionIds[q.sectionIds.length - 1] ?? null, isSection: false }); }));
	describers.forEach((targets, key) => {
		if (targets.length < 2 || !objectMap.has(key)) return;
		result.push({ kind: 'shared-object', notice: true, key, message: 'SHARED-OBJECT', params: { object: key, where: targets.map(t => t.numbering).join(', ') }, targets });
	});

	return result;
}
