import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { FileUtils } from '@app/core/services/utilities/file-utils.service';
import { BreadcrumbService } from '@app/ui/misc/breadcrumb/breadcrumb.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import * as FileSaver from 'file-saver';
import { takeUntil } from 'rxjs/operators';
import { STATUS_DRAFT, STATUS_FINALIZED, VERSION_CURRENT } from '../structure-editor-base';
import { VersionHistoryItem, VersionHistorySource } from './version-history-source';
import { NavigationBreadcrumbComponent } from '../../../misc/breadcrumb/navigation-breadcrumb.component';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatMenuTrigger, MatMenu, MatMenuContent, MatMenuItem } from '@angular/material/menu';
import { TranslatePipe } from '@ngx-translate/core';
import { DateTimeFormatPipe } from '../../../../core/pipes/date-time-format.pipe';
import { Bp2CountPipe } from '../count.pipe';

/**
 * One group's versions, newest first: which one authors get, which one is being drafted, how many use each, and a
 * way into every one of them. Serves plan blueprints and description templates through `VersionHistorySource`.
 */
@Component({
    selector: 'app-bp2-version-history',
    templateUrl: './version-history.component.html',
    styleUrls: ['./version-history.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [NavigationBreadcrumbComponent, MatButton, RouterLink, MatIcon, MatIconButton, MatMenuTrigger, MatMenu, MatMenuContent, MatMenuItem, TranslatePipe, DateTimeFormatPipe, Bp2CountPipe]
})
export class Bp2VersionHistoryComponent extends BaseComponent implements OnInit {
	versions: VersionHistoryItem[] = null;
	groupId: Guid;
	shared = false;

	constructor(
		private route: ActivatedRoute,
		public source: VersionHistorySource,
		public routerUtils: RouterUtilsService,
		private authService: AuthService,
		private fileUtils: FileUtils,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private breadcrumbService: BreadcrumbService,
	) { super(); }

	ngOnInit(): void {
		this.breadcrumbService.addExcludedParam('versions', true);
		this.groupId = Guid.parse(this.route.snapshot.paramMap.get('groupid'));
		this.shared = this.route.snapshot.queryParamMap.get('belongsToCurrentTenant') === 'false';
		this.load();
	}

	private load(): void {
		// same rule as the admin listings: own rows are the tenant-set ones, shared rows are the default tenant's
		const tenantId = this.authService.getSelectedTenantId();
		const tenantIsSet = tenantId ? !this.shared : null;
		this.source.query(this.groupId, tenantIsSet).pipe(takeUntil(this._destroyed)).subscribe({
			next: items => {
				this.versions = items ?? [];
				const current = this.current ?? this.versions[0];
				if (current) this.breadcrumbService.addIdResolvedValue(this.groupId.toString(), current.label);
			},
			error: error => { this.versions = []; this.httpErrorHandlingService.handleBackedRequestError(error); }
		});
	}

	t(key: string): string { return this.source.term(key); }

	get current(): VersionHistoryItem { return this.versions?.find(v => v.versionStatus === VERSION_CURRENT && v.status === STATUS_FINALIZED) ?? null; }
	get draft(): VersionHistoryItem { return this.versions?.find(v => v.status === STATUS_DRAFT) ?? null; }
	get name(): string { return (this.current ?? this.versions?.[0])?.label; }
	get totalUses(): number { return (this.versions ?? []).reduce((n, v) => n + (v.usageCount ?? 0), 0); }
	get canEdit(): boolean { return this.authService.hasPermission(this.source.editPermission) && !this.shared; }
	/** One draft at a time: a new version can start only from the current finalized one, and only if no draft exists. */
	get canStartNewVersion(): boolean { return this.canEdit && !!this.current && !this.draft; }

	state(v: VersionHistoryItem): 'draft' | 'current' | 'previous' {
		if (v.status !== STATUS_FINALIZED) return 'draft';
		return v.versionStatus === VERSION_CURRENT ? 'current' : 'previous';
	}

	editorLink(v: VersionHistoryItem): string[] { return [this.routerUtils.generateUrl(`${this.source.urlBase}/${v.id}`)]; }
	newVersionLink(): string[] { return [this.routerUtils.generateUrl(`${this.source.urlBase}/new-version/${this.current?.id}`)]; }
	cloneLink(v: VersionHistoryItem): string[] { return [this.routerUtils.generateUrl(`${this.source.urlBase}/clone/${v.id}`)]; }

	usageRoute(v: VersionHistoryItem): string[] { return [this.routerUtils.generateUrl(this.source.usageLink(v).path)]; }

	downloadXML(v: VersionHistoryItem): void {
		this.source.downloadXML(v.id).pipe(takeUntil(this._destroyed)).subscribe({
			next: response => FileSaver.saveAs(new Blob([response.body], { type: 'application/xml' }), this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'))),
			error: error => this.httpErrorHandlingService.handleBackedRequestError(error)
		});
	}
}
