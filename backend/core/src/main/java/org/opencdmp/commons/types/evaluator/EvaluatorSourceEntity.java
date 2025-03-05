package org.opencdmp.commons.types.evaluator;

public class EvaluatorSourceEntity {

    private String url;
    private String evaluatorId;
    private String issuerUrl;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String rdaTransformerId;
    private int maxInMemorySizeInBytes;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
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

    public String getRdaTransformerId() {
        return rdaTransformerId;
    }

    public void setRdaTransformerId(String rdaTransformerId) {
        this.rdaTransformerId = rdaTransformerId;
    }

    public int getMaxInMemorySizeInBytes() {
        return maxInMemorySizeInBytes;
    }

    public void setMaxInMemorySizeInBytes(int maxInMemorySizeInBytes) {
        this.maxInMemorySizeInBytes = maxInMemorySizeInBytes;
    }
}
