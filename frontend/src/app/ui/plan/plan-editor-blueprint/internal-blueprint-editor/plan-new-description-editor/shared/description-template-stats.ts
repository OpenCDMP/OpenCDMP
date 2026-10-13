import { BaseSection, DescriptionTemplate } from '@app/core/model/description-template/description-template';

/** Number of field sets ("fields" in the UI) across the whole template, nested sections included. */
export function countTemplateFieldSets(template: DescriptionTemplate | null | undefined): number {
	return (template?.definition?.sections ?? []).reduce((sum, section) => sum + countSectionFieldSets(section), 0);
}

export function countSectionFieldSets(section: BaseSection | null | undefined): number {
	const own = section?.fieldSets?.length ?? 0;
	return own + (section?.sections ?? []).reduce((sum, child) => sum + countSectionFieldSets(child), 0);
}

/** Top-level sections only — what the overview lists as the template structure. */
export function countTemplateSections(template: DescriptionTemplate | null | undefined): number {
	return template?.definition?.sections?.length ?? 0;
}
