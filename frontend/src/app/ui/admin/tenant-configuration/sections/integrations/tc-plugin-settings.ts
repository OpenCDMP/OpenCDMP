import { UntypedFormGroup } from '@angular/forms';
import { TenantConfigurationType } from '@app/core/common/enum/tenant-configuration-type';
import { PluginConfiguration, PluginRepositoryConfiguration } from '@app/core/model/plugin-configuration/plugin-configuration';
import { TenantConfiguration } from '@app/core/model/tenant-configuaration/tenant-configuration';
import { TenantConfigurationEditorModel } from '@app/ui/admin/tenant-configuration/models/plugin-configuration/plugin-configuration-editor.model';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { ValidationContext } from '@common/forms/validation/validation-context';
import { TcEditorModel } from '../../shared/tc-part';

/**
 * The plugin settings model, completed with what the plugins currently declare: a plugin missing from the
 * stored record, or a setting a plugin added since it was saved, shows up empty instead of not at all.
 */
export class TcPluginSettingsModel implements TcEditorModel {
	private inner = new TenantConfigurationEditorModel();

	constructor(
		private readonly repositories: PluginRepositoryConfiguration[],
		private readonly available: PluginConfiguration[],
	) {
		this.fromModel(null);
	}

	get validationErrorModel(): ValidationErrorModel {
		return this.inner.validationErrorModel;
	}

	fromModel(item: TenantConfiguration | null): TcPluginSettingsModel {
		this.inner = new TenantConfigurationEditorModel().fromModel(this.complete(item), this.repositories, this.available);
		return this;
	}

	buildForm(context?: ValidationContext, disabled?: boolean): UntypedFormGroup {
		return this.inner.buildForm(context, disabled);
	}

	private complete(item: TenantConfiguration | null): TenantConfiguration {
		const stored = item?.pluginConfiguration?.pluginConfigurations ?? [];
		const same = (x: PluginConfiguration, y: PluginConfiguration) => x.pluginCode === y.pluginCode && x.pluginType === y.pluginType;
		const merged: PluginConfiguration[] = this.available.map(declared => {
			const saved = stored.find(x => same(x, declared));
			if (!saved) return declared;
			const fields = [...(saved.fields ?? [])];
			for (const field of declared.fields ?? []) {
				if (!fields.some(x => x.code === field.code)) fields.push(field);
			}
			return { ...saved, fields };
		});
		// settings of plugins that are not reachable right now are kept as they are
		merged.push(...stored.filter(x => !this.available.some(y => same(x, y))));
		return {
			...(item ?? {}),
			type: TenantConfigurationType.PluginConfiguration,
			pluginConfiguration: { ...(item?.pluginConfiguration ?? {}), pluginConfigurations: merged },
		} as TenantConfiguration;
	}
}
