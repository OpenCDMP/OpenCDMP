package org.opencdmp.service.kpi;

import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.enums.kpi.KpiVersionType;
import org.opencdmp.integrationevent.outbox.indicator.IndicatorElasticEvent;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEvent;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface KpiService {

    IndicatorElasticEvent createIndicator();

    IndicatorResetEvent resetIndicator();

    void sendIndicatorAccessEntryEvents() throws InvalidApplicationException;

    void sendIndicatorPointUserCountEntryEvents() throws InvalidApplicationException;

    void sendIndicatorPointPlanCountEntryEvents() throws InvalidApplicationException;

    void sendIndicatorPointDescriptionCountEntryEvents() throws InvalidApplicationException;

    void sendIndicatorPointReferenceCountEntryEvents() throws InvalidApplicationException;

    void sendIndicatorPointPlanBlueprintCountEntryEvents() throws InvalidApplicationException;

    void sendIndicatorPointDescriptionTemplateCountEntryEvents() throws InvalidApplicationException;

    void sendIndicatorPointTenantCountEntryEvents() throws InvalidApplicationException;

    //
    // single indicator point
    //
    void sendIndicatorPointPlanEntry(KpiDirectionType type) throws InvalidApplicationException;

    void sendIndicatorPointPlanPerBlueprintEntry(KpiDirectionType type, UUID planId) throws InvalidApplicationException;

    void sendIndicatorPointDescriptionEntry(KpiDirectionType type) throws InvalidApplicationException;

    void sendIndicatorPointDescriptionPerTemplateEntry(KpiDirectionType type, UUID descriptionId) throws InvalidApplicationException;

    void sendIndicatorPointReferenceEntry(KpiDirectionType type, UUID referenceId) throws InvalidApplicationException;

    void sendIndicatorPointPlanBlueprintEntry(KpiDirectionType type, KpiVersionType versionType) throws InvalidApplicationException;

    void sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType type, KpiVersionType versionType) throws InvalidApplicationException;

    void sendIndicatorPointUserEntry(KpiDirectionType type) throws InvalidApplicationException;

    void sendIndicatorPointUserEntry(KpiDirectionType type, Boolean useDefaultTenantCode) throws InvalidApplicationException;

    void sendIndicatorPointTenantEntry(KpiDirectionType type) throws InvalidApplicationException;
}
