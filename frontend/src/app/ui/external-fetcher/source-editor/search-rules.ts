// What the backend's ExternalFetcherServiceImpl.replaceLookupFieldQuery does with the search text, mirrored so a
// rule can be tried before saving. Rules about a picked entry can't be tried here: nothing is picked.

export interface Rt2CaseValue { likePattern?: string; separator?: string; value?: string; referenceTypeId?: unknown; referenceTypeSourceKey?: string; }
export interface Rt2QueryValue { name?: string; defaultValue?: string; cases?: Rt2CaseValue[]; }

export interface Rt2QueryOutcome { name: string; ruleIndex: number; value: string; }

/** Condition presets offered for a rule; anything else is kept as the author's own pattern. */
export const RT2_CONDITION_PRESETS: { id: string, pattern: string, label: string }[] = [
	{ id: 'any', pattern: '', label: 'REFERENCE-TYPE-EDITOR.RULE.PRESET-ANY' },
	{ id: 'two-words', pattern: '^\\S+ \\S+$', label: 'REFERENCE-TYPE-EDITOR.RULE.PRESET-TWO-WORDS' },
	{ id: 'orcid', pattern: '\\d{4}-\\d{4}-\\d{4}-\\d{3}[\\dX]', label: 'REFERENCE-TYPE-EDITOR.RULE.PRESET-ORCID' },
	{ id: 'doi', pattern: '10\\.\\d{4,9}/\\S+', label: 'REFERENCE-TYPE-EDITOR.RULE.PRESET-DOI' },
	{ id: 'custom', pattern: null, label: 'REFERENCE-TYPE-EDITOR.RULE.PRESET-CUSTOM' },
];

/** Java's String.matches: the whole text must match. */
export function rt2Matches(text: string, pattern: string): boolean {
	if (!pattern) return true;
	try { return new RegExp(`^(?:${pattern})$`).test(text); } catch { return false; }
}

export function rt2ApplyCase(rule: Rt2CaseValue, like: string, defaultValue: string): string {
	if (!like) return defaultValue ?? '';
	let value = rule.value ?? '';
	if (rule.separator != null && rule.separator !== '') {
		let parts: string[];
		try { parts = like.split(new RegExp(rule.separator)); } catch { parts = like.split(rule.separator); }
		parts.forEach((part, i) => { value = value.split(`{like${i + 1}}`).join(part); });
	} else {
		value = value.split('{like}').join(like);
	}
	return value;
}

export function rt2SimulateQueries(source: { queries?: Rt2QueryValue[] }, like: string): Rt2QueryOutcome[] {
	return (source.queries ?? []).map(query => {
		const ruleIndex = (query.cases ?? []).findIndex(c => !c.referenceTypeId && !c.referenceTypeSourceKey && rt2Matches(like, c.likePattern));
		const value = ruleIndex >= 0 ? rt2ApplyCase(query.cases[ruleIndex], like, query.defaultValue) : (query.defaultValue ?? '');
		return { name: query.name, ruleIndex, value };
	});
}

// Reads a field mapping's response path from one result, the way ExternalFetcherServiceImpl.jsonToExternalDataResult
// does: "a.b", "['a b']", "[0]", ".first()" (first element of a list), and @{{path}} templates. Anything else
// (filters, wildcards, recursive descent) is reported as not readable so the caller can fall back.
export function rt2ReadPath(item: unknown, path: string): { ok: boolean, value: string | null } {
	const first = path.endsWith('.first()');
	const body = first ? path.slice(0, -'.first()'.length) : path;
	if (body.includes('@{{')) {
		let ok = true;
		const value = body.replace(/@\{\{(.*?)\}\}/g, (_m, inner: string) => {
			const part = readParts(item, inner);
			if (!part.ok) ok = false;
			return normalize(part.value, first) ?? '';
		});
		return { ok, value };
	}
	const read = readParts(item, body);
	return { ok: read.ok, value: normalize(read.value, first) };
}

function readParts(item: unknown, path: string): { ok: boolean, value: unknown } {
	let value: unknown = item;
	const parts = path.split('.first()');
	for (let i = 0; i < parts.length; i++) {
		const step = walk(value, parts[i]);
		if (!step.ok) return step;
		value = step.value;
		if (i < parts.length - 1 && Array.isArray(value)) value = value[0];
	}
	return { ok: true, value };
}

function walk(from: unknown, path: string): { ok: boolean, value: unknown } {
	let rest = path.trim().replace(/^\$\.?/, '');
	let value = from;
	while (rest.length) {
		let m: RegExpMatchArray | null;
		if ((m = rest.match(/^\.?([^.[\]'"*?()@$]+)/))) value = value == null ? undefined : (value as Record<string, unknown>)[m[1]];
		else if ((m = rest.match(/^\[(\d+)\]/))) value = Array.isArray(value) ? value[Number(m[1])] : undefined;
		else if ((m = rest.match(/^\[['"]([^'"]+)['"]\]/))) value = value == null ? undefined : (value as Record<string, unknown>)[m[1]];
		else return { ok: false, value: undefined };
		rest = rest.slice(m[0].length);
	}
	return { ok: true, value };
}

function normalize(value: unknown, first: boolean): string | null {
	if (value === undefined || value === null) return null;
	if (Array.isArray(value)) return first ? (value.length ? stringify(value[0]) : null) : JSON.stringify(value);
	return stringify(value);
}

function stringify(value: unknown): string { return typeof value === 'object' ? JSON.stringify(value) : String(value); }
