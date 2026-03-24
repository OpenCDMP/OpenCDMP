package org.opencdmp.websocket.interceptors;


import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.JwtPrincipal;
import gr.cite.tools.logging.LoggerService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;


@Component
public class StompAuthnChannelInterceptor implements ChannelInterceptor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StompAuthnChannelInterceptor.class));


    private final JwtDecoder jwtDecoder;
	private final CurrentPrincipalResolverFactory currentPrincipalResolverFactory;
    public StompAuthnChannelInterceptor(JwtDecoder jwtDecoder, CurrentPrincipalResolverFactory currentPrincipalResolverFactory) {
	    this.jwtDecoder = jwtDecoder;
        this.currentPrincipalResolverFactory = currentPrincipalResolverFactory;
    }

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
	    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	    assert accessor != null;
	    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
		    String authToken = accessor.getFirstNativeHeader("Authorization");

		    if (authToken != null && authToken.startsWith("Bearer ")) {
			    String token = authToken.substring(7);

			    try {
				    Jwt jwt = this.jwtDecoder.decode(token);


				    Authentication authentication = new JwtAuthenticationToken(jwt, null);
				    authentication.setAuthenticated(true);
				    this.currentPrincipalResolverFactory.getInstance().push(this.buildJwtPrincipal(authentication));
					
				    accessor.setUser(authentication);
				    accessor.setLeaveMutable(true);

				    return MessageBuilder.createMessage(
						    message.getPayload(),
						    accessor.getMessageHeaders()
				    );

			    } catch (Exception e) {
				    throw new IllegalArgumentException("Invalid JWT", e);
			    }
		    }
	    }
	    return message;
    }

	private JwtPrincipal buildJwtPrincipal(Authentication authentication){
		boolean isAuthenticated = false;
		Jwt jwtToken = null;
		if(authentication != null) {
			Object principalObj = authentication.getPrincipal();
			if (principalObj instanceof Jwt) {
				jwtToken = (Jwt) principalObj;
				isAuthenticated = authentication.isAuthenticated();
			}
		}
		return new JwtPrincipal(isAuthenticated, jwtToken);
	}
}
