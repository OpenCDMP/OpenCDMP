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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LogoTenantConfigurationPersist {
	private UUID storageFileId;
	public static final String _storageFileId = "storageFileId";

	public UUID getStorageFileId() {
		return storageFileId;
	}

	public void setStorageFileId(UUID storageFileId) {
		this.storageFileId = storageFileId;
	}

	@Component(LogoTenantConfigurationPersist.LogoTenantConfigurationPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class LogoTenantConfigurationPersistValidator extends BaseValidator<LogoTenantConfigurationPersist> {

		public static final String ValidatorName = "LogoTenantConfigurationPersistValidator";

		private final MessageSource messageSource;

		protected LogoTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
			this.messageSource = messageSource;
		}

		@Override
		protected Class<LogoTenantConfigurationPersist> modelClass() {
			return LogoTenantConfigurationPersist.class;
		}

		@Override
		protected List<Specification> specifications(LogoTenantConfigurationPersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> this.isValidGuid(item.getStorageFileId()))
							.failOn(LogoTenantConfigurationPersist._storageFileId).failWith(messageSource.getMessage("Validation_Required", new Object[]{LogoTenantConfigurationPersist._storageFileId}, LocaleContextHolder.getLocale()))
			);
		}
	}
}
