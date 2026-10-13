package org.opencdmp.migration;

import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.RootSectionEntity;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Must run after V2_0_0_003: parsing a legacy blueprint with the 1.4 model drops its fields, and saving it destroys them.
@Component
public class V2_0_0_004__Set_plan_blueprint_can_edit_description_templates extends BaseJavaMigration {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(V2_0_0_004__Set_plan_blueprint_can_edit_description_templates.class));

    private final XmlHandlingService xmlHandlingService;

    public V2_0_0_004__Set_plan_blueprint_can_edit_description_templates(XmlHandlingService xmlHandlingService) {
        this.xmlHandlingService = xmlHandlingService;
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        int total = 0, updated = 0;
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery("SELECT id, label, definition FROM public.\"PlanBlueprint\"");
             PreparedStatement update = connection.prepareStatement("UPDATE public.\"PlanBlueprint\" SET definition = ?::xml WHERE id = ?")) {
            while (rows.next()) {
                total++;
                UUID id = rows.getObject("id", UUID.class);
                String xml = rows.getString("definition");
                if (xml == null || xml.isBlank()) continue;

                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, xml);
                if (definition == null) throw new FlywayException("The definition of plan blueprint " + id + " could not be parsed. Fix it, then restart.");
                // an empty definition (<root/>) parses with null sections
                if (definition.getSections() == null) continue;

                // Blueprints from before the flag let plan owners change a section's templates, so the missing flag becomes true.
                // Sections without templates are left alone: the flag does nothing there, and an admin who later adds
                // templates should choose it rather than find it already on.
                List<String> changedSections = new ArrayList<>();
                for (RootSectionEntity section : definition.getSections()) {
                    if (Boolean.TRUE.equals(section.getHasTemplates()) && section.getCanEditDescriptionTemplates() == null) {
                        section.setCanEditDescriptionTemplates(true);
                        changedSections.add(section.getTitle());
                    }
                }
                if (changedSections.isEmpty()) continue;
                logger.info("Plan blueprint {} ({}): plan owners may now change the templates of sections {}", id, rows.getString("label"), changedSections);

                String newXml = this.xmlHandlingService.toXmlSafe(definition);
                if (newXml == null) throw new FlywayException("The updated definition of plan blueprint " + id + " could not be serialized.");
                update.setString(1, newXml);
                update.setObject(2, id);
                update.addBatch();
                updated++;
            }
            update.executeBatch();
        }
        logger.info("Plan blueprint can-edit-description-templates: inspected {}, updated {}", total, updated);
    }
}
