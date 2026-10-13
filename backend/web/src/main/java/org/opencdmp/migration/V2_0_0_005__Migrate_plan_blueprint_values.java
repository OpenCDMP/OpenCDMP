package org.opencdmp.migration;

import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.commons.types.plan.migration.PlanBlueprintValueEntityMigration;
import org.opencdmp.commons.types.plan.migration.PlanPropertiesEntityMigration;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planreference.PlanReferenceDataEntity;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

// Must run after V2_0_0_003: plan values are keyed by the field sets the blueprint migration creates.
@Component
public class V2_0_0_005__Migrate_plan_blueprint_values extends BaseJavaMigration {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(V2_0_0_005__Migrate_plan_blueprint_values.class));

    private static final int PAGE_SIZE = 200;

    private final XmlHandlingService xmlHandlingService;
    private final JsonHandlingService jsonHandlingService;

    public V2_0_0_005__Migrate_plan_blueprint_values(XmlHandlingService xmlHandlingService, JsonHandlingService jsonHandlingService) {
        this.xmlHandlingService = xmlHandlingService;
        this.jsonHandlingService = jsonHandlingService;
    }

    private static class PlanReferenceRow {
        UUID referenceId;
        String data;
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        Map<UUID, String> blueprintDefinitions = this.blueprintDefinitions(connection);
        Map<UUID, DefinitionEntity> parsedBlueprints = new HashMap<>();
        Map<UUID, List<PlanReferenceRow>> referencesByPlanId = this.activeReferencesByPlanId(connection);

        int processed = 0, migrated = 0, alreadyMigrated = 0, blueprintMissing = 0, unmappable = 0;
        UUID lastId = null;
        try (PreparedStatement firstPage = connection.prepareStatement("SELECT id, blueprint, properties FROM public.\"Plan\" ORDER BY id LIMIT " + PAGE_SIZE);
             PreparedStatement nextPage = connection.prepareStatement("SELECT id, blueprint, properties FROM public.\"Plan\" WHERE id > ? ORDER BY id LIMIT " + PAGE_SIZE);
             PreparedStatement update = connection.prepareStatement("UPDATE public.\"Plan\" SET properties = ? WHERE id = ?")) {
            while (true) {
                PreparedStatement page = lastId == null ? firstPage : nextPage;
                if (lastId != null) nextPage.setObject(1, lastId);
                int pageRows = 0;
                try (ResultSet rows = page.executeQuery()) {
                    while (rows.next()) {
                        pageRows++;
                        processed++;
                        UUID planId = rows.getObject("id", UUID.class);
                        UUID blueprintId = rows.getObject("blueprint", UUID.class);
                        String properties = rows.getString("properties");
                        lastId = planId;

                        PlanPropertiesEntity currentProperties = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, properties);
                        // Already in the 1.4 layout: the legacy parser would see no values and rewrite the plan from its references alone.
                        if (currentProperties != null && currentProperties.getFieldSets() != null) {
                            alreadyMigrated++;
                            continue;
                        }
                        PlanPropertiesEntityMigration oldProperties = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntityMigration.class, properties);
                        if (oldProperties == null && properties != null && !properties.isBlank()) throw new FlywayException("The properties of plan " + planId + " could not be parsed. Fix them, then restart.");

                        List<PlanReferenceRow> planReferences = referencesByPlanId.getOrDefault(planId, List.of());
                        boolean hasValues = oldProperties != null && oldProperties.getPlanBlueprintValues() != null && !oldProperties.getPlanBlueprintValues().isEmpty();
                        if (!hasValues && planReferences.isEmpty()) continue;

                        if (blueprintId == null || !blueprintDefinitions.containsKey(blueprintId)) {
                            blueprintMissing++;
                            logger.warn("Plan {} points at blueprint {}, which does not exist; leaving it untouched", planId, blueprintId);
                            continue;
                        }
                        DefinitionEntity blueprintDefinition = parsedBlueprints.computeIfAbsent(blueprintId, k -> this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, blueprintDefinitions.get(k)));
                        if (blueprintDefinition == null || blueprintDefinition.getAllFieldSets() == null || blueprintDefinition.getAllFieldSets().isEmpty())
                            throw new FlywayException("Plan blueprint " + blueprintId + " has no field sets, so the values of plan " + planId + " cannot be mapped.");

                        Map<String, PropertyDefinitionFieldSetEntity> fieldSetsValues = new HashMap<>();
                        if (hasValues) this.mapBlueprintValues(planId, oldProperties.getPlanBlueprintValues(), blueprintDefinition, fieldSetsValues);
                        this.mapReferences(planId, planReferences, blueprintDefinition, fieldSetsValues);

                        if (fieldSetsValues.isEmpty()) {
                            // 1.3 also stored system-field entries with a null fieldId; keep the legacy JSON rather than overwrite it with nothing
                            boolean hadMappableInput = !planReferences.isEmpty() || (hasValues && oldProperties.getPlanBlueprintValues().stream().anyMatch(x -> x.getFieldId() != null));
                            if (hadMappableInput) {
                                unmappable++;
                                logger.warn("None of the legacy values of plan {} map to a field set of its blueprint; leaving it untouched", planId);
                            }
                            continue;
                        }

                        PlanPropertiesEntity newProperties = new PlanPropertiesEntity();
                        if (oldProperties != null) newProperties.setContacts(oldProperties.getContacts());
                        newProperties.setFieldSets(fieldSetsValues);
                        String newJson = this.jsonHandlingService.toJsonSafe(newProperties);
                        if (newJson == null) throw new FlywayException("The migrated properties of plan " + planId + " could not be serialized.");
                        update.setString(1, newJson);
                        update.setObject(2, planId);
                        update.addBatch();
                        migrated++;
                    }
                }
                update.executeBatch();
                if (pageRows < PAGE_SIZE) break;
            }
        }
        logger.info("Plan blueprint values: inspected {}, migrated {}, already migrated {}, blueprint missing {}, not mappable {}", processed, migrated, alreadyMigrated, blueprintMissing, unmappable);
    }

    private void mapBlueprintValues(UUID planId, List<PlanBlueprintValueEntityMigration> oldValues, DefinitionEntity blueprintDefinition, Map<String, PropertyDefinitionFieldSetEntity> fieldSetsValues) {
        for (PlanBlueprintValueEntityMigration oldValue : oldValues) {
            if (oldValue.getFieldId() == null) continue;
            String fieldId = oldValue.getFieldId().toString();

            List<FieldSetEntity> fieldSetEntities = blueprintDefinition.getFieldSetByFieldId(fieldId);
            List<FieldEntity> fieldEntities = blueprintDefinition.getFieldById(fieldId);
            if (fieldSetEntities == null || fieldSetEntities.isEmpty() || fieldEntities == null || fieldEntities.isEmpty()) {
                logger.warn("Plan {} has a value for field {}, which its blueprint does not have", planId, fieldId);
                continue;
            }
            for (FieldSetEntity fieldSetEntity : fieldSetEntities) {
                org.opencdmp.commons.types.description.FieldEntity fieldValue = new org.opencdmp.commons.types.description.FieldEntity();
                fieldValue.setTextValue(oldValue.getValue());
                fieldValue.setDateValue(oldValue.getDateValue());
                fieldValue.setNumberValue(oldValue.getNumberValue());

                Map<String, org.opencdmp.commons.types.description.FieldEntity> fieldMap = new HashMap<>();
                fieldMap.put(fieldEntities.getFirst().getId(), fieldValue);
                PropertyDefinitionFieldSetItemEntity item = new PropertyDefinitionFieldSetItemEntity();
                item.setFields(fieldMap);
                item.setOrdinal(0);

                PropertyDefinitionFieldSetEntity fieldSetValue = new PropertyDefinitionFieldSetEntity();
                fieldSetValue.setItems(List.of(item));
                fieldSetsValues.put(fieldSetEntity.getId(), fieldSetValue);
            }
        }
    }

    private void mapReferences(UUID planId, List<PlanReferenceRow> planReferences, DefinitionEntity blueprintDefinition, Map<String, PropertyDefinitionFieldSetEntity> fieldSetsValues) {
        for (PlanReferenceRow reference : planReferences) {
            PlanReferenceDataEntity referenceData = this.jsonHandlingService.fromJsonSafe(PlanReferenceDataEntity.class, reference.data);
            if (referenceData == null || referenceData.getBlueprintFieldId() == null) {
                logger.warn("Plan {} has reference {} without a blueprint field", planId, reference.referenceId);
                continue;
            }
            String blueprintFieldId = referenceData.getBlueprintFieldId();
            List<FieldSetEntity> fieldSetEntities = blueprintDefinition.getFieldSetByFieldId(blueprintFieldId);
            if (fieldSetEntities == null || fieldSetEntities.isEmpty()) {
                logger.warn("Plan {} has a reference for field {}, which its blueprint does not have", planId, blueprintFieldId);
                continue;
            }

            for (FieldSetEntity fieldSetEntity : fieldSetEntities) {
                PropertyDefinitionFieldSetEntity fieldSetValue = fieldSetsValues.computeIfAbsent(fieldSetEntity.getId(), k -> new PropertyDefinitionFieldSetEntity());
                if (fieldSetValue.getItems() == null) fieldSetValue.setItems(new ArrayList<>());

                int ordinal = referenceData.getOrdinal();
                PropertyDefinitionFieldSetItemEntity item = fieldSetValue.getItems().stream()
                        .filter(x -> x.getOrdinal() == ordinal)
                        .findFirst()
                        .orElseGet(() -> {
                            PropertyDefinitionFieldSetItemEntity newItem = new PropertyDefinitionFieldSetItemEntity();
                            newItem.setOrdinal(ordinal);
                            newItem.setFields(new HashMap<>());
                            fieldSetValue.getItems().add(newItem);
                            return newItem;
                        });

                org.opencdmp.commons.types.description.FieldEntity fieldValue = item.getFields().computeIfAbsent(blueprintFieldId, k -> new org.opencdmp.commons.types.description.FieldEntity());
                List<String> updated = new ArrayList<>(fieldValue.getTextListValue() != null ? fieldValue.getTextListValue() : List.of());
                updated.add(reference.referenceId.toString());
                fieldValue.setTextListValue(updated);
            }
        }
    }

    private Map<UUID, String> blueprintDefinitions(Connection connection) throws SQLException {
        Map<UUID, String> definitions = new HashMap<>();
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT id, definition FROM public.\"PlanBlueprint\"")) {
            while (rows.next()) definitions.put(rows.getObject("id", UUID.class), rows.getString("definition"));
        }
        return definitions;
    }

    private Map<UUID, List<PlanReferenceRow>> activeReferencesByPlanId(Connection connection) throws SQLException {
        Map<UUID, List<PlanReferenceRow>> references = new HashMap<>();
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT plan_id, reference_id, data FROM public.\"PlanReference\" WHERE is_active = 1")) {
            while (rows.next()) {
                PlanReferenceRow row = new PlanReferenceRow();
                row.referenceId = rows.getObject("reference_id", UUID.class);
                row.data = rows.getString("data");
                references.computeIfAbsent(rows.getObject("plan_id", UUID.class), k -> new ArrayList<>()).add(row);
            }
        }
        return references;
    }
}
