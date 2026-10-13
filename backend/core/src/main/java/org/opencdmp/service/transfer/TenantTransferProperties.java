package org.opencdmp.service.transfer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tenant-transfer")
public class TenantTransferProperties {

    private String annotationServiceUrl;

    public String getAnnotationServiceUrl() {
        return this.annotationServiceUrl;
    }

    public void setAnnotationServiceUrl(String annotationServiceUrl) {
        this.annotationServiceUrl = annotationServiceUrl;
    }
}
