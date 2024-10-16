//package org.opencdmp.data.tenant;
//
//import jakarta.persistence.EntityManager;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.hibernate.Session;
//import org.opencdmp.commons.scope.tenant.TenantScope;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.management.InvalidApplicationException;
//
//
//@Aspect
//@Component
////@ConditionalOnMissingBean(TenantScope.class)
//public class TenantFilterAspect {
//
//    private final TenantScope tenantScope;
//
//    @Autowired
//    public TenantFilterAspect(
//            TenantScope tenantScope
//    ) {
//        this.tenantScope = tenantScope;
//    }
//
//    @AfterReturning(
//            pointcut = "bean(entityManagerFactory) && execution(* createEntityManager(..))",
//            returning = "retVal")
//    public void getSessionAfter(JoinPoint joinPoint, Object retVal) throws InvalidApplicationException {
//        if (retVal instanceof EntityManager && this.tenantScope.isSet()) {
//            Session session = ((EntityManager) retVal).unwrap(Session.class);
//            if(!this.tenantScope.isDefaultTenant()) {
//                session
//                        .enableFilter(TenantScopedBaseEntity.TENANT_FILTER)
//                        .setParameter(TenantScopedBaseEntity.TENANT_FILTER_TENANT_PARAM, this.tenantScope.getTenant().toString());
//            } else {
//                session
//                        .enableFilter(TenantScopedBaseEntity.DEFAULT_TENANT_FILTER);
//            }
//        }
//    }
//}
