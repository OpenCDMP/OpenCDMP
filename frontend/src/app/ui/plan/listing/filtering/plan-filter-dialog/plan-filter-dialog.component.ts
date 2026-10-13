import { PlanFilterService } from '../services/plan-filter.service';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Guid } from '@common/types/guid';
import { Component, EventEmitter, Input, OnInit, Output, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { ReferencesWithType } from '@app/core/query/description.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { PlanFilterComponent, PlanListingFilters } from '../plan-filter.component';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-filter-dialog',
    templateUrl: './plan-filter-dialog.component.html',
    styleUrls: ['./plan-filter-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatIconButton, MatIcon, PlanFilterComponent, TranslatePipe]
})

export class PlanFilterDialogComponent implements OnInit {
	@ViewChild(PlanFilterComponent, { static: true }) filter: PlanFilterComponent;

    @Input() isPublic: boolean;
	@Input() hasSelectedTenant: boolean;
	@Input() lookup: PlanLookup;
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
    @Output() filtersChanged = new EventEmitter<PlanListingFilters>();
    
	filters: PlanListingFilters;
	constructor(
		private analyticsService: AnalyticsService
    ) {
		
	}

    public open(){
        this.filters = PlanFilterService.buildFilters(this.lookup, this.referencesWithTypeItems)
    }

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.PlanFilterDialog);
	}
}
