package org.opencdmp.integrationevent.outbox.indicatoraccess;


import java.util.UUID;

public interface IndicatorAccessEventHandler {

    void handle(IndicatorAccessEvent event, UUID tenant);

}
