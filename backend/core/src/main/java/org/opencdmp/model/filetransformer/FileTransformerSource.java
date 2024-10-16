package org.opencdmp.model.filetransformer;

public class FileTransformerSource {

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
}