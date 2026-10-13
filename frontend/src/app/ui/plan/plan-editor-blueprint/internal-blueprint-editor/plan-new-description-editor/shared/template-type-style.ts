import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';

export interface TemplateTypeStyle {
	background: string;
	color: string;
}

// Light chip background + dark text, all pairs pass WCAG AA. Asset template types are
// tenant-defined, so a stable hash of the type picks the pair instead of a hard-coded map.
const PALETTE: TemplateTypeStyle[] = [
	{ background: '#E3F2FD', color: '#0D47A1' },
	{ background: '#E8F5E9', color: '#1B5E20' },
	{ background: '#FFF3E0', color: '#BF360C' },
	{ background: '#E0F2F1', color: '#00695C' },
	{ background: '#F3E5F5', color: '#6A1B9A' },
	{ background: '#FCE4EC', color: '#880E4F' },
];

export function templateTypeStyle(type: DescriptionTemplateType | null | undefined): TemplateTypeStyle {
	const key = type?.id?.toString() ?? type?.code ?? type?.name;
	if (!key) { return PALETTE[0]; }
	let hash = 0;
	for (let i = 0; i < key.length; i++) {
		hash = (hash * 31 + key.charCodeAt(i)) >>> 0;
	}
	return PALETTE[hash % PALETTE.length];
}
