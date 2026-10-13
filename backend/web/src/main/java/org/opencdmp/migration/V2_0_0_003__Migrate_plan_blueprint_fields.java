package org.opencdmp.migration;

import gr.cite.tools.logging.LoggerService;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.*;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprint.RootSectionEntity;
import org.opencdmp.commons.types.planblueprint.migration.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Component
public class V2_0_0_003__Migrate_plan_blueprint_fields extends BaseJavaMigration {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(V2_0_0_003__Migrate_plan_blueprint_fields.class));

    private final XmlHandlingService xmlHandlingService;

    public V2_0_0_003__Migrate_plan_blueprint_fields(XmlHandlingService xmlHandlingService) {
        this.xmlHandlingService = xmlHandlingService;
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        Map<UUID, String> referenceTypeNames = this.referenceTypeNames(connection);
        int total = 0, migrated = 0, droppedUserFields = 0;
        try (Statement select = connection.createStatement();
             ResultSet rows = select.executeQuery("SELECT id, definition FROM public.\"PlanBlueprint\"");
             PreparedStatement update = connection.prepareStatement("UPDATE public.\"PlanBlueprint\" SET definition = ?::xml WHERE id = ?")) {
            while (rows.next()) {
                total++;
                UUID id = rows.getObject("id", UUID.class);
                String xml = rows.getString("definition");
                if (xml == null || xml.isBlank()) continue;

                DefinitionEntityMigration oldDefinition = this.xmlHandlingService.fromXmlSafe(DefinitionEntityMigration.class, xml);
                if (oldDefinition == null) throw new FlywayException("The definition of plan blueprint " + id + " could not be parsed. Fix it, then restart.");
                // Only the legacy layout has a section "label" or "fields"; a migrated blueprint parses here with neither.
                // Sections without fields (template-only or draft blueprints) still need their labels turned into titles.
                boolean isLegacyLayout = oldDefinition.getSections() != null
                        && oldDefinition.getSections().stream().anyMatch(x -> x.getLabel() != null || (x.getFields() != null && !x.getFields().isEmpty()));
                if (!isLegacyLayout) continue;

                List<RootSectionEntity> newSections = new ArrayList<>();
                for (SectionEntityMigration oldSection : oldDefinition.getSections()) {
                    RootSectionEntity rootSectionEntity = new RootSectionEntity();
                    rootSectionEntity.setId(oldSection.getId().toString());
                    rootSectionEntity.setTitle(oldSection.getLabel());
                    rootSectionEntity.setDescription(oldSection.getDescription());
                    rootSectionEntity.setOrdinal(oldSection.getOrdinal());
                    rootSectionEntity.setHasTemplates(oldSection.getHasTemplates());
                    rootSectionEntity.setPrefillingSourcesEnabled(oldSection.getPrefillingSourcesEnabled());
                    rootSectionEntity.setCanEditDescriptionTemplates(oldSection.getCanEditDescriptionTemplates());
                    rootSectionEntity.setDescriptionTemplates(oldSection.getDescriptionTemplates());
                    rootSectionEntity.setPrefillingSourcesIds(oldSection.getPrefillingSourcesIds());

                    if (oldSection.getFields() != null && !oldSection.getFields().isEmpty()) {
                        List<FieldSetEntity> newFieldSets = new ArrayList<>();
                        int fieldIndex = 0;
                        for (FieldEntityMigration oldField : oldSection.getFields()) {
                            // 1.4 dropped the Authors/Contributors field: plan users are managed outside the editor, and the
                            // description form has no renderer for it.
                            if (oldField instanceof SystemFieldEntityMigration oldSystemField && PlanBlueprintSystemFieldType.User.equals(oldSystemField.getType())) {
                                droppedUserFields++;
                                continue;
                            }
                            newFieldSets.add(this.toFieldSet(oldField, fieldIndex, referenceTypeNames));
                            fieldIndex++;
                        }
                        if (!newFieldSets.isEmpty()) rootSectionEntity.setFieldSets(newFieldSets);
                    }

                    newSections.add(rootSectionEntity);
                }

                DefinitionEntity newDefinition = new DefinitionEntity();
                newDefinition.setSections(newSections);
                newDefinition.setIconConfiguration(oldDefinition.getIconConfiguration());
                newDefinition.setPluginConfigurations(oldDefinition.getPluginConfigurations());
                newDefinition.setPlanCreationConsent(oldDefinition.getPlanCreationConsent());

                String newXml = this.xmlHandlingService.toXmlSafe(newDefinition);
                if (newXml == null) throw new FlywayException("The migrated definition of plan blueprint " + id + " could not be serialized.");
                update.setString(1, newXml);
                update.setObject(2, id);
                update.addBatch();
                migrated++;
            }
            update.executeBatch();
        }
        logger.info("Plan blueprint fields: inspected {}, migrated {}, dropped Authors/Contributors fields {}", total, migrated, droppedUserFields);
    }

    private FieldSetEntity toFieldSet(FieldEntityMigration oldField, int fieldIndex, Map<UUID, String> referenceTypeNames) {
        FieldSetEntity fieldSetEntity = new FieldSetEntity();
        fieldSetEntity.setId(UUID.randomUUID().toString());
        // the plan editor, the table of contents and the blueprint editor all sort field sets by ordinal
        fieldSetEntity.setOrdinal(oldField.getOrdinal() != null ? oldField.getOrdinal() : fieldIndex);
        fieldSetEntity.setTitle(oldField.getLabel());
        fieldSetEntity.setDescription(oldField.getDescription());
        fieldSetEntity.setHasCommentField(false);
        fieldSetEntity.setHasMultiplicity(false);

        FieldEntity newField = new FieldEntity();
        newField.setId(oldField.getId().toString());
        newField.setOrdinal(0);
        newField.setSemantics(oldField.getSemantics());
        if (oldField.isRequired()) newField.setValidations(List.of(FieldValidationType.Required));
        newField.setIncludeInExport(true);

        BaseFieldDataEntity data = null;
        switch (oldField.getCategory()) {
            case System -> {
                SystemFieldEntityMigration oldSystemField = (SystemFieldEntityMigration) oldField;
                SystemFieldDataEntity newSystemField = new SystemFieldDataEntity();
                newSystemField.setSystemFieldType(SystemFieldType.buildFromBlueprintFieldTypeMigration(oldSystemField.getType()));
                newSystemField.setLabel(oldField.getPlaceholder());
                newSystemField.setFieldType(FieldType.SYSTEM);
                data = newSystemField;

                if (fieldSetEntity.getTitle() == null || fieldSetEntity.getTitle().isEmpty()) {
                    if (oldSystemField.getType().equals(PlanBlueprintSystemFieldType.AccessRights)) fieldSetEntity.setTitle("Access");
                    else fieldSetEntity.setTitle(oldSystemField.getType().name());
                }
            }
            case Extra -> {
                ExtraFieldEntityMigration oldExtraField = (ExtraFieldEntityMigration) oldField;
                switch (oldExtraField.getType()) {
                    case Text -> data = this.labelData(oldField, FieldType.FREE_TEXT);
                    case RichTex -> data = this.labelData(oldField, FieldType.RICH_TEXT_AREA);
                    case Date -> data = this.labelData(oldField, FieldType.DATE_PICKER);
                    case Number -> {
                        NumberDataEntity numberDataEntity = new NumberDataEntity();
                        numberDataEntity.setLabel(oldField.getPlaceholder());
                        numberDataEntity.setFieldType(FieldType.NUMBER);
                        data = numberDataEntity;
                    }
                }
            }
            case ReferenceType -> {
                ReferenceTypeFieldEntityMigration oldReferenceField = (ReferenceTypeFieldEntityMigration) oldField;
                ReferenceTypeDataEntity newReferenceTypeData = new ReferenceTypeDataEntity();
                newReferenceTypeData.setLabel(oldReferenceField.getPlaceholder());
                newReferenceTypeData.setReferenceTypeId(oldReferenceField.getReferenceTypeId());
                newReferenceTypeData.setMultipleSelect(oldReferenceField.getMultipleSelect());
                newReferenceTypeData.setFieldType(FieldType.REFERENCE_TYPES);
                data = newReferenceTypeData;

                if (fieldSetEntity.getTitle() == null || fieldSetEntity.getTitle().isEmpty()) fieldSetEntity.setTitle(referenceTypeNames.get(oldReferenceField.getReferenceTypeId()));
            }
            case Upload -> {
                UploadFieldEntityMigration oldUploadField = (UploadFieldEntityMigration) oldField;
                UploadDataEntity newUploadData = new UploadDataEntity();
                newUploadData.setLabel(oldUploadField.getPlaceholder());
                if (oldUploadField.getTypes() != null && !oldUploadField.getTypes().isEmpty()) {
                    List<UploadDataEntity.UploadDataOptionEntity> options = new ArrayList<>();
                    for (UploadFieldEntityMigration.UploadOptionEntity oldOption : oldUploadField.getTypes()) {
                        UploadDataEntity.UploadDataOptionEntity newOption = new UploadDataEntity.UploadDataOptionEntity();
                        newOption.setLabel(oldOption.getLabel());
                        newOption.setValue(oldOption.getValue());
                        options.add(newOption);
                    }
                    newUploadData.setTypes(options);
                }
                newUploadData.setFieldType(FieldType.UPLOAD);
                newUploadData.setMaxFileSizeInMB(oldUploadField.getMaxFileSizeInMB());
                data = newUploadData;
            }
        }

        newField.setData(data);
        fieldSetEntity.setFields(List.of(newField));
        return fieldSetEntity;
    }

    private LabelDataEntity labelData(FieldEntityMigration oldField, FieldType fieldType) {
        LabelDataEntity labelData = new LabelDataEntity();
        labelData.setLabel(oldField.getPlaceholder());
        labelData.setFieldType(fieldType);
        return labelData;
    }

    private Map<UUID, String> referenceTypeNames(Connection connection) throws SQLException {
        Map<UUID, String> names = new HashMap<>();
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT id, name FROM public.\"ReferenceType\"")) {
            while (rows.next()) names.put(rows.getObject("id", UUID.class), rows.getString("name"));
        }
        return names;
    }
}
