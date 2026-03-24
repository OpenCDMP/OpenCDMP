package org.opencdmp.commons.scope;

import org.springframework.context.ApplicationContext;

public abstract class DynamicScopeFactory<T> {
    private final ApplicationContext applicationContext;
    private final Class<T> serviceType;
    private final ScopeContextDetector scopeDetector;

    protected DynamicScopeFactory(ApplicationContext applicationContext, Class<T> serviceType, ScopeContextDetector scopeDetector) {
        this.applicationContext = applicationContext;
        this.serviceType = serviceType;
        this.scopeDetector = scopeDetector;
    }

    /**
     * Gets the appropriate service instance based on current scope.
     */
    public T getInstance() {
        ScopeContextDetector.ScopeType scope = scopeDetector.detectCurrentScope();

        String beanName = switch (scope) {
            case REQUEST -> getRequestScopeBeanName();
            case WEBSOCKET -> getWebSocketScopeBeanName();
            case NONE -> getFallbackBeanName();
        };

        if (beanName == null) {
            return null;
//			throw new IllegalStateException("No bean configured for scope: " + scope);
        }

        return applicationContext.getBean(beanName, serviceType);
    }

    protected abstract String getRequestScopeBeanName();

    protected abstract String getWebSocketScopeBeanName();

    protected String getFallbackBeanName() { return  getRequestScopeBeanName(); }
}
