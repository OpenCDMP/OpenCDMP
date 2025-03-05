import { Component, OnDestroy, OnInit } from '@angular/core';
import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Params } from '@angular/router';
import { RoleOrganizationType } from '@app/core/common/enum/role-organization-type';
import { ReferenceType } from '@app/core/model/reference-type/reference-type';
import { Reference } from '@app/core/model/reference/reference';
import { Tenant } from '@app/core/model/tenant/tenant';
import { User, UserAdditionalInfo, UserCredential, UserPersist } from '@app/core/model/user/user';
import { AuthService } from '@app/core/services/auth/auth.service';
import { ConfigurationService } from '@app/core/services/configuration/configuration.service';
import { CultureInfo, CultureService } from '@app/core/services/culture/culture-service';
import { PrincipalService } from '@app/core/services/http/principal.service';
import { LanguageService } from '@app/core/services/language/language.service';
import { AnalyticsService } from '@app/core/services/matomo/analytics-service';
import { SnackBarNotificationLevel, UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { ReferenceTypeService } from '@app/core/services/reference-type/reference-type.service';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { TenantHandlingService } from '@app/core/services/tenant/tenant-handling.service';
import { TimezoneService } from '@app/core/services/timezone/timezone-service';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { PopupNotificationDialogComponent } from "@app/library/notification/popup/popup-notification.component";
import { BaseComponent } from '@common/base/base.component';
import { FormService } from '@common/forms/form-service';
import { FormValidationErrorsDialogComponent } from '@common/forms/form-validation-errors-dialog/form-validation-errors-dialog.component';
import { BaseHttpParams } from '@common/http/base-http-params';
import { InterceptorType } from '@common/http/interceptors/interceptor-type';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';
import { AddAccountDialogComponent } from './add-account/add-account-dialog.component';
import { UserProfileEditorModel } from './user-profile-editor.model';

@Component({
    selector: 'app-user-profile',
    templateUrl: './user-profile.component.html',
    styleUrls: ['./user-profile.component.scss'],
    standalone: false
})
export class UserProfileComponent extends BaseComponent implements OnInit, OnDestroy {

	userProfileEditorModel: UserProfileEditorModel;
	user: Observable<User>;
	//TODO: refactor
	userCredentials: Observable<UserCredential[]>;
	firstEmail: String;
	userLanguage: String;
	currentUserId: string;
	cultureValues = new Array<CultureInfo>();
	timezoneValues = new Array<string>();
	filteredCultures = new Array<CultureInfo>();
	filteredTimezones = new Array<string>();
	timezones: Observable<any[]>;
	editMode = false;
	languages = [];
	roleOrganizationEnum = RoleOrganizationType;
	errorMessages = [];
	nestedCount = [];
	nestedIndex = 0;
	tenants: Observable<Array<Tenant>>;
	expandedPreferences: boolean = false;

	organisationsSingleAutoCompleteConfiguration: SingleAutoCompleteConfiguration;

	formGroup: UntypedFormGroup;
	tenantFormGroup: UntypedFormGroup;

	constructor(
		private userService: UserService,
		private route: ActivatedRoute,
		private authService: AuthService,
		private language: TranslateService,
		private cultureService: CultureService,
		private timezoneService: TimezoneService,
		private languageService: LanguageService,
		private configurationService: ConfigurationService,
		private uiNotificationService: UiNotificationService,
		private dialog: MatDialog,
		public enumUtils: EnumUtils,
		private formBuilder: UntypedFormBuilder,
		private tenantHandlingService: TenantHandlingService,
		private principalService: PrincipalService,
		private formService: FormService,
		private referenceService: ReferenceService,
		private referenceTypeService: ReferenceTypeService,
		private analyticsService: AnalyticsService,
		private httpErrorHandlingService: HttpErrorHandlingService
	) {
		super();
		this.languages = this.languageService.getAvailableLanguagesCodes();
	}


	public getProviderIcons(userCredential: UserCredential, culture: string): string[] {

		if (userCredential.data.externalProviderNames === undefined || userCredential.data.externalProviderNames?.length === 0) {
			return [this.configurationService.authProviders.defaultAuthProvider.providerClass];
		}

		const providerNames: string[] = [];
		for (let providerName of userCredential.data.externalProviderNames) {
			const providerImage = this.configurationService.authProviders.findOrGetDefault(providerName.toString(), culture).providerClass;
			if (providerImage !== null) {
				providerNames.push(providerImage);
			}
		}

		return providerNames;
	}

	ngOnInit() {
		this.cultureValues = this.cultureService.getCultureValues();
		this.timezoneValues = this.timezoneService.getTimezoneValues();

		this.tenantFormGroup = this.formBuilder.group({
			tenantCode: [this.authService.selectedTenant(), [Validators.required]]
		});
		this.analyticsService.trackPageView(AnalyticsService.UserProfile);

		this.organisationsSingleAutoCompleteConfiguration = this.referenceService.getSingleAutocompleteSearchConfiguration(this.referenceTypeService.getOrganizationReferenceType(), null);
		this.route.params
			.pipe(takeUntil(this._destroyed))
			.subscribe((params: Params) => {
				this.getOrRefreshData();
			});

	}

	getOrRefreshData() {
		this.currentUserId = this.authService.userId()?.toString();
		this.user = this.userService.getSingle(
			Guid.parse(this.currentUserId),
			[
				nameof<User>(x => x.id),
				nameof<User>(x => x.name),
				nameof<User>(x => x.additionalInfo.language),
				nameof<User>(x => x.additionalInfo.timezone),
				nameof<User>(x => x.additionalInfo.culture),
				[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), nameof<Reference>(x => x.id)].join('.'),
				[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), nameof<Reference>(x => x.label)].join('.'),
				[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), nameof<Reference>(x => x.type), nameof<ReferenceType>(x => x.id)].join('.'),
				[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), nameof<Reference>(x => x.reference)].join('.'),
				[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), nameof<Reference>(x => x.source)].join('.'),
				[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), nameof<Reference>(x => x.sourceType)].join('.'),
				[nameof<User>(x => x.additionalInfo), nameof<UserAdditionalInfo>(x => x.organization), nameof<Reference>(x => x.isActive)].join('.'),
				nameof<User>(x => x.additionalInfo.roleOrganization),
				nameof<User>(x => x.createdAt),
				nameof<User>(x => x.updatedAt),
				nameof<User>(x => x.hash),
				`${nameof<User>(x => x.credentials)}.${nameof<UserCredential>(x => x.id)}`,
				`${nameof<User>(x => x.credentials)}.${nameof<UserCredential>(x => x.data.email)}`,
				`${nameof<User>(x => x.credentials)}.${nameof<UserCredential>(x => x.data.externalProviderNames)}`,
			]
		)
			.pipe(map(result => {
				this.userLanguage = result.additionalInfo.language;
				this.firstEmail = result.credentials[0].data.email;
				this.userCredentials = of(result.credentials);

				this.userProfileEditorModel = new UserProfileEditorModel().fromModel(result);
				this.formGroup = this.userProfileEditorModel.buildForm(this.languageService.getAvailableLanguagesCodes());

				this.registerChangeListeners();

				this.tenants = this.loadUserTenants();
				this.unlock();
				return result;
			}));
	}

	registerChangeListeners() {

		this.filteredCultures = this.cultureValues.filter((culture) => culture.name === this.userProfileEditorModel.additionalInfo.culture);
		this.filteredTimezones = this.timezoneValues.filter((zone) => zone === this.userProfileEditorModel.additionalInfo.timezone);
		// set change listeners
		this.formGroup.get('additionalInfo').get('timezone').valueChanges
			.pipe(takeUntil(this._destroyed))
			.subscribe((text) => {
				const searchText = text.toLowerCase();
				const result = this.timezoneValues.filter((zone) => zone.toLowerCase().indexOf(searchText) >= 0);
				this.filteredTimezones = result;
			});

		this.formGroup.get('additionalInfo').get('culture').valueChanges
			.pipe(takeUntil(this._destroyed))
			.subscribe((text) => {
				const searchText = text.toLowerCase();
				const result = this.cultureValues.filter((culture) => culture.name.toLowerCase().indexOf(searchText) >= 0);
				this.filteredCultures = result;
			});
	}



	save() {
		this.formService.removeAllBackEndErrors(this.formGroup);
		this.formService.touchAllFormFields(this.formGroup);
		if (!this.formGroup.valid) {
			this.printErrors(this.formGroup);
			this.showValidationErrorsDialog();
			this.nestedCount = [];
			this.nestedIndex = 0;
			this.errorMessages = [];
			return;
		}
		const formData = this.formService.getValue(this.formGroup.value) as UserPersist;
		if (formData.additionalInfo.organization) formData.additionalInfo.organization.typeId = Guid.parse(this.referenceTypeService.getOrganizationReferenceType());
		this.userService.persist(formData)
			.pipe(takeUntil(this._destroyed))
			.subscribe(
				x => {
					this.editMode = false;
					this.languageService.changeLanguage(this.formGroup.get('additionalInfo').get('language').value);
					this.authService.refresh()
						.pipe(takeUntil(this._destroyed))
						.subscribe(result => { //TODO HANDLE-ERRORS
							this.uiNotificationService.snackBarNotification(this.language.instant('GENERAL.SNACK-BAR.SUCCESSFUL-UPDATE'), SnackBarNotificationLevel.Success);
							window.location.reload();
						});
				},
				error => this.httpErrorHandlingService.handleBackedRequestError(error));
	}

	public unlock() {
		this.editMode = true;
		this.formGroup.enable();
	}

	private showValidationErrorsDialog(projectOnly?: boolean) {
		const dialogRef = this.dialog.open(FormValidationErrorsDialogComponent, {
			disableClose: true,
			restoreFocus: false,
			data: {
				errorMessages: this.errorMessages,
				projectOnly: projectOnly
			},
		});
	}

	public applyFallbackAvatar(ev: Event) {
		(ev.target as HTMLImageElement).src = 'assets/images/profile-placeholder.png';
	}

	public removeAccount(userCredential: any) {
		this.dialog.open(ConfirmationDialogComponent, {
			data: {
				message: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT-DIALOG.MESSAGE'),
				confirmButton: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT-DIALOG.CONFIRM'),
				cancelButton: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT-DIALOG.CANCEL')
			},
			maxWidth: '35em'
		})
			.afterClosed()
			.subscribe(confirm => {
				if (confirm) {

					this.userService.removeCredentialAccount({ credentialId: userCredential.id }).subscribe(result => {
						this.dialog.open(PopupNotificationDialogComponent, {
							data: {
								title: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT.TITLE'),
								message: this.language.instant('USER-PROFILE.UNLINK-ACCOUNT.MESSAGE', { 'accountToBeUnlinked': userCredential.data?.email })
							}, maxWidth: '30em'
						});
					},
						error => this.httpErrorHandlingService.handleBackedRequestError(error));
				}
			});
	}

	public addAccount() {
		const dialogRef = this.dialog.open(AddAccountDialogComponent, {
			restoreFocus: false,
			width: 'min(30vw, 600px)',
			minWidth: 'fit-content',
			data: {
				email: ''
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
			if (result) {
				this.userService.mergeAccount({ email: result.email })
					.subscribe(result => {
						if (result) {
							this.dialog.open(PopupNotificationDialogComponent, {
								data: {
									title: this.language.instant('USER-PROFILE.MERGING-EMAILS-DIALOG.TITLE'),
									message: this.language.instant('USER-PROFILE.MERGING-EMAILS-DIALOG.MESSAGE')
								}, maxWidth: '30em'
							});
						}
					},
						error => this.httpErrorHandlingService.handleBackedRequestError(error));
			}
		});
	}

	printErrors(rootform: UntypedFormGroup) {
		if (!rootform.valid) {
			Object.keys(rootform.controls).forEach(key => {
				const errors = rootform.get(key).errors;
				if (errors !== null) {
					let numbering: string = '';
					for (let j = 0; j < this.nestedCount.length; j++) {
						numbering += this.nestedCount[j];
						if (j < this.nestedIndex) {
							numbering += '.';
						} else {
							break;
						}
					}
					Object.keys(errors).forEach(keyError => {
						if (typeof errors[keyError] === 'boolean') {
							this.errorMessages.push(
                                numbering + ' ' + this.language.instant(this.formLabelMap.get(key)?? '') + ' ' + (keyError === 'required'? this.language.instant('GENERAL.FORM-VALIDATION-DISPLAY-DIALOG.REQUIRED') : keyError)
                            );
						} else {
							this.errorMessages.push(
                                numbering + ' ' + this.language.instant(this.formLabelMap.get(key)?? '') + ': ' + keyError + ': ' + JSON.stringify(errors[keyError])
                            );
						}
					});
				} else {
					if (rootform.get(key) instanceof UntypedFormGroup) {
						this.printErrors(<UntypedFormGroup>rootform.get(key));
					} else if (rootform.get(key) instanceof UntypedFormArray) {
						this.nestedIndex++;
						this.nestedCount[this.nestedIndex] = 0;
						for (let childForm of (<UntypedFormArray>rootform.get(key)).controls) {
							this.nestedCount[this.nestedIndex]++;
							this.printErrors(<any>childForm);
						}
						this.nestedCount[this.nestedIndex] = 0;
						this.nestedIndex--;

					}
				}
			});
		}
	}

	// Switch Tenant
	loadUserTenants(): Observable<Array<Tenant>> {
		const params = new BaseHttpParams();
		params.interceptorContext = {
			excludedInterceptors: [InterceptorType.TenantHeaderInterceptor]
		};
		return this.principalService.myTenants({ params: params });
	}

	switchTenant(): void {
		const selectedTenant = this.tenantFormGroup.get('tenantCode').value;
        
		if (!this.tenantFormGroup.valid || !selectedTenant) return;


		this.authService.selectedTenant(selectedTenant);
		window.location.href = this.tenantHandlingService.getCurrentUrlEnrichedWithTenantCode(selectedTenant, true);
	}

	//Preferences
	expandPreferences(): void {
		this.expandedPreferences = true;
	}

	displayCultureFn(cultureName?: string): string | undefined {

		const culture: CultureInfo = this.cultureValues?.find(x => x.name == cultureName);
		if (culture == null
			|| culture.displayName == null)
			return undefined;

		return culture.displayName + ' [' + culture.name + ']';
	}

    formLabelMap = new Map([
        ['name', 'USER-PROFILE.SETTINGS.NAME'], 
        ['avatarUrl', 'ALT-TEXT.USER-AVATAR'],
        ['timezone', 'USER-PROFILE.SETTINGS.TIMEZONE'],
        ['culture', 'USER-PROFILE.SETTINGS.CULTURE'],
        ['language', 'USER-PROFILE.SETTINGS.LANGUAGE'],
        ['roleOrganization', 'USER-PROFILE.SETTINGS.ROLE'],
        ['organization', 'USER-PROFILE.SETTINGS.ORGANIZATION'],
    ])

}
