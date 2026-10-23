
import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { AbstractControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { UiNotificationService } from '@app/core/services/notification/ui-notification-service';
import { AuthService } from '@app/core/services/auth/auth.service';
import { FormService } from '@common/forms/form-service';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { Guid } from '@common/types/guid';
import { StorageFile } from '@app/core/model/storage-file/storage-file';
import { PluginDataType } from '@app/core/common/enum/plugin-data-type';
import { BaseComponent } from '@common/base/base.component';
import { ConfigurationField, ConfigurationFieldOption, PluginConfiguration, PluginRepositoryConfiguration, PluginRepositoryUserConfiguration} from '@app/core/model/plugin-configuration/plugin-configuration';
import { PluginConfigurationForm } from './plugin-editor.model';
import { PluginType } from '@app/core/common/enum/plugin-type';
import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { TemplateValidationIssueLevel, TemplateValidationResult } from '@app/core/model/file/template-validation-result.model';
import { FileTransformerHttpService } from '@app/core/services/file-transformer/file-transformer.http.service';
import { takeUntil } from 'rxjs/operators';
import { MatListSubheaderCssMatStyler } from '@angular/material/list';
import { MatFormField, MatLabel, MatSuffix, MatHint, MatError } from '@angular/material/form-field';
import { ReactiveAsteriskDirective } from '../../../common/forms/reactive-asterisk-directive';
import { MatInput } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { FileUploadComponent } from '../../library/file-uploader/file-uploader.component';
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatButton } from '@angular/material/button';
import { NgxColorsModule } from 'ngx-colors';


@Component({
    selector: 'app-plugin-editor',
    templateUrl: 'plugin-editor.component.html',
    styleUrls: ['./plugin-editor.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatListSubheaderCssMatStyler, MatFormField, MatLabel, ReactiveAsteriskDirective, MatInput, FormsModule, ReactiveFormsModule, MatIcon, MatSuffix, MatHint, MatError, FileUploadComponent, MatSlideToggle, MatSelect, MatOption, MatButton, NgxColorsModule, TranslatePipe]
})
export class PluginEditorComponent extends BaseComponent {

	@Input() formGroup: PluginConfigurationForm = null;
	@Input() data: PluginConfiguration[] = [];
	@Input() fileMap = new Map<Guid, StorageFile>([]);
	@Input() showPluginText = false;
	@Input() showPluginFieldText = false;
	@Input() pluginConfiguration: PluginRepositoryConfiguration[] = [];
	@Input() pluginUserConfiguration: PluginRepositoryUserConfiguration[] = [];
	/** Shows only this plugin's settings; the others stay in the form untouched. */
	@Input() onlyPluginCode: string = null;

	pluginDataTypeEnum = PluginDataType;
	templateValidationIssueLevelEnum = TemplateValidationIssueLevel;
	hideStates: { [key: string]: boolean } = {};
	templateValidationResults: { [key: string]: TemplateValidationResult } = {};

	constructor(
		protected dialog: MatDialog,
		protected language: TranslateService,
		protected formService: FormService,
		protected uiNotificationService: UiNotificationService,
		protected httpErrorHandlingService: HttpErrorHandlingService,
		protected authService: AuthService,
		protected fileTransformerHttpService: FileTransformerHttpService,

	) {
		super();
	}

	getConfigurationFieldLabel(pluginCode: string, fieldCode: string): string {
		const translationKey = `PLUGIN-EDITOR.ADMIN-CONFIGURATION.${pluginCode.toUpperCase()}.${fieldCode.toUpperCase()}`;
		const translated = this.language.instant(translationKey);
		if (translated !== translationKey) return translated;
		return this.pluginConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode)?.label ?? fieldCode;
	}

	getConfigurationUserFieldLabel(pluginCode: string, fieldCode: string): string {
		const translationKey = `PLUGIN-EDITOR.USER-CONFIGURATION.${pluginCode.toUpperCase()}.${fieldCode.toUpperCase()}`;
		const translated = this.language.instant(translationKey);
		if (translated !== translationKey) return translated;
		return this.pluginUserConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode)?.label ?? fieldCode;
	}

	hasStringUserFields(plugin: AbstractControl): boolean {
		const userFields = plugin.get('userFields')?.value;
		const inlineTypes = [PluginDataType.String, PluginDataType.Boolean, PluginDataType.Select, PluginDataType.Color, PluginDataType.Number, PluginDataType.TextArea, PluginDataType.OrderedList, PluginDataType.KeyValue, PluginDataType.ReferenceTypeFields];
		return Array.isArray(userFields) && userFields.some(field => inlineTypes.includes(field.type));
	}

	getConfigurationFieldOptions(pluginCode: string, fieldCode: string): ConfigurationFieldOption[] {
		return this.pluginConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode)?.options ?? [];
	}

	getConfigurationUserFieldOptions(pluginCode: string, fieldCode: string): ConfigurationFieldOption[] {
		return this.pluginUserConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode)?.options ?? [];
	}

	/** Edited as plain text here; the new settings pages have dedicated editors for the list types. */
	isMultiLine(type: PluginDataType): boolean {
		return [PluginDataType.TextArea, PluginDataType.OrderedList, PluginDataType.KeyValue, PluginDataType.ReferenceTypeFields].includes(type);
	}

	isBooleanFieldChecked(field: AbstractControl): boolean {
		return field.get('textValue').value === 'true';
	}

	setBooleanField(field: AbstractControl, checked: boolean): void {
		const control = field.get('textValue');
		control.setValue(checked ? 'true' : 'false');
		control.markAsDirty();
	}

	/**
	 * The value the plugin applies when the field is left empty, as shown to the user: the option label for
	 * selects. Editors only display it; the field stays empty so broader configuration scopes still apply.
	 */
	defaultValue(pluginCode: string, fieldCode: string, userField: boolean): string | null {
		const field = this.fieldDefinition(pluginCode, fieldCode, userField);
		if (field?.defaultValue == null || field.defaultValue === '') return null;
		return field.options?.find(x => x.value === field.defaultValue)?.label ?? field.defaultValue;
	}

	defaultOptionLabel(pluginCode: string, fieldCode: string, userField: boolean): string {
		const fallback = this.defaultValue(pluginCode, fieldCode, userField);
		return fallback != null
			? this.language.instant('PLUGIN-EDITOR.DEFAULT-OPTION-WITH-VALUE', { value: fallback })
			: this.language.instant('PLUGIN-EDITOR.DEFAULT-OPTION');
	}

	private fieldDefinition(pluginCode: string, fieldCode: string, userField: boolean): ConfigurationField | undefined {
		const configurations = userField ? this.pluginUserConfiguration : this.pluginConfiguration;
		return configurations?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode);
	}

	/** Text fields are masked when the plugin marks them as credentials, or when their definition is unknown. */
	isSecretField(pluginCode: string, fieldCode: string, userField: boolean): boolean {
		const field = this.fieldDefinition(pluginCode, fieldCode, userField);
		return field == null || field.authInfo === true;
	}

	// a stored secret never reaches the browser: textValue null keeps it, '' removes it, anything else replaces it
	private replacing = new Set<AbstractControl>();

	secretState(field: AbstractControl): 'stored' | 'removed' | 'editing' {
		if (!field.get('secretSet')?.value || this.replacing.has(field)) return 'editing';
		return field.get('textValue').value === '' ? 'removed' : 'stored';
	}

	replaceSecret(field: AbstractControl): void {
		this.replacing.add(field);
		this.setSecretText(field, '');
	}

	removeSecret(field: AbstractControl): void {
		this.replacing.delete(field);
		this.setSecretText(field, '');
	}

	keepSecret(field: AbstractControl): void {
		this.replacing.delete(field);
		this.setSecretText(field, null);
	}

	private setSecretText(field: AbstractControl, value: string | null): void {
		const control = field.get('textValue');
		control.markAsDirty();
		control.setValue(value);
	}

	toggleVisibility(pluginIndex: number, fieldIndex: number): void {
	const key = `${pluginIndex}_${fieldIndex}`;
	this.hideStates[key] = !this.hideStates[key];
	}

	validateUploadedTemplate(plugin: AbstractControl, field: AbstractControl, key: string): void {
		delete this.templateValidationResults[key];
		if (plugin.get('pluginType')?.value !== PluginType.FileTransformer) return;
		const fileId = field.get('fileValue')?.value;
		if (!fileId) return;

		const pluginCode = plugin.get('pluginCode').value;
		const fieldCode = field.get('code').value;
		const configuration = this.pluginConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode)
			?? this.pluginUserConfiguration?.find(x => x.repositoryId === pluginCode)?.fields?.find(x => x.code === fieldCode);
		const entityType = configuration?.appliesTo?.length ? configuration.appliesTo[0] : PluginEntityType.Plan;

		this.fileTransformerHttpService.validateTemplate(fileId, pluginCode, entityType)
			.pipe(takeUntil(this._destroyed))
			.subscribe({
				next: (result) => {
					if (result && result.issues?.length) this.templateValidationResults[key] = result;
				},
				// validation is advisory: a failing call must not block the upload flow
				error: () => { }
			});
	}

	clearTemplateValidation(key: string): void {
		delete this.templateValidationResults[key];
	}
}
