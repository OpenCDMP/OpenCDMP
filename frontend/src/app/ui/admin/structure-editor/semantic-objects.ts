import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { PluginSemantics, SemanticKeyUsage, SemanticObject } from '@app/core/model/semantic/plugin-semantics';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';

/**
 * Semantic objects on sections and questions: a section or question can say which object of a plugin's output it
 * describes (e.g. `rda.dataset.distribution`), so the answers inside it make up one object (a section) or one object
 * per answer (a question). Keys stay full keys everywhere; only the display shortens them under an object.
 */

/** The object keys a section or question form describes. */
export function objectsOf(form: UntypedFormGroup): string[] {
	return ((form?.get('semantics')?.value ?? []) as string[]).filter(k => !!k?.trim());
}

/** The key/value rows of an input's select or radio options. */
export function optionSemanticsOf(fieldForm: UntypedFormGroup): { key: string, value: string }[] {
	const options = (fieldForm?.get('data')?.get('options') as UntypedFormArray)?.controls ?? [];
	return options.flatMap(o => ((o.get('semantics')?.value ?? []) as { key: string, value: string }[]).filter(x => !!x?.key?.trim()));
}

/** An object some plugin's manifest declares for this kind of structure, merged across plugins by key. */
export interface KnownObject extends SemanticObject { readers: string[]; }

export function knownObjects(plugins: PluginSemantics[], entityType: PluginEntityType, nameOf: (p: PluginSemantics) => string): KnownObject[] {
	const byKey = new Map<string, KnownObject>();
	(plugins ?? []).forEach(p => (p.semantics?.objects ?? [])
		.filter(o => !o.entityTypes?.length || o.entityTypes.includes(entityType))
		.forEach(o => {
			const known = byKey.get(o.key);
			if (known) { if (!known.readers.includes(nameOf(p))) known.readers.push(nameOf(p)); }
			else byKey.set(o.key, { ...o, readers: [nameOf(p)] });
		}));
	return [...byKey.values()].sort((a, b) => a.key.localeCompare(b.key));
}

/** A value some plugin reads from this kind of structure, with the plugin that reads it. */
export interface KnownUsage { usage: SemanticKeyUsage; plugin: PluginSemantics; }

export function knownUsages(plugins: PluginSemantics[], entityType: PluginEntityType): KnownUsage[] {
	return (plugins ?? []).flatMap(plugin => (plugin.semantics?.keys ?? [])
		.filter(u => !u.entityTypes?.length || u.entityTypes.includes(entityType))
		.map(usage => ({ usage, plugin })));
}

/** The usages a key fills: exact keys, or prefix usages the key falls under. */
export function usagesOfKey(usages: KnownUsage[], key: string): KnownUsage[] {
	return usages.filter(x => (x.usage.keys ?? []).some(k => x.usage.prefix ? (key === k || key.startsWith(k + '.')) : key === k));
}

/** The question's own objects and those of the sections around it, innermost first. */
export function objectContext(entries: ToCEntry[], fieldSetForm: UntypedFormGroup): string[] {
	let found: string[] = null;
	const visit = (list: ToCEntry[], around: UntypedFormGroup[]) => list?.forEach(e => {
		if (found) return;
		if (e.type === ToCEntryType.FieldSet) {
			if (e.form === fieldSetForm) found = [...objectsOf(fieldSetForm), ...[...around].reverse().flatMap(objectsOf)];
		} else visit(e.subEntries, [...around, e.form as UntypedFormGroup]);
	});
	visit(entries, []);
	return found ?? [];
}

/** The objects of the sections around a section, innermost first (not its own). */
export function sectionObjectContext(entries: ToCEntry[], sectionForm: UntypedFormGroup): string[] {
	let found: string[] = null;
	const visit = (list: ToCEntry[], around: UntypedFormGroup[]) => list?.forEach(e => {
		if (found || e.type === ToCEntryType.FieldSet) return;
		if (e.form === sectionForm) { found = [...around].reverse().flatMap(objectsOf); return; }
		visit(e.subEntries, [...around, e.form as UntypedFormGroup]);
	});
	visit(entries, []);
	return found ?? [];
}

/** The most specific object among `context` that the key sits under, or null. */
export function objectOfKey(key: string, context: string[]): string {
	return (context ?? []).filter(o => key.startsWith(o + '.')).sort((a, b) => b.length - a.length)[0] ?? null;
}

/** A key as shown under an object: `rda.dataset.distribution.host.url` under `…host` reads `…host.url`. */
export function shortKey(key: string, context: string[]): string {
	const object = objectOfKey(key, context);
	if (!object) return key;
	return '…' + object.slice(object.lastIndexOf('.') + 1) + key.slice(object.length);
}

/**
 * Everything that makes an object "used" in the structure: keys on inputs, keys an answer option stands for, and
 * the objects sections and questions describe.
 */
export function usedMarkers(sectionForms: UntypedFormGroup[], fieldSetForms: UntypedFormGroup[], fieldForms: UntypedFormGroup[]): Set<string> {
	const result = new Set<string>();
	fieldForms.forEach(f => ((f.get('semantics')?.value ?? []) as string[]).forEach(k => k && result.add(k)));
	fieldForms.forEach(f => optionSemanticsOf(f).forEach(x => result.add(x.key)));
	[...sectionForms, ...fieldSetForms].forEach(form => objectsOf(form).forEach(k => result.add(k)));
	return result;
}
