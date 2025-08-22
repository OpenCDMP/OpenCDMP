package org.opencdmp.service.accounting;

import org.opencdmp.commons.types.usagelimit.DefinitionEntity;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface AccountingService {

    Integer getCurrentMetricValue(String metric, DefinitionEntity definition) throws InvalidApplicationException;

    void set(String metric, UUID tenantId, String tenantCode, Integer value) throws InvalidApplicationException;

    void increase(String metric) throws InvalidApplicationException;

    void decrease(String metric) throws InvalidApplicationException;
}
