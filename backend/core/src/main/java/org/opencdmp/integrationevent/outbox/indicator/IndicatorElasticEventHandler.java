package org.opencdmp.integrationevent.outbox.indicator;


public interface IndicatorElasticEventHandler {

    void handle(IndicatorElasticEvent event);

}
