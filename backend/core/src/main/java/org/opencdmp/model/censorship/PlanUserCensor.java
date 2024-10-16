package org.opencdmp.model.censorship;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.PlanUser;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.censorship.plan.PlanCensor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanUserCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanUserCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public PlanUserCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowsePlanUser, Permission.DeferredAffiliation);
        FieldSet planFields = fields.extractPrefixed(this.asIndexerPrefix(PlanUser._plan));
        this.censorFactory.censor(PlanCensor.class).censor(planFields, userId);
        FieldSet userFields = fields.extractPrefixed(this.asIndexerPrefix(PlanUser._user));
        this.censorFactory.censor(UserCensor.class).censor(userFields, userId);
    }

}
