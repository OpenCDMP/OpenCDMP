import { Component, inject, OnDestroy, OnInit, TemplateRef, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, UntypedFormArray, UntypedFormGroup, AbstractControl } from '@angular/forms';
import { FieldType } from '@app/core/common/enum/field-type';
import { FieldValidationType } from '@app/core/common/enum/field-validation-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { SystemFieldType } from '@app/core/common/enum/system-field-type';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { SnackBarNotificationLevel } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { TemplateFieldSetEditorModel, TemplateSectionEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { DefinitionTocEntryHelper } from '@app/ui/admin/description-template/editor/table-of-contents/definition-toc-helper';
import { ToCEntry, ToCEntryType } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseEditor } from '@common/base/base-editor';
import { BaseEditorModel } from '@common/base/base-form-editor-model';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { interval, Observable, of, Subject } from 'rxjs';
import { debounceTime, skip, switchMap, takeUntil } from 'rxjs/operators';
import { Data } from '@angular/router';
import { CompareVersionsData, CompareVersionsDialogComponent } from './dialogs/compare-versions/compare-versions.dialog';
import { FinalizePreflightData, FinalizePreflightDialogComponent } from './dialogs/finalize-preflight/finalize-preflight.dialog';
import { MoveToData, MoveToDialogComponent, MoveToResult } from './dialogs/move-to/move-to.dialog';
import { UnsavedChangesData, UnsavedChangesDialogComponent, UnsavedChangesResult } from './dialogs/unsaved-changes/unsaved-changes.dialog';
import { defaultTypeFor, InputKind, RuleLink, ruleLinks } from './input-kinds';
import { BlueprintStructureHelper } from './structure.helper';
import { StructureTransfer, StructureTransferService, TransferItem, TransferPayload } from './structure-transfer';
import { InsertFromData, InsertFromDialogComponent } from './dialogs/insert-from/insert-from.dialog';
import { EditorTab, StructureProblem } from './structure-problems';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { PluginSemantics } from '@app/core/model/semantic/plugin-semantics';
import { SemanticsService } from '@app/core/services/semantic/semantics.service';
import { AttachedUse, pluginKindKey, pluginName, StructureUse } from './plugin-coverage';
import { KnownObject, KnownUsage, knownObjects, knownUsages, optionSemanticsOf, usedMarkers } from './semantic-objects';
import { SemanticCheck, semanticChecks } from './semantic-checks';

/** Draft and Finalized share their values across plan blueprints and description templates. */
export const STATUS_DRAFT = 0;
export const STATUS_FINALIZED = 1;
/** Current, Previous and NotFinalized share their values too. */
export const VERSION_CURRENT = 0;

/** What the header, versions menu and banners need to know about one version of the thing being edited. */
export interface StructureEditorItem {
	id?: Guid;
	label?: string;
	code?: string;
	/** A number on blueprints, a numeric string on description templates. */
	version?: number | string;
	status?: number;
	versionStatus?: number;
	groupId?: Guid;
	usageCount?: number;
	updatedAt?: Date;
	isActive?: IsActive;
	belongsToCurrentTenant?: boolean;
	type?: { name?: string };
	definition?: any;
	hash?: string;
}

type EditorModel = BaseEditorModel & { validationErrorModel: ValidationErrorModel, status?: number };

/**
 * Everything the plan blueprint and description template editors share: the outline and its
 * operations, presets, rules, problems, versions, locking, save, finalize and close. Both forms hold the same
 * section › question › input tree (`TemplateSectionEditorModel` and below), so the structure components work on
 * either through this class; each editor provides itself as `StructureEditorBase` and fills in the hooks.
 */
// a component like BaseEditor, which Angular requires of anything extending it; never declared or rendered itself
@Component({ selector: 'app-se2-structure-editor-base', template: '', changeDetection: ChangeDetectionStrategy.Eager,
 standalone: false })
export abstract class StructureEditorBase<M extends EditorModel, E extends StructureEditorItem> extends BaseEditor<M, E> implements OnInit, OnDestroy {
	declare public language: TranslateService;
	declare public authService: AuthService;
	declare public configurationService: ConfigurationService;
	public readonly enumUtils = inject(EnumUtils);
	public readonly routerUtils = inject(RouterUtilsService);
	public readonly referenceTypeService = inject(ReferenceTypeService);
	protected readonly breadcrumbService = inject(BreadcrumbService);
	protected readonly fileUtils = inject(FileUtils);
	protected readonly transferService = inject(StructureTransferService);
	protected readonly semanticsService = inject(SemanticsService);

	formGroup: FormGroup<any>;
	item: E;

	tab: EditorTab = 'details';
	isClone = false;
	isNewVersion = false;
	isDeleted = false;
	belongsToCurrentTenant = true;
	fileMap = new Map<Guid, StorageFile>([]);
	availableReferenceTypes: ReferenceType[] = [];

	toCEntries: ToCEntry[] = [];
	selectedTocEntry: ToCEntry;
	/** Which question card is open in the pane. Preset questions never open; they are one-liners. */
	expandedQuestionId: string = null;

	problems: StructureProblem[] = [];
	/** Every rule resolved to numbered questions on both ends; refreshed with the problems list. */
	ruleLinks: RuleLink[] = [];
	showProblems = false;
	/** Set once the admin tried to save or finalize; before that, problems stay quiet in the panel only. */
	attemptedSubmit = false;

	versions: E[] = null;
	/** Who holds the edit lock when someone else does, for the banner. */
	lockedByName: string = null;
	saving = false;

	/** Shown under a top-level section in the pane (the blueprint's assets block). Null when the editor has none. */
	rootSectionExtra: TemplateRef<{ $implicit: ToCEntry }> = null;

	readonly structureHelper = BlueprintStructureHelper;
	protected problemsRefresh$ = new Subject<void>();

	// ---------------------------------------------------------------- hooks

	/** The route the editor lives under, e.g. `/plan-blueprints`. */
	abstract readonly urlBase: string;
	abstract readonly lockTargetType: LockTargetType;
	/** Presets that must be present before finalizing, in the order the bar shows them. */
	abstract readonly requiredPresets: SystemFieldType[];
	abstract readonly optionalPresets: SystemFieldType[];
	/** Semantic keys the tenant's other items carry (this group left out), most used first; loaded once. */
	tenantSemantics: { key: string, count: number }[] = [];
	/** A semantic prefix whose questions the outline highlights, set from the keys-in-use card; null for none. */
	semanticHighlight: string = null;
	/** What the Structure tab's left panel lists: the outline, or the keys in use grouped by object. */
	outlineMode: 'outline' | 'keys' = 'outline';
	/** Questions whose answer options show the "Stands for" keys even though none is set yet. */
	answerKeysOn = new Set<string>();
	/** Asks the Review tab's author preview to scroll to a question and highlight it (a question id). */
	readonly previewFocus$ = new Subject<string>();
	protected abstract semanticsInUseRequest(excludeGroupId: Guid): Observable<Record<string, number>>;
	/** What plugins read this structure for: a plan (blueprint) or a description (template). */
	abstract readonly pluginEntityType: PluginEntityType;
	/** The semantics manifests of the tenant's plugins; loaded once, empty when none publishes one. */
	pluginSemantics: PluginSemantics[] = [];
	/** Bumped on every form change, so the semantic checks recompute only when something changed. */
	private semanticRevision = 0;
	private checksMemo: { revision: number, plugins: PluginSemantics[], checks: SemanticCheck[] } = null;

	/** Full i18n keys this editor words differently, mapped to its own keys. */
	protected readonly termOverrides: Record<string, string> = {};

	abstract get sectionsArray(): UntypedFormArray;
	abstract get readOnly(): boolean;
	abstract get canClone(): boolean;
	abstract get canDelete(): boolean;
	abstract get canCreateNewVersion(): boolean;
	/** A new top-level section form, pushed at `index`. */
	protected abstract newRootSectionForm(id: string, index: number, title: string): UntypedFormGroup;
	/** A section form of the right shape for its level, from a raw value (moving between levels, copying). */
	protected abstract sectionFormFor(value: any, root: boolean): UntypedFormGroup;
	protected abstract reapplyValidators(): void;
	protected abstract computeProblems(): StructureProblem[];
	protected abstract persistRequest(formValue: any, newVersion: boolean): Observable<E>;
	protected abstract deleteRequest(id: Guid): Observable<any>;
	protected abstract downloadRequest(id: Guid): Observable<any>;
	protected abstract fetchFull(id: Guid): Observable<E>;
	protected abstract queryVersions(groupId: Guid): Observable<E[]>;
	/** What changed on the Details tab since the last save. */
	protected abstract detailsDirty(): boolean;
	/** How this editor's XML exports are read and imported. */
	abstract refreshData(id?: Guid): void;

	/** The i18n key this editor uses for a shared one. */
	term(key: string): string { return this.termOverrides[key] ?? key; }

	// ---------------------------------------------------------------- lifecycle

	protected initStructureEditor(): void {
		this.initModelFlags(this.route.snapshot.data['action']);
		const requestedTab = this.route.snapshot.queryParamMap.get('tab') as EditorTab;
		if (requestedTab && ['details', 'structure', 'review'].includes(requestedTab)) this.tab = requestedTab;
		this.referenceTypeService.query(ReferenceTypeService.DefaultReferenceTypeLookup(null)).pipe(takeUntil(this._destroyed))
			.subscribe(referenceTypes => this.availableReferenceTypes = referenceTypes.items as ReferenceType[]);
		this.problemsRefresh$.pipe(debounceTime(250), takeUntil(this._destroyed)).subscribe(() => this.recomputeProblems());
		// another item on the same route (another version, a copy pushed into another draft) reuses this editor
		this.route.data.pipe(skip(1), takeUntil(this._destroyed)).subscribe(data => this.openRoutedItem(data));
	}

	private openRoutedItem(data: Data): void {
		const entity = data['entity'] as E;
		if (!entity?.id || entity.id.toString() === this.item?.id?.toString()) return;
		// not unlockTarget: its callback would also stop the timer of the lock taken next
		if (this.isLockedByUser && this.lockedItemId) this.lockService.unlockTarget(this.lockedItemId).pipe(takeUntil(this._destroyed)).subscribe();
		this.touchLock$?.unsubscribe();
		this.touchLock$ = null;
		this.isLockedByUser = false;
		this.isLocked = false;
		this.lockId = null;
		this.lockedByName = null;
		this.selectedTocEntry = null;
		this.expandedQuestionId = null;
		this.attemptedSubmit = false;
		this.showProblems = false;
		this.isNew = false;
		this.initModelFlags(data['action']);
		const requestedTab = this.route.snapshot.queryParamMap.get('tab') as EditorTab;
		if (requestedTab && ['details', 'structure', 'review'].includes(requestedTab)) this.tab = requestedTab;
		this.prepareForm(entity);
	}

	ngOnDestroy(): void {
		super.ngOnDestroy();
		if (this.isLockedByUser) this.unlockTarget(this.editorModel.id);
	}

	protected initModelFlags(action: string): void {
		this.isClone = action == 'clone';
		this.isNewVersion = action == 'new-version';
		if (this.isClone || this.isNewVersion) this.isNew = false;
	}

	/** The shared part of `prepareForm`, once the editor model and form are built. */
	protected afterFormPrepared(data: E): void {
		this.item = data;
		this.isDeleted = data?.isActive === IsActive.Inactive;
		this.belongsToCurrentTenant = this.isNew || this.isClone || data?.belongsToCurrentTenant !== false;
		if (data?.id) this.checkLock(data.id, this.lockTargetType, null, null);
		this.refreshToCEntries();
		if (this.toCEntries.length && !this.selectedTocEntry) this.selectedTocEntry = this.toCEntries[0];
		if (data?.groupId) this.loadVersions(data.groupId);
		if (!this.tenantSemantics.length) this.semanticsInUseRequest(data?.groupId ?? null).pipe(takeUntil(this._destroyed)).subscribe({
			next: counts => this.tenantSemantics = Object.entries(counts ?? {}).map(([key, count]) => ({ key, count })),
			error: () => this.tenantSemantics = []
		});
		if (!this.pluginSemantics.length) this.semanticsService.getPluginSemantics().pipe(takeUntil(this._destroyed)).subscribe({
			next: plugins => this.pluginSemantics = (plugins ?? []).filter(p => p?.semantics?.keys?.length),
			error: () => this.pluginSemantics = []
		});
		this.formGroup.valueChanges.pipe(takeUntil(this._destroyed)).subscribe(() => { this.semanticRevision++; this.problemsRefresh$.next(); });
		this.recomputeProblems();
		// a copy pushed here from another editor lands now, unsaved, so the admin reviews it and saves
		const pending = data?.id ? this.transferService.take(data.id.toString()) : null;
		if (pending && !this.readOnly) this.insertCopies(pending.payload, pending.targetSectionId);
	}

	refreshOnNavigateToData(id?: Guid): void {
		this.formGroup.markAsPristine();
		if (this.isNew || this.isNewVersion || this.isClone) {
			this.router.navigate([this.editorUrl(id)], { queryParams: { tab: this.tab }, replaceUrl: true, relativeTo: this.route });
		} else {
			this.refreshData(id);
		}
	}

	// ---------------------------------------------------------------- urls

	editorUrl(id: Guid | string): string { return this.routerUtils.generateUrl(`${this.urlBase}/${id}`); }
	cloneUrl(id: Guid | string): string { return this.routerUtils.generateUrl(`${this.urlBase}/clone/${id}`); }
	newVersionUrl(id: Guid | string): string { return this.routerUtils.generateUrl(`${this.urlBase}/new-version/${id}`); }
	versionsUrl(groupId: Guid | string): string { return this.routerUtils.generateUrl(`${this.urlBase}/versions/${groupId}`); }
	listingUrl(): string { return this.routerUtils.generateUrl(this.urlBase); }

	// ---------------------------------------------------------------- save, delete, close

	persistEntity(onSuccess?: (response) => void): void {
		this.saving = true;
		const done = (complete) => { this.saving = false; if (onSuccess) onSuccess(complete); else this.onCallbackSuccess(complete); };
		const fail = (error) => { this.saving = false; this.formGroup.get('status').setValue(this.editorModel.status ?? STATUS_DRAFT); this.onCallbackError(error); this.recomputeProblems(); };
		const formData = JSON.parse(JSON.stringify(this.formGroup.value));
		if (!this.isNewVersion) formData.code = this.formGroup.get('code').getRawValue();
		this.persistRequest(formData, this.isNewVersion).pipe(takeUntil(this._destroyed)).subscribe({ next: done, error: fail });
	}

	formSubmit(onSuccess?: () => void): void {
		this.attemptedSubmit = true;
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		this.recomputeProblems();
		if (this.saveBlockers.length) {
			this.showProblems = true;
			this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NOT-SAVED', { count: this.saveBlockers.length }), SnackBarNotificationLevel.Error);
			return;
		}
		this.persistEntity(onSuccess);
	}

	save(): void { this.clearErrorModel(); this.formSubmit(); }

	clearErrorModel() {
		this.editorModel.validationErrorModel.clear();
		this.formService.validateAllFormFields(this.formGroup);
	}

	/** Close asks what to do with unsaved work instead of the generic "leave the page" prompt. */
	close(): void {
		if (!this.dirty || this.readOnly) { this.navigateToListing(); return; }
		this.recomputeProblems();
		const dialogRef = this.dialog.open<UnsavedChangesDialogComponent, UnsavedChangesData, UnsavedChangesResult>(UnsavedChangesDialogComponent, { width: 'min(480px, 95vw)', data: this.unsavedSummary() });
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result === 'discard') { this.formGroup.markAsPristine(); this.navigateToListing(); }
			else if (result === 'save') { this.clearErrorModel(); this.formSubmit(() => { this.formGroup.markAsPristine(); this.navigateToListing(); }); }
		});
	}
	public cancel(): void { this.close(); }
	protected navigateToListing(): void { this.router.navigate([this.listingUrl()]); }

	/** What changed since the last save, counted the way an admin thinks about it. */
	private unsavedSummary(): UnsavedChangesData {
		const saved = new Set<string>();
		const collect = (sections: any[]) => sections?.forEach(s => { if (s?.id) saved.add(s.id.toString()); s?.fieldSets?.forEach(fs => fs?.id && saved.add(fs.id.toString())); collect(s?.sections); });
		collect(this.item?.definition?.sections as any[]);
		let sections = 0, questions = 0, questionsAdded = 0;
		const walk = (array: UntypedFormArray) => array?.controls?.forEach(section => {
			if (!saved.has(section.get('id')?.value?.toString())) sections++;
			((section.get('fieldSets') as UntypedFormArray)?.controls ?? []).forEach(fs => { if (!saved.has(fs.get('id')?.value?.toString())) questionsAdded++; else if (fs.dirty) questions++; });
			walk(section.get('sections') as UntypedFormArray);
		});
		walk(this.sectionsArray);
		return { questions, questionsAdded, sections, details: this.detailsDirty(), canSave: !this.saveBlockers.length };
	}

	public delete() {
		const id = this.formGroup.getRawValue().id;
		if (!id) return;
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '400px',
			data: {
				message: this.language.instant(this.term('PLAN-BLUEPRINT-EDITOR.DELETE.MESSAGE'), { name: this.formGroup.get('label').value }),
				confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
				cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'),
				isDeleteConfirmation: true
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (!result) return;
			this.deleteRequest(id).pipe(takeUntil(this._destroyed)).subscribe({ complete: () => this.onCallbackDeleteSuccess(), error: (error) => this.onCallbackError(error) });
		});
	}

	onCallbackDeleteSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.formGroup.markAsPristine();
		this.close();
	}

	downloadXML(): void {
		const id = this.formGroup.get('id').value;
		if (id == null) return;
		this.downloadRequest(id).pipe(takeUntil(this._destroyed)).subscribe({
			next: response => {
				const blob = new Blob([response.body], { type: 'application/xml' });
				FileSaver.saveAs(blob, this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition')));
			},
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
		});
	}

	/**
	 * Same lock handshake as the base editor, but a lock held by someone else opens the editor read-only with a
	 * banner naming them, instead of a modal that blocks the whole page.
	 */
	protected checkLock(itemId: Guid, targetType: LockTargetType, _title: string, _message: string, retried = false): void {
		if (itemId == null) return;
		this.isNew = false;
		// reloading the item in place (after a save or finalize) must not lock it a second time
		if (this.isLockedByUser && this.lockedItemId?.toString() === itemId.toString()) return;
		this.lockService.checkLockStatus(itemId, ['lock.id', 'lock.lockedBy.name', 'lock.touchedAt']).pipe(takeUntil(this._destroyed)).subscribe(lockStatus => {
			this.isLocked = lockStatus.status;
			if (this.isLocked) {
				this.lockedByName = lockStatus.lock?.lockedBy?.name ?? null;
				this.lockId = lockStatus.lock?.id ?? null;
				this.formGroup.disable();
				return;
			}
			this.lockService.lock(itemId, targetType).pipe(takeUntil(this._destroyed)).subscribe({
				next: () => {
					this.isLockedByUser = true;
					this.lockedItemId = itemId;
					this.touchLock$ = interval(this.configurationService.lockInterval).pipe(takeUntil(this._destroyed)).subscribe(() => this.touchLock(itemId));
				},
				error: () => {
					// opened from another editor of the same item, whose unlock may still be on its way: check once more
					if (!retried) { this.checkLock(itemId, targetType, _title, _message, true); return; }
					this.isLockedByUser = false;
					this.touchLock$?.unsubscribe();
					this.touchLock$ = null;
					this.isLocked = true;
					this.formGroup.disable();
				}
			});
		});
	}

	/** The other person's lock, when someone else holds it; taking over deletes it. */
	lockId: Guid = null;
	private lockedItemId: Guid = null;

	/** Whoever may delete the lock may take over: admins, and for description templates their editors. */
	protected takeOverAllowed(): boolean { return this.authService.hasPermission(AppPermission.DeleteLock); }
	get canTakeOver(): boolean { return this.isLocked && !!this.lockId && !!this.item?.id && this.takeOverAllowed(); }

	/** Takes the edit lock from whoever holds it, after saying what that means for them, then reopens the item editable. */
	takeOver(): void {
		const t = (k: string, p?: object) => this.language.instant(k, p);
		const name = this.lockedByName || t('PLAN-BLUEPRINT-EDITOR.BANNERS.SOMEONE');
		this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '440px',
			data: { message: t('PLAN-BLUEPRINT-EDITOR.BANNERS.TAKE-OVER-CONFIRM', { name }), confirmButton: t('PLAN-BLUEPRINT-EDITOR.BANNERS.TAKE-OVER'), cancelButton: t('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'), isDeleteConfirmation: true }
		}).afterClosed().pipe(takeUntil(this._destroyed)).subscribe(ok => {
			if (!ok) return;
			this.lockService.delete(this.lockId, this.item.id).pipe(takeUntil(this._destroyed)).subscribe({
				next: () => {
					this.isLocked = false;
					this.lockedByName = null;
					this.lockId = null;
					this.uiNotificationService.snackBarNotification(t('PLAN-BLUEPRINT-EDITOR.BANNERS.TAKEN-OVER', { name }), SnackBarNotificationLevel.Success);
					this.refreshData(this.item.id);
				},
				error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
			});
		});
	}

	/** Minutes of inactivity after which another admin's lock lapses. */
	get lockMinutes(): number { return Math.max(1, Math.round((this.configurationService.lockInterval ?? 0) / 60000)); }

	// ---------------------------------------------------------------- state

	get isTransient(): boolean { return this.isNew || this.isClone || this.isNewVersion; }
	get isFinalized(): boolean { return this.editorModel?.status == STATUS_FINALIZED; }
	get canDownloadXML(): boolean { return this.isFinalized && !this.isTransient; }
	get isShared(): boolean { return !this.isNew && !this.isClone && this.belongsToCurrentTenant === false; }
	get dirty(): boolean { return this.formGroup?.dirty ?? false; }
	get currentVersion(): number { const v = this.item?.version; return v == null || v === '' ? null : Number(v); }
	get sectionCount(): number { return this.sectionsArray?.length ?? 0; }
	get blockers(): StructureProblem[] { return this.problems.filter(p => p.severity === 'blocker'); }
	/** Problems that stop a save: a save must at least be a valid form; finalize adds the preset rules. */
	get saveBlockers(): StructureProblem[] { return this.problems.filter(p => p.severity === 'blocker' && p.blocksSave); }
	get optionalProblems(): StructureProblem[] { return this.problems.filter(p => p.severity === 'optional'); }
	get iconName(): string { return this.formGroup?.controls?.definition?.get('iconConfiguration')?.get('matIconName')?.value ?? null; }
	get iconSet(): boolean { const icon = this.formGroup?.controls?.definition?.get('iconConfiguration'); return !!icon?.get('matIconName')?.value || !!icon?.get('storageFileId')?.value; }

	setTab(tab: EditorTab): void {
		this.tab = tab;
		this.router.navigate([], { relativeTo: this.route, queryParams: { tab }, queryParamsHandling: 'merge', replaceUrl: true });
	}

	// ---------------------------------------------------------------- structure: outline

	refreshToCEntries(): ToCEntry[] {
		this.toCEntries = DefinitionTocEntryHelper.getTocEntries(this.sectionsArray as any);
		// entries capture the title when built; read it live so the outline, breadcrumbs and rule targets follow typing
		const live = (list: ToCEntry[]) => list?.forEach(e => {
			Object.defineProperty(e, 'label', { get: () => e.form?.get('title')?.value ?? '', configurable: true, enumerable: true });
			live(e.subEntries);
		});
		live(this.toCEntries);
		if (this.selectedTocEntry) this.selectedTocEntry = DefinitionTocEntryHelper.findTocEntryById(this.selectedTocEntry.id, this.toCEntries);
		return this.toCEntries;
	}

	selectEntry(entry: ToCEntry): void {
		this.selectedTocEntry = entry;
		if (this.tab !== 'structure') this.setTab('structure');
	}

	selectById(id: string): void {
		const entry = DefinitionTocEntryHelper.findTocEntryById(id, this.toCEntries);
		if (entry) this.selectEntry(entry);
	}

	afterStructureChange(selectId?: string): void {
		this.refreshToCEntries();
		DefinitionTocEntryHelper.updateOrdinals(this.toCEntries);
		this.refreshToCEntries();
		this.reapplyValidators();
		this.formGroup.markAsDirty();
		this.formGroup.updateValueAndValidity();
		if (selectId) this.selectById(selectId);
		this.problemsRefresh$.next();
	}

	/** Adds a section under `parent` (a top-level section when `parent` is null). Returns the new entry id. */
	addSection(parent: ToCEntry | null, title?: string): string {
		const id = Guid.create().toString();
		if (!parent) {
			this.sectionsArray.push(this.newRootSectionForm(id, this.sectionsArray.length, title ?? null));
		} else {
			const sectionsArray = parent.form.get('sections') as UntypedFormArray;
			const section = new TemplateSectionEditorModel(this.editorModel.validationErrorModel);
			section.id = id;
			section.ordinal = sectionsArray.length;
			section.title = title ?? null;
			sectionsArray.push(section.buildForm({ rootPath: `${parent.validationRootPath}.sections[${sectionsArray.length}].` }));
		}
		this.afterStructureChange(id);
		return id;
	}

	/** Adds a question with one input of the given type to the section entry. */
	addQuestion(sectionEntry: ToCEntry, type: FieldType = FieldType.FREE_TEXT, preset?: SystemFieldType): string {
		if (!sectionEntry || sectionEntry.type !== ToCEntryType.Section) return null;
		const fieldSets = sectionEntry.form.get('fieldSets') as UntypedFormArray;
		const field = BlueprintStructureHelper.newField({ type, ordinal: 0, maxFileSizeInMB: this.configurationService.maxFileSizeInMB, systemFieldType: preset });
		const title = preset != null ? this.enumUtils.toSystemFieldTypeString(preset) : null;
		if (preset != null) (field.data as any).label = title;
		const fieldSetForm = BlueprintStructureHelper.newFieldSetForm({
			validationErrorModel: this.editorModel.validationErrorModel,
			ordinal: fieldSets.length,
			title,
			field,
			rootPath: `${sectionEntry.validationRootPath}.fieldSets[${fieldSets.length}].`
		});
		fieldSets.push(fieldSetForm);
		const id = fieldSetForm.get('id').value;
		this.afterStructureChange(sectionEntry.id);
		this.expandedQuestionId = preset != null ? null : id;
		return id;
	}

	/** Adds a question whose single input is of the given kind (a kind may sit on a type plus a flag). */
	addQuestionOfKind(sectionEntry: ToCEntry, kind: InputKind): string {
		const id = this.addQuestion(sectionEntry, defaultTypeFor(kind));
		if (kind === 'multiple' && id) {
			const fieldSets = DefinitionTocEntryHelper.findTocEntryById(sectionEntry.id, this.toCEntries)?.form.get('fieldSets') as UntypedFormArray;
			const fieldSet = fieldSets?.controls.find(c => c.get('id').value === id);
			BlueprintStructureHelper.firstFieldForm(fieldSet as UntypedFormGroup)?.get('data')?.get('multipleSelect')?.setValue(true);
		}
		return id;
	}

	toggleQuestion(id: string): void { this.expandedQuestionId = this.expandedQuestionId === id ? null : id; }

	removeEntry(entry: ToCEntry): void {
		const isSection = entry.type === ToCEntryType.Section;
		const questions = isSection ? BlueprintStructureHelper.countQuestions(entry) : 0;
		const message = isSection
			? this.language.instant(questions ? 'PLAN-BLUEPRINT-EDITOR.DELETE.SECTION-WITH-QUESTIONS' : 'PLAN-BLUEPRINT-EDITOR.DELETE.SECTION', { name: entry.label || this.language.instant('PLAN-BLUEPRINT-EDITOR.UNTITLED-SECTION'), count: questions })
			: this.language.instant('PLAN-BLUEPRINT-EDITOR.DELETE.QUESTION', { name: entry.label || this.language.instant('PLAN-BLUEPRINT-EDITOR.UNTITLED-QUESTION') });
		const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
			maxWidth: '420px',
			data: { message, confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'), cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL'), isDeleteConfirmation: true }
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => { if (result) this.deleteEntry(entry); });
	}

	private deleteEntry(entry: ToCEntry): void {
		const path = BlueprintStructureHelper.pathTo(entry.id, this.toCEntries) ?? [];
		const parentEntry = path.length > 1 ? path[path.length - 2] : null;
		const parentArray = entry.form.parent as UntypedFormArray;
		const index = parentArray.controls.findIndex(c => c.get('id').value === entry.id);
		if (index < 0) return;
		parentArray.removeAt(index);
		BlueprintStructureHelper.renumber(parentArray);
		if (this.expandedQuestionId === entry.id) this.expandedQuestionId = null;
		const wasSelected = this.selectedTocEntry && (this.selectedTocEntry.id === entry.id || DefinitionTocEntryHelper.tocEntryIsChildOf(this.selectedTocEntry, entry));
		this.afterStructureChange();
		if (wasSelected) this.selectedTocEntry = parentEntry ? DefinitionTocEntryHelper.findTocEntryById(parentEntry.id, this.toCEntries) : (this.toCEntries[0] ?? null);
	}

	cloneQuestion(entry: ToCEntry): void {
		if (entry.type !== ToCEntryType.FieldSet) return;
		const values = (entry.form as UntypedFormGroup).getRawValue();
		const parentArray = entry.form.parent as UntypedFormArray;
		const index = parentArray.controls.findIndex(c => c.get('id').value === entry.id);
		values.id = Guid.create().toString();
		values.ordinal = index + 1;
		values.fields.forEach(f => f.id = Guid.create().toString());
		const cloned = new TemplateFieldSetEditorModel(this.editorModel.validationErrorModel).fromModel(values).buildForm({ rootPath: `${entry.validationRootPath}.fieldSets[${index + 1}].` });
		parentArray.insert(index + 1, cloned);
		BlueprintStructureHelper.renumber(parentArray);
		this.afterStructureChange();
		this.expandedQuestionId = cloned.get('id').value;
	}

	moveEntry(entry: ToCEntry, direction: -1 | 1): void {
		const parentArray = entry.form.parent as UntypedFormArray;
		const index = parentArray.controls.findIndex(c => c.get('id').value === entry.id);
		const target = index + direction;
		if (index < 0 || target < 0 || target >= parentArray.length) return;
		const control = parentArray.at(index);
		parentArray.removeAt(index);
		parentArray.insert(target, control);
		BlueprintStructureHelper.renumber(parentArray);
		this.afterStructureChange();
	}

	// ---------------------------------------------------------------- structure: moving and copying across sections

	/**
	 * Why an entry cannot go under `target` (null = top level), or null when it can. A section holds either
	 * subsections or questions; assets live on top-level sections only; nothing moves into itself.
	 */
	moveBlockReason(entry: ToCEntry, target: ToCEntry | null): string {
		const t = (k: string) => this.language.instant(k);
		if (entry.type === ToCEntryType.FieldSet) {
			if (!target) return t('PLAN-BLUEPRINT-EDITOR.MOVE.NO-QUESTION-TOP');
			if (target.type !== ToCEntryType.Section) return t('PLAN-BLUEPRINT-EDITOR.MOVE.NOT-A-SECTION');
			if (BlueprintStructureHelper.hasSubsections(target)) return t('PLAN-BLUEPRINT-EDITOR.MOVE.HOLDS-SUBSECTIONS');
			return null;
		}
		if (target) {
			if (target.id === entry.id || DefinitionTocEntryHelper.tocEntryIsChildOf(target, entry)) return t('PLAN-BLUEPRINT-EDITOR.MOVE.INTO-ITSELF');
			if (BlueprintStructureHelper.hasQuestions(target)) return t('PLAN-BLUEPRINT-EDITOR.MOVE.HOLDS-QUESTIONS');
			if (entry.root && entry.form.get('hasTemplates')?.value) return t('PLAN-BLUEPRINT-EDITOR.MOVE.COLLECTS-ASSETS');
		}
		return null;
	}

	private childArrayOf(entry: ToCEntry, target: ToCEntry | null): UntypedFormArray {
		if (!target) return this.sectionsArray;
		return target.form.get(entry.type === ToCEntryType.FieldSet ? 'fieldSets' : 'sections') as UntypedFormArray;
	}

	/** Moves (or, with `copy`, copies) a section or question under another parent at the given index. */
	moveEntryTo(entry: ToCEntry, target: ToCEntry | null, index: number, copy = false): string {
		if (this.moveBlockReason(entry, target)) return null;
		const sourceArray = entry.form.parent as UntypedFormArray;
		const targetArray = this.childArrayOf(entry, target);
		if (!sourceArray || !targetArray) return null;
		const sourceIndex = sourceArray.controls.findIndex(c => c.get('id').value === entry.id);
		let control: UntypedFormGroup;
		if (copy) {
			control = this.copyOf(entry, target);
		} else {
			const levelChanges = entry.type === ToCEntryType.Section && (!target) !== !!entry.root;
			control = entry.form as UntypedFormGroup;
			sourceArray.removeAt(sourceIndex);
			BlueprintStructureHelper.renumber(sourceArray);
			if (levelChanges) control = this.sectionFormFor(control.getRawValue(), !target);
			if (sourceArray === targetArray && sourceIndex < index) index--;
		}
		const at = Math.max(0, Math.min(index, targetArray.length));
		targetArray.insert(at, control);
		BlueprintStructureHelper.renumber(targetArray);
		const id = control.get('id').value;
		this.afterStructureChange(entry.type === ToCEntryType.Section ? id : (target?.id ?? null));
		if (entry.type === ToCEntryType.FieldSet) this.expandedQuestionId = null;
		return id;
	}

	/**
	 * A copy with new ids. Rules pointing inside the copied part follow the copy; rules pointing elsewhere stay as
	 * they are. Preset questions are left out, since each preset exists once per structure.
	 */
	private copyOf(entry: ToCEntry, target: ToCEntry | null): UntypedFormGroup {
		const value = (entry.form as UntypedFormGroup).getRawValue();
		const ids = new Map<string, string>();
		const fresh = (old: string) => { const next = Guid.create().toString(); if (old) ids.set(old, next); return next; };
		const isPreset = (fs: any) => (fs?.fields ?? []).some((f: any) => f?.data?.fieldType === FieldType.SYSTEM);
		const renewFieldSet = (fs: any) => { fs.id = fresh(fs.id); (fs.fields ?? []).forEach((f: any) => f.id = fresh(f.id)); return fs; };
		const renewSection = (s: any) => {
			s.id = fresh(s.id);
			s.fieldSets = (s.fieldSets ?? []).filter((fs: any) => !isPreset(fs)).map(renewFieldSet);
			s.sections = (s.sections ?? []).map(renewSection);
			return s;
		};
		const copied = entry.type === ToCEntryType.Section ? renewSection(value) : renewFieldSet(value);
		const remap = (s: any) => {
			(s.fieldSets ?? [s]).forEach((fs: any) => (fs.fields ?? []).forEach((f: any) => (f.visibilityRules ?? []).forEach((r: any) => { if (ids.has(r.target)) r.target = ids.get(r.target); })));
			(s.sections ?? []).forEach(remap);
		};
		remap(copied);
		copied.title = copied.title ? this.language.instant('PLAN-BLUEPRINT-EDITOR.MOVE.COPY-OF', { name: copied.title }) : copied.title;
		if (entry.type === ToCEntryType.FieldSet) {
			return new TemplateFieldSetEditorModel(this.editorModel.validationErrorModel).fromModel(copied).buildForm({ rootPath: 'definition.sections[0].fieldSets[0].' });
		}
		return this.sectionFormFor(copied, !target);
	}

	/** "Move to…": pick a destination in the section tree; it lands at the end there. */
	openMoveTo(entry: ToCEntry): void {
		const path = BlueprintStructureHelper.pathTo(entry.id, this.toCEntries) ?? [];
		const parent = path.length > 1 ? path[path.length - 2] : null;
		const dialogRef = this.dialog.open<MoveToDialogComponent, MoveToData, MoveToResult>(MoveToDialogComponent, {
			width: 'min(560px, 95vw)',
			data: {
				entry, entries: this.toCEntries, currentParentId: parent?.id ?? null, reason: (target) => this.moveBlockReason(entry, target),
				currentId: this.item?.id ?? null,
				holdsSubsectionsReason: this.language.instant('PLAN-BLUEPRINT-EDITOR.MOVE.HOLDS-SUBSECTIONS'),
				hereKey: this.term('PLAN-BLUEPRINT-EDITOR.TRANSFER.HERE'),
				unsavedChanges: this.formGroup.dirty && !this.readOnly
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (!result) return;
			if (result.external) { this.pushCopy(entry, result.external, result.targetId); return; }
			const target = result.targetId === 'top' ? null : DefinitionTocEntryHelper.findTocEntryById(result.targetId, this.toCEntries);
			if (result.targetId !== 'top' && !target) return;
			const array = this.childArrayOf(entry, target);
			this.moveEntryTo(entry, target, array?.length ?? 0, result.copy);
		});
	}

	// ---------------------------------------------------------------- copying between templates and blueprints

	/**
	 * Inserts copied sections and questions. Sections go to the top level, after the top-level section holding
	 * `targetSectionId` (at the end for 'top'); questions go into that section, or into a new section named after the
	 * source when it cannot hold questions. Everything gets new ids; see `StructureTransfer.renew`.
	 */
	insertCopies(raw: TransferPayload, targetSectionId: string | 'top' | null): void {
		const present = new Set(this.presentPresets);
		const allowed = new Set([...this.requiredPresets, ...this.optionalPresets]);
		const { payload, droppedRules, skippedPresets } = StructureTransfer.renew(raw, preset => {
			const ok = allowed.has(preset) && !present.has(preset);
			if (ok) present.add(preset);
			return ok;
		});
		const target = targetSectionId && targetSectionId !== 'top' ? DefinitionTocEntryHelper.findTocEntryById(targetSectionId, this.toCEntries) : null;
		let firstId: string = null;
		if (payload.sections.length) {
			const root = target ? (BlueprintStructureHelper.pathTo(target.id, this.toCEntries) ?? [])[0] : null;
			const rootIndex = root ? this.sectionsArray.controls.findIndex(c => c.get('id').value === root.id) : -1;
			const at = rootIndex >= 0 ? rootIndex + 1 : this.sectionsArray.length;
			payload.sections.forEach((section, i) => this.sectionsArray.insert(at + i, this.sectionFormFor(section, true)));
			BlueprintStructureHelper.renumber(this.sectionsArray);
			firstId = payload.sections[0].id;
		}
		if (payload.questions.length) {
			this.refreshToCEntries();
			let into = target ? DefinitionTocEntryHelper.findTocEntryById(target.id, this.toCEntries) : null;
			if (!into || into.type !== ToCEntryType.Section || BlueprintStructureHelper.hasSubsections(into)) {
				const id = this.addSection(null, raw.sourceLabel || null);
				into = DefinitionTocEntryHelper.findTocEntryById(id, this.toCEntries);
			}
			const fieldSets = into.form.get('fieldSets') as UntypedFormArray;
			payload.questions.forEach(q => fieldSets.push(new TemplateFieldSetEditorModel(this.editorModel.validationErrorModel).fromModel(q).buildForm({ rootPath: `${into.validationRootPath}.fieldSets[${fieldSets.length}].` })));
			BlueprintStructureHelper.renumber(fieldSets);
			firstId = firstId ?? into.id;
		}
		if (!firstId) { this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-BLUEPRINT-EDITOR.TRANSFER.NOTHING'), SnackBarNotificationLevel.Warning); return; }
		this.afterStructureChange(firstId);
		const t = (k: string, p?: object) => this.language.instant(k, p);
		const parts = [t('PLAN-BLUEPRINT-EDITOR.TRANSFER.INSERTED', { source: raw.sourceLabel })];
		if (droppedRules) parts.push(t(droppedRules === 1 ? 'PLAN-BLUEPRINT-EDITOR.TRANSFER.DROPPED-RULES-ONE' : 'PLAN-BLUEPRINT-EDITOR.TRANSFER.DROPPED-RULES', { count: droppedRules }));
		if (skippedPresets) parts.push(t(skippedPresets === 1 ? 'PLAN-BLUEPRINT-EDITOR.TRANSFER.SKIPPED-PRESETS-ONE' : 'PLAN-BLUEPRINT-EDITOR.TRANSFER.SKIPPED-PRESETS', { count: skippedPresets }));
		this.uiNotificationService.snackBarNotification(parts.join(' '), SnackBarNotificationLevel.Success);
	}

	/** "Insert from another template": pick sections or questions from any template or blueprint and copy them here. */
	openInsertFrom(section: ToCEntry): void {
		const path = section ? (BlueprintStructureHelper.pathTo(section.id, this.toCEntries) ?? []) : [];
		const root = path[0] ?? null;
		const name = (e: ToCEntry) => `${e.numbering} ${e.label || this.language.instant('PLAN-BLUEPRINT-EDITOR.UNTITLED-SECTION')}`;
		const data: InsertFromData = {
			currentId: this.item?.id ?? null,
			sectionLabel: section ? name(section) : null,
			rootLabel: root ? name(root) : null,
			sectionTakesQuestions: !!section && !BlueprintStructureHelper.hasSubsections(section),
			term: (key) => this.term(key)
		};
		this.dialog.open<InsertFromDialogComponent, InsertFromData, TransferPayload>(InsertFromDialogComponent, { width: 'min(760px, 96vw)', data })
			.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(payload => { if (payload) this.insertCopies(payload, section?.id ?? 'top'); });
	}

	/** The other end of the same operation: copy a section or question into another draft, and open it there. */
	pushCopy(entry: ToCEntry, destination: TransferItem, targetSectionId: string | 'top'): void {
		const value = (entry.form as UntypedFormGroup).getRawValue();
		const sourceLabel = this.formGroup.get('label').value;
		const payload: TransferPayload = entry.type === ToCEntryType.Section ? { sourceLabel, sections: [value], questions: [] } : { sourceLabel, sections: [], questions: [value] };
		const open = () => {
			this.transferService.set({ destinationId: destination.id.toString(), targetSectionId, payload });
			const base = destination.kind === 'template' ? '/asset-templates' : '/plan-blueprints';
			this.router.navigate([this.routerUtils.generateUrl(`${base}/${destination.id}`)], { queryParams: { tab: 'structure' } });
		};
		// unsaved changes would stop the navigation, so they are saved first
		if (this.formGroup.dirty && !this.readOnly) { this.formSubmit(() => { this.formGroup.markAsPristine(); open(); }); return; }
		open();
	}

	/** Duplicates a section right after itself, at the same level. */
	duplicateSection(entry: ToCEntry): void {
		const path = BlueprintStructureHelper.pathTo(entry.id, this.toCEntries) ?? [];
		const parent = path.length > 1 ? path[path.length - 2] : null;
		const array = entry.form.parent as UntypedFormArray;
		const index = array.controls.findIndex(c => c.get('id').value === entry.id);
		this.moveEntryTo(entry, parent, index + 1, true);
	}

	/** Turns a section's questions into one subsection, so the section can then hold more subsections. */
	wrapQuestionsInSubsection(entry: ToCEntry): void {
		if (entry.type !== ToCEntryType.Section || !BlueprintStructureHelper.hasQuestions(entry)) return;
		const fieldSets = entry.form.get('fieldSets') as UntypedFormArray;
		const values = fieldSets.controls.map(c => (c as UntypedFormGroup).getRawValue());
		const sub = new TemplateSectionEditorModel(this.editorModel.validationErrorModel);
		sub.id = Guid.create().toString();
		sub.ordinal = 0;
		sub.title = entry.label || null;
		const subForm = sub.buildForm({ rootPath: `${entry.validationRootPath}.sections[0].` });
		const subFieldSets = subForm.get('fieldSets') as UntypedFormArray;
		values.forEach((v, i) => subFieldSets.push(new TemplateFieldSetEditorModel(this.editorModel.validationErrorModel).fromModel(v).buildForm({ rootPath: `${entry.validationRootPath}.sections[0].fieldSets[${i}].` })));
		while (fieldSets.length) fieldSets.removeAt(0);
		(entry.form.get('sections') as UntypedFormArray).push(subForm);
		this.afterStructureChange(sub.id);
	}

	/** Drag-and-drop reorder within one sibling list. */
	reorder(parentArray: UntypedFormArray, previousIndex: number, currentIndex: number): void {
		if (previousIndex === currentIndex) return;
		const control = parentArray.at(previousIndex);
		parentArray.removeAt(previousIndex);
		parentArray.insert(currentIndex, control);
		BlueprintStructureHelper.renumber(parentArray);
		this.afterStructureChange();
	}

	onFieldTypeChanged(): void { this.afterStructureChange(); }

	// ---------------------------------------------------------------- presets

	get presentPresets(): SystemFieldType[] { return this.sectionsArray ? BlueprintStructureHelper.presetsIn(this.sectionsArray) : []; }
	get missingRequiredPresets(): SystemFieldType[] { const present = this.presentPresets; return this.requiredPresets.filter(p => !present.includes(p)); }
	get availablePresets(): SystemFieldType[] { const present = this.presentPresets; return [...this.requiredPresets, ...this.optionalPresets].filter(p => !present.includes(p)); }

	/** Where a preset added from the bar lands: the selected section if it can hold questions, else the first root section that can. */
	presetTarget(): ToCEntry {
		const canHold = (e: ToCEntry) => e?.type === ToCEntryType.Section && !BlueprintStructureHelper.hasSubsections(e);
		if (canHold(this.selectedTocEntry)) return this.selectedTocEntry;
		const path = this.selectedTocEntry ? BlueprintStructureHelper.pathTo(this.selectedTocEntry.id, this.toCEntries) : null;
		const parent = path && path.length > 1 ? path[path.length - 2] : null;
		if (canHold(parent)) return parent;
		return this.toCEntries.find(e => canHold(e)) ?? null;
	}

	addPreset(preset: SystemFieldType): void {
		let target = this.presetTarget();
		if (!target) {
			const id = this.addSection(null, this.language.instant('PLAN-BLUEPRINT-EDITOR.PRESETS.BASICS'));
			target = DefinitionTocEntryHelper.findTocEntryById(id, this.toCEntries);
		}
		this.addQuestion(target, FieldType.SYSTEM, preset);
		this.expandedQuestionId = null;
		this.selectById(target.id);
	}

	/**
	 * Adds every missing required preset as its own question. They go into the selected section when it can hold
	 * questions, else into the first root section that can, else into a new root section named "Basics".
	 */
	addMissingPresets(): void {
		const missing = this.missingRequiredPresets;
		if (!missing.length) return;
		let target: ToCEntry = this.presetTarget();
		if (!target) {
			const id = this.addSection(null, this.language.instant('PLAN-BLUEPRINT-EDITOR.PRESETS.BASICS'));
			target = DefinitionTocEntryHelper.findTocEntryById(id, this.toCEntries);
			// a brand-new structure gets its Basics first
			const parentArray = this.sectionsArray;
			const index = parentArray.controls.findIndex(c => c.get('id').value === id);
			if (index > 0) { const control = parentArray.at(index); parentArray.removeAt(index); parentArray.insert(0, control); BlueprintStructureHelper.renumber(parentArray); this.afterStructureChange(id); target = DefinitionTocEntryHelper.findTocEntryById(id, this.toCEntries); }
		}
		missing.forEach(preset => { this.addQuestion(target, FieldType.SYSTEM, preset); target = DefinitionTocEntryHelper.findTocEntryById(target.id, this.toCEntries); });
		this.expandedQuestionId = null;
		this.selectById(target.id);
	}

	// ---------------------------------------------------------------- problems, finalize

	recomputeProblems(): void {
		if (!this.formGroup) return;
		this.ruleLinks = ruleLinks(this.toCEntries, this.language);
		this.problems = this.computeProblems();
	}

	goToProblem(problem: StructureProblem): void {
		if (problem.tab === 'structure' && problem.entryId) {
			this.selectById(problem.entryId);
			if (problem.questionId) this.expandedQuestionId = problem.questionId;
		} else {
			this.setTab(problem.tab);
		}
		if (problem.action === 'add-presets') this.addMissingPresets();
	}

	/** The version being edited: the next one for a new version, 1 for something not saved yet. */
	get nextVersionNumber(): number { return this.isNewVersion ? (this.currentVersion ?? 0) + 1 : (this.currentVersion ?? 1); }

	openPreflight(): void {
		this.attemptedSubmit = true;
		this.formService.touchAllFormFields(this.formGroup);
		this.recomputeProblems();
		const dialogRef = this.dialog.open<FinalizePreflightDialogComponent, FinalizePreflightData>(FinalizePreflightDialogComponent, {
			width: 'min(600px, 95vw)',
			data: { name: this.formGroup.get('label').value, problems: this.problems, version: this.nextVersionNumber, tenantName: this.authService.getSelectedTenantName() || this.language.instant('PLAN-BLUEPRINT-EDITOR.THIS-INSTALLATION'), introKey: this.term('PLAN-BLUEPRINT-EDITOR.FINALIZE.INTRO') }
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result === 'finalize') this.finalize();
			else if (result?.problem) this.goToProblem(result.problem);
		});
	}

	private finalize(): void {
		if (this.blockers.length) { this.showProblems = true; return; }
		this.formGroup.get('status').setValue(STATUS_FINALIZED);
		const finalized = (complete: E) => {
			this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-BLUEPRINT-EDITOR.FINALIZE.DONE', { name: this.formGroup.get('label').value }), SnackBarNotificationLevel.Success);
			this.formGroup.markAsPristine();
			// navigating to the URL already open does not reload the editor
			if (!this.isTransient && complete.id?.toString() === this.item?.id?.toString()) { this.refreshData(complete.id); this.setTab('review'); return; }
			this.router.navigate([this.editorUrl(complete.id)], { queryParams: { tab: 'review' }, replaceUrl: true, relativeTo: this.route });
		};
		if (!this.isNewVersion) { this.persistEntity(finalized); return; }
		// the blueprint new-version endpoint always saves a draft, so a new version is finalized in a second save
		this.persistEntity((created: E) => {
			this.saving = true;
			this.fetchFull(created.id).pipe(
				switchMap(draft => draft.status === STATUS_FINALIZED ? of(draft) : this.persistRequest({ ...JSON.parse(JSON.stringify(this.formGroup.value)), id: draft.id, hash: draft.hash, code: this.formGroup.get('code').getRawValue(), status: STATUS_FINALIZED }, false)),
				takeUntil(this._destroyed)
			).subscribe({
				next: complete => { this.saving = false; this.isNewVersion = false; finalized(complete); },
				error: error => {
					this.saving = false;
					this.onCallbackError(error);
					// the draft version exists now; open it rather than leave the unsaved new-version form
					this.formGroup.markAsPristine();
					this.router.navigate([this.editorUrl(created.id)], { replaceUrl: true, relativeTo: this.route });
				}
			});
		});
	}

	// ---------------------------------------------------------------- versions

	private loadVersions(groupId: Guid): void {
		this.queryVersions(groupId).pipe(takeUntil(this._destroyed)).subscribe({ next: items => this.versions = items, error: () => this.versions = [] });
	}

	/** A saved draft of a later version (v2+), as opposed to the unsaved new-version form. */
	get isDraftVersion(): boolean { return !this.isTransient && this.editorModel?.status == STATUS_DRAFT && (this.currentVersion ?? 1) > 1 && !this.isDeleted; }
	/** The version this one is compared with: the one it was made from, or the one authors get today. */
	get compareWith(): E {
		if (this.isNewVersion) return this.item;
		if (this.isDraftVersion) return this.currentPublishedVersion;
		return null;
	}
	get currentPublishedVersion(): E { return this.versions?.find(v => v.versionStatus === VERSION_CURRENT && v.status === STATUS_FINALIZED) ?? null; }
	isFinalizedVersion(v: E): boolean { return v?.status === STATUS_FINALIZED; }
	isCurrentVersion(v: E): boolean { return v?.status === STATUS_FINALIZED && v?.versionStatus === VERSION_CURRENT; }
	get isCurrent(): boolean { return this.isCurrentVersion(this.item); }
	/** How many plans (or assets) use the version being edited, from the versions list; null until it loads. */
	get usageCount(): number { const mine = this.versions?.find(v => v.id?.toString() === this.item?.id?.toString()); return mine?.usageCount ?? null; }

	openCompare(): void {
		const other = this.compareWith;
		if (!other?.id) return;
		this.fetchFull(other.id).pipe(takeUntil(this._destroyed)).subscribe({
			next: full => this.dialog.open<CompareVersionsDialogComponent, CompareVersionsData>(CompareVersionsDialogComponent, {
				width: 'min(620px, 95vw)',
				data: { current: this.formGroup.getRawValue(), other: full as any, currentVersion: this.nextVersionNumber, otherVersion: Number(full.version) }
			}),
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
		});
	}

	openVersion(version: E): void {
		if (version.id?.toString() === this.item?.id?.toString()) return;
		this.router.navigate([this.editorUrl(version.id)]);
	}

	// ---------------------------------------------------------------- walking the form

	/** Every question (field set) in the structure, depth first. */
	/** The values the tenant's plugins read from this kind of structure. */
	knownUsages(): KnownUsage[] { return knownUsages(this.pluginSemantics, this.pluginEntityType); }
	/** The objects the tenant's plugins declare for this kind of structure, for the object picker. */
	knownObjects(): KnownObject[] { return knownObjects(this.pluginSemantics, this.pluginEntityType, p => this.pluginLabel(p)); }
	/** Keys mapped in ways that will not come out right; recomputed only after a change. */
	semanticChecks(): SemanticCheck[] {
		const memo = this.checksMemo;
		if (memo && memo.revision === this.semanticRevision && memo.plugins === this.pluginSemantics) return memo.checks;
		const checks = semanticChecks(this.toCEntries, this.knownUsages(), this.knownObjects());
		this.checksMemo = { revision: this.semanticRevision, plugins: this.pluginSemantics, checks };
		return checks;
	}
	/** The checks about one input, for its key chips. */
	checksFor(fieldForm: AbstractControl): SemanticCheck[] { return this.semanticChecks().filter(c => c.fieldForm === fieldForm); }
	/** Every section and subsection form, outermost first. */
	allSectionForms(): UntypedFormGroup[] {
		const result: UntypedFormGroup[] = [];
		const visit = (section: UntypedFormGroup) => {
			result.push(section);
			(section.get('sections') as UntypedFormArray)?.controls?.forEach(s => visit(s as UntypedFormGroup));
		};
		this.sectionsArray?.controls?.forEach(s => visit(s as UntypedFormGroup));
		return result;
	}
	allFieldSets(): UntypedFormGroup[] {
		const result: UntypedFormGroup[] = [];
		const visit = (section: UntypedFormGroup) => {
			(section.get('sections') as UntypedFormArray)?.controls?.forEach(s => visit(s as UntypedFormGroup));
			(section.get('fieldSets') as UntypedFormArray)?.controls?.forEach(fs => result.push(fs as UntypedFormGroup));
		};
		this.sectionsArray?.controls?.forEach(s => visit(s as UntypedFormGroup));
		return result;
	}
	/** Every input in the structure. */
	/** Every semantic key an input of the structure carries. */
	inputSemanticKeys(): Set<string> { return new Set(this.allFields().flatMap(f => (f.get('semantics')?.value ?? []) as string[])); }
	/** What the structure gives plugins: keys that come out (inputs and answer options), plus the objects it describes. */
	structureUse(): StructureUse {
		const fields = this.allFields();
		const keys = new Set([...fields.flatMap(f => (f.get('semantics')?.value ?? []) as string[]), ...fields.flatMap(f => optionSemanticsOf(f).map(x => x.key))].filter(Boolean));
		return { keys, markers: usedMarkers(this.allSectionForms(), this.allFieldSets(), fields) };
	}
	/** What other structures give the plugins along with this one: a blueprint's attached templates. */
	attachedUses: AttachedUse[] = [];
	/** Loads `attachedUses` for the Review tab; nothing to load for a template. */
	refreshAttachedUses(): void { }
	/** A plugin as the coverage cards name it, e.g. "RDA maDMP export". */
	pluginLabel(plugin: PluginSemantics): string { return this.language.instant(pluginKindKey(plugin.pluginType), { name: pluginName(plugin) }); }
	allFields(): UntypedFormGroup[] { return this.allFieldSets().flatMap(fs => ((fs.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[]); }

	/** The problems (not notices) of the semantic checks that name a question, for its marker in the outline. */
	checkProblemsFor(questionId: string): SemanticCheck[] { return this.semanticChecks().filter(c => !c.notice && c.targets.some(t => t.id === questionId && !t.isSection)); }

	/**
	 * Whether a question's answer options show what they stand for: when the question or a section around it describes
	 * an object, an option already carries keys, or the admin switched it on.
	 */
	answerKeysShown(entry: ToCEntry): boolean {
		if (!entry) return false;
		if (this.answerKeysOn.has(entry.id)) return true;
		const fields = ((entry.form.get('fields') as UntypedFormArray)?.controls ?? []) as UntypedFormGroup[];
		const options = fields.flatMap(f => ((f.get('data')?.get('options') as UntypedFormArray)?.controls ?? []));
		if (options.some(o => (o.get('semantics')?.value ?? []).length || o.get('exportsNothing')?.value)) return true;
		// the question and the sections around it
		const path = BlueprintStructureHelper.pathTo(entry.id, this.toCEntries) ?? [entry];
		return path.some(p => ((p.form?.get('semantics')?.value ?? []) as string[]).some(Boolean));
	}

	/** Opens a question in the Structure tab. */
	openQuestion(questionId: string, sectionId: string): void {
		if (sectionId) this.selectById(sectionId);
		this.expandedQuestionId = questionId;
		if (this.tab !== 'structure') this.setTab('structure');
	}

	/** Shows the keys in use, grouped by object, in the Structure tab's left panel. */
	openKeysView(): void {
		this.outlineMode = 'keys';
		this.setTab('structure');
	}

	// ---------------------------------------------------------------- counts for the header

	get questionCount(): number { return this.allFieldSets().length; }
	get requiredInputCount(): number { return this.allFields().filter(f => ((f.get('validations')?.value ?? []) as FieldValidationType[]).includes(FieldValidationType.Required)).length; }
	get ruleCount(): number { return this.allFields().reduce((n, f) => n + ((f.get('visibilityRules') as UntypedFormArray)?.length ?? 0), 0); }

	// ---------------------------------------------------------------- rules

	trackEntry(_: number, entry: ToCEntry) { return entry.id; }

	/** Rules on a question's inputs, and rules elsewhere that show or hide this question or one of its inputs. */
	rulesFrom(fieldSetForm: UntypedFormGroup): RuleLink[] {
		const ids = new Set(((fieldSetForm?.get('fields') as UntypedFormArray)?.controls ?? []).map(f => f.get('id').value));
		return this.ruleLinks.filter(l => ids.has(l.sourceFieldId));
	}
	rulesInto(fieldSetForm: UntypedFormGroup): RuleLink[] {
		const id = fieldSetForm?.get('id')?.value;
		const ids = new Set([id, ...((fieldSetForm?.get('fields') as UntypedFormArray)?.controls ?? []).map(f => f.get('id').value)]);
		return this.ruleLinks.filter(l => ids.has(l.targetId));
	}
	rulesFromField(fieldForm: UntypedFormGroup): RuleLink[] {
		const id = fieldForm?.get('id')?.value;
		return this.ruleLinks.filter(l => l.sourceFieldId === id);
	}
}
