import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { LanguageService } from '@app/core/services/language/language.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { BaseComponent } from '@common/base/base.component';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';

@Component({
	selector: 'app-terms',
	templateUrl: './terms.component.html',
	styleUrls: ['./terms.component.scss']
})
export class TermsComponent extends BaseComponent implements OnInit {

	termsHTMLUrl: SafeResourceUrl;
	sanitizedGuideUrl: any;

	constructor(
		private supportiveMaterialService: SupportiveMaterialService,
		private sanitizer: DomSanitizer,
		private languageService: LanguageService,
		private translate: TranslateService,
		private router: Router,
		private routerUtils: RouterUtilsService,
		private analyticsService: AnalyticsService,
	) { super(); }

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.Terms);

		this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
			this.router.navigate(['/reload'], { skipLocationChange: true }).then(() => this.router.navigate([this.routerUtils.generateUrl('/terms-and-conditions')]));
		});

		this.supportiveMaterialService.getPayload(SupportiveMaterialFieldType.TermsOfService, this.languageService.getCurrentLanguage())
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => { //TODO HANDLE-ERRORS
				const blob = new Blob([response.body], { type: 'text/html' });
				this.termsHTMLUrl = this.sanitizer.bypassSecurityTrustResourceUrl((window.URL ? URL : webkitURL).createObjectURL(blob));
			});
	}

}
