package org.opencdmp.websocket.interceptors;


import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.logging.LoggerService;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.service.websocket.StompEndpointHelper;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;


@Component
public class StompAuthzChannelInterceptor implements ChannelInterceptor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StompAuthzChannelInterceptor.class));


	private final StompEndpointHelper stompEndpointHelper;

	private final QueryFactory queryFactory;

	public StompAuthzChannelInterceptor(StompEndpointHelper stompEndpointHelper, QueryFactory queryFactory) {
		this.stompEndpointHelper = stompEndpointHelper;
        this.queryFactory = queryFactory;
    }

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
	    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	    assert accessor != null;
	    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
		    String destination = accessor.getDestination();
		    if (destination != null) {
				if (destination.startsWith("/app") || destination.startsWith("/user")){
					return message;
			    } else {
					if (stompEndpointHelper.isSubscriptionGetPlanUsers(destination)) {
						PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(stompEndpointHelper.extractSubscriptionGetPlanUsersPlanId(destination));
						if (query.count() == 0) throw new MyForbiddenException("Access is denied");
						return message;
					}
				}
			    throw new MyForbiddenException("Access is denied");
		    }
	    }

	    return message;
    }
}
