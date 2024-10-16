package org.opencdmp.controllers;

import org.opencdmp.audit.AuditableAction;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.model.*;
import org.opencdmp.model.censorship.*;
import org.opencdmp.model.user.User;
import org.opencdmp.service.dashborad.DashboardService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/dashboard")
public class DashboardController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DashboardController.class));

    private final AuditService auditService;

    private final DashboardService dashboardService;

    private final CensorFactory censorFactory;

    private final UserScope userScope;
    public DashboardController(
            AuditService auditService,
            DashboardService dashboardService, 
            CensorFactory censorFactory,
            UserScope userScope) {
        this.auditService = auditService;
        this.dashboardService = dashboardService;
        this.censorFactory = censorFactory;
        this.userScope = userScope;
    }



    @PostMapping("mine/recent-activity")
    public List<RecentActivityItem> getMyRecentActivityItems(@RequestBody RecentActivityItemLookup lookup) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + User.class.getSimpleName()).And("lookup", lookup));

        this.censorFactory.censor(RecentActivityItemCensor.class).censor(lookup.getProject(), this.userScope.getUserId());
        
        lookup.setUserIds(List.of(this.userScope.getUserId()));
        List<RecentActivityItem> models = this.dashboardService.getMyRecentActivityItems(lookup);

        this.auditService.track(AuditableAction.Dashboard_MyRecentActivityItems, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("lookup", lookup)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return models;
    }

    @GetMapping("mine/get-statistics")
    public DashboardStatistics getMyDashboardStatistics() throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving public statistics"));

        this.censorFactory.censor(MyDashboardStatisticsCensor.class).censor(this.userScope.getUserIdSafe());

        DashboardStatistics model = this.dashboardService.getMyDashboardStatistics();

        this.auditService.track(AuditableAction.Dashboard_PublicDashboardStatistics);

        return model;
    }

    @GetMapping("public/get-statistics")
    public DashboardStatistics getPublicDashboardStatistics() throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving public statistics"));

        this.censorFactory.censor(PublicDashboardStatisticsCensor.class).censor(null);

        DashboardStatistics model = this.dashboardService.getPublicDashboardStatistics();
        
        this.auditService.track(AuditableAction.Dashboard_PublicDashboardStatistics);

        return model;
    }

}
