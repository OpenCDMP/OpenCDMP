import { HttpBackend, HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ConfigurationService } from "./configuration/configuration.service";
import { catchError, map, Observable, throwError } from "rxjs";
import { Guid } from "@common/types/guid";
import { AuthService } from "./auth/auth.service";
import { nameof } from "ts-simple-nameof";

interface Lookup {
    ids: Guid[],
    project?: { fields: string[] }
}

interface Result {
    id?: Guid,
    name?: string
}

interface QueryResult<T> {
    items: T[];
}

@Injectable(
)
export class StatisticsService {
    constructor(
        private http: HttpClient,
        private configurationService: ConfigurationService,
    ) {
    }

    private get apiBase(): string { return `${this.configurationService.server}statistics`; }

    private query(entityType: string, q: Lookup): Observable<QueryResult<Result>> {
        const url = `${this.apiBase}/${entityType}/names`;
        let headers = new HttpHeaders();
        return this.http.post<Result[]>(url, q, { headers }).pipe(
            map(items => ({ items })),
            catchError(error => throwError(() => error))
        );
    }

    queryPlanStatusNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('plan-status', this.buildAutocompleteLookup(ids))
    }

    queryBlueprintGroupNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('blueprint-group', this.buildAutocompleteLookup(ids))

    }

    queryDescriptionStatusNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('description-status', this.buildAutocompleteLookup(ids))

    }

    queryTemplateGroupNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('template-group', this.buildAutocompleteLookup(ids))

    }

    queryReferenceTypeNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('reference-type', this.buildAutocompleteLookup(ids))

    }

    queryPlanNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('plan', this.buildAutocompleteLookup(ids))

    }

    queryReferenceNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('reference', this.buildAutocompleteLookup(ids))

    }

    queryPlanBlueprintTypeNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('plan-blueprint-type', this.buildAutocompleteLookup(ids))

    }

    queryDescriptionTemplateTypeNames(ids: Guid[]): Observable<QueryResult<Result>> {
        return this.query('description-template-type', this.buildAutocompleteLookup(ids))

    }

    public buildAutocompleteLookup(ids?: Guid[]): Lookup {
        const lookup: Lookup = { ids: [] };
        if (ids && ids.length > 0) { lookup.ids = ids; }
        lookup.project = {
            fields: [
                nameof<Result>(x=>x.id) ,
                nameof<Result>(x=>x.name) 
            ]
        }
        return lookup;
    }


}