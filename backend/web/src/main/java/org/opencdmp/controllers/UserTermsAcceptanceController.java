package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.service.usertermsacceptance.UserTermsAcceptanceService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InvalidApplicationException;

@RestController
@RequestMapping("/api/user-terms-acceptance/")
@SwaggerCommonErrorResponses
public class UserTermsAcceptanceController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserTermsAcceptanceController.class));

    private final UserTermsAcceptanceService userTermsAcceptanceService;
    private final AuditService auditService;

    @Autowired
    public UserTermsAcceptanceController(UserTermsAcceptanceService userTermsAcceptanceService, AuditService auditService) {
        this.userTermsAcceptanceService = userTermsAcceptanceService;
        this.auditService = auditService;
    }

    @RequestMapping(path = "accept", method = RequestMethod.POST)
    @Transactional
    public void accept(HttpServletRequest request) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("accept user terms"));
        String clientIp = this.extractClientIp(request);
        this.userTermsAcceptanceService.accept(clientIp);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
