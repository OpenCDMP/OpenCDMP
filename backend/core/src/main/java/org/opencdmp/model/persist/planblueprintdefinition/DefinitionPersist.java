package org.opencdmp.model.persist.planblueprintdefinition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptiontemplatedefinition.FieldPersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.FieldSetPersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.SectionPersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.SystemFieldDataPersist;
import org.opencdmp.model.persist.iconconfiguration.IconConfigurationPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

public class DefinitionPersist {

	private List<RootSectionPersist> sections;

	public static final String _sections = "sections";

	private List<PluginConfigurationPersist> pluginConfigurations;

	public static final String _pluginConfigurations = "pluginConfigurations";

	private PlanCreationConsentPersist planCreationConsent;

	public static final String _planCreationConsent = "planCreationConsent";

	private IconConfigurationPersist iconConfiguration;

	public static final String _iconConfiguration = "iconConfiguration";

	public List<RootSectionPersist> getSections() {
		return this.sections;
	}

	public void setSections(List<RootSectionPersist> sections) {
		this.sections = sections;
	}

	public static final String _hasAnyDescriptionTemplatesError = "hasAnyDescriptionTemplates";

	public List<PluginConfigurationPersist> getPluginConfigurations() {
		return pluginConfigurations;
	}

	public void setPluginConfigurations(List<PluginConfigurationPersist> pluginConfigurations) {
		this.pluginConfigurations = pluginConfigurations;
	}

	public PlanCreationConsentPersist getPlanCreationConsent() {
		return planCreationConsent;
	}

	public void setPlanCreationConsent(PlanCreationConsentPersist planCreationConsent) {
		this.planCreationConsent = planCreationConsent;
	}

	public IconConfigurationPersist getIconConfiguration() {
		return iconConfiguration;
	}

	public void setIconConfiguration(IconConfigurationPersist iconConfiguration) {
		this.iconConfiguration = iconConfiguration;
	}

	@JsonIgnore
	public List<FieldPersist> getAllField() {
		List<FieldPersist> fieldPersists = new ArrayList<>();
		if (this.getSections() != null) {
			for (SectionPersist rootSectionPersist : this.getSections()) {
				fieldPersists.addAll(rootSectionPersist.getAllField());
			}
		}
		return fieldPersists;
	}

	@JsonIgnore
	public List<FieldPersist> getFieldById(String id) {
		return this.getAllField().stream().filter(x -> id.equals(x.getId())).toList();
	}

	@JsonIgnore
	public List<FieldSetPersist> getAllFieldSets() {
		List<FieldSetPersist> result = new ArrayList<>();
		if (this.getSections() != null) {
			for (SectionPersist section : this.getSections()) {
				result.addAll(section.getAllFieldSets());
			}
		}
		return result;
	}

	@JsonIgnore
	public List<FieldSetPersist> getFieldSetById(String id) {
		return this.getAllFieldSets().stream().filter(x-> id != null && id.equals(x.getId())).toList();
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
							.using((itm) -> this.validatorFactory.validator(RootSectionPersist.RootSectionPersistValidator.class)),
					this.refSpec()
							.iff(() -> !this.isNull(item.getIconConfiguration()))
							.on(DefinitionPersist._iconConfiguration)
							.over(item.getIconConfiguration())
							.using(() -> this.validatorFactory.validator(IconConfigurationPersist.IconConfigurationPersistValidator.class)),
					this.navSpec()
							.iff(() -> !this.isListNullOrEmpty(item.getPluginConfigurations()))
							.on(DefinitionPersist._pluginConfigurations)
							.over(item.getPluginConfigurations())
							.using((itm) -> this.validatorFactory.validator(PluginConfigurationPersist.PluginConfigurationPersistValidator.class)),
					this.refSpec()
							.iff(() -> !this.isNull(item.getPlanCreationConsent()))
							.on(DefinitionPersist._planCreationConsent)
							.over(item.getPlanCreationConsent())
							.using(() -> this.validatorFactory.validator(PlanCreationConsentPersist.PlanCreationConsentPersistValidator.class)),
					this.spec()
							.must(() -> this.hasDescriptionTemplates(item.getSections()))
							.failOn(DefinitionPersist._sections).failWith(messageSource.getMessage("Validation_Required", new Object[]{DefinitionPersist._sections}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> this.hasSystemField(item, SystemFieldType.PlanTitle) && this.hasSystemField(item, SystemFieldType.PlanDescription) && this.hasSystemField(item, SystemFieldType.PlanLanguage) && this.hasSystemField(item, SystemFieldType.PlanAccessRights))
							.failOn(DefinitionPersist._sections).failWith(messageSource.getMessage("Validation_BlueprintSystemFieldsRequired", new Object[]{DefinitionPersist._sections}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> this.hasNoDuplicateSystemFieldTypes(item))
							.failOn(DefinitionPersist._sections).failWith(messageSource.getMessage("Validation_DuplicateSystemFieldTypes", new Object[]{DefinitionPersist._sections}, LocaleContextHolder.getLocale()))
			);
		}

		private boolean hasDescriptionTemplates(List<RootSectionPersist> sections) {
			if (this.isListNullOrEmpty(sections)) return false;
			return sections.stream().anyMatch(x -> x.getHasTemplates() != null && x.getHasTemplates().equals(true));
		}

		private boolean hasSystemField(DefinitionPersist item, SystemFieldType systemFieldType) {
			if (this.isListNullOrEmpty(item.getSections()) || this.isListNullOrEmpty(item.getAllField())) return false;

			List<FieldPersist> systemFields = item.getAllField().stream().filter(x -> x.getData() != null && x.getData().getFieldType() != null && x.getData().getFieldType().equals(FieldType.SYSTEM)).toList();
			if (this.isListNullOrEmpty(systemFields)) return false;

			return systemFields.stream()
					.map(field -> (SystemFieldDataPersist) field.getData())
					.anyMatch(systemField ->
							systemField != null && Objects.equals(systemField.getSystemFieldType(), systemFieldType)
					);
		}

		private boolean hasNoDuplicateSystemFieldTypes(DefinitionPersist item) {
			if (item == null || item.getAllField() == null) return true;

			EnumSet<SystemFieldType> seen = EnumSet.noneOf(SystemFieldType.class);

			for (FieldPersist field : item.getAllField()) {
				if (field == null || !(field.getData() instanceof SystemFieldDataPersist systemField)) {
					continue;
				}

				SystemFieldType type = systemField.getSystemFieldType();

				if (type == null || !seen.add(type)) {
					return false;
				}
			}

			return true;
		}


	}

}
