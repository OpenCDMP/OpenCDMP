package org.opencdmp.service.planupdaterequest;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.description.PropertyDefinitionModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.FieldModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetItemModel;
import org.opencdmp.commonmodels.models.plandescriptionfields.PropertyDefinitionFieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
import org.opencdmp.commonmodels.models.plan.PlanContactModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.plan.PlanPropertiesModel;
import org.opencdmp.commons.enums.*;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingDescriptionModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.data.*;
import org.opencdmp.model.builder.commonmodels.dto.description.DescriptionCommonModelBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SystemFieldDataEntity;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.commons.types.plan.PlanContactEntity;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.DescriptionCommonModelConfig;
import org.opencdmp.model.persist.PlanSuggestion;
import org.opencdmp.model.persist.descriptionproperties.FieldPersist;
import org.opencdmp.model.persist.descriptionproperties.FieldPersistComparison;
import org.opencdmp.model.planupdaterequest.preview.DescriptionChange;
import org.opencdmp.model.planupdaterequest.preview.DescriptionChangeKind;
import org.opencdmp.model.planupdaterequest.preview.FieldChange;
import org.opencdmp.model.planupdaterequest.preview.IncomingField;
import org.opencdmp.model.planupdaterequest.preview.PlanUpdatePreview;
import org.opencdmp.model.planupdaterequest.preview.PreviewKind;
import org.opencdmp.query.*;
import org.opencdmp.service.description.DescriptionService;
import org.opencdmp.service.plandescriptionfields.PlanDescriptionFieldsService;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.plan.PlanServiceProperties;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.opencdmp.authorization.AuthorizationFlags.AllExceptPublic;

@Service
public class PlanUpdatePreviewServiceImpl implements PlanUpdatePreviewService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanUpdatePreviewServiceImpl.class));

    private static final String PLAN_FIELD_CHANGE_NAMESPACE = "plan|";
    private static final String DESCRIPTION_UPDATE_NAMESPACE = "description|update|";
    private static final String DESCRIPTION_DELETE_NAMESPACE = "description|delete|";
    private static final String DESCRIPTION_ADD_NAMESPACE = "description|add|";
    private static final String DESCRIPTION_FIELD_NAMESPACE = "description|field|";
    private static final String DESCRIPTION_SYSTEM_FIELD_NAMESPACE = "description|system|";

    private final QueryFactory queryFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final JsonHandlingService jsonHandlingService;
    private final FileTransformerService fileTransformerService;
    private final PlanServiceProperties planServiceProperties;
    private final DescriptionService descriptionService;
    private final PlanDescriptionFieldsService fieldsService;
    private final XmlHandlingService xmlHandlingService;
    private final BuilderFactory builderFactory;

    public PlanUpdatePreviewServiceImpl(
            QueryFactory queryFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            ErrorThesaurusProperties errors,
            JsonHandlingService jsonHandlingService,
            FileTransformerService fileTransformerService,
            PlanServiceProperties planServiceProperties, DescriptionService descriptionService, PlanDescriptionFieldsService fieldsService, XmlHandlingService xmlHandlingService, BuilderFactory builderFactory) {
        this.queryFactory = queryFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.errors = errors;
        this.jsonHandlingService = jsonHandlingService;
        this.fileTransformerService = fileTransformerService;
        this.planServiceProperties = planServiceProperties;
        this.descriptionService = descriptionService;
        this.fieldsService = fieldsService;
        this.xmlHandlingService = xmlHandlingService;
        this.builderFactory = builderFactory;
    }

    @Override
    public PlanUpdatePreview preview(UUID planUpdateRequestId) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug("computing plan update preview context: {}", planUpdateRequestId);

        PlanUpdateRequestEntity request = this.queryFactory.query(PlanUpdateRequestQuery.class)
                .authorize(AllExceptPublic)
                .ids(planUpdateRequestId)
                .isActive(IsActive.Active)
                .first();
        if (request == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planUpdateRequestId, PlanUpdateRequestEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!request.getStatus().equals(PlanUpdateRequestStatus.Pending)) throw new MyValidationException(this.errors.getPlanUpdateRequestIsCompleted().getCode(), this.errors.getPlanUpdateRequestIsCompleted().getMessage());

        PlanUpdatePreview preview = new PlanUpdatePreview();
        preview.setPlanUpdateRequestId(request.getId());
        preview.setPlanFieldChanges(new ArrayList<FieldChange>());
        preview.setDescriptionChanges(new ArrayList<DescriptionChange>());

        if (request.getActionType() == null || !request.getActionType().equals(PlanUpdateRequestActionType.UpdatePlan)) {
            preview.setKind(PreviewKind.CREATE);
            return preview;
        }

        preview.setKind(PreviewKind.UPDATE);

        PlanEntity plan = this.queryFactory.query(PlanQuery.class)
                .authorize(AllExceptPublic)
                .ids(request.getPlanId())
                .isActive(IsActive.Active)
                .first();
        if (plan == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{request.getPlanId(), PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanStatusEntity statusEntity = this.queryFactory.query(PlanStatusQuery.class).disableTracking().ids(plan.getStatusId()).first();
        if (statusEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{plan.getStatusId(), PlanStatusEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (statusEntity.getInternalStatus() != null && statusEntity.getInternalStatus().equals(PlanStatus.Finalized)) throw new MyValidationException(this.errors.getPlanIsFinalized().getCode(), this.errors.getPlanIsFinalized().getMessage());

        preview.setPlanId(plan.getId());
        preview.setPlanVersion(this.conventionService.hashValue(plan.getUpdatedAt()));

        PlanModel incoming = this.runImporter(plan, request);
        if (incoming == null) return preview;

        List<FieldChange> planFieldChanges = new ArrayList<>(this.diffPlanBlueprintValues(plan, incoming));
        planFieldChanges.addAll(this.diffPlanSystemFields(plan, incoming));
        preview.setPlanFieldChanges(planFieldChanges);

        byte[] rawData = request.getData() != null ? request.getData().getBytes(StandardCharsets.UTF_8) : new byte[0];
        PreprocessingPlanModel preprocessing = this.runPreprocessing(rawData);
        preview.setDescriptionChanges(this.diffDescriptions(plan, incoming, preprocessing));

        return preview;
    }

    @Override
    public Description descriptionFromPreview(UUID planUpdateRequestId, String datasetId, UUID planDescriptionTemplateId) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug("building description persist from preview: request={}, datasetId={}, planDescriptionTemplateId={}", planUpdateRequestId, datasetId, planDescriptionTemplateId);

        // Load request and its plan
        PlanUpdateRequestEntity request = this.queryFactory.query(PlanUpdateRequestQuery.class)
                .authorize(AllExceptPublic)
                .ids(planUpdateRequestId)
                .isActive(IsActive.Active)
                .first();
        if (request == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planUpdateRequestId, PlanUpdateRequestEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanEntity plan = this.queryFactory.query(PlanQuery.class)
                .ids(request.getPlanId())
                .isActive(IsActive.Active)
                .first();
        if (plan == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{request.getPlanId(), PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        // Resolve PlanDescriptionTemplate → sectionId + descriptionTemplateId
        PlanDescriptionTemplateEntity planDescriptionTemplate = this.queryFactory.query(PlanDescriptionTemplateQuery.class)
                .disableTracking()
                .ids(planDescriptionTemplateId)
                .isActive(IsActive.Active)
                .first();
        if (planDescriptionTemplate == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDescriptionTemplateId, "PlanDescriptionTemplate"}, LocaleContextHolder.getLocale()));

        DescriptionTemplateEntity descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class)
                .disableTracking()
                .groupIds(planDescriptionTemplate.getDescriptionTemplateGroupId())
                .versionStatuses(DescriptionTemplateVersionStatus.Current)
                .isActive(IsActive.Active)
                .statuses(DescriptionTemplateStatus.Finalized)
                .first();
        if (descriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDescriptionTemplate.getDescriptionTemplateGroupId(), "DescriptionTemplate"}, LocaleContextHolder.getLocale()));

        // Build a PlanSuggestion that includes this new description so importPlanPreview maps it
        PlanSuggestion suggestion = new PlanSuggestion();
        suggestion.setPlanUpdateRequestId(planUpdateRequestId);
        suggestion.setBlueprintId(plan.getBlueprintId());
        DescriptionCommonModelConfig config = new DescriptionCommonModelConfig();
        config.setId(datasetId);
        config.setSectionId(planDescriptionTemplate.getSectionId());
        config.setTemplateId(descriptionTemplateEntity.getId());
        suggestion.setDescriptions(List.of(config));

        // Import the plan model with the new description included
        byte[] data = request.getData() != null ? request.getData().getBytes(StandardCharsets.UTF_8) : new byte[0];
        PlanModel incoming;
        try {
            incoming = this.fileTransformerService.importPlanPreview(data, suggestion, this.planServiceProperties.getRdaTransformerId());
        } catch (Exception e) {
            throw new MyApplicationException("Failed to import plan preview for description persist: " + e.getMessage());
        }
        if (incoming == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planUpdateRequestId, "PlanModel"}, LocaleContextHolder.getLocale()));


        DescriptionModel descriptionModel = null;
        if (incoming.getDescriptions() != null && !incoming.getDescriptions().isEmpty()) {
            if (incoming.getDescriptions().size() == 1) descriptionModel = incoming.getDescriptions().getFirst();
            else {
                // Find the DescriptionModel matching the requested datasetId
                for (DescriptionModel dm : incoming.getDescriptions()) {
                    if (dm.getId() != null && dm.getId().toString().equals(datasetId)) {
                        descriptionModel = dm;
                        break;
                    }
                }
            }

        }
        if (descriptionModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{datasetId, "DescriptionModel"}, LocaleContextHolder.getLocale()));

        return this.builderFactory.builder(DescriptionCommonModelBuilder.class).build(descriptionModel);
    }

    private PreprocessingPlanModel runPreprocessing(byte[] data) {
        try {
            return this.fileTransformerService.preprocessingPlanPreview(data, this.planServiceProperties.getRdaTransformerId());
        } catch (Exception e) {
            logger.warn("Plan update request preprocessing failed (non-fatal): {}", e.getMessage());
            return null;
        }
    }

    private PlanModel runImporter(PlanEntity plan, PlanUpdateRequestEntity request) throws MyApplicationException {
        PlanSuggestion suggestion = this.buildSuggestion(plan, request.getId());
        byte[] data = request.getData() != null ? request.getData().getBytes(StandardCharsets.UTF_8) : new byte[0];
        try {
            return this.fileTransformerService.importPlanPreview(data, suggestion, this.planServiceProperties.getRdaTransformerId());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MyApplicationException("Plan update request preview failed while mapping incoming data: " + e.getMessage());
        }
    }

    private PlanSuggestion buildSuggestion(PlanEntity plan, UUID planUpdateRequestId) {
        PlanSuggestion suggestion = new PlanSuggestion();
        suggestion.setPlanUpdateRequestId(planUpdateRequestId);
        suggestion.setBlueprintId(plan.getBlueprintId());

        List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class)
                .disableTracking()
                .planIds(plan.getId())
                .isActive(IsActive.Active)
                .collect();
        if (descriptions == null || descriptions.isEmpty()) {
            suggestion.setDescriptions(Collections.emptyList());
            return suggestion;
        }

        Set<UUID> planDescriptionTemplateIds = new HashSet<>();
        for (DescriptionEntity description : descriptions) {
            if (description.getPlanDescriptionTemplateId() != null) planDescriptionTemplateIds.add(description.getPlanDescriptionTemplateId());
        }

        Map<UUID, String> pdtIdToSectionId = new HashMap<>();
        if (!planDescriptionTemplateIds.isEmpty()) {
            List<PlanDescriptionTemplateEntity> pdts = this.queryFactory.query(PlanDescriptionTemplateQuery.class)
                    .disableTracking()
                    .ids(planDescriptionTemplateIds)
                    .isActive(IsActive.Active)
                    .collect();
            if (pdts != null) for (PlanDescriptionTemplateEntity pdt : pdts) pdtIdToSectionId.put(pdt.getId(), pdt.getSectionId());
        }

        List<DescriptionCommonModelConfig> configs = new ArrayList<>();
        for (DescriptionEntity description : descriptions) {
            String sectionId = pdtIdToSectionId.get(description.getPlanDescriptionTemplateId());
            if (sectionId == null || description.getDescriptionTemplateId() == null) continue;
            DescriptionCommonModelConfig config = new DescriptionCommonModelConfig();
            config.setId(description.getId().toString());
            config.setSectionId(sectionId);
            config.setTemplateId(description.getDescriptionTemplateId());
            configs.add(config);
        }
        suggestion.setDescriptions(configs);
        return suggestion;
    }

    private List<FieldChange> diffPlanBlueprintValues(PlanEntity plan, PlanModel incoming) {
        // Plan blueprint field values are now stored as description-template style fieldSets
        // (Map<fieldSetId, PropertyDefinitionFieldSet...>) on the plan properties, mirroring descriptions.
        PlanBlueprintEntity blueprint = this.queryFactory.query(PlanBlueprintQuery.class)
                .ids(plan.getBlueprintId())
                .disableTracking()
                .first();
        final org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = (blueprint != null && blueprint.getDefinition() != null)
                ? this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprint.getDefinition())
                : null;

        Map<String, PropertyDefinitionFieldSetEntity> currentFieldSets = Collections.emptyMap();
        if (plan.getProperties() != null && !plan.getProperties().isBlank()) {
            PlanPropertiesEntity currentProperties = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, plan.getProperties());
            if (currentProperties != null && currentProperties.getFieldSets() != null) currentFieldSets = currentProperties.getFieldSets();
        }

        Map<String, PropertyDefinitionFieldSetModel> incomingFieldSets = Collections.emptyMap();
        PlanPropertiesModel incomingProperties = incoming.getProperties();
        if (incomingProperties != null && incomingProperties.getFieldSets() != null) incomingFieldSets = incomingProperties.getFieldSets();

        List<ReferenceEntity> references = this.loadReferencesForFieldSets(currentFieldSets);
        List<? extends SectionModel> incomingSections = (incoming.getPlanBlueprint() != null && incoming.getPlanBlueprint().getDefinition() != null)
                ? incoming.getPlanBlueprint().getDefinition().getSections() : null;

        return this.diffFieldSetChanges(currentFieldSets, incomingFieldSets,
                fieldKey -> definition != null ? definition.getFieldById(fieldKey) : Collections.emptyList(),
                references, incomingSections, PLAN_FIELD_CHANGE_NAMESPACE, plan.getId().toString());
    }

    /**
     * Diffs the description-template style fieldSets between a stored entity map and an incoming common-model map,
     * producing a FieldChange per changed field. Shared by plan blueprint field diffing and description diffing.
     */
    private List<FieldChange> diffFieldSetChanges(
            Map<String, PropertyDefinitionFieldSetEntity> currentFieldSets,
            Map<String, PropertyDefinitionFieldSetModel> incomingFieldSets,
            java.util.function.Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> fieldResolver,
            List<ReferenceEntity> references,
            List<? extends SectionModel> incomingSections,
            String changeNamespace,
            String entityId) {
        Set<String> allFieldSetIds = new HashSet<>();
        allFieldSetIds.addAll(currentFieldSets.keySet());
        allFieldSetIds.addAll(incomingFieldSets.keySet());

        List<FieldChange> fieldChanges = new ArrayList<>();
        for (String fieldSetId : allFieldSetIds) {
            PropertyDefinitionFieldSetEntity currentFs = currentFieldSets.get(fieldSetId);
            PropertyDefinitionFieldSetModel incomingFs = incomingFieldSets.get(fieldSetId);

            Map<Integer, Map<String, FieldEntity>> currentByOrd = new HashMap<>();
            if (currentFs != null && currentFs.getItems() != null) {
                for (PropertyDefinitionFieldSetItemEntity item : currentFs.getItems()) {
                    currentByOrd.put(item.getOrdinal(), item.getFields() != null ? item.getFields() : Collections.emptyMap());
                }
            }
            Map<Integer, Map<String, FieldModel>> incomingByOrd = new HashMap<>();
            if (incomingFs != null && incomingFs.getItems() != null) {
                for (PropertyDefinitionFieldSetItemModel item : incomingFs.getItems()) {
                    if (item.getOrdinal() == null) continue;
                    incomingByOrd.put(item.getOrdinal(), item.getFields() != null ? item.getFields() : Collections.emptyMap());
                }
            }

            Set<Integer> ordinals = new HashSet<>();
            ordinals.addAll(currentByOrd.keySet());
            ordinals.addAll(incomingByOrd.keySet());

            for (Integer ord : ordinals) {
                Map<String, FieldEntity> currentFields = currentByOrd.getOrDefault(ord, Collections.emptyMap());
                Map<String, FieldModel> incomingFields = incomingByOrd.getOrDefault(ord, Collections.emptyMap());

                Set<String> allFieldKeys = new HashSet<>();
                allFieldKeys.addAll(currentFields.keySet());
                allFieldKeys.addAll(incomingFields.keySet());

                for (String fieldKey : allFieldKeys) {
                    List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity> fieldEntities = fieldResolver.apply(fieldKey);
                    if (fieldEntities == null || fieldEntities.isEmpty()) {
                        logger.warn("not found field definition with id: " + fieldKey);
                        continue;
                    }
                    FieldPersist currentValue = this.fieldsService.buildFieldPersist(currentFields.get(fieldKey), fieldEntities.getFirst(), references);
                    FieldPersist incomingValue = this.fieldsService.commonModelFieldToPersist(incomingFields.get(fieldKey), incomingSections);
                    if (fieldPersistEquals(currentValue, incomingValue)) continue;

                    String qualifiedId = fieldSetId + "[" + ord + "]." + fieldKey;
                    FieldChange fc = new FieldChange();
                    fc.setChangeId(UUID.nameUUIDFromBytes((changeNamespace + entityId + "|" + qualifiedId).getBytes(StandardCharsets.UTF_8)));
                    fc.setFieldId(qualifiedId);
                    fc.setCurrentValue(currentValue);
                    fc.setIncomingValue(incomingValue);
                    fieldChanges.add(fc);
                }
            }
        }
        return fieldChanges;
    }

    private List<ReferenceEntity> loadReferencesForFieldSets(Map<String, PropertyDefinitionFieldSetEntity> fieldSets) {
        if (fieldSets == null || fieldSets.isEmpty()) return Collections.emptyList();
        Set<UUID> ids = new HashSet<>();
        for (PropertyDefinitionFieldSetEntity fs : fieldSets.values()) {
            if (fs == null || fs.getItems() == null) continue;
            for (PropertyDefinitionFieldSetItemEntity item : fs.getItems()) {
                if (item.getFields() == null) continue;
                for (FieldEntity f : item.getFields().values()) {
                    if (f == null || f.getTextListValue() == null) continue;
                    for (String v : f.getTextListValue()) {
                        UUID id = tryParseUuid(v);
                        if (id != null) ids.add(id);
                    }
                }
            }
        }
        if (ids.isEmpty()) return Collections.emptyList();
        return this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(new ArrayList<>(ids)).collect();
    }

    private static UUID tryParseUuid(String s) {
        if (s == null) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
    }

    private static final String PLAN_SYSTEM_FIELD_CHANGE_NAMESPACE = "plan|system|";

    private List<FieldChange> diffPlanSystemFields(PlanEntity plan, PlanModel incoming) {
        PlanBlueprintEntity blueprint = this.queryFactory.query(PlanBlueprintQuery.class)
                .ids(plan.getBlueprintId())
                .disableTracking()
                .first();
        if (blueprint == null || blueprint.getDefinition() == null) return Collections.emptyList();

        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =
                this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprint.getDefinition());
        if (definition == null) return Collections.emptyList();

        List<PlanContactEntity> currentContacts = Collections.emptyList();
        if (plan.getProperties() != null && !plan.getProperties().isBlank()) {
            PlanPropertiesEntity currentProperties = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, plan.getProperties());
            if (currentProperties != null && currentProperties.getContacts() != null) {
                currentContacts = currentProperties.getContacts();
            }
        }

        List<FieldChange> changes = new ArrayList<>();
        for (org.opencdmp.commons.types.descriptiontemplate.FieldEntity field : definition.getAllField()) {
            if (field == null || field.getId() == null) continue;
            // Blueprint system fields are now description-template fields whose data is a SystemFieldDataEntity.
            if (!(field.getData() instanceof SystemFieldDataEntity)) continue;
            SystemFieldType systemFieldType = ((SystemFieldDataEntity) field.getData()).getSystemFieldType();
            if (systemFieldType == null) continue;

            if (systemFieldType == SystemFieldType.PlanContact) {
                FieldChange contactChange = this.diffContactField(field.getId(), currentContacts,
                        incoming != null && incoming.getProperties() != null ? incoming.getProperties().getContacts() : null);
                if (contactChange != null) changes.add(contactChange);
                continue;
            }

            String currentText = null;
            String incomingText = null;

            switch (systemFieldType) {
                case PlanTitle:
                    currentText = plan.getLabel();
                    incomingText = incoming != null ? incoming.getLabel() : null;
                    break;
                case PlanDescription:
                    currentText = plan.getDescription();
                    incomingText = incoming != null ? incoming.getDescription() : null;
                    break;
                case PlanLanguage:
                    currentText = plan.getLanguage();
                    incomingText = incoming != null ? incoming.getLanguage() : null;
                    break;
                case PlanAccessRights:
                    currentText = plan.getAccessType() != null ? plan.getAccessType().getValue().toString() : null;
                    incomingText = (incoming != null && incoming.getAccessType() != null) ? incoming.getAccessType().getValue().toString() : null;
                    break;
                default:
                    continue;
            }

            if (Objects.equals(currentText, incomingText)) continue;

            FieldPersist currentValue = null;
            if (currentText != null) {
                currentValue = new FieldPersist();
                currentValue.setTextValue(currentText);
            }
            FieldPersist incomingValue = null;
            if (incomingText != null) {
                incomingValue = new FieldPersist();
                incomingValue.setTextValue(incomingText);
            }

            FieldChange change = new FieldChange();
            change.setChangeId(UUID.nameUUIDFromBytes((PLAN_SYSTEM_FIELD_CHANGE_NAMESPACE + field.getId()).getBytes(StandardCharsets.UTF_8)));
            change.setFieldId(field.getId());
            change.setCurrentValue(currentValue);
            change.setIncomingValue(incomingValue);
            changes.add(change);
        }
        return changes;
    }

    private static final class ContactJson {
        public String firstName;
        public String lastName;
        public String email;
        ContactJson() {}
        ContactJson(String firstName, String lastName, String email) {
            this.firstName = firstName; this.lastName = lastName; this.email = email;
        }
        String sortKey() { return email != null ? email.toLowerCase() : ""; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof ContactJson)) return false;
            ContactJson c = (ContactJson) o;
            return Objects.equals(firstName, c.firstName) && Objects.equals(lastName, c.lastName) && Objects.equals(email, c.email);
        }
        @Override public int hashCode() { return Objects.hash(firstName, lastName, email); }
    }

    private FieldChange diffContactField(String systemFieldId,
                                          List<PlanContactEntity> currentContacts,
                                          List<PlanContactModel> incomingContacts) {
        List<ContactJson> current = currentContacts == null ? Collections.emptyList() :
                currentContacts.stream()
                        .map(c -> new ContactJson(c.getFirstName(), c.getLastName(), c.getEmail()))
                        .sorted(Comparator.comparing(ContactJson::sortKey))
                        .collect(Collectors.toList());
        List<ContactJson> incoming = incomingContacts == null ? Collections.emptyList() :
                incomingContacts.stream()
                        .map(c -> new ContactJson(c.getFirstName(), c.getLastName(), c.getEmail()))
                        .sorted(Comparator.comparing(ContactJson::sortKey))
                        .collect(Collectors.toList());

        if (current.equals(incoming)) return null;

        FieldChange change = new FieldChange();
        change.setChangeId(UUID.nameUUIDFromBytes((PLAN_SYSTEM_FIELD_CHANGE_NAMESPACE + systemFieldId).getBytes(StandardCharsets.UTF_8)));
        change.setFieldId(systemFieldId);
        change.setCurrentValue(contactsToFieldPersist(current));
        change.setIncomingValue(contactsToFieldPersist(incoming));
        return change;
    }

    private static FieldPersist contactsToFieldPersist(List<ContactJson> contacts) {
        if (contacts == null || contacts.isEmpty()) return null;
        FieldPersist p = new FieldPersist();
        // textValue: JSON array for structured write-back by the frontend
        p.setTextValue(contactsToJson(contacts));
        // textListValue: human-readable summaries for display in the review bar
        p.setTextListValue(contacts.stream()
                .map(c -> {
                    String name = ((c.firstName != null ? c.firstName : "") + " " + (c.lastName != null ? c.lastName : "")).trim();
                    return name.isEmpty() ? c.email : (c.email != null ? name + " (" + c.email + ")" : name);
                })
                .collect(Collectors.toList()));
        return p;
    }

    private static String contactsToJson(List<ContactJson> contacts) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < contacts.size(); i++) {
            ContactJson c = contacts.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"firstName\":").append(jsonString(c.firstName))
              .append(",\"lastName\":").append(jsonString(c.lastName))
              .append(",\"email\":").append(jsonString(c.email))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }


    private List<DescriptionChange> diffDescriptions(PlanEntity plan, PlanModel incoming, PreprocessingPlanModel preprocessing) {
        List<DescriptionEntity> currentDescriptions = this.queryFactory.query(DescriptionQuery.class)
                .disableTracking()
                .planIds(plan.getId())
                .isActive(IsActive.Active)
                .collect();
        if (currentDescriptions == null) currentDescriptions = Collections.emptyList();

        Map<UUID, DescriptionEntity> currentById = new HashMap<>();
        for (DescriptionEntity d : currentDescriptions) currentById.put(d.getId(), d);

        // Build a set of datasetIds that exist in the current plan (by string id from descriptions).
        Set<String> currentDatasetIds = new HashSet<>();
        for (DescriptionEntity d : currentDescriptions) currentDatasetIds.add(d.getId().toString());

        Map<UUID, DescriptionModel> incomingById = new HashMap<>();
        if (incoming.getDescriptions() != null) {
            for (DescriptionModel d : incoming.getDescriptions()) {
                if (d.getId() != null) incomingById.put(d.getId(), d);
            }
        }

        List<DescriptionTemplateEntity> currentDescriptionTemplates = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(currentDescriptions)) {
            currentDescriptionTemplates = this.queryFactory.query(DescriptionTemplateQuery.class)
                    .disableTracking()
                    .ids(currentDescriptions.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().toList())
                    .collect();
        }

        // Batch-load tags for all current descriptions
        Map<UUID, List<String>> tagsByDescriptionId = new HashMap<>();
        if (!this.conventionService.isListNullOrEmpty(currentDescriptions)) {
            List<UUID> descriptionIds = currentDescriptions.stream().map(DescriptionEntity::getId).toList();
            List<DescriptionTagEntity> descriptionTags = this.queryFactory.query(DescriptionTagQuery.class)
                    .disableTracking()
                    .descriptionIds(descriptionIds)
                    .isActive(IsActive.Active)
                    .collect();
            if (!this.conventionService.isListNullOrEmpty(descriptionTags)) {
                List<UUID> tagIds = descriptionTags.stream().map(DescriptionTagEntity::getTagId).distinct().toList();
                List<TagEntity> tags = this.queryFactory.query(TagQuery.class)
                        .disableTracking()
                        .ids(tagIds)
                        .isActive(IsActive.Active)
                        .collect();
                Map<UUID, String> tagLabelById = new HashMap<>();
                if (tags != null) tags.forEach(t -> tagLabelById.put(t.getId(), t.getLabel()));
                for (DescriptionTagEntity dt : descriptionTags) {
                    String label = tagLabelById.get(dt.getTagId());
                    if (label != null) {
                        tagsByDescriptionId.computeIfAbsent(dt.getDescriptionId(), k -> new ArrayList<>()).add(label);
                    }
                }
            }
        }

        List<DescriptionChange> changes = new ArrayList<>();
        for (DescriptionEntity current : currentDescriptions) {
            DescriptionTemplateEntity descriptionTemplateEntity = currentDescriptionTemplates.stream().filter(x -> x.getId().equals(current.getDescriptionTemplateId())).findFirst().orElse(null);
            if (descriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{current.getDescriptionTemplateId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, descriptionTemplateEntity.getDefinition());

            List<UUID> referenceIds = this.descriptionService.calculateAllReferenceIdsFromData( this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, current.getProperties()), definition);
            List<ReferenceEntity> references = null;
            if (!this.conventionService.isListNullOrEmpty(referenceIds)){
                references = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(referenceIds).collect();
            }

            List<String> currentTags = tagsByDescriptionId.getOrDefault(current.getId(), Collections.emptyList());
            DescriptionModel inc = incomingById.get(current.getId());
            if (inc != null) changes.add(this.updateDescriptionChange(current, inc, definition, references, currentTags));
            else changes.add(this.deleteDescriptionChange(current));
        }

        List<UUID> candidateTemplateIds = new ArrayList<>(new HashSet<>(collectTemplateIdsInUse(currentDescriptions)));
        for (Map.Entry<UUID, DescriptionModel> e : incomingById.entrySet()) {
            if (currentById.containsKey(e.getKey())) continue;
            changes.add(this.addDescriptionChange(e.getValue(), candidateTemplateIds));
        }

        // Detect new descriptions present in the raw data via preprocessing that are not in the
        // current plan. These descriptions were not given a suggestion config, so importPlanPreview
        // did not produce DescriptionModel entries for them — we surface them as ADD changes with
        // their label from the preprocessing result so the user can choose a template + section.
        if (preprocessing != null && preprocessing.getPreprocessingDescriptionModels() != null) {
            // Collect datasetIds already covered by ADD changes built from incomingById above.
            Set<String> alreadyCovered = new HashSet<>(currentDatasetIds);
            for (DescriptionChange c : changes) {
                if (c.getKind() == DescriptionChangeKind.ADD && c.getDatasetId() != null) {
                    alreadyCovered.add(c.getDatasetId());
                }
            }
            for (PreprocessingDescriptionModel pdm : preprocessing.getPreprocessingDescriptionModels()) {
                if (pdm == null) continue;
                if (pdm.getId() != null && !pdm.getId().isBlank() && alreadyCovered.contains(pdm.getId())) continue;
                changes.add(this.addDescriptionChangeFromPreprocessing(pdm, candidateTemplateIds));
            }
        }

        return changes;
    }

    private DescriptionChange addDescriptionChangeFromPreprocessing(PreprocessingDescriptionModel pdm, List<UUID> candidateTemplateIds) {
        DescriptionChange change = new DescriptionChange();
        change.setChangeId(UUID.nameUUIDFromBytes((DESCRIPTION_ADD_NAMESPACE + pdm.getId()).getBytes(StandardCharsets.UTF_8)));
        change.setKind(DescriptionChangeKind.ADD);
        change.setDatasetId(pdm.getId());
        change.setCurrentLabel(pdm.getLabel());
        change.setCandidateTemplateIds(candidateTemplateIds);
        change.setIncomingFields(Collections.emptyList());
        return change;
    }

    private static List<UUID> collectTemplateIdsInUse(List<DescriptionEntity> descriptions) {
        List<UUID> ids = new ArrayList<>();
        for (DescriptionEntity d : descriptions) {
            if (d.getDescriptionTemplateId() != null) ids.add(d.getDescriptionTemplateId());
        }
        return ids;
    }

    private DescriptionChange updateDescriptionChange(DescriptionEntity current, DescriptionModel incoming, DefinitionEntity definitionEntity, List<ReferenceEntity> references, List<String> currentTags) {
        DescriptionChange change = new DescriptionChange();
        change.setChangeId(UUID.nameUUIDFromBytes((DESCRIPTION_UPDATE_NAMESPACE + current.getId()).getBytes(StandardCharsets.UTF_8)));
        change.setKind(DescriptionChangeKind.UPDATE);
        change.setDatasetId(current.getId().toString());
        change.setDescriptionId(current.getId());
        change.setTemplateId(current.getDescriptionTemplateId());

        PropertyDefinitionEntity currentProps = null;
        if (current.getProperties() != null && !current.getProperties().isBlank()) {
            currentProps = this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, current.getProperties());
        }
        Map<String, PropertyDefinitionFieldSetEntity> currentFieldSets = currentProps != null && currentProps.getFieldSets() != null
                ? currentProps.getFieldSets() : Collections.emptyMap();
        Map<String, PropertyDefinitionFieldSetModel> incomingFieldSets = incoming.getProperties() != null && incoming.getProperties().getFieldSets() != null
                ? incoming.getProperties().getFieldSets() : Collections.emptyMap();

        List<? extends SectionModel> incomingSections = (incoming.getDescriptionTemplate() != null && incoming.getDescriptionTemplate().getDefinition() != null)
                ? incoming.getDescriptionTemplate().getDefinition().getSections() : null;
        List<FieldChange> fieldChanges = this.diffFieldSetChanges(currentFieldSets, incomingFieldSets,
                definitionEntity::getFieldById, references, incomingSections,
                DESCRIPTION_FIELD_NAMESPACE, current.getId().toString());

        // Description system fields: label, description, tags
        this.diffDescriptionSystemTextField(current.getId(), "system.label",
                current.getLabel(), incoming.getLabel(), fieldChanges);
        this.diffDescriptionSystemTextField(current.getId(), "system.description",
                current.getDescription(), incoming.getDescription(), fieldChanges);
        List<String> sortedCurrentTags = currentTags == null ? Collections.emptyList() :
                currentTags.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
        List<String> sortedIncomingTags = incoming.getTags() == null ? Collections.emptyList() :
                incoming.getTags().stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
        if (!sortedCurrentTags.equals(sortedIncomingTags)) {
            FieldChange tc = new FieldChange();
            tc.setChangeId(UUID.nameUUIDFromBytes((DESCRIPTION_SYSTEM_FIELD_NAMESPACE + current.getId() + "|system.tags").getBytes(StandardCharsets.UTF_8)));
            tc.setFieldId("system.tags");
            if (!sortedCurrentTags.isEmpty()) {
                FieldPersist cv = new FieldPersist();
                cv.setTextListValue(sortedCurrentTags);
                tc.setCurrentValue(cv);
            }
            if (!sortedIncomingTags.isEmpty()) {
                FieldPersist iv = new FieldPersist();
                iv.setTextListValue(sortedIncomingTags);
                tc.setIncomingValue(iv);
            }
            fieldChanges.add(tc);
        }

        change.setFieldChanges(fieldChanges);
        return change;
    }

    private void diffDescriptionSystemTextField(UUID descriptionId, String fieldId,
                                                String currentText, String incomingText,
                                                List<FieldChange> fieldChanges) {
        if (Objects.equals(currentText, incomingText)) return;
        FieldChange fc = new FieldChange();
        fc.setChangeId(UUID.nameUUIDFromBytes((DESCRIPTION_SYSTEM_FIELD_NAMESPACE + descriptionId + "|" + fieldId).getBytes(StandardCharsets.UTF_8)));
        fc.setFieldId(fieldId);
        if (currentText != null) {
            FieldPersist cv = new FieldPersist();
            cv.setTextValue(currentText);
            fc.setCurrentValue(cv);
        }
        if (incomingText != null) {
            FieldPersist iv = new FieldPersist();
            iv.setTextValue(incomingText);
            fc.setIncomingValue(iv);
        }
        fieldChanges.add(fc);
    }

    private DescriptionChange deleteDescriptionChange(DescriptionEntity current) {
        DescriptionChange change = new DescriptionChange();
        change.setChangeId(UUID.nameUUIDFromBytes((DESCRIPTION_DELETE_NAMESPACE + current.getId()).getBytes(StandardCharsets.UTF_8)));
        change.setKind(DescriptionChangeKind.DELETE);
        change.setDatasetId(current.getId().toString());
        change.setDescriptionId(current.getId());
        change.setTemplateId(current.getDescriptionTemplateId());
        change.setCurrentLabel(current.getLabel());
        return change;
    }

    private DescriptionChange addDescriptionChange(DescriptionModel incoming, List<UUID> candidateTemplateIds) {
        DescriptionChange change = new DescriptionChange();
        String datasetId = incoming.getId() != null ? incoming.getId().toString() : null;
        change.setChangeId(UUID.nameUUIDFromBytes((DESCRIPTION_ADD_NAMESPACE + datasetId).getBytes(StandardCharsets.UTF_8)));
        change.setKind(DescriptionChangeKind.ADD);
        change.setDatasetId(datasetId);
        change.setCandidateTemplateIds(candidateTemplateIds);

        List<IncomingField> fields = new ArrayList<>();
        List<? extends SectionModel> incomingSections = (incoming.getDescriptionTemplate() != null && incoming.getDescriptionTemplate().getDefinition() != null)
                ? incoming.getDescriptionTemplate().getDefinition().getSections() : null;
        PropertyDefinitionModel props = incoming.getProperties();
        if (props != null && props.getFieldSets() != null) {
            for (Map.Entry<String, PropertyDefinitionFieldSetModel> fsEntry : props.getFieldSets().entrySet()) {
                String fieldSetId = fsEntry.getKey();
                PropertyDefinitionFieldSetModel fs = fsEntry.getValue();
                if (fs == null || fs.getItems() == null) continue;
                for (PropertyDefinitionFieldSetItemModel item : fs.getItems()) {
                    Integer ord = item.getOrdinal();
                    if (item.getFields() == null) continue;
                    for (Map.Entry<String, FieldModel> fEntry : item.getFields().entrySet()) {
                        FieldPersist value = this.fieldsService.commonModelFieldToPersist(fEntry.getValue(), incomingSections);
                        if (value == null) continue;
                        String qualifiedId = fieldSetId + "[" + ord + "]." + fEntry.getKey();
                        IncomingField inf = new IncomingField();
                        inf.setKey(qualifiedId);
                        inf.setLabel(qualifiedId);
                        inf.setValue(value);
                        fields.add(inf);
                    }
                }
            }
        }
        change.setIncomingFields(fields);
        return change;
    }

    private static boolean fieldPersistEquals(FieldPersist a, FieldPersist b) {
        return FieldPersistComparison.fieldPersistEquals(a, b);
    }
}
