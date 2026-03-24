package org.opencdmp.commons.scope.tenant;

import org.opencdmp.data.TenantEntityManager;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface TenantScope {

    Boolean isMultitenant();

    Boolean supportExpansionTenant();

    String getDefaultTenantCode();

    Boolean isSet();

    Boolean isDefaultTenant();

    UUID getTenant() throws InvalidApplicationException;

    String getTenantCode() throws InvalidApplicationException;

    void setTempTenant(TenantEntityManager entityManager, UUID tenant, String tenantCode) throws InvalidApplicationException;

    void removeTempTenant(TenantEntityManager entityManager) throws InvalidApplicationException;

    void setTenant(UUID tenant, String tenantCode);
}
