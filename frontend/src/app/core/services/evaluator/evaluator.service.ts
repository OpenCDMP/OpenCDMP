import { Component, EventEmitter, Input, OnInit, Output, Injectable, effect, signal } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { catchError, map, takeUntil } from 'rxjs/operators';
import { EvaluatorHttpService } from './evaluator.http.service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { AuthService } from '../auth/auth.service';
import { EvaluatorEntityType } from '@app/core/common/enum/evaluator-entity-type';
import { EvaluatorConfiguration } from '@app/core/model/evaluator/evaluator-configuration';
import { TranslateService } from '@ngx-translate/core';
import { Guid } from '@common/types/guid';
import {
	SnackBarNotificationLevel,
	UiNotificationService
} from '@app/core/services/notification/ui-notification-service';
import { Observable, throwError } from 'rxjs';
import { tap, share } from 'rxjs/operators';
import { RankResultModel } from '@app/core/model/evaluator/evaluator-plan-model.model';
import { HttpResponse } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class EvaluatorService extends BaseService {

   constructor(
    private evaluatorHttpService: EvaluatorHttpService,
    private authentication: AuthService,
    private httpErrorHandlingService: HttpErrorHandlingService,
	private language: TranslateService,
	private uiNotificationService: UiNotificationService,
   ) { 
        super();
        effect(() => {
            const authenticationComplete = this.authentication.currentAccountIsAuthenticated();
            const loading = this._loading();
            if(authenticationComplete && !loading && !this._initialized){
                this.init();
            }
        })
   }

   private _initialized: boolean = false;
   private _loading = signal<boolean>(false);

   private _availableEvaluators= signal<EvaluatorConfiguration[]>(null);

   public availableEvaluators = this._availableEvaluators.asReadonly();

   public availableEvaluatorsFor(entityType: EvaluatorEntityType) {
	// Filter evaluators by entity type
	// The fetch logo config should be here.
	if (this.availableEvaluators) {
	  const filteredEvaluators = this.availableEvaluators()?.filter(x => {
		return x.evaluatorEntityTypes && x.evaluatorEntityTypes.includes(entityType);
	  });
	  
	  return filteredEvaluators;
	}
	
	return [];
  }

   init() {
	this._loading.set(true);
	this.evaluatorHttpService.getAvailableConfigurations()
	  .pipe(takeUntil(this._destroyed), catchError((error) => {
		this._loading.set(false);
		this._initialized = true;
		this.httpErrorHandlingService.handleBackedRequestError(error);
		return [];
	  }))
	  .subscribe(items => {
		this._availableEvaluators.set(items);
		this._loading.set(false);
		this._initialized = true;
	  });
    }
	
	rankPlan(id: Guid, evaluatorId: string, format: string, benchmarkIds: string[] = [], isPublic: boolean = false): Observable<RankResultModel> {
		this._loading.set(true);
		
		return this.evaluatorHttpService.rankPlan(id, evaluatorId, format, benchmarkIds).pipe(
            map((response) => response.body),
            tap({
                next: (doi) => {
                    this.onCallbackSuccess();
                },
                error: (error) => {
                this.onCallbackError(error);
                // Ensure loading state is turned off in case of error
                this._loading.set(false);
                },
                complete: () => {
                this._loading.set(false);
                }
            }),
            catchError((error) => {
                // Ensure loading state is turned off in case of error
                this._loading.set(false);
                return throwError(error);
            }),
            share()
		);
	  }

	  rankDescription(id: Guid, evaluatorId: string, format: string, benchmarkIds: string[] = [], isPublic: boolean = false): Observable<RankResultModel> {
		this._loading.set(true);
		return this.evaluatorHttpService.rankDescription(id, evaluatorId, format, benchmarkIds)
		  .pipe(
			takeUntil(this._destroyed),
            map((response) => response.body),
			tap(response => {
			  this._loading.set(false);
			  this.onCallbackSuccess();
			}),
			catchError(error => {
			  this._loading.set(false);
			  this.onCallbackError(error);
			  return throwError(error);
			})
		  );
	}

	getLogo(evaluatorId: string): Observable<HttpResponse<string>> {
		return this.evaluatorHttpService.getLogo(evaluatorId).pipe(
			catchError((error) => {
				this.httpErrorHandlingService.handleBackedRequestError(error);
				return throwError(error);
			})
		);
	}
	  
	onCallbackSuccess(): void {
		this.uiNotificationService.snackBarNotification(this.language.instant('PLAN-EDITOR.SNACK-BAR.SUCCESSFUL-EVALUATION'), SnackBarNotificationLevel.Success);
	}

	onCallbackError(error) {
		this.uiNotificationService.snackBarNotification(error.error.message ? error.error.message : this.language.instant('PLAN-EDITOR.SNACK-BAR.UNSUCCESSFUL-EVALUATION'), SnackBarNotificationLevel.Error);
	}

}