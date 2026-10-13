import { ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, input, output, signal, untracked } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionTemplateType } from '@app/core/model/description-template-type/description-template-type';
import { BaseFieldSet, BaseSection, DescriptionTemplate, DescriptionTemplateDefinition } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { RootSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { Plan } from '@app/core/model/plan/plan';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { DescriptionService } from '@app/core/services/description/description.service';
import { DescriptionTemplateEditorResolver } from '@app/ui/admin/description-template/editor/description-template-editor.resolver';
import { DescriptionEditorHelper } from '@app/ui/plan/plan-editor-blueprint/plan-description-editor/plan-description-editor-helper';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateModule } from '@ngx-translate/core';
import { finalize, Subscription } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { PlanEditorForm } from '../../plan-editor.model';
import { DescriptionInfo, PlanTempStorageService } from '../../plan-temp-storage.service';
import { NameStepBanner, NameStepResult, NewDescriptionNameStepComponent } from './name-step/new-description-name-step.component';
import { NewDescriptionFromCloneComponent } from './new-from-clone/new-description-from-clone.component';
import { ImportedDescription, NewDescriptionImportComponent } from './new-from-import/new-description-import.component';
import { NewDescriptionFromTemplateComponent } from './new-from-template/new-description-from-template.component';
import { TemplateOverviewDialogComponent, TemplateOverviewDialogData, TemplateOverviewDialogResult } from './new-from-template/template-overview-dialog/template-overview-dialog.component';

type Approach = 'manual' | 'import' | 'copy';
type View = 'approach' | 'template' | 'import' | 'name';

/** Flags every description created from this panel carries until the plan is saved. */
const NEW_DESCRIPTION_FLAGS: Partial<Description> = {
	authorizationFlags: [AppPermission.EditDescription, AppPermission.DeleteDescription, AppPermission.FinalizeDescription, AppPermission.AnnotateDescription],
	isActive: IsActive.Active,
	belongsToCurrentTenant: true,
};

/**
 * "Add a new asset" panel of the plan editor: a short wizard — how to fill the asset in (manually /
 * import from a published source / copy an existing asset) → template or import lookup → name it.
 * Every path ends by emitting a `Description` that the plan editor adds to the section and opens
 * for editing.
 */
@Component({
	selector: 'app-plan-new-description-editor',
	imports: [
		MatButtonModule, MatIconModule, MatProgressBarModule, MatTooltipModule, TranslateModule,
		NewDescriptionFromTemplateComponent, NewDescriptionFromCloneComponent, NewDescriptionImportComponent, NewDescriptionNameStepComponent,
	],
	templateUrl: './plan-new-description-editor.component.html',
	styleUrl: './plan-new-description-editor.component.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanNewDescriptionEditorComponent {
	private readonly descriptionTemplateService = inject(DescriptionTemplateService);
	private readonly descriptionService = inject(DescriptionService);
	private readonly planTempStorage = inject(PlanTempStorageService);
	private readonly authService = inject(AuthService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly dialog = inject(MatDialog);
	private readonly destroyRef = inject(DestroyRef);

	section = input<RootSection | undefined>();
	plan = input<Plan | undefined>();
	formGroup = input<FormGroup<PlanEditorForm> | undefined>();
	canEdit = input<boolean>(false);

	closed = output<void>();
	addDescription = output<Description>();
	navigateToParentSection = output<string>();

	// ---- wizard state ----
	protected readonly approach = signal<Approach | null>(null);
	protected readonly step = signal<number>(1);
	protected readonly selectedTemplate = signal<DescriptionTemplate | null>(null);
	/** Pre-built description (imported or cloned) waiting for its name; null on the manual path. */
	protected readonly draft = signal<Description | null>(null);
	protected readonly banner = signal<NameStepBanner | null>(null);
	protected readonly busy = signal(false);

	protected readonly totalSteps = computed(() => this.approach() === 'copy' ? 2 : 3);
	protected readonly view = computed<View>(() => {
		const step = this.step();
		const approach = this.approach();
		if (step === 1) { return 'approach'; }
		if (step === 2) { return approach === 'manual' ? 'template' : approach === 'import' ? 'import' : 'name'; }
		return 'name';
	});
	protected readonly nameHintKey = computed(() => {
		if (!this.draft()) { return 'NEW-DESCRIPTION.NAME-STEP.HINT'; }
		return this.approach() === 'copy' ? 'NEW-DESCRIPTION.NAME-STEP.HINT-COPIED' : 'NEW-DESCRIPTION.NAME-STEP.HINT-IMPORTED';
	});

	// ---- section data ----
	protected readonly sectionId = computed(() => this.section()?.id);
	protected readonly attachedTemplateIds = signal<Guid[]>([]);
	protected readonly templates = signal<DescriptionTemplate[]>([]);
	protected readonly templatesLoading = signal(false);

	protected readonly canClone = this.authService.hasAnyPermission([AppPermission.CloneDescription, AppPermission.PublicCloneDescription]);
	protected readonly importAvailable = computed(() => !!this.section()?.prefillingSourcesEnabled);

	protected readonly canEditTemplateList = computed(() =>
		!!this.sectionId() && this.canEdit() && !!this.blueprintSection()?.canEditDescriptionTemplates);

	private readonly blueprintSection = computed(() =>
		this.plan()?.blueprint?.definition?.sections?.find(x => x.id == this.sectionId()));

	private readonly descriptionsInSection = computed<DescriptionInfo[]>(() => {
		const sectionId = this.sectionId();
		if (!sectionId) { return []; }
		const ids = this.planTempStorage.descriptionIdsBySectionMap().get(sectionId) ?? [];
		return ids.map(id => this.planTempStorage.descriptions().get(id.toString())).filter(x => !!x);
	});

	/** Template groups whose max multiplicity in this section is already reached. */
	protected readonly excludedGroupIds = computed<Guid[]>(() => {
		const definitions = this.blueprintSection()?.descriptionTemplates ?? [];
		const usedGroupIds = this.descriptionsInSection().map(x => x.lastPersist?.descriptionTemplate?.groupId);
		return definitions
			.filter(d => d.maxMultiplicity != null && usedGroupIds.filter(g => g === d.descriptionTemplate?.groupId).length >= d.maxMultiplicity)
			.map(d => d.descriptionTemplate.groupId);
	});

	private attachedTemplatesSubscription?: Subscription;

	constructor() {
		// Follow the section's linked-template control (owners can change it while the panel is open).
		effect(() => {
			const sectionId = this.sectionId();
			const control = sectionId ? this.formGroup()?.controls?.descriptionTemplates?.get(sectionId) as FormControl<Guid[]> | null : null;
			untracked(() => {
				this.attachedTemplatesSubscription?.unsubscribe();
				this.attachedTemplateIds.set(control?.getRawValue() ?? []);
				this.attachedTemplatesSubscription = control?.valueChanges
					.pipe(takeUntilDestroyed(this.destroyRef))
					.subscribe(value => this.attachedTemplateIds.set(value ?? []));
			});
		});
		effect(() => {
			const groupIds = this.attachedTemplateIds();
			untracked(() => this.loadTemplates(groupIds));
		});
	}

	// ---- step 1: approach ----

	protected selectApproach(approach: Approach): void {
		this.approach.set(approach);
		this.draft.set(null);
		this.banner.set(null);
		this.selectedTemplate.set(null);
		this.step.set(approach === 'copy' ? 1 : 2);
	}

	protected back(): void {
		const step = this.step();
		this.draft.set(null);
		this.banner.set(null);
		if (step === 3) {
			this.step.set(2);
			if (this.approach() === 'import') { this.selectedTemplate.set(null); }
		} else if (step === 2) {
			this.selectedTemplate.set(null);
			this.step.set(1);
		}
	}

	// ---- step 2: template / import / copy ----

	protected onUseTemplate(template: DescriptionTemplate): void {
		if (this.excludedGroupIds().includes(template.groupId)) { return; }
		this.selectedTemplate.set(template);
		this.step.set(3);
	}

	protected onPreviewTemplate(template: DescriptionTemplate): void {
		const disabled = this.excludedGroupIds().includes(template.groupId);
		this.dialog.open<TemplateOverviewDialogComponent, TemplateOverviewDialogData, TemplateOverviewDialogResult>(TemplateOverviewDialogComponent, {
			width: '440px',
			maxWidth: '100vw',
			height: '100%',
			position: { right: '0px' },
			panelClass: 'dialog-side-panel',
			ariaLabelledBy: 'template-overview-title',
			autoFocus: 'first-tabbable',
			data: { template, disabled, disabledReason: 'PLAN-EDITOR.DESCRIPTION-TEMPLATES.MAX-MULTIPLICITY' },
		}).afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
			if (result === 'use') { this.onUseTemplate(template); }
		});
	}

	protected onImported(event: ImportedDescription): void {
		this.draft.set(event.description);
		this.selectedTemplate.set(event.template);
		this.banner.set({ titleKey: 'NEW-DESCRIPTION.IMPORT.IMPORTED-FROM', titleParams: { source: event.source.label }, subtitle: event.record.label });
		this.step.set(3);
	}

	protected onCopy(source: Description): void {
		if (this.busy()) { return; }
		this.busy.set(true);
		this.descriptionService.clone(source.id, [...DescriptionEditorHelper.BaseDescriptionLookupFields(), ...DescriptionEditorHelper.DescriptionTemplateInDescriptionLookupFields()])
			.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.busy.set(false)))
			.subscribe({
				next: clone => {
					if (!clone) { return; }
					this.draft.set(clone);
					this.selectedTemplate.set(clone.descriptionTemplate ?? null);
					this.banner.set({ titleKey: 'NEW-DESCRIPTION.COPY.COPIED-FROM', titleParams: { name: source.label }, subtitle: source.descriptionTemplate?.label });
					this.step.set(2);
				},
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
			});
	}

	// ---- last step: name ----

	protected onNameSubmitted(result: NameStepResult): void {
		const draft = this.draft();
		if (draft) {
			this.addDescription.emit({ ...draft, ...NEW_DESCRIPTION_FLAGS, label: result.label, description: result.description || null });
			return;
		}
		const template = this.selectedTemplate();
		if (!template || this.busy()) { return; }
		this.busy.set(true);
		// The list projection only carries the section skeleton; the editor needs the full definition.
		this.descriptionTemplateService.getSingle(template.id, [nameof<DescriptionTemplate>(x => x.id), ...DescriptionTemplateEditorResolver.definitionLookupFields()])
			.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.busy.set(false)))
			.subscribe({
				next: full => this.addDescription.emit({
					...NEW_DESCRIPTION_FLAGS,
					label: result.label,
					description: result.description || null,
					descriptionTemplate: { ...template, definition: full.definition },
				}),
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
			});
	}

	protected onNavigateToParentSection(): void {
		const sectionId = this.sectionId();
		if (sectionId) { this.navigateToParentSection.emit(sectionId); }
	}

	private loadTemplates(groupIds: Guid[]): void {
		if (!groupIds?.length) {
			this.templates.set([]);
			this.templatesLoading.set(false);
			return;
		}
		this.templatesLoading.set(true);
		this.descriptionTemplateService.query(this.descriptionTemplateService.buildLookup({
			groupIds,
			isActive: [IsActive.Active],
			statuses: [DescriptionTemplateStatus.Finalized],
			versionStatuses: [DescriptionTemplateVersionStatus.Current],
			order: { items: [nameof<DescriptionTemplate>(x => x.label)] },
			project: { fields: TEMPLATE_LIST_FIELDS },
		})).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.templatesLoading.set(false)))
			.subscribe({
				next: result => this.templates.set(result?.items ?? []),
				error: error => {
					this.templates.set([]);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			});
	}
}

const definitionPath = (...parts: string[]) => [nameof<DescriptionTemplate>(x => x.definition), ...parts].join('.');

/** Enough of a template to list it, count its fields and preview its structure — not the full field definitions. */
const TEMPLATE_LIST_FIELDS: string[] = [
	nameof<DescriptionTemplate>(x => x.id),
	nameof<DescriptionTemplate>(x => x.groupId),
	nameof<DescriptionTemplate>(x => x.label),
	nameof<DescriptionTemplate>(x => x.description),
	[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
	[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
	[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.code)].join('.'),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.sections)),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.id)),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.title)),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.sections)),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets)),
	definitionPath(nameof<DescriptionTemplateDefinition>(x => x.sections), nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.id)),
];
