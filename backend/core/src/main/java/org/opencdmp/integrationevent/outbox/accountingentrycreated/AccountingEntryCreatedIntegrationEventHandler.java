package org.opencdmp.integrationevent.outbox.accountingentrycreated;

import org.opencdmp.commons.enums.accounting.AccountingValueType;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface AccountingEntryCreatedIntegrationEventHandler {

    void handleAccountingEntry(String metric, AccountingValueType valueType, String subjectId, UUID tenantId, String tenantCode, Integer value, UUID userId) throws InvalidApplicationException;
}
