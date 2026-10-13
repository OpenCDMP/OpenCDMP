package org.opencdmp.data;

import jakarta.persistence.*;
import org.opencdmp.commons.enums.MaintenanceRunStatus;
import org.opencdmp.data.converters.enums.MaintenanceRunStatusConverter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"MaintenanceRun\"")
public class MaintenanceRunEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "preset", length = MaintenanceRunEntity._presetLength)
    private String preset;
    public static final String _preset = "preset";
    public static final int _presetLength = 100;

    @Column(name = "status", nullable = false)
    @Convert(converter = MaintenanceRunStatusConverter.class)
    private MaintenanceRunStatus status;
    public static final String _status = "status";

    @Column(name = "stop_on_failure", nullable = false)
    private Boolean stopOnFailure;
    public static final String _stopOnFailure = "stopOnFailure";

    @Column(name = "stop_requested", nullable = false)
    private Boolean stopRequested;
    public static final String _stopRequested = "stopRequested";

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;
    public static final String _createdBy = "createdBy";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    public static final String _createdAt = "createdAt";

    @Column(name = "finished_at")
    private Instant finishedAt;
    public static final String _finishedAt = "finishedAt";

    @Column(name = "heartbeat_at", nullable = false)
    private Instant heartbeatAt;
    public static final String _heartbeatAt = "heartbeatAt";

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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
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

    public Instant getHeartbeatAt() {
        return heartbeatAt;
    }

    public void setHeartbeatAt(Instant heartbeatAt) {
        this.heartbeatAt = heartbeatAt;
    }
}
