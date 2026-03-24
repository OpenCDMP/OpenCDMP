package org.opencdmp.authorization.authorizationcontentresolver;

import org.opencdmp.commons.scope.DynamicScopeFactory;
import org.opencdmp.commons.scope.ScopeContextDetector;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationContentResolverFactory extends DynamicScopeFactory<AuthorizationContentResolver> {
    public AuthorizationContentResolverFactory(ApplicationContext applicationContext, ScopeContextDetector scopeDetector) {
        super(applicationContext, AuthorizationContentResolver.class, scopeDetector);
    }

    @Override
    protected String getRequestScopeBeanName() {
        return AuthorizationContentResolverImpl.QualifierName;
    }

    @Override
    protected String getWebSocketScopeBeanName() {
        return WebSocketAuthorizationContentResolverImpl.QualifierName;
    }
}
