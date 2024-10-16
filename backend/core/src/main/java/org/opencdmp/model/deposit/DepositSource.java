package org.opencdmp.model.deposit;

public class DepositSource {

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
}
