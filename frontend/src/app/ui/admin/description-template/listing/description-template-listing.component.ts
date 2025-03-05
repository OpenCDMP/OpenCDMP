
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { SumarizeTextPipe } from '@app/core/pipes/sumarize-text.pipe';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { AuthService } from '@app/core/services/auth/auth.service';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { QueryParamsService } from '@app/core/services/utilities/query-params.service';
import { BaseListingComponent } from '@common/base/base-listing-component';
import { PipeService } from '@common/formatting/pipe.service';
import { DataTableDateTimeFormatPipe } from '@app/core/pipes/date-time-format.pipe';
import { IsActiveTypePipe } from '@common/formatting/pipes/is-active-type.pipe';
import { QueryResult } from '@common/model/query-result';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { ColumnDefinition, ColumnsChangedEvent, HybridListingComponent, PageLoadEvent } from '@common/modules/hybrid-listing/hybrid-listing.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import * as FileSaver from 'file-saver';
import { Observable } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ImportDescriptionTemplateDialogComponent } from './import-description-template/import-description-template.dialog.component';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { AppPermission } from '@app/core/common/enum/permission.enum';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';


@Component({
    selector: 'app-description-template-listing-component',
    templateUrl: './description-template-listing.component.html',
    styleUrls: ['./description-template-listing.component.scss'],
    standalone: false
})
export class DescriptionTemplateListingComponent extends BaseListingComponent<DescriptionTemplate, DescriptionTemplateLookup> implements OnInit {
	publish = false;
	userSettingsKey = { key: 'DescriptionTemplateListingUserSettings' };
	propertiesAvailableForOrder: ColumnDefinition[];
	descriptionTemplateStatuses = DescriptionTemplateStatus;
	mode;

	groupId: Guid | null = null;

	public permissionEnum = AppPermission;

	@ViewChild('descriptionTemplateStatus', { static: true }) descriptionTemplateStatus?: TemplateRef<any>;
	@ViewChild('actions', { static: true }) actions?: TemplateRef<any>;
	@ViewChild(HybridListingComponent, { static: true }) hybridListingComponent: HybridListingComponent;

	private readonly lookupFields: string[] = [
		nameof<DescriptionTemplate>(x => x.id),
		nameof<DescriptionTemplate>(x => x.label),
		nameof<DescriptionTemplate>(x => x.code),
		nameof<DescriptionTemplate>(x => x.description),
		nameof<DescriptionTemplate>(x => x.status),
		nameof<DescriptionTemplate>(x => x.version),
		nameof<DescriptionTemplate>(x => x.versionStatus),
		nameof<DescriptionTemplate>(x => x.groupId),
		nameof<DescriptionTemplate>(x => x.updatedAt),
		nameof<DescriptionTemplate>(x => x.createdAt),
		nameof<DescriptionTemplate>(x => x.hash),
		nameof<DescriptionTemplate>(x => x.belongsToCurrentTenant),
		nameof<DescriptionTemplate>(x => x.isActive),
		[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.EditDescriptionTemplate].join('.'),
		[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.DeleteDescriptionTemplate].join('.'),
		[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.CloneDescriptionTemplate].join('.'),
		[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.CreateNewVersionDescriptionTemplate].join('.'),
		[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.ImportDescriptionTemplate].join('.'),
		[nameof<DescriptionTemplate>(x => x.authorizationFlags), AppPermission.ExportDescriptionTemplate].join('.'),
	];

	rowIdentity = x => x.id;


	public hasPermission(permission: AppPermission, row: DescriptionTemplate): boolean {
		return this.authService.hasPermission(permission) || row?.authorizationFlags?.some(x => x === permission);
	}

	public hasExplicitPermission(permission: AppPermission): boolean {
		return this.authService.hasPermission(permission);
	}

	constructor(
		protected router: Router,
		protected route: ActivatedRoute,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected queryParamsService: QueryParamsService,
		private descriptionTemplateService: DescriptionTemplateService,
		public authService: AuthService,
		private pipeService: PipeService,
		public enumUtils: EnumUtils,
		private language: TranslateService,
		private dialog: MatDialog,
		private fileUtils: FileUtils,
		private sumarizeTextPipe: SumarizeTextPipe,
		private analyticsService: AnalyticsService,
		public routerUtils: RouterUtilsService,
		private breadcrumbService: BreadcrumbService,
	) {
		super(router, route, uiNotificationService, httpErrorHandlingService, queryParamsService);
		// Lookup setup
		// Default lookup values are defined in the user settings class.
		this.mode = this.route.snapshot?.data['mode'];
		this.lookup = this.initializeLookup();
		this.breadcrumbService.addExcludedParam("versions", true);
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionTemplateListing);
		super.ngOnInit();
	}

	protected initializeLookup(): DescriptionTemplateLookup {
		const lookup = new DescriptionTemplateLookup();
		lookup.metadata = { countAll: true };
		lookup.page = { offset: 0, size: this.ITEMS_PER_PAGE };
		lookup.isActive = [IsActive.Active];
		lookup.order = { items: [this.toDescSortField(nameof<DescriptionTemplate>(x => x.createdAt))] };
		if (this.mode && this.mode == 'versions-listing') {
			this.groupId = Guid.parse(this.route.snapshot.paramMap.get('groupid'));
			this.breadcrumbService.addExcludedParam(this.groupId.toString(), true);
			lookup.groupIds = [this.groupId];
			lookup.versionStatuses = null;
		} else {
			this.groupId = null;
			lookup.versionStatuses = [DescriptionTemplateVersionStatus.Current, DescriptionTemplateVersionStatus.NotFinalized];
		}
		this.updateOrderUiFields(lookup.order);

		lookup.project = {
			fields: this.lookupFields
		};

		return lookup;
	}

	protected setupColumns() {
		this.gridColumns.push(...[{
			prop: nameof<DescriptionTemplate>(x => x.label),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.NAME'
		},
		{
			prop: nameof<DescriptionTemplate>(x => x.code),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.CODE'
		},
		{
			prop: nameof<DescriptionTemplate>(x => x.description),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.DESCRIPTION',
			pipe: this.sumarizeTextPipe
		},
		{
			prop: nameof<DescriptionTemplate>(x => x.status),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.STATUS',
			cellTemplate: this.descriptionTemplateStatus
		},
		{
			prop: nameof<DescriptionTemplate>(x => x.version),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.VERSION'
		},
		{
			prop: nameof<DescriptionTemplate>(x => x.createdAt),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.CREATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<DescriptionTemplate>(x => x.updatedAt),
			sortable: true,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.UPDATED-AT',
			pipe: this.pipeService.getPipe<DataTableDateTimeFormatPipe>(DataTableDateTimeFormatPipe).withFormat('short')
		},
		{
			prop: nameof<DescriptionTemplate>(x => x.isActive),
			sortable: false,
			languageName: 'DESCRIPTION-TEMPLATE-LISTING.FIELDS.IS-ACTIVE',
			pipe: this.pipeService.getPipe<IsActiveTypePipe>(IsActiveTypePipe)
		},
		{
			alwaysShown: true,
			cellTemplate: this.actions,
			maxWidth: 120
		}
		]);
		this.propertiesAvailableForOrder = this.gridColumns.filter(x => x.sortable);
	}

	//
	// Listing Component functions
	//
	onColumnsChanged(event: ColumnsChangedEvent) {
		super.onColumnsChanged(event);
		this.onColumnsChangedInternal(event.properties.map(x => x.toString()));
	}

	public showActions(descriptionTemplate: DescriptionTemplate): boolean {
		return this.canEdit(descriptionTemplate) || this.canAddNewVersion(descriptionTemplate) || 
			this.canClone(descriptionTemplate) || this.canViewVersions(descriptionTemplate) || 
			this.canDownloadXml(descriptionTemplate) || this.canDelete(descriptionTemplate);
	}

	public canEdit(descriptionTemplate: DescriptionTemplate): boolean {	
		return descriptionTemplate.status !== DescriptionTemplateStatus.Finalized && this.hasPermission(AppPermission.EditDescriptionTemplate, descriptionTemplate) && descriptionTemplate.belongsToCurrentTenant && descriptionTemplate.isActive == IsActive.Active;
	}

	public canCreateNew(): boolean {
		return this.hasExplicitPermission(AppPermission.EditDescriptionTemplate);
	}

	public canAddNewVersion(descriptionTemplate: DescriptionTemplate): boolean {	
		return descriptionTemplate.versionStatus == DescriptionTemplateVersionStatus.Current && descriptionTemplate.status == DescriptionTemplateStatus.Finalized && descriptionTemplate.belongsToCurrentTenant != false && descriptionTemplate.isActive == IsActive.Active && this.canCreateNew();
	}

	public canClone(descriptionTemplate: DescriptionTemplate): boolean {
		return this.hasPermission(AppPermission.CloneDescriptionTemplate, descriptionTemplate);
	}

	public canViewVersions(descriptionTemplate: DescriptionTemplate): boolean {
		return descriptionTemplate.isActive == IsActive.Active && this.canCreateNew();
	}

	public canDownloadXml(descriptionTemplate: DescriptionTemplate): boolean {
        return this.authService.hasPermission(AppPermission.ExportDescriptionTemplate);
	}

	public canDelete(descriptionTemplate: DescriptionTemplate): boolean {
		return descriptionTemplate.belongsToCurrentTenant != false && descriptionTemplate.isActive == IsActive.Active && this.hasPermission(AppPermission.DeleteDescriptionTemplate, descriptionTemplate);
	}

	private onColumnsChangedInternal(columns: string[]) {
		// Here are defined the projection fields that always requested from the api.
		const fields = new Set(this.lookupFields);
		this.gridColumns.map(x => x.prop)
			.filter(x => !columns?.includes(x as string))
			.forEach(item => {
				fields.delete(item as string)
			});
		this.lookup.project = { fields: [...fields] };
		this.onPageLoad({ offset: 0 } as PageLoadEvent);
	}

	protected loadListing(): Observable<QueryResult<DescriptionTemplate>> {
		this.lookup.onlyCanEdit = true;
		return this.descriptionTemplateService.query(this.lookup).pipe(
			takeUntil(this._destroyed),
			tap((descriptionTemplates: QueryResult<DescriptionTemplate>) => {
				const groupCode = descriptionTemplates.items?.[0]?.code;
				if (this.groupId != null && groupCode != null) {
						this.breadcrumbService.addIdResolvedValue(this.groupId.toString(), groupCode);
				}
			})
		);
	}

	public delete(id: Guid) {
		if (id) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				data: {
					isDeleteConfirmation: true,
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-ITEM'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRM'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCEL')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.descriptionTemplateService.delete(id).pipe(takeUntil(this._destroyed))
						.subscribe(
							complete => this.onCallbackSuccess(),
							error => this.onCallbackError(error)
						);
				}
			});
		}
	}

	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-DELETE'), SnackBarNotificationLevel.Success);
		this.refresh();
	}

	export(event: PointerEvent, id: Guid): void {
		event?.stopPropagation();
		this.descriptionTemplateService.downloadXML(id)
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => {
				const blob = new Blob([response.body], { type: 'application/xml' });
				const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
				FileSaver.saveAs(blob, filename);
			},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	import(): void {
		const dialogRef = this.dialog.open(ImportDescriptionTemplateDialogComponent, {
			restoreFocus: false,
			data: {
				message: this.language.instant('DESCRIPTION-TEMPLATE-LISTING.IMPORT.UPLOAD-XML-FILE-TITLE'),
				confirmButton: this.language.instant('DESCRIPTION-TEMPLATE-LISTING.IMPORT.UPLOAD-XML'),
				cancelButton: this.language.instant('DESCRIPTION-TEMPLATE-LISTING.IMPORT.UPLOAD-XML-FILE-CANCEL'),
				name: '',
				file: FileList,
				sucsess: false
			},
            width: 'min(600px, 90vw)'
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(data => {
			if (data && data.sucsess && data.name != null && data.file != null) {
				this.descriptionTemplateService.uploadFile(data.file, data.name)
					.pipe(takeUntil(this._destroyed))
					.subscribe(_ => {
						this.uiNotificationService.snackBarNotification(this.language.instant('DESCRIPTION-TEMPLATE-LISTING.MESSAGES.TEMPLATE-UPLOAD-SUCCESS'), SnackBarNotificationLevel.Success);
						this.refresh();
					},
					error => this.httpErrorHandlingService.handleBackedRequestError(error));
			}
		});
	}
}
