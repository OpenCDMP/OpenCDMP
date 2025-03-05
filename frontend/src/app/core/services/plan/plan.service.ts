import { HttpClient, HttpHeaders, HttpParamsOptions, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlanStatusEnum } from '@app/core/common/enum/plan-status';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { PlanDescriptionTemplateLookup } from '@app/core/query/plan-description-template.lookup';
import { PlanLookup } from '@app/core/query/plan.lookup';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { QueryResult } from '@common/model/query-result';
import { FilterService } from '@common/modules/text-filter/filter-service';
import { Guid } from '@common/types/guid';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { BaseHttpParams } from '../../../../common/http/base-http-params';
import { InterceptorType } from '../../../../common/http/interceptors/interceptor-type';
import { ClonePlanPersist, Plan, PlanPersist, PlanUser, PlanUserInvitePersist, PlanUserPersist, PlanUserRemovePersist, NewVersionPlanPersist, PublicPlan, PlanInvitationResult } from '../../model/plan/plan';
import { AuthService } from '../auth/auth.service';
import { ConfigurationService } from '../configuration/configuration.service';
import { BaseHttpV2Service } from '../http/base-http-v2.service';
import { PlanValidationResult } from '@app/ui/plan/plan-finalize-dialog/plan-finalize-dialog.component';
import { PlanCommonModelConfig, PreprocessingPlanModel } from '@app/core/model/plan/plan-import';

@Injectable()
export class PlanService {

	private headers = new HttpHeaders();

	constructor(
		private http: BaseHttpV2Service,
		private httpClient: HttpClient,
		private configurationService: ConfigurationService,
		private filterService: FilterService,
		private authService: AuthService
	) {
	}

	private get apiBase(): string { return `${this.configurationService.server}plan`; }

	query(q: PlanLookup): Observable<QueryResult<Plan>> {
		const url = `${this.apiBase}/query`;
		return this.http.post<QueryResult<Plan>>(url, q).pipe(catchError((error: any) => throwError(error)));
	}

	publicQuery(q: PlanLookup): Observable<QueryResult<PublicPlan>> {
		const url = `${this.apiBase}/public/query`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken,
				InterceptorType.TenantHeaderInterceptor]
		};
		return this.http.post<QueryResult<PublicPlan>>(url, q, {params: params}).pipe(catchError((error: any) => throwError(error)));
	}

	getSingle(id: Guid, reqFields: string[] = []): Observable<Plan> {
		const url = `${this.apiBase}/${id}`;
		const options = { params: { f: reqFields } };

		return this.http
			.get<Plan>(url, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	getPublicSingle(id: Guid, reqFields: string[] = []): Observable<PublicPlan> {
		const url = `${this.apiBase}/public/${id}`;

		const options: HttpParamsOptions = { fromObject: { f: reqFields } };

		let params: BaseHttpParams = new BaseHttpParams(options);
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.AuthToken,
				InterceptorType.TenantHeaderInterceptor]
		};

		return this.http
			.get<PublicPlan>(url, { params: params }).pipe(
				catchError((error: any) => throwError(error)));
	}

	persist(item: PlanPersist): Observable<Plan> {
		const url = `${this.apiBase}/persist`;

		return this.http
			.post<Plan>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	delete(id: Guid): Observable<Plan> {
		const url = `${this.apiBase}/${id}`;

		return this.http
			.delete<Plan>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	setStatus(id: Guid, newStatusId: Guid, descriptionIds: Guid[] = []): Observable<Boolean> {
		const url = `${this.apiBase}/set-status/${id}/${newStatusId}`;

		return this.http
			.post<Boolean>(url, {descriptionIds: descriptionIds}).pipe(
				catchError((error: any) => throwError(error)));
	}

	validate(id: Guid): Observable<PlanValidationResult> {
		const url = `${this.apiBase}/validate/${id}`;

		return this.http
			.get<PlanValidationResult>(url).pipe(
				catchError((error: any) => throwError(error)));
	}

	clone(item: ClonePlanPersist, reqFields: string[] = []): Observable<Plan> {
		const url = `${this.apiBase}/clone`;
		const options = { params: { f: reqFields } };

		return this.http
			.post<Plan>(url, item, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	publicClone(item: ClonePlanPersist, reqFields: string[] = []): Observable<Plan> {
		const url = `${this.apiBase}/public-clone`;
		const options = { params: { f: reqFields } };

		return this.http
			.post<Plan>(url, item, options).pipe(
				catchError((error: any) => throwError(error)));
	}

	newVersion(item: NewVersionPlanPersist, reqFields: string[] = []): Observable<Plan> {
		const url = `${this.apiBase}/new-version`;
		const options = { params: { f: reqFields } };

		return this.http
			.post<Plan>(url, item, options ).pipe(
				catchError((error: any) => throwError(error)));
	}

	assignUsers(id: Guid, items: PlanUserPersist[], reqFields: string[] = []): Observable<PlanUser> {
		const url = `${this.apiBase}/${id}/assign-users`;
		const options = { params: { f: reqFields } };

		return this.http
			.post<PlanUser>(url, items).pipe(
				catchError((error: any) => throwError(error)));
	}

	removeUser(item: PlanUserRemovePersist, reqFields: string[] = []): Observable<Plan> {
		const url = `${this.apiBase}/remove-user`;

		return this.http
			.post<Plan>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	inviteUsers(PlanId: Guid, item: PlanUserInvitePersist): Observable<any> {
		const url = `${this.apiBase}/${PlanId}/invite-users`;

		return this.http
			.post<any>(url, item).pipe(
				catchError((error: any) => throwError(error)));
	}

	acceptInvitation(token: string): Observable<PlanInvitationResult> {
		const url = `${this.apiBase}/token/${token}/invite-accept`;

		return this.http.get<PlanInvitationResult>(url).pipe(catchError((error: any) => throwError(error)));
	}

	downloadXML(id: Guid): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/xml/export/${id}`;
		let headerXml: HttpHeaders = this.headers.set('Content-Type', 'application/xml');
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		return this.httpClient.get(url, { params: params, responseType: 'blob', observe: 'response', headers: headerXml });
	}

	downloadPublicXML(id: Guid): Observable<HttpResponse<Blob>> {
		const url = `${this.apiBase}/xml/export-public/${id}`;
		let headerXml: HttpHeaders = this.headers.set('Content-Type', 'application/xml');
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		return this.httpClient.get(url, { params: params, responseType: 'blob', observe: 'response', headers: headerXml });
	}

	uploadXml(file: File, label: string, reqFields: string[] = []): Observable<Plan> {
		const url = `${this.apiBase}/xml/import`;
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		const formData = new FormData();
		formData.append('file', file);
		formData.append('label', label);
		if (reqFields.length > 0){
			for (var i = 0; i < reqFields.length; i++) {
				formData.append('field[]', reqFields[i]);
			}
		}
		
		return this.http.post(url, formData, { params: params }).pipe(
			catchError((error: any) => throwError(error)));;
	}

	preprocessingPlan(fileId: Guid, repositoryId: string): Observable<PreprocessingPlanModel> {
		const url = `${this.apiBase}/json/preprocessing`;
		const params = new BaseHttpParams();

		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.JSONContentType]
		};
		const formData = new FormData();
		formData.append('fileId', fileId.toString());
		formData.append('repositoryId', repositoryId);
		
		return this.http.post<PreprocessingPlanModel>(url, formData, { params: params }).pipe(catchError((error: any) => throwError(error)));
	}

	uploadJson(item: PlanCommonModelConfig, reqFields: string[] = []): Observable<Plan> {
		const url = `${this.apiBase}/json/import`;

		return this.http.post<Plan>(url, item).pipe(catchError((error: any) => throwError(error)));

	}

	//
	// Autocomplete Commons
	//
	// tslint:disable-next-line: member-ordering
	singleAutocompleteConfiguration: SingleAutoCompleteConfiguration = {
		initialItems: (data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active])).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], searchQuery)).pipe(map(x => x.items)),
		getSelectedItem: (selectedItem: any) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, [selectedItem])).pipe(map(x => x.items[0])),
		displayFn: (item: Plan) => item.label,
		titleFn: (item: Plan) => item.label,
		valueAssign: (item: Plan) => item.id,
	};

	// tslint:disable-next-line: member-ordering
	multipleAutocompleteConfiguration: MultipleAutoCompleteConfiguration = {
		initialItems: (excludedItems: any[], data?: any) => this.query(this.buildAutocompleteLookup([IsActive.Active], null, excludedItems ? excludedItems : null)).pipe(map(x => x.items)),
		filterFn: (searchQuery: string, excludedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active],searchQuery, excludedItems)).pipe(map(x => x.items)),
		getSelectedItems: (selectedItems: any[]) => this.query(this.buildAutocompleteLookup([IsActive.Active, IsActive.Inactive], null, null, selectedItems)).pipe(map(x => x.items)),
		displayFn: (item: Plan) => item.label,
		titleFn: (item: Plan) => item.label,
		valueAssign: (item: Plan) => item.id,
	};

	public buildAutocompleteLookup(isActive: IsActive[], like?: string, excludedIds?: Guid[], ids?: Guid[], statusIds?: Guid[], planDescriptionTemplateSubQuery?: PlanDescriptionTemplateLookup): PlanLookup {
		const lookup: PlanLookup = new PlanLookup();
		lookup.page = { size: 100, offset: 0 };
		if (excludedIds && excludedIds.length > 0) { lookup.excludedIds = excludedIds; }
		if (ids && ids.length > 0) { lookup.ids = ids; }
		lookup.isActive = isActive;
		lookup.statusIds = statusIds;
		lookup.project = {
			fields: [
				nameof<Plan>(x => x.id),
				nameof<Plan>(x => x.label)
			]
		};
		if (planDescriptionTemplateSubQuery != null) lookup.planDescriptionTemplateSubQuery = planDescriptionTemplateSubQuery;
		lookup.order = { items: [nameof<Plan>(x => x.label)] };
		if (like) { lookup.like = this.filterService.transformLike(like); }
		return lookup;
	}

	//
	//
	// UI Helpers
	//
	//

	getCurrentUserRolesInPlan(planUsers: PlanUser[], isDeletedPlan: boolean = false): PlanUserRole[] {
		const principalId: Guid = this.authService.userId();
		let planUserRoles: PlanUserRole[] = null;
		if (principalId) {
			if (isDeletedPlan) {
				planUserRoles = planUsers.filter(element => element?.user?.id === principalId).map(x => x.role);
			} else {
				planUserRoles = planUsers.filter(element => element.isActive == IsActive.Active && element?.user?.id === principalId).map(x => x.role);
			}
		}
		return planUserRoles;
	}
}
