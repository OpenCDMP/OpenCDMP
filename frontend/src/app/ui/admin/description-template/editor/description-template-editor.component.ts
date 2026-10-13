import { Component, forwardRef, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { FormArray, FormGroup, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { UserDescriptionTemplateRole } from '@app/core/common/enum/user-description-template-role';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { DescriptionTemplatePersist, NewVersionDescriptionTemplatePersist } from '@app/core/model/description-template/description-template-persist';
import { LanguageInfo } from '@app/core/model/language-info';
import { PluginRepositoryConfiguration } from '@app/core/model/plugin-configuration/plugin-configuration';
import { User } from '@app/core/model/user/user';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageInfoService } from '@app/core/services/culture/language-info-service';
import { DescriptionTemplateTypeService } from '@app/core/services/description-template-type/description-template-type.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { LockService } from '@app/core/services/lock/lock.service';
import { LoggingService } from '@app/core/services/logging/logging-service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { PluginConfigurationService } from '@app/core/services/plugin/plugin-configuration.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { StructureEditorBase } from '@app/ui/admin/structure-editor/structure-editor-base';
import { StructureProblem, StructureProblemsHelper } from '@app/ui/admin/structure-editor/structure-problems';
import { BlueprintStructureHelper } from '@app/ui/admin/structure-editor/structure.helper';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { DescriptionTemplateEditorModel, DescriptionTemplateForm, TemplateSectionEditorModel, UserDescriptionTemplateEditorModel } from '../editor/description-template-editor.model';
import { DescriptionTemplateEditorResolver } from './description-template-editor.resolver';
import { DescriptionTemplateEditorService } from '../editor/description-template-editor.service';
import { TEMPLATE_TERMS } from './template-terms';
import { EditorShellComponent } from '../../structure-editor/shell/editor-shell.component';
import { MatIcon } from '@angular/material/icon';
import { Dt2DetailsTabComponent } from './tabs/details-tab/details-tab.component';
import { Bp2StructureTabComponent } from '../../structure-editor/structure/structure-tab.component';
import { Dt2ReviewTabComponent } from './tabs/review-tab/review-tab.component';
import { Bp2CountPipe } from '../../structure-editor/count.pipe';

/**
 * The description template ("asset template") editor, on the reactive form of `DescriptionTemplateEditorModel`.
 * Everything structural, the frame, the dialogs and most Details and Review cards are shared with the plan blueprint
 * editor through `StructureEditorBase`.
 */
@Component({
    selector: 'app-description-template-editor',
    templateUrl: './description-template-editor.component.html',
    styleUrls: ['./description-template-editor.component.scss'],
    providers: [DescriptionTemplateEditorService, { provide: StructureEditorBase, useExisting: forwardRef(() => DescriptionTemplateEditorComponent) }],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [EditorShellComponent, MatIcon, forwardRef(() => Dt2DetailsTabComponent), Bp2StructureTabComponent, forwardRef(() => Dt2ReviewTabComponent), Bp2CountPipe]
})
export class DescriptionTemplateEditorComponent extends StructureEditorBase<DescriptionTemplateEditorModel, DescriptionTemplate> implements OnInit {
	declare formGroup: FormGroup<DescriptionTemplateForm>;

	pluginsInitialized: Observable<PluginRepositoryConfiguration[]>;
	typeConfiguration: SingleAutoCompleteConfiguration;
	readonly languages: LanguageInfo[] = this.languageInfoService.getLanguageInfoValues();
	/** People on the Editors card, by id, so rows can show names. */
	usersMap = new Map<string, User>();

	readonly urlBase = '/asset-templates';
	readonly lockTargetType = LockTargetType.DescriptionTemplate;
	readonly pluginEntityType = PluginEntityType.Description;
	readonly requiredPresets = BlueprintStructureHelper.TEMPLATE_REQUIRED_PRESETS;
	readonly optionalPresets = BlueprintStructureHelper.TEMPLATE_OPTIONAL_PRESETS;
	protected readonly termOverrides = TEMPLATE_TERMS;

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
		private descriptionTemplateService: DescriptionTemplateService,
		private descriptionTemplateTypeService: DescriptionTemplateTypeService,
		private descriptionTemplateEditorService: DescriptionTemplateEditorService,
		private languageInfoService: LanguageInfoService,
		private logger: LoggingService,
		private analyticsService: AnalyticsService,
		private pluginConfigurationService: PluginConfigurationService,
	) {
		super(dialog, language, formService, router, uiNotificationService, httpErrorHandlingService, filterService, route, queryParamsService, lockService, authService, configurationService);
		this.pluginsInitialized = toObservable(this.pluginConfigurationService.pluginRepositoryConfiguration);
	}

	// ---------------------------------------------------------------- lifecycle

	ngOnInit(): void {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionTemplateEditor);
		this.initStructureEditor();
		this.pluginsInitialized.pipe(takeUntil(this._destroyed)).subscribe((initialized) => {
			if (!initialized) return;
			super.ngOnInit();
			this.route.data.pipe(takeUntil(this._destroyed)).subscribe(d => this.initModelFlags(d['action']));
		});
		this.typeConfiguration = this.descriptionTemplateTypeService.getSingleAutocompleteConfiguration([DescriptionTemplateTypeStatus.Finalized]);
	}

	// ---------------------------------------------------------------- data

	get pluginRepositoryConfigurations(): PluginRepositoryConfiguration[] { return this.pluginConfigurationService.getPluginRepositoryConfigurationFor(PluginType.FileTransformer, [PluginEntityType.Description]); }
	get plugins() { return this.pluginConfigurationService.getAvailablePluginsFor(PluginType.FileTransformer, [PluginEntityType.Description]); }

	getItem(itemId: Guid, successFunction: (item: DescriptionTemplate) => void) {
		this.descriptionTemplateService.getSingle(itemId, DescriptionTemplateEditorResolver.lookupFields())
			.pipe(map(data => data as DescriptionTemplate), takeUntil(this._destroyed))
			.subscribe({ next: (data) => successFunction(data), error: (error) => this.onCallbackError(error) });
	}

	prepareForm(data: DescriptionTemplate) {
		try {
			// the resolver's object is shared with the route; work on a copy
			let copy: DescriptionTemplate = data ? JSON.parse(JSON.stringify(data)) : null;
			this.fileMap.clear();
			if (copy == null) {
				copy = { status: DescriptionTemplateStatus.Draft, definition: { pluginConfigurations: this.plugins, sections: [] } } as DescriptionTemplate;
			} else if (!copy.definition?.pluginConfigurations) {
				copy.definition = { ...(copy.definition ?? {}), pluginConfigurations: this.plugins } as any;
			}
			copy.definition?.pluginConfigurations?.forEach(x => x.fields?.forEach(field => { if (field.fileValue) this.fileMap.set(field.fileValue.id, field.fileValue); }));
			if (this.isClone) copy.belongsToCurrentTenant = true;
			this.item = copy;
			this.editorModel = new DescriptionTemplateEditorModel().fromModel(copy, this.pluginRepositoryConfigurations);
			this.usersMap.clear();
			(copy.users ?? []).forEach(u => { if (u.user?.id) this.usersMap.set(u.user.id.toString(), u.user); });
			this.isDeleted = copy.isActive === IsActive.Inactive;
			this.belongsToCurrentTenant = this.isNew || this.isClone || copy.belongsToCurrentTenant !== false;
			this.buildForm();
			this.afterFormPrepared(copy);
		} catch (error) {
			this.logger.error('Could not parse descriptionTemplate item: ' + data + error);
			this.uiNotificationService.snackBarNotification(this.language.instant('COMMONS.ERRORS.DEFAULT'), SnackBarNotificationLevel.Error);
		}
	}

	buildForm() {
		this.formGroup = this.editorModel.buildForm(null, this.readOnly);
		this.descriptionTemplateEditorService.setValidationErrorModel(this.editorModel.validationErrorModel);
		if (this.isNewVersion) {
			this.formGroup.get('code').disable();
			this.formGroup.get('status').setValue(DescriptionTemplateStatus.Draft);
		}
	}

	refreshData(id?: Guid): void {
		this.getItem(id ?? this.editorModel.id, (data: DescriptionTemplate) => this.prepareForm(data));
	}

	protected persistRequest(formValue: any, newVersion: boolean): Observable<DescriptionTemplate> {
		return newVersion ? this.descriptionTemplateService.newVersion(formValue as NewVersionDescriptionTemplatePersist) : this.descriptionTemplateService.persist(formValue as DescriptionTemplatePersist);
	}
	protected semanticsInUseRequest(excludeGroupId: Guid) { return this.descriptionTemplateService.semanticsInUse(excludeGroupId); }
	protected deleteRequest(id: Guid) { return this.descriptionTemplateService.delete(id); }
	protected downloadRequest(id: Guid) { return this.descriptionTemplateService.downloadXML(id); }
	protected fetchFull(id: Guid) { return this.descriptionTemplateService.getSingle(id, DescriptionTemplateEditorResolver.lookupFields()); }
	protected queryVersions(groupId: Guid): Observable<DescriptionTemplate[]> {
		const lookup = new DescriptionTemplateLookup();
		lookup.page = { offset: 0, size: 50 };
		lookup.groupIds = [groupId];
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: ['-' + nameof<DescriptionTemplate>(x => x.version)] };
		lookup.project = { fields: [nameof<DescriptionTemplate>(x => x.id), nameof<DescriptionTemplate>(x => x.label), nameof<DescriptionTemplate>(x => x.version), nameof<DescriptionTemplate>(x => x.status), nameof<DescriptionTemplate>(x => x.versionStatus), nameof<DescriptionTemplate>(x => x.updatedAt), nameof<DescriptionTemplate>(x => x.usageCount)] };
		return this.descriptionTemplateService.query(lookup).pipe(map(result => result.items));
	}

	protected detailsDirty(): boolean {
		const definition = this.formGroup.controls.definition;
		return ['label', 'code', 'description', 'language', 'type', 'users'].some(k => this.formGroup.get(k)?.dirty)
			|| ['iconConfiguration', 'pluginConfigurations'].some(k => definition.get(k)?.dirty);
	}

	// ---------------------------------------------------------------- state & guards

	/** A global permission, or one granted on this template (an editor listed on it). */
	/** A template's editors may take over its lock, like admins (the backend grants DeleteLock to both). */
	protected takeOverAllowed(): boolean { return super.takeOverAllowed() || this.hasPermission(AppPermission.EditDescriptionTemplate); }

	hasPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission) || !!this.editorModel?.permissions?.includes(permission) || !!this.item?.authorizationFlags?.some(x => x === permission);
	}
	/** Read-only: locked by someone else, or for good (deleted, finalized, shared, no permission). A new version is editable. */
	get readOnly(): boolean {
		return this.isLocked || ((this.isDeleted || this.isFinalized || !this.hasPermission(AppPermission.EditDescriptionTemplate) || !this.belongsToCurrentTenant) && !this.isNewVersion);
	}
	get canDelete(): boolean { return !this.isDeleted && !this.isTransient && this.belongsToCurrentTenant && this.hasPermission(AppPermission.DeleteDescriptionTemplate); }
	get canCreateNewVersion(): boolean {
		return this.isFinalized && !this.isTransient && this.item?.versionStatus == DescriptionTemplateVersionStatus.Current && this.belongsToCurrentTenant
			&& (this.hasPermission(AppPermission.CreateNewVersionDescriptionTemplate) || this.hasPermission(AppPermission.EditDescriptionTemplate));
	}
	get canClone(): boolean { return !this.isTransient && (this.hasPermission(AppPermission.CloneDescriptionTemplate) || this.hasPermission(AppPermission.EditDescriptionTemplate)); }
	get sectionsArray(): UntypedFormArray { return this.formGroup?.controls?.definition?.controls?.sections as unknown as UntypedFormArray; }

	// ---------------------------------------------------------------- editors

	/** The template's language by name, for the header. */
	get languageName(): string { const code = this.formGroup?.get('language')?.value; return code ? (this.languages.find(l => l.code === code)?.name ?? code) : null; }

	get usersArray(): FormArray { return this.formGroup.get('users') as FormArray; }
	userName(userId: Guid | string): string { return this.usersMap.get(userId?.toString())?.name ?? ''; }
	isMe(userId: Guid | string): boolean { return !!userId && userId?.toString() === this.authService.userId()?.toString(); }
	editorRows(): { userId: Guid, name: string, role: UserDescriptionTemplateRole }[] {
		return this.usersArray.controls.map(c => ({ userId: c.get('userId').value, name: this.userName(c.get('userId').value), role: c.get('role').value }));
	}
	/** Editors can be managed whenever the template may be changed at all, finalized or not: they are not part of a version. */
	get canManageEditors(): boolean {
		return !this.isDeleted && !this.isLocked && this.belongsToCurrentTenant && this.hasPermission(AppPermission.EditDescriptionTemplate);
	}
	/** A saved template's editors are saved on their own, like plan collaborators; a new one's go in with its first save. */
	get editorsSaveOnTheirOwn(): boolean { return !this.isTransient && !!this.item?.id; }
	editorsSaving = false;
	get ownerCount(): number { return this.usersArray.controls.filter(c => c.get('role').value === UserDescriptionTemplateRole.Owner).length; }
	get memberCount(): number { return this.usersArray.length - this.ownerCount; }
	get myRole(): UserDescriptionTemplateRole { return this.usersArray.controls.find(c => this.isMe(c.get('userId').value))?.get('role').value ?? null; }
	/** "Maria, Nikos and Eleni", in the reader's language. */
	nameList(names: string[]): string {
		try { return new Intl.ListFormat(this.language.currentLang || 'en', { style: 'long', type: 'conjunction' }).format(names); } catch { return names.join(', '); }
	}

	/** Adds a person to the template, as a member unless said otherwise. */
	addUser(user: User, role: UserDescriptionTemplateRole = UserDescriptionTemplateRole.Member): void {
		if (!user?.id || this.usersArray.controls.some(c => c.get('userId').value?.toString() === user.id.toString())) return;
		this.usersMap.set(user.id.toString(), user);
		const before = this.usersArray.getRawValue();
		this.usersArray.push(this.userForm(user.id, role, this.usersArray.length));
		this.usersChanged(before, 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.ADDED', { name: user.name, role: this.enumUtils.toUserDescriptionTemplateRoleString(role) });
	}

	setUserRole(index: number, role: UserDescriptionTemplateRole): void {
		const control = this.usersArray.at(index)?.get('role');
		if (!control || control.value === role) return;
		const before = this.usersArray.getRawValue();
		control.setValue(role);
		this.usersChanged(before, 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.ROLE-CHANGED', { name: this.userName(this.usersArray.at(index).get('userId').value), role: this.enumUtils.toUserDescriptionTemplateRoleString(role) });
	}

	removeUser(index: number): void {
		const before = this.usersArray.getRawValue();
		const name = this.userName(this.usersArray.at(index)?.get('userId').value);
		this.usersArray.removeAt(index);
		this.reapplyValidators();
		this.usersChanged(before, 'DESCRIPTION-TEMPLATE-EDITOR.EDITORS.REMOVED', { name });
	}

	private userForm(userId: Guid, role: UserDescriptionTemplateRole, index: number) {
		const model = new UserDescriptionTemplateEditorModel(this.editorModel.validationErrorModel);
		model.userId = userId;
		model.role = role;
		return model.buildForm({ rootPath: `users[${index}].` });
	}

	private setUsers(users: { userId: Guid, role: UserDescriptionTemplateRole }[]): void {
		while (this.usersArray.length) this.usersArray.removeAt(0);
		users.forEach((u, i) => this.usersArray.push(this.userForm(u.userId, u.role, i)));
	}

	/** Saves the editors right away when the template exists; otherwise they wait for the template's first save. */
	private usersChanged(before: { userId: Guid, role: UserDescriptionTemplateRole }[], messageKey: string, params: object): void {
		if (!this.editorsSaveOnTheirOwn) { this.usersArray.markAsDirty(); return; }
		this.editorsSaving = true;
		const users = this.usersArray.getRawValue();
		const fields = [nameof<DescriptionTemplate>(x => x.id), ...['user.id', 'user.name', 'role', 'isActive'].map(f => `users.${f}`)];
		this.descriptionTemplateService.assignUsers(this.item.id, users, fields).pipe(takeUntil(this._destroyed)).subscribe({
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

	// ---------------------------------------------------------------- structure hooks

	protected reapplyValidators(): void {
		DescriptionTemplateEditorModel.reApplyDefinitionValidators({ formGroup: this.formGroup as unknown as UntypedFormGroup, validationErrorModel: this.editorModel.validationErrorModel });
	}

	protected newRootSectionForm(id: string, index: number, title: string): UntypedFormGroup {
		const section = new TemplateSectionEditorModel(this.editorModel.validationErrorModel);
		section.id = id;
		section.ordinal = index;
		section.title = title;
		return section.buildForm({ rootPath: `definition.sections[${index}].` });
	}

	/** Every level of a template holds the same kind of section. */
	protected sectionFormFor(value: any, _root: boolean): UntypedFormGroup {
		return new TemplateSectionEditorModel(this.editorModel.validationErrorModel).fromModel(value).buildForm({ rootPath: 'definition.sections[0].' });
	}

	protected computeProblems(): StructureProblem[] {
		const t = (key: string, args?: any) => this.language.instant(this.term(key), args);
		const form = this.formGroup;
		const details = t('PLAN-BLUEPRINT-EDITOR.TABS.DETAILS');
		const problems: StructureProblem[] = [];
		const blocker = (id: string, message: string) => problems.push({ id, severity: 'blocker', blocksSave: true, message, where: details, tab: 'details' });

		if (form.get('label').invalid) blocker('label', t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NAME-REQUIRED'));
		const code = form.get('code');
		if (code.enabled && code.invalid) blocker('code', t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.CODE-REQUIRED'));
		if (form.get('type').hasError('required')) blocker('type', t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.TYPE-REQUIRED'));
		if (form.get('language').hasError('required')) blocker('language', t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.LANGUAGE-REQUIRED'));
		if (form.get('description').hasError('required')) blocker('description', t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.DESCRIPTION-REQUIRED'));
		const be = (key: string, labelKey: string) => StructureProblemsHelper.backendOn(problems, form.get(key), `${key}-be`, t(labelKey), details);
		be('label', 'PLAN-BLUEPRINT-EDITOR.DETAILS.NAME');
		be('code', 'PLAN-BLUEPRINT-EDITOR.DETAILS.CODE');
		be('description', 'PLAN-BLUEPRINT-EDITOR.DETAILS.DESCRIPTION');
		be('language', 'DESCRIPTION-TEMPLATE-EDITOR.DETAILS.LANGUAGE');
		be('type', 'PLAN-BLUEPRINT-EDITOR.DETAILS.TYPE');
		if ((form.controls.definition.get('pluginConfigurations') as UntypedFormArray)?.invalid) blocker('plugins', t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.EXPORT-TEMPLATE'));

		problems.push(...StructureProblemsHelper.compute({ sections: this.sectionsArray, entries: this.toCEntries, requiredPresets: this.requiredPresets, language: this.language, enumUtils: this.enumUtils, term: (k) => this.term(k) }));

		if (!this.iconSet) problems.push({ id: 'icon', severity: 'optional', blocksSave: false, tab: 'details', where: details, message: t('PLAN-BLUEPRINT-EDITOR.PROBLEMS.NO-ICON') });
		if (!problems.some(p => p.tab === 'details' && p.severity === 'blocker')) problems.unshift({ id: 'details-ok', severity: 'ok', blocksSave: false, tab: 'details', where: details, message: t('DESCRIPTION-TEMPLATE-EDITOR.PROBLEMS.DETAILS-OK') });
		return problems;
	}

	// ---------------------------------------------------------------- author-facing preview data

	/** The form as a persist model for the author preview; taken when the Review tab opens. */
	previewPersist(): DescriptionTemplatePersist { return this.formGroup.getRawValue() as DescriptionTemplatePersist; }
}
