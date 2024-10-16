package org.opencdmp.model.censorship.descriptionreference;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.censorship.reference.ReferenceCensor;
import org.opencdmp.model.censorship.description.DescriptionCensor;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionReferenceCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionReferenceCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    @Autowired
    public DescriptionReferenceCensor(ConventionService conventionService,
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

        this.authService.authorizeForce(Permission.BrowseDescriptionReference, Permission.DeferredAffiliation);
        FieldSet descriptionFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionReference._description));
        this.censorFactory.censor(DescriptionCensor.class).censor(descriptionFields, userId);
        FieldSet referenceFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionReference._reference));
        this.censorFactory.censor(ReferenceCensor.class).censor(referenceFields, userId);
        FieldSet dataFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionReference._data));
        this.censorFactory.censor(DescriptionReferenceDataCensor.class).censor(dataFields, userId);
    }

}
