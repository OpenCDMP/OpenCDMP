package org.opencdmp.controllers;

import org.opencdmp.audit.AuditableAction;
import org.opencdmp.query.lookup.SemanticsLookup;
import org.opencdmp.service.semantic.SemanticsService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(path = {"api/semantics"})
public class SemanticsController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(SemanticsController.class));

    private final AuditService auditService;

    private final MessageSource messageSource;

    private final AuthorizationService authService;

    private final SemanticsService semanticsService;

    @Autowired
    public SemanticsController(AuditService auditService,
                               MessageSource messageSource, AuthorizationService authService, SemanticsService semanticsService) {
        this.auditService = auditService;
        this.messageSource = messageSource;
        this.authService = authService;
        this.semanticsService = semanticsService;
    }

    @PostMapping()
    public List<String> getSemantics(@RequestBody SemanticsLookup lookup) throws IOException {
        logger.debug(new MapLogEntry("retrieving semantics "));

        List<String> semantics = this.semanticsService.getSemantics(lookup);

        this.auditService.track(AuditableAction.GetSemantics, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("lookup", lookup)
        ));

        return semantics;
    }
}
