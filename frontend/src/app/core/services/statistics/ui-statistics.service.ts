import { Injectable, signal } from '@angular/core';
import { DashboardService } from '../dashboard/dashboard.service';
import { AuthService } from '../auth/auth.service';
import { catchError, forkJoin, of, takeUntil, timer } from 'rxjs';
import { DashboardReferenceTypeStatistics, DashboardStatistics } from '@app/core/model/dashboard/dashboard-statistics';
import { BaseService } from '@common/base/base.service';

@Injectable({
  providedIn: 'root',
})
export class UIStatisticsService extends BaseService{
    private _dashboardStatistics = signal<MergedDashboardStatistics>(null);
    public dashboardStatistics = this._dashboardStatistics.asReadonly();
    readonly STATISTICS_INTERVAL = 120000;
    constructor(
        private dashboardService: DashboardService,
        private authService: AuthService
    ){
        super();
        timer(2000, this.STATISTICS_INTERVAL)
        .pipe(takeUntil(this._destroyed))
        .subscribe(x => {
            this.getStatistics();
        });
        // this.authService.authenticationStateSubject.pipe(
        //     map(x => x.loginStatus),
        // ).subscribe((loginStatus) => {
        //     this.getStatistics();
        // })
    }

    getStatistics(){
        forkJoin([
            this.dashboardService.getPublicDashboardStatistics().pipe(catchError((err) => of(null))),
            (this.authService.currentAccountIsAuthenticated() ? this.dashboardService.getMyDashboardStatistics() : of(null)).pipe(catchError((err) => of(null)))
        ]).pipe(takeUntil(this._destroyed))
        .subscribe(([publicStatistics, privateStatistics]) => {
            if(!publicStatistics && !privateStatistics){ 
                this._dashboardStatistics.set(null);
                return;
            }
            this._dashboardStatistics.set({
                ...privateStatistics,
                publicDescriptionCount: publicStatistics?.descriptionCount,
                publicPlanCount: publicStatistics?.planCount,
                publicReferenceTypeStatistics: publicStatistics?.referenceTypeStatistics,
            })
        })
    }
}

export interface MergedDashboardStatistics extends DashboardStatistics {
    publicPlanCount: number;
    publicDescriptionCount: number;
    publicReferenceTypeStatistics: DashboardReferenceTypeStatistics[];
}