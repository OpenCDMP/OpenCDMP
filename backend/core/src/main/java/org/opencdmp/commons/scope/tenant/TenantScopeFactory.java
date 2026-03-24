package org.opencdmp.commons.scope.tenant;

import org.opencdmp.commons.scope.DynamicScopeFactory;
import org.opencdmp.commons.scope.ScopeContextDetector;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class TenantScopeFactory extends DynamicScopeFactory<TenantScope> {
    public TenantScopeFactory(ApplicationContext applicationContext, ScopeContextDetector scopeDetector) {
        super(applicationContext, TenantScope.class, scopeDetector);
    }

    @Override
    protected String getRequestScopeBeanName() {
        return TenantScopeImpl.QualifierName;
    }

    @Override
    protected String getWebSocketScopeBeanName() {
        return WebSocketTenantScopeImpl.QualifierName;
    }
}
