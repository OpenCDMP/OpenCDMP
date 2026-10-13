import { CdkDragDrop, CdkDropList, CdkDrag, CdkDragHandle } from '@angular/cdk/drag-drop';
import { Component, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BlueprintStructureHelper } from '../../structure.helper';
import { InputKind, KIND_GROUPS, kindHintKey, kindIcon, kindLabelKey } from '../../input-kinds';
import { StructureEditorBase } from '../../structure-editor-base';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatDivider } from '@angular/material/list';
import { MatFormField, MatLabel, MatError } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormFocusDirective } from '../../../../../../common/forms/form-focus.directive';
import { RichTextEditorComponent } from '../../../../../library/rich-text-editor/rich-text-editor.component';
import { ObjectFieldComponent } from '../object-field/object-field.component';
import { NgTemplateOutlet } from '@angular/common';
import { MatTooltip } from '@angular/material/tooltip';
import { Bp2QuestionCardComponent } from '../question-card/question-card.component';
import { TranslatePipe } from '@ngx-translate/core';
import { Bp2CountPipe } from '../../count.pipe';

/** The right pane of the Structure tab: the selected section, its assets block and its questions or subsections. */
@Component({
    selector: 'app-bp2-section-pane',
    templateUrl: './section-pane.component.html',
    styleUrls: ['./section-pane.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, MatIconButton, MatMenuTrigger, MatMenu, MatMenuItem, MatDivider, MatFormField, MatLabel, MatInput, FormsModule, ReactiveFormsModule, MatError, FormFocusDirective, RichTextEditorComponent, ObjectFieldComponent, NgTemplateOutlet, CdkDropList, CdkDrag, CdkDragHandle, MatButton, MatTooltip, Bp2QuestionCardComponent, TranslatePipe, Bp2CountPipe]
})
export class Bp2SectionPaneComponent {
	readonly ToCEntryType = ToCEntryType;
	readonly FieldType = FieldType;
	readonly kindGroups = KIND_GROUPS;
	readonly kindIcon = kindIcon;
	readonly kindLabelKey = kindLabelKey;
	readonly kindHintKey = kindHintKey;
	private descriptionOpen = new Set<string>();

	constructor(public editor: StructureEditorBase<any, any>) { }

	/** The section shown in the pane: the selected one, or the parent of a selected question. */
	get section(): ToCEntry {
		const selected = this.editor.selectedTocEntry;
		if (!selected) return null;
		if (selected.type === ToCEntryType.Section) return selected;
		const path = BlueprintStructureHelper.pathTo(selected.id, this.editor.toCEntries) ?? [];
		return path.length > 1 ? path[path.length - 2] : null;
	}
	get path(): ToCEntry[] { return this.section ? (BlueprintStructureHelper.pathTo(this.section.id, this.editor.toCEntries) ?? [this.section]) : []; }
	get form(): UntypedFormGroup { return this.section?.form as UntypedFormGroup; }
	get isRoot(): boolean { return !!this.section?.root; }
	get rootSection(): ToCEntry { return this.path[0] ?? null; }
	get subsections(): ToCEntry[] { return this.section?.subEntries?.filter(e => e.type === ToCEntryType.Section) ?? []; }
	get questions(): ToCEntry[] { return this.section?.subEntries?.filter(e => e.type === ToCEntryType.FieldSet) ?? []; }
	get hasSubsections(): boolean { return this.subsections.length > 0; }
	get hasQuestions(): boolean { return this.questions.length > 0; }
	get availablePresets(): SystemFieldType[] { return this.editor.availablePresets; }
	/** Required presets still missing, shown as "Add here" rows in the section a preset added from the bar would land in. */
	get missingPresetsHere(): SystemFieldType[] {
		if (this.editor.readOnly || this.hasSubsections || this.editor.presetTarget()?.id !== this.section?.id) return [];
		return this.editor.missingRequiredPresets;
	}
	presetName(p: SystemFieldType): string { return this.editor.enumUtils.toSystemFieldTypeString(p); }
	get isEmpty(): boolean { return !this.editor.toCEntries?.length; }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }

	descriptionShown(): boolean { return !!this.form?.get('description')?.value || this.descriptionOpen.has(this.section?.id); }
	openDescription(): void { if (this.section) this.descriptionOpen.add(this.section.id); }

	addQuestion(kind: InputKind): void { this.editor.addQuestionOfKind(this.section, kind); }
	addPreset(preset: SystemFieldType): void { this.editor.addQuestion(this.section, FieldType.SYSTEM, preset); }
	addSubsection(): void { this.editor.addSection(this.section); }
	questionCount(entry: ToCEntry): number { return BlueprintStructureHelper.countQuestions(entry); }
	warn(entry: ToCEntry): boolean { return entry.form.invalid && (this.editor.attemptedSubmit || entry.form.touched || entry.form.dirty); }
	sectionInvalid(entry: ToCEntry): boolean { return entry.form.invalid; }

	dropQuestions(event: CdkDragDrop<ToCEntry[]>): void {
		if (event.previousIndex === event.currentIndex) return;
		this.editor.reorder(this.form.get('fieldSets') as UntypedFormArray, event.previousIndex, event.currentIndex);
	}
	dropSubsections(event: CdkDragDrop<ToCEntry[]>): void {
		if (event.previousIndex === event.currentIndex) return;
		this.editor.reorder(this.form.get('sections') as UntypedFormArray, event.previousIndex, event.currentIndex);
	}

	startBlank(): void { this.editor.addMissingPresets(); }
	startEmpty(): void { this.editor.addSection(null); }

}
