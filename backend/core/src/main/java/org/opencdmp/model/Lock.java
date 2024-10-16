package org.opencdmp.model;

import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class Lock {

    private UUID id;
    public static final String _id = "id";

    private UUID target;
    public static final String _target = "target";

    private LockTargetType targetType;
    public static final String _targetType = "targetType";

    private User lockedBy;
    public static final String _lockedBy = "lockedBy";

    private Instant lockedAt;
    public static final String _lockedAt = "lockedAt";

    private Instant touchedAt;
    public static final String _touchedAt = "touchedAt";

    private String hash;

    public static final String _hash = "hash";

    private Boolean belongsToCurrentTenant;
    public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTarget() {
        return target;
    }

    public void setTarget(UUID target) {
        this.target = target;
    }

    public LockTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(LockTargetType targetType) {
        this.targetType = targetType;
    }

    public User getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(User lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Instant getTouchedAt() {
        return touchedAt;
    }

    public void setTouchedAt(Instant touchedAt) {
        this.touchedAt = touchedAt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getBelongsToCurrentTenant() {
        return belongsToCurrentTenant;
    }

    public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
        this.belongsToCurrentTenant = belongsToCurrentTenant;
    }
}
