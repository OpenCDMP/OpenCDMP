import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, computed, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PristineChangeEvent, UntypedFormGroup, ValueChangeEvent } from '@angular/forms';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { TenantConfiguration, TenantConfigurationPersist } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationService } from '@app/core/services/tenant-configuration/tenant-configuration.service';
import { FormService } from '@common/forms/form-service';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { ValidationContext } from '@common/forms/validation/validation-context';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Observable, catchError, filter, map, of, switchMap, tap, throwError } from 'rxjs';

// The types the application resolves with the default tenant's record when a tenant has none (getActiveType); the plugin lists and the terms are per tenant only.
export const INHERITABLE_TYPES = new Set<TenantConfigurationType>([
	TenantConfigurationType.CssColors,
	TenantConfigurationType.Logo,
	TenantConfigurationType.DefaultUserLocale,
	TenantConfigurationType.DefaultPlanBlueprint,
	TenantConfigurationType.FeaturedEntities,
	TenantConfigurationType.ViewPreferences,
	TenantConfigurationType.PluginConfiguration,
	TenantConfigurationType.GuidedTours,
]);

/** The editor model contract every tenant configuration editor model (under `models/`) follows. */
export interface TcEditorModel {
	validationErrorModel: ValidationErrorModel;
	fromModel(item: TenantConfiguration): TcEditorModel;
	buildForm(context?: ValidationContext, disabled?: boolean): UntypedFormGroup;
}

export interface TcPartDependencies {
	tenantConfigurationService: TenantConfigurationService;
	formService: FormService;
	httpErrorHandlingService: HttpErrorHandlingService;
	destroyRef: DestroyRef;
}

/**
 * One tenant configuration type (one stored record) edited on a section of the tenant configuration page,
 * through its editor model and the lookup fields of its resolver.
 */
export class TcPart {
	readonly record = signal<TenantConfiguration | null>(null);
	readonly form = signal<UntypedFormGroup | null>(null);
	readonly loading = signal(true);
	readonly dirty = signal(false);
	/** The form's raw value (disabled controls included), updated on every change. */
	readonly value = signal<any>(null);
	/** True when the tenant stores its own value for this type; otherwise the installation default applies. */
	readonly changed = computed(() => !!this.record()?.id && this.record()?.belongsToCurrentTenant !== false);
	/** A record inherited from the default tenant is shown read-only. */
	readonly inherited = computed(() => this.record()?.belongsToCurrentTenant === false);

	private model: TcEditorModel | null = null;
	/** The inherited record while the tenant edits its own copy of it, so discarding goes back to it. */
	private inheritedRecord: TenantConfiguration | null = null;

	/** Backend validation errors of the current form, for controls a section adds after loading. */
	get validationErrorModel(): ValidationErrorModel | null {
		return this.model?.validationErrorModel ?? null;
	}

	constructor(
		private readonly deps: TcPartDependencies,
		readonly type: TenantConfigurationType,
		private readonly lookupFields: string[],
		private readonly createModel: () => TcEditorModel,
		private readonly readOnly: () => boolean,
	) { }

	load(): Observable<TenantConfiguration | null> {
		this.loading.set(true);
		const service = this.deps.tenantConfigurationService;
		const request = INHERITABLE_TYPES.has(this.type) ? service.getActiveType(this.type, this.lookupFields) : service.getType(this.type, this.lookupFields);
		return request.pipe(
			map(data => (data ?? null) as TenantConfiguration | null),
			tap(data => {
				this.inheritedRecord = null;
				this.record.set(data);
				this.rebuild();
				this.loading.set(false);
			}),
			catchError(error => {
				this.loading.set(false);
				this.deps.httpErrorHandlingService.handleBackedRequestError(error);
				return of(null);
			}),
			takeUntilDestroyed(this.deps.destroyRef),
		);
	}

	/** Re-reads the form's dirty flag, for callers that mark the form dirty or pristine without changing a value. */
	refreshDirty(): void {
		this.dirty.set(!!this.form()?.dirty);
	}

	/** Throws away unsaved edits. */
	discard(): void {
		if (this.inheritedRecord) {
			this.record.set(this.inheritedRecord);
			this.inheritedRecord = null;
		}
		this.rebuild();
	}

	/** Starts the tenant's own copy of an inherited record; saving the section creates it. */
	customise(): void {
		const data = this.record();
		if (!data || !this.inherited()) return;
		this.inheritedRecord = data;
		this.record.set({ ...data, id: null, hash: null, belongsToCurrentTenant: true });
		this.rebuild();
		this.form()?.markAsDirty();
		this.dirty.set(true);
	}

	/** Validates and saves; resolves to false (and marks the fields) when the form is invalid. */
	save(): Observable<boolean> {
		const form = this.form();
		if (!form || !this.dirty()) return of(true);
		this.deps.formService.removeAllBackEndErrors(form);
		this.deps.formService.touchAllFormFields(form);
		if (!form.valid) return of(false);
		const persist = this.deps.formService.getValue(form.value) as TenantConfigurationPersist;
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

	/** Deletes the tenant's own record, so the installation default applies again. */
	reset(): Observable<void> {
		const id = this.record()?.id;
		if (!id) return of(void 0);
		return this.deps.tenantConfigurationService.delete(id).pipe(switchMap(() => this.load()), map(() => void 0));
	}

	private rebuild(): void {
		const data = this.record();
		this.model = data ? this.createModel().fromModel(data) : this.createModel();
		const disabled = this.readOnly() || this.inherited();
		const form = this.model.buildForm(null, disabled);
		// several of the existing editor models do not pass `disabled` down to their nested groups
		if (disabled) form.disable({ emitEvent: false });
		// value and pristine events both: controls such as the file uploader set a value first and mark it dirty after
		form.events.pipe(
			filter(event => event instanceof ValueChangeEvent || event instanceof PristineChangeEvent),
			takeUntilDestroyed(this.deps.destroyRef),
		).subscribe(() => {
			this.dirty.set(form.dirty);
			this.value.set(form.getRawValue());
		});
		this.form.set(form);
		this.value.set(form.getRawValue());
		this.dirty.set(false);
	}
}
