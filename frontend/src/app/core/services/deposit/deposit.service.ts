import { HttpResponse } from "@angular/common/http";
import { effect, Injectable, signal } from "@angular/core";
import { BaseService } from "@common/base/base.service";
import { AuthService } from "../auth/auth.service";
import { DepositConfiguration } from "@app/core/model/deposit/deposit-configuration";
import { DepositHttpService } from "./deposit.http.service";
import { Observable, throwError } from 'rxjs';
import { tap, catchError, takeUntil } from 'rxjs/operators';
import { HttpErrorHandlingService } from "@common/modules/errors/error-handling/http-error-handling.service";
import { DepositAuthenticateRequest, DepositRequest } from "@app/core/model/deposit/deposit-request";
import { SnackBarNotificationLevel, UiNotificationService } from "../notification/ui-notification-service";
import { TranslateService } from "@ngx-translate/core";
import { EntityDoi } from "@app/core/model/entity-doi/entity-doi";
import { nameof } from "ts-simple-nameof";
import { DepositAuthMethodResult } from "@app/core/model/deposit/deposit-auth-method-result";


@Injectable({
    providedIn: 'root'
})
export class DepositService extends BaseService {

   constructor(
    private depositHttpService: DepositHttpService,
    private authentication: AuthService,
    private httpErrorHandlingService: HttpErrorHandlingService,
    private uiNotificationService: UiNotificationService,
    private language: TranslateService,

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

   private _availableDeposits = signal<DepositConfiguration[]>(null);

   public availableDeposits = this._availableDeposits.asReadonly();

    init() {
        this._loading.set(true);
        this.depositHttpService.getAvailableRepos([
                nameof<DepositConfiguration>(x => x.depositType),
                nameof<DepositConfiguration>(x => x.repositoryId),
                nameof<DepositConfiguration>(x => x.repositoryAuthorizationUrl),
                nameof<DepositConfiguration>(x => x.repositoryRecordUrl),
                nameof<DepositConfiguration>(x => x.repositoryClientId),
                nameof<DepositConfiguration>(x => x.hasLogo),
                nameof<DepositConfiguration>(x => x.redirectUri),
                nameof<DepositConfiguration>(x => x.configurationFields),
                nameof<DepositConfiguration>(x => x.userConfigurationFields),
                nameof<DepositConfiguration>(x => x.pluginType)
              ])
            .pipe(takeUntil(this._destroyed), catchError((error) => {
                this._loading.set(false);
                this._initialized = true;
                this.httpErrorHandlingService.handleBackedRequestError(error);
                return [];
        }))
        .subscribe(items => {
            this._availableDeposits.set(items);
            this._loading.set(false);
            this._initialized = true;
        });
    }

    
    getRepository(repositoryId: string, reqFields: string[] = []): Observable<DepositConfiguration> {
        this._loading.set(true);
        return this.depositHttpService.getRepository(repositoryId, reqFields)
          .pipe(
            takeUntil(this._destroyed),
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

    getAccessToken(item: DepositAuthenticateRequest): Observable<string> {
            this._loading.set(true);
            return this.depositHttpService.getAccessToken(item)
              .pipe(
                takeUntil(this._destroyed),
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

    deposit(item: DepositRequest): Observable<EntityDoi> {
        return this.depositHttpService.deposit(item).pipe(
            catchError((error) => {
                this.httpErrorHandlingService.handleBackedRequestError(error);
                return throwError(error);
            })
        );
    }        

    getLogo(repositoryId: string): Observable<HttpResponse<string>> {
        return this.depositHttpService.getLogo(repositoryId).pipe(
            catchError((error) => {
                this.httpErrorHandlingService.handleBackedRequestError(error);
                return throwError(error);
            })
        );
    }

    getAvailableAuthMethods(repositoryId: string): Observable<DepositAuthMethodResult> {
      return this.depositHttpService.getAvailableAuthMethods(repositoryId).pipe(
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