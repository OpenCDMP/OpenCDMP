package org.opencdmp.service.maintenance;

import org.opencdmp.model.maintenance.InstallationInfo;
import org.opencdmp.model.persist.InstallationPersist;

import javax.management.InvalidApplicationException;

public interface InstallationService {

	InstallationInfo info();

	InstallationInfo persist(InstallationPersist model) throws InvalidApplicationException;
}
