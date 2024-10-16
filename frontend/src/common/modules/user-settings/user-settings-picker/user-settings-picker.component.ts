import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { UserSettingsInformation } from '@app/core/model/user-settings/user-settings.model';
import { UserSetting, UserSettingsService } from '@app/core/services/user-settings/user-settings.service';
import { BaseComponent } from '@common/base/base.component';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { Guid } from '@common/types/guid';
import { TranslateService } from '@ngx-translate/core';
import { isNullOrUndefined } from '@swimlane/ngx-datatable';
import { takeUntil } from 'rxjs/operators';
import { nameof } from 'ts-simple-nameof';

@Component({
	selector: 'app-user-settings-picker',
	templateUrl: './user-settings-picker.component.html',
	styleUrls: ['./user-settings-picker.component.scss']
})
export class UserSettingsPickerComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() key: UserSettingsInformation<any>;
	@Input() userPreference: any;
	@Input() autoSelectUserSettings: boolean;
	@Output() onSettingSelected = new EventEmitter<any>();

	settings: any;
	availableUserSettings: UserSetting<any>[] = [];
	currentUserSetting: UserSetting<any>;

	constructor(
		private userSettingsService: UserSettingsService,
		private dialog: MatDialog,
		private language: TranslateService
	) { super(); }

	ngOnInit() {

		this.userSettingsService.getUserSettingUpdatedObservable().pipe(takeUntil(this._destroyed)).subscribe(key => { //TODO HANDLE-ERRORS
			if (key === this.key.key) { this.getSettings(); }
		});
	}

	ngOnChanges(changes: SimpleChanges) {


		const keyChange = changes[nameof<UserSettingsPickerComponent>(x => x.key)];
		if (keyChange) {
			this.settings = null;
			this.availableUserSettings = [];
			this.currentUserSetting = null;

			if (this.key?.key) {
				this.getSettings();

			}

		}

		if (changes['autoSelectUserSettings']) {
			if (changes['autoSelectUserSettings'].currentValue) {
				if (this.currentUserSetting == null && this.settings != null) {
					this.currentUserSetting = this.settings.defaultSetting;
					this.onSettingSelected.emit(this.currentUserSetting ? this.currentUserSetting.value : null);
				}
			}
		}
	}

	public resetToDraft(): void {
		this.currentUserSetting = null;
	}

	private getSettings() {
		this.userSettingsService.get(this.key).pipe(takeUntil(this._destroyed)).subscribe(s => { //TODO HANDLE-ERRORS
			if (s != null) {
				const settings = JSON.parse(JSON.stringify(s));
				this.settings = settings;
				this.availableUserSettings = settings.settings;
				if (this.autoSelectUserSettings) {
					this.currentUserSetting = settings.defaultSetting;
				} else {
					if (this.currentUserSetting) {
						const filterIndex = this.availableUserSettings.findIndex(x => x.name === this.currentUserSetting.name);
						this.currentUserSetting = this.availableUserSettings[filterIndex];
					}
				}
			} else {
				this.settings = null;
				this.currentUserSetting = null;
				this.availableUserSettings = [];
			}
			if (this.autoSelectUserSettings) { this.onSettingSelected.emit(s != null ? (this.currentUserSetting ? this.currentUserSetting.value : null) : null); }
		});
	}

	getSettingName(setting: UserSetting<any>) {
		return !isNullOrUndefined(setting?.name) ? setting.name : 'Default';
	}

	settingSelected(event: UserSetting<any>) {

		const setting = this.availableUserSettings.find(x => x.id === event.id);
		if (setting === null) { return; }

		// give time to close menu
		setTimeout(() => {
			//Persist the active user setting
			this.onSettingSelected.emit(setting.value);
			this.userSettingsService.set(setting, true, this.key);
			this.currentUserSetting = event;
		}, 100)
	}

	renameCurrentUserSetting(): void {

	}

	settingDeleted(value: Guid = this.currentUserSetting.id) {
		// const value = this.currentUserSetting.id;
		if (value) {
			const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
				maxWidth: '400px',
				restoreFocus: false,
				data: {
					message: this.language.instant('GENERAL.CONFIRMATION-DIALOG.DELETE-USER-SETTING-PROFILE'),
					confirmButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CONFIRMATION'),
					cancelButton: this.language.instant('GENERAL.CONFIRMATION-DIALOG.ACTIONS.CANCELATION')
				}
			});
			dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(result => {
				if (result) {
					this.userSettingsService.remove(value, this.key);
				}
			});
		}
	}

	saveFilter() {
		//TODO: implement
	}


	private persistLookupChangesManually(setting: UserSetting<any>, isDefault: boolean) {
		this.userSettingsService.set(setting, isDefault, this.key);
	}

	private createNewFilter(name: string) {
		let setting: UserSetting<any>;
		setting = this.currentUserSetting ? JSON.parse(JSON.stringify(this.currentUserSetting)) : {};
		setting.value = this.userPreference;
		setting.id = null;
		setting.hash = null;
		setting.name = name;
		setting.isDefault = true;
		setting.createdAt = null;
		setting.updatedAt = null;
		setting.userId = null;
		this.currentUserSetting = setting;

		this.persistLookupChangesManually(this.currentUserSetting, true);
	}

	compareFn(c1: UserSetting<any>, c2: UserSetting<any>): boolean {
		return c1 && c2 ? c1.id === c2.id : c1 === c2;
	}
}
