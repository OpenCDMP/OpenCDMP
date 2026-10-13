import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ChangeDetectionStrategy } from '@angular/core';
import { UntypedFormArray, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { FieldType } from '@app/core/common/enum/field-type';
import { FieldValidationType } from '@app/core/common/enum/field-validation-type';
import { TemplateFieldEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { BlueprintStructureHelper } from '../../structure.helper';
import { defaultValueText, kindIcon, kindLabelKey, kindOf } from '../../input-kinds';
import { StructureEditorBase } from '../../structure-editor-base';
import { objectsOf } from '../../semantic-objects';
import { MatIcon } from '@angular/material/icon';
import { CdkDragHandle } from '@angular/cdk/drag-drop';
import { MatTooltip } from '@angular/material/tooltip';
import { NgTemplateOutlet } from '@angular/common';
import { MatFormField, MatError, MatLabel, MatHint } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatIconButton, MatButton } from '@angular/material/button';
import { Bp2QuestionReadComponent } from '../question-read/question-read.component';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { RichTextEditorComponent } from '../../../../../library/rich-text-editor/rich-text-editor.component';
import { ReactiveAsteriskDirective } from '../../../../../../common/forms/reactive-asterisk-directive';
import { Bp2InputRowComponent } from '../input-row/input-row.component';
import { ObjectFieldComponent } from '../object-field/object-field.component';
import { MatMenuTrigger, MatMenu, MatMenuItem } from '@angular/material/menu';
import { MatDivider } from '@angular/material/list';
import { Bp2CountPipe } from '../../count.pipe';

/**
 * One question (field set). Collapsed it is a single line that says what it is; expanded it shows the title,
 * the help texts named by where authors see them, the inputs and the repeat / comment settings.
 */
@Component({
    selector: 'app-bp2-question-card',
    templateUrl: './question-card.component.html',
    styleUrls: ['./question-card.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, CdkDragHandle, MatTooltip, NgTemplateOutlet, MatFormField, MatInput, FormsModule, ReactiveFormsModule, MatError, MatIconButton, Bp2QuestionReadComponent, MatSlideToggle, RichTextEditorComponent, ReactiveAsteriskDirective, Bp2InputRowComponent, MatButton, ObjectFieldComponent, MatLabel, MatHint, MatMenuTrigger, MatMenu, MatMenuItem, MatDivider, TranslatePipe, Bp2CountPipe]
})
export class Bp2QuestionCardComponent implements OnChanges {
	@Input() entry: ToCEntry;
	@Input() section: ToCEntry;
	@Input() expanded = false;
	@Output() toggled = new EventEmitter<void>();

	showDescription = false;
	showExtended = false;
	showInfo = false;
	/** The help editors; closed to one line of what is on. */
	helpOpen = false;
	/** The Export row; open when the question describes an object or its answers stand for keys. */
	exportOpen = false;
	readonly typeIcon = BlueprintStructureHelper.typeIcon;

	constructor(public editor: StructureEditorBase<any, any>, private dialog: MatDialog, private language: TranslateService) { }

	ngOnChanges(changes: SimpleChanges): void {
		if (changes['entry'] || changes['expanded']) {
			this.showDescription = !!this.form?.get('description')?.value;
			this.showExtended = !!this.form?.get('extendedDescription')?.value;
			this.showInfo = !!this.form?.get('additionalInformation')?.value;
			this.helpOpen = false;
			this.exportOpen = this.objects.length > 0 || this.optionsWithKeys > 0;
		}
	}

	private get options(): UntypedFormGroup[] {
		return (this.fields?.controls ?? []).flatMap(f => ((f.get('data')?.get('options') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[]);
	}
	/** Answer options across the question's select and radio inputs. */
	get optionTotal(): number { return this.options.length; }
	/** Options that stand for keys or export nothing. */
	get optionsWithKeys(): number { return this.options.filter(o => (o.get('semantics')?.value ?? []).length || o.get('exportsNothing')?.value).length; }
	get answerKeysShown(): boolean { return this.editor.answerKeysShown(this.entry); }
	/** On while an option stands for keys, the question describes an object, or a section around it does. */
	get answerKeysLocked(): boolean { return this.answerKeysShown && !this.editor.answerKeysOn.has(this.entry?.id); }
	setAnswerKeys(on: boolean): void {
		if (on) this.editor.answerKeysOn.add(this.entry.id); else this.editor.answerKeysOn.delete(this.entry.id);
	}

	get form(): UntypedFormGroup { return this.entry?.form as UntypedFormGroup; }
	get fields(): UntypedFormArray { return this.form?.get('fields') as UntypedFormArray; }
	get firstField(): UntypedFormGroup { return BlueprintStructureHelper.firstFieldForm(this.form); }
	get isPreset(): boolean { return BlueprintStructureHelper.isPresetQuestion(this.form); }
	get readOnly(): boolean { return this.editor.readOnly; }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }
	get invalid(): boolean { return this.form?.invalid && (this.editor.attemptedSubmit || this.form.touched || this.form.dirty); }
	get fieldSetIndex(): number { return (this.form?.parent as UntypedFormArray)?.controls.indexOf(this.form) ?? 0; }
	get fieldsRootPath(): string { return `${this.entry?.validationRootPath}.fieldSets[${this.fieldSetIndex}].`; }

	// ---- collapsed summary

	/** The first input in words: its kind plus the detail that matters (options, reference type, link target, size). */
	get typeName(): string {
		const first = this.firstField;
		const type = BlueprintStructureHelper.fieldType(first);
		if (type == FieldType.SYSTEM) return this.editor.enumUtils.toSystemFieldTypeString(BlueprintStructureHelper.systemFieldType(first));
		const kind = kindOf(first);
		if (!kind) return null;
		const t = (k: string, p?: object) => this.editor.language.instant(k, p);
		const label = t(kindLabelKey(kind));
		const data = first.get('data');
		if (kind === 'single' || kind === 'multiple') {
			const n = (data.get('options') as UntypedFormArray)?.length ?? 0;
			return t(n === 1 ? 'PLAN-BLUEPRINT-EDITOR.QUESTION.WITH-OPTIONS-ONE' : 'PLAN-BLUEPRINT-EDITOR.QUESTION.WITH-OPTIONS', { kind: label, count: n });
		}
		if (kind === 'reference') {
			const id = data.get('referenceTypeId')?.value;
			const name = this.editor.availableReferenceTypes.find(r => r.id?.toString() === id?.toString())?.name;
			return name ? `${label}: ${name}` : label;
		}
		if (kind === 'internal') return `${label}: ${t(type === FieldType.INTERNAL_ENTRIES_DESCRIPTIONS ? 'PLAN-BLUEPRINT-EDITOR.INPUT.LINK-ASSETS' : 'PLAN-BLUEPRINT-EDITOR.INPUT.LINK-PLANS')}`;
		if (kind === 'upload') { const mb = data.get('maxFileSizeInMB')?.value; return mb ? `${label}, ${mb} MB` : label; }
		return label;
	}
	get firstType(): FieldType { return BlueprintStructureHelper.fieldType(this.firstField); }
	get firstIcon(): string { return kindIcon(kindOf(this.firstField)); }
	get defaultText(): string { return this.firstField ? defaultValueText(this.firstField, this.editor.language) : null; }
	get excludedFromDownloads(): boolean { return this.firstField ? !this.firstField.get('includeInExport')?.value : false; }
	/** "Shows 2.2 when “Other”", for rules on this question's inputs. */
	get rulesFromText(): string {
		const links = this.editor.rulesFrom(this.form).filter(l => l.targetId);
		if (!links.length) return null;
		const l = links[0];
		const text = this.editor.language.instant(l.value ? 'PLAN-BLUEPRINT-EDITOR.RULES.SHOWS-WHEN' : 'PLAN-BLUEPRINT-EDITOR.RULES.SHOWS', { target: l.targetNumbering, value: l.value });
		return links.length > 1 ? `${text} +${links.length - 1}` : text;
	}
	/** "Shown when 2.1 is “Other”", on the question a rule shows. */
	get rulesIntoText(): string {
		const links = this.editor.rulesInto(this.form);
		if (!links.length) return null;
		const l = links[0];
		const text = this.editor.language.instant(l.value ? 'PLAN-BLUEPRINT-EDITOR.RULES.SHOWN-WHEN' : 'PLAN-BLUEPRINT-EDITOR.RULES.SHOWN-BY', { source: l.sourceNumbering, value: l.value });
		return links.length > 1 ? `${text} +${links.length - 1}` : text;
	}
	semPrefix(s: string): string { return s.includes('.') ? s.split('.')[0] : s; }
	semRest(s: string): string { return s.includes('.') ? s.slice(s.indexOf('.')) : ''; }
	get required(): boolean { return BlueprintStructureHelper.hasValidation(this.firstField, FieldValidationType.Required); }
	get hasUrl(): boolean { return BlueprintStructureHelper.hasValidation(this.firstField, FieldValidationType.Url); }
	get hasEmail(): boolean { return BlueprintStructureHelper.hasValidation(this.firstField, FieldValidationType.Email); }
	/** The objects the question describes, shown as chips on the collapsed row. */
	get objects(): string[] { return objectsOf(this.form); }
	objectName(key: string): string { return key.slice(key.lastIndexOf('.') + 1); }
	get semantics(): string[] { return this.fields?.controls?.flatMap(f => BlueprintStructureHelper.semanticsOf(f as UntypedFormGroup)) ?? []; }
	get ruleCount(): number { return this.fields?.controls?.reduce((n, f) => n + ((f.get('visibilityRules') as UntypedFormArray)?.length ?? 0), 0) ?? 0; }
	get repeatable(): boolean { return !!this.form?.get('hasMultiplicity')?.value; }
	get extraInputs(): number { return Math.max(0, (this.fields?.length ?? 0) - 1); }

	// ---- help texts

	setHelp(kind: 'description' | 'extendedDescription' | 'additionalInformation', on: boolean): void {
		if (kind === 'description') this.showDescription = on;
		if (kind === 'extendedDescription') this.showExtended = on;
		if (kind === 'additionalInformation') this.showInfo = on;
		if (!on) { const c = this.form.get(kind); c.setValue(null); c.markAsDirty(); }
	}

	// ---- inputs

	addInput(type: FieldType = FieldType.FREE_TEXT): void {
		const ordinal = this.fields.length;
		const field = BlueprintStructureHelper.newField({ type, ordinal, maxFileSizeInMB: this.editor.configurationService.maxFileSizeInMB });
		this.fields.push(new TemplateFieldEditorModel(this.editor.editorModel.validationErrorModel).fromModel(field).buildForm({ rootPath: `${this.fieldsRootPath}fields[${ordinal}].` }));
		this.fields.markAsDirty();
		this.editor.onFieldTypeChanged();
	}

	removeInput(index: number): void {
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: { message: this.language.instant('PLAN-BLUEPRINT-EDITOR.INPUT.DELETE-CONFIRM'), confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'), cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'), isDeleteConfirmation: true }
		});
		dialogRef.afterClosed().subscribe(result => {
			if (!result) return;
			this.fields.removeAt(index);
			BlueprintStructureHelper.renumber(this.fields);
			this.fields.markAsDirty();
			this.editor.onFieldTypeChanged();
		});
	}

	moveInput(index: number, direction: -1 | 1): void {
		const target = index + direction;
		if (target < 0 || target >= this.fields.length) return;
		const control = this.fields.at(index);
		this.fields.removeAt(index);
		this.fields.insert(target, control);
		BlueprintStructureHelper.renumber(this.fields);
		this.fields.markAsDirty();
	}

	onInputChanged(): void {
		// a preset input rules out repeated answers
		if (this.isPreset) {
			this.form.get('hasMultiplicity').setValue(false, { emitEvent: false });
			this.form.get('hasMultiplicity').disable({ emitEvent: false });
		} else {
			this.form.get('hasMultiplicity').enable({ emitEvent: false });
		}
		this.editor.onFieldTypeChanged();
	}

	// ---- repeatable & comment

	setRepeatable(on: boolean): void {
		const multiplicity = this.form.get('multiplicity') as UntypedFormGroup;
		this.form.get('hasMultiplicity').setValue(on);
		this.form.get('hasMultiplicity').markAsDirty();
		if (on) {
			multiplicity.get('min').setValue(multiplicity.get('min').value ?? 0);
			multiplicity.get('max').setValue(multiplicity.get('max').value ?? 1);
			multiplicity.get('placeholder').setValue(multiplicity.get('placeholder').value ?? '');
			multiplicity.get('tableView').setValue(multiplicity.get('tableView').value ?? false);
		} else {
			['min', 'max', 'placeholder', 'tableView', 'transposedTableView'].forEach(k => multiplicity.get(k).setValue(null));
		}
		multiplicity.markAsDirty();
		multiplicity.updateValueAndValidity();
	}

	get multiplicity(): UntypedFormGroup { return this.form?.get('multiplicity') as UntypedFormGroup; }
	get tableView(): boolean { return !!this.multiplicity?.get('tableView')?.value; }
}
