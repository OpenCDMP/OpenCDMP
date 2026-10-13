import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { RouterLink } from '@angular/router';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { filter } from 'rxjs';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSectionService } from '../../shared/tc-section.service';
import { TcAddPluginDialogComponent, TcAddPluginDialogData, TcAddPluginDialogResult } from './tc-add-plugin-dialog.component';
import { TC_PLUGIN_KINDS, TcPluginEntry, TcPluginKind, TcPluginSources } from './tc-plugin-sources';

type TcPluginFilter = 'all' | 'on' | 'off' | 'problems';

/** Integrations: every export, deposit and evaluation plugin, switched on or off per tenant. */
@Component({
	selector: 'app-tc-integrations',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, RouterLink, FormsModule, TcSaveBarComponent],
	providers: [TcSectionService],
	templateUrl: './tc-integrations.component.html',
	styleUrl: './tc-integrations.component.scss',
})
export class TcIntegrationsComponent {
	protected readonly section = inject(TcSectionService);
	// each visit to the Plugins list checks reachability again; the overview card reuses the latest answer
	protected readonly sources = new TcPluginSources(this.section, true);
	private readonly routerUtils = inject(RouterUtilsService);
	private readonly dialog = inject(MatDialog);

	protected readonly kinds = TC_PLUGIN_KINDS;
	protected readonly search = signal('');
	protected readonly statusFilter = signal<TcPluginFilter>('all');
	protected readonly filters: TcPluginFilter[] = ['all', 'on', 'off', 'problems'];

	protected readonly visible = computed(() => {
		const term = this.search().trim().toLowerCase();
		const status = this.statusFilter();
		return this.sources.entries().filter(x =>
			(!term || x.id.toLowerCase().includes(term) || x.capabilities.some(c => c.toLowerCase().includes(term)))
			&& (status === 'all'
				|| (status === 'on' && x.enabled)
				|| (status === 'off' && !x.enabled)
				|| (status === 'problems' && x.status === 'unreachable')));
	});

	protected readonly problemCount = computed(() => this.sources.entries().filter(x => x.status === 'unreachable').length);

	constructor() {
		this.section.saved.pipe(takeUntilDestroyed()).subscribe(() => this.sources.refreshAfterSave());
	}

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	protected entriesOf(kind: TcPluginKind): TcPluginEntry[] {
		return this.visible().filter(x => x.kind === kind);
	}

	protected totalOf(kind: TcPluginKind): number {
		return this.sources.entries().filter(x => x.kind === kind).length;
	}

	protected pluginUrl(entry: TcPluginEntry): string {
		return this.routerUtils.generateUrl(`/tenant-configuration/integrations/${entry.kind.slug}/${encodeURIComponent(entry.id)}`);
	}

	protected readOnly(entry: TcPluginEntry): boolean {
		return !this.section.canEdit || this.sources.parts[entry.kind.slug].inherited();
	}

	protected addPlugin(): void {
		const data: TcAddPluginDialogData = { idTaken: (kind, id) => this.sources.idTaken(kind, id) };
		this.dialog.open(TcAddPluginDialogComponent, { data, width: '640px', maxWidth: '95vw', autoFocus: 'first-tabbable' })
			.afterClosed()
			.pipe(filter((x): x is TcAddPluginDialogResult => !!x))
			.subscribe(result => this.sources.addSource(result.kind, result.values));
	}
}
