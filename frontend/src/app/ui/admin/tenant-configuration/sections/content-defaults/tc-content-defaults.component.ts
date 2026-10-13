import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { ChangeDetectionStrategy, Component, Injector, Signal, afterNextRender, computed, effect, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { PlanBlueprint } from '@app/core/model/plan-blueprint/plan-blueprint';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { SingleAutoCompleteComponent } from '@app/library/auto-complete/single/single-auto-complete.component';
import { DescriptionTemplatePreviewDialogComponent } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component';
import { DescriptionTemplatePreviewDialogModule } from '@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.module';
import { TenantConfigurationEditorModel as DefaultPlanBlueprintEditorModel } from '@app/ui/admin/tenant-configuration/models/default-plan-blueprint/default-plan-blueprint-editor.model';
import { DefaultPlanBlueprintEditorResolver } from '@app/ui/admin/tenant-configuration/models/default-plan-blueprint/default-plan-blueprint-editor.resolver';
import { TenantConfigurationEditorModel as FeaturedEntitiesEditorModel, FeaturedDescriptionTemplateEditorModel, FeaturedPlanBlueprintEditorModel } from '@app/ui/admin/tenant-configuration/models/featured-entities/featured-entities-editor.model';
import { FeaturedEntitiesEditorResolver } from '@app/ui/admin/tenant-configuration/models/featured-entities/featured-entities-editor.resolver';
import { ViewPreferenceEditorModel, TenantConfigurationEditorModel as ViewPreferencesEditorModel } from '@app/ui/admin/tenant-configuration/models/view-preferences/view-preferences-editor.model';
import { ViewPreferencesEditorResolver } from '@app/ui/admin/tenant-configuration/models/view-preferences/view-preferences-editor.resolver';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { TranslateService } from '@ngx-translate/core';
import { filter, map, of, startWith, switchMap } from 'rxjs';
import { TcPart } from '../../shared/tc-part';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSectionService } from '../../shared/tc-section.service';
import { TcStateTagComponent } from '../../shared/tc-state-tag.component';

/** One entry of an ordered list edited on this section; `key` is the id stored in the form array. */
interface TcOrderedItem {
	key: string;
	label: string;
	inactive: boolean;
}

/** The ordered lists of this section, each backed by a form array of one of the parts. */
type TcListId = 'blueprints' | 'templates' | 'planRefs' | 'assetRefs';

/**
 * Content defaults: the blueprint pre-selected for new plans, the blueprints and asset templates featured
 * first, and the reference types shown on each row of the plan and asset lists.
 */
@Component({
	selector: 'app-tc-content-defaults',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, AutoCompleteModule, DragDropModule, DescriptionTemplatePreviewDialogModule, TcSaveBarComponent, TcStateTagComponent],
	providers: [TcSectionService],
	templateUrl: './tc-content-defaults.component.html',
	styleUrl: './tc-content-defaults.component.scss',
})
export class TcContentDefaultsComponent {
	protected readonly section = inject(TcSectionService);
	protected readonly planBlueprintService = inject(PlanBlueprintService);
	private readonly descriptionTemplateService = inject(DescriptionTemplateService);
	private readonly referenceTypeService = inject(ReferenceTypeService);
	private readonly tenantHandlingService = inject(TenantHandlingService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly injector = inject(Injector);

	protected readonly blueprint = this.section.part(TenantConfigurationType.DefaultPlanBlueprint, DefaultPlanBlueprintEditorResolver.lookupFields(), () => new DefaultPlanBlueprintEditorModel());
	protected readonly featured = this.section.part(TenantConfigurationType.FeaturedEntities, FeaturedEntitiesEditorResolver.lookupFields(), () => new FeaturedEntitiesEditorModel());
	protected readonly views = this.section.part(TenantConfigurationType.ViewPreferences, ViewPreferencesEditorResolver.lookupFields(), () => new ViewPreferencesEditorModel());

	protected readonly ready = computed(() => !!this.blueprint.form() && !!this.featured.form() && !!this.views.form());

	private readonly blueprintPicker = viewChild<SingleAutoCompleteComponent>('blueprintPicker');
	private readonly templatePicker = viewChild<SingleAutoCompleteComponent>('templatePicker');
	private readonly planRefPicker = viewChild<SingleAutoCompleteComponent>('planRefPicker');
	private readonly assetRefPicker = viewChild<SingleAutoCompleteComponent>('assetRefPicker');

	// ---- blueprint for new plans

	protected readonly blueprintGroupId = computed(() => this.blueprint.form()?.get('defaultPlanBlueprint')?.get('groupId') as UntypedFormControl);
	protected readonly defaultBlueprintConfiguration = this.planBlueprintService.planBlueprintGroupSingleAutocompleteConfiguration;

	// ---- featured blueprints and asset templates

	private readonly featuredValue = this.valueOf(this.featured);
	private readonly blueprintsArray = computed(() => this.featured.form()?.get('featuredEntities')?.get('planBlueprints') as UntypedFormArray);
	private readonly templatesArray = computed(() => this.featured.form()?.get('featuredEntities')?.get('descriptionTemplates') as UntypedFormArray);
	/** Names of items picked on this page and not saved yet; the saved ones come with the record. */
	private readonly pickedLabels = signal(new Map<string, string>());

	protected readonly blueprintRows = computed(() => {
		const known = new Map((this.featured.record()?.featuredEntities?.planBlueprints ?? []).map(x => [String(x.groupId), x.label] as const));
		return this.rowsOf(this.featuredValue()?.featuredEntities?.planBlueprints, 'groupId', known);
	});
	protected readonly templateRows = computed(() => {
		const known = new Map((this.featured.record()?.featuredEntities?.descriptionTemplates ?? []).map(x => [String(x.groupId), x.label] as const));
		return this.rowsOf(this.featuredValue()?.featuredEntities?.descriptionTemplates, 'groupId', known);
	});

	protected readonly blueprintPickerConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: () => this.planBlueprintService.query(this.planBlueprintService.buildPlanBlueprintGroupAutocompleteLookup({
			isActive: [IsActive.Active],
			excludedGroupIds: this.keysOf(this.blueprintsArray(), 'groupId'),
		})).pipe(map(x => x.items)),
		filterFn: (searchQuery: string) => this.planBlueprintService.query(this.planBlueprintService.buildPlanBlueprintGroupAutocompleteLookup({
			isActive: [IsActive.Active],
			like: searchQuery,
			excludedGroupIds: this.keysOf(this.blueprintsArray(), 'groupId'),
		})).pipe(map(x => x.items)),
		getSelectedItem: null,
		displayFn: () => null,
		titleFn: (item: PlanBlueprint) => item.label,
		subtitleFn: (item: PlanBlueprint) => this.language.instant('PLAN-EDITOR.FIELDS.PLAN-BLUEPRINT-VERSION') + ' ' + item.version,
		valueAssign: () => null,
	};

	protected readonly templatePickerConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: () => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
			isActive: [IsActive.Active],
			excludedGroupIds: this.keysOf(this.templatesArray(), 'groupId'),
		})).pipe(map(x => x.items)),
		filterFn: (searchQuery: string) => this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
			isActive: [IsActive.Active],
			like: searchQuery,
			excludedGroupIds: this.keysOf(this.templatesArray(), 'groupId'),
		})).pipe(map(x => x.items)),
		getSelectedItem: () => null,
		displayFn: () => null,
		titleFn: (item: DescriptionTemplate) => item.label,
		subtitleFn: (item: DescriptionTemplate) => item.description,
		valueAssign: () => null,
		popupItemActionIcon: 'visibility',
	};

	// ---- references shown in lists

	private readonly viewsValue = this.valueOf(this.views);
	private readonly planRefsArray = computed(() => this.views.form()?.get('viewPreferences')?.get('planPreferences') as UntypedFormArray);
	private readonly assetRefsArray = computed(() => this.views.form()?.get('viewPreferences')?.get('descriptionPreferences') as UntypedFormArray);
	private readonly knownReferenceTypes = computed(() => {
		const prefs = this.views.record()?.viewPreferences;
		return new Map([...(prefs?.planPreferences ?? []), ...(prefs?.descriptionPreferences ?? [])]
			.filter(x => x.referenceType?.id)
			.map(x => [String(x.referenceType.id), x.referenceType] as const));
	});

	protected readonly planRefRows = computed(() => this.referenceRows(this.viewsValue()?.viewPreferences?.planPreferences));
	protected readonly assetRefRows = computed(() => this.referenceRows(this.viewsValue()?.viewPreferences?.descriptionPreferences));
	protected readonly previewPills = computed(() => this.planRefRows().filter(x => !x.inactive).map(x => x.label));

	protected readonly planRefPickerConfiguration = this.referencePickerConfiguration(this.planRefsArray);
	protected readonly assetRefPickerConfiguration = this.referencePickerConfiguration(this.assetRefsArray);

	// ---- editing state

	/** Items of a record inherited from the default tenant, or seen without edit rights, cannot be changed. */
	protected readonly blueprintEditable = computed(() => this.section.canEdit && !this.blueprint.inherited());
	protected readonly featuredEditable = computed(() => this.section.canEdit && !this.featured.inherited());
	protected readonly viewsEditable = computed(() => this.section.canEdit && !this.views.inherited());

	/** Screen reader announcement after an item was moved with the keyboard. */
	protected readonly announcement = signal('');

	constructor() {
		// The loaded default blueprint is shared with the plan creation flow.
		toObservable(this.blueprint.record).pipe(filter(Boolean), takeUntilDestroyed()).subscribe(data => this.tenantHandlingService.addPlanBlueprintGroupIdSubject(data));

		// The old editor model only disables the top level fields, so lock the picker explicitly when read-only.
		effect(() => {
			const control = this.blueprintGroupId();
			if (control && !this.blueprintEditable()) control.disable({ emitEvent: false });
		});
	}

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	// ---- featured entities

	protected addBlueprint(item: PlanBlueprint): void {
		const array = this.blueprintsArray();
		if (!item?.groupId || !array || this.keysOf(array, 'groupId').some(x => String(x) === String(item.groupId))) return;
		const model = new FeaturedPlanBlueprintEditorModel(new ValidationErrorModel());
		model.groupId = item.groupId;
		model.ordinal = array.length;
		this.rememberLabel(item.groupId, item.label);
		this.push(array, model.buildForm({ rootPath: `featuredEntities.planBlueprints[${array.length}].` }));
		this.blueprintPicker()?.clearValue(false);
	}

	protected addTemplate(item: DescriptionTemplate): void {
		const array = this.templatesArray();
		if (!item?.groupId || !array || this.keysOf(array, 'groupId').some(x => String(x) === String(item.groupId))) return;
		const model = new FeaturedDescriptionTemplateEditorModel(new ValidationErrorModel());
		model.groupId = item.groupId;
		model.ordinal = array.length;
		this.rememberLabel(item.groupId, item.label);
		this.push(array, model.buildForm({ rootPath: `featuredEntities.descriptionTemplates[${array.length}].` }));
		this.templatePicker()?.clearValue(false);
	}

	/** The eye icon of the asset template picker previews a template; picking it from the preview adds it. */
	protected previewTemplate(item: DescriptionTemplate): void {
		this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
			width: 'min(800px, 95vw)',
			minHeight: '200px',
			data: { descriptionTemplateId: item.id },
			panelClass: 'custom-modalbox',
			injector: this.injector,
		}).afterClosed().pipe(filter(Boolean)).subscribe((template: DescriptionTemplate) => this.addTemplate(template));
	}

	// ---- view preferences

	protected addReference(list: 'planRefs' | 'assetRefs', item: ReferenceType): void {
		const array = this.arrayOf(list);
		if (!item?.id || !array || this.keysOf(array, 'referenceTypeId').some(x => String(x) === String(item.id))) return;
		const model = new ViewPreferenceEditorModel(new ValidationErrorModel());
		model.referenceTypeId = item.id;
		model.ordinal = array.length;
		this.rememberLabel(item.id, item.name);
		const path = list === 'planRefs' ? 'planPreferences' : 'descriptionPreferences';
		this.push(array, model.buildForm({ rootPath: `viewPreferences.${path}[${array.length}].` }));
		(list === 'planRefs' ? this.planRefPicker() : this.assetRefPicker())?.clearValue(false);
	}

	/** Alt + arrow keys move a focused reference type chip. */
	protected onChipKeydown(event: KeyboardEvent, list: TcListId, index: number, item: TcOrderedItem): void {
		if (!event.altKey) return;
		const step = event.key === 'ArrowLeft' || event.key === 'ArrowUp' ? -1 : event.key === 'ArrowRight' || event.key === 'ArrowDown' ? 1 : 0;
		if (step === 0) return;
		event.preventDefault();
		this.moveBy(list, index, step, item, `tc-cd-${list}-${item.key}`);
	}

	// ---- shared list operations

	protected drop(list: TcListId, event: CdkDragDrop<unknown>): void {
		if (event.previousIndex === event.currentIndex) return;
		this.move(this.arrayOf(list), event.previousIndex, event.currentIndex);
	}

	/** Moves an item one place up or down and keeps the keyboard focus on it. */
	protected moveBy(list: TcListId, index: number, step: -1 | 1, item: TcOrderedItem, focusId?: string): void {
		const array = this.arrayOf(list);
		if (!array) return;
		const target = index + step;
		if (target < 0 || target >= array.length) {
			this.announcement.set(this.language.instant(step < 0 ? 'ALT-TEXT.REORDER-TABLE.ITEM-AT-TOP' : 'ALT-TEXT.REORDER-TABLE.ITEM-AT-END', { item: item.label }));
			return;
		}
		this.move(array, index, target);
		this.announcement.set(this.language.instant('ALT-TEXT.REORDER-TABLE.ITEM-MOVED', { item: item.label, index: target + 1, length: array.length }));
		const focus = focusId ?? this.moveButtonId(list, item, target === 0 ? 1 : target === array.length - 1 ? -1 : step);
		afterNextRender(() => document.getElementById(focus)?.focus(), { injector: this.injector });
	}

	protected remove(list: TcListId, index: number): void {
		const array = this.arrayOf(list);
		if (!array || index < 0 || index >= array.length) return;
		array.markAsDirty();
		array.removeAt(index, { emitEvent: false });
		this.renumber(array);
	}

	protected moveButtonId(list: TcListId, item: TcOrderedItem, step: -1 | 1): string {
		return `tc-cd-${list}-${item.key}-${step < 0 ? 'up' : 'down'}`;
	}

	private arrayOf(list: TcListId): UntypedFormArray | null {
		switch (list) {
			case 'blueprints': return this.blueprintsArray();
			case 'templates': return this.templatesArray();
			case 'planRefs': return this.planRefsArray();
			case 'assetRefs': return this.assetRefsArray();
		}
	}

	private push(array: UntypedFormArray, group: UntypedFormGroup): void {
		array.markAsDirty();
		array.push(group);
	}

	private move(array: UntypedFormArray | null, from: number, to: number): void {
		if (!array) return;
		const control = array.at(from);
		array.markAsDirty();
		array.removeAt(from, { emitEvent: false });
		array.insert(to, control, { emitEvent: false });
		this.renumber(array);
	}

	/** Keeps the stored ordinals equal to the list order, then notifies the part that the value changed. */
	private renumber(array: UntypedFormArray): void {
		array.controls.forEach((control, index) => control.get('ordinal')?.setValue(index, { emitEvent: false }));
		array.updateValueAndValidity();
	}

	private rememberLabel(key: Guid, label: string): void {
		this.pickedLabels.update(labels => new Map(labels).set(String(key), label));
	}

	private keysOf(array: UntypedFormArray | null, field: string): Guid[] {
		return (array?.getRawValue() ?? []).map((x: Record<string, Guid>) => x[field]).filter(x => x != null);
	}

	private rowsOf(values: Record<string, unknown>[] | undefined, field: string, known: Map<string, string>): TcOrderedItem[] {
		const picked = this.pickedLabels();
		return (values ?? []).map(x => {
			const key = String(x[field]);
			return { key, label: known.get(key) ?? picked.get(key) ?? '', inactive: false };
		});
	}

	private referenceRows(values: { referenceTypeId: Guid }[] | undefined): TcOrderedItem[] {
		const known = this.knownReferenceTypes();
		const picked = this.pickedLabels();
		return (values ?? []).map(x => {
			const key = String(x.referenceTypeId);
			const type = known.get(key);
			return { key, label: type?.name ?? picked.get(key) ?? '', inactive: !!type && type.isActive !== IsActive.Active };
		});
	}

	private referencePickerConfiguration(array: Signal<UntypedFormArray | null>): SingleAutoCompleteConfiguration {
		return {
			initialItems: () => this.referenceTypeService.query(this.referenceTypeService.buildAutocompleteLookup([IsActive.Active], null, this.keysOf(array(), 'referenceTypeId'))).pipe(map(x => x.items)),
			filterFn: (searchQuery: string) => this.referenceTypeService.query(this.referenceTypeService.buildAutocompleteLookup([IsActive.Active], searchQuery, this.keysOf(array(), 'referenceTypeId'))).pipe(map(x => x.items)),
			getSelectedItem: () => null,
			displayFn: () => null,
			titleFn: (item: ReferenceType) => item.name,
			valueAssign: () => null,
		};
	}

	/** The raw value of a part's form, following edits and form rebuilds (load, discard, reset). */
	private valueOf(part: TcPart): Signal<any> {
		return toSignal(toObservable(part.form).pipe(
			switchMap(form => form ? form.valueChanges.pipe(startWith(null), map(() => form.getRawValue())) : of(null)),
		), { initialValue: null });
	}
}
