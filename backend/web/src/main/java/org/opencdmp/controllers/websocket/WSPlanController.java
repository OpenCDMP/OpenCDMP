package org.opencdmp.controllers.websocket;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.WSActionType;
import org.opencdmp.controllers.controllerhandler.GlobalExceptionHandler;
import org.opencdmp.model.censorship.plan.PlanCensor;
import org.opencdmp.model.persist.PlanPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.model.user.User;
import org.opencdmp.model.websocket.UserActionPayload;
import org.opencdmp.model.websocket.WSMessage;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.lookup.PlanLookup;
import org.opencdmp.service.elastic.ElasticQueryHelperService;
import org.opencdmp.service.plan.PlanService;
import org.opencdmp.service.plan.PlanWebSocketService;
import org.opencdmp.service.websocket.StompEndpointHelper;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Controller
public class WSPlanController {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(WSPlanController.class));

    private final PlanWebSocketService planWebSocketService;
    private final SimpMessagingTemplate messagingTemplate;
	private final JsonHandlingService jsonHandlingService;
	private final StompEndpointHelper stompEndpointHelper;
	private final CensorFactory censorFactory;
	private final AuditService auditService;
	private final ElasticQueryHelperService elasticQueryHelperService;
	private final PlanService planService;
	private final ValidatorFactory validatorFactory;
	private final QueryFactory queryFactory;
	
    public WSPlanController(PlanWebSocketService planWebSocketService, SimpMessagingTemplate messagingTemplate, JsonHandlingService jsonHandlingService, StompEndpointHelper stompEndpointHelper, CensorFactory censorFactory, AuditService auditService, ElasticQueryHelperService elasticQueryHelperService, PlanService planService, ValidatorFactory validatorFactory, QueryFactory queryFactory) {
        this.planWebSocketService = planWebSocketService;
        this.messagingTemplate = messagingTemplate;
	    this.jsonHandlingService = jsonHandlingService;
	    this.stompEndpointHelper = stompEndpointHelper;
        this.censorFactory = censorFactory;
        this.auditService = auditService;
        this.elasticQueryHelperService = elasticQueryHelperService;
        this.planService = planService;
        this.validatorFactory = validatorFactory;
        this.queryFactory = queryFactory;
    }

	@SubscribeMapping("/plan/query")
	public QueryResult<Plan> Query(@Payload PlanLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", Plan.class.getSimpleName());

		this.censorFactory.censor(PlanCensor.class).censor(lookup.getProject(), null);

		QueryResult<Plan> queryResult = this.elasticQueryHelperService.collect(lookup, AuthorizationFlags.AllExceptPublic, null);

		this.auditService.track(AuditableAction.Plan_Query, "lookup", lookup);

		return queryResult;
	}

	@MessageMapping("/plan/persist")
	@SendToUser("/queue/plan/persist")
	@Transactional
	public Plan Persist(
			@Payload PlanPersist model
	) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException, JAXBException {
		logger.debug(new MapLogEntry("persisting" + Plan.class.getSimpleName()).And("model", model).And("fieldSet", null));
		this.validatorFactory.validator(PlanPersist.PlanPersistValidator.class).validateForce(model);

		Plan persisted = this.planService.persist(model, null);
		
		this.auditService.track(AuditableAction.Plan_Persist, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model)
//				new AbstractMap.SimpleEntry<String, Object>("fields", null)
		));
		return persisted;
	}

	@MessageMapping("/plan/{id}/join")
	public void joinPlan(@DestinationVariable UUID id, SimpMessageHeaderAccessor headerAccessor, Principal principal) {

		PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
		if (query.count() == 0) throw new MyForbiddenException("Access is denied");

		User user = planWebSocketService.getUserFromSubjectId(principal.getName());
		WSMessage<User> message = new WSMessage<>(user, WSActionType.PlanJoin, null);
		messagingTemplate.convertAndSend(this.stompEndpointHelper.buildSubscriptionGetPlanUsers(id), message);
	}

    @MessageMapping("/plan/{id}/leave")
    public void leavePlan(@DestinationVariable UUID id, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
		PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
		if (query.count() == 0) throw new MyForbiddenException("Access is denied");

		User user = planWebSocketService.getUserFromSubjectId(principal.getName());
		WSMessage<User> message = new WSMessage<>(user, WSActionType.PlanLeave, null);
        messagingTemplate.convertAndSend(this.stompEndpointHelper.buildSubscriptionGetPlanUsers(id), message);
    }

	@MessageMapping("/plan/{id}/user-action")
	public void userAction(@DestinationVariable UUID id, @Payload UserActionPayload request, SimpMessageHeaderAccessor headerAccessor, Principal principal) {

		PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
		if (query.count() == 0) throw new MyForbiddenException("Access is denied");

		User user = planWebSocketService.getUserFromSubjectId(principal.getName());
		WSMessage<UserActionPayload> message = new WSMessage<>(user, WSActionType.PlanUserAction, request);
		messagingTemplate.convertAndSend(this.stompEndpointHelper.buildSubscriptionGetPlanUsers(id), message);
	}

	@MessageExceptionHandler
	@SendToUser(destinations="/queue/errors", broadcast=false)
	public GlobalExceptionHandler.HandledException handleException(Exception exception)  {
		var handler = new GlobalExceptionHandler(jsonHandlingService);
		return handler.handleException(exception, null);
	}
}
