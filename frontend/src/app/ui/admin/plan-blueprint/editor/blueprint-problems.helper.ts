import { FormGroup, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { StructureProblem, StructureProblemsHelper } from '@app/ui/admin/structure-editor/structure-problems';
import { BlueprintStructureHelper } from '@app/ui/admin/structure-editor/structure.helper';
import { TranslateService } from '@ngx-translate/core';
import { PlanBlueprintForm } from '../editor/plan-blueprint-editor.model';

export type BlueprintProblem = StructureProblem;

/**
 * One list of what is wrong with the blueprint, built from the same validators the form already runs, so
 * the header count, the problems panel, the Details checklist and the Finalize dialog never disagree. The
 * structure rules are shared with the description template editor; the details and assets rules are the blueprint's.
 */

function compute(params: { formGroup: FormGroup<PlanBlueprintForm>, entries: ToCEntry[], language: TranslateService, enumUtils: EnumUtils, iconSet: boolean }): BlueprintProblem[] {
	const { formGroup, entries, language, enumUtils, iconSet } = params;
	const t = (key: string, args?: any) => language.instant(key, args);
	const problems: BlueprintProblem[] = [];
	const details = t('PLAN-BLUEPRINT-EDITOR.TABS.DETAILS');

	// ---- details
	if (formGroup.get('label').invalid) problems.push({ id: 'label', severity: 'blocker', blocksSave: true, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NAME-REQUIRED'), where: details, tab: 'details' });
	const code = formGroup.get('code');
	if (code.enabled && code.invalid) problems.push({ id: 'code', severity: 'blocker', blocksSave: true, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.CODE-REQUIRED'), where: details, tab: 'details' });
	StructureProblemsHelper.backendOn(problems, formGroup.get('label'), 'label-be', t('PLAN-BLUEPRINT-EDITOR.DETAILS.NAME'), details);
	StructureProblemsHelper.backendOn(problems, formGroup.get('code'), 'code-be', t('PLAN-BLUEPRINT-EDITOR.DETAILS.CODE'), details);
	StructureProblemsHelper.backendOn(problems, formGroup.get('description'), 'description-be', t('PLAN-BLUEPRINT-EDITOR.DETAILS.DESCRIPTION'), details);
	const consent = formGroup.controls.definition.get('planCreationConsent') as unknown as UntypedFormGroup;
	if (consent?.get('enabled')?.value && consent.get('message')?.invalid) problems.push({ id: 'consent-message', severity: 'blocker', blocksSave: true, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.CONSENT-MESSAGE'), where: details, tab: 'details' });
	const plugins = formGroup.controls.definition.get('pluginConfigurations') as UntypedFormArray;
	if (plugins?.invalid) problems.push({ id: 'plugins', severity: 'blocker', blocksSave: true, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.EXPORT-TEMPLATE'), where: details, tab: 'details' });

	// ---- presets, sections & questions (the form validator also blocks save on missing presets today)
	const sections = formGroup.controls.definition.controls.sections as unknown as UntypedFormArray;
	const structureProblems = StructureProblemsHelper.compute({ sections, entries, requiredPresets: BlueprintStructureHelper.REQUIRED_PRESETS, language, enumUtils, term: (k) => k });

	// ---- assets, listed right after the section-level checks
	const structure = t('PLAN-BLUEPRINT-EDITOR.TABS.STRUCTURE');
	const assets: BlueprintProblem[] = [];
	if (sections?.hasError('planBlueprintDescriptionTemplatesRequired')) {
		assets.push({ id: 'assets', severity: 'blocker', blocksSave: true, tab: 'structure', where: structure, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NO-ASSETS') });
	} else if (sections?.length) {
		assets.push({ id: 'assets', severity: 'ok', blocksSave: false, tab: 'structure', where: structure, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.ASSETS-OK') });
	}
	const after = Math.max(structureProblems.findIndex(p => p.id === 'no-sections'), structureProblems.findIndex(p => p.id === 'presets')) + 1;
	structureProblems.splice(after, 0, ...assets);
	problems.push(...structureProblems);

	// ---- optional
	if (!iconSet) problems.push({ id: 'icon', severity: 'optional', blocksSave: false, tab: 'details', where: details, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NO-ICON') });
	if (!formGroup.get('description')?.value) problems.push({ id: 'description', severity: 'optional', blocksSave: false, tab: 'details', where: details, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NO-DESCRIPTION') });
	if (!consent?.get('enabled')?.value) problems.push({ id: 'consent', severity: 'optional', blocksSave: false, tab: 'details', where: details, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NO-CONSENT') });
	if (!problems.some(p => p.id === 'label')) problems.unshift({ id: 'details-ok', severity: 'ok', blocksSave: false, tab: 'details', where: details, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.DETAILS-OK') });

	return problems;
}

function presetsSummary(present: SystemFieldType[]): { preset: SystemFieldType, present: boolean }[] {
	return BlueprintStructureHelper.REQUIRED_PRESETS.map(preset => ({ preset, present: present.includes(preset) }));
}

export const BlueprintProblemsHelper = {
	compute,
	presetsSummary
};
