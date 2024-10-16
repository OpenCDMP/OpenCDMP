import { DOCUMENT, LocationStrategy } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { PRIMARY_OUTLET, Router, UrlSegment, UrlSegmentGroup, UrlSerializer, UrlTree } from '@angular/router';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { CssColorsTenantConfiguration, TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { BaseService } from '@common/base/base.service';
import { Observable, takeUntil } from 'rxjs';
import { nameof } from 'ts-simple-nameof';
import { TenantConfigurationService } from '../tenant-configuration/tenant-configuration.service';

@Injectable()
export class TenantHandlingService extends BaseService {

	constructor(
		@Inject(DOCUMENT) private readonly document: Document,
		private readonly locationStrategy: LocationStrategy,
		private readonly router: Router,
		private urlSerializer: UrlSerializer,
		private tenantConfigurationService: TenantConfigurationService
	) {
		super();
	}

	extractTenantCodeFromUrlPath(path: string): string {
		//Searches for "/t/<tenant_code>/" in a url;
		const tenantRegex = new RegExp("\/t\/([^\/]+)");
		const regexResult = tenantRegex.exec(path);
		let tenantCode = null;
		if (Array.isArray(regexResult) && regexResult.length > 0) {
			tenantCode = regexResult[1];
		}
		return tenantCode;
	}

	getCurrentUrlEnrichedWithTenantCode(tenantCode: string, withOrigin: boolean) {
		const path = this.getUrlEnrichedWithTenantCode(this.router.routerState.snapshot.url, tenantCode)
		return withOrigin ? this.getBaseUrl() + path.toString().substring(1) : path;
	}

	getUrlEnrichedWithTenantCode(url: string, tenantCode: string): string {

		const urlTree: UrlTree = this.router.parseUrl(url);
		const urlSegmentGroup: UrlSegmentGroup = urlTree.root.children[PRIMARY_OUTLET];
		const urlSegments: UrlSegment[] = urlSegmentGroup?.segments ?? [];

		const tenantParamIndex = urlSegments?.findIndex(x => x.path == 't');
		if (tenantParamIndex >= 0) {

			if (tenantCode == 'default') {
				urlSegments.splice(tenantParamIndex, 2);
			} else {
				const tenantCodeSegment = urlSegments.at(tenantParamIndex + 1);
				tenantCodeSegment.path = tenantCode;
			}
		} else {
			if (tenantCode != 'default' && urlTree?.root?.children[PRIMARY_OUTLET]) {
				urlTree.root.children[PRIMARY_OUTLET].segments = [new UrlSegment('t', {}), new UrlSegment(tenantCode, {}), ...urlSegments];
			}
		}

		return this.urlSerializer.serialize(urlTree);
	}

	getBaseUrl(): string {
		return this.document.location.origin + this.locationStrategy.getBaseHref();
	}


	public loadAndApplyTenantCssColors() {
		this.loadTenantCssColors().pipe(takeUntil(this._destroyed)).subscribe(x => this.applyTenantCssColors(x?.cssColors))
	}

	public loadTenantCssColors(): Observable<TenantConfiguration> {
		return this.tenantConfigurationService.getCurrentTenantType(TenantConfigurationType.CssColors, [
			nameof<TenantConfiguration>(x => x.type),
			[nameof<TenantConfiguration>(x => x.cssColors), nameof<CssColorsTenantConfiguration>(x => x.primaryColor)].join('.'),
			[nameof<TenantConfiguration>(x => x.cssColors), nameof<CssColorsTenantConfiguration>(x => x.primaryColor2)].join('.'),
			[nameof<TenantConfiguration>(x => x.cssColors), nameof<CssColorsTenantConfiguration>(x => x.primaryColor3)].join('.'),
			[nameof<TenantConfiguration>(x => x.cssColors), nameof<CssColorsTenantConfiguration>(x => x.secondaryColor)].join('.'),
		]);
	}

	public applyTenantCssColors(cssColors: CssColorsTenantConfiguration) {

		if (cssColors) {
			if (cssColors.primaryColor) document.documentElement.style.setProperty(`--primary-color`, cssColors.primaryColor);
			if (cssColors.primaryColor2) document.documentElement.style.setProperty(`--primary-color-2`, cssColors.primaryColor2);
			if (cssColors.primaryColor3) document.documentElement.style.setProperty(`--primary-color-3`, cssColors.primaryColor3);
			if (cssColors.secondaryColor) document.documentElement.style.setProperty(`--secondary-color`, cssColors.secondaryColor);
		}
	}
}