package org.opencdmp.data.converters.enums;

import gr.cite.queueoutbox.entity.QueueOutboxNotifyStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class QueueOutboxNotifyStatusConverter implements AttributeConverter<QueueOutboxNotifyStatus, Short> {
    @Override
    public Short convertToDatabaseColumn(QueueOutboxNotifyStatus value) {
        if (value == null)  throw new IllegalArgumentException("Value could not be null for: " + this.getClass().getSimpleName());
        return value.getValue();
    }

    @Override
    public QueueOutboxNotifyStatus convertToEntityAttribute(Short dbData) {
        return QueueOutboxNotifyStatus.of(dbData);
    }
}
