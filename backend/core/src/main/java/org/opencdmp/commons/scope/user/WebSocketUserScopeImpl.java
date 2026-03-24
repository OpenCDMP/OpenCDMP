package org.opencdmp.commons.scope.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service(WebSocketUserScopeImpl.QualifierName)
@Qualifier(WebSocketUserScopeImpl.QualifierName)
@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebSocketUserScopeImpl extends UserScopeImpl{
    public static final String QualifierName = "webSocketUserScopeImpl";
}