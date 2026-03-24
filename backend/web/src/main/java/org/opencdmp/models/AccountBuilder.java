package org.opencdmp.models;

import gr.cite.commons.web.authz.configuration.Permission;
import gr.cite.commons.web.authz.configuration.PermissionPolicyContext;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolverFactory;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorFactory;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractorKeys;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolverFactory;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.commons.types.user.AdditionalInfoEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.UserEntity;
import org.opencdmp.model.builder.TenantBuilder;
import org.opencdmp.query.TenantQuery;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AccountBuilder {

    private final ClaimExtractorFactory claimExtractorFactory;
    private final Set<String> excludeMoreClaim;
    private final CurrentPrincipalResolverFactory currentPrincipalResolver;
    private final PermissionPolicyContext permissionPolicyContext;
    private final AuthorizationContentResolverFactory authorizationContentResolverFactory;
    private final JsonHandlingService jsonHandlingService;
    private final UserScopeFactory userScopeFactory;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;

    public AccountBuilder(ClaimExtractorFactory claimExtractorFactory, CurrentPrincipalResolverFactory currentPrincipalResolver, @Qualifier("OpencdmpPermissionPolicyContextImpl") PermissionPolicyContext permissionPolicyContext, AuthorizationContentResolverFactory authorizationContentResolverFactory, JsonHandlingService jsonHandlingService, UserScopeFactory userScopeFactory, TenantEntityManagerFactory tenantEntityManagerFactory, TenantScopeFactory tenantScopeFactory, QueryFactory queryFactory, BuilderFactory builderFactory) {
        this.claimExtractorFactory = claimExtractorFactory;
        this.currentPrincipalResolver = currentPrincipalResolver;
        this.permissionPolicyContext = permissionPolicyContext;
	    this.authorizationContentResolverFactory = authorizationContentResolverFactory;
	    this.jsonHandlingService = jsonHandlingService;
        this.userScopeFactory = userScopeFactory;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.excludeMoreClaim = Set.of(
                ClaimExtractorKeys.Subject,
                ClaimExtractorKeys.Name,
                ClaimExtractorKeys.Scope,
                ClaimExtractorKeys.Client,
                ClaimExtractorKeys.IssuedAt,
                ClaimExtractorKeys.NotBefore,
                ClaimExtractorKeys.AuthenticatedAt,
                ClaimExtractorKeys.ExpiresAt);
    }

    public Account build(FieldSet fields, MyPrincipal principal) throws InvalidApplicationException {
        Account model = new Account();
        if (principal == null || !principal.isAuthenticated()) {
            model.setIsAuthenticated(Boolean.FALSE);
            return model;
        }
        model.setIsAuthenticated(Boolean.TRUE);

        FieldSet principalFields = fields.extractPrefixed(BaseFieldSet.asIndexerPrefix(Account._principal));
        if (!principalFields.isEmpty()) model.setPrincipal(new Account.PrincipalInfo());
        if (principalFields.hasField(Account.PrincipalInfo._subject))
            model.getPrincipal().setSubject(this.claimExtractorFactory.getInstance().subjectUUID(principal));
        if (principalFields.hasField(Account.PrincipalInfo._userId))
            model.getPrincipal().setUserId(this.userScopeFactory.getInstance().getUserIdSafe());
        if (principalFields.hasField(Account.PrincipalInfo._name))
            model.getPrincipal().setName(this.claimExtractorFactory.getInstance().name(principal));
        if (principalFields.hasField(Account.PrincipalInfo._scope))
            model.getPrincipal().setScope(this.claimExtractorFactory.getInstance().scope(principal));
        if (principalFields.hasField(Account.PrincipalInfo._client))
            model.getPrincipal().setClient(this.claimExtractorFactory.getInstance().client(principal));
        if (principalFields.hasField(Account.PrincipalInfo._issuedAt))
            model.getPrincipal().setIssuedAt(this.claimExtractorFactory.getInstance().issuedAt(principal));
        if (principalFields.hasField(Account.PrincipalInfo._notBefore))
            model.getPrincipal().setNotBefore(this.claimExtractorFactory.getInstance().notBefore(principal));
        if (principalFields.hasField(Account.PrincipalInfo._authenticatedAt))
            model.getPrincipal().setAuthenticatedAt(this.claimExtractorFactory.getInstance().authenticatedAt(principal));
        if (principalFields.hasField(Account.PrincipalInfo._expiresAt))
            model.getPrincipal().setExpiresAt(this.claimExtractorFactory.getInstance().expiresAt(principal));
        if (principalFields.hasField(Account.PrincipalInfo._more)) {
            model.getPrincipal().setMore(new HashMap<>());
            for (String key : this.claimExtractorFactory.getInstance().knownPublicKeys()) {
                if (this.excludeMoreClaim.contains(key))
                    continue;
                List<String> values = this.claimExtractorFactory.getInstance().asStrings(principal, key);
                if (values == null || values.isEmpty())
                    continue;
                if (!model.getPrincipal().getMore().containsKey(key))
                    model.getPrincipal().getMore().put(key, new ArrayList<>());
                model.getPrincipal().getMore().get(key).addAll(values);
            }
        }
        if (fields.hasField(Account._permissions)) {
            List<String> roles = this.claimExtractorFactory.getInstance().roles(this.currentPrincipalResolver.getInstance().currentPrincipal());
            Set<String> permissions = this.permissionPolicyContext.permissionsOfRoles(roles);
            for (Map.Entry<String, Permission> permissionEntry : this.permissionPolicyContext.getRawPolicies().entrySet()){
                if (permissionEntry.getValue().getAllowAuthenticated()){
                    permissions.add(permissionEntry.getKey());
                }
            }
            if (!permissions.contains(org.opencdmp.authorization.Permission.ViewDescriptionTemplatePage)){
                if (this.authorizationContentResolverFactory.getInstance().hasAtLeastOneDescriptionTemplateAffiliation()) permissions.add(org.opencdmp.authorization.Permission.ViewDescriptionTemplatePage);
            }
            model.setPermissions(new ArrayList<>(permissions));
        }

        FieldSet selectedTenantFields = fields.extractPrefixed(BaseFieldSet.asIndexerPrefix(Account._selectedTenant));
        if (!selectedTenantFields.isEmpty() && this.tenantScopeFactory.getInstance().isSet()) {

            if (!this.tenantScopeFactory.getInstance().getTenantCode().equalsIgnoreCase(this.tenantScopeFactory.getInstance().getDefaultTenantCode())) {
               TenantQuery query = this.queryFactory.query(TenantQuery.class).disableTracking().ids(this.tenantScopeFactory.getInstance().getTenant());
                model.setSelectedTenant(this.builderFactory.builder(TenantBuilder.class).build(selectedTenantFields, query.first()));
            }
        }

        FieldSet profileFields = fields.extractPrefixed(BaseFieldSet.asIndexerPrefix(Account._profile));

        UserEntity data = (fields.hasField(Account._userExists)  || !profileFields.isEmpty()) && this.userScopeFactory.getInstance().getUserIdSafe() != null ? this.tenantEntityManagerFactory.getInstance().find(UserEntity.class, this.userScopeFactory.getInstance().getUserIdSafe()) : null;
        if (fields.hasField(Account._userExists)) model.setUserExists(data != null);
        if (!profileFields.isEmpty()){
            model.setProfile(new Account.UserProfileInfo());
            AdditionalInfoEntity additionalInfoEntity = data == null ? null : this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, data.getAdditionalInfo());
            if (profileFields.hasField(Account.UserProfileInfo._avatarUrl) && additionalInfoEntity != null) model.getProfile().setAvatarUrl(additionalInfoEntity.getAvatarUrl());
            if (profileFields.hasField(Account.UserProfileInfo._language) && additionalInfoEntity != null) model.getProfile().setLanguage(additionalInfoEntity.getLanguage());
            if (profileFields.hasField(Account.UserProfileInfo._timezone) && additionalInfoEntity != null) model.getProfile().setTimezone(additionalInfoEntity.getTimezone());
            if (profileFields.hasField(Account.UserProfileInfo._culture) && additionalInfoEntity != null) model.getProfile().setCulture(additionalInfoEntity.getCulture());
        }
       
        return model;
    }
}
