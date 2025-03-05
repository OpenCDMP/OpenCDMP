import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-deposit-oauth2-dialog',
    templateUrl: './deposit-oauth2-dialog.component.html',
    styleUrls: ['./deposit-oauth2-dialog.component.scss'],
    standalone: false
})
export class DepositOauth2DialogComponent extends BaseComponent implements OnInit{

	constructor(
		private route: ActivatedRoute
	) {
		super();
	}

	error: string = null;

	ngOnInit(): void {
		this.error = null;
		this.route.queryParams.pipe(takeUntil(this._destroyed))
			.subscribe((params: Params) => {
				if (params['url']) {
					this.loadUrl(params['url'])
				} else if (params['code'])  {
					localStorage.setItem('repositoryOauthCode', params['code']);
					window.close();
				} else {
					this.error = 'Repository id required'
				}
		});
	}

	private loadUrl(url: string ) {
		window.location.href = url;
	}
}
