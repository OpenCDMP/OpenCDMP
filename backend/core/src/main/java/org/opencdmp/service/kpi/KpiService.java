package org.opencdmp.service.kpi;

import org.opencdmp.integrationevent.outbox.indicator.IndicatorElasticEvent;
import org.opencdmp.integrationevent.outbox.indicatorreset.IndicatorResetEvent;

import javax.management.InvalidApplicationException;

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

}
