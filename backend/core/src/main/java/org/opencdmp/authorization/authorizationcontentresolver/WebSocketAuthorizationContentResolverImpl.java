package org.opencdmp.authorization.authorizationcontentresolver;

import gr.cite.tools.data.query.QueryFactory;
import org.opencdmp.authorization.PermissionNameProvider;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.tenant.WebSocketTenantScopeImpl;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service(WebSocketAuthorizationContentResolverImpl.QualifierName)
@Qualifier(WebSocketAuthorizationContentResolverImpl.QualifierName)
@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebSocketAuthorizationContentResolverImpl extends AuthorizationContentResolverImpl {
	public static final String QualifierName = "webSocketAuthorizationContentResolver";
	public WebSocketAuthorizationContentResolverImpl(QueryFactory queryFactory, UserScopeFactory userScope, TenantScopeFactory tenantScopeFactory, AffiliationCacheService affiliationCacheService, PermissionNameProvider permissionNameProvider, TenantEntityManagerFactory tenantEntityManager) {
		super(queryFactory, userScope, tenantScopeFactory, affiliationCacheService, permissionNameProvider, tenantEntityManager);
	}
}
