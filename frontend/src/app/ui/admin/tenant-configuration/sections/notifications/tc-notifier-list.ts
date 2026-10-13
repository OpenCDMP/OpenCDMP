import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, computed, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { NotificationContactType } from '@notification-service/core/enum/notification-contact-type';
import { NotificationType } from '@notification-service/core/enum/notification-type.enum';
import { TenantConfigurationType } from '@notification-service/core/enum/tenant-configuration-type';
import { TenantConfiguration, TenantConfigurationPersist } from '@notification-service/core/model/tenant-configuration';
import { NotifierListLookup } from '@notification-service/core/query/notifier-list.lookup';
import { TenantConfigurationService } from '@notification-service/services/http/tenant-configuration.service';
import { TenantConfigurationEditorModel } from '@notification-service/ui/admin/tenant-configuration/notifier-list/notifier-list-editor.model';
import { NotifierListEditorResolver } from '@notification-service/ui/admin/tenant-configuration/notifier-list/notifier-list-editor.resolver';
import { Observable, catchError, forkJoin, map, of, switchMap, tap, throwError } from 'rxjs';
import { TcSaveable } from '../../shared/tc-section.service';

export type NotifierMap = Record<string, NotificationContactType[]>;

export interface TcNotifierListDependencies {
	tenantConfigurationService: TenantConfigurationService;
	formService: FormService;
	httpErrorHandlingService: HttpErrorHandlingService;
	destroyRef: DestroyRef;
}

/**
 * The notifier list of the notification service (per notification type, the ordered channels OpenCDMP
 * tries), edited on the tenant configuration page. It is a notification-service record, not a main-app
 * tenant configuration, so it has its own adapter, modelled on TcPart, around the notifier list editor model
 * and resolver.
 */
export class TcNotifierList implements TcSaveable {
	readonly record = signal<TenantConfiguration | null>(null);
	/** The channels each notification type supports, in the installation's default order. */
	readonly available = signal<NotifierMap>({});
	readonly form = signal<UntypedFormGroup | null>(null);
	readonly loading = signal(true);
	readonly dirty = signal(false);
	/** The current (possibly unsaved) ordered channel list per notification type, as held by the form. */
	readonly values = signal<NotifierMap>({});
	/** True when the tenant stores its own notifier list; otherwise the installation default applies. */
	readonly changed = computed(() => !!this.record()?.id);

	private model: TenantConfigurationEditorModel | null = null;

	constructor(
		private readonly deps: TcNotifierListDependencies,
		private readonly readOnly: () => boolean,
	) { }

	load(): Observable<boolean> {
		this.loading.set(true);
		return forkJoin({
			available: this.deps.tenantConfigurationService.getNotifierList(new NotifierListLookup()),
			record: this.deps.tenantConfigurationService.getCurrentTenantType(TenantConfigurationType.NotifierList, NotifierListEditorResolver.lookupFields()),
		}).pipe(
			tap(({ available, record }) => {
				this.available.set(available?.notifiers ?? {});
				this.record.set(record ?? null);
				this.rebuild();
				this.loading.set(false);
			}),
			map(() => true),
			catchError(error => {
				this.loading.set(false);
				this.deps.httpErrorHandlingService.handleBackedRequestError(error);
				return of(false);
			}),
			takeUntilDestroyed(this.deps.destroyRef),
		);
	}

	/** The channels a notification type is sent through, first one tried first; never empty for a known type. */
	effective(type: string): NotificationContactType[] {
		const available = this.available()[type] ?? [];
		const stored = (this.values()[type] ?? []).filter(x => available.includes(x));
		return stored.length > 0 ? stored : available;
	}

	/** Turns a channel on (tried last) or off for a notification type. */
	toggle(type: string, channel: NotificationContactType, enabled: boolean): void {
		const current = this.effective(type);
		const next = enabled ? (current.includes(channel) ? current : [...current, channel]) : current.filter(x => x !== channel);
		if (next.length === 0) return;
		this.write(type, next);
	}

	/** Moves a channel to the front of a notification type's list, so it is tried first. */
	tryFirst(type: string, channel: NotificationContactType): void {
		const current = this.effective(type);
		if (!current.includes(channel) || current[0] === channel) return;
		this.write(type, [channel, ...current.filter(x => x !== channel)]);
	}

	/** Throws away unsaved edits. */
	discard(): void {
		this.rebuild();
	}

	/** Saves the notifier list, as the existing editor does, then reloads it. */
	save(): Observable<boolean> {
		const form = this.form();
		if (!form || !this.dirty()) return of(true);
		this.model?.validationErrorModel.clear();
		this.deps.formService.removeAllBackEndErrors(form);
		this.deps.formService.touchAllFormFields(form);
		if (!form.valid) return of(false);
		const persist = this.deps.formService.getValue(form.value) as TenantConfigurationPersist;
		// Empty selections are left out, so the installation default applies to them (as on the existing editor).
		const notifiers = persist?.notifierList?.notifiers;
		if (notifiers) {
			Object.keys(notifiers).forEach(key => {
				if (notifiers[key] == null || notifiers[key].length === 0) delete notifiers[key];
			});
		}
		return this.deps.tenantConfigurationService.persist(persist).pipe(
			switchMap(() => this.load()),
			map(() => true),
			catchError((error: HttpErrorResponse) => {
				const parsed = this.deps.httpErrorHandlingService.getError(error);
				if (parsed.statusCode === 400 && this.model) {
					this.model.validationErrorModel.fromJSONObject(error.error);
					this.deps.formService.validateAllFormFields(form);
				}
				return throwError(() => error);
			}),
		);
	}

	/** Deletes the tenant's own notifier list, so the installation default applies again. */
	reset(): Observable<void> {
		const id = this.record()?.id;
		if (!id) return of(void 0);
		return this.deps.tenantConfigurationService.delete(id).pipe(switchMap(() => this.load()), map(() => void 0));
	}

	private write(type: string, channels: NotificationContactType[]): void {
		const control = this.form()?.get('notifierList')?.get('notifiers')?.get(type) as UntypedFormControl | null;
		if (!control || control.disabled) return;
		control.setValue(channels);
		control.markAsDirty();
		this.sync();
	}

	private rebuild(): void {
		const data = this.record();
		const available = this.available();
		this.model = data?.notifierList?.notifiers ? new TenantConfigurationEditorModel().fromModel(data) : new TenantConfigurationEditorModel();
		const form = this.model.buildForm(Object.keys(available) as NotificationType[], available, null, this.readOnly());
		if (this.readOnly()) form.disable({ emitEvent: false });
		form.valueChanges.pipe(takeUntilDestroyed(this.deps.destroyRef)).subscribe(() => this.sync());
		this.form.set(form);
		this.sync();
		this.dirty.set(false);
	}

	private sync(): void {
		const form = this.form();
		this.values.set({ ...(form?.getRawValue()?.notifierList?.notifiers ?? {}) });
		this.dirty.set(!!form?.dirty);
	}
}
