import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { UserSetting, UserSettingsService } from '@app/core/services/user-settings/user-settings.service';
import { BaseComponent } from '@common/base/base.component';
import { Lookup } from '@common/model/lookup';
import { ConfirmationDialogComponent } from '@common/modules/confirmation-dialog/confirmation-dialog.component';
import { TranslateService } from '@ngx-translate/core';
import { isNullOrUndefined } from '@swimlane/ngx-datatable';
import { takeUntil } from 'rxjs/operators';

@Component({
	selector: 'app-user-settings-selector',
	templateUrl: './user-settings-selector.component.html',
	styleUrls: ['./user-settings-selector.component.scss']
})
export class UserSettingsSelectorComponent extends BaseComponent implements OnInit, OnChanges {

	@Input() key: any;
	@Input() lookup: Lookup;
	@Input() autoSelectUserSettings: boolean;
	@Output() onSettingSelected = new EventEmitter<Lookup>();

	settings: any;
	availableUserSettings: UserSetting<any>[] = [];
	currentUserSetting: UserSetting<any>;

	constructor(
		private userSettingsService: UserSettingsService,
		private dialog: MatDialog,
		private language: TranslateService,
	) { super(); }

	ngOnInit() {

		this.userSettingsService.getUserSettingUpdatedObservable().pipe(takeUntil(this._destroyed)).subscribe(key => { //TODO HANDLE-ERRORS
			if (key === this.key.key) { this.getSettings(); }
		});

		this.getSettings();
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['autoSelectUserSettings']) {
			if (changes['autoSelectUserSettings'].currentValue) {
				if (this.currentUserSetting == null && this.settings != null) {
					this.currentUserSetting = this.settings.defaultSetting;
					this.onSettingSelected.emit(this.currentUserSetting ? this.currentUserSetting.value : null);
				}
			}
		}
		if (changes['lookup'] && this.currentUserSetting != null) {
			this.currentUserSetting.value = this.lookup;
		}
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
					} else {
						this.currentUserSetting = settings.defaultSetting;
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
		return !isNullOrUndefined(setting.name) ? setting.name : 'Default';
	}

	settingSelected(event: UserSetting<any>) {
		const setting = this.availableUserSettings.find(x => x.id === event.id);
		if (setting === null) { return; }

		//Persist the active user setting
		this.userSettingsService.set(setting, true, this.key);
	}

	settingDeleted() {
		const value = this.currentUserSetting.id;
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
					this.userSettingsService.remove(this.currentUserSetting.id, this.key);
				}
			});
		}
	}

	saveFilter() {
		//TODO: implement
	}

	updateFilter() {
		this.currentUserSetting.value = this.lookup;
		this.persistLookupChangesManually(this.currentUserSetting, true);
	}

	private persistLookupChangesManually(setting: UserSetting<any>, isDefault: boolean) {
		this.userSettingsService.set(setting, isDefault, this.key);
	}

	private createNewFilter(name: string) {
		let setting: UserSetting<any>;
		setting = this.currentUserSetting ? JSON.parse(JSON.stringify(this.currentUserSetting)) : {};
		setting.value = this.lookup;
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
