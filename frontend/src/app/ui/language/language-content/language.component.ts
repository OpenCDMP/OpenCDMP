import { Component, EventEmitter, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonToggleChange, MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { Router } from '@angular/router';
import { AuthService } from '@app/core/services/auth/auth.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { UserService } from '@app/core/services/user/user.service';
import { BaseComponent } from '@common/base/base.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { takeUntil } from 'rxjs/operators';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-language',
    templateUrl: './language.component.html',
    styleUrls: ['./language.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatButtonToggleGroup, MatButtonToggle, MatIcon]
})
export class LanguageComponent extends BaseComponent implements OnInit {

	@Output() languageChange: EventEmitter<any> = new EventEmitter();

	constructor(
		private router: Router,
		private authentication: AuthService,
		protected languageService: LanguageService,
		private userService: UserService,
		private httpErrorHandlingService: HttpErrorHandlingService
	) {
		super();
	}

	// Read on each render: the names it sorts by are only known once the translations have loaded.
	get languages(): string[] {
		return this.languageService.getAvailableLanguagesCodes();
	}

	ngOnInit() {
		this.languageChange.emit(this.getCurrentLanguage())
	}

	public isAuthenticated(): boolean {
		return this.authentication.currentAccountIsAuthenticated();
	}

	public getCurrentLanguage(): string {
		return this.currentLanguageOf(this.languages);
	}

	protected currentLanguageOf(languages: string[]): string {
		return languages.find(lang => lang === this.languageService.getCurrentLanguage());
	}

	onLanguageSelected(selectedLanguage: MatButtonToggleChange) {
		// signed in, the choice is the profile's language, which every later visit and e-mail uses
		if (this.isAuthenticated()) {
			this.userService.updateLanguageMine(selectedLanguage.value)
				.pipe(takeUntil(this._destroyed))
				.subscribe({ error: error => this.httpErrorHandlingService.handleBackedRequestError(error) });
		}
		this.languageService.changeLanguage(selectedLanguage.value);
		this.router.navigateByUrl(this.router.url);
		this.languageChange.emit(selectedLanguage.value);
	}

}
