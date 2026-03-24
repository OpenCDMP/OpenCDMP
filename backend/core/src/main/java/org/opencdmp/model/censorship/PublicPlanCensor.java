package org.opencdmp.model.censorship;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.plan.Plan;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PublicPlanCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public PublicPlanCensor(ConventionService conventionService,
                            AuthorizationService authService,
                            CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.PublicBrowsePlan);

        FieldSet planReferencesFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._planReferences));
        this.censorFactory.censor(PublicPlanReferenceCensor.class).censor(planReferencesFields);
        FieldSet doisFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._entityDois));
        this.censorFactory.censor(PublicEntityDoiCensor.class).censor(doisFields);
        FieldSet otherPlanVersionsFields = fields.extractPrefixed(this.asIndexerPrefix(Plan._otherPlanVersions));
        this.censorFactory.censor(PublicPlanCensor.class).censor(otherPlanVersionsFields);
    }

}
