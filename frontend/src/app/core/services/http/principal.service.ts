import { Injectable, Injector, signal } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { PrincipalHttpService } from './principal.http..service';
import { catchError, map, Observable, of, takeUntil, tap } from 'rxjs';
import { Tenant } from '@app/core/model/tenant/tenant';
import { AppAccount } from '@app/core/model/auth/principal';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
    providedIn: 'root'
})
export class PrincipalService extends BaseService {

	private _loading = signal<boolean>(false);

	private _me = signal<AppAccount>(null);
	public meState = this._me.asReadonly();

	private _myTenants = signal<Tenant[]>(null);
	public myTenantsState = this._myTenants.asReadonly();

	constructor(
		private principalHttpService: PrincipalHttpService,
		private injector: Injector
	) {
		super();
	}

	private get translate(): TranslateService {
		return this.injector.get(TranslateService);
	}

	public me(options?: Object, forceReload = false): Observable<AppAccount> {
		const cached = this._me();

		if (cached && !forceReload) {
			return of(cached);
		}

		this._loading.set(true);

		return this.principalHttpService.me(options).pipe(
			takeUntil(this._destroyed),
			tap((account) => {
				this._me.set(account);
				this._loading.set(false);
			}),
			catchError(() => {
				this._loading.set(false);
				return of(null);
			})
		);
	}

	public myTenants(options?: Object, forceReload = false): Observable<Tenant[]> {
		const cached = this._myTenants();

		if (cached && !forceReload) {
			return of(cached);
		}

		this._loading.set(true);

		return this.principalHttpService.myTenants(options).pipe(
			takeUntil(this._destroyed),
			tap((tenants) => {
				this._myTenants.set(tenants);
				this._loading.set(false);
			}),
			catchError(() => {
				this._loading.set(false);
				return of([] as Tenant[]);
			})
		);
	}
	

	public myTenantsTranslated(options?: Object, forceReload = false): Observable<Tenant[]> {
		return this.myTenants(options, forceReload).pipe(
			map(tenants => this.applyTenantTranslations(tenants, true))
		);
	}

	private applyTenantTranslations(tenants: Tenant[] | null, translateNames: boolean): Tenant[] {
		if (!tenants?.length) {
			return [];
		}

		if (!translateNames) {
			return tenants;
		}

		return tenants.map(tenant => ({
			...tenant,
			name: this.getTranslatedTenantName(tenant)
		}));
	}

	public getTranslatedTenantName(tenant: Tenant): string {
		if (!tenant?.code?.length) {
			return tenant?.name;
		}

		const code = tenant.code.trim().toUpperCase();
		const key = `MY-TENANTS.CODE.${code}`;
		const translated = this.translate.instant(key);

		return translated !== key ? translated : tenant.name;
	}

	public get loading() {
		return this._loading.asReadonly();
	}
}
