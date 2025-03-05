import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs';

@Component({
    selector: 'app-cookies-policy',
    templateUrl: './cookies-policy.component.html',
    styleUrls: ['./cookies-policy.component.scss'],
    standalone: false
})
export class CookiesPolicyComponent extends BaseComponent implements OnInit {

	cookiePolicyHTMLUrl: SafeResourceUrl;
	sanitizedGuideUrl: any;

	constructor(
		private authService: AuthService,
		private supportiveMaterialService: SupportiveMaterialService,
		private languageService: LanguageService,
		private sanitizer: DomSanitizer,
	) { super(); }

	ngOnInit() {
		this.supportiveMaterialService.getPayload(SupportiveMaterialFieldType.CookiePolicy, this.languageService.getCurrentLanguage(), this.authService.selectedTenant())
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => { //TODO HANDLE-ERRORS
				const blob = new Blob([response.body], { type: 'text/html' });
				this.cookiePolicyHTMLUrl = this.sanitizer.bypassSecurityTrustResourceUrl((window.URL ? URL : webkitURL).createObjectURL(blob));
			});
	}

}
