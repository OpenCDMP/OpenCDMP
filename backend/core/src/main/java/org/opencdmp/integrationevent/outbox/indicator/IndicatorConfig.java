package org.opencdmp.integrationevent.outbox.indicator;


import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class IndicatorConfig {

	private AccessRequestConfig accessRequestConfig;
	public static final String _accessRequestConfig = "accessRequestConfig";

	public AccessRequestConfig getAccessRequestConfig() {
		return accessRequestConfig;
	}

	public void setAccessRequestConfig(AccessRequestConfig accessRequestConfig) {
		this.accessRequestConfig = accessRequestConfig;
	}

	@Component(IndicatorConfig.IndicatorConfigValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class IndicatorConfigValidator extends BaseValidator<IndicatorConfig> {

		public static final String ValidatorName = "IndicatorConfigValidator";

		private final MessageSource messageSource;
		private final ValidatorFactory validatorFactory;


		protected IndicatorConfigValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected Class<IndicatorConfig> modelClass() {
			return IndicatorConfig.class;
		}

		@Override
		protected List<Specification> specifications(IndicatorConfig item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isNull(item.getAccessRequestConfig()))
							.failOn(IndicatorConfig._accessRequestConfig).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{IndicatorConfig._accessRequestConfig}, LocaleContextHolder.getLocale())),
					this.refSpec()
							.iff(() -> !this.isNull(item.getAccessRequestConfig()))
							.on(IndicatorConfig._accessRequestConfig)
							.over(item.getAccessRequestConfig())
							.using(() -> this.validatorFactory.validator(AccessRequestConfig.AccessRequestConfigValidator.class))
			);
		}
	}
}
