package org.opencdmp.websocket;

import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
public class SubscriptionDebugListener {

	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(SubscriptionDebugListener.class));

	@EventListener
	public void handleSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
		logger.debug("=== SUBSCRIBE EVENT ===");
        logger.debug("Session ID: {}", sha.getSessionId());
        logger.debug("Subscription ID: {}", sha.getSubscriptionId());
        logger.debug("Destination: {}", sha.getDestination());
        logger.debug("User: {}", sha.getUser() != null ? sha.getUser().getName() : "null");
		logger.debug("=======================");
	}

	@EventListener
	public void handleUnsubscribe(SessionUnsubscribeEvent event) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
		logger.debug("=== UNSUBSCRIBE EVENT ===");
        logger.debug("Session ID: {}", sha.getSessionId());
        logger.debug("Subscription ID: {}", sha.getSubscriptionId());
        logger.debug("User: {}", sha.getUser() != null ? sha.getUser().getName() : "null");
		logger.debug("=========================");

		// If this is NOT being printed, the UNSUBSCRIBE is being blocked somewhere
	}
}
