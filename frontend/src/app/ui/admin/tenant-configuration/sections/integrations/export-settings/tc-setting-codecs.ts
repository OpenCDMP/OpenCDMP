/**
 * The text formats plugins store list-like settings in (see `DataType` in common-models). Editors read and write
 * these strings, so values stay exactly what the plugin parses, and a hand-written value survives a round trip.
 */

export interface OrderedListEntry {
	value: string;
	/** Replaces the option's own label, e.g. a renamed cover row. */
	label: string | null;
}

/** `"a, b=Renamed, c"` */
export function parseOrderedList(text: string | null | undefined): OrderedListEntry[] {
	if (!text?.trim()) return [];
	return text.split(/[,;]/).map(x => x.trim()).filter(Boolean).map(entry => {
		const equals = entry.indexOf('=');
		const value = (equals >= 0 ? entry.substring(0, equals) : entry).trim();
		const label = equals >= 0 ? entry.substring(equals + 1).trim() : '';
		return { value, label: label || null };
	}).filter(x => x.value);
}

export function formatOrderedList(entries: OrderedListEntry[]): string | null {
	const text = entries.filter(x => x.value).map(x => x.label?.trim() ? `${x.value}=${clean(x.label)}` : x.value).join(', ');
	return text || null;
}

/** `"key=text; key2=text"`; an empty text is kept, as it hides a built-in text (e.g. `generatedWith=`). */
export function parseKeyValue(text: string | null | undefined): Map<string, string> {
	const result = new Map<string, string>();
	if (!text?.trim()) return result;
	for (const entry of text.split(';')) {
		const equals = entry.indexOf('=');
		if (equals < 0) continue;
		const key = entry.substring(0, equals).trim();
		if (key) result.set(key, entry.substring(equals + 1).trim());
	}
	return result;
}

export function formatKeyValue(values: Map<string, string>): string | null {
	const text = [...values.entries()].filter(([key]) => key).map(([key, value]) => `${key}=${value.replace(/;/g, ',').trim()}`).join('; ');
	return text || null;
}

/** `"type:field,field; type2:field"` */
export function parseReferenceTypeFields(text: string | null | undefined): Map<string, string[]> {
	const result = new Map<string, string[]>();
	if (!text?.trim()) return result;
	for (const entry of text.split(';')) {
		const colon = entry.indexOf(':');
		if (colon < 0) continue;
		const type = entry.substring(0, colon).trim();
		const fields = entry.substring(colon + 1).split(',').map(x => x.trim()).filter(Boolean);
		if (type) result.set(type, fields);
	}
	return result;
}

export function formatReferenceTypeFields(values: Map<string, string[]>): string | null {
	const text = [...values.entries()].filter(([type]) => type).map(([type, fields]) => `${type}:${fields.join(',')}`).join('; ');
	return text || null;
}

/** Labels may not contain the separators of the list they are stored in. */
function clean(label: string): string {
	return label.replace(/[,;=]/g, ' ').replace(/\s+/g, ' ').trim();
}
