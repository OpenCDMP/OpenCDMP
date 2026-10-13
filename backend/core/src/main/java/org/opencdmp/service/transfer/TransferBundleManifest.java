package org.opencdmp.service.transfer;

import java.time.Instant;
import java.util.Map;

public class TransferBundleManifest {

    public static final int CURRENT_FORMAT_VERSION = 1;

    private int bundleFormatVersion;

    private String sourceTenantCode;

    private String sourceDbVersion;

    private Instant exportedAt;

    private Map<String, Integer> entityCounts;

    private int fileCount;

    public int getBundleFormatVersion() {
        return this.bundleFormatVersion;
    }

    public void setBundleFormatVersion(int bundleFormatVersion) {
        this.bundleFormatVersion = bundleFormatVersion;
    }

    public String getSourceTenantCode() {
        return this.sourceTenantCode;
    }

    public void setSourceTenantCode(String sourceTenantCode) {
        this.sourceTenantCode = sourceTenantCode;
    }

    public String getSourceDbVersion() {
        return this.sourceDbVersion;
    }

    public void setSourceDbVersion(String sourceDbVersion) {
        this.sourceDbVersion = sourceDbVersion;
    }

    public Instant getExportedAt() {
        return this.exportedAt;
    }

    public void setExportedAt(Instant exportedAt) {
        this.exportedAt = exportedAt;
    }

    public Map<String, Integer> getEntityCounts() {
        return this.entityCounts;
    }

    public void setEntityCounts(Map<String, Integer> entityCounts) {
        this.entityCounts = entityCounts;
    }

    public int getFileCount() {
        return this.fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
}
