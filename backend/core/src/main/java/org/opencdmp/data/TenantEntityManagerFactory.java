package org.opencdmp.data;

import org.opencdmp.commons.scope.DynamicScopeFactory;
import org.opencdmp.commons.scope.ScopeContextDetector;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class TenantEntityManagerFactory extends DynamicScopeFactory<TenantEntityManager> {
    public TenantEntityManagerFactory(ApplicationContext applicationContext, ScopeContextDetector scopeDetector) {
        super(applicationContext, TenantEntityManager.class, scopeDetector);
    }

    @Override
    protected String getRequestScopeBeanName() {
        return TenantEntityManagerImpl.QualifierName;
    }

    @Override
    protected String getWebSocketScopeBeanName() {
        return WebSocketTenantEntityManagerImpl.QualifierName;
    }
}
