import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { IndicatorQueryParams } from '@citesa/kpi-client/types';
import { BaseComponent } from '@common/base/base.component';
import { TranslateService } from '@ngx-translate/core';


@Component({
    selector: 'app-indicator-dashboard',
    templateUrl: './indicator-dashboard.component.html',
    styleUrls: ['./indicator-dashboard.component.scss'],
    standalone: false
})
export class IndicatorDashboardComponent extends BaseComponent implements OnInit {
    viewTypeEnum = ViewTypeEnum;
    viewType: ViewTypeEnum = ViewTypeEnum.List;
	indicatorQueryParams: IndicatorQueryParams = {
	  dashboard: this.configurationService.kpiDashboardId,
	  displayName: undefined as any,
	  keywordFilters: [{field: this.configurationService.keywordFilter, values: [this.authService.selectedTenant() ? this.authService.selectedTenant(): 'default']}],
	  groupHash: null as any,
	}

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		public configurationService: ConfigurationService,
		private authService: AuthService
	) {
		super();
	}

	ngOnInit(): void {
	}

    viewChange(event: MatButtonToggleChange){
        const elements = document.getElementsByClassName('indicators-list');
        if(event?.value === ViewTypeEnum.Grid){
            for(let i = 0; i < elements.length; i++){
                elements.item(i).classList.add('grid-view');
            }
        }else {
            for(let i = 0; i < elements.length; i++){
                elements.item(i).classList.remove('grid-view');
            }
        }
    }

}

enum ViewTypeEnum {
    'List',
    'Grid'
}