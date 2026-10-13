package org.opencdmp.model.maintenance;

import org.opencdmp.commons.enums.MaintenanceTask;
import org.opencdmp.commons.enums.MaintenanceTaskRunStatus;

import java.time.Instant;

public class MaintenanceTaskRun {

    private MaintenanceTask task;
    public static final String _task = "task";

    private MaintenanceTaskRunStatus status;
    public static final String _status = "status";

    private Instant startedAt;
    public static final String _startedAt = "startedAt";

    private Instant finishedAt;
    public static final String _finishedAt = "finishedAt";

    private String error;
    public static final String _error = "error";

    public MaintenanceTask getTask() {
        return task;
    }

    public void setTask(MaintenanceTask task) {
        this.task = task;
    }

    public MaintenanceTaskRunStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceTaskRunStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
