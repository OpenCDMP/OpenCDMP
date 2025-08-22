package org.opencdmp.controllers;

import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.model.Tenant;
import org.opencdmp.models.Account;
import org.opencdmp.models.AccountBuilder;
import org.opencdmp.service.tenant.TenantService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.List;

@RestController
@RequestMapping("/api/principal/")
@Tag(name = "Principal", description = "Get user account information")
@SwaggerCommonErrorResponses
public class PrincipalController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PrincipalController.class));

    private final AuditService auditService;

    private final CurrentPrincipalResolver currentPrincipalResolver;

    private final AccountBuilder accountBuilder;

    private final TenantService tenantService;

    @Autowired
    public PrincipalController(
            CurrentPrincipalResolver currentPrincipalResolver,
            AccountBuilder accountBuilder,
            AuditService auditService, TenantService tenantService) {
        this.currentPrincipalResolver = currentPrincipalResolver;
        this.accountBuilder = accountBuilder;
        this.auditService = auditService;
        this.tenantService = tenantService;
    }

    @RequestMapping(path = "me", method = RequestMethod.GET)
    @OperationWithTenantHeader(summary = "Fetch auth information of the logged in user", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Account.class
                    ))
            ),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "1")))
    public Account me(
            @RequestParam(required = false) @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = false, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.Principal.endpoint_field_set_example, allowableValues = SwaggerHelpers.Principal.available_field_set )) FieldSet fieldSet
    ) throws InvalidApplicationException {
        logger.debug("me");

        if (fieldSet == null || fieldSet.isEmpty()) {
            fieldSet = new BaseFieldSet(
                    Account._isAuthenticated,
                    Account._userExists,
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._subject),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._userId),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._name),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._scope),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._client),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._issuedAt),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._notBefore),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._authenticatedAt),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._expiresAt),
                    BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._more),
                    BaseFieldSet.asIndexer(Account._profile, Account.UserProfileInfo._avatarUrl),
                    BaseFieldSet.asIndexer(Account._profile, Account.UserProfileInfo._language),
                    BaseFieldSet.asIndexer(Account._profile, Account.UserProfileInfo._culture),
                    BaseFieldSet.asIndexer(Account._profile, Account.UserProfileInfo._timezone),
                    Account._permissions,
                    BaseFieldSet.asIndexer(Account._selectedTenant, Tenant._id),
                    BaseFieldSet.asIndexer(Account._selectedTenant, Tenant._name),
                    BaseFieldSet.asIndexer(Account._selectedTenant, Tenant._code));
        }

        MyPrincipal principal = this.currentPrincipalResolver.currentPrincipal();

        Account me = this.accountBuilder.build(fieldSet, principal);

        this.auditService.track(AuditableAction.Principal_Lookup);
        //auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return me;
    }

    @GetMapping("my-tenants")
    @OperationWithTenantHeader(summary = "Fetch a list with the tenants the user belongs to", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "2")))
    public List<Tenant> myTenants(
            @RequestParam(required = false) @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = false, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example =  SwaggerHelpers.Principal.endpoint_field_set_example , allowableValues = SwaggerHelpers.Principal.available_field_set )) FieldSet fieldSet
    ) {
        logger.debug("my-tenants");

        List<Tenant> models = this.tenantService.myTenants(fieldSet);

        this.auditService.track(AuditableAction.Principal_MyTenants);
        //auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return models;
    }

}
