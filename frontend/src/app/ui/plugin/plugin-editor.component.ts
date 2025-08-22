
import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, UntypedFormArray } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService } from '@ngx-translate/core';
import { Guid } from '@common/types/guid';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PluginDataType } from '@app/core/common/enum/plugin-data-type';
import { BaseComponent } from '@common/base/base.component';
import { PluginConfiguration, PluginRepositoryConfiguration, PluginRepositoryUserConfiguration} from '@app/core/model/plugin-configuration/plugin-configuration';
import { PluginConfigurationForm } from './plugin-editor.model';


@Component({
	selector: 'app-plugin-editor',
	templateUrl: 'plugin-editor.component.html',
	styleUrls: ['./plugin-editor.component.scss'],
    standalone: false
})
export class PluginEditorComponent extends BaseComponent implements OnInit {

	@Input() formGroup: PluginConfigurationForm = null;
	@Input() data: PluginConfiguration[] = [];
	@Input() fileMap = new Map<Guid, StorageFile>([]);
	@Input() showPluginText = false;
	@Input() showPluginFieldText = false;
	@Input() pluginConfiguration: PluginRepositoryConfiguration[] = [];
	@Input() pluginUserConfiguration: PluginRepositoryUserConfiguration[] = [];

	pluginDataTypeEnum = PluginDataType;
	hideStates: { [key: string]: boolean } = {};

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected authService: AuthService,

	) {
		super();
	}

	ngOnInit(): void {
	}

	getConfigurationFieldLabel(pluginCode: string, fieldCode: string): String{
		return this.pluginConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode).label
	}

	getConfigurationUserFieldLabel(pluginCode: string, fieldCode: string): String{
		return this.pluginUserConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode).label
	}

	hasStringUserFields(plugin: AbstractControl): boolean {
		const userFields = plugin.get('userFields')?.value;
		return Array.isArray(userFields) && userFields.some(field => field.type === this.pluginDataTypeEnum.String);
	}

	toggleVisibility(pluginIndex: number, fieldIndex: number): void {
	const key = `${pluginIndex}_${fieldIndex}`;
	this.hideStates[key] = !this.hideStates[key];
	}
}
