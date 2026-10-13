package org.opencdmp.service.maintenance;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.model.maintenance.DatabaseMigration;
import org.opencdmp.model.maintenance.DatabaseMigrationInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseMigrationServiceImpl implements DatabaseMigrationService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DatabaseMigrationServiceImpl.class));

    private final AuthorizationService authorizationService;
    private final ObjectProvider<Flyway> flyway;

    public DatabaseMigrationServiceImpl(AuthorizationService authorizationService, ObjectProvider<Flyway> flyway) {
        this.authorizationService = authorizationService;
        this.flyway = flyway;
    }

    @Override
    public DatabaseMigrationInfo info() {
        logger.debug("database migration info");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        DatabaseMigrationInfo model = new DatabaseMigrationInfo();
        Flyway flyway = this.flyway.getIfAvailable();
        model.setEnabled(flyway != null);
        if (flyway == null) return model;

        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        model.setCurrentVersion(current == null || current.getVersion() == null ? null : current.getVersion().getVersion());
        model.setApplied(info.applied().length);
        model.setPending(info.pending().length);

        int failed = 0;
        List<DatabaseMigration> migrations = new ArrayList<>();
        MigrationInfo[] all = info.all();
        // newest first
        for (int i = all.length - 1; i >= 0; i--) {
            MigrationInfo migration = all[i];
            if (migration.getState().isFailed()) failed++;
            DatabaseMigration item = new DatabaseMigration();
            item.setVersion(migration.getVersion() == null ? null : migration.getVersion().getVersion());
            item.setDescription(migration.getDescription());
            item.setType(migration.getType() == null ? null : migration.getType().name());
            item.setInstalledOn(migration.getInstalledOn() == null ? null : migration.getInstalledOn().toInstant());
            item.setExecutionTime(migration.getExecutionTime());
            item.setState(migration.getState().name());
            migrations.add(item);
        }
        model.setFailed(failed);
        model.setMigrations(migrations);
        return model;
    }
}
