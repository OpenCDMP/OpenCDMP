package org.opencdmp.data.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.opencdmp.commons.scope.tenant.TenantScoped;

import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
//@Getter
//@Setter
//@NoArgsConstructor
@FilterDef(name = TenantScopedBaseEntity.TENANT_FILTER, parameters = @ParamDef(name = TenantScopedBaseEntity.TENANT_FILTER_TENANT_PARAM, type = String.class))
@FilterDef(name = TenantScopedBaseEntity.TENANT_FILTER_EXPLICT, parameters = @ParamDef(name = TenantScopedBaseEntity.TENANT_FILTER_TENANT_PARAM, type = String.class))
@FilterDef(name = TenantScopedBaseEntity.DEFAULT_TENANT_FILTER)
@Filter(name = TenantScopedBaseEntity.TENANT_FILTER, condition = "(tenant = (cast(:tenantId as uuid)) or tenant is null)")
@Filter(name = TenantScopedBaseEntity.TENANT_FILTER_EXPLICT, condition = "(tenant = (cast(:tenantId as uuid)))")
@Filter(name = TenantScopedBaseEntity.DEFAULT_TENANT_FILTER, condition = "(tenant = tenant is null)")
@EntityListeners(TenantListener.class)
public abstract class TenantScopedBaseEntity implements TenantScoped, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String TENANT_FILTER = "tenantFilter";
    public static final String DEFAULT_TENANT_FILTER = "defaultTenantFilter";
    public static final String TENANT_FILTER_EXPLICT = "tenantFilterExplict";
    public static final String TENANT_FILTER_TENANT_PARAM = "tenantId";

    @Column(name = "tenant", columnDefinition = "uuid", nullable = true)
    private UUID tenantId;
    public static final String _tenantId = "tenantId";
    public UUID getTenantId() {
        return this.tenantId;
    }

    @Override
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}
