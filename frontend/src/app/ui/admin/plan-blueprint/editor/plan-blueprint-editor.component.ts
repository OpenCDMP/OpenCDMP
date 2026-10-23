import { Component, forwardRef, OnInit, TemplateRef, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { FormArray, FormGroup, UntypedFormArray, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { PlanBlueprintTypeStatus } from '@app/core/common/enum/plan-blueprint-type-status';
import { PlanBlueprintVersionStatus } from '@app/core/common/enum/plan-blueprint-version-status';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { UserPlanBlueprintRole } from '@app/core/common/enum/user-plan-blueprint-role';
import { BaseField, BaseFieldSet, BaseSection, DescriptionTemplate, DescriptionTemplateDefinition, FieldSelectData, FieldSelectOption, SemanticValue } from '@app/core/model/description-template/description-template';
import { NewVersionPlanBlueprintPersist, PlanBlueprint, PlanBlueprintPersist } from '@app/core/model/plan-blueprint/plan-blueprint';
import { PluginRepositoryConfiguration } from '@app/core/model/plugin-configuration/plugin-configuration';
import { User } from '@app/core/model/user/user';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PlanBlueprintTypeService } from '@app/core/services/plan-blueprint-type/plan-blueprint-type.service';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { TemplateSectionEditorModel } from '@app/ui/admin/description-template/editor/description-template-editor.model';
import { ToCEntry } from '@app/ui/admin/description-template/editor/table-of-contents/description-template-table-of-contents-entry';
import { AttachedUse, StructureUse } from '@app/ui/admin/structure-editor/plugin-coverage';
import { StructureEditorBase } from '@app/ui/admin/structure-editor/structure-editor-base';
import { EditorTab, StructureProblem } from '@app/ui/admin/structure-editor/structure-problems';
import { BlueprintStructureHelper } from '@app/ui/admin/structure-editor/structure.helper';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { PlanBlueprintEditorModel, PlanBlueprintForm, PlanBlueprintRootSectionEditorModel, UserPlanBlueprintEditorModel } from '../editor/plan-blueprint-editor.model';
import { PlanBlueprintEditorResolver } from '../editor/plan-blueprint-editor.resolver';
import { PlanBlueprintEditorService } from '../editor/plan-blueprint-editor.service';
import { BlueprintProblemsHelper } from './blueprint-problems.helper';
import { Bp2AssetsBlockComponent } from './tabs/structure-tab/assets-block/assets-block.component';
import { EditorShellComponent } from '../../structure-editor/shell/editor-shell.component';
import { Bp2DetailsTabComponent } from './tabs/details-tab/details-tab.component';
import { Bp2StructureTabComponent } from '../../structure-editor/structure/structure-tab.component';
import { Bp2ReviewTabComponent } from './tabs/review-tab/review-tab.component';
import { MatIcon } from '@angular/material/icon';
import { Bp2CountPipe } from '../../structure-editor/count.pipe';

export type { EditorTab };

/**
 * The plan blueprint editor, on the reactive form of `PlanBlueprintEditorModel`. The structure itself is shared with
 * the description template editor through `StructureEditorBase`.
 */
@Component({
    selector: 'app-plan-blueprint-editor',
    templateUrl: './plan-blueprint-editor.component.html',
    styleUrls: ['./plan-blueprint-editor.component.scss'],
    providers: [PlanBlueprintEditorService, { provide: StructureEditorBase, useExisting: forwardRef(() => PlanBlueprintEditorComponent) }],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [forwardRef(() => Bp2AssetsBlockComponent), EditorShellComponent, MatIcon, forwardRef(() => Bp2DetailsTabComponent), Bp2StructureTabComponent, forwardRef(() => Bp2ReviewTabComponent), Bp2CountPipe]
})
export class PlanBlueprintEditorComponent extends StructureEditorBase<PlanBlueprintEditorModel, PlanBlueprint> implements OnInit {
	declare formGroup: FormGroup<PlanBlueprintForm>;

	pluginsInitialized: Observable<PluginRepositoryConfiguration[]>;
	planBlueprintTypeConfiguration: SingleAutoCompleteConfiguration;

	/** Templates referenced by the sections, by group id, so rows and the preview can show names. */
	descriptionTemplateMap = new Map<string, DescriptionTemplate>();
	/** People on the Editors card, by id, so rows can show names. */
	usersMap = new Map<string, User>();
	private attachedUseByGroup = new Map<string, AttachedUse>();

	readonly urlBase = '/plan-blueprints';
	readonly lockTargetType = LockTargetType.PlanBlueprint;
	readonly pluginEntityType = PluginEntityType.Plan;
	readonly requiredPresets = BlueprintStructureHelper.REQUIRED_PRESETS;
	readonly optionalPresets = BlueprintStructureHelper.OPTIONAL_PRESETS;
	readonly PlanBlueprintStatus = PlanBlueprintStatus;
	readonly PlanBlueprintVersionStatus = PlanBlueprintVersionStatus;

	@ViewChild('assetsBlock', { static: true }) assetsBlock: TemplateRef<{ $implicit: ToCEntry }>;

	constructor(
		protected dialog: MatDialog,
		public language: TranslateService,
		protected formService: FormService,
		protected router: Router,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected filterService: FilterService,
		protected route: ActivatedRoute,
		protected queryParamsService: QueryParamsService,
		protected lockService: LockService,
		public authService: AuthService,
		public configurationService: ConfigurationService,
		private planBlueprintService: PlanBlueprintService,
		private logger: LoggingService,
		private planBlueprintEditorService: PlanBlueprintEditorService,
		public descriptionTemplateService: DescriptionTemplateService,
		public planBlueprintTypeService: PlanBlueprintTypeService,
		private analyticsService: AnalyticsService,
		private pluginConfigurationService: PluginConfigurationService,
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
		this.pluginsInitialized = toObservable(this.pluginConfigurationService.pluginRepositoryConfiguration);
	}

	// ---------------------------------------------------------------- lifecycle

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.PlanBlueprintEditor);
		this.initStructureEditor();
		this.rootSectionExtra = this.assetsBlock;
		this.pluginsInitialized.pipe(takeUntil(this._destroyed)).subscribe((initialized) => {
			if (!initialized) return;
			super.ngOnInit();
			this.route.data.pipe(takeUntil(this._destroyed)).subscribe(d => this.initModelFlags(d['action']));
		});
		this.planBlueprintTypeConfiguration = this.planBlueprintTypeService.getSingleAutocompleteConfiguration([PlanBlueprintTypeStatus.Finalized]);
	}

	// ---------------------------------------------------------------- data

	get pluginRepositoryConfigurations(): PluginRepositoryConfiguration[] { return this.pluginConfigurationService.getPluginRepositoryConfigurationFor(PluginType.FileTransformer, [PluginEntityType.Plan]); }
	get plugins() { return this.pluginConfigurationService.getAvailablePluginsFor(PluginType.FileTransformer, [PluginEntityType.Plan]); }

	getItem(itemId: Guid, successFunction: (item: PlanBlueprint) => void) {
		this.planBlueprintService.getSingle(itemId, PlanBlueprintEditorResolver.lookupFields())
			.pipe(map(data => data as PlanBlueprint), takeUntil(this._destroyed))
			.subscribe({ next: (data) => successFunction(data), error: (error) => this.onCallbackError(error) });
	}

	prepareForm(data: PlanBlueprint) {
		try {
			this.fileMap.clear();
			const pluginConfigurations = this.plugins;
			if (data == null) {
				data = { status: PlanBlueprintStatus.Draft, definition: { pluginConfigurations, sections: [] } };
			} else {
				data.definition.pluginConfigurations = data?.definition?.pluginConfigurations ? this.pluginConfigurationService.completeWithAvailable(data.definition.pluginConfigurations, pluginConfigurations) : pluginConfigurations;
				data.definition.pluginConfigurations?.forEach(x => x.fields?.forEach(field => { if (field.fileValue) this.fileMap.set(field.fileValue.id, field.fileValue); }));
			}
			this.item = data;
			this.editorModel = new PlanBlueprintEditorModel().fromModel(data, this.pluginRepositoryConfigurations);
			this.usersMap.clear();
			(data.users ?? []).forEach(u => { if (u.user?.id) this.usersMap.set(u.user.id.toString(), u.user); });
			this.isDeleted = data.isActive === IsActive.Inactive;
			this.belongsToCurrentTenant = this.isNew || this.isClone || data.belongsToCurrentTenant !== false;
			this.buildForm();
			this.seedDescriptionTemplateMap(data);
			this.afterFormPrepared(data);
		} catch (error) {
			this.logger.error('Could not parse planBlueprint item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(this._destroyed, null, this.readOnly, (this.isNew || this.isClone));
		this.planBlueprintEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		if (this.isNewVersion) this.formGroup.get('code').disable();
		this.applyConsentValidator(this.formGroup.controls.definition.get('planCreationConsent').get('enabled').value);
	}

	private seedDescriptionTemplateMap(data: PlanBlueprint) {
		data?.definition?.sections?.forEach(section => section.descriptionTemplates?.forEach(item => {
			if (item.descriptionTemplate?.groupId) this.descriptionTemplateMap.set(item.descriptionTemplate.groupId.toString(), item.descriptionTemplate);
		}));
		const groupIds = [...this.descriptionTemplateMap.keys()];
		if (!groupIds.length) return;
		// the resolver only carries label and group id; fetch the rest the rows show
		this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
			isActive: [IsActive.Active, IsActive.Inactive],
			groupIds: groupIds.map(x => Guid.parse(x)),
			lookupFields: [
				nameof<DescriptionTemplate>(x => x.id), nameof<DescriptionTemplate>(x => x.label), nameof<DescriptionTemplate>(x => x.groupId),
				nameof<DescriptionTemplate>(x => x.description), nameof<DescriptionTemplate>(x => x.code), nameof<DescriptionTemplate>(x => x.version),
				nameof<DescriptionTemplate>(x => x.status), nameof<DescriptionTemplate>(x => x.versionStatus),
				[nameof<DescriptionTemplate>(x => x.type), 'name'].join('.')
			]
		})).pipe(takeUntil(this._destroyed)).subscribe(result => result.items?.forEach(t => this.descriptionTemplateMap.set(t.groupId.toString(), t)));
	}

	refreshAttachedUses(): void {
		const groupIds = [...new Set(this.sectionsArray?.controls.flatMap(s => ((s.get('descriptionTemplates') as UntypedFormArray)?.controls ?? [])
			.map(t => t.get('descriptionTemplateGroupId')?.value?.toString()).filter(Boolean)) ?? [])];
		const pick = () => this.attachedUses = groupIds.map(id => this.attachedUseByGroup.get(id)).filter(Boolean);
		const missing = groupIds.filter(id => !this.attachedUseByGroup.has(id));
		if (!missing.length) { pick(); return; }
		const definition = (...path: string[]) => [nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.sections), ...path].join('.');
		this.descriptionTemplateService.query(this.descriptionTemplateService.buildDescriptionTemplateGroupAutocompleteLookup({
			isActive: [IsActive.Active],
			groupIds: missing.map(x => Guid.parse(x)),
			lookupFields: [
				nameof<DescriptionTemplate>(x => x.id), nameof<DescriptionTemplate>(x => x.label), nameof<DescriptionTemplate>(x => x.groupId),
				definition(nameof<BaseSection>(x => x.semantics)),
				definition(nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.semantics)),
				definition(nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.semantics)),
				definition(nameof<BaseSection>(x => x.fieldSets), nameof<BaseFieldSet>(x => x.fields), nameof<BaseField>(x => x.data), nameof<FieldSelectData>(x => x.options), nameof<FieldSelectOption>(x => x.semantics), nameof<SemanticValue>(x => x.key)),
			]
		})).pipe(takeUntil(this._destroyed)).subscribe(result => {
			result.items?.forEach(t => this.attachedUseByGroup.set(t.groupId.toString(), { label: t.label, use: this.templateUse(t.definition?.sections ?? []) }));
			pick();
		});
	}

	/** What a template's definition gives plugins, read from the model as `structureUse()` reads it from the form. */
	private templateUse(sections: BaseSection[]): StructureUse {
		const keys = new Set<string>();
		const markers = new Set<string>();
		const visit = (section: BaseSection) => {
			section.semantics?.forEach(k => k && markers.add(k));
			section.fieldSets?.forEach(fieldSet => {
				fieldSet.semantics?.forEach(k => k && markers.add(k));
				fieldSet.fields?.forEach(field => {
					const options = ((field.data as FieldSelectData)?.options ?? []).flatMap(o => o.semantics ?? []).map(x => x.key);
					[...(field.semantics ?? []), ...options].filter(Boolean).forEach(k => { keys.add(k); markers.add(k); });
				});
			});
			section.sections?.forEach(visit);
		};
		sections.forEach(visit);
		return { keys, markers };
	}

	refreshData(id?: Guid): void {
		this.getItem(id ?? this.editorModel.id, (data: PlanBlueprint) => this.prepareForm(data));
	}

	protected persistRequest(formValue: any, newVersion: boolean): Observable<PlanBlueprint> {
		return newVersion ? this.planBlueprintService.newVersion(formValue as NewVersionPlanBlueprintPersist) : this.planBlueprintService.persist(formValue as PlanBlueprintPersist);
	}
	protected semanticsInUseRequest(excludeGroupId: Guid) { return this.planBlueprintService.semanticsInUse(excludeGroupId); }
	protected deleteRequest(id: Guid) { return this.planBlueprintService.delete(id); }
	protected downloadRequest(id: Guid) { return this.planBlueprintService.downloadXML(id); }
	protected fetchFull(id: Guid) { return this.planBlueprintService.getSingle(id, PlanBlueprintEditorResolver.lookupFields()); }
	protected queryVersions(groupId: Guid): Observable<PlanBlueprint[]> {
		return this.planBlueprintService.query({
			page: { offset: 0, size: 50 },
			groupIds: [groupId],
			isActive: [IsActive.Active],
			order: { items: ['-' + nameof<PlanBlueprint>(x => x.version)] },
			project: { fields: [nameof<PlanBlueprint>(x => x.id), nameof<PlanBlueprint>(x => x.label), nameof<PlanBlueprint>(x => x.version), nameof<PlanBlueprint>(x => x.status), nameof<PlanBlueprint>(x => x.versionStatus), nameof<PlanBlueprint>(x => x.updatedAt), nameof<PlanBlueprint>(x => x.usageCount)] }
		} as any).pipe(map(result => result.items));
	}

	protected detailsDirty(): boolean {
		const definition = this.formGroup.controls.definition;
		return ['label', 'code', 'description', 'type', 'users'].some(k => this.formGroup.get(k)?.dirty)
			|| ['planCreationConsent', 'iconConfiguration', 'pluginConfigurations'].some(k => definition.get(k)?.dirty);
	}

	// ---------------------------------------------------------------- state & guards

	/** A blueprint's editors may take over its lock, like admins (the backend grants DeleteLock to both). */
	protected takeOverAllowed(): boolean { return super.takeOverAllowed() || this.hasPermission(AppPermission.EditPlanBlueprint); }

	/** A global permission, or one granted on this blueprint (an editor listed on it). */
	hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || !!this.editorModel?.permissions?.includes(permission) || !!this.item?.authorizationFlags?.some(x => x === permission);
	}
	/** Read-only: locked by someone else, or for good (deleted, finalized, shared, no permission). A new version is editable. */
	get readOnly(): boolean {
		return this.isLocked || ((this.isDeleted || this.isFinalized || !this.hasPermission(AppPermission.EditPlanBlueprint) || !this.belongsToCurrentTenant) && !this.isNewVersion);
	}
	get canDelete(): boolean { return !this.isDeleted && !this.isTransient && this.belongsToCurrentTenant && this.authService.hasPermission(AppPermission.DeletePlanBlueprint); }
	get canCreateNewVersion(): boolean { return this.isFinalized && !this.isTransient && this.item?.versionStatus == PlanBlueprintVersionStatus.Current && this.belongsToCurrentTenant && this.authService.hasPermission(AppPermission.CreateNewVersionPlanBlueprint); }
	get canClone(): boolean { return !this.isTransient && this.authService.hasPermission(AppPermission.EditPlanBlueprint); }
	get sectionsArray(): UntypedFormArray { return this.formGroup?.controls?.definition?.controls?.sections as unknown as UntypedFormArray; }

	// ---------------------------------------------------------------- editors

	get usersArray(): FormArray { return this.formGroup.get('users') as FormArray; }
	userName(userId: Guid | string): string { return this.usersMap.get(userId?.toString())?.name ?? ''; }
	isMe(userId: Guid | string): boolean { return !!userId && userId?.toString() === this.authService.userId()?.toString(); }
	editorRows(): { userId: Guid, name: string, role: UserPlanBlueprintRole }[] {
		return this.usersArray.controls.map(c => ({ userId: c.get('userId').value, name: this.userName(c.get('userId').value), role: c.get('role').value }));
	}
	/** Editors can be managed whenever the blueprint may be changed at all, finalized or not: they are not part of a version. */
	get canManageEditors(): boolean {
		return !this.isDeleted && !this.isLocked && this.belongsToCurrentTenant && this.hasPermission(AppPermission.EditPlanBlueprint);
	}
	/** A saved blueprint's editors are saved on their own, like plan collaborators; a new one's go in with its first save. */
	get editorsSaveOnTheirOwn(): boolean { return !this.isTransient && !!this.item?.id; }
	editorsSaving = false;
	get ownerCount(): number { return this.usersArray.controls.filter(c => c.get('role').value === UserPlanBlueprintRole.Owner).length; }
	get memberCount(): number { return this.usersArray.length - this.ownerCount; }
	get myRole(): UserPlanBlueprintRole { return this.usersArray.controls.find(c => this.isMe(c.get('userId').value))?.get('role').value ?? null; }
	/** "Maria, Nikos and Eleni", in the reader's language. */
	nameList(names: string[]): string {
		try { return new Intl.ListFormat(this.language.currentLang || 'en', { style: 'long', type: 'conjunction' }).format(names); } catch { return names.join(', '); }
	}

	/** Adds a person to the blueprint, as a member unless said otherwise. */
	addUser(user: User, role: UserPlanBlueprintRole = UserPlanBlueprintRole.Member): void {
		if (!user?.id || this.usersArray.controls.some(c => c.get('userId').value?.toString() === user.id.toString())) return;
		this.usersMap.set(user.id.toString(), user);
		const before = this.usersArray.getRawValue();
		this.usersArray.push(this.userForm(user.id, role, this.usersArray.length));
		this.usersChanged(before, 'PLAN-BLUEPRINT-EDITOR.EDITORS.ADDED', { name: user.name, role: this.enumUtils.toUserPlanBlueprintRoleString(role) });
	}

	setUserRole(index: number, role: UserPlanBlueprintRole): void {
		const control = this.usersArray.at(index)?.get('role');
		if (!control || control.value === role) return;
		const before = this.usersArray.getRawValue();
		control.setValue(role);
		this.usersChanged(before, 'PLAN-BLUEPRINT-EDITOR.EDITORS.ROLE-CHANGED', { name: this.userName(this.usersArray.at(index).get('userId').value), role: this.enumUtils.toUserPlanBlueprintRoleString(role) });
	}

	removeUser(index: number): void {
		const before = this.usersArray.getRawValue();
		const name = this.userName(this.usersArray.at(index)?.get('userId').value);
		this.usersArray.removeAt(index);
		this.reapplyValidators();
		this.usersChanged(before, 'PLAN-BLUEPRINT-EDITOR.EDITORS.REMOVED', { name });
	}

	private userForm(userId: Guid, role: UserPlanBlueprintRole, index: number) {
		const model = new UserPlanBlueprintEditorModel(this.editorModel.validationErrorModel);
		model.userId = userId;
		model.role = role;
		return model.buildForm({ rootPath: `users[${index}].` });
	}

	private setUsers(users: { userId: Guid, role: UserPlanBlueprintRole }[]): void {
		while (this.usersArray.length) this.usersArray.removeAt(0);
		users.forEach((u, i) => this.usersArray.push(this.userForm(u.userId, u.role, i)));
	}

	/** Saves the editors right away when the blueprint exists; otherwise they wait for the blueprint's first save. */
	private usersChanged(before: { userId: Guid, role: UserPlanBlueprintRole }[], messageKey: string, params: object): void {
		if (!this.editorsSaveOnTheirOwn) { this.usersArray.markAsDirty(); return; }
		this.editorsSaving = true;
		const users = this.usersArray.getRawValue();
		const fields = [nameof<PlanBlueprint>(x => x.id), ...['user.id', 'user.name', 'role', 'isActive'].map(f => `users.${f}`)];
		this.planBlueprintService.assignUsers(this.item.id, users, fields).pipe(takeUntil(this._destroyed)).subscribe({
			next: saved => {
				this.editorsSaving = false;
				const active = (saved?.users ?? []).filter(u => u.isActive === IsActive.Active);
				active.forEach(u => { if (u.user?.id) this.usersMap.set(u.user.id.toString(), u.user); });
				this.setUsers(active.map(u => ({ userId: u.user?.id, role: u.role })));
				this.item = { ...this.item, users: saved?.users };
				this.usersArray.markAsPristine();
				this.uiNotificationService.snackBarNotification(this.language.instant(messageKey, params), SnackBarNotificationLevel.Success);
			},
			error: error => {
				this.editorsSaving = false;
				this.setUsers(before);
				this.usersArray.markAsPristine();
				this.httpErrorHandlingService.handleBackedRequestError(error);
			}
		});
	}

	// ---------------------------------------------------------------- consent

	onConsentEnabledChange(enabled: boolean): void {
		const consent = this.formGroup.controls.definition.get('planCreationConsent') as unknown as UntypedFormGroup;
		consent.get('enabled').setValue(enabled);
		consent.get('enabled').markAsDirty();
		this.applyConsentValidator(enabled);
	}

	private applyConsentValidator(enabled: boolean): void {
		const messageControl = this.formGroup.controls.definition.get('planCreationConsent').get('message') as any;
		if (!messageControl) return;
		if (messageControl.__initialValidator === undefined) messageControl.__initialValidator = messageControl.validator ?? null;
		messageControl.setValidators(enabled ? [messageControl.__initialValidator, Validators.required] : messageControl.__initialValidator);
		messageControl.updateValueAndValidity({ emitEvent: false });
	}

	// ---------------------------------------------------------------- structure hooks

	protected reapplyValidators(): void {
		PlanBlueprintEditorModel.reApplyDefinitionValidators({ formGroup: this.formGroup, validationErrorModel: this.editorModel.validationErrorModel });
	}

	protected newRootSectionForm(id: string, index: number, title: string): UntypedFormGroup {
		const section = new PlanBlueprintRootSectionEditorModel(this.editorModel.validationErrorModel);
		section.id = id;
		section.ordinal = index;
		section.title = title;
		return section.buildForm({ destroyRef: this._destroyed, rootPath: `definition.sections[${index}].` }) as any;
	}

	/** Top-level sections carry the assets settings; a section leaving the top level drops them. */
	protected sectionFormFor(value: any, root: boolean): UntypedFormGroup {
		const vem = this.editorModel.validationErrorModel;
		if (root) {
			const model = new PlanBlueprintRootSectionEditorModel(vem).fromModel({ ...value, descriptionTemplates: [], prefillingSources: [] } as any);
			return model.buildForm({ destroyRef: this._destroyed, rootPath: 'definition.sections[0].' }) as any;
		}
		return new TemplateSectionEditorModel(vem).fromModel(value).buildForm({ rootPath: 'definition.sections[0].' });
	}

	protected computeProblems(): StructureProblem[] {
		return BlueprintProblemsHelper.compute({ formGroup: this.formGroup, entries: this.toCEntries, language: this.language, enumUtils: this.enumUtils, iconSet: this.iconSet });
	}

	// ---------------------------------------------------------------- templates

	onTemplatesPicked(templates: DescriptionTemplate[]): void {
		templates.forEach(t => this.descriptionTemplateMap.set(t.groupId.toString(), t));
		this.afterStructureChange(this.selectedTocEntry?.id);
	}

	// ---------------------------------------------------------------- author-facing preview data

	/** The form as a persist model for the author preview; refreshed when the Review tab opens. */
	get previewPersist(): PlanBlueprintPersist {
		const raw = this.formGroup.getRawValue();
		return { ...raw, definition: { pluginConfigurations: [], sections: raw?.definition?.sections } } as PlanBlueprintPersist;
	}

	get descriptionTemplateGuidMap(): Map<Guid, DescriptionTemplate> {
		const result = new Map<Guid, DescriptionTemplate>();
		this.descriptionTemplateMap.forEach((value) => result.set(value.groupId, value));
		return result;
	}
}
