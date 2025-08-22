import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanAssociatedUser, RemoveCredentialRequestPersist, User, UserMergeRequestPersist, UserPersist, UserRolePatchPersist, UserTenantUsersInviteRequest } from '@app/core/model/user/user';
import { UserLookup } from '@app/core/query/user.lookup';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';

@Injectable()
export class UserService {

	private headers = new HttpHeaders();

	constructor(private http: BaseHttpV2Service, private httpClient: HttpClient, private configurationService: ConfigurationService, private filterService: FilterService) {
	}

	private get apiBase(): string { return `${this.configurationService.server}user`; }

	query(q: UserLookup): Observable<QueryResult<User>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<User>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	queryPlanAssociated(q: UserLookup): Observable<QueryResult<PlanAssociatedUser>> {
		const url = `${this.apiBase}/plan-associated/query`;
		return this.http.post<QueryResult<PlanAssociatedUser>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<User> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<User>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getByEmail(id: Guid, reqFields: string[] = []): Observable<User> {
		const url = `${this.apiBase}/by-email/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<User>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: UserPersist): Observable<User> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<User>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	persistRoles(item: UserRolePatchPersist): Observable<User> {
		const url = `${this.apiBase}/persist/roles`;

		return this.http
			.post<User>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<User> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<User>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	exportCSV(hasTenantAdminMode: boolean): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/export/csv/${hasTenantAdminMode}`;
		let headerXml: HttpHeaders = this.headers.set('Content-Type', 'application/xml');
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		return this.httpClient.get(url, { params: params, responseType: 'blob', observe: 'response', headers: headerXml });
	}

	uploadFile(file: File, labelSent: string, reqFields: string[] = []): Observable<User> {
		const url = `${this.apiBase}/xml/import`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		const formData = new FormData();
		formData.append('file', file, labelSent);
		return this.http.post(url, formData, { params: params });
	}

	mergeAccount(item: UserMergeRequestPersist): Observable<boolean> {
		const url = `${this.apiBase}/mine/merge-account-request`;

		return this.http
			.post<boolean>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	removeCredentialAccount(item: RemoveCredentialRequestPersist): Observable<boolean> {
		const url = `${this.apiBase}/mine/remove-credential-request`;

		return this.http
			.post<boolean>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	getUserTokenPermission(token: Guid): Observable<boolean> {
		const url = `${this.apiBase}/mine/allow-merge-account/token/${token}`;

		return this.http
			.get<boolean>(url).pipe(
				catchError((error: any) => throwError(error)));
	}
	
	confirmMergeAccount(token: Guid): Observable<boolean> {
		const url = `${this.apiBase}/mine/confirm-merge-account/token/${token}`;

		return this.http
			.get<boolean>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	confirmRemoveCredentialAccount(token: Guid): Observable<boolean> {
		const url = `${this.apiBase}/mine/confirm-remove-credential/token/${token}`;

		return this.http
			.get<boolean>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	inviteUsersToTenant(item: UserTenantUsersInviteRequest): Observable<boolean> {
		const url = `${this.apiBase}/invite-users-to-tenant`;

		return this.http
			.post<boolean>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	confirmInviteUser(token: Guid): Observable<boolean> {
		const url = `${this.apiBase}/confirm-invite-user-to-tenant/token/${token}`;

		return this.http
			.get<boolean>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	//
	// Autocomplete Commons
	//
	// tslint:disable-next-line: member-ordering
	singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup()).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: User) => item.name,
		titleFn: (item: User) => item.name,
		valueAssign: (item: User) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup(null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup(searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup(null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: User) => item.name,
		titleFn: (item: User) => item.name,
		valueAssign: (item: User) => item.id,
	};

	singleAutoCompletePlanAssociatedUserConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.queryPlanAssociated(this.buildAutocompleteLookup()).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.queryPlanAssociated(this.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.queryPlanAssociated(this.buildAutocompleteLookup(null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: PlanAssociatedUser) => item.name,
		subtitleFn: (item: PlanAssociatedUser) => item.email,
		titleFn: (item: PlanAssociatedUser) => item.name,
		valueAssign: (item: PlanAssociatedUser) => item.id,
	};

	public buildAutocompleteLookup(like?: string, excludedIds?: Guid[], ids?: Guid[]): UserLookup {
		const lookup: UserLookup = new UserLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = [IsActive.Active];
		lookup.project = {
			fields: [
				nameof<User>(x => x.id),
				nameof<User>(x => x.name)
			]
		};
		lookup.order = { items: [nameof<User>(x => x.name)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}
}
