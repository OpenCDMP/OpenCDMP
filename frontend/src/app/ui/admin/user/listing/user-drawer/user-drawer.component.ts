import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AppRole } from '@app/core/common/enum/app-role';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { User } from '@app/core/model/user/user';
import { TenantUserLookup } from '@app/core/query/tenant-user.lookup';
import { UserRoleLookup } from '@app/core/query/user-role.lookup';
import { UserLookup } from '@app/core/query/user.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { nameof } from 'ts-simple-nameof';

interface RoleOption {
	role: AppRole;
	hintKey: string;
}

const TENANT_ROLES: RoleOption[] = [
	{ role: AppRole.TenantAdmin, hintKey: 'USER-LISTING.DRAWER.HINTS.TENANT-ADMIN' },
	{ role: AppRole.TenantPlanManager, hintKey: 'USER-LISTING.DRAWER.HINTS.TENANT-PLAN-MANAGER' },
	{ role: AppRole.TenantConfigManager, hintKey: 'USER-LISTING.DRAWER.HINTS.TENANT-CONFIG-MANAGER' },
	{ role: AppRole.TenantUser, hintKey: 'USER-LISTING.DRAWER.HINTS.TENANT-USER' },
];

const GLOBAL_ROLES: RoleOption[] = [
	{ role: AppRole.Admin, hintKey: 'USER-LISTING.DRAWER.HINTS.ADMIN' },
	{ role: AppRole.InstallationAdmin, hintKey: 'USER-LISTING.DRAWER.HINTS.INSTALLATION-ADMIN' },
	{ role: AppRole.User, hintKey: 'USER-LISTING.DRAWER.HINTS.USER' },
];

/** One person: their contact details and the roles they hold, changed only after a confirmation. */
@Component({
	selector: 'app-user-drawer',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule],
	templateUrl: './user-drawer.component.html',
	styleUrl: './user-drawer.component.scss',
	host: { '(document:keydown.escape)': 'close()' },
})
export class UserDrawerComponent implements OnInit {
	private readonly userService = inject(UserService);
	private readonly authService = inject(AuthService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly language = inject(TranslateService);
	private readonly destroyRef = inject(DestroyRef);
	protected readonly enumUtils = inject(EnumUtils);

	readonly user = input.required<User>();
	/** This organisation's members and their organisation roles; otherwise every account with its installation roles too. */
	readonly tenantMode = input(true);
	readonly tenantName = input<string | null>(null);
	readonly closed = output<void>();
	readonly saved = output<void>();

	protected readonly tenantRoles = TENANT_ROLES;
	protected readonly globalRoles = GLOBAL_ROLES;
	protected readonly draft = signal<AppRole[]>([]);
	private readonly original = signal<AppRole[]>([]);
	protected readonly confirming = signal(false);
	protected readonly busy = signal(false);
	/** Nobody else holds TenantAdmin in the organisation being edited. */
	private readonly lastTenantAdmin = signal(false);
	private readonly isSelf = computed(() => this.user().id?.toString() === this.authService.userId()?.toString());
	// Matches the backend, which lets only a System Administrator give or take away that role.
	private readonly isAdmin = computed(() => this.authService.getSelectedRoles().includes(AppRole.Admin));
	// The default organisation has no membership, so roles there never add or remove anyone.
	private readonly isDefaultOrganisation = (this.authService.selectedTenant() ?? 'default') === 'default';

	protected readonly emails = computed(() => (this.user().contacts ?? []).map(x => x.value).filter(x => !!x));
	// Matches the backend, which refuses role changes for an account without an identity-provider id.
	protected readonly signedIn = computed(() => (this.user().credentials ?? []).some(x => !!x.externalId));
	protected readonly changedRoles = computed(() => {
		const added = this.draft().filter(r => !this.original().includes(r));
		const removed = this.original().filter(r => !this.draft().includes(r));
		return { added, removed, any: added.length + removed.length > 0 };
	});
	protected readonly confirmText = computed(() => {
		const { added, removed } = this.changedRoles();
		const name = this.user().name ?? '';
		const label = (roles: AppRole[]) => roles.map(r => this.enumUtils.toAppRoleString(r)).join(', ');
		const parts: string[] = [];
		if (added.length) parts.push(this.language.instant('USER-LISTING.DRAWER.CONFIRM-ADD', { name, roles: label(added) }));
		if (removed.length) parts.push(this.language.instant('USER-LISTING.DRAWER.CONFIRM-REMOVE', { roles: label(removed) }));
		// The backend removes someone from the organisation when no organisation role is left, and adds a non-member who gets one.
		if (!this.isDefaultOrganisation) {
			const before = this.tenantRolesIn(this.original()).length;
			const after = this.tenantRolesIn(this.draft()).length;
			const organisation = this.tenantName() ?? '';
			if (before > 0 && after === 0) parts.push(this.language.instant('USER-LISTING.DRAWER.CONFIRM-LEAVES', { name, organisation }));
			if (before === 0 && after > 0) parts.push(this.language.instant('USER-LISTING.DRAWER.CONFIRM-JOINS', { name, organisation }));
		}
		return parts.join(' ');
	});

	ngOnInit(): void {
		const user = this.user();
		const tenant = (user.tenantRoles ?? []).map(x => x.role as AppRole);
		const global = (user.globalRoles ?? []).map(x => x.role as AppRole);
		// Saving here needs a global role, so User is always kept, even for an account that never had it.
		const roles = this.tenantMode() ? tenant : [...new Set([AppRole.User, ...global, ...tenant])];
		this.original.set(roles);
		this.draft.set([...roles]);
		if (tenant.includes(AppRole.TenantAdmin) && !this.isSelf()) this.checkLastTenantAdmin();
	}

	/** Why a role can't be changed here, or null when it can. */
	protected lockReason(role: AppRole): string | null {
		const held = this.original().includes(role);
		if (!this.tenantMode() && role === AppRole.User) return 'USER-LISTING.DRAWER.LOCKED.USER';
		if (!this.tenantMode() && role === AppRole.Admin && !this.isAdmin()) return 'USER-LISTING.DRAWER.LOCKED.ADMIN-ONLY';
		if (held && this.isSelf() && (role === AppRole.Admin || role === AppRole.TenantAdmin)) return 'USER-LISTING.DRAWER.LOCKED.SELF';
		if (held && role === AppRole.TenantAdmin && this.lastTenantAdmin()) return 'USER-LISTING.DRAWER.LOCKED.LAST-ADMIN';
		return null;
	}

	private tenantRolesIn(roles: AppRole[]): AppRole[] {
		return roles.filter(r => TENANT_ROLES.some(x => x.role === r));
	}

	// Counts the other people who hold TenantAdmin in the organisation being edited.
	private checkLastTenantAdmin(): void {
		const lookup = new UserLookup();
		lookup.excludedIds = [this.user().id];
		lookup.isActive = [IsActive.Active];
		lookup.userRoleSubQuery = new UserRoleLookup();
		lookup.userRoleSubQuery.roles = [AppRole.TenantAdmin];
		if (!this.isDefaultOrganisation) {
			lookup.tenantUserSubQuery = new TenantUserLookup();
			lookup.tenantUserSubQuery.tenantIds = [this.authService.getSelectedTenantId()];
			lookup.tenantUserSubQuery.isActive = [IsActive.Active];
		}
		lookup.page = { offset: 0, size: 1 };
		lookup.order = { items: [nameof<User>(x => x.id)] };
		lookup.metadata = { countAll: true };
		lookup.project = { fields: [nameof<User>(x => x.id)] };
		this.userService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => this.lastTenantAdmin.set(result.count === 0),
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
		});
	}

	protected close(): void {
		this.closed.emit();
	}

	protected has(role: AppRole): boolean {
		return this.draft().includes(role);
	}

	protected toggle(role: AppRole, on: boolean): void {
		this.confirming.set(false);
		this.draft.update(list => on ? [...list, role] : list.filter(r => r !== role));
	}

	protected askToSave(): void {
		if (this.changedRoles().any) this.confirming.set(true);
	}

	protected confirm(): void {
		this.busy.set(true);
		this.userService.persistRoles({ id: this.user().id, roles: this.draft(), hasTenantAdminMode: this.tenantMode(), hash: this.user().hash })
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: () => {
					this.busy.set(false);
					this.saved.emit();
					this.closed.emit();
				},
				error: error => {
					this.busy.set(false);
					this.confirming.set(false);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			});
	}
}
