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

public class CssColorsTenantConfigurationPersist {
	private String primaryColor;
	public static final String _primaryColor = "primaryColor";
	private String cssOverride;
	public static final String _cssOverride = "cssOverride";

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public String getCssOverride() {
		return cssOverride;
	}

	public void setCssOverride(String cssOverride) {
		this.cssOverride = cssOverride;
	}

	@Component(CssColorsTenantConfigurationPersist.CssColorsTenantConfigurationPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class CssColorsTenantConfigurationPersistValidator extends BaseValidator<CssColorsTenantConfigurationPersist> {

		public static final String ValidatorName = "CssColorsTenantConfigurationPersistValidator";

		private final MessageSource messageSource;

		protected CssColorsTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
			this.messageSource = messageSource;
		}

		@Override
		protected Class<CssColorsTenantConfigurationPersist> modelClass() {
			return CssColorsTenantConfigurationPersist.class;
		}

		@Override
		protected List<Specification> specifications(CssColorsTenantConfigurationPersist item) {
			return Arrays.asList(
			);
		}
	}
}
