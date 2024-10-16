package org.opencdmp.data.converters.enums;

import gr.cite.queueinbox.entity.QueueInboxStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class QueueInboxStatusConverter implements AttributeConverter<QueueInboxStatus, Short> {
    @Override
    public Short convertToDatabaseColumn(QueueInboxStatus value) {
        if (value == null)  throw new IllegalArgumentException("Value could not be null for: " + this.getClass().getSimpleName());
        return value.getValue();
    }

    @Override
    public QueueInboxStatus convertToEntityAttribute(Short dbData) {
        return QueueInboxStatus.of(dbData);
    }
}
