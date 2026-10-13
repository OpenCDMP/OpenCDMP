import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, computed, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormGroup } from '@angular/forms';
import { PlanWorkflowPersist } from '@app/core/model/workflow/plan-workflow-persist';
import { PlanWorkflowDefinitionForm, PlanWorkflowDefinitionTransitionForm, PlanWorkflowForm } from '@app/ui/admin/tenant-configuration/models/plan-workflow/plan-workflow-editor.model';
import { FormService } from '@common/forms/form-service';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { ValidationContext } from '@common/forms/validation/validation-context';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';
import { TcSaveable } from '../../shared/tc-section.service';
import { TcWorkflowTransition } from './tc-workflow-layout';

/** The plan and asset workflow forms have the same shape; the plan types stand for both. */
export type TcWorkflowForm = FormGroup<PlanWorkflowForm>;

/** What PlanWorkflowEditorModel and DescriptionWorkflowEditorModel both offer. */
export interface TcWorkflowEditorModel<TRecord> {
	validationErrorModel: ValidationErrorModel;
	fromModel(item: TRecord): TcWorkflowEditorModel<TRecord>;
	buildForm(params: { context?: ValidationContext; disabled?: boolean }): TcWorkflowForm;
	buildStatusTransitionForm(index: number): FormGroup<PlanWorkflowDefinitionTransitionForm>;
	reApplyDefinitionValidators(formGroup: FormGroup<PlanWorkflowDefinitionForm>): void;
}

/** What PlanWorkflowService and DescriptionWorkflowService both offer. */
export interface TcWorkflowApi<TRecord> {
	getCurrent(reqFields?: string[]): Observable<TRecord>;
	persist(item: PlanWorkflowPersist): Observable<unknown>;
	delete(id: Guid): Observable<void>;
}

export interface TcWorkflowRecord {
	id?: Guid;
	belongsToCurrentTenant?: boolean;
}

export interface TcWorkflowDependencies {
	formService: FormService;
	httpErrorHandlingService: HttpErrorHandlingService;
	destroyRef: DestroyRef;
}

export interface TcWorkflowValue {
	startingStatusId: Guid | null;
	transitions: TcWorkflowTransition[];
}

const sameId = (a: Guid | string | null | undefined, b: Guid | string | null | undefined): boolean =>
	!!a && !!b && a.toString().toLowerCase() === b.toString().toLowerCase();

/**
 * The plan or asset workflow of the current tenant, edited on the Workflows section. Not a tenant
 * configuration record: it is read, saved and deleted through its own workflow service, but otherwise
 * behaves like TcPart (editor model, form and validation).
 */
export class TcWorkflow<TRecord extends TcWorkflowRecord> implements TcSaveable {
	readonly record = signal<TRecord | null>(null);
	readonly form = signal<TcWorkflowForm | null>(null);
	readonly loading = signal(true);
	readonly dirty = signal(false);
	/** The current form value, kept as a signal so the diagram and the grid follow edits live. */
	readonly value = signal<TcWorkflowValue>({ startingStatusId: null, transitions: [] });
	/** The value as last loaded, so the grid keeps a stable order while boxes are ticked. */
	readonly savedValue = signal<TcWorkflowValue>({ startingStatusId: null, transitions: [] });
	/** True when the tenant has its own workflow; otherwise it follows the default tenant's, if there is one. */
	readonly changed = computed(() => !!this.record()?.id && this.record()?.belongsToCurrentTenant !== false);
	readonly inherited = computed(() => this.record()?.belongsToCurrentTenant === false);
	readonly readOnly = computed(() => !this.canEdit || this.inherited());

	private model: TcWorkflowEditorModel<TRecord> | null = null;
	/** The inherited workflow while the tenant edits its own copy of it, so discarding goes back to it. */
	private inheritedRecord: TRecord | null = null;

	constructor(
		private readonly deps: TcWorkflowDependencies,
		private readonly api: TcWorkflowApi<TRecord>,
		private readonly lookupFields: string[],
		private readonly createModel: () => TcWorkflowEditorModel<TRecord>,
		readonly canEdit: boolean,
		readonly canReset: boolean,
	) { }

	load(): Observable<TRecord | null> {
		this.loading.set(true);
		return this.api.getCurrent(this.lookupFields).pipe(
			map(data => (data ?? null) as TRecord | null),
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

	discard(): void {
		if (this.inheritedRecord) {
			this.record.set(this.inheritedRecord);
			this.inheritedRecord = null;
		}
		this.rebuild();
	}

	/** Starts the tenant's own copy of the inherited workflow; saving the section creates it. */
	customise(): void {
		const data = this.record();
		if (!data || !this.inherited()) return;
		this.inheritedRecord = data;
		this.record.set({ ...data, id: null, hash: null, belongsToCurrentTenant: true });
		this.rebuild();
		this.form()?.markAsDirty();
		this.dirty.set(true);
	}

	hasTransition(fromStatusId: Guid, toStatusId: Guid): boolean {
		return this.value().transitions.some(t => sameId(t.fromStatusId, fromStatusId) && sameId(t.toStatusId, toStatusId));
	}

	/** Adds or removes the from → to transition entry of the form array. */
	setTransition(fromStatusId: Guid, toStatusId: Guid, allowed: boolean): void {
		const form = this.form();
		if (!form || !this.model || form.disabled || sameId(fromStatusId, toStatusId)) return;
		const definition = form.controls.definition;
		const array = definition.controls.statusTransitions;
		const index = array.controls.findIndex(c => sameId(c.controls.fromStatusId.value, fromStatusId) && sameId(c.controls.toStatusId.value, toStatusId));
		form.markAsDirty();
		if (allowed && index < 0) {
			const entry = this.model.buildStatusTransitionForm(array.length);
			entry.setValue({ fromStatusId, toStatusId });
			array.push(entry);
		} else if (!allowed && index >= 0) {
			array.removeAt(index);
			this.model.reApplyDefinitionValidators(definition);
			array.updateValueAndValidity();
		}
		array.markAsTouched();
		this.dirty.set(true);
	}

	/** Validates and saves; resolves to false (and marks the fields) when the form is invalid. */
	save(): Observable<boolean> {
		const form = this.form();
		if (!form || !this.dirty()) return of(true);
		this.model?.validationErrorModel.clear();
		this.deps.formService.removeAllBackEndErrors(form);
		this.deps.formService.touchAllFormFields(form);
		this.deps.formService.validateAllFormFields(form);
		if (!form.valid) return of(false);
		const persist = JSON.parse(JSON.stringify(form.value)) as PlanWorkflowPersist;
		return this.api.persist(persist).pipe(
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

	/** Deletes the tenant's own workflow, so the installation default applies again. */
	reset(): Observable<void> {
		const id = this.record()?.id;
		if (!id) return of(void 0);
		return this.api.delete(id).pipe(switchMap(() => this.load()), map(() => void 0));
	}

	private rebuild(): void {
		const data = this.record();
		this.model = data ? this.createModel().fromModel(data) : this.createModel();
		const form = this.model.buildForm({ disabled: !this.canEdit || data?.belongsToCurrentTenant === false });
		form.valueChanges.pipe(takeUntilDestroyed(this.deps.destroyRef)).subscribe(() => {
			this.dirty.set(form.dirty);
			this.value.set(this.readValue(form));
		});
		this.form.set(form);
		this.value.set(this.readValue(form));
		this.savedValue.set(this.value());
		this.dirty.set(false);
	}

	private readValue(form: TcWorkflowForm): TcWorkflowValue {
		const definition = form.controls.definition.getRawValue();
		return {
			startingStatusId: definition.startingStatusId ?? null,
			transitions: (definition.statusTransitions ?? []).filter(t => t.fromStatusId && t.toStatusId),
		};
	}
}
