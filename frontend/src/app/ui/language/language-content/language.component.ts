import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { UserService } from '@app/core/services/user/user.service';
import { BaseComponent } from '@common/base/base.component';

@Component({
	selector: 'app-language',
	templateUrl: './language.component.html',
	styleUrls: ['./language.component.scss']
})
export class LanguageComponent extends BaseComponent implements OnInit {

	@Output() languageChange: EventEmitter<any> = new EventEmitter();
	languages: string[] = [];

	constructor(
		private router: Router,
		private authentication: AuthService,
		private languageService: LanguageService
	) {
		super();
		this.languages = this.languageService.getAvailableLanguagesCodes();
	}

	ngOnInit() {
		this.languageChange.emit(this.getCurrentLanguage())
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	public getCurrentLanguage(): string {
		const lang = this.languages.find(lang => lang === this.languageService.getCurrentLanguage());
		return lang;
	}

	onLanguageSelected(selectedLanguage: MatButtonToggleChange) {
		if (this.isAuthenticated()) {
			const langMap = new Map<string, string>();
			langMap.set('language', selectedLanguage.value);
			this.languageService.changeLanguage(selectedLanguage.value);
			this.router.navigateByUrl(this.router.url);
		} else {
			this.languageService.changeLanguage(selectedLanguage.value);
			this.router.navigateByUrl(this.router.url);
		}
		this.languageChange.emit(selectedLanguage.value);
	}

}
