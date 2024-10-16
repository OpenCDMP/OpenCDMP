package org.opencdmp.commons.scope.tenant;

import org.opencdmp.data.TenantEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequestScope
public class TenantScope {
    public static final String TenantReplaceParameter = "::TenantCode::";
    private final MultitenancyConfiguration multitenancyConfiguration;
    private final AtomicReference<UUID> tenant = new AtomicReference<>();
    private final AtomicReference<String> tenantCode = new AtomicReference<>();
    private final AtomicReference<UUID> initialTenant = new AtomicReference<>();
    private final AtomicReference<String> initialTenantCode = new AtomicReference<>();

    @Autowired
    public TenantScope(MultitenancyConfiguration multitenancyConfiguration) {
        this.multitenancyConfiguration = multitenancyConfiguration;
    }

    public Boolean isMultitenant() {
        return this.multitenancyConfiguration.getConfig().isMultitenant();
    }

    public Boolean supportExpansionTenant() {
        return this.multitenancyConfiguration.getConfig().getSupportExpansionTenant();
    }

    public String getDefaultTenantCode() {
        return this.multitenancyConfiguration.getConfig().getDefaultTenantCode();
    }

    public Boolean isSet() {
        if (!this.isMultitenant())
            return Boolean.TRUE;
        return this.tenant.get() != null || this.isDefaultTenant();
    }

    public Boolean isDefaultTenant() {
        if (!this.isMultitenant())
            return Boolean.FALSE;
        return this.supportExpansionTenant() && this.multitenancyConfiguration.getConfig().getDefaultTenantCode().equalsIgnoreCase(this.tenantCode.get());
    }

    public UUID getTenant() throws InvalidApplicationException {
        if (!this.isMultitenant())
            return null;
        if (this.tenant.get() == null && !this.isDefaultTenant())
            throw new InvalidApplicationException("tenant not set");
        return this.isDefaultTenant() ? null : this.tenant.get();
    }

    public String getTenantCode() throws InvalidApplicationException {
        if (!this.isMultitenant())
            return null;
        if (this.tenantCode.get() == null)
            throw new InvalidApplicationException("tenant not set");
        return this.tenantCode.get();
    }

    public void setTempTenant(TenantEntityManager entityManager, UUID tenant, String tenantCode) throws InvalidApplicationException {
        this.tenant.set(tenant);
        this.tenantCode.set(tenantCode);

        entityManager.reloadTenantFilters();
    }

    public void removeTempTenant(TenantEntityManager entityManager) throws InvalidApplicationException {
        this.tenant.set(this.initialTenant.get());
        this.tenantCode.set(this.initialTenantCode.get());

        entityManager.reloadTenantFilters();
    }

    public void setTenant(UUID tenant, String tenantCode) {
        if (this.isMultitenant()) {
            this.tenant.set(tenant);
            this.initialTenant.set(tenant);
            this.tenantCode.set(tenantCode);
            this.initialTenantCode.set(tenantCode);
        }
    }
}

