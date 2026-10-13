package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.MaintenanceTaskRunStatus;

@Converter
public class MaintenanceTaskRunStatusConverter extends DatabaseEnumConverter<MaintenanceTaskRunStatus, Short> {
    protected MaintenanceTaskRunStatus of(Short i) {
        return MaintenanceTaskRunStatus.of(i);
    }
}
