package org.opencdmp.model.persist.tenantconfiguration;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.filetransformer.FileTransformerSourcePersist;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class FileTransformerTenantConfigurationPersist {

	private List<FileTransformerSourcePersist> sources;
	public static final String _sources = "sources";
	private Boolean disableSystemSources;
	public static final String _disableSystemSources = "disableSystemSources";

	public List<FileTransformerSourcePersist> getSources() {
		return sources;
	}

	public void setSources(List<FileTransformerSourcePersist> sources) {
		this.sources = sources;
	}

	public Boolean getDisableSystemSources() {
		return disableSystemSources;
	}

	public void setDisableSystemSources(Boolean disableSystemSources) {
		this.disableSystemSources = disableSystemSources;
	}

	@Component(FileTransformerTenantConfigurationPersist.FileTransformerTenantConfigurationPersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class FileTransformerTenantConfigurationPersistValidator extends BaseValidator<FileTransformerTenantConfigurationPersist> {

		public static final String ValidatorName = "FileTransformerTenantConfigurationPersistValidator";

		private final MessageSource messageSource;

		private final ValidatorFactory validatorFactory;


		protected FileTransformerTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
			super(conventionService, errors);
			this.messageSource = messageSource;
			this.validatorFactory = validatorFactory;
		}

		@Override
		protected Class<FileTransformerTenantConfigurationPersist> modelClass() {
			return FileTransformerTenantConfigurationPersist.class;
		}

		@Override
		protected List<Specification> specifications(FileTransformerTenantConfigurationPersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isNull(item.getDisableSystemSources()))
							.failOn(FileTransformerTenantConfigurationPersist._disableSystemSources).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerTenantConfigurationPersist._disableSystemSources}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isListNullOrEmpty(item.getSources()))
							.failOn(FileTransformerTenantConfigurationPersist._sources).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerTenantConfigurationPersist._sources}, LocaleContextHolder.getLocale())),
					this.navSpec()
							.iff(() -> !this.isListNullOrEmpty(item.getSources()))
							.on(FileTransformerTenantConfigurationPersist._sources)
							.over(item.getSources())
							.using((itm) -> this.validatorFactory.validator(FileTransformerSourcePersist.FileTransformerSourcePersistValidator.class))
			);
		}
	}
}
