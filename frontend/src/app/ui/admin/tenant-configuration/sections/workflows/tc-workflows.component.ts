import { ChangeDetectionStrategy, Component, DestroyRef, Signal, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, ReactiveFormsModule } from '@angular/forms';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { DescriptionWorkflow } from '@app/core/model/workflow/description-workflow';
import { PlanStatus } from '@app/core/model/plan-status/plan-status';
import { PlanWorkflow } from '@app/core/model/workflow/plan-workflow';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { DescriptionWorkflowService } from '@app/core/services/description-workflow/description-workflow.service';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { PlanWorkflowService } from '@app/core/services/plan/plan-workflow.service';
import { DescriptionWorkflowEditorModel } from '@app/ui/admin/tenant-configuration/models/description-workflow/description-workflow-editor.model';
import { DescriptionWorkflowEditorResolver } from '@app/ui/admin/tenant-configuration/models/description-workflow/description-workflow-editor.resolver';
import { PlanWorkflowEditorModel } from '@app/ui/admin/tenant-configuration/models/plan-workflow/plan-workflow-editor.model';
import { PlanWorkflowEditorResolver } from '@app/ui/admin/tenant-configuration/models/plan-workflow/plan-workflow-editor.resolver';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { Observable, map } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { TcSaveBarComponent } from '../../shared/tc-save-bar.component';
import { TcSectionService } from '../../shared/tc-section.service';
import { TcStateTagComponent } from '../../shared/tc-state-tag.component';
import { TcWorkflow, TcWorkflowApi, TcWorkflowEditorModel, TcWorkflowRecord } from './tc-workflow';
import { TcStatusDrawerComponent } from './tc-status-drawer.component';
import { TcWorkflowEdge, TcWorkflowLayout, TcWorkflowNode, TcWorkflowStatus, layoutWorkflow, orderWorkflowStatuses } from './tc-workflow-layout';

type TcWorkflowKind = 'plan' | 'description';

/** Everything one tab (plans or assets) of the section shows. */
interface TcWorkflowTab {
	kind: TcWorkflowKind;
	/** i18n key prefix of the texts that differ between plans and assets. */
	texts: string;
	workflow: TcWorkflow<TcWorkflowRecord>;
	statuses: Signal<TcWorkflowStatus[] | null>;
	reloadStatuses: () => void;
	canEditStatus: boolean;
	canDeleteStatus: boolean;
	/** Statuses in diagram order, following the edits. */
	ordered: Signal<TcWorkflowStatus[]>;
	/** Statuses in the order of the saved workflow, so grid rows and columns do not move while editing. */
	gridOrder: Signal<TcWorkflowStatus[]>;
	layout: Signal<TcWorkflowLayout>;
	diagramLabel: Signal<string>;
	startingStatus: Signal<AbstractControl | null>;
	transitions: Signal<AbstractControl | null>;
}

/** Workflows: which status changes people can make on plans and on assets. */
@Component({
	selector: 'app-tc-workflows',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, TcSaveBarComponent, TcStateTagComponent, TcStatusDrawerComponent],
	providers: [TcSectionService],
	templateUrl: './tc-workflows.component.html',
	styleUrl: './tc-workflows.component.scss',
})
export class TcWorkflowsComponent {
	protected readonly section = inject(TcSectionService);
	private readonly authService = inject(AuthService);
	private readonly language = inject(TranslateService);
	private readonly formService = inject(FormService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly planStatusService = inject(PlanStatusService);
	private readonly descriptionStatusService = inject(DescriptionStatusService);
	private readonly isDefaultOrganisation = (this.authService.selectedTenant() ?? 'default') === 'default';

	protected readonly tabs: TcWorkflowTab[] = [
		this.createTab('plan',
			inject(PlanWorkflowService) as TcWorkflowApi<PlanWorkflow>,
			PlanWorkflowEditorResolver.lookupFields(),
			() => new PlanWorkflowEditorModel() as unknown as TcWorkflowEditorModel<PlanWorkflow>,
			AppPermission.EditPlanWorkflow, AppPermission.DeletePlanWorkflow,
			() => this.planStatusService.query(this.planStatusService.buildLookup({ size: 100, lookupFields: this.statusLookupFields<PlanStatus>(), order: [nameof<PlanStatus>(x => x.ordinal)] })).pipe(map(x => x.items as TcWorkflowStatus[])),
			AppPermission.EditPlanStatus, AppPermission.DeletePlanStatus),
		this.createTab('description',
			inject(DescriptionWorkflowService) as TcWorkflowApi<DescriptionWorkflow>,
			DescriptionWorkflowEditorResolver.lookupFields(),
			() => new DescriptionWorkflowEditorModel() as unknown as TcWorkflowEditorModel<DescriptionWorkflow>,
			AppPermission.EditDescriptionWorkflow, AppPermission.DeleteDescriptionWorkflow,
			() => this.descriptionStatusService.query(this.descriptionStatusService.buildLookup({ size: 100, lookupFields: this.statusLookupFields<DescriptionStatus>(), order: [nameof<DescriptionStatus>(x => x.ordinal)] })).pipe(map(x => x.items as TcWorkflowStatus[])),
			AppPermission.EditDescriptionStatus, AppPermission.DeleteDescriptionStatus),
	];

	protected readonly selectedTab = signal(0);
	protected readonly canSave = this.tabs.some(t => t.workflow.canEdit);
	/** The status open in the drawer; a null id is a new status. */
	protected readonly editing = signal<{ tab: TcWorkflowTab; statusId: Guid | null } | null>(null);
	/** The status the pointer or keyboard focus is on, whose arrows are highlighted. */
	protected readonly hovered = signal<string | null>(null);
	private readonly focusKey = computed(() => this.hovered() ?? this.editing()?.statusId?.toString().toLowerCase() ?? null);

	canDeactivate(): boolean {
		return this.section.dirtyCount() === 0;
	}

	protected asControl(control: AbstractControl | null): FormControl {
		return control as FormControl;
	}

	protected openStatus(tab: TcWorkflowTab, node: TcWorkflowNode): void {
		const status = (tab.statuses() ?? []).find(s => s.id?.toString().toLowerCase() === node.id);
		if (status) this.editing.set({ tab, statusId: status.id });
	}

	protected addStatus(tab: TcWorkflowTab): void {
		this.editing.set({ tab, statusId: null });
	}

	protected edgeDimmed(edge: TcWorkflowEdge): boolean {
		const focus = this.focusKey();
		return !!focus && !edge.key.split('>').includes(focus);
	}

	protected nodeDimmed(tab: TcWorkflowTab, node: TcWorkflowNode): boolean {
		const focus = this.focusKey();
		if (!focus || node.id === focus) return false;
		return !tab.layout().edges.some(e => e.key === `${focus}>${node.id}` || e.key === `${node.id}>${focus}`);
	}

	protected transitionLabel(from: TcWorkflowStatus, to: TcWorkflowStatus): string {
		return this.language.instant('TENANT-CONFIGURATION.WORKFLOWS.TRANSITION', { from: from.name, to: to.name });
	}

	protected isSame(a: TcWorkflowStatus, b: TcWorkflowStatus): boolean {
		return a.id?.toString().toLowerCase() === b.id?.toString().toLowerCase();
	}

	/** Backend validation messages about the transitions, shown under the grid. */
	protected transitionErrors(tab: TcWorkflowTab): string[] {
		const definition = tab.workflow.form()?.controls.definition;
		const array = definition?.controls.statusTransitions;
		if (!array) return [];
		const messages = [array, definition, ...array.controls.flatMap(c => [c, c.controls.fromStatusId, c.controls.toStatusId])]
			.filter(c => c.hasError('backendError'))
			.map(c => c.getError('backendError').message as string);
		return [...new Set(messages)];
	}

	private statusLookupFields<T extends PlanStatus | DescriptionStatus>(): string[] {
		return [nameof<T>(x => x.id), nameof<T>(x => x.name), nameof<T>(x => x.ordinal), nameof<T>(x => x.internalStatus)];
	}

	private createTab<TRecord extends TcWorkflowRecord>(
		kind: TcWorkflowKind,
		api: TcWorkflowApi<TRecord>,
		lookupFields: string[],
		createModel: () => TcWorkflowEditorModel<TRecord>,
		editPermission: AppPermission,
		deletePermission: AppPermission,
		loadStatuses: () => Observable<TcWorkflowStatus[]>,
		editStatusPermission: AppPermission,
		deleteStatusPermission: AppPermission,
	): TcWorkflowTab {
		const workflow = new TcWorkflow<TRecord>(
			{ formService: this.formService, httpErrorHandlingService: this.httpErrorHandlingService, destroyRef: this.destroyRef },
			api, [...lookupFields, 'belongsToCurrentTenant'], createModel,
			// the default organisation's workflow is the one every other organisation falls back to, so it cannot be reset
			this.authService.hasPermission(editPermission), this.authService.hasPermission(deletePermission) && !this.isDefaultOrganisation);
		this.section.register(workflow);
		workflow.load().subscribe();

		const statuses = signal<TcWorkflowStatus[] | null>(null);
		const reloadStatuses = (): void => {
			loadStatuses().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: items => statuses.set(items),
				error: error => {
					this.httpErrorHandlingService.handleBackedRequestError(error);
					statuses.set([]);
				},
			});
		};
		reloadStatuses();

		const order = computed(() => {
			const value = workflow.value();
			return orderWorkflowStatuses(statuses() ?? [], value.startingStatusId, value.transitions);
		});
		const ordered = computed(() => order().ordered);
		const gridOrder = computed(() => {
			const saved = workflow.savedValue();
			return orderWorkflowStatuses(statuses() ?? [], saved.startingStatusId, saved.transitions).ordered;
		});
		const layout = computed(() => layoutWorkflow(order().ordered, order().reachable, workflow.value().startingStatusId, workflow.value().transitions));

		const texts = kind === 'plan' ? 'TENANT-CONFIGURATION.WORKFLOWS.PLAN' : 'TENANT-CONFIGURATION.WORKFLOWS.DESCRIPTION';
		const diagramLabel = computed(() => {
			const list = ordered();
			const name = (id: Guid): string => list.find(s => s.id?.toString().toLowerCase() === id?.toString().toLowerCase())?.name ?? '';
			const value = workflow.value();
			const parts = [this.language.instant(`${texts}.DIAGRAM`)];
			const start = name(value.startingStatusId);
			if (start) parts.push(this.language.instant(`${texts}.STARTS-AS`, { status: start }));
			const changes = value.transitions
				.map(t => ({ from: name(t.fromStatusId), to: name(t.toStatusId) }))
				.filter(t => t.from && t.to)
				.map(t => this.language.instant('TENANT-CONFIGURATION.WORKFLOWS.TRANSITION', t));
			parts.push(changes.length
				? this.language.instant('TENANT-CONFIGURATION.WORKFLOWS.DIAGRAM-CHANGES', { changes: changes.join(', ') })
				: this.language.instant('TENANT-CONFIGURATION.WORKFLOWS.DIAGRAM-NO-CHANGES'));
			return parts.join(' ');
		});

		const definition = computed(() => workflow.form()?.controls.definition ?? null);
		const startingStatus = computed(() => definition()?.controls.startingStatusId ?? null);
		const transitions = computed(() => definition()?.controls.statusTransitions ?? null);
		return {
			kind, texts, workflow: workflow as unknown as TcWorkflow<TcWorkflowRecord>, statuses, reloadStatuses,
			canEditStatus: this.authService.hasPermission(editStatusPermission),
			canDeleteStatus: this.authService.hasPermission(deleteStatusPermission),
			ordered, gridOrder, layout, diagramLabel, startingStatus, transitions,
		};
	}
}
