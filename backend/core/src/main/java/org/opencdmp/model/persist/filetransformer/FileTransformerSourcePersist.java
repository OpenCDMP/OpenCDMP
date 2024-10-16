package org.opencdmp.model.persist.filetransformer;

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

public class FileTransformerSourcePersist {

	private String url;
	public static final String _url = "url";
	private String transformerId;
	public static final String _transformerId = "transformerId";
	private String issuerUrl;
	public static final String _issuerUrl = "issuerUrl";
	private String clientId;
	public static final String _clientId = "clientId";
	private String clientSecret;
	public static final String _clientSecret = "clientSecret";
	private String scope;
	public static final String _scope = "scope";

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTransformerId() {
		return transformerId;
	}

	public void setTransformerId(String transformerId) {
		this.transformerId = transformerId;
	}

	public String getIssuerUrl() {
		return issuerUrl;
	}

	public void setIssuerUrl(String issuerUrl) {
		this.issuerUrl = issuerUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@Component(FileTransformerSourcePersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class FileTransformerSourcePersistValidator extends BaseValidator<FileTransformerSourcePersist> {

		public static final String ValidatorName = "FileTransformerSourcePersistValidator";

		private final MessageSource messageSource;

		protected FileTransformerSourcePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
			this.messageSource = messageSource;
		}

		@Override
		protected Class<FileTransformerSourcePersist> modelClass() {
			return FileTransformerSourcePersist.class;
		}

		@Override
		protected List<Specification> specifications(FileTransformerSourcePersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isEmpty(item.getTransformerId()))
							.failOn(FileTransformerSourcePersist._transformerId).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerSourcePersist._transformerId}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getUrl()))
							.failOn(FileTransformerSourcePersist._url).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerSourcePersist._url}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getIssuerUrl()))
							.failOn(FileTransformerSourcePersist._issuerUrl).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerSourcePersist._issuerUrl}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getClientId()))
							.failOn(FileTransformerSourcePersist._clientId).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerSourcePersist._clientId}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getClientSecret()))
							.failOn(FileTransformerSourcePersist._clientSecret).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerSourcePersist._clientSecret}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getScope()))
							.failOn(FileTransformerSourcePersist._scope).failWith(messageSource.getMessage("Validation_Required", new Object[]{FileTransformerSourcePersist._scope}, LocaleContextHolder.getLocale()))
			);
		}
	}
}