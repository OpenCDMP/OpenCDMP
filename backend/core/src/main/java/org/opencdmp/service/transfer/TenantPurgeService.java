package org.opencdmp.service.transfer;

import javax.management.InvalidApplicationException;

public interface TenantPurgeService {

    TenantPurgeReport analyzePurge(TenantPurgeRequest request) throws InvalidApplicationException;

    TenantPurgeReport purge(TenantPurgeRequest request) throws InvalidApplicationException;
}
