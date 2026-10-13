import { DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { DepositService } from '@app/core/services/deposit/deposit.service';
import { EvaluatorService } from '@app/core/services/evaluator/evaluator.service';
import { FileTransformerService } from '@app/core/services/file-transformer/file-transformer.service';
import { PluginIntegrationService, PluginSourceOverview, PluginSourceProvider } from '@app/core/services/plugin-integration/plugin-integration.service';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { DepositSourceEditorModel, TenantConfigurationEditorModel as DepositEditorModel } from '@app/ui/admin/tenant-configuration/models/deposit/deposit-editor.model';
import { DepositEditorResolver } from '@app/ui/admin/tenant-configuration/models/deposit/deposit-editor.resolver';
import { EvaluatorSourceEditorModel, TenantConfigurationEditorModel as EvaluatorEditorModel } from '@app/ui/admin/tenant-configuration/models/evaluator/evaluator-editor.model';
import { EvaluatorEditorResolver } from '@app/ui/admin/tenant-configuration/models/evaluator/evaluator-editor.resolver';
import { FileTransformerSourceEditorModel, TenantConfigurationEditorModel as FileTransformerEditorModel } from '@app/ui/admin/tenant-configuration/models/file-transformer/file-transformer-editor.model';
import { FileTransformerEditorResolver } from '@app/ui/admin/tenant-configuration/models/file-transformer/file-transformer-editor.resolver';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TcPart } from '../../shared/tc-part';
import { TcSectionService } from '../../shared/tc-section.service';

/** The three kinds of plugin, with what differs between their stored records. */
export interface TcPluginKind {
	/** Route segment: `integrations/<slug>/<id>`. */
	slug: 'file-transformer' | 'deposit' | 'evaluator';
	pluginType: PluginType;
	type: TenantConfigurationType;
	/** Property of the tenant configuration record holding this kind's sources. */
	formKey: 'fileTransformerPlugins' | 'depositPlugins' | 'evaluatorPlugins';
	/** Property of a source holding the plugin id. */
	idKey: 'transformerId' | 'repositoryId' | 'evaluatorId';
	label: string;
	hint: string;
	icon: string;
}

export const TC_PLUGIN_KINDS: TcPluginKind[] = [
	{ slug: 'file-transformer', pluginType: PluginType.FileTransformer, type: TenantConfigurationType.FileTransformerPlugins, formKey: 'fileTransformerPlugins', idKey: 'transformerId', label: 'TENANT-CONFIGURATION.INTEGRATIONS.KINDS.FILE-TRANSFORMER', hint: 'TENANT-CONFIGURATION.INTEGRATIONS.KINDS.FILE-TRANSFORMER-HINT', icon: 'description' },
	{ slug: 'deposit', pluginType: PluginType.Deposit, type: TenantConfigurationType.DepositPlugins, formKey: 'depositPlugins', idKey: 'repositoryId', label: 'TENANT-CONFIGURATION.INTEGRATIONS.KINDS.DEPOSIT', hint: 'TENANT-CONFIGURATION.INTEGRATIONS.KINDS.DEPOSIT-HINT', icon: 'cloud_upload' },
	{ slug: 'evaluator', pluginType: PluginType.Evaluation, type: TenantConfigurationType.EvaluatorPlugins, formKey: 'evaluatorPlugins', idKey: 'evaluatorId', label: 'TENANT-CONFIGURATION.INTEGRATIONS.KINDS.EVALUATOR', hint: 'TENANT-CONFIGURATION.INTEGRATIONS.KINDS.EVALUATOR-HINT', icon: 'fact_check' },
];

export type TcPluginStatus = 'connected' | 'unreachable' | 'off' | 'unsaved';

/** One plugin row: what the server reported, overlaid with the unsaved edits of this page. */
export interface TcPluginEntry {
	kind: TcPluginKind;
	id: string;
	provider: PluginSourceProvider;
	enabled: boolean;
	status: TcPluginStatus;
	/** Short capability text, e.g. the export formats of a file transformer. */
	capabilities: string[];
	/** How many settings the plugin exposes to the tenant. */
	settingsCount: number;
	url?: string;
}

/**
 * The plugin sources of a section: one tenant configuration part per kind (the tenant's own sources and what it
 * switched off), the server's overview of every source, and the plugins' self-descriptions.
 */
export class TcPluginSources {
	private readonly integrationService = inject(PluginIntegrationService);
	private readonly fileTransformerService = inject(FileTransformerService);
	private readonly depositService = inject(DepositService);
	private readonly evaluatorService = inject(EvaluatorService);
	private readonly pluginConfigurationService = inject(PluginConfigurationService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly destroyRef = inject(DestroyRef);

	readonly parts: Record<TcPluginKind['slug'], TcPart>;
	readonly overview = signal<PluginSourceOverview[] | null>(null);
	readonly loading = computed(() => this.overview() == null || Object.values(this.parts).some(x => x.loading()));

	readonly entries = computed<TcPluginEntry[]>(() => {
		const overview = this.overview() ?? [];
		const result: TcPluginEntry[] = [];
		for (const kind of TC_PLUGIN_KINDS) {
			const value = this.parts[kind.slug].value()?.[kind.formKey];
			const disabled = new Set<string>(value?.disabledSourceIds ?? []);
			const systemOff = !!value?.disableSystemSources;
			const reported = overview.filter(x => x.pluginType === kind.pluginType);

			for (const item of reported.filter(x => x.provider === 'installation')) {
				const enabled = !systemOff && !disabled.has(item.id);
				result.push(this.entry(kind, item.id, 'installation', enabled, item));
			}
			for (const source of (value?.sources ?? []) as Record<string, string>[]) {
				const id = source[kind.idKey];
				if (!id) continue;
				result.push(this.entry(kind, id, 'tenant', !disabled.has(id), reported.find(x => x.provider === 'tenant' && x.id === id), source['url']));
			}
		}
		return result;
	});

	/** recheck: ask the plugins again instead of reusing the last reachability answer. */
	constructor(section: TcSectionService, recheck = false) {
		this.parts = {
			'file-transformer': section.part(TenantConfigurationType.FileTransformerPlugins, FileTransformerEditorResolver.lookupFields(), () => new FileTransformerEditorModel()),
			'deposit': section.part(TenantConfigurationType.DepositPlugins, DepositEditorResolver.lookupFields(), () => new DepositEditorModel()),
			'evaluator': section.part(TenantConfigurationType.EvaluatorPlugins, EvaluatorEditorResolver.lookupFields(), () => new EvaluatorEditorModel()),
		};
		this.loadOverview(recheck);
	}

	/** After saving: the plugin lists the rest of the application uses are loaded once, so reload them too. */
	refreshAfterSave(): void {
		this.reloadOverview();
		this.fileTransformerService.init();
		this.depositService.init();
		this.evaluatorService.init();
	}

	/** Asks the server again which plugins exist and which answer. */
	reloadOverview(): void {
		this.loadOverview(true);
	}

	private loadOverview(reload: boolean): void {
		this.integrationService.getSources(reload).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: items => this.overview.set(items ?? []),
			error: error => {
				this.overview.set([]);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	kind(slug: string): TcPluginKind | undefined {
		return TC_PLUGIN_KINDS.find(x => x.slug === slug);
	}

	find(slug: string, id: string): TcPluginEntry | undefined {
		return this.entries().find(x => x.kind.slug === slug && x.id === id);
	}

	/** Switches a plugin on or off for the tenant. Takes effect when the section is saved. */
	setEnabled(entry: TcPluginEntry, enabled: boolean): void {
		const group = this.group(entry.kind);
		if (!group) return;
		const disabled = new Set<string>(group.get('disabledSourceIds').value ?? []);
		const systemOff = group.get('disableSystemSources');

		// "all installation plugins off" is the older, coarser switch: spell it out per plugin before changing one
		if (entry.provider === 'installation' && systemOff.value) {
			this.entries().filter(x => x.kind === entry.kind && x.provider === 'installation').forEach(x => disabled.add(x.id));
			systemOff.markAsDirty();
			systemOff.setValue(false);
		}
		if (enabled) disabled.delete(entry.id);
		else disabled.add(entry.id);

		const control = group.get('disabledSourceIds');
		control.markAsDirty();
		control.setValue([...disabled]);
		this.parts[entry.kind.slug].refreshDirty();
	}

	/** The tenant's own source of this id, as a form group of the kind's part. */
	sourceForm(kind: TcPluginKind, id: string): UntypedFormGroup | null {
		const sources = this.group(kind)?.get('sources') as UntypedFormArray;
		return (sources?.controls.find(x => x.get(kind.idKey)?.value === id) as UntypedFormGroup) ?? null;
	}

	/** Adds a tenant source with these connection details. Takes effect when the section is saved. */
	addSource(kind: TcPluginKind, values: Record<string, unknown>): void {
		const sources = this.group(kind)?.get('sources') as UntypedFormArray;
		if (!sources) return;
		const part = this.parts[kind.slug];
		const errors = part.validationErrorModel ?? undefined;
		const rootPath = `${kind.formKey}.sources[${sources.length}].`;
		let form: UntypedFormGroup;
		switch (kind.slug) {
			case 'deposit': form = new DepositSourceEditorModel(errors).fromModel(values as never).buildForm({ rootPath }); break;
			case 'evaluator': form = new EvaluatorSourceEditorModel(errors).fromModel(values as never).buildForm({ rootPath }); break;
			default: form = new FileTransformerSourceEditorModel(errors).fromModel(values as never).buildForm({ rootPath }); break;
		}
		sources.push(form);
		sources.markAsDirty();
		part.refreshDirty();
		part.value.set(part.form()?.getRawValue());
	}

	/** Removes one of the tenant's own sources. Takes effect when the section is saved. */
	removeSource(entry: TcPluginEntry): void {
		const group = this.group(entry.kind);
		const sources = group?.get('sources') as UntypedFormArray;
		const index = sources?.controls.findIndex(x => x.get(entry.kind.idKey)?.value === entry.id) ?? -1;
		if (index < 0) return;
		sources.removeAt(index);
		const control = group.get('disabledSourceIds');
		control.setValue((control.value ?? []).filter((x: string) => x !== entry.id));
		sources.markAsDirty();
		this.parts[entry.kind.slug].refreshDirty();
	}

	/** Whether an id is already taken by a plugin of this kind. */
	idTaken(kind: TcPluginKind, id: string): boolean {
		return this.entries().some(x => x.kind === kind && x.id === id);
	}

	private group(kind: TcPluginKind): UntypedFormGroup | null {
		return (this.parts[kind.slug].form()?.get(kind.formKey) as UntypedFormGroup) ?? null;
	}

	private entry(kind: TcPluginKind, id: string, provider: PluginSourceProvider, enabled: boolean, reported: PluginSourceOverview | undefined, url?: string): TcPluginEntry {
		let status: TcPluginStatus;
		if (!enabled) status = 'off';
		// switched on, or added, on this page and not saved yet: the server has not tried it
		else if (!reported || !reported.enabled) status = 'unsaved';
		else status = reported.reachable ? 'connected' : 'unreachable';
		return {
			kind, id, provider, enabled, status, url: url ?? reported?.url,
			capabilities: this.capabilities(kind, id),
			settingsCount: this.pluginConfigurationService.pluginRepositoryConfiguration()?.find(x => x.pluginType === kind.pluginType && x.repositoryId === id)?.fields?.length ?? 0,
		};
	}

	private capabilities(kind: TcPluginKind, id: string): string[] {
		switch (kind.slug) {
			case 'file-transformer': {
				const item = this.fileTransformerService.availableFormats()?.find(x => x.fileTransformerId === id);
				return (item?.exportVariants ?? []).map(x => x.format?.toUpperCase()).filter(Boolean);
			}
			case 'evaluator': {
				const item = this.evaluatorService.availableEvaluators()?.find(x => x.evaluatorId === id);
				return (item?.availableBenchmarks ?? []).map(x => x.label).filter(Boolean).slice(0, 3);
			}
			default:
				return this.depositService.availableDeposits()?.some(x => x.repositoryId === id && x.supportsPreview) ? ['preview'] : [];
		}
	}
}
