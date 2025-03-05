import { HttpClient } from '@angular/common/http';
import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { RouterUtilsService } from '@app/core/services/router/router-utils.service';
import { SupportiveMaterialService } from '@app/core/services/supportive-material/supportive-material.service';
import { BaseComponent } from '@common/base/base.component';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subject, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'app-user-guide-content',
    templateUrl: './user-guide-content.component.html',
    styleUrls: ['./user-guide-content.component.scss'],
    standalone: false
})
export class UserGuideContentComponent extends BaseComponent implements OnInit {
    @Input() isDialog: boolean;
    
	readonly useInnerHTML: boolean = false; //GK: Change for TESTING PURPOSES ONLY

	guideHTML: any;
	guideHTMLUrl: SafeResourceUrl;
	sanitizedGuideUrl: any;
	private scrollEvent: EventListener;
	private tocScrollEvent: EventListener;
	private _transformed: Subject<boolean> = new Subject();
	private _parsed: Subject<boolean> = new Subject();

	@ViewChild('guide') guide: ElementRef;

	constructor(
		private authService: AuthService,
		private supportiveMaterialService: SupportiveMaterialService,
		private sanitizer: DomSanitizer,
		private languageService: LanguageService,
		private httpClient: HttpClient,
		private configurationService: ConfigurationService,
		private translate: TranslateService,
		private router: Router,
		private routerUtils: RouterUtilsService,
		private analyticsService: AnalyticsService,
	) { super(); }

	ngOnInit() {
		this.analyticsService.trackPageView(AnalyticsService.UserGuideContent);

		this.scrollEvent = ((ev) => this.scroll(ev));
		this.tocScrollEvent = ((ev) => {
			this.activeToc(ev);
			this.scroll(ev);
		});

		this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
			this.router.navigate([this.routerUtils.generateUrl('/user-guide')], { skipLocationChange: true }).then(() => {
				this.getPayload();
			});
		});
		
		this.getPayload();
	}

	getPayload() {
		this.supportiveMaterialService.getPayload(SupportiveMaterialFieldType.UserGuide, this.languageService.getCurrentLanguage(), this.authService.selectedTenant())
			.pipe(takeUntil(this._destroyed))
			.subscribe(response => { //TODO HANDLE-ERRORS
				const blob = new Blob([response.body], { type: 'text/html' });
				if (this.useInnerHTML) {
					this.readBlob(blob);
				} else {
					this.guideHTMLUrl = this.sanitizer.bypassSecurityTrustResourceUrl((window.URL ? URL : webkitURL).createObjectURL(blob));
				}
			});
	}

	readBlob(blob: Blob) {
		const fr = new FileReader();
		fr.onload = ev => {
			const HTMLstring = fr.result as string;
			this.guideHTML = this.sanitizer.bypassSecurityTrustHtml(HTMLstring);
			this.addScripts(HTMLstring);
			if (this.guideHTML) {
				interval(1000).pipe(takeUntil(this._transformed)).subscribe(() => this.transform());
				interval(1000).pipe(takeUntil(this._parsed)).subscribe(() => this.parse());
			}
		};
		fr.readAsText(blob);
	}

	activeToc(ev: Event) {
		const current = document.getElementsByClassName('active');
		if (current.length > 0) {
			current[0].classList.remove('active');
		}
		(ev.currentTarget as Element).classList.add('active');
	}

	scroll(ev: Event) {
		document.getElementById((ev.currentTarget as Element).getAttribute('path')).scrollIntoView({ behavior: 'smooth', block: 'start' });
	}

	private parse() {
		const specialElements: HTMLCollection = document.getElementsByTagName('a');
		for (let i = 0; i < specialElements.length; i++) {
			const element = specialElements.item(i) as HTMLAnchorElement;
			if (element.href.includes('#')) {
				this.hrefToPath(element);
				element.removeEventListener('click', this.scrollEvent);
				element.addEventListener('click', this.scrollEvent);
			}
			if (!this._parsed.isStopped) {
				this._parsed.next(true);
				this._parsed.complete();
			}
		}

	}

	private addScripts(HTMLString: string) {
		const fragment = document.createRange().createContextualFragment(HTMLString);
		this.guide.nativeElement.appendChild(fragment);
	}

	transform() {
		if (this.useInnerHTML) {
			const tocElements = document.getElementsByClassName('nav-link');
			for (let i = 0; i < tocElements.length; i++) {
				const href = (tocElements[i] as HTMLAnchorElement).href;
				if (href.includes('#')) {
					this.hrefToPath(tocElements[i] as HTMLAnchorElement);
					tocElements[i].addEventListener('click', this.tocScrollEvent);
					tocElements[i].classList.add('tocElement');
					if (!this._transformed.isStopped) {
						this._transformed.next(true);
						this._transformed.complete();
					}
				}
			}
		} else {
			const userguide = document.getElementById('userguide') as HTMLIFrameElement;
			const images = userguide.contentWindow.document.getElementsByTagName('img');
			for (let i = 0; i < images.length; i++) {
				const image = images[i];
				image.src = `${this.configurationService.app}${image.src}`;
				if (!this._transformed.isStopped) {
					this._transformed.next(true);
					this._transformed.complete();
				}
			}
		}
	}

	private hrefToPath(element: HTMLAnchorElement) {
		const href = element.href;
		const hashtagIndex = href.lastIndexOf('#');
		element.removeAttribute('href');
		element.setAttribute('path', href.slice(hashtagIndex + 1));
	}


	onIFrameLoad(iframe: HTMLIFrameElement) {

		try {
			const images = iframe.contentWindow.document.getElementsByTagName('img');

			for (let i = 0; i < images.length; i++) {
				const tempDiv = document.createElement('div');
				const currentImage = images.item(i);
				tempDiv.innerHTML = currentImage.outerHTML.trim();
				currentImage.src = (tempDiv.firstChild as HTMLImageElement).src;
			}
		} catch {
			console.warn('Could not find contentDocument');
		}
	}
}
