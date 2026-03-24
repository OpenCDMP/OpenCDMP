package org.opencdmp.service.plan;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserCredentialEntity;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.user.User;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.model.usercredential.UserCredentialData;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.UserCredentialQuery;
import org.opencdmp.query.UserQuery;
import org.opencdmp.service.websocket.StompEndpointHelper;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanWebSocketServiceImpl implements PlanWebSocketService{


	private final SimpUserRegistry simpUserRegistry;
	private final StompEndpointHelper stompEndpointHelper;
    private final QueryFactory queryFactory;
    private final ConventionService conventionService;
    private final BuilderFactory builderFactory;

    public PlanWebSocketServiceImpl(
            SimpUserRegistry simpUserRegistry, StompEndpointHelper stompEndpointHelper, QueryFactory queryFactory, ConventionService conventionService, BuilderFactory builderFactory
    ) {
        this.simpUserRegistry = simpUserRegistry;
	    this.stompEndpointHelper = stompEndpointHelper;
        this.queryFactory = queryFactory;
        this.conventionService = conventionService;
        this.builderFactory = builderFactory;
    }

    @Override
    public List<User> getUsersSubscribedToPlan(UUID planId) {

        PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(planId);

        if (query.count() == 0) throw new MyForbiddenException("Access is denied");

        String destination = stompEndpointHelper.buildSubscriptionGetPlanUsers(planId);

        List<String> userCredentials = simpUserRegistry.getUsers().stream()
                .filter(Objects::nonNull)
                .filter(user -> user.getSessions().stream()
                        .flatMap(session -> session.getSubscriptions().stream())
                        .anyMatch(sub -> destination.equalsIgnoreCase(sub.getDestination())))
                .map(SimpUser::getName)
                .collect(Collectors.toList());

        if (userCredentials.isEmpty()) return new ArrayList<>();

        return getUserFromSubjectIds(userCredentials);
    }

    @Override
    public User getUserFromSubjectId(String credential) {
        List<User> users = getUserFromSubjectIds(List.of(credential));
        return !this.conventionService.isListNullOrEmpty(users) ? users.getFirst(): null;
    }

    private List<User> getUserFromSubjectIds(List<String> userCredentials) {
        List<UserCredentialEntity> userCredentialEntities = this.queryFactory.query(UserCredentialQuery.class).externalIds(userCredentials).collectAs(new BaseFieldSet().ensure(UserCredential._user));
        if (this.conventionService.isListNullOrEmpty(userCredentialEntities)) throw new MyNotFoundException("user credential ids not found:" + userCredentials);

        List<UUID> userIds = userCredentialEntities.stream().map(UserCredentialEntity::getUserId).distinct().toList();

        UserQuery userQuery = this.queryFactory.query(UserQuery.class).ids(userIds).isActive(IsActive.Active);

        Set<String> fields = Set.of(
                User._id,
                User._name,
                User._isActive
        );
        FieldSet fieldSet = new BaseFieldSet(fields);

        return this.builderFactory.builder(UserBuilder.class).build(fieldSet, userQuery.collectAs(fieldSet));
    }
}
