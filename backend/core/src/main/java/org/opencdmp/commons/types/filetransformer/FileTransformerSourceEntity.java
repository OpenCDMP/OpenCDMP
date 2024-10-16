package org.opencdmp.commons.types.filetransformer;

public class FileTransformerSourceEntity {

	private String url;
	private String transformerId;
	private String issuerUrl;
	private String clientId;
	private String clientSecret;
	private String scope;
	private int maxInMemorySizeInBytes;

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTransformerId() {
		return this.transformerId;
	}

	public void setTransformerId(String transformerId) {
		this.transformerId = transformerId;
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

	public int getMaxInMemorySizeInBytes() {
		return this.maxInMemorySizeInBytes;
	}

	public void setMaxInMemorySizeInBytes(int maxInMemorySizeInBytes) {
		this.maxInMemorySizeInBytes = maxInMemorySizeInBytes;
	}
}