package org.opencdmp.integrationevent.outbox.indicatoraccess;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.TenantUserEntity;
import org.opencdmp.data.UserRoleEntity;
import org.opencdmp.integrationevent.outbox.OutboxIntegrationEvent;
import org.opencdmp.integrationevent.outbox.OutboxService;
import org.opencdmp.model.Tenant;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.TenantUserQuery;
import org.opencdmp.query.UserRoleQuery;
import org.opencdmp.service.kpi.KpiProperties;
import org.opencdmp.service.kpi.KpiSchemaFieldCodes;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndicatorAccessEventHandlerImpl implements IndicatorAccessEventHandler {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(IndicatorAccessEventHandlerImpl.class));

    private final OutboxService outboxService;

    private final KpiProperties kpiProperties;

    private final ValidatorFactory validatorFactory;

    private final QueryFactory queryFactory;

    private final TenantEntityManager entityManager;

    private final TenantScope tenantScope;

    private final ConventionService conventionService;

    @Autowired
    public IndicatorAccessEventHandlerImpl(OutboxService outboxService, KpiProperties kpiProperties, ValidatorFactory validatorFactory, QueryFactory queryFactory, TenantEntityManager entityManager, TenantScope tenantScope, ConventionService conventionService) {
        this.outboxService = outboxService;
        this.kpiProperties = kpiProperties;
        this.validatorFactory = validatorFactory;
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
        this.tenantScope = tenantScope;
        this.conventionService = conventionService;
    }

    @Override
    public void handle(UUID userId) throws InvalidApplicationException {
        OutboxIntegrationEvent message = new OutboxIntegrationEvent();
        message.setMessageId(UUID.randomUUID());
        message.setType(OutboxIntegrationEvent.INDICATOR_ACCESS_ENTRY);

        try {
            this.entityManager.disableTenantFilters();
            List<String> allowedRoles = new ArrayList<>();
            allowedRoles.addAll(this.kpiProperties.getIndicator().getRoles());
            allowedRoles.addAll(this.kpiProperties.getIndicator().getTenantRoles());

            List<UserRoleEntity> userRoleEntities = this.queryFactory.query(UserRoleQuery.class).disableTracking().userIds(userId).roles(allowedRoles).collect();

            if (this.conventionService.isListNullOrEmpty(userRoleEntities)) {
                message.setEvent(this.createIndicatorAccessEvent(userId, null, true));
                message.setTenantId(null);
                this.outboxService.publish(message);
                return;
            }

            UserRoleEntity globalRole = userRoleEntities.stream().filter(x -> this.kpiProperties.getIndicator().getRoles().contains(x.getRole())).findFirst().orElse(null);
            UserRoleEntity defaultTenantRole = userRoleEntities.stream().filter(x -> x.getTenantId() == null && this.kpiProperties.getIndicator().getTenantRoles().contains(x.getRole())).findFirst().orElse(null);

            List<String> tenantCodes = new ArrayList<>();

            if (defaultTenantRole != null || globalRole != null) tenantCodes.add(this.tenantScope.getDefaultTenantCode());

            List<TenantEntity> tenantEntities = null;
            List<TenantUserEntity> tenantUserEntities = this.queryFactory.query(TenantUserQuery.class).disableTracking().userIds(userId).isActive(IsActive.Active).collect();
            if (!this.conventionService.isListNullOrEmpty(tenantUserEntities)) {
                tenantEntities = this.queryFactory.query(TenantQuery.class).disableTracking().isActive(IsActive.Active).ids(tenantUserEntities.stream().map(TenantUserEntity::getTenantId).distinct().toList()).collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
                if (tenantEntities != null) {
                    for (TenantUserEntity user: tenantUserEntities) {
                        UserRoleEntity tenantRole = userRoleEntities.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(user.getTenantId()) && this.kpiProperties.getIndicator().getTenantRoles().contains(x.getRole())).findFirst().orElse(null);
                        if (globalRole != null || tenantRole != null) {
                            TenantEntity tenant = tenantEntities.stream().filter(x -> x.getId().equals(user.getTenantId())).findFirst().orElse(null);
                            // collect tenant codes
                            if (tenant != null) tenantCodes.add(tenant.getCode());
                        }
                    }
                }
            }
            if (globalRole != null || defaultTenantRole != null) {
                message.setEvent(this.createIndicatorAccessEvent(userId, tenantCodes, false));
                message.setTenantId(null);
                this.outboxService.publish(message);
            }

            if (tenantEntities != null && !tenantEntities.isEmpty()) {
                for (TenantUserEntity user: tenantUserEntities) {
                    UserRoleEntity tenantRole = userRoleEntities.stream().filter(x -> x.getTenantId() != null && x.getTenantId().equals(user.getTenantId()) && this.kpiProperties.getIndicator().getTenantRoles().contains(x.getRole())).findFirst().orElse(null);
                    if (globalRole != null || tenantRole != null) {
                        TenantEntity tenant = tenantEntities.stream().filter(x -> x.getId().equals(user.getTenantId())).findFirst().orElse(null);
                        if (tenant != null){
                            message.setEvent(this.createIndicatorAccessEvent(userId, tenantCodes, false));
                            message.setTenantId(tenant.getId());
                            this.outboxService.publish(message);
                        }
                    }
                }
            }

        } finally {
            this.entityManager.reloadTenantFilters();
        }

    }

    private IndicatorAccessEvent createIndicatorAccessEvent(UUID userId, List<String> tenantCodes, boolean toDelete) {
        IndicatorAccessEvent event = new IndicatorAccessEvent();
        event.setIndicatorId(this.kpiProperties.getIndicator().getId());
        event.setUserId(userId);

        if (!this.conventionService.isListNullOrEmpty(tenantCodes)) {
            List<FilterColumnConfig> globalFilterColumns = new ArrayList<>();
            FilterColumnConfig filterColumn = new FilterColumnConfig();
            filterColumn.setColumn(KpiSchemaFieldCodes.TenantCode);
            filterColumn.setValues(tenantCodes);
            globalFilterColumns.add(filterColumn);

            IndicatorAccessConfig config = new IndicatorAccessConfig();
            config.setGlobalFilterColumns(globalFilterColumns);

            event.setConfig(config);
        }

        event.setToDelete(toDelete);

        this.validatorFactory.validator(IndicatorAccessEvent.IndicatorAccessEventValidator.class).validateForce(event);
        return event;
    }
}
