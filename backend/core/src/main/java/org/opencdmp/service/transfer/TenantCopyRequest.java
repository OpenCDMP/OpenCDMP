package org.opencdmp.service.transfer;

/**
 * Same-installation copy: same selection options as an export, applied from the source tenant
 * directly into the current (target) tenant with fresh identities.
 */
public class TenantCopyRequest extends TransferExportRequest {

    private String sourceTenantCode;

    public String getSourceTenantCode() {
        return this.sourceTenantCode;
    }

    public void setSourceTenantCode(String sourceTenantCode) {
        this.sourceTenantCode = sourceTenantCode;
    }
}
