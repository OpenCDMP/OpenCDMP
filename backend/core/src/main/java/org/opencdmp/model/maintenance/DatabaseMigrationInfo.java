package org.opencdmp.model.maintenance;

import java.util.List;

public class DatabaseMigrationInfo {

    private Boolean enabled;
    public static final String _enabled = "enabled";

    private String currentVersion;
    public static final String _currentVersion = "currentVersion";

    private Integer applied;
    public static final String _applied = "applied";

    private Integer pending;
    public static final String _pending = "pending";

    private Integer failed;
    public static final String _failed = "failed";

    private List<DatabaseMigration> migrations;
    public static final String _migrations = "migrations";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Integer getApplied() {
        return applied;
    }

    public void setApplied(Integer applied) {
        this.applied = applied;
    }

    public Integer getPending() {
        return pending;
    }

    public void setPending(Integer pending) {
        this.pending = pending;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public List<DatabaseMigration> getMigrations() {
        return migrations;
    }

    public void setMigrations(List<DatabaseMigration> migrations) {
        this.migrations = migrations;
    }
}
