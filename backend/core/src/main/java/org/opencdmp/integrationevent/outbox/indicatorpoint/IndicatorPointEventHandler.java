package org.opencdmp.integrationevent.outbox.indicatorpoint;

public interface IndicatorPointEventHandler {

    void handle(IndicatorPointEvent event);

}
