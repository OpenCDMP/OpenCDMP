package org.opencdmp.commons.scope.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tenant.multitenancy")
public class MultitenancyProperties {
    private boolean isMultitenant;
    private boolean supportExpansionTenant;
    private String defaultTenantCode;

    public boolean isMultitenant() {
        return this.isMultitenant;
    }

    public void setIsMultitenant(boolean multitenant) {
	    this.isMultitenant = multitenant;
    }

    public void setMultitenant(boolean multitenant) {
	    this.isMultitenant = multitenant;
    }

    public String getDefaultTenantCode() {
        return this.defaultTenantCode;
    }

    public void setDefaultTenantCode(String defaultTenantCode) {
        this.defaultTenantCode = defaultTenantCode;
    }

    public boolean getSupportExpansionTenant() {
        return this.supportExpansionTenant;
    }

    public void setSupportExpansionTenant(boolean supportExpansionTenant) {
        this.supportExpansionTenant = supportExpansionTenant;
    }
}

