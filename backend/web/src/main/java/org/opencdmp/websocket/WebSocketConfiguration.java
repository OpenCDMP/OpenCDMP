package org.opencdmp.websocket;

import org.opencdmp.websocket.interceptors.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties({WebSocketProperties.class})
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer  {

	private final WebSocketProperties webSocketProperties;
	private final StompAuthnChannelInterceptor stompAuthnChannelInterceptor;
	private final StompTenantInterceptor stompTenantInterceptor;
	private final StompTenantScopeHeaderInterceptor stompTenantScopeHeaderInterceptor;
	private final StompTenantScopeClaimInterceptor stompTenantScopeClaimInterceptor;
	private final StompUserInterceptor stompUserInterceptor;
	private final StompAuthzChannelInterceptor stompAuthzChannelInterceptor;
	
	public WebSocketConfiguration(WebSocketProperties webSocketProperties,
                                  StompAuthnChannelInterceptor stompAuthnChannelInterceptor, StompTenantInterceptor stompTenantInterceptor, StompTenantScopeHeaderInterceptor stompTenantScopeHeaderInterceptor, StompTenantScopeClaimInterceptor stompTenantScopeClaimInterceptor, StompUserInterceptor stompUserInterceptor, StompAuthzChannelInterceptor stompAuthzChannelInterceptor)
	{
        this.webSocketProperties = webSocketProperties;
        this.stompAuthnChannelInterceptor = stompAuthnChannelInterceptor;
		this.stompTenantInterceptor = stompTenantInterceptor;
		this.stompTenantScopeHeaderInterceptor = stompTenantScopeHeaderInterceptor;
		this.stompTenantScopeClaimInterceptor = stompTenantScopeClaimInterceptor;
		this.stompUserInterceptor = stompUserInterceptor;
		this.stompAuthzChannelInterceptor = stompAuthzChannelInterceptor;
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/queue/","/topic");
		config.setUserDestinationPrefix("/user");
		config.setApplicationDestinationPrefixes("/app");
		config.setPreservePublishOrder(true);
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/plans")
				.setAllowedOriginPatterns(this.webSocketProperties.getAllowedOrigins())
				.withSockJS()
				.setSessionCookieNeeded(false)
				.setHeartbeatTime(25000);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(
				stompAuthnChannelInterceptor, 
				stompTenantScopeHeaderInterceptor, 
				stompTenantScopeClaimInterceptor, 
				stompUserInterceptor, 
				stompTenantInterceptor, 
				stompAuthzChannelInterceptor
				);
	}

}