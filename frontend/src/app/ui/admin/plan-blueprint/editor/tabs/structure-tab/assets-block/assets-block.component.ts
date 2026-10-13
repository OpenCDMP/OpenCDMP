import { CdkDragDrop, CdkDropList, CdkDrag, CdkDragHandle } from '@angular/cdk/drag-drop';
import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PrefillingSource } from '@app/core/model/prefilling-source/prefilling-source';
import { PrefillingSourceService } from '@app/core/services/prefilling-source/prefilling-source.service';
import { PrefillingSourceLookup } from '@app/core/query/prefilling-source.lookup';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { Guid } from '@common/types/guid';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { DescriptionTemplatesInSectionEditorModel } from '../../../../editor/plan-blueprint-editor.model';
import { BlueprintStructureHelper } from '@app/ui/admin/structure-editor/structure.helper';
import { PlanBlueprintEditorComponent } from '../../../plan-blueprint-editor.component';
import { TemplatePickerDialogComponent } from '../../../dialogs/template-picker/template-picker.dialog';
import { MatIcon } from '@angular/material/icon';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatError, MatFormField } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../../../../../../common/forms/reactive-asterisk-directive';
import { MatIconButton, MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MultipleAutoCompleteComponent } from '../../../../../../../library/auto-complete/multiple/multiple-auto-complete.component';

/**
 * "Assets in this section": one framed setting on a root section. The switch decides whether authors add
 * assets here; the list says with which templates; the two rows below say whether they may pick others and
 * whether metadata can be prefilled from repositories.
 */
@Component({
    selector: 'app-bp2-assets-block',
    templateUrl: './assets-block.component.html',
    styleUrls: ['./assets-block.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIcon, MatSlideToggle, CdkDropList, CdkDrag, CdkDragHandle, MatError, ReactiveAsteriskDirective, FormsModule, ReactiveFormsModule, MatIconButton, MatTooltip, MatButton, MatFormField, MultipleAutoCompleteComponent, TranslatePipe]
})
export class Bp2AssetsBlockComponent extends BaseComponent implements OnInit {
	@Input() section: ToCEntry;

	allSources: PrefillingSource[] = null;
	limiting = false;

	constructor(public editor: PlanBlueprintEditorComponent, private dialog: MatDialog, private language: TranslateService, public prefillingSourceService: PrefillingSourceService) { super(); }

	ngOnInit(): void {
		const lookup = new PrefillingSourceLookup();
		lookup.page = { offset: 0, size: 200 };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [nameof<PrefillingSource>(x => x.label)] };
		lookup.project = { fields: [nameof<PrefillingSource>(x => x.id), nameof<PrefillingSource>(x => x.label), nameof<PrefillingSource>(x => x.code)] };
		this.prefillingSourceService.query(lookup).pipe(takeUntil(this._destroyed)).subscribe({ next: r => { this.allSources = r.items ?? []; this.limiting = this.isLimited; }, error: () => this.allSources = [] });
	}

	get form(): UntypedFormGroup { return this.section?.form as UntypedFormGroup; }
	get enabled(): boolean { return !!this.form?.get('hasTemplates')?.value; }
	get templates(): UntypedFormArray { return this.form?.get('descriptionTemplates') as UntypedFormArray; }
	get canEditTemplates(): UntypedFormControl { return this.form?.get('canEditDescriptionTemplates') as UntypedFormControl; }
	get prefillEnabled(): UntypedFormControl { return this.form?.get('prefillingSourcesEnabled') as UntypedFormControl; }
	get prefillIds(): UntypedFormControl { return this.form?.get('prefillingSourcesIds') as UntypedFormControl; }
	get readOnly(): boolean { return this.editor.readOnly; }
	get showErrors(): boolean { return this.editor.attemptedSubmit; }

	templateOf(control: UntypedFormGroup): DescriptionTemplate {
		const groupId = control.get('descriptionTemplateGroupId').value;
		return groupId ? this.editor.descriptionTemplateMap.get(groupId.toString()) : null;
	}

	toggle(on: boolean): void {
		if (on) {
			this.form.get('hasTemplates').setValue(true);
			this.form.get('hasTemplates').markAsDirty();
			this.editor.onTemplatesPicked([]);
			return;
		}
		const count = this.templates.length;
		if (!count) { this.disable(); return; }
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '420px',
			data: {
				message: this.language.instant('PLAN-BLUEPRINT-EDITOR.ASSETS.TURN-OFF-CONFIRM', { count }),
				confirmButton: this.language.instant('PLAN-BLUEPRINT-EDITOR.ASSETS.TURN-OFF'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => result ? this.disable() : null);
	}

	private disable(): void {
		this.templates.clear();
		this.prefillEnabled.setValue(false);
		this.prefillIds.setValue([]);
		this.form.get('hasTemplates').setValue(false);
		this.form.markAsDirty();
		this.editor.onTemplatesPicked([]);
	}

	addTemplates(): void {
		const used: Guid[] = this.templates.controls.map(c => c.get('descriptionTemplateGroupId').value).filter(x => !!x);
		const dialogRef = this.dialog.open(TemplatePickerDialogComponent, { width: 'min(640px, 95vw)', data: { excludedGroupIds: used } });
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe((picked: DescriptionTemplate[]) => {
			if (!picked?.length) return;
			const sectionIndex = this.editor.sectionsArray.controls.findIndex(x => x.get('id').value === this.section.id);
			picked.forEach(template => {
				const model = new DescriptionTemplatesInSectionEditorModel(this.editor.editorModel.validationErrorModel);
				model.descriptionTemplateGroupId = template.groupId;
				this.templates.push(model.buildForm({ rootPath: `definition.sections[${sectionIndex}].descriptionTemplates[${this.templates.length}].` }));
			});
			this.templates.markAsDirty();
			this.editor.onTemplatesPicked(picked);
		});
	}

	remove(index: number): void {
		this.templates.removeAt(index);
		this.templates.markAsDirty();
		this.editor.onTemplatesPicked([]);
	}

	preview(control: UntypedFormGroup): void {
		const template = this.templateOf(control);
		if (!template?.id) return;
		this.dialog.open(DescriptionTemplatePreviewDialogComponent, { width: 'min(800px, 95vw)', minHeight: '200px', data: { descriptionTemplateId: template.id }, panelClass: 'custom-modalbox' });
	}

	drop(event: CdkDragDrop<any>): void {
		if (event.previousIndex === event.currentIndex) return;
		this.editor.reorder(this.templates, event.previousIndex, event.currentIndex);
	}

	// ---- prefilling

	get selectedSources(): PrefillingSource[] {
		const ids: Guid[] = this.prefillIds?.value ?? [];
		return (this.allSources ?? []).filter(s => ids.some(id => id?.toString() === s.id?.toString()));
	}
	get isLimited(): boolean {
		if (!this.allSources) return false;
		const ids: Guid[] = this.prefillIds?.value ?? [];
		return ids.length > 0 && ids.length < this.allSources.length;
	}
	get selectedSourceNames(): string { return this.selectedSources.map(s => s.label).join(', '); }
	/** "Authors add 1 to 5", or "any number", for the read-only view. */
	rangeText(control: AbstractControl): string {
		const min = control.get('minMultiplicity')?.value;
		const max = control.get('maxMultiplicity')?.value;
		const t = (k: string, p?: object) => this.language.instant(k, p);
		if (min == null && max == null) return t('PLAN-BLUEPRINT-EDITOR.READ.RANGE-ANY');
		return t('PLAN-BLUEPRINT-EDITOR.READ.RANGE', { min: min ?? 0, max: max ?? t('PLAN-BLUEPRINT-EDITOR.ASSETS.ANY') });
	}
	get allSourceNames(): string { return (this.allSources ?? []).map(s => s.label).join(', '); }

	togglePrefill(on: boolean): void {
		this.prefillEnabled.setValue(on);
		this.prefillEnabled.markAsDirty();
		if (on) {
			const ids: Guid[] = this.prefillIds.value ?? [];
			if (!ids.length && this.allSources?.length) this.useAllSources();
		} else {
			this.prefillIds.setValue([]);
			this.limiting = false;
		}
		this.prefillIds.markAsDirty();
	}

	useAllSources(): void {
		this.prefillIds.setValue((this.allSources ?? []).map(s => s.id));
		this.prefillIds.markAsDirty();
		this.limiting = false;
	}

	get sourcesHint(): string {
		if (!this.allSources) return this.language.instant('PLAN-BLUEPRINT-EDITOR.ASSETS.PREFILL-LOADING');
		if (!this.allSources.length) return this.language.instant('PLAN-BLUEPRINT-EDITOR.ASSETS.PREFILL-NONE-CONFIGURED');
		return this.language.instant('PLAN-BLUEPRINT-EDITOR.ASSETS.PREFILL-HINT', { count: this.allSources.length, names: this.allSourceNames });
	}

	readonly isPresetQuestion = BlueprintStructureHelper.isPresetQuestion;
}
