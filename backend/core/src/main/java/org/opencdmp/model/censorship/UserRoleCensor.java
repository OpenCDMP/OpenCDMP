package org.opencdmp.model.censorship;

import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.censorship.descriptionreference.DescriptionReferenceCensor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.opencdmp.model.UserRole;

import java.util.List;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserRoleCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserRoleCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public UserRoleCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;
        this.authService.authorizeAtLeastOneForce(userId != null ? List.of(new OwnedResource(userId)) : null, Permission.BrowseUser);

        FieldSet userFields = fields.extractPrefixed(this.asIndexerPrefix(UserRole._user));
        this.censorFactory.censor(UserCensor.class).censor(userFields, userId);
    }

}
