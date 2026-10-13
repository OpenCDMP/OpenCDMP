package org.opencdmp.service.transfer;

import java.util.ArrayList;
import java.util.List;

public class TransferImportReport {

    private boolean applied;

    private String sourceTenantCode;

    private List<TypeReport> types = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();

    public boolean isApplied() {
        return this.applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public String getSourceTenantCode() {
        return this.sourceTenantCode;
    }

    public void setSourceTenantCode(String sourceTenantCode) {
        this.sourceTenantCode = sourceTenantCode;
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

    public TypeReport typeReport(TransferEntityType type) {
        for (TypeReport item : this.types) {
            if (item.getEntityType().equals(type.name())) return item;
        }
        TypeReport report = new TypeReport();
        report.setEntityType(type.name());
        this.types.add(report);
        return report;
    }

    public static class TypeReport {

        private String entityType;

        private int total;

        private int toCreate;

        private int matchedById;

        private int matchedByKey;

        private int conflicts;

        private List<String> messages = new ArrayList<>();

        public String getEntityType() {
            return this.entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public int getTotal() {
            return this.total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getToCreate() {
            return this.toCreate;
        }

        public void setToCreate(int toCreate) {
            this.toCreate = toCreate;
        }

        public int getMatchedById() {
            return this.matchedById;
        }

        public void setMatchedById(int matchedById) {
            this.matchedById = matchedById;
        }

        public int getMatchedByKey() {
            return this.matchedByKey;
        }

        public void setMatchedByKey(int matchedByKey) {
            this.matchedByKey = matchedByKey;
        }

        public int getConflicts() {
            return this.conflicts;
        }

        public void setConflicts(int conflicts) {
            this.conflicts = conflicts;
        }

        public List<String> getMessages() {
            return this.messages;
        }

        public void setMessages(List<String> messages) {
            this.messages = messages;
        }
    }
}
