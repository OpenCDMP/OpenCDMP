import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class BreadcrumbService { //todo maybe some memory management

	private paramToStringDictionary: Record<string, string> = {};
	private excludedParamsDictionary: Record<string, boolean> = {};
	private pathOverrideMap = new Map<string, string>();

	private resolvedValues$ = new BehaviorSubject<Record<string, string>>({ ...this.paramToStringDictionary })
	private excludedValues$ = new BehaviorSubject<Record<string, boolean>>({ ...this.excludedParamsDictionary })


	public addIdResolvedValue(param: string, value: string) {
		if (!param) {
			return;
		}
		if (this.paramToStringDictionary[param] === value) { // value already in dictionary
			return;
		}

		this.paramToStringDictionary[param] = value;
		// sync excludedParamsDictionary if needed
		if (this.excludedParamsDictionary[param])
		{
			delete this.excludedParamsDictionary[param];
			this.excludedValues$.next({ ...this.excludedParamsDictionary })
		}

		this.resolvedValues$.next({ ...this.paramToStringDictionary })
	}
	
	public addExcludedParam(param: string, value: boolean) {
		if (!param) {
			return;
		}
		if (this.excludedParamsDictionary[param] === value) { // value already in dictionary
			return;
		}

		this.excludedParamsDictionary[param] = value;
		// sync paramToStringDictionary if needed
		if (this.paramToStringDictionary[param])
		{
			delete this.paramToStringDictionary[param];
			this.resolvedValues$.next({ ...this.paramToStringDictionary });
		}

		this.excludedValues$.next({ ...this.excludedParamsDictionary })
	}

	public resolvedValues(): Observable<Record<string, string>> {
		return this.resolvedValues$.asObservable();
	}
	
	public excludedValues(): Observable<Record<string, boolean>> {
		return this.excludedValues$.asObservable();
	}

	public addPathOverride(pathOverride: PathOverride): void {
		if (!pathOverride) {
			return;
		}

		this.pathOverrideMap.set(pathOverride.target, pathOverride.redirectTo);
	}

	public getPathOverrideFor(pathString: string): string {
		return this.pathOverrideMap.get(pathString);
	}

	public emptyPathOverrides(): void {
		this.pathOverrideMap.clear();
	}




	public static readonly ROUTE_DATA_KEY: string = 'breadcrumb'; // TODO USE IT ELSEWHERE

	// For the moment only to make data params typed
	public static generateRouteDataConfiguration(params: BreadCrumbRouteData): Record<string, BreadCrumbRouteData> {
		return {
			[BreadcrumbService.ROUTE_DATA_KEY]: params
		};
	}
}


export interface BreadCrumbRouteData {
	title?: string;
	skipNavigation?: boolean;
	hideNavigationItem?: boolean;
	titleFactory?: (resolutions: BreadcrumbTitlePathResolutions) => { languageKey: string, translateParams?: Record<string, string> }
}



interface BreadcrumbTitlePathResolutions {
	/**
	 * Resolved path params
	 * 
	 * for example: somePath/:id => somepath/<guid>
	 */
	pathResolutions?: Record<string, string>;
	/**
	 * Resolved values that we have registered into breadcrumb service
	 * 
	 * for example: <guid \ languagkey variable> => John Doe
	 */
	valueResolutions?: Record<string, string>;
}


export interface PathOverride {
	target: string;
	redirectTo: string;
}