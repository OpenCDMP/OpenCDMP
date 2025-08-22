package org.opencdmp.model.persist.tenantconfiguration;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptiontemplatedefinition.DefinitionPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class PluginTenantConfigurationPersist {

	private List<PluginConfigurationPersist> pluginConfigurations;
	public static final String _pluginConfigurations = "pluginConfigurations";

	public List<PluginConfigurationPersist> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationPersist> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

	@Component(PluginTenantConfigurationPersist.PluginTenantConfigurationPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class PluginTenantConfigurationPersistValidator extends BaseValidator<PluginTenantConfigurationPersist> {

		public static final String ValidatorName = "PluginTenantConfigurationPersistValidator";

		private final MessageSource messageSource;
		private final ValidatorFactory validatorFactory;

		protected PluginTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

		@Override
		protected Class<PluginTenantConfigurationPersist> modelClass() {
			return PluginTenantConfigurationPersist.class;
		}

		@Override
		protected List<Specification> specifications(PluginTenantConfigurationPersist item) {
			return Arrays.asList(
					this.navSpec()
							.iff(() -> !this.isListNullOrEmpty(item.getPluginConfigurations()))
							.on(PluginTenantConfigurationPersist._pluginConfigurations)
							.over(item.getPluginConfigurations())
							.using((itm) -> this.validatorFactory.validator(PluginConfigurationPersist.PluginConfigurationPersistValidator.class))
			);
		}
	}
}
