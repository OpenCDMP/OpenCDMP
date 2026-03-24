package org.opencdmp.commons.scope;

import org.springframework.messaging.simp.SimpAttributes;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ScopeContextDetector {

    public enum ScopeType {
        REQUEST,
        WEBSOCKET,
        NONE
    }

    /**
     * Detects the current scope type based on the execution context.
     */
    public ScopeType detectCurrentScope() {
        // Then check for HTTP request scope
        // This works for regular HTTP requests in @RequestMapping, @RestController, etc.
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                return ScopeType.REQUEST;
            }
        } catch (IllegalStateException e) {
            // No request context bound to thread
        }

        try {
            SimpAttributes simpAttributes = SimpAttributesContextHolder.getAttributes();
            if (simpAttributes != null) {
                return ScopeType.WEBSOCKET;
            }
        } catch (IllegalStateException e) {
            // No WebSocket context bound to thread
        }

        // No web scope available
        return ScopeType.NONE;
    }

    private boolean isHttpRequest(RequestAttributes attributes) {
        // Try to detect if this is an HTTP request by checking common request attributes
        try {
            attributes.getSessionId(); // This works for HTTP requests
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}