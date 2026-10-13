import { Injectable } from '@angular/core';
import { FieldType } from '@app/core/common/enum/field-type';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { Guid } from '@common/types/guid';

/**
 * Copying sections and questions between templates and blueprints. Both structures hold the same section ›
 * question › input tree, so a copy is plain values: new ids, mappings kept, rules kept only when they stay inside
 * the copied part, preset questions skipped when the destination cannot hold them.
 */

/** Something whose structure can be copied from or into: a description template or a plan blueprint. */
export interface TransferItem {
	kind: 'template' | 'blueprint';
	id: Guid;
	label: string;
	version?: number | string;
	status?: number;
	belongsToCurrentTenant?: boolean;
}

/** A section or question of a source, numbered the way its outline numbers it. */
export interface SourceNode {
	id: string;
	numbering: string;
	title: string;
	kind: 'section' | 'question';
	/** The API value: a section (with its sections and fieldSets) or a field set. */
	value: any;
	children: SourceNode[];
	parent: SourceNode;
}

/** What is copied: whole sections (with everything in them) and single questions. */
export interface TransferPayload {
	sourceLabel: string;
	sections: any[];
	questions: any[];
}

/** A copy waiting for its destination editor to open: pushed from another editor, inserted on arrival. */
export interface PendingTransfer {
	destinationId: string;
	/** The section the copy lands in (questions) or after (sections); 'top' for the end of the top level. */
	targetSectionId: string | 'top';
	payload: TransferPayload;
}

/** Holds a pushed copy between leaving one editor and opening the other. */
@Injectable({ providedIn: 'root' })
export class StructureTransferService {
	private pending: PendingTransfer = null;
	set(transfer: PendingTransfer): void { this.pending = transfer; }
	/** The copy meant for this destination, once; null when there is none. */
	take(destinationId: string): PendingTransfer {
		if (!this.pending || this.pending.destinationId?.toString() !== destinationId?.toString()) return null;
		const transfer = this.pending;
		this.pending = null;
		return transfer;
	}
}

/** The source's sections as a numbered tree (1, 1.1, 1.1.1; questions numbered inside their section). */
function buildSourceTree(sections: any[]): SourceNode[] {
	const bySort = (list: any[]) => [...(list ?? [])].sort((a, b) => (a?.ordinal ?? 0) - (b?.ordinal ?? 0));
	const visit = (list: any[], prefix: string, parent: SourceNode): SourceNode[] => bySort(list).map((section, i) => {
		const numbering = prefix ? `${prefix}.${i + 1}` : `${i + 1}`;
		const node: SourceNode = { id: section.id?.toString(), numbering, title: section.title ?? '', kind: 'section', value: section, children: [], parent };
		const subsections = visit(section.sections, numbering, node);
		const questions = bySort(section.fieldSets).map((fs, j) => ({ id: fs.id?.toString(), numbering: `${numbering}.${subsections.length + j + 1}`, title: fs.title ?? '', kind: 'question' as const, value: fs, children: [], parent: node }));
		node.children = [...subsections, ...questions];
		return node;
	});
	return visit(sections, '', null);
}

function flatten(nodes: SourceNode[]): SourceNode[] { return nodes.flatMap(n => [n, ...flatten(n.children)]); }

/** Every field set inside a section value, depth first. */
function fieldSetsIn(section: any): any[] { return [...(section?.fieldSets ?? []), ...(section?.sections ?? []).flatMap(fieldSetsIn)]; }
function fieldsOf(fieldSet: any): any[] { return fieldSet?.fields ?? []; }

/** The preset a question stores, or null. */
function presetOf(fieldSet: any): SystemFieldType {
	const field = fieldsOf(fieldSet).find(f => f?.data?.fieldType === FieldType.SYSTEM);
	return field ? (field.data?.systemFieldType ?? null) : null;
}

/** Ids of the questions and inputs a payload copies: rules pointing at them stay, the rest are dropped. */
function idsIn(payload: TransferPayload): Set<string> {
	const ids = new Set<string>();
	const addFieldSet = (fs: any) => { if (fs?.id) ids.add(fs.id.toString()); fieldsOf(fs).forEach(f => f?.id && ids.add(f.id.toString())); };
	payload.sections.forEach(s => fieldSetsIn(s).forEach(addFieldSet));
	payload.questions.forEach(addFieldSet);
	return ids;
}

/** The rules a payload would drop: a question or input in the copy that shows something outside it. */
function rulesLeaving(payload: TransferPayload, describe: (targetId: string) => string): { from: string, to: string }[] {
	const inside = idsIn(payload);
	const result: { from: string, to: string }[] = [];
	const check = (fs: any) => fieldsOf(fs).forEach(f => (f?.visibilityRules ?? []).forEach((r: any) => {
		if (r?.target && !inside.has(r.target.toString())) result.push({ from: fs.title ?? '', to: describe(r.target.toString()) });
	}));
	payload.sections.forEach(s => fieldSetsIn(s).forEach(check));
	payload.questions.forEach(check);
	return result;
}

/**
 * A deep copy with new ids for every section, question and input; rules inside the copy follow it, rules leaving it
 * are dropped; preset questions the destination refuses are left out. Returns the copy and what was left behind.
 */
function renew(payload: TransferPayload, acceptPreset: (preset: SystemFieldType) => boolean): { payload: TransferPayload, droppedRules: number, skippedPresets: number } {
	const copy: TransferPayload = JSON.parse(JSON.stringify(payload));
	const ids = new Map<string, string>();
	let skippedPresets = 0;
	const fresh = (old: any) => { const next = Guid.create().toString(); if (old) ids.set(old.toString(), next); return next; };
	const keep = (fs: any) => { const preset = presetOf(fs); if (preset == null) return true; if (acceptPreset(preset)) return true; skippedPresets++; return false; };
	const renewFieldSet = (fs: any) => { fs.id = fresh(fs.id); fieldsOf(fs).forEach(f => f.id = fresh(f.id)); return fs; };
	const renewSection = (s: any) => {
		s.id = fresh(s.id);
		s.fieldSets = (s.fieldSets ?? []).filter(keep).map(renewFieldSet);
		s.sections = (s.sections ?? []).map(renewSection);
		return s;
	};
	copy.sections = copy.sections.map(renewSection);
	copy.questions = copy.questions.filter(keep).map(renewFieldSet);
	let droppedRules = 0;
	const remap = (fs: any) => fieldsOf(fs).forEach(f => {
		const rules = f.visibilityRules ?? [];
		f.visibilityRules = rules.filter((r: any) => { const next = r?.target && ids.get(r.target.toString()); if (next) { r.target = next; return true; } droppedRules++; return false; });
	});
	copy.sections.forEach(s => fieldSetsIn(s).forEach(remap));
	copy.questions.forEach(remap);
	return { payload: copy, droppedRules, skippedPresets };
}

export const StructureTransfer = {
	buildSourceTree,
	flatten,
	fieldSetsIn,
	presetOf,
	rulesLeaving,
	renew
};
