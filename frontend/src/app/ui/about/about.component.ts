import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService, LangChangeEvent } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-about-componet',
    templateUrl: './about.component.html',
    styleUrls: ['./about.component.scss'],
    standalone: false
})
export class AboutComponent extends BaseComponent implements OnInit {

	aboutHTMLUrl: SafeResourceUrl;
	sanitizedGuideUrl: any;

	constructor(
		private authService: AuthService,
		private supportiveMaterialService: SupportiveMaterialService,
		private sanitizer: DomSanitizer,
		private languageService: LanguageService,
		private translate: TranslateService,
		private router: Router,
		private routerUtils: RouterUtilsService,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService
	) { super(); }

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.About);
		this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
			this.router.navigate([this.routerUtils.generateUrl('/about')], { skipLocationChange: true }).then(() => {
				this.getPayload();
			});
		});

		this.getPayload();
	}

	getPayload() {
		this.supportiveMaterialService.getPayload(SupportiveMaterialFieldType.About, this.languageService.getCurrentLanguage(), this.authService.selectedTenant())
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => {
				const blob = new Blob([response.body], { type: 'text/html' });
				this.aboutHTMLUrl = this.sanitizer.bypassSecurityTrustResourceUrl((window.URL ? URL : webkitURL).createObjectURL(blob));
			},
			error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

}

