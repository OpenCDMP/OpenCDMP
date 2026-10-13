import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { questionIndex, QuestionRef } from '../../input-kinds';
import { objectsOf, optionSemanticsOf, usagesOfKey } from '../../semantic-objects';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';
import { Bp2KeyPartsPipe } from '../../key-parts.pipe';

/** A key in use and the questions whose inputs (or answer options) carry it. */
export interface KeyInUse { key: string; questions: QuestionRef[]; }

function fieldsOf(q: QuestionRef): UntypedFormGroup[] { return ((q.fieldSetForm.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[]; }
function inputKeys(field: UntypedFormGroup): string[] { return ((field.get('semantics')?.value ?? []) as string[]).filter(Boolean); }

/** Every key the structure's inputs and answer options carry, in outline order, with the questions carrying it. */
export function keysInUse(entries: ToCEntry[]): KeyInUse[] {
	const byKey = new Map<string, QuestionRef[]>();
	questionIndex(entries).forEach(q => fieldsOf(q).forEach(field => [...inputKeys(field), ...optionSemanticsOf(field).map(x => x.key)].filter(Boolean).forEach(key => {
		if (!byKey.has(key)) byKey.set(key, []);
		const list = byKey.get(key);
		if (!list.includes(q)) list.push(q);
	})));
	return [...byKey.entries()].map(([key, questions]) => ({ key, questions }));
}

/** The questions that carry no key and describe no object; preset questions are left out, they map themselves. */
export function unmappedQuestions(entries: ToCEntry[]): QuestionRef[] {
	return questionIndex(entries).filter(q => {
		const fields = fieldsOf(q);
		const preset = fields.some(f => f.get('data')?.get('fieldType')?.value === FieldType.SYSTEM);
		const mapped = objectsOf(q.fieldSetForm).length > 0 || fields.some(f => inputKeys(f).length > 0 || optionSemanticsOf(f).length > 0);
		return !mapped && !preset;
	});
}

interface KeyGroup { id: string; label: string; object: string; keys: KeyInUse[]; }

/**
 * The Structure tab's Keys view: every key in use, grouped by the object a plugin says it belongs to (keys no plugin
 * places in an object are grouped by prefix), each linking to its questions; then the questions that carry no key.
 */
@Component({
    selector: 'app-se2-keys-view',
    templateUrl: './keys-view.component.html',
    styleUrls: ['./keys-view.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, TranslatePipe, Bp2CountPipe, Bp2KeyPartsPipe]
})
export class KeysViewComponent {
	showUnmapped = false;

	constructor(public editor: StructureEditorBase<any, any>) { }

	get groups(): KeyGroup[] {
		const usages = this.editor.knownUsages();
		const objects = new Map(this.editor.knownObjects().map(o => [o.key, o] as const));
		const groups = new Map<string, KeyGroup>();
		keysInUse(this.editor.toCEntries).forEach(k => {
			const object = usagesOfKey(usages, k.key).map(x => x.usage.object).find(Boolean) ?? null;
			const known = object ? objects.get(object) : null;
			const prefix = k.key.includes('.') ? k.key.split('.')[0] : k.key;
			const id = object ?? `prefix:${prefix}`;
			if (!groups.has(id)) groups.set(id, { id, object, label: known?.label ?? (object ?? prefix), keys: [] });
			groups.get(id).keys.push(k);
		});
		return [...groups.values()];
	}

	get unmapped(): QuestionRef[] { return unmappedQuestions(this.editor.toCEntries); }

	/** A key as listed under its object: the part after the object, e.g. `…host.url`. */
	shortKey(key: string, object: string): string { return object && key.startsWith(object + '.') ? '…' + key.slice(object.length + 1) : key; }

	open(q: QuestionRef): void { this.editor.openQuestion(q.id, q.sectionIds[q.sectionIds.length - 1] ?? null); }
}
