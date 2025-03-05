package org.opencdmp.integrationevent.outbox.accountingentrycreated;

import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.enums.accounting.AccountingMeasureType;
import org.opencdmp.commons.enums.accounting.AccountingValueType;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.opencdmp.service.accounting.AccountingProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AccountingEntryCreatedIntegrationEventHandlerImpl implements AccountingEntryCreatedIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(AccountingEntryCreatedIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final AccountingProperties accountingProperties;

    public AccountingEntryCreatedIntegrationEventHandlerImpl(OutboxService outboxService, AccountingProperties accountingProperties) {
        this.outboxService = outboxService;
        this.accountingProperties = accountingProperties;
    }

    private void handle(AccountingEntryCreatedIntegrationEvent event)  {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.ACCOUNTING_ENTRY_CREATED);
        message.setEvent(event);
        this.outboxService.publish(message);
    }

    @Override
    public void handleAccountingEntry(String metric, AccountingValueType valueType, String subjectId, UUID tenantId, String tenantCode, Integer value) throws InvalidApplicationException {
        if (accountingProperties.getEnabled()) {
            AccountingEntryCreatedIntegrationEvent event = new AccountingEntryCreatedIntegrationEvent();
            event.setTimeStamp(Instant.now());
            event.setServiceId(accountingProperties.getServiceId());
            event.setAction(metric);
            event.setMeasure(AccountingMeasureType.Unit);
            event.setType(valueType);
            event.setResource(tenantCode);
            event.setValue((double) value);
            event.setTenant(tenantId);

            this.handle(event);
        }
    }

}
