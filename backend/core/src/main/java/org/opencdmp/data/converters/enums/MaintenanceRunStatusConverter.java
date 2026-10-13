package org.opencdmp.data.converters.enums;

import jakarta.persistence.Converter;
import org.opencdmp.commons.enums.MaintenanceRunStatus;

@Converter
public class MaintenanceRunStatusConverter extends DatabaseEnumConverter<MaintenanceRunStatus, Short> {
    protected MaintenanceRunStatus of(Short i) {
        return MaintenanceRunStatus.of(i);
    }
}
