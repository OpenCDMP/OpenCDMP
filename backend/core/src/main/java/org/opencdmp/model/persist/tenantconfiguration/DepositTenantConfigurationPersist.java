package org.opencdmp.model.persist.tenantconfiguration;

import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.deposit.DepositSourcePersist;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class DepositTenantConfigurationPersist {
	private List<DepositSourcePersist> sources;
	public static final String _sources = "sources";
	private Boolean disableSystemSources;
	public static final String _disableSystemSources = "disableSystemSources";
	private List<String> disabledSourceIds;
	public static final String _disabledSourceIds = "disabledSourceIds";

	public List<DepositSourcePersist> getSources() {
		return sources;
	}

	public void setSources(List<DepositSourcePersist> sources) {
		this.sources = sources;
	}

	public Boolean getDisableSystemSources() {
		return disableSystemSources;
	}

	public void setDisableSystemSources(Boolean disableSystemSources) {
		this.disableSystemSources = disableSystemSources;
	}

	public List<String> getDisabledSourceIds() {
		return disabledSourceIds;
	}

	public void setDisabledSourceIds(List<String> disabledSourceIds) {
		this.disabledSourceIds = disabledSourceIds;
	}

	@Component(DepositTenantConfigurationPersist.DepositTenantConfigurationPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class DepositTenantConfigurationPersistValidator extends BaseValidator<DepositTenantConfigurationPersist> {

		public static final String ValidatorName = "DepositTenantConfigurationPersistValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;


		protected DepositTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected Class<DepositTenantConfigurationPersist> modelClass() {
			return DepositTenantConfigurationPersist.class;
		}

		@Override
		protected List<Specification> specifications(DepositTenantConfigurationPersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isNull(item.getDisableSystemSources()))
							.failOn(DepositTenantConfigurationPersist._disableSystemSources).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositTenantConfigurationPersist._disableSystemSources}, LocaleContextHolder.getLocale())),
					this.navSpec()
							.iff(() -> !this.isListNullOrEmpty(item.getSources()))
							.on(DepositTenantConfigurationPersist._sources)
							.over(item.getSources())
							.using((itm) -> this.validatorFactory.validator(DepositSourcePersist.DepositSourcePersistValidator.class))
			);
		}
	}
}
