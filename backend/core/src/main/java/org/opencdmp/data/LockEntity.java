package org.opencdmp.data;

import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.data.converters.enums.LockTargetTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"Lock\"")
public class LockEntity extends TenantScopedBaseEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    public static final String _id = "id";

    @Column(name = "target", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID target;
    public static final String _target = "target";

    @Column(name = "target_type", nullable = false)
    @Convert(converter = LockTargetTypeConverter.class)
    private LockTargetType targetType;
    public static final String _targetType = "targetType";


    @Column(name = "locked_by", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID lockedBy;
    public static final String _lockedBy = "lockedBy";


    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt = null;
    public static final String _lockedAt = "lockedAt";

    @Column(name = "touched_at", nullable = true)
    private Instant touchedAt;
    public static final String _touchedAt = "touchedAt";


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

    public UUID getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(UUID lockedBy) {
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
}
