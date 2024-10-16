package org.opencdmp.data;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.UsageLimitTargetMetricConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.data.types.SQLXMLType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UsageLimit\"")
public class UsageLimitEntity extends TenantScopedBaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    public static final String _id = "id";

    @Column(name = "label", length = _labelLength, nullable = false)
    private String label;

    public static final String _label = "label";

    public static final int _labelLength = 250;


    @Column(name = "target_metric", nullable = false)
    @Convert(converter = UsageLimitTargetMetricConverter.class)
    private UsageLimitTargetMetric targetMetric;

    public static final String _targetMetric = "targetMetric";

    @Column(name = "value", nullable = false)
    private Long value;

    public static final String _value = "value";

    @Type(SQLXMLType.class)
    @Column(name = "definition", columnDefinition = "xml")
    private String definition;

    public static final String _definition = "definition";

    @Column(name = "is_active", nullable = false)
    @Convert(converter = IsActiveConverter.class)
    private IsActive isActive;

    public static final String _isActive = "isActive";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static final String _createdAt = "createdAt";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static final String _updatedAt = "updatedAt";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UsageLimitTargetMetric getTargetMetric() {
        return targetMetric;
    }

    public void setTargetMetric(UsageLimitTargetMetric targetMetric) {
        this.targetMetric = targetMetric;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public IsActive getIsActive() {
        return isActive;
    }

    public void setIsActive(IsActive isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
