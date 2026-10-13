package org.opencdmp.data;

import jakarta.persistence.*;
import org.opencdmp.commons.enums.MaintenanceTaskRunStatus;
import org.opencdmp.data.converters.enums.MaintenanceTaskRunStatusConverter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"MaintenanceTaskRun\"")
public class MaintenanceTaskRunEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "run", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID run;
    public static final String _run = "run";

    @Column(name = "task", length = MaintenanceTaskRunEntity._taskLength, updatable = false, nullable = false)
    private String task;
    public static final String _task = "task";
    public static final int _taskLength = 100;

    @Column(name = "ordinal", updatable = false, nullable = false)
    private Integer ordinal;
    public static final String _ordinal = "ordinal";

    @Column(name = "status", nullable = false)
    @Convert(converter = MaintenanceTaskRunStatusConverter.class)
    private MaintenanceTaskRunStatus status;
    public static final String _status = "status";

    @Column(name = "started_at")
    private Instant startedAt;
    public static final String _startedAt = "startedAt";

    @Column(name = "finished_at")
    private Instant finishedAt;
    public static final String _finishedAt = "finishedAt";

    @Column(name = "error")
    private String error;
    public static final String _error = "error";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRun() {
        return run;
    }

    public void setRun(UUID run) {
        this.run = run;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
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
