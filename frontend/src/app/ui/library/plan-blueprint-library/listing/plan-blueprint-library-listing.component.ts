import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { PlanBlueprintVersionStatus } from '@app/core/common/enum/plan-blueprint-version-status';
import { IconConfiguration } from '@app/core/model/icon-configuration/icon-configuration';
import { PlanBlueprint, PlanBlueprintDefinition } from '@app/core/model/plan-blueprint/plan-blueprint';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PlanBlueprintLookup } from '@app/core/query/plan-blueprint.lookup';
import { PlanBlueprintService } from '@app/core/services/plan/plan-blueprint.service';
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
    selector: 'app-plan-blueprint-library-listing',
    templateUrl: './plan-blueprint-library-listing.component.html',
    styleUrl: './plan-blueprint-library-listing.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [TextFilterComponent, MatCard, MatCardHeader, MatCardContent, NgTemplateOutlet, MatButton, RouterLink, MatIcon, TranslatePipe, DateTimeFormatPipe]
})
export class PlanBlueprintLibraryListingComponent extends BaseComponent implements OnInit{
    searchTerm: string = null;
    pageSize: number = 12;
    page: number = 1;
    count: number = 0;
    initialLoad: boolean = false;
    blueprints: PlanBlueprint[] = [];

    constructor(
        private planBlueprintService: PlanBlueprintService,
        protected enumUtils: EnumUtils,
        private storageFileService: StorageFileService,
        private sanitizer: DomSanitizer,
        private httpErrorHandlingService: HttpErrorHandlingService
    ){
        super();
    }

    ngOnInit(){
        this.getBlueprints();
    }

    getBlueprints(append: boolean = false){
        this.planBlueprintService.query(this.initializeLookup())
        .pipe(takeUntil(this._destroyed))
        .subscribe(
            {
                next: (res) => {
                    this.blueprints = append ? [...this.blueprints, ...res.items] : res.items;
                    this.count = res.count;
                    this.initialLoad = true;
                    this.downloadStorageFiles(res.items?.map((x: PlanBlueprint) => x?.definition?.iconConfiguration?.storageFile?.id)?.filter((x) => x) ?? [])
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

    protected initializeLookup(groupId?: Guid): PlanBlueprintLookup {
        const lookup = new PlanBlueprintLookup();
        lookup.metadata = {countAll: true};
        lookup.page = {offset: (this.page - 1) * this.pageSize, size: this.pageSize};
        lookup.like=this.searchTerm;
        if (groupId) { lookup.groupIds = [groupId]; }
        lookup.isActive = [IsActive.Active];
        lookup.statuses = [PlanBlueprintStatus.Finalized]
        lookup.versionStatuses = [PlanBlueprintVersionStatus.Current]
        lookup.order = {items: ['-' + nameof<PlanBlueprint>(x => x.updatedAt), '+' + nameof<PlanBlueprint>(x => x.label)]};
        lookup.project = {
            fields: this.lookupFields
        };

        return lookup;
    }

    private readonly lookupFields: string[] = [
        nameof<PlanBlueprint>(x => x.id),
        nameof<PlanBlueprint>(x => x.label),
        nameof<PlanBlueprint>(x => x.description),
        nameof<PlanBlueprint>(x => x.code),
        nameof<PlanBlueprint>(x => x.groupId),
        nameof<PlanBlueprint>(x => x.createdAt),
        nameof<PlanBlueprint>(x => x.updatedAt),
        nameof<PlanBlueprint>(x => x.usageCount),
        nameof<PlanBlueprint>(x => x.version),
        [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.matIconName)].join('.'),
        [nameof<PlanBlueprint>(x => x.definition), nameof<PlanBlueprintDefinition>(x => x.iconConfiguration), nameof<IconConfiguration>(x => x.storageFile), nameof<StorageFile>(x => x.id)].join('.'),
    ];

    loadMore(){
		this.page++;
		this.getBlueprints(true);
	}

    search(){
		this.page = 1;
		this.blueprints = [];
		this.getBlueprints();
	}
}
