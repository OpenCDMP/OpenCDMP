package org.opencdmp.model.censorship;

import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.usercredential.UserCredentialCensor;
import org.opencdmp.model.user.User;
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
public class UserCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public UserCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;
        this.authService.authorizeAtLeastOneForce(userId != null ? List.of(new OwnedResource(userId)) : null, Permission.BrowseUser, Permission.DeferredAffiliation);

        FieldSet globalRolesFields = fields.extractPrefixed(this.asIndexerPrefix(User._globalRoles));
        this.censorFactory.censor(UserRoleCensor.class).censor(globalRolesFields, userId);

        FieldSet tenantRolesFields = fields.extractPrefixed(this.asIndexerPrefix(User._tenantRoles));
        this.censorFactory.censor(UserRoleCensor.class).censor(tenantRolesFields, userId);

        FieldSet contactsFields = fields.extractPrefixed(this.asIndexerPrefix(User._contacts));
        this.censorFactory.censor(UserContactInfoCensor.class).censor(contactsFields, userId);

        FieldSet credentialsFields = fields.extractPrefixed(this.asIndexerPrefix(User._credentials));
        this.censorFactory.censor(UserCredentialCensor.class).censor(credentialsFields, userId);

        FieldSet additionalInfoFields = fields.extractPrefixed(this.asIndexerPrefix(User._additionalInfo));
        this.censorFactory.censor(UserAdditionalInfoCensor.class).censor(additionalInfoFields, userId);

        FieldSet tenantUsersFields = fields.extractPrefixed(this.asIndexerPrefix(User._tenantUsers));
        this.censorFactory.censor(TenantUserCensor.class).censor(tenantUsersFields, userId);
    }

}
