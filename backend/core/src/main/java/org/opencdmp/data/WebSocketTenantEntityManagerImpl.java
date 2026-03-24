package org.opencdmp.data;

import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service(WebSocketTenantEntityManagerImpl.QualifierName)
@Qualifier(WebSocketTenantEntityManagerImpl.QualifierName)
@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebSocketTenantEntityManagerImpl extends TenantEntityManagerImpl {
    public static final String QualifierName = "webSocketTenantEntityManagerImpl";

    public WebSocketTenantEntityManagerImpl(TenantScopeFactory tenantScopeFactory, ErrorThesaurusProperties errors) {
        super(tenantScopeFactory, errors);
    }
}
