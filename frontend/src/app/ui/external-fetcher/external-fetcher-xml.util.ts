// Generic, lossless JSON <-> XML mirror used to export/import external-fetcher backed
// editor data (reference-type / prefilling-source definitions) purely on the frontend.
// Every value is tagged with its JS type so re-parsing reconstructs the exact same shape
// (arrays vs single objects, numbers vs strings, null vs undefined, booleans).

const ROOT_TAG = 'root';
const ARRAY_ITEM_TAG = 'item';

type JsonValue = null | undefined | string | number | boolean | JsonValue[] | { [key: string]: JsonValue };

function escapeXmlText(value: string): string {
	return value
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;');
}

function isPlainObject(value: unknown): value is { [key: string]: JsonValue } {
	return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function buildNode(tagName: string, value: JsonValue): string {
	if (value === null) {
		return `<${tagName} type="null"></${tagName}>`;
	}
	if (value === undefined) {
		return `<${tagName} type="undefined"></${tagName}>`;
	}
	if (Array.isArray(value)) {
		const children = value.map(item => buildNode(ARRAY_ITEM_TAG, item)).join('');
		return `<${tagName} type="array">${children}</${tagName}>`;
	}
	if (isPlainObject(value)) {
		const children = Object.keys(value)
			.map(key => buildNode(key, value[key]))
			.join('');
		return `<${tagName} type="object">${children}</${tagName}>`;
	}
	if (typeof value === 'boolean') {
		return `<${tagName} type="boolean">${value}</${tagName}>`;
	}
	if (typeof value === 'number') {
		return `<${tagName} type="number">${value}</${tagName}>`;
	}
	return `<${tagName} type="string">${escapeXmlText(String(value))}</${tagName}>`;
}

export function toXmlString(data: JsonValue): string {
	const body = buildNode(ROOT_TAG, data);
	return `<?xml version="1.0" encoding="UTF-8"?>\n${body}`;
}

function parseNode(element: Element): JsonValue {
	const type = element.getAttribute('type');
	switch (type) {
		case 'null':
			return null;
		case 'undefined':
			return undefined;
		case 'array':
			return Array.from(element.children).map(child => parseNode(child));
		case 'object': {
			const obj: { [key: string]: JsonValue } = {};
			Array.from(element.children).forEach(child => {
				obj[child.tagName] = parseNode(child);
			});
			return obj;
		}
		case 'boolean':
			return element.textContent === 'true';
		case 'number':
			return element.textContent === '' ? null : Number(element.textContent);
		case 'string':
		default:
			return element.textContent ?? '';
	}
}

export class XmlParseError extends Error { }

export function fromXmlString(xml: string): JsonValue {
	const parser = new DOMParser();
	const doc = parser.parseFromString(xml, 'application/xml');

	const parserError = doc.getElementsByTagName('parsererror');
	if (parserError.length > 0) {
		throw new XmlParseError('Invalid XML file.');
	}

	const root = doc.documentElement;
	if (!root) {
		throw new XmlParseError('Invalid XML file.');
	}

	return parseNode(root);
}
