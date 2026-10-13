import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, ReactiveFormsModule, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { PluginConnectionTestResult, PluginIntegrationService } from '@app/core/services/plugin-integration/plugin-integration.service';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { PluginConfigurationEditorResolver } from '@app/ui/admin/tenant-configuration/models/plugin-configuration/plugin-configuration-editor.resolver';
import { PluginEditorModule } from '@app/ui/plugin/plugin-editor.module';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { combineLatest, filter, map, take } from 'rxjs';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSectionService } from '../../shared/tc-section.service';
import { TcStateTagComponent } from '../../shared/tc-state-tag.component';
import { TcConnectionFieldsComponent } from './tc-connection-fields.component';
import { TcExportSettingsComponent } from './export-settings/tc-export-settings.component';
import { TcPluginSettingsModel } from './tc-plugin-settings';
import { TcPluginSources } from './tc-plugin-sources';

/** One plugin: switched on or off, its settings for the tenant, and how the platform connects to it. */
@Component({
	selector: 'app-tc-plugin',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, RouterLink, ReactiveFormsModule, PluginEditorModule, TcSaveBarComponent, TcStateTagComponent, TcConnectionFieldsComponent, TcExportSettingsComponent],
	providers: [TcSectionService],
	templateUrl: './tc-plugin.component.html',
	styleUrl: './tc-plugin.component.scss',
})
export class TcPluginComponent {
	protected readonly section = inject(TcSectionService);
	protected readonly sources = new TcPluginSources(this.section);
	private readonly pluginConfigurationService = inject(PluginConfigurationService);
	private readonly integrationService = inject(PluginIntegrationService);
	private readonly fileTransformerService = inject(FileTransformerService);
	private readonly routerUtils = inject(RouterUtilsService);
	private readonly router = inject(Router);
	private readonly dialog = inject(MatDialog);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);

	// the kind is a parameter of the parent route (`integrations/:kind/:id`)
	private readonly route = inject(ActivatedRoute);
	private readonly params = toSignal(combineLatest([this.route.parent!.paramMap, this.route.paramMap]).pipe(map(([parent, own]) => ({ kind: parent.get('kind'), id: own.get('id') }))));
	protected readonly kind = computed(() => this.sources.kind(this.params()?.kind ?? ''));
	protected readonly id = computed(() => this.params()?.id ?? '');
	protected readonly entry = computed(() => this.sources.find(this.params()?.kind ?? '', this.id()));

	protected readonly repositories = this.pluginConfigurationService.pluginRepositoryConfiguration;
	/** Loaded once the plugins have described their settings, which the form needs to know each field's type. */
	protected readonly settings = this.section.part(
		TenantConfigurationType.PluginConfiguration,
		PluginConfigurationEditorResolver.lookupFields(),
		() => new TcPluginSettingsModel(this.repositories() ?? [], this.pluginConfigurationService.availablePlugins() ?? []),
		{ load: false });
	protected readonly hasSettings = computed(() => !!this.repositories()?.some(x => x.pluginType === this.kind()?.pluginType && x.repositoryId === this.id() && x.fields?.length > 0));
	protected readonly pluginConfigurations = computed(() => this.settings.form()?.get('pluginConfiguration')?.get('pluginConfigurations') as UntypedFormArray | undefined);
	/** The plugin's own settings, as it declares them. */
	protected readonly definitions = computed(() => this.repositories()?.find(x => x.pluginType === this.kind()?.pluginType && x.repositoryId === this.id())?.fields ?? []);
	/** Plugins that group their settings get the dedicated editor; others the generic field list. */
	protected readonly grouped = computed(() => this.definitions().some(x => !!x.group));
	protected readonly pluginForm = computed(() => {
		this.settings.value();
		return (this.pluginConfigurations()?.controls.find(x => x.get('pluginCode')?.value === this.id() && x.get('pluginType')?.value === this.kind()?.pluginType) as UntypedFormGroup) ?? null;
	});
	protected readonly canPreview = computed(() => this.kind()?.slug === 'file-transformer' && !!this.entry()?.capabilities.includes('PDF'));
	protected readonly fileMap = computed(() => {
		const files = new Map<Guid, StorageFile>();
		this.settings.record()?.pluginConfiguration?.pluginConfigurations?.forEach(x => x.fields?.forEach(f => { if (f.fileValue) files.set(f.fileValue.id, f.fileValue); }));
		return files;
	});

	protected readonly connection = computed(() => {
		const kind = this.kind();
		// read the value so a discard, which rebuilds the form, is picked up
		this.sources.parts[kind?.slug ?? 'deposit']?.value();
		return kind ? this.sources.sourceForm(kind, this.id()) : null;
	});
	protected readonly readOnly = computed(() => {
		const kind = this.kind();
		return !this.section.canEdit || (!!kind && this.sources.parts[kind.slug].inherited());
	});

	/** File transformers a deposit plugin can use for the copies it deposits. */
	protected readonly transformerIds = computed(() => (this.fileTransformerService.availableFormats() ?? []).map(x => x.fileTransformerId).filter(Boolean));

	protected readonly testing = signal(false);
	protected readonly testResult = signal<PluginConnectionTestResult | null>(null);

	constructor() {
		let loaded = false;
		effect(() => {
			if (loaded || !this.repositories()) return;
			loaded = true;
			untracked(() => this.settings.load().subscribe());
		});
		this.section.saved.pipe(takeUntilDestroyed()).subscribe(() => this.sources.refreshAfterSave());
	}

	protected control(group: AbstractControl, name: string): UntypedFormControl {
		return group.get(name) as UntypedFormControl;
	}

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	protected get listUrl(): string {
		return this.routerUtils.generateUrl('/tenant-configuration/integrations');
	}

	protected toggle(enabled: boolean): void {
		const entry = this.entry();
		if (entry) this.sources.setEnabled(entry, enabled);
	}

	protected test(): void {
		const kind = this.kind();
		if (!kind) return;
		const form = this.connection();
		if (form) {
			form.markAllAsTouched();
			if (form.invalid) return;
		}
		this.testing.set(true);
		this.testResult.set(null);
		this.integrationService.test({ pluginType: kind.pluginType, id: this.id(), ...(form ? form.getRawValue() : {}) })
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: result => { this.testing.set(false); this.testResult.set(result); },
				error: () => { this.testing.set(false); this.testResult.set({ success: false, reportedId: null, message: null }); },
			});
	}

	/** Removing saves right away: the page is about this plugin and has nothing left to show afterwards. */
	protected remove(): void {
		const entry = this.entry();
		if (!entry || entry.provider !== 'tenant') return;
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('TENANT-CONFIGURATION.INTEGRATIONS.PLUGIN.REMOVE-CONFIRM', { name: entry.id }),
				confirmButton: this.language.instant('TENANT-CONFIGURATION.INTEGRATIONS.PLUGIN.REMOVE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(filter(Boolean), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
			this.section.saved.pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe(() => this.router.navigateByUrl(this.listUrl));
			this.sources.removeSource(entry);
			this.section.saveAll();
		});
	}
}
