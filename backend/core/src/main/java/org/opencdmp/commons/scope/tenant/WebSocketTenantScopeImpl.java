package org.opencdmp.commons.scope.tenant;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Qualifier(WebSocketTenantScopeImpl.QualifierName)
@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebSocketTenantScopeImpl extends TenantScopeImpl {
    public static final String QualifierName = "webSocketTenantScopeImpl";
    public WebSocketTenantScopeImpl(MultitenancyConfiguration multitenancyConfiguration) {
        super(multitenancyConfiguration);
    }
}
