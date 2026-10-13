import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { DescriptionTemplate, DescriptionTemplateDefinition } from '@app/core/model/description-template/description-template';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { DescriptionTemplateLookup } from '@app/core/query/description-template.lookup';
import { DescriptionTemplateService } from '@app/core/services/description-template/description-template.service';
import { StorageFileService } from '@app/core/services/storage-file/storage-file.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { IsActive } from '@notification-service/core/enum/is-active.enum';
import { takeUntil } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { TextFilterComponent } from '../../../../../common/modules/text-filter/text-filter.component';
import { MatCard, MatCardHeader, MatCardContent } from '@angular/material/card';
import { NgTemplateOutlet } from '@angular/common';
import { MatButton } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';
import { DateTimeFormatPipe } from '../../../../core/pipes/date-time-format.pipe';

@Component({
    selector: 'app-description-template-library-listing',
    templateUrl: './description-template-library-listing.component.html',
    styleUrl: './description-template-library-listing.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [TextFilterComponent, MatCard, MatCardHeader, MatCardContent, NgTemplateOutlet, MatButton, RouterLink, MatIcon, TranslatePipe, DateTimeFormatPipe]
})
export class DescriptionTemplateLibraryListingComponent extends BaseComponent implements OnInit {
    searchTerm: string = null;
    pageSize: number = 12;
    page: number = 1;
    count: number = 0;
    initialLoad: boolean = false;
    descriptionTemplates: DescriptionTemplate[] = [];

    constructor(
        private descriptionTemplateService: DescriptionTemplateService,
        protected enumUtils: EnumUtils,
        private storageFileService: StorageFileService,
        private sanitizer: DomSanitizer,
        private httpErrorHandlingService: HttpErrorHandlingService
    ){
        super();
    }

    ngOnInit(){
        this.getDescriptionTemplates();
    }

    getDescriptionTemplates(append: boolean = false){
        this.descriptionTemplateService.query(this.initializeLookup())
        .pipe(takeUntil(this._destroyed))
        .subscribe(
            {
                next: (res) => {
                    this.descriptionTemplates = append ? [...this.descriptionTemplates, ...res.items] : res.items;
                    this.count = res.count;
                    this.initialLoad = true;
                    this.downloadStorageFiles(res.items?.map((x: DescriptionTemplate) => x?.definition?.iconConfiguration?.storageFile?.id)?.filter((x) => x) ?? [])
                },
                error: (error) => this.onCallbackError(error)
            }
        )
    }

    protected storageFileMap = new Map<Guid, SafeUrl>([]);
    protected downloadStorageFiles(storageFileIds: Guid[]){
        const newIds = new Set(storageFileIds.filter((id) => !this.storageFileMap.has(id)));
        newIds.forEach((id) => {
            this.storageFileService.download(id).pipe(takeUntil(this._destroyed))
            .subscribe({
                next: response => {
                    this.storageFileMap.set(id, this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(response.body)));
                },
                error: error => this.onCallbackError(error)
            });
        })
    }

    protected onCallbackError(errorResponse: HttpErrorResponse) {		
		this.httpErrorHandlingService.handleBackedRequestError(errorResponse);
	}

    protected initializeLookup(groupId?: Guid): DescriptionTemplateLookup {
        const lookup = new DescriptionTemplateLookup();
        lookup.metadata = {countAll: true};
        lookup.page = {offset: (this.page - 1) * this.pageSize, size: this.pageSize};
        lookup.like=this.searchTerm;
        if (groupId) { lookup.groupIds = [groupId]; }
        lookup.isActive = [IsActive.Active];
        lookup.statuses = [DescriptionTemplateStatus.Finalized]
        lookup.versionStatuses = [DescriptionTemplateVersionStatus.Current]
        lookup.order = {items: ['-' + nameof<DescriptionTemplate>(x => x.updatedAt), '+' + nameof<DescriptionTemplate>(x => x.label)]};
        lookup.project = {
            fields: this.lookupFields
        };

        return lookup;
    }

    private readonly lookupFields: string[] = [
        nameof<DescriptionTemplate>(x => x.id),
        nameof<DescriptionTemplate>(x => x.label),
        nameof<DescriptionTemplate>(x => x.description),
        nameof<DescriptionTemplate>(x => x.code),
        nameof<DescriptionTemplate>(x => x.groupId),
        nameof<DescriptionTemplate>(x => x.createdAt),
        nameof<DescriptionTemplate>(x => x.updatedAt),
        nameof<DescriptionTemplate>(x => x.usageCount),
        nameof<DescriptionTemplate>(x => x.version),
        [nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
        [nameof<DescriptionTemplate>(x => x.definition), nameof<DescriptionTemplateDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
    ];

    loadMore(){
		this.page++;
		this.getDescriptionTemplates(true);
	}

    search(){
		this.page = 1;
		this.descriptionTemplates = [];
		this.getDescriptionTemplates();
	}
}
