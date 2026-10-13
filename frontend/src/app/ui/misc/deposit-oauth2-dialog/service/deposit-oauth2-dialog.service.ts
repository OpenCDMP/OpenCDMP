import { Injectable } from '@angular/core';
import { BaseService } from '@common/base/base.service';
import { BehaviorSubject, Observable, interval } from 'rxjs';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { takeUntil } from 'rxjs/operators';
import { DepositConfiguration } from '@app/core/model/deposit/deposit-configuration';

@Injectable()
export class DepositOauth2DialogService extends BaseService{



	constructor(private configurationService: ConfigurationService) {
		super();
	}

	public getLoginUrl(repo: DepositConfiguration) {
		// plugins built before they declared their scope expect the one this app always asked for
		const scope = (repo.oauthScope ?? 'deposit:write deposit:actions user:email').trim().split(/\s+/).filter(x => !!x).join('+');
		return repo.repositoryAuthorizationUrl + '?client_id=' + repo.repositoryClientId
			+ '&response_type=code' + (scope ? '&scope=' + scope : '') + '&redirect_uri='
			+ repo.redirectUri
	}

	public login(url: string): Observable<any> {
		const code: BehaviorSubject<any> = new BehaviorSubject(undefined);
		const windows = window.open(this.configurationService.app + 'deposit/oauth2?url=' + encodeURIComponent(url) ,'', `height=500px,width=500px,top=${(window.screen.height / 2) - 200}px,left=${(window.screen.width / 2) - 200}px`);
		const sub = interval(300).pipe(takeUntil(this._destroyed)).subscribe(() => {
			if (windows.closed) {
				let oauthCode;
				if (localStorage.getItem('repositoryOauthCode')) {
					oauthCode = localStorage.getItem('repositoryOauthCode');
					localStorage.removeItem('repositoryOauthCode');
				}
				code.next(oauthCode);
				sub.unsubscribe();
			}
		});
		return code.asObservable();
	}

}
