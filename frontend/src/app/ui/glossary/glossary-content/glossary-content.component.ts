import { Component, OnInit, Input } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { BaseComponent } from '@common/base/base.component';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';

@Component({
	selector: 'app-glossary-content',
	templateUrl: './glossary-content.component.html',
	styleUrls: ['./glossary-content.component.scss']
})
export class GlossaryContentComponent extends BaseComponent implements OnInit {

	@Input() isDialog: boolean;

	glossaryHTMLUrl: SafeResourceUrl;
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
	) { super(); }

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.Glossary);

		this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
			this.router.navigate([this.routerUtils.generateUrl('/glossary')], { skipLocationChange: true }).then(() => {
				this.getPayload();
			});
		});

		this.getPayload();
	}

	getPayload() {
		this.supportiveMaterialService.getPayload(SupportiveMaterialFieldType.Glossary, this.languageService.getCurrentLanguage(), this.authService.selectedTenant())
		.pipe(takeUntil(this._destroyed))
		.subscribe(response => { //TODO HANDLE-ERRORS
			const blob = new Blob([response.body], { type: 'text/html' });
			this.glossaryHTMLUrl = this.sanitizer.bypassSecurityTrustResourceUrl((window.URL ? URL : webkitURL).createObjectURL(blob));
		});
	}

}
