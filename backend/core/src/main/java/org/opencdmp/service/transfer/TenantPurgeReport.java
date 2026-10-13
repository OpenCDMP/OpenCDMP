package org.opencdmp.service.transfer;

import java.util.ArrayList;
import java.util.List;

public class TenantPurgeReport {

    private boolean applied;

    private String tenantCode;

    private List<TypeReport> types = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();

    public boolean isApplied() {
        return this.applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public String getTenantCode() {
        return this.tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public List<TypeReport> getTypes() {
        return this.types;
    }

    public void setTypes(List<TypeReport> types) {
        this.types = types;
    }

    public List<String> getWarnings() {
        return this.warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void addType(String entityType, int toDelete, int kept) {
        if (toDelete == 0 && kept == 0) return;
        TypeReport report = new TypeReport();
        report.setEntityType(entityType);
        report.setToDelete(toDelete);
        report.setKept(kept);
        this.types.add(report);
    }

    public static class TypeReport {

        private String entityType;

        private int toDelete;

        private int kept;

        public String getEntityType() {
            return this.entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public int getToDelete() {
            return this.toDelete;
        }

        public void setToDelete(int toDelete) {
            this.toDelete = toDelete;
        }

        public int getKept() {
            return this.kept;
        }

        public void setKept(int kept) {
            this.kept = kept;
        }
    }
}
