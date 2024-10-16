package org.opencdmp.model.censorship.plan;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.*;
import org.opencdmp.model.censorship.planreference.PlanReferenceCensor;
import org.opencdmp.model.plan.Plan;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public PlanCensor(ConventionService conventionService,
                      AuthorizationService authService,
                      CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowsePlan, Permission.DeferredAffiliation);

        FieldSet planUsersFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._planUsers));
        this.censorFactory.censor(PlanUserCensor.class).censor(planUsersFields, userId);
        FieldSet planReferencesFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._planReferences));
        this.censorFactory.censor(PlanReferenceCensor.class).censor(planReferencesFields, userId);
        FieldSet creatorFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._creator));
        this.censorFactory.censor(UserCensor.class).censor(creatorFields, userId);
        FieldSet doisFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._entityDois));
        this.censorFactory.censor(EntityDoiCensor.class).censor(doisFields, userId);
        FieldSet propertiesFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._properties));
        this.censorFactory.censor(PlanPropertiesCensor.class).censor(propertiesFields, userId);
        FieldSet otherPlanVersionsFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._otherPlanVersions));
        this.censorFactory.censor(PlanCensor.class).censor(otherPlanVersionsFields, userId);
    }
}
