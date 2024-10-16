package org.opencdmp.model.persist.tenantconfiguration;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class DefaultUserLocaleTenantConfigurationPersist {
	private String timezone;
	public static final String _timezone = "timezone";
	private String language;
	public static final String _language = "language";
	private String culture;
	public static final String _culture = "culture";

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	@Component(DefaultUserLocaleTenantConfigurationPersist.DefaultUserLocaleTenantConfigurationPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class DefaultUserLocaleTenantConfigurationPersistValidator extends BaseValidator<DefaultUserLocaleTenantConfigurationPersist> {

		public static final String ValidatorName = "DefaultUserLocaleTenantConfigurationPersistValidator";

		private final MessageSource messageSource;

		protected DefaultUserLocaleTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
			this.messageSource = messageSource;
		}

		@Override
		protected Class<DefaultUserLocaleTenantConfigurationPersist> modelClass() {
			return DefaultUserLocaleTenantConfigurationPersist.class;
		}

		@Override
		protected List<Specification> specifications(DefaultUserLocaleTenantConfigurationPersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isEmpty(item.getTimezone()))
							.failOn(DefaultUserLocaleTenantConfigurationPersist._timezone).failWith(messageSource.getMessage("Validation_Required", new Object[]{DefaultUserLocaleTenantConfigurationPersist._timezone}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getLanguage()))
							.failOn(DefaultUserLocaleTenantConfigurationPersist._language).failWith(messageSource.getMessage("Validation_Required", new Object[]{DefaultUserLocaleTenantConfigurationPersist._language}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getCulture()))
							.failOn(DefaultUserLocaleTenantConfigurationPersist._culture).failWith(messageSource.getMessage("Validation_Required", new Object[]{DefaultUserLocaleTenantConfigurationPersist._culture}, LocaleContextHolder.getLocale()))
			);
		}
	}
}

