package org.opencdmp.commons.scope.user;

import org.opencdmp.commons.scope.DynamicScopeFactory;
import org.opencdmp.commons.scope.ScopeContextDetector;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class UserScopeFactory extends DynamicScopeFactory<UserScope> {
    public UserScopeFactory(ApplicationContext applicationContext, ScopeContextDetector scopeDetector) {
        super(applicationContext, UserScope.class, scopeDetector);
    }

    @Override
    protected String getRequestScopeBeanName() {
        return UserScopeImpl.QualifierName;
    }

    @Override
    protected String getWebSocketScopeBeanName() {
        return WebSocketUserScopeImpl.QualifierName;
    }
}
