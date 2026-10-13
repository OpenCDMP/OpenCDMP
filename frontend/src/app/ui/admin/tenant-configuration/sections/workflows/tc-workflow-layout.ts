import { Guid } from '@common/types/guid';

/** A status as the workflow section needs it: plan and asset statuses share this shape. */
export interface TcWorkflowStatus {
	id: Guid;
	name: string;
	ordinal?: number;
	/** 0 = draft, 1 = finalized, 2 = canceled (assets only). */
	internalStatus?: number;
}

export interface TcWorkflowTransition {
	fromStatusId: Guid;
	toStatusId: Guid;
}

export interface TcWorkflowNode {
	id: string;
	name: string;
	label: string;
	x: number;
	y: number;
	start: boolean;
	reachable: boolean;
}

export type TcWorkflowEdgeKind = 'next' | 'skip' | 'back';

export interface TcWorkflowEdge {
	key: string;
	kind: TcWorkflowEdgeKind;
	path: string;
}

export interface TcWorkflowLayout {
	width: number;
	height: number;
	boxWidth: number;
	boxHeight: number;
	nodes: TcWorkflowNode[];
	edges: TcWorkflowEdge[];
}

const key = (id: Guid | string | null | undefined): string => (id ?? '').toString().toLowerCase();

const BOX_HEIGHT = 48;
const MARGIN_X = 12;
const ARC_BASE = 22;
const ARC_STEP = 16;
const ARC_MAX = 110;
const PORT_OFFSET = 12;

/**
 * Puts the statuses in a sensible order: the starting status first, then by the longest chain of forward
 * transitions leading to each one (so a status is drawn after everything that leads to it), ties broken by
 * finalized / canceled statuses last and then by ordinal. Statuses that cannot be reached from the start
 * come last. Transitions closing a cycle (e.g. Finalized back to Draft) count as going back.
 */
export function orderWorkflowStatuses(statuses: TcWorkflowStatus[], startingStatusId: Guid | null, transitions: TcWorkflowTransition[]): { ordered: TcWorkflowStatus[]; reachable: Set<string> } {
	const byKey = new Map(statuses.map(s => [key(s.id), s]));
	const rank = (s: TcWorkflowStatus): number => (s.internalStatus ?? 0) > 0 ? 1 : 0;
	const compare = (a: TcWorkflowStatus, b: TcWorkflowStatus): number =>
		rank(a) - rank(b) || (a.ordinal ?? 0) - (b.ordinal ?? 0) || (a.name ?? '').localeCompare(b.name ?? '');

	const out = new Map<string, string[]>();
	for (const t of transitions) {
		const from = key(t.fromStatusId);
		const to = key(t.toStatusId);
		if (!byKey.has(from) || !byKey.has(to) || from === to) continue;
		if (!out.has(from)) out.set(from, []);
		if (!out.get(from).includes(to)) out.get(from).push(to);
	}
	out.forEach(list => list.sort((a, b) => compare(byKey.get(a), byKey.get(b))));

	// Depth-first from the start: an edge to a status still on the stack closes a cycle and counts as backward.
	const start = key(startingStatusId);
	const reachable = new Set<string>();
	const forward: [string, string][] = [];
	if (byKey.has(start)) {
		const onStack = new Set<string>();
		const visit = (node: string): void => {
			reachable.add(node);
			onStack.add(node);
			for (const next of out.get(node) ?? []) {
				if (onStack.has(next)) continue;
				forward.push([node, next]);
				if (!reachable.has(next)) visit(next);
			}
			onStack.delete(node);
		};
		visit(start);
	}

	// Longest forward chain from the start; the forward edges form a DAG, so this settles within n rounds.
	const layer = new Map<string, number>([[start, 0]]);
	for (let round = 0; round < reachable.size; round++) {
		let changed = false;
		for (const [from, to] of forward) {
			const candidate = (layer.get(from) ?? 0) + 1;
			if (to !== start && candidate > (layer.get(to) ?? 0)) {
				layer.set(to, candidate);
				changed = true;
			}
		}
		if (!changed) break;
	}

	const ordered = [
		...statuses.filter(s => reachable.has(key(s.id))).sort((a, b) => (layer.get(key(a.id)) ?? 0) - (layer.get(key(b.id)) ?? 0) || compare(a, b)),
		...statuses.filter(s => !reachable.has(key(s.id))).sort(compare),
	];
	return { ordered, reachable };
}

/** Boxes in one row, straight arrows to the next box, arcs above for skips, dashed arcs below for going back. */
export function layoutWorkflow(ordered: TcWorkflowStatus[], reachable: Set<string>, startingStatusId: Guid | null, transitions: TcWorkflowTransition[]): TcWorkflowLayout {
	const n = ordered.length;
	const boxWidth = n <= 4 ? 140 : n <= 6 ? 116 : 96;
	const gap = n <= 4 ? 56 : n <= 6 ? 44 : 34;
	const index = new Map(ordered.map((s, i) => [key(s.id), i]));

	const seen = new Set<string>();
	const pairs: { from: number; to: number; key: string }[] = [];
	for (const t of transitions) {
		const from = index.get(key(t.fromStatusId));
		const to = index.get(key(t.toStatusId));
		const pairKey = `${key(t.fromStatusId)}>${key(t.toStatusId)}`;
		if (from === undefined || to === undefined || from === to || seen.has(pairKey)) continue;
		seen.add(pairKey);
		pairs.push({ from, to, key: pairKey });
	}

	const arcHeight = (span: number): number => Math.min(ARC_MAX, ARC_BASE + ARC_STEP * (span - 1));
	const above = Math.max(0, ...pairs.filter(p => p.to > p.from + 1).map(p => arcHeight(p.to - p.from)));
	const below = Math.max(0, ...pairs.filter(p => p.to < p.from).map(p => arcHeight(p.from - p.to)));
	const top = 8 + above;
	const height = top + BOX_HEIGHT + below + 8;
	const width = Math.max(1, MARGIN_X * 2 + n * boxWidth + Math.max(0, n - 1) * gap);
	const left = (i: number): number => MARGIN_X + i * (boxWidth + gap);
	const centre = (i: number): number => left(i) + boxWidth / 2;
	const maxChars = Math.max(4, Math.floor((boxWidth - 16) / 7.2));
	const start = key(startingStatusId);

	const nodes: TcWorkflowNode[] = ordered.map((s, i) => ({
		id: key(s.id),
		name: s.name,
		label: (s.name ?? '').length > maxChars ? `${s.name.slice(0, maxChars - 1).trimEnd()}…` : s.name,
		x: left(i),
		y: top,
		start: key(s.id) === start,
		reachable: reachable.has(key(s.id)),
	}));

	const edges: TcWorkflowEdge[] = pairs.map(p => {
		if (p.to === p.from + 1) {
			const y = top + BOX_HEIGHT / 2;
			return { key: p.key, kind: 'next', path: `M ${left(p.from) + boxWidth} ${y} L ${left(p.to) - 2} ${y}` };
		}
		const h = arcHeight(Math.abs(p.to - p.from));
		if (p.to > p.from) {
			// Leaves from the right of the box top, lands on the left of the target's top.
			const x1 = centre(p.from) + PORT_OFFSET;
			const x2 = centre(p.to) - PORT_OFFSET;
			const c = h / 0.75;
			return { key: p.key, kind: 'skip', path: `M ${x1} ${top} C ${x1} ${top - c}, ${x2} ${top - c}, ${x2} ${top - 2}` };
		}
		const y = top + BOX_HEIGHT;
		const x1 = centre(p.from) - PORT_OFFSET;
		const x2 = centre(p.to) + PORT_OFFSET;
		const c = h / 0.75;
		return { key: p.key, kind: 'back', path: `M ${x1} ${y} C ${x1} ${y + c}, ${x2} ${y + c}, ${x2} ${y + 2}` };
	});

	return { width, height, boxWidth, boxHeight: BOX_HEIGHT, nodes, edges };
}
