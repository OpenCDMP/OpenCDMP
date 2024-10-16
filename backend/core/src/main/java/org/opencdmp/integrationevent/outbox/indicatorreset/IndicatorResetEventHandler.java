package org.opencdmp.integrationevent.outbox.indicatorreset;



public interface IndicatorResetEventHandler {

    void handle(IndicatorResetEvent event);

}
