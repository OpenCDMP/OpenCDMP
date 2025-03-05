import { Component, OnInit, Input } from '@angular/core';
import { SafeResourceUrl, DomSanitizer } from '@angular/platform-browser';
import { LanguageService } from '@app/core/services/language/language.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { AuthService } from '@app/core/services/auth/auth.service';

@Component({
    selector: 'app-faq-content',
    templateUrl: './faq-content.component.html',
    styleUrls: ['./faq-content.component.scss'],
    standalone: false
})
export class FaqContentComponent extends BaseComponent implements OnInit {

	@Input() isDialog: boolean;

	faqHTMLUrl: SafeResourceUrl;
	sanitizedGuideUrl: any;

	constructor(
		private authService: AuthService,
		private supportiveMaterialService: SupportiveMaterialService,
		private sanitizer: DomSanitizer,
		private languageService: LanguageService,
		private analyticsService: AnalyticsService,
	) { super(); }

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.FAQ);

		this.supportiveMaterialService.getPayload(SupportiveMaterialFieldType.Faq, this.languageService.getCurrentLanguage(), this.authService.selectedTenant())
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => { //TODO HANDLE-ERRORS
				const blob = new Blob([response.body], { type: 'text/html' });
				this.faqHTMLUrl = this.sanitizer.bypassSecurityTrustResourceUrl((window.URL ? URL : webkitURL).createObjectURL(blob));
			});
	}

}
