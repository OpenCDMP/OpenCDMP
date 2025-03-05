package org.opencdmp.integrationevent.outbox.usertouched;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.types.user.AdditionalInfoEntity;
import org.opencdmp.data.*;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.opencdmp.model.TenantUser;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.user.User;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.query.TenantUserQuery;
import org.opencdmp.query.UserContactInfoQuery;
import org.opencdmp.query.UserCredentialQuery;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("outboxusertouchedintegrationeventhandler")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserTouchedIntegrationEventHandlerImpl implements UserTouchedIntegrationEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserTouchedIntegrationEventHandlerImpl.class));

    private final OutboxService outboxService;
    private final JsonHandlingService jsonHandlingService;
    private final MessageSource messageSource;
	private final TenantEntityManager entityManager;

    private final QueryFactory queryFactory;
    public UserTouchedIntegrationEventHandlerImpl(
		    OutboxService outboxService, JsonHandlingService jsonHandlingService, MessageSource messageSource, TenantEntityManager entityManager, QueryFactory queryFactory) {
        this.outboxService = outboxService;
	    this.jsonHandlingService = jsonHandlingService;
	    this.messageSource = messageSource;
	    this.entityManager = entityManager;
	    this.queryFactory = queryFactory;
    }

    @Override
    public void handle(UUID userId) throws InvalidApplicationException {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.USER_TOUCH);

	    try {
		    this.entityManager.disableTenantFilters();

		    UserEntity user = this.queryFactory.query(UserQuery.class).ids(userId).disableTracking()
				    .firstAs(new BaseFieldSet().ensure(User._name).ensure(User._additionalInfo));
		    if (user == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userId, User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		    List<UserContactInfoEntity> userContactInfoEntities = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().userIds(userId)
				    .collectAs(new BaseFieldSet().ensure(UserContactInfo._type).ensure(UserContactInfo._ordinal).ensure(UserContactInfo._value));

		    List<UserCredentialEntity> userCredentialEntities = this.queryFactory.query(UserCredentialQuery.class).disableTracking().userIds(userId)
				    .collectAs(new BaseFieldSet().ensure(UserCredential._id, UserCredential._externalId));

		    List<TenantUserEntity> tenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().userIds(userId).isActive(IsActive.Active)
				    .collectAs(new BaseFieldSet().ensure(TenantUser._id, TenantUser._tenant));

		    UserTouchedIntegrationEvent event = new UserTouchedIntegrationEvent();
		    event.setId(userId);
		    event.setName(user.getName());
		    if (userCredentialEntities != null && !userCredentialEntities.isEmpty()) {
			    event.setCredentials(new ArrayList<>());
				for (UserCredentialEntity userCredential : userCredentialEntities){
					UserTouchedIntegrationEvent.UserCredential userCredentialEvent = new UserTouchedIntegrationEvent.UserCredential();
					userCredentialEvent.setSubjectId(userCredential.getExternalId());
					event.getCredentials().add(userCredentialEvent);
				}
		    }

		    if (tenantUserEntities != null && !tenantUserEntities.isEmpty()) {
			    event.setTenantUsers(new ArrayList<>());
			    for (TenantUserEntity tenantUserEntity : tenantUserEntities){
				    UserTouchedIntegrationEvent.TenantUser tenantUser = new UserTouchedIntegrationEvent.TenantUser();
				    tenantUser.setTenant(tenantUserEntity.getTenantId());
				    event.getTenantUsers().add(tenantUser);
			    }
		    }
			
			AdditionalInfoEntity definition = this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, user.getAdditionalInfo());
		    if (definition != null) {
			    event.setProfile(new UserTouchedIntegrationEvent.UserProfile());
			    event.getProfile().setCulture(definition.getCulture());
			    event.getProfile().setLanguage(definition.getLanguage());
			    event.getProfile().setTimezone(definition.getTimezone());
		    }

		    if (userContactInfoEntities != null&& !userContactInfoEntities.isEmpty()){
			    event.setUserContactInfo(new ArrayList<>());
			    for (UserContactInfoEntity contactInfoEntity : userContactInfoEntities){
				    UserTouchedIntegrationEvent.UserContactInfo contactInfo = new UserTouchedIntegrationEvent.UserContactInfo();
				    contactInfo.setType(contactInfoEntity.getType());
				    contactInfo.setValue(contactInfoEntity.getValue());
				    contactInfo.setOrdinal(contactInfoEntity.getOrdinal());
				    event.getUserContactInfo().add(contactInfo);
			    }
		    }

		    message.setEvent(event);
	    }finally {
			this.entityManager.reloadTenantFilters();
	    }

        
        this.outboxService.publish(message);
    }
}
