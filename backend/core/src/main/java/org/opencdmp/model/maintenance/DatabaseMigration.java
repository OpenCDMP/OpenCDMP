package org.opencdmp.model.maintenance;

import java.time.Instant;

public class DatabaseMigration {

    private String version;
    public static final String _version = "version";

    private String description;
    public static final String _description = "description";

    private String type;
    public static final String _type = "type";

    private Instant installedOn;
    public static final String _installedOn = "installedOn";

    private Integer executionTime;
    public static final String _executionTime = "executionTime";

    private String state;
    public static final String _state = "state";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getInstalledOn() {
        return installedOn;
    }

    public void setInstalledOn(Instant installedOn) {
        this.installedOn = installedOn;
    }

    public Integer getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Integer executionTime) {
        this.executionTime = executionTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
