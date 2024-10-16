package org.opencdmp.service.contactsupport;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.commons.enums.notification.NotificationContactType;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.notification.*;
import org.opencdmp.data.UserContactInfoEntity;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.UserContactInfo;
import org.opencdmp.model.persist.ContactSupportPersist;
import org.opencdmp.model.persist.PublicContactSupportPersist;
import org.opencdmp.query.UserContactInfoQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactSupportServiceImpl implements ContactSupportService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ContactSupportServiceImpl.class));


    private final AuthorizationService authorizationService;

    private final QueryFactory queryFactory;

    private final JsonHandlingService jsonHandlingService;

    private final UserScope userScope;

    private final NotifyIntegrationEventHandler notifyIntegrationEventHandler;

    private final NotificationProperties notificationProperties;
    @Autowired
    public ContactSupportServiceImpl(
		    AuthorizationService authorizationService,
		    QueryFactory queryFactory,
		    JsonHandlingService jsonHandlingService,
		    UserScope userScope,
		    NotifyIntegrationEventHandler notifyIntegrationEventHandler,
            NotificationProperties notificationProperties) {
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.userScope = userScope;
	    this.notifyIntegrationEventHandler = notifyIntegrationEventHandler;
	    this.notificationProperties = notificationProperties;
    }

    @Override
    public void sendContactEmail(ContactSupportPersist model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("send contact email").And("model", model));
        this.authorizationService.authorizeForce(Permission.SendContactSupport);
        
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();

        event.setUserId(this.userScope.getUserId());

        UserContactInfoQuery query = this.queryFactory.query(UserContactInfoQuery.class).disableTracking().userIds(this.userScope.getUserId());
        query.setOrder(new Ordering().addAscending(UserContactInfo._ordinal));
        UserContactInfoEntity contactInfo = query.first();
        
        List<ContactPair> contactPairs = new ArrayList<>();
        contactPairs.add(new ContactPair(ContactInfoType.Email, this.notificationProperties.getContactSupportEmail()));
        NotificationContactData contactData = new NotificationContactData(contactPairs, null, null);
        event.setContactHint(this.jsonHandlingService.toJsonSafe(contactData));
        event.setContactTypeHint(NotificationContactType.EMAIL);

        event.setNotificationType(this.notificationProperties.getContactSupportType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{subject}", DataType.String, model.getSubject()));
        fieldInfoList.add(new FieldInfo("{description}", DataType.String, model.getDescription()));
        fieldInfoList.add(new FieldInfo("{email}", DataType.String, contactInfo.getValue()));
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

	    this.notifyIntegrationEventHandler.handle(event);
    }

    @Override
    public void sendPublicContactEmail(PublicContactSupportPersist model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("public send contact email").And("model", model));
        this.authorizationService.authorizeForce(Permission.PublicSendContactSupport);

        NotifyIntegrationEvent event = new NotifyIntegrationEvent();

        List<ContactPair> contactPairs = new ArrayList<>();
        contactPairs.add(new ContactPair(ContactInfoType.Email, this.notificationProperties.getContactSupportEmail()));
        NotificationContactData contactData = new NotificationContactData(contactPairs, null, null);
        event.setContactHint(this.jsonHandlingService.toJsonSafe(contactData));
        event.setContactTypeHint(NotificationContactType.EMAIL);

        event.setNotificationType(this.notificationProperties.getPublicContactSupportType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{subject}", DataType.String, model.getAffiliation()));
        fieldInfoList.add(new FieldInfo("{description}", DataType.String, model.getMessage()));
        fieldInfoList.add(new FieldInfo("{email}", DataType.String,  model.getEmail()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String,  model.getFullName()));
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

	    this.notifyIntegrationEventHandler.handle(event);
    }


}
