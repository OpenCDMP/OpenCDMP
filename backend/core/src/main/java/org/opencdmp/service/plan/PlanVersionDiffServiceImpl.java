package org.opencdmp.service.plan;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.types.description.DescriptionAdditionalInfoEntity;
import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.commons.types.plan.PlanContactEntity;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.model.persist.descriptionproperties.FieldPersist;
import org.opencdmp.model.persist.descriptionproperties.FieldPersistComparison;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.plan.versiondiff.DescriptionVersionDiff;
import org.opencdmp.model.plan.versiondiff.PlanVersionDiff;
import org.opencdmp.model.planupdaterequest.preview.DescriptionChangeKind;
import org.opencdmp.model.planupdaterequest.preview.FieldChange;
import org.opencdmp.query.*;
import org.opencdmp.service.plandescriptionfields.PlanDescriptionFieldsService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencdmp.authorization.AuthorizationFlags.AllExceptPublic;

@Service
public class PlanVersionDiffServiceImpl implements PlanVersionDiffService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanVersionDiffServiceImpl.class));

    private static final String PLAN_FIELD_NAMESPACE = "versiondiff|plan|";
    private static final String PLAN_SYSTEM_FIELD_NAMESPACE = "versiondiff|plan|system|";
    private static final String DESCRIPTION_FIELD_NAMESPACE = "versiondiff|description|";
    private static final String DESCRIPTION_SYSTEM_FIELD_NAMESPACE = "versiondiff|description|system|";

    private final QueryFactory queryFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final JsonHandlingService jsonHandlingService;
    private final XmlHandlingService xmlHandlingService;
    private final PlanDescriptionFieldsService fieldsService;

    public PlanVersionDiffServiceImpl(
            QueryFactory queryFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            JsonHandlingService jsonHandlingService,
            XmlHandlingService xmlHandlingService,
            PlanDescriptionFieldsService fieldsService) {
        this.queryFactory = queryFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.jsonHandlingService = jsonHandlingService;
        this.xmlHandlingService = xmlHandlingService;
        this.fieldsService = fieldsService;
    }

    @Override
    public PlanVersionDiff diff(UUID planId, UUID otherPlanId) throws MyForbiddenException, MyApplicationException, MyNotFoundException {
        logger.debug("computing plan version diff: {} vs {}", planId, otherPlanId);

        PlanEntity first = this.loadAuthorizedPlan(planId);
        PlanEntity second = this.loadAuthorizedPlan(otherPlanId);

        if (first.getGroupId() == null || !first.getGroupId().equals(second.getGroupId())) {
            throw new MyApplicationException("The requested plans are not versions of the same plan group");
        }
        if (first.getId().equals(second.getId())) {
            throw new MyApplicationException("Cannot compare a plan version with itself");
        }

        // "from" is always the older version so the diff reads as the change history direction.
        PlanEntity from = versionOf(first) <= versionOf(second) ? first : second;
        PlanEntity to = from == first ? second : first;

        PlanVersionDiff diff = new PlanVersionDiff();
        diff.setGroupId(from.getGroupId());
        diff.setFromPlanId(from.getId());
        diff.setToPlanId(to.getId());
        diff.setFromVersion(from.getVersion());
        diff.setToVersion(to.getVersion());
        diff.setBlueprintChanged(!Objects.equals(from.getBlueprintId(), to.getBlueprintId()));

        List<FieldChange> planFieldChanges = new ArrayList<>(this.diffPlanSystemFields(from, to));
        planFieldChanges.addAll(this.diffPlanBlueprintValues(from, to));
        diff.setPlanFieldChanges(planFieldChanges);

        diff.setDescriptionChanges(this.diffDescriptions(from, to));

        return diff;
    }

    private PlanEntity loadAuthorizedPlan(UUID id) {
        PlanEntity plan = this.queryFactory.query(PlanQuery.class)
                .authorize(AllExceptPublic)
                .disableTracking()
                .ids(id)
                .isActive(IsActive.Active)
                .first();
        if (plan == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        return plan;
    }

    private static short versionOf(PlanEntity plan) {
        return plan.getVersion() != null ? plan.getVersion() : 0;
    }

    //
    // Plan level
    //

    private List<FieldChange> diffPlanSystemFields(PlanEntity from, PlanEntity to) {
        List<FieldChange> changes = new ArrayList<>();
        this.addSystemTextChange(changes, PLAN_SYSTEM_FIELD_NAMESPACE, "system.label", from.getLabel(), to.getLabel());
        this.addSystemTextChange(changes, PLAN_SYSTEM_FIELD_NAMESPACE, "system.description", from.getDescription(), to.getDescription());
        this.addSystemTextChange(changes, PLAN_SYSTEM_FIELD_NAMESPACE, "system.language", from.getLanguage(), to.getLanguage());
        this.addSystemTextChange(changes, PLAN_SYSTEM_FIELD_NAMESPACE, "system.access",
                from.getAccessType() != null ? from.getAccessType().name() : null,
                to.getAccessType() != null ? to.getAccessType().name() : null);

        FieldChange contactChange = this.diffContacts(this.parsePlanProperties(from), this.parsePlanProperties(to));
        if (contactChange != null) changes.add(contactChange);

        return changes;
    }

    private PlanPropertiesEntity parsePlanProperties(PlanEntity plan) {
        if (plan.getProperties() == null || plan.getProperties().isBlank()) return null;
        return this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, plan.getProperties());
    }

    private FieldChange diffContacts(PlanPropertiesEntity fromProperties, PlanPropertiesEntity toProperties) {
        List<String> fromContacts = contactSummaries(fromProperties != null ? fromProperties.getContacts() : null);
        List<String> toContacts = contactSummaries(toProperties != null ? toProperties.getContacts() : null);
        if (fromContacts.equals(toContacts)) return null;

        FieldChange change = new FieldChange();
        change.setChangeId(UUID.nameUUIDFromBytes((PLAN_SYSTEM_FIELD_NAMESPACE + "system.contacts").getBytes(StandardCharsets.UTF_8)));
        change.setFieldId("system.contacts");
        change.setCurrentValue(textListPersist(fromContacts));
        change.setIncomingValue(textListPersist(toContacts));
        return change;
    }

    private static List<String> contactSummaries(List<PlanContactEntity> contacts) {
        if (contacts == null || contacts.isEmpty()) return Collections.emptyList();
        return contacts.stream()
                .map(c -> {
                    String name = (((c.getFirstName() != null ? c.getFirstName() : "") + " " + (c.getLastName() != null ? c.getLastName() : "")).trim());
                    if (name.isEmpty()) return c.getEmail() != null ? c.getEmail() : "";
                    return c.getEmail() != null ? name + " (" + c.getEmail() + ")" : name;
                })
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private static FieldPersist textListPersist(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        FieldPersist persist = new FieldPersist();
        persist.setTextListValue(values);
        return persist;
    }

    private void addSystemTextChange(List<FieldChange> changes, String namespace, String fieldId, String fromText, String toText) {
        if (Objects.equals(fromText, toText)) return;
        FieldChange change = new FieldChange();
        change.setChangeId(UUID.nameUUIDFromBytes((namespace + fieldId).getBytes(StandardCharsets.UTF_8)));
        change.setFieldId(fieldId);
        if (fromText != null) {
            FieldPersist persist = new FieldPersist();
            persist.setTextValue(fromText);
            change.setCurrentValue(persist);
        }
        if (toText != null) {
            FieldPersist persist = new FieldPersist();
            persist.setTextValue(toText);
            change.setIncomingValue(persist);
        }
        changes.add(change);
    }

    private List<FieldChange> diffPlanBlueprintValues(PlanEntity from, PlanEntity to) {
        Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> fromResolver = this.blueprintFieldResolver(from.getBlueprintId());
        Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> toResolver = this.blueprintFieldResolver(to.getBlueprintId());

        PlanPropertiesEntity fromProperties = this.parsePlanProperties(from);
        PlanPropertiesEntity toProperties = this.parsePlanProperties(to);
        Map<String, PropertyDefinitionFieldSetEntity> fromFieldSets = fromProperties != null && fromProperties.getFieldSets() != null ? fromProperties.getFieldSets() : Collections.emptyMap();
        Map<String, PropertyDefinitionFieldSetEntity> toFieldSets = toProperties != null && toProperties.getFieldSets() != null ? toProperties.getFieldSets() : Collections.emptyMap();

        List<ReferenceEntity> references = this.loadReferencesForFieldSets(fromFieldSets, toFieldSets);

        return this.diffEntityFieldSets(fromFieldSets, toFieldSets, fromResolver, toResolver, references, PLAN_FIELD_NAMESPACE, from.getGroupId().toString());
    }

    private Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> blueprintFieldResolver(UUID blueprintId) {
        PlanBlueprintEntity blueprint = blueprintId != null ? this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(blueprintId).first() : null;
        final org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = (blueprint != null && blueprint.getDefinition() != null)
                ? this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprint.getDefinition())
                : null;
        return fieldKey -> definition != null ? definition.getFieldById(fieldKey) : Collections.emptyList();
    }

    /**
     * Diffs two stored (entity-side) fieldSet maps, producing a FieldChange per differing field.
     * Field definitions are resolved against the "to" side first, falling back to the "from" side,
     * so fields that only exist in one blueprint/template version still render.
     */
    private List<FieldChange> diffEntityFieldSets(
            Map<String, PropertyDefinitionFieldSetEntity> fromFieldSets,
            Map<String, PropertyDefinitionFieldSetEntity> toFieldSets,
            Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> fromResolver,
            Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> toResolver,
            List<ReferenceEntity> references,
            String changeNamespace,
            String entityKey) {
        Set<String> allFieldSetIds = new HashSet<>();
        allFieldSetIds.addAll(fromFieldSets.keySet());
        allFieldSetIds.addAll(toFieldSets.keySet());

        List<FieldChange> fieldChanges = new ArrayList<>();
        for (String fieldSetId : allFieldSetIds) {
            Map<Integer, Map<String, FieldEntity>> fromByOrdinal = fieldsByOrdinal(fromFieldSets.get(fieldSetId));
            Map<Integer, Map<String, FieldEntity>> toByOrdinal = fieldsByOrdinal(toFieldSets.get(fieldSetId));

            Set<Integer> ordinals = new HashSet<>();
            ordinals.addAll(fromByOrdinal.keySet());
            ordinals.addAll(toByOrdinal.keySet());

            for (Integer ordinal : ordinals) {
                Map<String, FieldEntity> fromFields = fromByOrdinal.getOrDefault(ordinal, Collections.emptyMap());
                Map<String, FieldEntity> toFields = toByOrdinal.getOrDefault(ordinal, Collections.emptyMap());

                Set<String> allFieldKeys = new HashSet<>();
                allFieldKeys.addAll(fromFields.keySet());
                allFieldKeys.addAll(toFields.keySet());

                for (String fieldKey : allFieldKeys) {
                    List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity> fieldEntities = toResolver.apply(fieldKey);
                    if (fieldEntities == null || fieldEntities.isEmpty()) fieldEntities = fromResolver.apply(fieldKey);
                    if (fieldEntities == null || fieldEntities.isEmpty()) {
                        logger.warn("not found field definition with id: " + fieldKey);
                        continue;
                    }

                    FieldPersist fromValue = this.fieldsService.buildFieldPersist(fromFields.get(fieldKey), fieldEntities.getFirst(), references);
                    FieldPersist toValue = this.fieldsService.buildFieldPersist(toFields.get(fieldKey), fieldEntities.getFirst(), references);
                    if (FieldPersistComparison.fieldPersistEquals(fromValue, toValue)) continue;

                    String qualifiedId = fieldSetId + "[" + ordinal + "]." + fieldKey;
                    FieldChange change = new FieldChange();
                    change.setChangeId(UUID.nameUUIDFromBytes((changeNamespace + entityKey + "|" + qualifiedId).getBytes(StandardCharsets.UTF_8)));
                    change.setFieldId(qualifiedId);
                    change.setFieldLabel(fieldEntities.getFirst().getData() != null ? fieldEntities.getFirst().getData().getLabel() : null);
                    change.setCurrentValue(FieldPersistComparison.isEffectivelyEmpty(fromValue) ? null : fromValue);
                    change.setIncomingValue(FieldPersistComparison.isEffectivelyEmpty(toValue) ? null : toValue);
                    fieldChanges.add(change);
                }
            }
        }
        return fieldChanges;
    }

    private static Map<Integer, Map<String, FieldEntity>> fieldsByOrdinal(PropertyDefinitionFieldSetEntity fieldSet) {
        if (fieldSet == null || fieldSet.getItems() == null) return Collections.emptyMap();
        Map<Integer, Map<String, FieldEntity>> byOrdinal = new HashMap<>();
        for (PropertyDefinitionFieldSetItemEntity item : fieldSet.getItems()) {
            byOrdinal.put(item.getOrdinal(), item.getFields() != null ? item.getFields() : Collections.emptyMap());
        }
        return byOrdinal;
    }

    @SafeVarargs
    private List<ReferenceEntity> loadReferencesForFieldSets(Map<String, PropertyDefinitionFieldSetEntity>... fieldSetMaps) {
        Set<UUID> ids = new HashSet<>();
        for (Map<String, PropertyDefinitionFieldSetEntity> fieldSets : fieldSetMaps) {
            if (fieldSets == null) continue;
            for (PropertyDefinitionFieldSetEntity fieldSet : fieldSets.values()) {
                if (fieldSet == null || fieldSet.getItems() == null) continue;
                for (PropertyDefinitionFieldSetItemEntity item : fieldSet.getItems()) {
                    if (item.getFields() == null) continue;
                    for (FieldEntity field : item.getFields().values()) {
                        if (field == null || field.getTextListValue() == null) continue;
                        for (String value : field.getTextListValue()) {
                            UUID id = tryParseUuid(value);
                            if (id != null) ids.add(id);
                        }
                    }
                }
            }
        }
        if (ids.isEmpty()) return Collections.emptyList();
        return this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(new ArrayList<>(ids)).collect();
    }

    private static UUID tryParseUuid(String value) {
        if (value == null) return null;
        try { return UUID.fromString(value); } catch (IllegalArgumentException e) { return null; }
    }

    //
    // Description level
    //

    private static final int CLONE_CHAIN_MAX_DEPTH = 32;

    private record DescriptionSide(DescriptionEntity description, String sectionId, UUID templateGroupId, UUID clonedFromDescriptionId) {
        String bucketKey() {
            return (sectionId != null ? sectionId : "") + "|" + (templateGroupId != null ? templateGroupId : "");
        }
    }

    private List<DescriptionVersionDiff> diffDescriptions(PlanEntity from, PlanEntity to) {
        List<DescriptionSide> fromSides = this.loadDescriptionSides(from.getId());
        List<DescriptionSide> toSides = this.loadDescriptionSides(to.getId());

        List<DescriptionVersionDiff> diffs = new ArrayList<>();

        List<DescriptionSide[]> matchedPairs = new ArrayList<>();
        List<DescriptionSide> removed = new ArrayList<>();
        List<DescriptionSide> added = new ArrayList<>();

        // First pass: exact matching over clone provenance. New-version cloning stamps
        // clonedFromDescriptionId into the description properties, so a newer-side
        // description whose clone chain reaches an older-side description is the same asset.
        Map<UUID, DescriptionSide> fromById = new HashMap<>();
        for (DescriptionSide side : fromSides) fromById.put(side.description().getId(), side);
        List<DescriptionSide> unmatchedFrom = new ArrayList<>(fromSides);
        List<DescriptionSide> unmatchedTo = new ArrayList<>(toSides);

        Iterator<DescriptionSide> toIterator = unmatchedTo.iterator();
        while (toIterator.hasNext()) {
            DescriptionSide toSide = toIterator.next();
            UUID sourceId = this.resolveCloneSource(toSide.clonedFromDescriptionId(), fromById.keySet());
            if (sourceId == null) continue;
            DescriptionSide fromSide = fromById.get(sourceId);
            if (fromSide == null || !unmatchedFrom.remove(fromSide)) continue;
            matchedPairs.add(new DescriptionSide[]{fromSide, toSide});
            toIterator.remove();
        }

        // Second pass, for descriptions predating provenance stamping: match within
        // (section, template group) by exact label first, then positionally by creation order.
        Map<String, List<DescriptionSide>> fromBuckets = unmatchedFrom.stream().collect(Collectors.groupingBy(DescriptionSide::bucketKey));
        Map<String, List<DescriptionSide>> toBuckets = unmatchedTo.stream().collect(Collectors.groupingBy(DescriptionSide::bucketKey));

        Set<String> bucketKeys = new HashSet<>();
        bucketKeys.addAll(fromBuckets.keySet());
        bucketKeys.addAll(toBuckets.keySet());

        for (String bucketKey : bucketKeys) {
            List<DescriptionSide> fromBucket = new ArrayList<>(fromBuckets.getOrDefault(bucketKey, Collections.emptyList()));
            List<DescriptionSide> toBucket = new ArrayList<>(toBuckets.getOrDefault(bucketKey, Collections.emptyList()));

            Iterator<DescriptionSide> fromIterator = fromBucket.iterator();
            while (fromIterator.hasNext()) {
                DescriptionSide fromSide = fromIterator.next();
                DescriptionSide labelMatch = toBucket.stream()
                        .filter(t -> Objects.equals(t.description().getLabel(), fromSide.description().getLabel()))
                        .findFirst().orElse(null);
                if (labelMatch != null) {
                    matchedPairs.add(new DescriptionSide[]{fromSide, labelMatch});
                    toBucket.remove(labelMatch);
                    fromIterator.remove();
                }
            }

            fromBucket.sort(Comparator.comparing(s -> s.description().getCreatedAt()));
            toBucket.sort(Comparator.comparing(s -> s.description().getCreatedAt()));
            int pairable = Math.min(fromBucket.size(), toBucket.size());
            for (int i = 0; i < pairable; i++) matchedPairs.add(new DescriptionSide[]{fromBucket.get(i), toBucket.get(i)});
            for (int i = pairable; i < fromBucket.size(); i++) removed.add(fromBucket.get(i));
            for (int i = pairable; i < toBucket.size(); i++) added.add(toBucket.get(i));
        }

        for (DescriptionSide[] pair : matchedPairs) {
            DescriptionVersionDiff diff = this.updateDescriptionDiff(pair[0], pair[1]);
            if (diff != null) diffs.add(diff);
        }
        for (DescriptionSide side : removed) diffs.add(this.oneSidedDescriptionDiff(side, DescriptionChangeKind.DELETE));
        for (DescriptionSide side : added) diffs.add(this.oneSidedDescriptionDiff(side, DescriptionChangeKind.ADD));

        return diffs;
    }

    private List<DescriptionSide> loadDescriptionSides(UUID planId) {
        List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class)
                .disableTracking()
                .planIds(planId)
                .isActive(IsActive.Active)
                .collect();
        if (this.conventionService.isListNullOrEmpty(descriptions)) return Collections.emptyList();

        Set<UUID> planDescriptionTemplateIds = descriptions.stream()
                .map(DescriptionEntity::getPlanDescriptionTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, PlanDescriptionTemplateEntity> planDescriptionTemplatesById = new HashMap<>();
        if (!planDescriptionTemplateIds.isEmpty()) {
            List<PlanDescriptionTemplateEntity> planDescriptionTemplates = this.queryFactory.query(PlanDescriptionTemplateQuery.class)
                    .disableTracking()
                    .ids(planDescriptionTemplateIds)
                    .collect();
            if (planDescriptionTemplates != null) for (PlanDescriptionTemplateEntity entity : planDescriptionTemplates) planDescriptionTemplatesById.put(entity.getId(), entity);
        }

        List<DescriptionSide> sides = new ArrayList<>();
        for (DescriptionEntity description : descriptions) {
            PlanDescriptionTemplateEntity planDescriptionTemplate = planDescriptionTemplatesById.get(description.getPlanDescriptionTemplateId());
            sides.add(new DescriptionSide(
                    description,
                    planDescriptionTemplate != null ? planDescriptionTemplate.getSectionId() : null,
                    planDescriptionTemplate != null ? planDescriptionTemplate.getDescriptionTemplateGroupId() : null,
                    this.parseClonedFromDescriptionId(description)));
        }
        return sides;
    }

    private UUID parseClonedFromDescriptionId(DescriptionEntity description) {
        if (description.getAdditionalInfo() == null || description.getAdditionalInfo().isBlank()) return null;
        DescriptionAdditionalInfoEntity additionalInfo = this.jsonHandlingService.fromJsonSafe(DescriptionAdditionalInfoEntity.class, description.getAdditionalInfo());
        return additionalInfo != null ? additionalInfo.getClonedFromDescriptionId() : null;
    }

    /**
     * Walks the clone-provenance chain starting at the given description id until it reaches
     * one of the target ids or the chain ends. Comparing non-adjacent plan versions crosses
     * intermediate (possibly soft-deleted) descriptions, so lookups do not filter on isActive.
     */
    private UUID resolveCloneSource(UUID startId, Set<UUID> targetIds) {
        UUID current = startId;
        Set<UUID> visited = new HashSet<>();
        for (int depth = 0; current != null && depth < CLONE_CHAIN_MAX_DEPTH; depth++) {
            if (targetIds.contains(current)) return current;
            if (!visited.add(current)) return null;
            DescriptionEntity intermediate = this.queryFactory.query(DescriptionQuery.class)
                    .disableTracking()
                    .ids(current)
                    .first();
            if (intermediate == null) return null;
            current = this.parseClonedFromDescriptionId(intermediate);
        }
        return null;
    }

    private DescriptionVersionDiff updateDescriptionDiff(DescriptionSide fromSide, DescriptionSide toSide) {
        DescriptionEntity from = fromSide.description();
        DescriptionEntity to = toSide.description();

        Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> fromResolver = this.templateFieldResolver(from.getDescriptionTemplateId());
        Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> toResolver = this.templateFieldResolver(to.getDescriptionTemplateId());

        Map<String, PropertyDefinitionFieldSetEntity> fromFieldSets = this.parseDescriptionFieldSets(from);
        Map<String, PropertyDefinitionFieldSetEntity> toFieldSets = this.parseDescriptionFieldSets(to);

        List<ReferenceEntity> references = this.loadReferencesForFieldSets(fromFieldSets, toFieldSets);
        List<FieldChange> fieldChanges = this.diffEntityFieldSets(fromFieldSets, toFieldSets, fromResolver, toResolver, references,
                DESCRIPTION_FIELD_NAMESPACE, from.getId() + "|" + to.getId());

        this.addSystemTextChange(fieldChanges, DESCRIPTION_SYSTEM_FIELD_NAMESPACE + from.getId() + "|", "system.label", from.getLabel(), to.getLabel());
        this.addSystemTextChange(fieldChanges, DESCRIPTION_SYSTEM_FIELD_NAMESPACE + from.getId() + "|", "system.description", from.getDescription(), to.getDescription());
        this.diffDescriptionTags(fieldChanges, from.getId(), to.getId());

        if (fieldChanges.isEmpty()) return null;

        DescriptionVersionDiff diff = new DescriptionVersionDiff();
        diff.setKind(DescriptionChangeKind.UPDATE);
        diff.setFromDescriptionId(from.getId());
        diff.setToDescriptionId(to.getId());
        diff.setFromLabel(from.getLabel());
        diff.setToLabel(to.getLabel());
        diff.setSectionId(fromSide.sectionId() != null ? fromSide.sectionId() : toSide.sectionId());
        diff.setFromTemplateId(from.getDescriptionTemplateId());
        diff.setToTemplateId(to.getDescriptionTemplateId());
        diff.setTemplateChanged(!Objects.equals(from.getDescriptionTemplateId(), to.getDescriptionTemplateId()));
        diff.setFieldChanges(fieldChanges);
        return diff;
    }

    private void diffDescriptionTags(List<FieldChange> fieldChanges, UUID fromDescriptionId, UUID toDescriptionId) {
        List<String> fromTags = this.loadTagLabels(fromDescriptionId);
        List<String> toTags = this.loadTagLabels(toDescriptionId);
        if (fromTags.equals(toTags)) return;

        FieldChange change = new FieldChange();
        change.setChangeId(UUID.nameUUIDFromBytes((DESCRIPTION_SYSTEM_FIELD_NAMESPACE + fromDescriptionId + "|system.tags").getBytes(StandardCharsets.UTF_8)));
        change.setFieldId("system.tags");
        change.setCurrentValue(textListPersist(fromTags));
        change.setIncomingValue(textListPersist(toTags));
        fieldChanges.add(change);
    }

    private List<String> loadTagLabels(UUID descriptionId) {
        List<DescriptionTagEntity> descriptionTags = this.queryFactory.query(DescriptionTagQuery.class)
                .disableTracking()
                .descriptionIds(descriptionId)
                .isActive(IsActive.Active)
                .collect();
        if (this.conventionService.isListNullOrEmpty(descriptionTags)) return Collections.emptyList();

        List<TagEntity> tags = this.queryFactory.query(TagQuery.class)
                .disableTracking()
                .ids(descriptionTags.stream().map(DescriptionTagEntity::getTagId).distinct().toList())
                .isActive(IsActive.Active)
                .collect();
        if (this.conventionService.isListNullOrEmpty(tags)) return Collections.emptyList();
        return tags.stream().map(TagEntity::getLabel).filter(Objects::nonNull).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }

    private DescriptionVersionDiff oneSidedDescriptionDiff(DescriptionSide side, DescriptionChangeKind kind) {
        DescriptionEntity description = side.description();
        DescriptionVersionDiff diff = new DescriptionVersionDiff();
        diff.setKind(kind);
        if (kind == DescriptionChangeKind.DELETE) {
            diff.setFromDescriptionId(description.getId());
            diff.setFromLabel(description.getLabel());
            diff.setFromTemplateId(description.getDescriptionTemplateId());
        } else {
            diff.setToDescriptionId(description.getId());
            diff.setToLabel(description.getLabel());
            diff.setToTemplateId(description.getDescriptionTemplateId());
        }
        diff.setSectionId(side.sectionId());
        diff.setTemplateChanged(false);
        diff.setFieldChanges(Collections.emptyList());
        return diff;
    }

    private Map<String, PropertyDefinitionFieldSetEntity> parseDescriptionFieldSets(DescriptionEntity description) {
        if (description.getProperties() == null || description.getProperties().isBlank()) return Collections.emptyMap();
        PropertyDefinitionEntity properties = this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, description.getProperties());
        return properties != null && properties.getFieldSets() != null ? properties.getFieldSets() : Collections.emptyMap();
    }

    private Function<String, List<org.opencdmp.commons.types.descriptiontemplate.FieldEntity>> templateFieldResolver(UUID descriptionTemplateId) {
        DescriptionTemplateEntity template = descriptionTemplateId != null ? this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(descriptionTemplateId).first() : null;
        final org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition = (template != null && template.getDefinition() != null)
                ? this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class, template.getDefinition())
                : null;
        return fieldKey -> definition != null ? definition.getFieldById(fieldKey) : Collections.emptyList();
    }
}
