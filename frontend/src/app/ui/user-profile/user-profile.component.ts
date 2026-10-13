import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule, UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { RoleOrganizationType } from '@app/core/common/enum/role-organization-type';
import { ConfigurationField, PluginConfigurationUser, PluginConfigurationUserField } from '@app/core/model/plugin-configuration/plugin-configuration';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { User, UserAdditionalInfo, UserCredential, UserPersist } from '@app/core/model/user/user';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DefaultUserLocaleService } from '@app/core/services/default-user-locale/default-user-locale.service';
import { EnqueueService } from '@app/core/services/enqueue.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { AutoCompleteModule } from '@app/library/auto-complete/auto-complete.module';
import { PopupNotificationDialogComponent } from '@app/library/notification/popup/popup-notification.component';
import { TcSaveBarComponent } from '@app/ui/admin/tenant-configuration/shared/tc-save-bar.component';
import { ReferenceFieldModule } from '@app/ui/reference/reference-field/reference-field.module';
import { FormService } from '@common/forms/form-service';
import { FallbackAvatarDirective } from '@common/http/image/fallback-avatar.directive';
import { UserLogoPipe } from '@common/http/image/user-logo.pipe';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TranslateService } from '@ngx-translate/core';
import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { NOTIFICATION_GROUP_ORDER, NotificationGroup, notificationGroupOf } from '@notification-service/core/formatting/notification-type-group';
import { UserNotificationPreference } from '@notification-service/core/model/user-notification-preference.model';
import { NotifierListLookup } from '@notification-service/core/query/notifier-list.lookup';
import { UserNotificationPreferenceService } from '@notification-service/services/http/user-notification-preference.service';
import { NotifierMap } from '@notification-service/ui/notification-channels-table/notification-channels-table.component';
import { forkJoin, Observable, of, startWith, switchMap } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { AddAccountDialogComponent } from './add-account/add-account-dialog.component';
import { AddAccountDialogModule } from './add-account/add-account-dialog.module';
import { UserProfileEditorModel } from './user-profile-editor.model';

type ProfileTab = 'profile' | 'region' | 'notifications' | 'repositories';

/** Channels shown left to right, as in the organisation's notification settings. */
const CHANNEL_ORDER: NotificationContactType[] = [NotificationContactType.IN_APP, NotificationContactType.EMAIL];

interface NotificationRow {
	type: string;
	name: string;
	channels: NotificationContactType[];
	first: NotificationContactType;
}

interface NotificationGroupView {
	group: NotificationGroup;
	rows: NotificationRow[];
	/** Channels the group's notifications can come first in; one means there is nothing to choose. */
	channels: NotificationContactType[];
	/** The channel all its notifications come first in, or null when they differ. */
	first: NotificationContactType | null;
}

interface RepositoryField {
	config: ConfigurationField;
	group: UntypedFormGroup;
	/** The value loaded: null for a secret, which never reaches the browser. */
	initial: string | null;
}

interface RepositoryAccount {
	code: string;
	name: string;
	group: UntypedFormGroup;
	fields: RepositoryField[];
	connected: boolean;
}

/** The signed-in user's own profile, in four tabs that share one save bar. */
@Component({
	selector: 'app-user-profile',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, AutoCompleteModule, ReferenceFieldModule, AddAccountDialogModule, UserLogoPipe, FallbackAvatarDirective, TcSaveBarComponent],
	templateUrl: './user-profile.component.html',
	styleUrl: './user-profile.component.scss',
})
export class UserProfileComponent {
	private readonly userService = inject(UserService);
	protected readonly authService = inject(AuthService);
	private readonly language = inject(TranslateService);
	protected readonly languageService = inject(LanguageService);
	private readonly configurationService = inject(ConfigurationService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly dialog = inject(MatDialog);
	protected readonly enumUtils = inject(EnumUtils);
	private readonly notificationEnumUtils = inject(NotificationServiceEnumUtils);
	private readonly formService = inject(FormService);
	private readonly referenceTypeService = inject(ReferenceTypeService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly pluginConfigurationService = inject(PluginConfigurationService);
	private readonly notificationPreferenceService = inject(UserNotificationPreferenceService);
	private readonly enqueueService = inject(EnqueueService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly defaultUserLocaleService = inject(DefaultUserLocaleService);

	protected readonly timezoneConfiguration = this.defaultUserLocaleService.singleTimezoneAutocompleteConfiguration;
	protected readonly cultureConfiguration = this.defaultUserLocaleService.singleCultureAutocompleteConfiguration;
	protected readonly languages = this.languageService.getAvailableLanguagesCodes().slice().sort((x, y) => x.localeCompare(y));
	protected readonly roles = Object.values(RoleOrganizationType);

	protected readonly tab = signal<ProfileTab>('profile');
	protected readonly user = signal<User | null>(null);
	protected readonly form = signal<UntypedFormGroup | null>(null);
	protected readonly repositories = signal<RepositoryAccount[]>([]);
	protected readonly organizationReferenceType = signal<ReferenceType | null>(null);
	protected readonly saving = this.enqueueService.exhaustPipelineBusy;

	// notification preferences are stored by the notification service, apart from the profile
	private readonly notifiersAvailable = signal<NotifierMap | null>(null);
	private readonly notifiers = signal<NotifierMap | null>(null);
	private readonly notifiersSaved = signal<string | null>(null);
	protected readonly notifiersFailed = signal(false);
	protected readonly openGroup = signal<NotificationGroup | null>(null);

	protected readonly editing = signal<ReadonlySet<string>>(new Set());
	protected readonly removing = signal<ReadonlySet<string>>(new Set());

	// bumped on every form change so the computed state below re-reads the form
	private readonly formVersion = signal(0);

	protected readonly credentials = computed(() => this.user()?.credentials ?? []);
	protected readonly primaryEmail = computed(() => this.credentials()[0]?.data?.email ?? null);
	protected readonly displayName = computed(() => { this.formVersion(); return this.control('name')?.value || this.user()?.name; });
	protected readonly organizationLabel = computed(() => { this.formVersion(); return this.control('additionalInfo.organization')?.value?.label ?? null; });
	protected readonly roleLabel = computed(() => {
		this.formVersion();
		const role = this.control('additionalInfo.roleOrganization')?.value;
		return role ? this.enumUtils.toRoleOrganizationString(role) : null;
	});

	private readonly aboutDirty = computed(() => { this.formVersion(); return this.isDirty('name', 'additionalInfo.organization', 'additionalInfo.roleOrganization'); });
	private readonly regionDirty = computed(() => { this.formVersion(); return this.isDirty('additionalInfo.language', 'additionalInfo.timezone', 'additionalInfo.culture'); });
	private readonly notificationsDirty = computed(() => this.notifiers() != null && JSON.stringify(this.notifiers()) !== this.notifiersSaved());
	protected readonly dirtyCount = computed(() => {
		this.formVersion();
		return [this.aboutDirty(), this.regionDirty(), this.notificationsDirty(), ...this.repositories().map(x => x.group.dirty)].filter(x => x).length;
	});

	protected readonly dateExample = computed(() => this.example(culture => new Intl.DateTimeFormat(culture, { dateStyle: 'short', timeStyle: 'short' }).format(new Date())));
	protected readonly numberExample = computed(() => this.example(culture => new Intl.NumberFormat(culture).format(1234.5)));

	protected readonly notificationGroups = computed<NotificationGroupView[]>(() => {
		const available = this.notifiersAvailable();
		const current = this.notifiers();
		if (!available || !current) return [];
		const byGroup = new Map<NotificationGroup, NotificationRow[]>();
		Object.keys(available).forEach(type => {
			const channels = this.sortChannels(available[type] ?? []);
			if (!channels.length) return;
			const row: NotificationRow = {
				type,
				name: this.notificationEnumUtils.toNotificationTypeString(type as NotificationType) || type,
				channels,
				first: current[type]?.[0] ?? channels[0],
			};
			const group = notificationGroupOf(type);
			byGroup.set(group, [...(byGroup.get(group) ?? []), row]);
		});
		return NOTIFICATION_GROUP_ORDER.filter(group => byGroup.has(group)).map(group => {
			const rows = byGroup.get(group);
			const channels = this.sortChannels([...new Set(rows.flatMap(x => x.channels))]);
			const choosable = rows.filter(x => x.channels.length > 1);
			const firsts = new Set(choosable.map(x => x.first));
			return { group, rows, channels, first: firsts.size === 1 ? [...firsts][0] : (choosable.length ? null : channels[0]) };
		});
	});

	constructor() {
		inject(AnalyticsService).trackPageView(AnalyticsService.UserProfile);
		this.referenceTypeService.getSingle(Guid.parse(this.referenceTypeService.getOrganizationReferenceType()), [nameof<ReferenceType>(x => x.id), nameof<ReferenceType>(x => x.name)])
			.pipe(takeUntilDestroyed()).subscribe(x => this.organizationReferenceType.set(x));

		// the repositories asking users for credentials are known once the plugins have answered
		toObservable(this.pluginConfigurationService.pluginRepositoryUserConfiguration)
			.pipe(switchMap(x => x ? this.loadUser() : of(null)), takeUntilDestroyed())
			.subscribe({ next: user => user && this.apply(user), error: error => this.httpErrorHandlingService.handleBackedRequestError(error) });

		toObservable(this.form)
			.pipe(switchMap(form => form ? form.valueChanges.pipe(startWith(null)) : of(null)), takeUntilDestroyed())
			.subscribe(() => this.formVersion.update(x => x + 1));

		this.loadNotifications();
	}

	canDeactivate(): boolean {
		return this.dirtyCount() === 0;
	}

	protected control(path: string): UntypedFormControl {
		return this.form()?.get(path) as UntypedFormControl;
	}

	private isDirty(...paths: string[]): boolean {
		return paths.some(x => this.form()?.get(x)?.dirty);
	}

	private example(format: (culture: string) => string): string | null {
		this.formVersion();
		const culture = this.control('additionalInfo.culture')?.value;
		if (!culture) return null;
		try {
			return format(culture);
		} catch {
			return null;
		}
	}

	private loadUser(): Observable<User> {
		const plugin = (...path: string[]) => [nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.pluginConfigurations), ...path].join('.');
		const organization = (field: string) => [nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), field].join('.');
		return this.userService.getSingle(Guid.parse(this.authService.userId()?.toString()), [
			nameof<User>(x => x.id),
			nameof<User>(x => x.name),
			nameof<User>(x => x.hash),
			nameof<User>(x => x.additionalInfo.language),
			nameof<User>(x => x.additionalInfo.timezone),
			nameof<User>(x => x.additionalInfo.culture),
			nameof<User>(x => x.additionalInfo.roleOrganization),
			organization(nameof<Reference>(x => x.id)),
			organization(nameof<Reference>(x => x.label)),
			organization([nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.')),
			organization(nameof<Reference>(x => x.reference)),
			organization(nameof<Reference>(x => x.source)),
			organization(nameof<Reference>(x => x.sourceType)),
			organization(nameof<Reference>(x => x.isActive)),
			plugin(nameof<PluginConfigurationUser>(x => x.pluginCode)),
			plugin(nameof<PluginConfigurationUser>(x => x.pluginType)),
			plugin(nameof<PluginConfigurationUser>(x => x.userFields), nameof<PluginConfigurationUserField>(x => x.code)),
			plugin(nameof<PluginConfigurationUser>(x => x.userFields), nameof<PluginConfigurationUserField>(x => x.textValue)),
			plugin(nameof<PluginConfigurationUser>(x => x.userFields), nameof<PluginConfigurationUserField>(x => x.secretSet)),
			plugin(nameof<PluginConfigurationUser>(x => x.userFields), nameof<PluginConfigurationUserField>(x => x.fileValue), nameof<StorageFile>(x => x.id)),
			`${nameof<User>(x => x.credentials)}.${nameof<UserCredential>(x => x.id)}`,
			`${nameof<User>(x => x.credentials)}.${nameof<UserCredential>(x => x.data.email)}`,
			`${nameof<User>(x => x.credentials)}.${nameof<UserCredential>(x => x.data.externalProviderNames)}`,
		]);
	}

	private apply(user: User): void {
		const configurations = this.pluginConfigurationService.pluginRepositoryUserConfiguration() ?? [];
		// every repository that asks for credentials gets its row, stored values or not
		const stored = user.additionalInfo?.pluginConfigurations ?? [];
		const plugins: PluginConfigurationUser[] = configurations.map(config => {
			const saved = stored.find(x => x.pluginCode === config.repositoryId && x.pluginType === config.pluginType);
			return {
				pluginCode: config.repositoryId,
				pluginType: config.pluginType,
				userFields: config.fields.map(field => saved?.userFields?.find(x => x.code === field.code) ?? { code: field.code, textValue: null, fileValue: null }),
			};
		});
		if (user.additionalInfo) user.additionalInfo.pluginConfigurations = plugins;

		const model = new UserProfileEditorModel().fromModel(user, configurations);
		const form = model.buildForm(this.languageService.getAvailableLanguagesCodes());
		const pluginArray = form.get('additionalInfo').get('pluginConfigurations') as UntypedFormArray;
		this.repositories.set(configurations.map((config, index) => {
			const group = pluginArray.at(index) as UntypedFormGroup;
			const groups = (group.get('userFields') as UntypedFormArray).controls as UntypedFormGroup[];
			const fields = config.fields.map((x, i) => ({ config: x, group: groups[i], initial: groups[i].get('textValue').value as string | null }));
			return {
				code: config.repositoryId,
				name: config.repositoryId.charAt(0).toUpperCase() + config.repositoryId.slice(1),
				group,
				fields,
				connected: fields.some(x => x.config.authInfo ? !!x.group.get('secretSet').value : !!x.initial),
			};
		}));
		this.editing.set(new Set());
		this.removing.set(new Set());
		this.user.set(user);
		this.form.set(form);
		if (this.tab() === 'repositories' && !this.repositories().length) this.tab.set('profile');
	}

	private loadNotifications(): void {
		this.notificationPreferenceService.getNotifierList(new NotifierListLookup())
			.pipe(switchMap(list => forkJoin({
				available: of(list.notifiers as NotifierMap),
				current: this.notificationPreferenceService.current(this.authService.userId(), [
					nameof<UserNotificationPreference>(x => x.type),
					nameof<UserNotificationPreference>(x => x.channel),
					nameof<UserNotificationPreference>(x => x.ordinal),
				]),
			})), takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: ({ available, current }) => {
					const ordered: NotifierMap = {};
					Object.keys(available).forEach(type => {
						const chosen = (current ?? []).filter(x => x.type === type && available[type].includes(x.channel)).sort((x, y) => x.ordinal - y.ordinal).map(x => x.channel);
						ordered[type] = [...chosen, ...available[type].filter(x => !chosen.includes(x))];
					});
					this.notifiersAvailable.set(available);
					this.notifiers.set(ordered);
					this.notifiersSaved.set(JSON.stringify(ordered));
				},
				error: () => this.notifiersFailed.set(true),
			});
	}

	private sortChannels(channels: NotificationContactType[]): NotificationContactType[] {
		const rank = (x: NotificationContactType) => {
			const index = CHANNEL_ORDER.indexOf(x);
			return index < 0 ? CHANNEL_ORDER.length : index;
		};
		return [...channels].sort((x, y) => rank(x) - rank(y) || x - y);
	}

	protected channelName(channel: NotificationContactType): string {
		return this.notificationEnumUtils.toContactTypeString(channel) || String(channel);
	}

	// only the order changes; every available channel is still sent, as the notification service expects
	protected setFirst(types: string[], channel: NotificationContactType): void {
		const next = { ...this.notifiers() };
		types.forEach(type => {
			const current = next[type] ?? [];
			if (current.includes(channel)) next[type] = [channel, ...current.filter((x: NotificationContactType) => x !== channel)];
		});
		this.notifiers.set(next);
	}

	protected setGroupFirst(group: NotificationGroupView, channel: NotificationContactType): void {
		this.setFirst(group.rows.filter(x => x.channels.length > 1).map(x => x.type), channel);
	}

	protected toggleGroup(group: NotificationGroup): void {
		this.openGroup.set(this.openGroup() === group ? null : group);
	}

	protected repositoryStatus(repository: RepositoryAccount): 'connected' | 'not-connected' | 'removed' | 'pending' {
		this.formVersion();
		if (this.removing().has(repository.code)) return 'removed';
		if (this.editing().has(repository.code) && repository.group.dirty) return 'pending';
		return repository.connected ? 'connected' : 'not-connected';
	}

	// a stored secret never reaches the browser: textValue null keeps it, '' removes it, anything else replaces it
	protected editRepository(repository: RepositoryAccount): void {
		this.editing.set(new Set([...this.editing(), repository.code]));
	}

	protected removeRepository(repository: RepositoryAccount): void {
		repository.fields.forEach(x => x.group.get('textValue').setValue(''));
		repository.group.markAsDirty();
		this.removing.set(new Set([...this.removing(), repository.code]));
	}

	protected restoreRepository(repository: RepositoryAccount): void {
		repository.fields.forEach(x => x.group.get('textValue').setValue(x.initial));
		repository.group.markAsPristine();
		this.editing.set(new Set([...this.editing()].filter(x => x !== repository.code)));
		this.removing.set(new Set([...this.removing()].filter(x => x !== repository.code)));
	}

	protected discard(): void {
		const user = this.user();
		if (user) this.apply(user);
		this.notifiers.set(this.notifiersSaved() ? JSON.parse(this.notifiersSaved()) : null);
	}

	protected save(): void {
		const form = this.form();
		if (!form) return;
		this.formService.removeAllBackEndErrors(form);
		this.formService.touchAllFormFields(form);
		if (!form.valid) {
			this.tab.set(form.get('name').invalid ? 'profile' : 'region');
			this.uiNotificationService.snackBarNotification(this.language.instant('TENANT-CONFIGURATION.SAVE-BAR.CHECK-FIELDS'), SnackBarNotificationLevel.Warning);
			return;
		}

		const regionChanged = this.regionDirty();
		const profileChanged = this.aboutDirty() || regionChanged || this.repositories().some(x => x.group.dirty);
		const calls: Observable<unknown>[] = [];
		if (profileChanged) {
			const persist = this.formService.getValue(form.value) as UserPersist;
			if (persist.additionalInfo.organization) persist.additionalInfo.organization.typeId = Guid.parse(this.referenceTypeService.getOrganizationReferenceType());
			calls.push(this.userService.persist(persist));
		}
		if (this.notificationsDirty()) {
			calls.push(this.notificationPreferenceService.persist({ notificationPreferences: this.notifiers(), userId: this.authService.userId() }));
		}
		if (!calls.length) return;

		this.enqueueService.enqueueExhaustChannel(forkJoin(calls).pipe(switchMap(() => profileChanged ? this.authService.refresh() : of(null))))
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: () => {
					if (regionChanged) {
						// dates, numbers and texts already on screen follow the new language and formats only after a reload
						this.languageService.changeLanguage(this.control('additionalInfo.language').value);
						window.location.reload();
						return;
					}
					this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
					this.notifiersSaved.set(JSON.stringify(this.notifiers()));
					if (profileChanged) this.loadUser().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(user => this.apply(user));
				},
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
			});
	}

	protected providerClasses(credential: UserCredential): string[] {
		const names = credential.data?.externalProviderNames ?? [];
		if (!names.length) return [this.configurationService.authProviders.defaultAuthProvider.providerClass];
		return names.map(x => this.configurationService.authProviders.findOrGetDefault(x.toString(), this.user()?.additionalInfo?.language).providerClass).filter(x => x != null);
	}

	protected unlink(credential: UserCredential): void {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT-DIALOG.MESSAGE'),
				confirmButton: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT-DIALOG.CONFIRM'),
				cancelButton: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT-DIALOG.CANCEL')
			},
			maxWidth: '35em'
		}).afterClosed().pipe(
			switchMap(confirmed => confirmed ? this.userService.removeCredentialAccount({ credentialId: credential.id }) : of(null)),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: result => {
				if (result == null) return;
				this.dialog.open(PopupNotificationDialogComponent, {
					data: {
						title: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT.TITLE'),
						message: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT.MESSAGE', { accountToBeUnlinked: credential.data?.email })
					}, maxWidth: '30em'
				});
			},
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
		});
	}

	protected link(): void {
		this.dialog.open(AddAccountDialogComponent, { width: 'min(30vw, 600px)', minWidth: 'fit-content', data: { email: '' } })
			.afterClosed().pipe(
				switchMap(result => result ? this.userService.mergeAccount({ email: result.email }) : of(null)),
				takeUntilDestroyed(this.destroyRef)
			).subscribe({
				next: result => {
					if (!result) return;
					this.dialog.open(PopupNotificationDialogComponent, {
						data: {
							title: this.language.instant('USER-PROFILE.MERGING-EMAILS-DIALOG.TITLE'),
							message: this.language.instant('USER-PROFILE.MERGING-EMAILS-DIALOG.MESSAGE')
						}, maxWidth: '30em'
					});
				},
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error),
			});
	}
}
