package org.opencdmp.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;

import javax.management.InvalidApplicationException;

public interface TenantEntityManager {

    void persist(Object entity);

    <T> T merge(T entity) throws InvalidApplicationException;

    void remove(Object entity) throws InvalidApplicationException;

    <T> T find(Class<T> entityClass, Object primaryKey) throws InvalidApplicationException;

    <T> T find(Class<T> entityClass, Object primaryKey, boolean disableTracking) throws InvalidApplicationException;

    void flush();

    void setFlushMode(FlushModeType flushMode);

    FlushModeType getFlushMode();

    void clear();

    void reloadTenantFilters() throws InvalidApplicationException;

    void loadExplicitTenantFilters() throws InvalidApplicationException;

    void disableTenantFilters();

    boolean isTenantFiltersDisabled();

    EntityManager getEntityManager();

    void setEntityManager(EntityManager entityManager);
}
