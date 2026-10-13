package org.opencdmp.model.maintenance;

import org.opencdmp.commons.enums.MaintenanceRunStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MaintenanceRun {

    private UUID id;
    public static final String _id = "id";

    private String preset;
    public static final String _preset = "preset";

    private MaintenanceRunStatus status;
    public static final String _status = "status";

    private Boolean stopOnFailure;
    public static final String _stopOnFailure = "stopOnFailure";

    private Boolean stopRequested;
    public static final String _stopRequested = "stopRequested";

    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    private Instant finishedAt;
    public static final String _finishedAt = "finishedAt";

    private List<MaintenanceTaskRun> tasks;
    public static final String _tasks = "tasks";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public MaintenanceRunStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceRunStatus status) {
        this.status = status;
    }

    public Boolean getStopOnFailure() {
        return stopOnFailure;
    }

    public void setStopOnFailure(Boolean stopOnFailure) {
        this.stopOnFailure = stopOnFailure;
    }

    public Boolean getStopRequested() {
        return stopRequested;
    }

    public void setStopRequested(Boolean stopRequested) {
        this.stopRequested = stopRequested;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public List<MaintenanceTaskRun> getTasks() {
        return tasks;
    }

    public void setTasks(List<MaintenanceTaskRun> tasks) {
        this.tasks = tasks;
    }
}
