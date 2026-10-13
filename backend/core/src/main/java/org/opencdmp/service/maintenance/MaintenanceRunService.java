package org.opencdmp.service.maintenance;

import org.opencdmp.model.maintenance.MaintenanceRun;
import org.opencdmp.model.maintenance.MaintenanceTaskRun;
import org.opencdmp.model.persist.MaintenanceRunPersist;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.UUID;

public interface MaintenanceRunService {

	MaintenanceRun start(MaintenanceRunPersist model) throws InvalidApplicationException;

	MaintenanceRun latest();

	MaintenanceRun stop(UUID id);

	List<MaintenanceTaskRun> lastTaskRuns();
}
