import { V } from "@angular/cdk/keycodes";

export class KpiConfiguration {

  private _enabled: boolean
  get enabled(): boolean {
	  	return this._enabled;
	}

	private _address: string
  get address(): string {
     return this._address;
  }

  private _dashboards: KpiDashboard[];
  get dashboards(): KpiDashboard[] {
    return this._dashboards;
  }
  
  set dashboards(dashboards: KpiDashboard[]) {
    this._dashboards = dashboards;
  }

  public static parseValue(value: any): KpiConfiguration {
    const kpiDashboardObj: KpiConfiguration = new KpiConfiguration();

    kpiDashboardObj.dashboards = [];
    for (let dashboardValue of value.dashboards) {
      const dashboardObj: KpiDashboard = KpiDashboard.parseValue(dashboardValue);
      kpiDashboardObj.dashboards.push(dashboardObj);
    }

    kpiDashboardObj._address = value.address;
    kpiDashboardObj._enabled = value.enabled === true || value.enabled === "true";

    return kpiDashboardObj;
  }
}

export class KpiDashboard {

    private _dashboardId: string
    get dashboardId(): string {
      return this._dashboardId;
    }
    
    set dashboardId(dashboardId: string) {
      this._dashboardId = dashboardId;
    }  

    private _keywordFilter: string
    get keywordFilter(): string {
        return this._keywordFilter;
    }

    set keywordFilter(keywordFilter: string) {
      this._keywordFilter = keywordFilter;
    }    

    private _availableToRoles: string[]
    get availableToRoles(): string[] {
      return this._availableToRoles;
    }

    set availableToRoles(availableToRoles: string[]) {
      this._availableToRoles = availableToRoles;
    }    

  public static parseValue(value: any): KpiDashboard {
    const obj: KpiDashboard = new KpiDashboard();
    obj.dashboardId = value.dashboardId;
    obj.keywordFilter = value.keywordFilter;
    obj.availableToRoles = value.availableToRoles;
    return obj;
  }  
}