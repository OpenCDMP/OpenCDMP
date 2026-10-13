package org.opencdmp.integrationevent.outbox;


import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
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
	        // The event is dropped on the floor here. Log what it was so it can be replayed by hand if needed -
	        // dumping the serialised exception instead buries the stack trace inside the message field.
			logger.error("Could not queue {} event for publishing. The event is lost: {}", event.getType(), event.getMessage(), ex);
        }

	}
}
