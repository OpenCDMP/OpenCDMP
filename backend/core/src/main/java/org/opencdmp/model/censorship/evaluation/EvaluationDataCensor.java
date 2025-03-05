package org.opencdmp.model.censorship.evaluation;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.evaluation.EvaluationData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationDataCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EvaluationDataCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public EvaluationDataCensor(ConventionService conventionService,
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

        this.authService.authorizeForce(Permission.BrowseEvaluation, Permission.DeferredAffiliation);
        FieldSet fieldsFields = fields.extractPrefixed(this.asIndexerPrefix(EvaluationData._rankConfig));
        this.censorFactory.censor(EvaluationRankConfigCensor.class).censor(fieldsFields, userId);

        FieldSet sourcesFields = fields.extractPrefixed(this.asIndexerPrefix(EvaluationData._rankModel));
        this.censorFactory.censor(EvaluationRankModelCensor.class).censor(sourcesFields, userId);
    }

}
