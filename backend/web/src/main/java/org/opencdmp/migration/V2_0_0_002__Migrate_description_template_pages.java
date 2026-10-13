package org.opencdmp.migration;

import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.PageEntity;
import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class V2_0_0_002__Migrate_description_template_pages extends BaseJavaMigration {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(V2_0_0_002__Migrate_description_template_pages.class));

    private final XmlHandlingService xmlHandlingService;

    public V2_0_0_002__Migrate_description_template_pages(XmlHandlingService xmlHandlingService) {
        this.xmlHandlingService = xmlHandlingService;
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        int total = 0, migrated = 0;
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery("SELECT id, definition FROM public.\"DescriptionTemplate\"");
             PreparedStatement update = connection.prepareStatement("UPDATE public.\"DescriptionTemplate\" SET definition = ?::xml WHERE id = ?")) {
            while (rows.next()) {
                total++;
                UUID id = rows.getObject("id", UUID.class);
                String xml = rows.getString("definition");
                if (xml == null || xml.isBlank()) continue;

                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, xml);
                if (definition == null) throw new FlywayException("The definition of description template " + id + " could not be parsed. Fix it, then restart.");
                if (definition.getPages() == null || definition.getPages().isEmpty() || (definition.getSections() != null && !definition.getSections().isEmpty())) continue;

                List<SectionEntity> sections = new ArrayList<>();
                for (PageEntity pageEntity : definition.getPages()) {
                    SectionEntity newSection = new SectionEntity();
                    newSection.setId(pageEntity.getId());
                    newSection.setTitle(pageEntity.getTitle());
                    newSection.setOrdinal(pageEntity.getOrdinal());
                    newSection.setSections(pageEntity.getSections());
                    sections.add(newSection);
                }
                definition.setSections(sections);
                definition.setPages(null);

                String newXml = this.xmlHandlingService.toXmlSafe(definition);
                if (newXml == null) throw new FlywayException("The migrated definition of description template " + id + " could not be serialized.");
                update.setString(1, newXml);
                update.setObject(2, id);
                update.addBatch();
                migrated++;
            }
            update.executeBatch();
        }
        logger.info("Description template pages: inspected {}, migrated {}", total, migrated);
    }
}
