package org.opencdmp.websocket.interceptors;


import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.validator.routines.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.data.UserContactInfoEntity;
import org.opencdmp.data.UserCredentialEntity;
import org.opencdmp.interceptors.user.UserInterceptorCacheService;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.query.UserContactInfoQuery;
import org.opencdmp.query.UserCredentialQuery;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class
StompUserInterceptor implements ChannelInterceptor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StompUserInterceptor.class));

	@PersistenceContext
	public EntityManager entityManager;
	private final ClaimExtractorFactory claimExtractorFactory;
	private final UserInterceptorCacheService userInterceptorCacheService;
	private final UserScopeFactory userScopeFactory;
	private final CurrentPrincipalResolverFactory currentPrincipalResolverFactory;
	private final QueryFactory queryFactory;

	public StompUserInterceptor(ClaimExtractorFactory claimExtractorFactory, UserInterceptorCacheService userInterceptorCacheService, UserScopeFactory userScopeFactory, CurrentPrincipalResolverFactory currentPrincipalResolverFactory, QueryFactory queryFactory) {
	    this.claimExtractorFactory = claimExtractorFactory;
	    this.userInterceptorCacheService = userInterceptorCacheService;
        this.userScopeFactory = userScopeFactory;
        this.currentPrincipalResolverFactory = currentPrincipalResolverFactory;
        this.queryFactory = queryFactory;
    }
	
    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
	    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	    assert accessor != null;
	    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			try {

				UUID userId = null;
				if (currentPrincipalResolverFactory.getInstance().currentPrincipal().isAuthenticated()) {
					String subjectId = this.claimExtractorFactory.getInstance().subjectString(currentPrincipalResolverFactory.getInstance().currentPrincipal());
					if (subjectId == null || subjectId.isBlank()) throw new MyForbiddenException("Empty subjects not allowed");

					UserInterceptorCacheService.UserInterceptorCacheValue cacheValue = this.userInterceptorCacheService.lookup(this.userInterceptorCacheService.buildKey(subjectId));
					if (cacheValue != null) {
						userId = cacheValue.getUserId();
					} else {
							String email = this.getEmailFromClaims(currentPrincipalResolverFactory.getInstance().currentPrincipal());
							userId = this.findExistingUserFromDbBySubject(subjectId);
							if (userId == null) {
								userId = this.findExistingUserFromDbByEmailAndAddCredentail(email);
							}
					}
					userScopeFactory.getInstance().setUserId(userId);
				}
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
	    }

        return message;
    }

	private String getEmailFromClaims(MyPrincipal principal) {
		String email = this.claimExtractorFactory.getInstance().email(principal);
		if (email == null || email.isBlank() || !EmailValidator.getInstance().isValid(email)) return null;
		return email.trim();
	}
	
	private UUID findExistingUserFromDbBySubject(String subjectId) {

		UserCredentialEntity userCredential = this.queryFactory.query(UserCredentialQuery.class).externalIds(subjectId).firstAs(new BaseFieldSet().ensure(UserCredential._user));
		if (userCredential != null)  return userCredential.getUserId();
		return null;
	}

	private UUID findExistingUserFromDbByEmailAndAddCredentail(String email) {
		if (email != null && !email.isBlank()) {

			UserContactInfoEntity userContactInfo = this.queryFactory.query(UserContactInfoQuery.class).types(ContactInfoType.Email).values(email).firstAs(new BaseFieldSet().ensure(UserContactInfo._user));
			return userContactInfo != null ? userContactInfo.getUserId() : null;
		} else {
			throw new MyForbiddenException("Email is required");
		}
	}
}
