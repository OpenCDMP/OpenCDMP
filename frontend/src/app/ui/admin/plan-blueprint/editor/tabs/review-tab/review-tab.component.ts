import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { BaseField, BaseFieldSet, BaseSection, FieldReferenceTypeData } from '@app/core/model/description-template/description-template';
import { BaseFieldPersist, FieldReferenceTypePersist, FieldSetPersist, SectionPersist } from '@app/core/model/description-template/description-template-persist';
import { PlanBlueprint, PlanBlueprintDefinition, PlanBlueprintDefinitionPersist, PlanBlueprintPersist, RootSection, RootSectionPersist } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan } from '@app/core/model/plan/plan';
import { VisibilityRulesService } from '@app/ui/description/editor/description-form/visibility-rules/visibility-rules.service';
import { PlanEditorForm, PlanEditorModel } from '@app/ui/plan/plan-editor-blueprint/plan-editor.model';
import { FormService } from '@common/forms/form-service';
import { PlanBlueprintEditorComponent } from '../../plan-blueprint-editor.component';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatIcon } from '@angular/material/icon';
import { MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatButton } from '@angular/material/button';
import { PreviewMappingsDirective } from '../../../../structure-editor/review/preview-mappings.directive';
import { PlanInternalBlueprintEditorComponent } from '../../../../../plan/plan-editor-blueprint/internal-blueprint-editor/plan-internal-blueprint-editor.component';
import { DownloadOutlineComponent } from '../../../../structure-editor/review/download-outline/download-outline.component';
import { PluginReviewComponent } from '../../../../structure-editor/review/plugin-review/plugin-review.component';
import { SemanticsCardComponent } from '../../../../structure-editor/review/semantics-card/semantics-card.component';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../../../structure-editor/count.pipe';

/**
 * Review tab: the blueprint as an author will meet it (starting a plan, filling it, downloading it). The readiness,
 * semantics, counts and download outline are shared with the description template editor.
 */
@Component({
    selector: 'app-bp2-review-tab',
    templateUrl: './review-tab.component.html',
    styleUrls: ['./review-tab.component.scss'],
    providers: [VisibilityRulesService],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatSlideToggle, FormsModule, MatIcon, MatButtonToggleGroup, MatButtonToggle, MatButton, PreviewMappingsDirective, PlanInternalBlueprintEditorComponent, ReactiveFormsModule, DownloadOutlineComponent, PluginReviewComponent, SemanticsCardComponent, TranslatePipe, Bp2CountPipe]
})
export class Bp2ReviewTabComponent implements OnInit {
	planBlueprint: PlanBlueprint;
	formGroup: FormGroup<PlanEditorForm>;
	planEditorModel: PlanEditorModel;
	visibilityRules: VisibilityRulesService;
	previewError = false;
	/** Which author moment the preview shows: starting a plan, filling it, or the download. */
	mode: 'start' | 'fill' | 'download' = 'fill';
	/** Shows each input's keys in the author preview. */
	showMappings = false;

	constructor(public editor: PlanBlueprintEditorComponent, private formService: FormService, public visibilityRulesService: VisibilityRulesService) { }

	ngOnInit(): void {
		try {
			this.planBlueprint = this.buildPlanBlueprint(this.editor.previewPersist);
			const mockPlan: Plan = { blueprint: this.planBlueprint };
			this.planEditorModel = new PlanEditorModel().fromModel(mockPlan);
			this.formGroup = this.planEditorModel.buildForm({ visibilityRules: this.visibilityRulesService });
			this.visibilityRules = new VisibilityRulesService(this.formService);
			this.visibilityRules.setContext(this.planBlueprint.definition, this.formGroup.controls.properties);
		} catch (e) {
			console.error(e);
			this.previewError = true;
		}
	}

	// ---- author moments

	get iconName(): string { return this.editor.formGroup.controls.definition.get('iconConfiguration')?.get('matIconName')?.value; }
	/** Top-level sections as the author meets them after creating the plan. */
	get sectionSteps(): { numbering: string, title: string, summary: string }[] {
		const t = (k: string, p?: object) => this.editor.language.instant(k, p);
		return (this.editor.toCEntries ?? []).map(e => {
			const questions = this.countIn(e);
			const assets = !!e.form.get('hasTemplates')?.value;
			const parts = [t(questions === 1 ? 'PLAN-BLUEPRINT-EDITOR.SECTION.QUESTIONS-COUNT-ONE' : 'PLAN-BLUEPRINT-EDITOR.SECTION.QUESTIONS-COUNT', { count: questions })];
			if (assets) parts.push(t('PLAN-BLUEPRINT-EDITOR.OUTLINE.ASSETS'));
			return { numbering: e.numbering, title: e.label, summary: parts.join(' · ') };
		});
	}

	private countIn(entry: any): number { return (entry.subEntries ?? []).reduce((n, e) => n + (e.type === 2 ? 1 : this.countIn(e)), 0); }

	// ---- counts

	get consentOn(): boolean { return !!this.editor.formGroup.controls.definition.get('planCreationConsent')?.get('enabled')?.value; }

	// ---- preview model, as the old final preview built it

	private buildPlanBlueprint(persist: PlanBlueprintPersist): PlanBlueprint {
		return { id: persist.id, label: persist.label, description: persist.description, groupId: undefined, version: undefined, type: undefined, status: persist.status, definition: this.buildDefinition(persist.definition) };
	}
	private buildDefinition(persist: PlanBlueprintDefinitionPersist): PlanBlueprintDefinition {
		return { sections: (persist?.sections ?? []).map(x => this.buildSection(x, true) as RootSection) };
	}
	private buildSection(persist: RootSectionPersist | SectionPersist, root = false): BaseSection | RootSection {
		const section = { id: persist.id, ordinal: persist.ordinal, title: persist.title, description: persist.description, sections: (persist.sections ?? []).map(x => this.buildSection(x)), fieldSets: (persist.fieldSets ?? []).map(x => this.buildFieldSet(x)) };
		if (!root) return section;
		const rootPersist = persist as RootSectionPersist;
		return {
			...section,
			canEditDescriptionTemplates: rootPersist.canEditDescriptionTemplates,
			hasTemplates: rootPersist.hasTemplates,
			descriptionTemplates: (rootPersist.descriptionTemplates ?? []).map(item => ({ descriptionTemplate: this.editor.descriptionTemplateMap.get(item.descriptionTemplateGroupId?.toString()), maxMultiplicity: item.maxMultiplicity, minMultiplicity: item.minMultiplicity })),
			prefillingSourcesEnabled: rootPersist.prefillingSourcesEnabled,
			prefillingSources: []
		} as RootSection;
	}
	private buildFieldSet(persist: FieldSetPersist): BaseFieldSet {
		return {
			id: persist.id, ordinal: persist.ordinal, title: persist.title, description: persist.description, extendedDescription: persist.extendedDescription, additionalInformation: persist.additionalInformation,
			hasMultiplicity: persist.hasMultiplicity,
			multiplicity: { max: persist.multiplicity?.max, min: persist.multiplicity?.min, placeholder: persist.multiplicity?.placeholder, tableView: persist.multiplicity?.tableView, transposedTableView: persist.multiplicity?.transposedTableView },
			hasCommentField: persist.hasCommentField,
			fields: (persist.fields ?? []).filter(f => f.data?.fieldType != null).map(x => this.buildField(x))
		};
	}
	private buildField(persist: BaseFieldPersist): BaseField {
		const field: BaseField = { id: persist.id, ordinal: persist.ordinal, semantics: persist.semantics, defaultValue: persist.defaultValue, visibilityRules: persist.visibilityRules, validations: persist.validations, includeInExport: persist.includeInExport, data: persist.data };
		if (persist.data?.fieldType === FieldType.REFERENCE_TYPES) {
			const referenceTypeId = (persist.data as FieldReferenceTypePersist).referenceTypeId;
			const selected = this.editor.availableReferenceTypes.find(r => r.id == referenceTypeId);
			(field.data as FieldReferenceTypeData).referenceType = { id: referenceTypeId, name: selected?.name };
		}
		return field;
	}
}
