import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { PluginSemantics, SemanticKeyLevel, SemanticKeyUsage, SemanticObject } from '@app/core/model/semantic/plugin-semantics';

/**
 * What the tenant's plugins can do with a structure, from the manifests they publish: which of the values each one
 * reads the structure's inputs cover. Nothing here is required by OpenCDMP and nothing blocks saving or finalizing.
 */

/**
 * What a structure gives plugins: `keys` come out in an export (keys on inputs and keys answer options stand for);
 * `markers` also hold the objects sections and questions describe, which make an object "used" without a value.
 */
export interface StructureUse { keys: Set<string>; markers: Set<string>; }

/**
 * A value a plugin reads, and whether the structure covers it: an input or answer carries one of its keys, or a
 * reference input carries its object and the plugin reads the value from the picked reference's own fields
 * (`viaReference`).
 */
export interface ValueStatus { usage: SemanticKeyUsage; covered: boolean; viaReference?: boolean; viaTemplates?: string[]; }

/** An asset template a blueprint attaches, with what it gives plugins: a plan value read from templates can come from it. */
export interface AttachedUse { label: string; use: StructureUse; }

/**
 * How a set of needed values stands: all covered, gaps the plugin fills with a fallback (amber), or gaps with nothing
 * in their place (red).
 */
export type NeedsTone = 'ok' | 'fallback' | 'gap';

export function needsTone(values: ValueStatus[]): NeedsTone {
	const missing = values.filter(x => !x.covered);
	if (!missing.length) return 'ok';
	return missing.some(x => !x.usage.fallback) ? 'gap' : 'fallback';
}

export interface PluginCoverage {
	pluginType: PluginType;
	pluginId: string;
	name: string;
	needs: ValueStatus[];
	canUse: ValueStatus[];
	needsDone: number;
	canUseDone: number;
}

/** A published standard the structure maps to: its needed values, covered or not, and the plugins that read them. */
export interface StandardMapping {
	prefix: string;
	name: string;
	values: ValueStatus[];
	done: number;
	readers: string[];
}

/** A prefix in use, with the plugins that read keys under it. */
export interface PrefixReaders { name: string; readers: string[]; }

/** The i18n key for a plugin type's operation: export, deposit or evaluation. */
export function pluginKindKey(type: PluginType): string {
	switch (type) {
		case PluginType.Deposit: return 'PLAN-BLUEPRINT-EDITOR.COVERAGE.KIND-DEPOSIT';
		case PluginType.Evaluation: return 'PLAN-BLUEPRINT-EDITOR.COVERAGE.KIND-EVALUATION';
		default: return 'PLAN-BLUEPRINT-EDITOR.COVERAGE.KIND-EXPORT';
	}
}

/** The plugin's own vocabulary name when it declares one ("RDA maDMP"), otherwise its id. */
export function pluginName(plugin: PluginSemantics): string {
	return plugin.semantics?.vocabularies?.[0]?.name || plugin.pluginId;
}

function usesKeyUnder(used: Set<string>, key: string): boolean {
	if (used.has(key)) return true;
	for (const k of used) if (k.startsWith(key + '.')) return true;
	return false;
}

/**
 * An input or answer carries one of the value's keys (or, for a prefix value, a key under one). A key naming a whole object
 * (a distribution a reference input fills) is covered too when the structure fills the object through keys under it.
 */
export function covers(usage: SemanticKeyUsage, used: Set<string>, objects?: Map<string, SemanticObject>): boolean {
	return (usage.keys ?? []).some(k => usage.prefix || objects?.has(k) ? usesKeyUnder(used, k) : used.has(k));
}

/**
 * The plugin reads the value from a picked reference's fields, and a reference input carries the value's object or
 * one around it (the host's url, with a reference input for the host or for the whole distribution).
 */
function coveredByReference(usage: SemanticKeyUsage, used: Set<string>, objects: Map<string, SemanticObject>): boolean {
	if (!usage.fromReferenceFields || !usage.object) return false;
	for (const k of used) {
		if (objects.get(k)?.fromReference && (usage.object === k || usage.object.startsWith(k + '.'))) return true;
	}
	return false;
}

/**
 * Whether the value counts for this structure: it is read from this kind of structure, and it sits in no object, in
 * an object every output has, or in an object the structure uses: an input or answer carries a key under it, or a
 * section or question describes it (or an object inside it).
 */
function counts(usage: SemanticKeyUsage, entityType: PluginEntityType, objects: Map<string, SemanticObject>, use: StructureUse, attached: AttachedUse[]): boolean {
	if (!(usage.entityTypes ?? []).includes(entityType)) return false;
	if (!usage.object) return true;
	if (objects.get(usage.object)?.alwaysPresent) return true;
	return usesKeyUnder(use.markers, usage.object) || fromTemplates(usage, attached).some(a => usesKeyUnder(a.use.markers, usage.object));
}

/** The attached templates that can fill the value: only for values the plugin also reads from an asset's answers. */
function fromTemplates(usage: SemanticKeyUsage, attached: AttachedUse[]): AttachedUse[] {
	return (usage.entityTypes ?? []).includes(PluginEntityType.Description) ? attached : [];
}

function statuses(plugin: PluginSemantics, level: SemanticKeyLevel, entityType: PluginEntityType, use: StructureUse, attached: AttachedUse[] = []): ValueStatus[] {
	const objects = new Map((plugin.semantics?.objects ?? []).map(o => [o.key, o] as [string, SemanticObject]));
	return (plugin.semantics?.keys ?? [])
		.filter(u => u.level === level && counts(u, entityType, objects, use, attached))
		.map(usage => {
			if (covers(usage, use.keys, objects)) return { usage, covered: true };
			const viaReference = coveredByReference(usage, use.keys, objects);
			if (viaReference) return { usage, covered: true, viaReference };
			const viaTemplates = fromTemplates(usage, attached).filter(a => covers(usage, a.use.keys, objects) || coveredByReference(usage, a.use.keys, objects)).map(a => a.label);
			return viaTemplates.length ? { usage, covered: true, viaTemplates } : { usage, covered: false };
		});
}

/**
 * Each plugin that reads something from this kind of structure, with what the structure covers of it, and for a
 * blueprint what its attached templates cover of the plan values the plugin also reads from assets.
 */
export function pluginCoverage(plugins: PluginSemantics[], entityType: PluginEntityType, use: StructureUse, attached: AttachedUse[] = []): PluginCoverage[] {
	return (plugins ?? []).map(plugin => {
		const needs = statuses(plugin, SemanticKeyLevel.Needs, entityType, use, attached);
		const canUse = statuses(plugin, SemanticKeyLevel.CanUse, entityType, use, attached);
		return {
			pluginType: plugin.pluginType,
			pluginId: plugin.pluginId,
			name: pluginName(plugin),
			needs,
			canUse,
			needsDone: needs.filter(x => x.covered).length,
			canUseDone: canUse.filter(x => x.covered).length,
		};
	}).filter(x => x.needs.length + x.canUse.length > 0);
}

/**
 * Per published standard any plugin declares, the needed values under its prefix across plugins (the same value read
 * by an export and an evaluator counts once), and which plugins read them.
 */
export function standardMappings(plugins: PluginSemantics[], entityType: PluginEntityType, use: StructureUse, nameOf: (p: PluginSemantics) => string): StandardMapping[] {
	const standards = new Map<string, string>();
	(plugins ?? []).forEach(p => (p.semantics?.vocabularies ?? []).filter(v => v.standard).forEach(v => { if (!standards.has(v.prefix)) standards.set(v.prefix, v.name); }));
	return [...standards.entries()].map(([prefix, name]) => {
		const byValue = new Map<string, ValueStatus>();
		const readers = new Set<string>();
		(plugins ?? []).forEach(plugin => statuses(plugin, SemanticKeyLevel.Needs, entityType, use)
			.filter(x => x.usage.keys?.some(k => k === prefix || k.startsWith(prefix + '.')))
			.forEach(x => {
				readers.add(nameOf(plugin));
				const id = x.usage.target || x.usage.keys[0];
				const known = byValue.get(id);
				if (!known) byValue.set(id, x);
				else if (x.covered && !known.covered) byValue.set(id, x);
			}));
		const values = [...byValue.values()];
		return { prefix, name, values, done: values.filter(x => x.covered).length, readers: [...readers] };
	}).filter(x => x.values.length > 0);
}

/** For a prefix, its vocabulary name if a plugin declares one, and the plugins reading any key under it. */
export function prefixReaders(plugins: PluginSemantics[], prefix: string, nameOf: (p: PluginSemantics) => string): PrefixReaders {
	let name: string = null;
	const readers: string[] = [];
	(plugins ?? []).forEach(p => {
		const vocabulary = p.semantics?.vocabularies?.find(v => v.prefix === prefix);
		if (vocabulary && !name) name = vocabulary.name;
		const reads = !!vocabulary || (p.semantics?.keys ?? []).some(u => (u.keys ?? []).some(k => k === prefix || k.startsWith(prefix + '.')));
		if (reads) { const n = nameOf(p); if (!readers.includes(n)) readers.push(n); }
	});
	return { name, readers };
}
