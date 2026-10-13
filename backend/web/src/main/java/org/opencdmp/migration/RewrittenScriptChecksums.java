package org.opencdmp.migration;

import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

// These scripts were made idempotent after some databases had already applied them through Flyway. Their content
// changed, not their effect, so a history row that still holds the checksum of the earlier text is moved to the
// current one. Any other mismatch is left for validation to report.
@Component
public class RewrittenScriptChecksums implements Callback {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(RewrittenScriptChecksums.class));

    private static class Rewrite {
        final String version;
        final int before;
        final int after;

        Rewrite(String version, int before, int after) {
            this.version = version;
            this.before = before;
            this.after = after;
        }
    }

    private static final List<Rewrite> REWRITES = List.of(
            new Rewrite("01.00.013", 105610405, -1708033981),
            new Rewrite("01.00.014", 2006728667, 2112123805),
            new Rewrite("01.01.001", -1204985161, 2123335127),
            new Rewrite("01.01.002", -2105450468, 1281955132),
            new Rewrite("01.01.003", 2104598794, 648327707),
            new Rewrite("01.01.006", 996423095, -72233067),
            new Rewrite("01.01.007", 816593903, -1472044555),
            new Rewrite("01.01.009", 638740263, -979897631)
    );

    @Override
    public boolean supports(Event event, Context context) {
        return event == Event.BEFORE_VALIDATE;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
        String table = context.getConfiguration().getTable();
        Connection connection = context.getConnection();
        try {
            try (PreparedStatement exists = connection.prepareStatement("SELECT to_regclass('public.\"' || ? || '\"') IS NOT NULL")) {
                exists.setString(1, table);
                try (ResultSet resultSet = exists.executeQuery()) {
                    resultSet.next();
                    if (!resultSet.getBoolean(1)) return;
                }
            }
            try (PreparedStatement update = connection.prepareStatement("UPDATE public.\"" + table + "\" SET checksum = ? WHERE version = ? AND checksum = ?")) {
                for (Rewrite rewrite : REWRITES) {
                    update.setInt(1, rewrite.after);
                    update.setString(2, rewrite.version);
                    update.setInt(3, rewrite.before);
                    if (update.executeUpdate() > 0) logger.info("Migration {} was applied before it was made idempotent; recorded the checksum of its current script", rewrite.version);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Could not update the checksums of the rewritten migration scripts", ex);
        }
    }

    @Override
    public String getCallbackName() {
        return "RewrittenScriptChecksums";
    }
}
