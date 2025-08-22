package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DefinitionPersist {

	private List<SectionPersist> sections;

	public static final String _sections = "sections";

	private List<PluginConfigurationPersist> pluginConfigurations;

	public static final String _pluginConfigurations = "pluginConfigurations";

	public List<SectionPersist> getSections() {
		return this.sections;
	}

	public void setSections(List<SectionPersist> sections) {
		this.sections = sections;
	}

	public static final String _hasAnyDescriptionTemplatesError = "hasAnyDescriptionTemplates";

	public List<PluginConfigurationPersist> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationPersist> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

	@Component(DefinitionPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class DefinitionPersistValidator extends BaseValidator<DefinitionPersist> {

		public static final String ValidatorName = "PlanBlueprint.DefinitionPersistValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;

		protected DefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

		@Override
		protected Class<DefinitionPersist> modelClass() {
			return DefinitionPersist.class;
		}

		@Override
		protected List<Specification> specifications(DefinitionPersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isListNullOrEmpty(item.getSections()))
							.failOn(DefinitionPersist._sections).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DefinitionPersist._sections}, LocaleContextHolder.getLocale())),
					this.navSpec()
							.iff(() -> !this.isListNullOrEmpty(item.getSections()))
							.on(DefinitionPersist._sections)
							.over(item.getSections())
							.using((itm) -> this.validatorFactory.validator(SectionPersist.SectionPersistValidator.class)),
					this.navSpec()
							.iff(() -> !this.isListNullOrEmpty(item.getPluginConfigurations()))
							.on(DefinitionPersist._pluginConfigurations)
							.over(item.getPluginConfigurations())
							.using((itm) -> this.validatorFactory.validator(PluginConfigurationPersist.PluginConfigurationPersistValidator.class))
					//TODO: We need to add a validation that check if hasTemplates boolean is true in at least one section.
//					this.spec()
//							.must(() -> !this.isListNullOrEmpty(item.getSections()) && item.sections.stream().anyMatch(x -> x.getHasTemplates()))
//							.failOn(DefinitionPersist._hasAnyDescriptionTemplatesError).failWith(messageSource.getMessage("Validation_Required", null, LocaleContextHolder.getLocale())),
					);
		}


	}

}
