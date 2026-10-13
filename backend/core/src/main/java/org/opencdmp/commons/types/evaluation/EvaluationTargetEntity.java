package org.opencdmp.commons.types.evaluation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.commons.enums.EntityType;

import java.util.UUID;

/**
 * Which part of the evaluated entity a rank result is about, as reported by the evaluator
 * plugin and verified by us before it was stored.
 * <p>
 * The label is denormalised on purpose. An evaluation is a point-in-time record, so it has
 * to keep rendering after the description is renamed or removed, and re-resolving on read
 * would either change history or break the dialog.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EvaluationTargetEntity {

    @XmlElement(name = "entityType")
    private EntityType entityType;
    public static final String _entityType = "entityType";

    @XmlElement(name = "entityId")
    private UUID entityId;
    public static final String _entityId = "entityId";

    @XmlElement(name = "label")
    private String label;
    public static final String _label = "label";

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
