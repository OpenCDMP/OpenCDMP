import { DescriptionFilterService } from '../description-filter.service';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Guid } from '@common/types/guid';
import { Component, EventEmitter, Input, OnInit, Output, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { DescriptionLookup, ReferencesWithType } from '@app/core/query/description.lookup';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { DescriptionFilterComponent, DescriptionListingFilters } from '../description-filter.component';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-description-filter-dialog',
    templateUrl: './description-filter-dialog.component.html',
    styleUrls: ['./description-filter-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIconButton, MatIcon, DescriptionFilterComponent, TranslatePipe]
})

export class DescriptionFilterDialogComponent implements OnInit {
	@ViewChild(DescriptionFilterComponent, { static: true }) filter: DescriptionFilterComponent;

    @Input() isPublic: boolean;
	@Input() hasSelectedTenant: boolean;
	@Input() lookup: DescriptionLookup;
	@Input() referencesWithTypeItems: ReferencesWithType[];
	@Input() featuredReferenceTypes: ReferenceType[] = [];
	@Input() expandReferenceTypeId: Guid = null;
	/** Hosted outside its listing (the dashboard): hides the deleted-items toggle and the status field. */
	@Input() embedded = false;
    private _isOpen: boolean = false;
    get isOpen(){return this._isOpen}
    @Input() set isOpen(val: boolean){
        this._isOpen = val ?? false;
        if(this.isOpen){
            this.open();
        }
    }
    
    @Output() closed = new EventEmitter<void>();
    @Output() filtersChanged = new EventEmitter<DescriptionListingFilters>();
    
	filters: DescriptionListingFilters;

	constructor(
		private analyticsService: AnalyticsService,
	) {
	}

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.DescriptionFilterDialog);
	}

    
    public open(){
        this.filters = DescriptionFilterService.buildFilters(this.lookup, this.referencesWithTypeItems)
    }
}
