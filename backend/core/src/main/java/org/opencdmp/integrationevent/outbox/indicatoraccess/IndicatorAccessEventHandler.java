package org.opencdmp.integrationevent.outbox.indicatoraccess;


import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface IndicatorAccessEventHandler {

    void handle(UUID userId) throws InvalidApplicationException;

}
