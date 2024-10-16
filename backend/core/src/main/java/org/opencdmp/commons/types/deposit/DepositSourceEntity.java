package org.opencdmp.commons.types.deposit;

public class DepositSourceEntity {

	private String repositoryId;
	private String url;
	private String issuerUrl;
	private String clientId;
	private String clientSecret;
	private String scope;
	private String pdfTransformerId;
	private String rdaTransformerId;
	private int maxInMemorySizeInBytes;

	public String getRepositoryId() {
		return this.repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIssuerUrl() {
		return this.issuerUrl;
	}

	public void setIssuerUrl(String issuerUrl) {
		this.issuerUrl = issuerUrl;
	}

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return this.clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getScope() {
		return this.scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getPdfTransformerId() {
		return this.pdfTransformerId;
	}

	public void setPdfTransformerId(String pdfTransformerId) {
		this.pdfTransformerId = pdfTransformerId;
	}

	public String getRdaTransformerId() {
		return this.rdaTransformerId;
	}

	public void setRdaTransformerId(String rdaTransformerId) {
		this.rdaTransformerId = rdaTransformerId;
	}

	public int getMaxInMemorySizeInBytes() {
		return this.maxInMemorySizeInBytes;
	}

	public void setMaxInMemorySizeInBytes(int maxInMemorySizeInBytes) {
		this.maxInMemorySizeInBytes = maxInMemorySizeInBytes;
	}
}
