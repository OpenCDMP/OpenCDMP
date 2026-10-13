package org.opencdmp.migration;

import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
public class V01_00_010__Baseline extends BaseJavaMigration {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(V01_00_010__Baseline.class));

    private static final String BASELINE_SCRIPT = "db/baseline/01.00.010-baseline.sql";

    private interface SchemaCheck {
        boolean holds(Connection connection) throws SQLException;
    }

    private static class FloorChange {
        final String version;
        final String description;
        final SchemaCheck present;

        FloorChange(String version, String description, SchemaCheck present) {
            this.version = version;
            this.description = description;
            this.present = present;
        }
    }

    // Judged by the schema, not by DBVersion rows: installs built from a seed script or patched by hand have these
    // changes without the rows.
    private static final List<FloorChange> FLOOR = List.of(
            new FloorChange("01.00.001", "PlanStatus and DescriptionStatus ordinal columns", c -> column(c, "PlanStatus", "ordinal") && column(c, "DescriptionStatus", "ordinal")),
            new FloorChange("01.00.003", "Plan and Description status as uuid", c -> columnType(c, "Plan", "status", "uuid") && columnType(c, "Description", "status", "uuid")),
            new FloorChange("01.00.005", "Evaluation table", c -> table(c, "Evaluation")),
            new FloorChange("01.00.006", "PlanBlueprint description column", c -> column(c, "PlanBlueprint", "description")),
            new FloorChange("01.00.010", "PlanUser ordinal column", c -> column(c, "PlanUser", "ordinal"))
    );

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!table(connection, "DBVersion")) {
            logger.info("No OpenCDMP schema found, creating it from {}", BASELINE_SCRIPT);
            try (Statement statement = connection.createStatement()) {
                statement.execute(StreamUtils.copyToString(new ClassPathResource(BASELINE_SCRIPT).getInputStream(), StandardCharsets.UTF_8));
            }
            return;
        }

        List<String> missing = new ArrayList<>();
        for (FloorChange change : FLOOR) {
            if (!change.present.holds(connection)) missing.add(change.version + " (" + change.description + ")");
        }
        if (!missing.isEmpty()) {
            throw new FlywayException("The database is older than OpenCDMP 1.2: it lacks the changes of " + String.join(", ", missing) + ". Apply the scripts up to 01.00.010 from the db/ folder of OpenCDMP 1.3.0 by hand, then restart.");
        }
    }

    private static boolean table(Connection connection, String name) throws SQLException {
        return exists(connection, "SELECT to_regclass('public.\"' || ? || '\"') IS NOT NULL", name);
    }

    private static boolean column(Connection connection, String table, String column) throws SQLException {
        return exists(connection, "SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?)", table, column);
    }

    private static boolean columnType(Connection connection, String table, String column, String dataType) throws SQLException {
        return exists(connection, "SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ? AND data_type = ?)", table, column, dataType);
    }

    private static boolean exists(Connection connection, String sql, String... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) statement.setString(i + 1, parameters[i]);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }
}
