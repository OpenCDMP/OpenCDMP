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
	private String primaryColor2;
	public static final String _primaryColor2 = "primaryColor2";
	private String primaryColor3;
	public static final String _primaryColor3 = "primaryColor3";
	private String secondaryColor;
	public static final String _secondaryColor = "secondaryColor";

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public String getPrimaryColor2() {
		return primaryColor2;
	}

	public void setPrimaryColor2(String primaryColor2) {
		this.primaryColor2 = primaryColor2;
	}

	public String getPrimaryColor3() {
		return primaryColor3;
	}

	public void setPrimaryColor3(String primaryColor3) {
		this.primaryColor3 = primaryColor3;
	}

	public String getSecondaryColor() {
		return secondaryColor;
	}

	public void setSecondaryColor(String secondaryColor) {
		this.secondaryColor = secondaryColor;
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
					this.spec()
							.must(() -> !this.isEmpty(item.getPrimaryColor()))
							.failOn(CssColorsTenantConfigurationPersist._primaryColor).failWith(messageSource.getMessage("Validation_Required", new Object[]{CssColorsTenantConfigurationPersist._primaryColor}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getPrimaryColor2()))
							.failOn(CssColorsTenantConfigurationPersist._primaryColor2).failWith(messageSource.getMessage("Validation_Required", new Object[]{CssColorsTenantConfigurationPersist._primaryColor2}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getPrimaryColor3()))
							.failOn(CssColorsTenantConfigurationPersist._primaryColor3).failWith(messageSource.getMessage("Validation_Required", new Object[]{CssColorsTenantConfigurationPersist._primaryColor3}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getSecondaryColor()))
							.failOn(CssColorsTenantConfigurationPersist._secondaryColor).failWith(messageSource.getMessage("Validation_Required", new Object[]{CssColorsTenantConfigurationPersist._secondaryColor}, LocaleContextHolder.getLocale()))
			);
		}
	}
}
