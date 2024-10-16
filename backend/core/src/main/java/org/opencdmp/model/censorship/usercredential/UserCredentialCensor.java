package org.opencdmp.model.censorship.usercredential;

import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.usercredential.UserCredential;
import org.opencdmp.model.censorship.descriptionreference.DescriptionReferenceCensor;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserCredentialCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserCredentialCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public UserCredentialCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;
        this.authService.authorizeAtLeastOneForce(userId != null ? List.of(new OwnedResource(userId)) : null, Permission.BrowseUser);

        FieldSet descriptionReferenceFields = fields.extractPrefixed(this.asIndexerPrefix(UserCredential._user));
        this.censorFactory.censor(DescriptionReferenceCensor.class).censor(descriptionReferenceFields, userId);

        FieldSet dataFields = fields.extractPrefixed(this.asIndexerPrefix(UserCredential._data));
        this.censorFactory.censor(UserCredentialDataCensor.class).censor(dataFields, userId);
    }

}
