package org.opencdmp.controllers;

import org.opencdmp.audit.AuditableAction;
import org.opencdmp.model.persist.ContactSupportPersist;
import org.opencdmp.model.persist.PublicContactSupportPersist;
import org.opencdmp.service.contactsupport.ContactSupportService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.AbstractMap;
import java.util.Map;

@RestController
@RequestMapping(path = "api/contact-support")
public class ContactSupportController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ContactSupportController.class));

    private final AuditService auditService;

    private final ContactSupportService contactSupportService;


    public ContactSupportController(
            AuditService auditService,
            ContactSupportService contactSupportService) {
        this.auditService = auditService;
        this.contactSupportService = contactSupportService;
    }



    @PostMapping("send")
    @Transactional
    @ValidationFilterAnnotation(validator = ContactSupportPersist.ContactSupportPersistValidator.ValidatorName, argumentName ="model")
    public void sendContactEmail(@RequestBody ContactSupportPersist model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("send support email").And("model", model));

        this.contactSupportService.sendContactEmail(model);
        
        this.auditService.track(AuditableAction.ContactSupport_Sent, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
    }

    

    @PostMapping(path = "public/send", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @Transactional
    @ValidationFilterAnnotation(validator = PublicContactSupportPersist.PublicContactSupportPersistValidator.ValidatorName, argumentName ="model")
    public void sendPublicContactEmail(@ModelAttribute PublicContactSupportPersist model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("send public support email").And("model", model));

        this.contactSupportService.sendPublicContactEmail(model);

        this.auditService.track(AuditableAction.ContactSupport_PublicSent, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
    }

    @PostMapping("public/send")
    @Transactional
    @ValidationFilterAnnotation(validator = PublicContactSupportPersist.PublicContactSupportPersistValidator.ValidatorName, argumentName ="model")
    public void sendPublicContactEmailJson(@RequestBody PublicContactSupportPersist model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("send public support email").And("model", model));

        this.contactSupportService.sendPublicContactEmail(model);

        this.auditService.track(AuditableAction.ContactSupport_PublicSent, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
    }
}
