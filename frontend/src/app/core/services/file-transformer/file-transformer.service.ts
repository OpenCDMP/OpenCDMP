import { effect, Injectable, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BaseService } from '@common/base/base.service';
import { of } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import { ConfigurationField } from '@app/core/model/plugin-configuration/plugin-configuration';
import { ExportOptionsDialogComponent, ExportSelectiveItem } from '@app/ui/plugin/export-options-dialog/export-options-dialog.component';

export interface ExportSelectiveContext {
	sections?: ExportSelectiveItem[];
	descriptions?: ExportSelectiveItem[];
}
import { FileTransformerHttpService } from './file-transformer.http.service';
import { Guid } from '@common/types/guid';
import * as FileSaver from 'file-saver';
import { FileUtils } from '../utilities/file-utils.service';
import { AuthService } from '../auth/auth.service';
import { FileTransformerConfiguration } from '@app/core/model/file/file-transformer-configuration.model';
import { FileTransformerEntityType } from '@app/core/common/enum/file-transformer-entity-type';
import { PlanService } from '../plan/plan.service';
import { DescriptionService } from '../description/description.service';
import { AnalyticsService } from '../matomo/analytics-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';

@Injectable()
export class FileTransformerService extends BaseService {

	private _initialized: boolean = false;
	private _loading = signal<boolean>(false);

	constructor(
		private fileTransformerHttpService: FileTransformerHttpService,
		private analyticsService: AnalyticsService,
		private fileUtils: FileUtils,
		private planService: PlanService,
		private descriptionService: DescriptionService,
		private authentication: AuthService,
		private httpErrorHandlingService: HttpErrorHandlingService,
		private dialog: MatDialog
	) {
        super();
        effect(() => {
            const authenticationComplete = this.authentication.currentAccountIsAuthenticated();
            const loading = this._loading();
            if(authenticationComplete && !loading && !this._initialized){
                this.init();
            }
        })
    }

	private xmlExportRepo: FileTransformerConfiguration = {
		exportEntityTypes: [FileTransformerEntityType.Description, FileTransformerEntityType.Plan],
		exportVariants: [{
			format: "xml",
			hasLogo: true,
			icon: "fa-file-code-o"
		}],
		fileTransformerId: "app_xml_export"
	}

	private _availableFormats = signal<FileTransformerConfiguration[]>(null);
	
    public availableFormats = this._availableFormats.asReadonly();

	public availableFormatsFor(entityType: FileTransformerEntityType) {
		if (this.availableFormats()) {
			return this.availableFormats().filter(x => x.exportEntityTypes.includes(entityType));
		}
		return [];
	}

	public availableImportFormatsFor(entityType: FileTransformerEntityType) {
		if (this.availableFormats()) {
			return this.availableFormats().filter(x => x.importEntityTypes?.includes(entityType));
		}
		return [];
	}

	init() {
		this._loading.set(true);
		this.fileTransformerHttpService.getAvailableConfigurations().pipe(takeUntil(this._destroyed), catchError((error) => {
			this._loading.set(false);
			this._initialized = true;
			// still offer the built-in XML export
			return of([]);
		})).subscribe(items => {
			this._availableFormats.set([...items, this.xmlExportRepo]);
			this._loading.set(false);
			this._initialized = true;
		});
	}

	exportPlan(id: Guid, fileTransformerId: string, format: string, isPublic: boolean = false, selective?: ExportSelectiveContext) {
		this.withExportOptions(fileTransformerId, FileTransformerEntityType.Plan, (options) => this.doExportPlan(id, fileTransformerId, format, isPublic, options), selective);
	}

	private withExportOptions(fileTransformerId: string, entityType: FileTransformerEntityType, proceed: (options?: { [code: string]: string }) => void, selective?: ExportSelectiveContext) {
		const fields: ConfigurationField[] = this.availableFormats()
			?.find(x => x.fileTransformerId === fileTransformerId)
			?.userConfigurationFields
			?.filter(field => field?.appliesTo?.some(applies => (applies as unknown as number) === (entityType as unknown as number))) ?? [];

		const selectiveCodes = ['include-sections', 'exclude-descriptions'];
		const regularFields = fields.filter(x => !selectiveCodes.includes(x.code));
		const hasSelectiveUi = (selective?.sections?.length > 0 && fields.some(x => x.code === 'include-sections'))
			|| (selective?.descriptions?.length > 0 && fields.some(x => x.code === 'exclude-descriptions'));
		if (regularFields.length === 0 && !hasSelectiveUi) {
			proceed(undefined);
			return;
		}
		this.dialog.open(ExportOptionsDialogComponent, {
			width: '440px',
			restoreFocus: false,
			data: { transformerId: fileTransformerId, fields: fields, sections: selective?.sections, descriptions: selective?.descriptions }
		}).afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result === null || result === undefined) return; // cancelled
			proceed(result);
		});
	}

	private doExportPlan(id: Guid, fileTransformerId: string, format: string, isPublic: boolean, options?: { [code: string]: string }) {
		this._loading.set(true);
		if (fileTransformerId == this.xmlExportRepo.fileTransformerId) {
			if (!isPublic) {
				this.planService.downloadXML(id)
				.pipe(takeUntil(this._destroyed))
				.subscribe(response => {
					const blob = new Blob([response.body], { type: 'application/xml' });
					const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
					FileSaver.saveAs(blob, filename);
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			} else {
				this.planService.downloadPublicXML(id)
				.pipe(takeUntil(this._destroyed))
				.subscribe(response => {
					const blob = new Blob([response.body], { type: 'application/xml' });
					const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
					FileSaver.saveAs(blob, filename);
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			}
		} else {
			if (!isPublic) {
				this.fileTransformerHttpService.exportPlan(id, fileTransformerId, format, options).pipe(takeUntil(this._destroyed), catchError((error) => {
					this._loading.set(false);
					return null;
				}))
				.subscribe(result => {
					if (result !== null) {
						const blob = new Blob([result.body], { type: 'application/octet-stream' });
						const filename = this.fileUtils.getFilenameFromContentDispositionHeader(result.headers.get('Content-Disposition'));

						FileSaver.saveAs(blob, filename);
						this.analyticsService.trackDownload(AnalyticsService.trackPlan, format, id.toString());
					}
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			} else {
				this.fileTransformerHttpService.exportPublicPlan(id, fileTransformerId, format, options).pipe(takeUntil(this._destroyed), catchError((error) => {
					this._loading.set(false);
					return null;
				}))
				.subscribe(result => {
					if (result !== null) {
						const blob = new Blob([result.body], { type: 'application/octet-stream' });
						const filename = this.fileUtils.getFilenameFromContentDispositionHeader(result.headers.get('Content-Disposition'));

						FileSaver.saveAs(blob, filename);
						this.analyticsService.trackDownload(AnalyticsService.trackPlan, format, id.toString());
					}
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			}
		}
	}

	exportDescription(id: Guid, repositoryId: string, format: string, isPublic: boolean = false) {
		this.withExportOptions(repositoryId, FileTransformerEntityType.Description, (options) => this.doExportDescription(id, repositoryId, format, isPublic, options));
	}

	private doExportDescription(id: Guid, repositoryId: string, format: string, isPublic: boolean, options?: { [code: string]: string }) {
		this._loading.set(true);
		if (repositoryId == this.xmlExportRepo.fileTransformerId) {
			if (!isPublic) {
				this.descriptionService.downloadXML(id)
				.pipe(takeUntil(this._destroyed))
				.subscribe(response => {
					const blob = new Blob([response.body], { type: 'application/xml' });
					const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
					FileSaver.saveAs(blob, filename);
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			} else {
				this.descriptionService.downloadPublicXML(id)
				.pipe(takeUntil(this._destroyed))
				.subscribe(response => {
					const blob = new Blob([response.body], { type: 'application/xml' });
					const filename = this.fileUtils.getFilenameFromContentDispositionHeader(response.headers.get('Content-Disposition'));
					FileSaver.saveAs(blob, filename);
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			}
		} else {
			if (!isPublic) {
				this.fileTransformerHttpService.exportDescription(id, repositoryId, format, options).pipe(takeUntil(this._destroyed), catchError((error) => {
					this._loading.set(false);
					return null;
				}))
				.subscribe(result => {
					if (result !== null) {
						const blob = new Blob([result.body], { type: 'application/octet-stream' });
						const filename = this.fileUtils.getFilenameFromContentDispositionHeader(result.headers.get('Content-Disposition'));

						FileSaver.saveAs(blob, filename);
						this.analyticsService.trackDownload(AnalyticsService.trackDescriptions, format, id.toString());
					}
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			} else {
				this.fileTransformerHttpService.exportPublicDescription(id, repositoryId, format, options).pipe(takeUntil(this._destroyed), catchError((error) => {
					this._loading.set(false);
					return null;
				}))
				.subscribe(result => {
					if (result !== null) {
						const blob = new Blob([result.body], { type: 'application/octet-stream' });
						const filename = this.fileUtils.getFilenameFromContentDispositionHeader(result.headers.get('Content-Disposition'));

						FileSaver.saveAs(blob, filename);
						this.analyticsService.trackDownload(AnalyticsService.trackDescriptions, format, id.toString());
					}
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
			}
		}
	}
}
