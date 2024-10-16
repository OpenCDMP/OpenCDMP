package org.opencdmp.integrationevent.outbox;


import org.opencdmp.commons.JsonHandlingService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class OutboxServiceImpl implements OutboxService {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(OutboxServiceImpl.class));

	private final ApplicationEventPublisher eventPublisher;

	public OutboxServiceImpl(
			ApplicationEventPublisher eventPublisher
	) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void publish(OutboxIntegrationEvent event) {
		try {
			eventPublisher.publishEvent(event);
        } catch (Exception ex) {
			logger.error(new MapLogEntry(String.format("Could not save message ", event.getMessage())).And("message", event.getMessage()).And("ex", ex));
			//Still want to skip it from processing
        }

	}
}
