import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, ElementRef, OnInit, computed, inject, input, output, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AppRole } from '@app/core/common/enum/app-role';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionStatusAvailableActionType } from '@app/core/common/enum/description-status-available-action-type';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanStatusAvailableActionType } from '@app/core/common/enum/plan-status-available-action-type';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { DescriptionStatus } from '@app/core/model/description-status/description-status';
import { PlanStatus, PlanStatusDefinition, PlanStatusDefinitionAuthorization, PlanStatusDefinitionAuthorizationItem } from '@app/core/model/plan-status/plan-status';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PlanStatusPersist } from '@app/core/model/plan-status/plan-status-persist';
import { DescriptionStatusService } from '@app/core/services/description-status/description-status.service';
import { PlanStatusService } from '@app/core/services/plan/plan-status.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { IconConfigurationEditorComponent } from '@app/ui/iconconfiguration/icon-configuration-editor.component';
import { IconConfigurationEditorModel } from '@app/ui/iconconfiguration/icon-configuration-editor.model';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { Observable, filter, of, switchMap } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { TcWorkflowStatus } from './tc-workflow-layout';

type CountsAs = 'draft' | 'final' | 'cancelled' | 'step';
type AnyStatus = PlanStatus | DescriptionStatus;

interface StatusApi {
	getSingle(id: Guid, fields: string[]): Observable<AnyStatus>;
	persist(item: PlanStatusPersist): Observable<AnyStatus>;
	delete(id: Guid): Observable<AnyStatus>;
}

// plan and asset statuses share these field names
const STATUS_FIELDS: string[] = [
	nameof<PlanStatus>(x => x.id),
	nameof<PlanStatus>(x => x.name),
	nameof<PlanStatus>(x => x.description),
	nameof<PlanStatus>(x => x.action),
	nameof<PlanStatus>(x => x.ordinal),
	nameof<PlanStatus>(x => x.internalStatus),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.roles)].join('.'),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.planRoles)].join('.'),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.allowAnonymous)].join('.'),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.authorization), nameof<PlanStatusDefinitionAuthorization>(x => x.edit), nameof<PlanStatusDefinitionAuthorizationItem>(x => x.allowAuthenticated)].join('.'),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.availableActions)].join('.'),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
	[nameof<PlanStatus>(x => x.definition), nameof<PlanStatusDefinition>(x => x.statusColor)].join('.'),
	nameof<PlanStatus>(x => x.hash),
	nameof<PlanStatus>(x => x.updatedAt),
	nameof<PlanStatus>(x => x.isActive),
	nameof<PlanStatus>(x => x.belongsToCurrentTenant),
];

const SWATCHES = ['#5b6b8c', '#2a5bb7', '#127a74', '#1f7a4d', '#b35c00', '#a1261c', '#6a3fb0'];

/** Edits one plan or asset status from the Workflows diagram: what it is, who can move work into it and where it can go next. */
@Component({
	selector: 'app-tc-status-drawer',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, IconConfigurationEditorComponent],
	templateUrl: './tc-status-drawer.component.html',
	styleUrl: './tc-status-drawer.component.scss',
	host: { '(document:keydown.escape)': 'close()' },
})
export class TcStatusDrawerComponent implements OnInit {
	private readonly planStatusService = inject(PlanStatusService);
	private readonly descriptionStatusService = inject(DescriptionStatusService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly dialog = inject(MatDialog);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly cdr = inject(ChangeDetectorRef);
	protected readonly enumUtils = inject(EnumUtils);

	readonly kind = input.required<'plan' | 'description'>();
	/** Null for a new status. */
	readonly statusId = input<Guid | null>(null);
	readonly statuses = input.required<TcWorkflowStatus[]>();
	readonly canEdit = input(false);
	readonly canDelete = input(false);
	readonly closed = output<void>();
	/** The status list changed and has to be loaded again. */
	readonly changed = output<void>();

	private readonly nameField = viewChild<ElementRef<HTMLInputElement>>('nameField');

	protected readonly planRoles = Object.values(PlanUserRole).filter((x): x is PlanUserRole => typeof x === 'number');
	protected readonly accountRoles = [AppRole.Admin, AppRole.InstallationAdmin, AppRole.User, AppRole.TenantAdmin, AppRole.TenantPlanManager, AppRole.TenantConfigManager, AppRole.TenantUser];
	protected readonly loading = signal(true);
	protected readonly busy = signal(false);
	private readonly loaded = signal<AnyStatus | null>(null);

	protected readonly form = new FormGroup({
		name: new FormControl<string>('', Validators.required),
		statusColor: new FormControl<string>(SWATCHES[0]),
		countsAs: new FormControl<CountsAs>('step'),
		action: new FormControl<string>(''),
		planRoles: new FormControl<PlanUserRole[]>([]),
		roles: new FormControl<AppRole[]>([AppRole.TenantAdmin]),
		availableActions: new FormControl<number[]>([]),
		iconConfiguration: new IconConfigurationEditorModel().buildForm(),
	});

	protected readonly isPlan = computed(() => this.kind() === 'plan');
	// a colour picked earlier outside the palette stays selectable
	protected readonly swatches = computed(() => {
		const current = this.loaded()?.definition?.statusColor;
		return current && !SWATCHES.includes(current) ? [...SWATCHES, current] : SWATCHES;
	});
	protected readonly countsAsOptions = computed<CountsAs[]>(() => this.isPlan() ? ['draft', 'final', 'step'] : ['draft', 'final', 'cancelled', 'step']);
	protected readonly actionOptions = computed<number[]>(() => this.isPlan()
		? Object.values(PlanStatusAvailableActionType).filter((x): x is number => typeof x === 'number')
		: Object.values(DescriptionStatusAvailableActionType).filter((x): x is number => typeof x === 'number'));
	protected readonly title = computed(() => this.loaded()?.name ?? '');
	/** A default-organisation status seen from another organisation: shared, so only the default organisation can change it. */
	protected readonly shared = computed(() => this.loaded()?.belongsToCurrentTenant === false);
	protected readonly editable = computed(() => this.canEdit() && !this.shared());

	ngOnInit(): void {
		// The icon uploader sets its value when the upload finishes, after the click that started it: repaint then.
		this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.cdr.markForCheck());
		if (!this.canEdit()) this.form.disable();
		const id = this.statusId();
		if (!id) {
			this.loading.set(false);
			setTimeout(() => this.nameField()?.nativeElement.focus());
			return;
		}
		this.api().getSingle(id, STATUS_FIELDS).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: status => {
				this.loaded.set(status);
				this.fill(status);
				if (this.shared()) this.form.disable();
				this.loading.set(false);
				setTimeout(() => this.nameField()?.nativeElement.focus());
			},
			error: error => {
				this.loading.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected close(): void {
		this.closed.emit();
	}

	protected togglePlanRole(role: PlanUserRole, on: boolean): void {
		const current = this.form.controls.planRoles.value ?? [];
		this.form.controls.planRoles.setValue(on ? [...current, role] : current.filter(x => x !== role));
		this.form.markAsDirty();
	}

	protected toggleAccountRole(role: AppRole, on: boolean): void {
		const current = this.form.controls.roles.value ?? [];
		this.form.controls.roles.setValue(on ? [...current, role] : current.filter(x => x !== role));
		this.form.markAsDirty();
	}

	protected toggleAction(action: number, on: boolean): void {
		const current = this.form.controls.availableActions.value ?? [];
		this.form.controls.availableActions.setValue(on ? [...current, action] : current.filter(x => x !== action));
		this.form.markAsDirty();
	}

	protected pickColour(colour: string): void {
		this.form.controls.statusColor.setValue(colour);
		this.form.markAsDirty();
	}

	protected actionLabel(action: number): string {
		return this.isPlan()
			? this.enumUtils.toPlanStatusAvailableActionTypeString(action as PlanStatusAvailableActionType)
			: this.enumUtils.toDescriptionStatusAvailableActionTypeString(action as DescriptionStatusAvailableActionType);
	}

	protected save(): void {
		this.form.markAllAsTouched();
		if (this.form.invalid) return;
		this.busy.set(true);
		const status$ = this.form.dirty || !this.statusId() ? this.api().persist(this.buildPersist()) : of(null);
		status$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: saved => {
				this.busy.set(false);
				if (saved) this.changed.emit();
				this.closed.emit();
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected delete(): void {
		const id = this.statusId();
		if (!id) return;
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('TENANT-CONFIGURATION.WORKFLOWS.STATUS.DELETE-CONFIRM', { name: this.title() }),
				confirmButton: this.language.instant('TENANT-CONFIGURATION.WORKFLOWS.STATUS.DELETE'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(
			filter(Boolean),
			switchMap(() => {
				this.busy.set(true);
				return this.api().delete(id);
			}),
			takeUntilDestroyed(this.destroyRef),
		).subscribe({
			next: () => {
				this.busy.set(false);
				this.changed.emit();
				this.closed.emit();
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	private fill(status: AnyStatus): void {
		const edit = status.definition?.authorization?.edit;
		this.form.reset({
			name: status.name ?? '',
			statusColor: status.definition?.statusColor ?? SWATCHES[0],
			countsAs: this.countsAsOf(status.internalStatus),
			action: status.action ?? '',
			planRoles: edit?.planRoles ?? [],
			roles: [...(edit?.roles ?? [])],
			availableActions: [...(status.definition?.availableActions ?? [])],
			iconConfiguration: {
				matIconName: status.definition?.iconConfiguration?.matIconName ?? null,
				storageFileId: status.definition?.iconConfiguration?.storageFile?.id ?? null,
			},
		});
	}

	private buildPersist(): PlanStatusPersist {
		const raw = this.form.getRawValue();
		const status = this.loaded();
		const edit = status?.definition?.authorization?.edit;
		const icon = raw.iconConfiguration;
		const ordinals = this.statuses().map(s => s.ordinal ?? 0);
		return {
			id: status?.id,
			hash: status?.hash,
			name: raw.name,
			description: status?.description ?? null,
			action: raw.action,
			ordinal: status?.ordinal ?? (ordinals.length ? Math.max(...ordinals) + 1 : 0),
			internalStatus: this.internalStatusOf(raw.countsAs) as PlanStatusEnum,
			definition: {
				authorization: {
					edit: {
						roles: raw.roles,
						planRoles: raw.planRoles,
						allowAuthenticated: edit?.allowAuthenticated ?? false,
						allowAnonymous: edit?.allowAnonymous ?? false,
					},
				},
				availableActions: raw.availableActions as PlanStatusAvailableActionType[],
				iconConfiguration: icon?.matIconName || icon?.storageFileId ? { matIconName: icon.matIconName, storageFileId: icon.storageFileId } : null,
				statusColor: raw.statusColor,
			},
		};
	}

	private countsAsOf(internal: PlanStatusEnum | DescriptionStatusEnum | null | undefined): CountsAs {
		if (internal === PlanStatusEnum.Draft) return 'draft';
		if (internal === PlanStatusEnum.Finalized) return 'final';
		if (!this.isPlan() && internal === DescriptionStatusEnum.Canceled) return 'cancelled';
		return 'step';
	}

	private internalStatusOf(countsAs: CountsAs): number | null {
		switch (countsAs) {
			case 'draft': return PlanStatusEnum.Draft;
			case 'final': return PlanStatusEnum.Finalized;
			case 'cancelled': return DescriptionStatusEnum.Canceled;
			default: return null;
		}
	}

	private api(): StatusApi {
		return (this.isPlan() ? this.planStatusService : this.descriptionStatusService) as unknown as StatusApi;
	}
}
