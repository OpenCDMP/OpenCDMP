import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { AppPermission } from '@app/core/common/enum/permission.enum';
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
    indicatorQueryParams: IndicatorQueryParams[] = [];
    selectedDashboardId: string;
    disableDropDown:boolean = false;

    selectedDashboard = (selectedDashboardId: string): IndicatorQueryParams => {
        if(!this.indicatorQueryParams?.length || !selectedDashboardId){ return; }
        return this.indicatorQueryParams.find(x => x.dashboard == selectedDashboardId);
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
        if(!this.configurationService?.kpi?.enabled && this.configurationService?.kpi?.dashboards?.length == 0) return;       
        
        this.configurationService.kpi.dashboards.forEach(dashboard => {

            if (this.authService.hasPermission(AppPermission.ViewIndicatorDashboardPage) && dashboard?.availableToRoles?.find(x => this.authService.getSelectedRoles()?.includes(x))) {
                this.indicatorQueryParams.push({
                    dashboard: dashboard?.dashboardId,
                    displayName: undefined as any,
                    keywordFilters: dashboard?.keywordFilter?.length > 0 ? [{field: dashboard.keywordFilter, values: [this.authService.selectedTenant() ? this.authService.selectedTenant(): 'default']}] : undefined,
                    groupHash: null as any,
                }) 
            }
        });
        if(this.indicatorQueryParams?.length){
            if (this.indicatorQueryParams?.length == 1) this.disableDropDown = true;
            this.selectedDashboardId = this.indicatorQueryParams[0].dashboard;
        }

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