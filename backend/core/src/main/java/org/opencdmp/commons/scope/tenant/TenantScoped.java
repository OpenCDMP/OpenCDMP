package org.opencdmp.commons.scope.tenant;

import java.beans.Transient;
import java.util.UUID;

public interface TenantScoped {
    void setTenantId(UUID tenantId);
    UUID getTenantId();
}
