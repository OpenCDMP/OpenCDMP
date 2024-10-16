package org.opencdmp.model.persist.deposit;

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

public class DepositSourcePersist {

	private String repositoryId;
	public static final String _repositoryId = "repositoryId";
	private String url;
	public static final String _url = "url";
	private String issuerUrl;
	public static final String _issuerUrl = "issuerUrl";
	private String clientId;
	public static final String _clientId = "clientId";
	private String clientSecret;
	public static final String _clientSecret = "clientSecret";
	private String scope;
	public static final String _scope = "scope";
	private String pdfTransformerId;
	public static final String _pdfTransformerId = "pdfTransformerId";
	private String rdaTransformerId;
	public static final String _rdaTransformerId = "rdaTransformerId";

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public String getPdfTransformerId() {
		return pdfTransformerId;
	}

	public void setPdfTransformerId(String pdfTransformerId) {
		this.pdfTransformerId = pdfTransformerId;
	}

	public String getRdaTransformerId() {
		return rdaTransformerId;
	}

	public void setRdaTransformerId(String rdaTransformerId) {
		this.rdaTransformerId = rdaTransformerId;
	}

	@Component(DepositSourcePersistValidator.ValidatorName)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class DepositSourcePersistValidator extends BaseValidator<DepositSourcePersist> {

		public static final String ValidatorName = "DepositSourcePersistValidator";

		private final MessageSource messageSource;

		protected DepositSourcePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
			super(conventionService, errors);
			this.messageSource = messageSource;
		}

		@Override
		protected Class<DepositSourcePersist> modelClass() {
			return DepositSourcePersist.class;
		}

		@Override
		protected List<Specification> specifications(DepositSourcePersist item) {
			return Arrays.asList(
					this.spec()
							.must(() -> !this.isEmpty(item.getRepositoryId()))
							.failOn(DepositSourcePersist._repositoryId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._repositoryId}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getUrl()))
							.failOn(DepositSourcePersist._url).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._url}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getIssuerUrl()))
							.failOn(DepositSourcePersist._issuerUrl).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._issuerUrl}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getClientId()))
							.failOn(DepositSourcePersist._clientId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._clientId}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getClientSecret()))
							.failOn(DepositSourcePersist._clientSecret).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._clientSecret}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getScope()))
							.failOn(DepositSourcePersist._scope).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._scope}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getPdfTransformerId()))
							.failOn(DepositSourcePersist._pdfTransformerId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._pdfTransformerId}, LocaleContextHolder.getLocale())),
					this.spec()
							.must(() -> !this.isEmpty(item.getRdaTransformerId()))
							.failOn(DepositSourcePersist._rdaTransformerId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DepositSourcePersist._rdaTransformerId}, LocaleContextHolder.getLocale()))
			);
		}
	}
}
