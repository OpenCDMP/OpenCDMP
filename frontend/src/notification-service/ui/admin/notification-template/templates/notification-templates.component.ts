import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { Language } from '@app/core/model/language/language';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageHttpService } from '@app/core/services/language/language.http.service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { EmailOverrideMode } from '@notification-service/core/enum/email-override-mode';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { NotificationTemplateChannel } from '@notification-service/core/enum/notification-template-channel.enum';
import { NotificationTemplateKind } from '@notification-service/core/enum/notification-template-kind.enum';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { NotificationServiceEnumUtils } from '@notification-service/core/formatting/enum-utils.service';
import { NOTIFICATION_GROUP_ORDER, NotificationGroup, notificationGroupOf } from '@notification-service/core/formatting/notification-type-group';
import { NotificationFieldOptions, NotificationTemplate, NotificationTemplateDefault, NotificationTemplatePersist } from '@notification-service/core/model/notification-template.model';
import { NotificationTemplateLookup } from '@notification-service/core/query/notification-template.lookup';
import { NotificationTemplateService } from '@notification-service/services/http/notification-template.service';
import { TranslateService } from '@ngx-translate/core';
import { catchError, filter, of } from 'rxjs';
import { nameof } from 'ts-simple-nameof';

interface TypeRow {
	type: NotificationType;
	name: string;
	channels: NotificationTemplateChannel[];
	customised: boolean;
}

interface Chip {
	key: string;
	label: string;
	missing: boolean;
}

const ALL_CHANNELS = [NotificationTemplateChannel.Email, NotificationTemplateChannel.InApp];

/** Every message the platform sends, with the organisation's own wording where it customised one. */
@Component({
	selector: 'app-notification-templates',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule],
	templateUrl: './notification-templates.component.html',
	styleUrl: './notification-templates.component.scss',
})
export class NotificationTemplatesComponent {
	private readonly templateService = inject(NotificationTemplateService);
	private readonly languageService = inject(LanguageHttpService);
	private readonly authService = inject(AuthService);
	private readonly uiNotificationService = inject(UiNotificationService);
	private readonly httpErrorHandlingService = inject(HttpErrorHandlingService);
	private readonly language = inject(TranslateService);
	private readonly dialog = inject(MatDialog);
	private readonly sanitizer = inject(DomSanitizer);
	private readonly destroyRef = inject(DestroyRef);
	protected readonly enumUtils = inject(NotificationServiceEnumUtils);

	protected readonly channelEnum = NotificationTemplateChannel;
	protected readonly overrideModes = [EmailOverrideMode.NotOverride, EmailOverrideMode.Additive, EmailOverrideMode.Replace];
	protected readonly canEdit = this.authService.hasPermission(AppPermission.EditNotificationTemplate);
	protected readonly canDelete = this.authService.hasPermission(AppPermission.DeleteNotificationTemplate);

	private readonly bodyField = viewChild<ElementRef<HTMLTextAreaElement>>('bodyField');

	protected readonly languages = signal<Language[]>([]);
	protected readonly languageCode = signal<string>('en');
	/** Null while loading; empty when the notification service is too old to list its built-in templates. */
	private readonly defaults = signal<NotificationTemplateDefault[] | null>(null);
	protected readonly defaultsSupported = signal(true);
	private readonly records = signal<NotificationTemplate[]>([]);
	protected readonly selectedType = signal<NotificationType | null>(null);
	protected readonly channel = signal<NotificationTemplateChannel>(NotificationTemplateChannel.Email);
	protected readonly builtIn = signal<NotificationTemplateDefault | null>(null);
	protected readonly busy = signal(false);

	protected readonly form = new FormGroup({
		subjectText: new FormControl<string>('', Validators.required),
		bodyText: new FormControl<string>('', Validators.required),
		cc: new FormControl<string>(''),
		ccMode: new FormControl<EmailOverrideMode>(EmailOverrideMode.NotOverride),
		bcc: new FormControl<string>(''),
		bccMode: new FormControl<EmailOverrideMode>(EmailOverrideMode.NotOverride),
		allowAttachments: new FormControl<boolean>(false),
	});
	private readonly formValue = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });
	protected readonly dirty = signal(false);

	protected readonly rows = computed<{ group: NotificationGroup; rows: TypeRow[] }[]>(() => {
		const defaults = this.defaults() ?? [];
		const channelsByType = new Map<NotificationType, NotificationTemplateChannel[]>();
		if (defaults.length > 0) {
			defaults.forEach(d => channelsByType.set(d.notificationType, [...(channelsByType.get(d.notificationType) ?? []), d.channel]));
		} else {
			Object.values(NotificationType).forEach(type => channelsByType.set(type, ALL_CHANNELS));
		}
		const records = this.records();
		const byGroup = new Map<NotificationGroup, TypeRow[]>();
		channelsByType.forEach((channels, type) => {
			const row: TypeRow = {
				type,
				name: this.enumUtils.toNotificationTypeString(type) || type,
				channels: ALL_CHANNELS.filter(x => channels.includes(x)),
				customised: records.some(r => r.notificationType === type && r.languageCode === this.languageCode()),
			};
			const group = notificationGroupOf(type);
			byGroup.set(group, [...(byGroup.get(group) ?? []), row]);
		});
		return NOTIFICATION_GROUP_ORDER.filter(g => byGroup.has(g)).map(group => ({ group, rows: byGroup.get(group).sort((a, b) => a.name.localeCompare(b.name)) }));
	});

	protected readonly customisedCount = computed(() => this.rows().flatMap(g => g.rows).filter(r => r.customised).length);
	protected readonly typeCount = computed(() => this.rows().flatMap(g => g.rows).length);
	protected readonly selectedRow = computed(() => this.rows().flatMap(g => g.rows).find(r => r.type === this.selectedType()) ?? null);

	protected readonly record = computed(() => this.records().find(r =>
		r.notificationType === this.selectedType() && r.channel === this.channel() && r.languageCode === this.languageCode()) ?? null);

	private readonly subjectOptions = computed(() => this.record()?.value?.subjectFieldOptions ?? this.builtIn()?.value?.subjectFieldOptions ?? this.configOptions('subject'));
	private readonly bodyOptions = computed(() => this.record()?.value?.bodyFieldOptions ?? this.builtIn()?.value?.bodyFieldOptions ?? this.configOptions('body'));

	protected readonly chips = computed<Chip[]>(() => {
		const body = this.formValue().bodyText ?? '';
		const subject = this.formValue().subjectText ?? '';
		const mandatory = new Set([...(this.subjectOptions()?.mandatory ?? []), ...(this.bodyOptions()?.mandatory ?? [])]);
		const optional = [...(this.subjectOptions()?.optional ?? []), ...(this.bodyOptions()?.optional ?? [])].map(x => x.key);
		return [...new Set([...mandatory, ...optional])].map(key => ({
			key,
			label: this.labelOf(key),
			missing: mandatory.has(key) && !body.includes(key) && !subject.includes(key),
		}));
	});

	protected readonly previewSubject = computed(() => this.fillSamples(this.formValue().subjectText ?? ''));
	protected readonly previewBody = computed<SafeHtml>(() =>
		// the preview frame is sandboxed without scripts, so the admin's own HTML is shown as written
		this.sanitizer.bypassSecurityTrustHtml(this.fillSamples(this.formValue().bodyText ?? '')));

	constructor() {
		this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.dirty.set(this.form.dirty));
		if (!this.canEdit) this.form.disable();

		this.languageService.query(this.languageService.buildAutocompleteLookup())
			.pipe(takeUntilDestroyed())
			.subscribe(data => this.languages.set(data.items ?? []));

		this.templateService.getDefaults().pipe(
			catchError(() => {
				this.defaultsSupported.set(false);
				return of([] as NotificationTemplateDefault[]);
			}),
			takeUntilDestroyed(),
		).subscribe(defaults => {
			this.defaults.set(defaults);
			if (!this.selectedType()) {
				const first = this.rows()[0]?.rows[0];
				if (first) this.open(first.type, first.channels[0]);
			}
		});
		this.loadRecords();
	}

	protected select(row: TypeRow): void {
		if (row.type === this.selectedType()) return;
		this.confirmDiscard(() => this.open(row.type, row.channels[0]));
	}

	protected selectChannel(channel: NotificationTemplateChannel): void {
		if (channel === this.channel()) return;
		this.confirmDiscard(() => this.open(this.selectedType(), channel));
	}

	protected selectLanguage(code: string): void {
		if (code === this.languageCode()) return;
		this.confirmDiscard(() => {
			this.languageCode.set(code);
			this.open(this.selectedType(), this.channel());
		});
	}

	protected insert(chip: Chip): void {
		const field = this.bodyField()?.nativeElement;
		const text = this.form.controls.bodyText.value ?? '';
		const at = field ? field.selectionStart ?? text.length : text.length;
		this.form.controls.bodyText.setValue(text.slice(0, at) + chip.key + text.slice(at));
		this.form.controls.bodyText.markAsDirty();
		this.dirty.set(true);
		field?.focus();
	}

	protected save(): void {
		if (this.form.invalid) {
			this.form.markAllAsTouched();
			return;
		}
		const persist = this.buildPersist();
		const record = this.record();
		if (record?.belongsToCurrentTenant !== false) {
			persist.id = record?.id;
			persist.hash = record?.hash;
		}
		this.busy.set(true);
		this.templateService.persist(persist).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
				this.form.markAsPristine();
				this.dirty.set(false);
				this.loadRecords();
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	protected reset(): void {
		const record = this.record();
		if (!record?.id) return;
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('NOTIFICATION-TEMPLATES.RESET-CONFIRM'),
				confirmButton: this.language.instant('NOTIFICATION-TEMPLATES.ACTIONS.RESET'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(filter(Boolean), takeUntilDestroyed(this.destroyRef)).subscribe(() => {
			this.busy.set(true);
			this.templateService.delete(record.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
				next: () => {
					this.records.update(list => list.filter(x => x.id !== record.id));
					this.busy.set(false);
					this.fillForm();
				},
				error: error => {
					this.busy.set(false);
					this.httpErrorHandlingService.handleBackedRequestError(error);
				},
			});
		});
	}

	protected sendTest(): void {
		this.busy.set(true);
		this.templateService.sendTest(this.buildPersist()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: () => {
				this.busy.set(false);
				const key = this.channel() === NotificationTemplateChannel.Email ? 'NOTIFICATION-TEMPLATES.TEST-SENT-EMAIL' : 'NOTIFICATION-TEMPLATES.TEST-SENT-IN-APP';
				this.uiNotificationService.snackBarNotification(this.language.instant(key), SnackBarNotificationLevel.Success);
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	private open(type: NotificationType, channel: NotificationTemplateChannel): void {
		this.selectedType.set(type);
		this.channel.set(channel);
		this.builtIn.set(null);
		this.fillForm();
		if (!this.defaultsSupported()) return;
		this.templateService.getDefault(type, channel, this.languageCode())
			.pipe(catchError(() => of(null)), takeUntilDestroyed(this.destroyRef))
			.subscribe(builtIn => {
				if (this.selectedType() !== type || this.channel() !== channel) return;
				this.builtIn.set(builtIn);
				if (!this.record() && !this.form.dirty) this.fillForm();
			});
	}

	private fillForm(): void {
		const value = this.record()?.value ?? this.builtIn()?.value;
		this.form.reset({
			subjectText: value?.subjectText ?? '',
			bodyText: value?.bodyText ?? '',
			cc: (value?.cc ?? []).join(', '),
			ccMode: value?.ccMode ?? EmailOverrideMode.NotOverride,
			bcc: (value?.bcc ?? []).join(', '),
			bccMode: value?.bccMode ?? EmailOverrideMode.NotOverride,
			allowAttachments: value?.allowAttachments ?? false,
		});
		this.dirty.set(false);
	}

	private loadRecords(): void {
		const lookup = new NotificationTemplateLookup();
		lookup.kinds = [NotificationTemplateKind.Primary];
		lookup.isActive = [IsActive.Active];
		lookup.page = { offset: 0, size: 1000 };
		lookup.order = { items: ['-' + nameof<NotificationTemplate>(x => x.updatedAt)] };
		lookup.project = {
			fields: [
				nameof<NotificationTemplate>(x => x.id),
				nameof<NotificationTemplate>(x => x.channel),
				nameof<NotificationTemplate>(x => x.notificationType),
				nameof<NotificationTemplate>(x => x.languageCode),
				nameof<NotificationTemplate>(x => x.hash),
				nameof<NotificationTemplate>(x => x.updatedAt),
				'belongsToCurrentTenant',
				...['subjectText', 'subjectKey', 'bodyText', 'bodyKey', 'priorityKey', 'allowAttachments', 'cc', 'ccMode', 'bcc', 'bccMode', 'extraDataKeys',
					'subjectFieldOptions.mandatory', 'subjectFieldOptions.optional.key', 'subjectFieldOptions.optional.type', 'subjectFieldOptions.optional.value', 'subjectFieldOptions.formatting',
					'bodyFieldOptions.mandatory', 'bodyFieldOptions.optional.key', 'bodyFieldOptions.optional.type', 'bodyFieldOptions.optional.value', 'bodyFieldOptions.formatting',
				].map(x => `${nameof<NotificationTemplate>(t => t.value)}.${x}`),
			],
		};
		this.templateService.query(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => {
				this.records.set(result.items ?? []);
				this.busy.set(false);
				if (!this.form.dirty) this.fillForm();
			},
			error: error => {
				this.busy.set(false);
				this.httpErrorHandlingService.handleBackedRequestError(error);
			},
		});
	}

	private buildPersist(): NotificationTemplatePersist {
		const raw = this.form.getRawValue();
		const base = this.record()?.value ?? this.builtIn()?.value;
		const split = (text: string) => (text ?? '').split(',').map(x => x.trim()).filter(x => x.length > 0);
		return {
			channel: this.channel(),
			notificationType: this.selectedType(),
			kind: NotificationTemplateKind.Primary,
			languageCode: this.languageCode(),
			value: {
				subjectText: raw.subjectText,
				subjectKey: base?.subjectKey ?? null,
				subjectFieldOptions: this.subjectOptions(),
				bodyText: raw.bodyText,
				bodyKey: base?.bodyKey ?? null,
				bodyFieldOptions: this.bodyOptions(),
				priorityKey: base?.priorityKey ?? null,
				allowAttachments: raw.allowAttachments,
				cc: split(raw.cc),
				ccMode: raw.ccMode,
				bcc: split(raw.bcc),
				bccMode: raw.bccMode,
				extraDataKeys: base?.extraDataKeys ?? [],
			},
		};
	}

	private configOptions(part: 'subject' | 'body'): NotificationFieldOptions | null {
		const entry = (this.defaults() ?? []).find(d => d.notificationType === this.selectedType() && d.channel === this.channel());
		return (part === 'subject' ? entry?.value?.subjectFieldOptions : entry?.value?.bodyFieldOptions) ?? null;
	}

	private fillSamples(text: string): string {
		let out = text.split('{installation-url}').join(window.location.origin).split('{tenant-url-path}').join('');
		this.chips().forEach(chip => out = out.split(chip.key).join(`[${chip.label}]`));
		return out;
	}

	// {planName}, {plan-name} and {planname} all read as "plan name"
	private labelOf(key: string): string {
		return key.replace(/[{}]/g, '').replace(/([a-z])([A-Z])/g, '$1 $2').replace(/[-_]/g, ' ').replace(/planname/i, 'plan name').replace(/planrole/i, 'plan role').toLowerCase();
	}

	private confirmDiscard(proceed: () => void): void {
		if (!this.form.dirty) {
			proceed();
			return;
		}
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant('NOTIFICATION-TEMPLATES.DISCARD-CONFIRM'),
				confirmButton: this.language.instant('NOTIFICATION-TEMPLATES.ACTIONS.DISCARD'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
			},
		}).afterClosed().pipe(filter(Boolean), takeUntilDestroyed(this.destroyRef)).subscribe(() => proceed());
	}
}
