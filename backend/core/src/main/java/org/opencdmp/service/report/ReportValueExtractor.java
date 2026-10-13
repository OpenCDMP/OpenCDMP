package org.opencdmp.service.report;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.ReportAnswerState;
import org.opencdmp.commons.enums.ReportDateBucket;
import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.RadioBoxDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SelectDataEntity;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.TagEntity;
import org.opencdmp.model.Tag;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.ReferenceQuery;
import org.opencdmp.query.TagQuery;
import org.opencdmp.service.visibility.VisibilityService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.management.InvalidApplicationException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Turns the stored answer of a question into the labels a report counts.
 *
 * <p>Values are bucketed by their <em>label</em>, not by their id: two reference rows both
 * called "Zenodo" are one answer to a reader, and that is the question a funder is asking.
 * Ids are resolved in one batch per entity type before extraction, so the number of
 * queries does not grow with the number of subjects.</p>
 */
@Service
public class ReportValueExtractor {

    static final String VALUE_YES = "Yes";
    static final String VALUE_NO = "No";
    static final String VALUE_PROVIDED = "Provided";
    private static final String OTHER_TEXT = "Other text";

    private final QueryFactory queryFactory;

    public ReportValueExtractor(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Resolves every reference, tag and internal-entry id the selected questions point at,
     * in one query per entity type.
     */
    public LabelCache buildLabelCache(List<ReportSubject> subjects, List<ReportQuestionSpec> specs, ReportDefinitionResolver.ParsedSource source) throws InvalidApplicationException {
        Set<UUID> referenceIds = new HashSet<>();
        Set<UUID> tagIds = new HashSet<>();
        Set<UUID> planIds = new HashSet<>();
        Set<UUID> descriptionIds = new HashSet<>();

        for (ReportSubject subject : subjects) {
            if (subject.properties() == null || subject.properties().getFieldSets() == null) continue;
            ReportDefinitionResolver.ParsedVersion version = source.getVersions().get(subject.sourceVersionId());
            if (version == null) continue;

            for (ReportQuestionSpec spec : specs) {
                for (ReportQuestionSpec.FieldTarget target : spec.targetsIn(version)) {
                    PropertyDefinitionFieldSetEntity fieldSet = subject.properties().getFieldSets().get(target.fieldSetId());
                    if (fieldSet == null || fieldSet.getItems() == null) continue;

                    for (PropertyDefinitionFieldSetItemEntity item : fieldSet.getItems()) {
                        if (item == null || item.getFields() == null) continue;
                        FieldEntity value = item.getFields().get(target.fieldId());
                        org.opencdmp.commons.types.descriptiontemplate.FieldEntity definition = version.field(target.fieldId());
                        if (value == null || definition == null || definition.getData() == null) continue;

                        FieldType fieldType = definition.getData().getFieldType();
                        if (fieldType == null || value.getTextListValue() == null) continue;

                        for (String raw : value.getTextListValue()) {
                            UUID id = toUuid(raw);
                            if (id == null) continue;
                            if (FieldType.isReferenceType(fieldType)) referenceIds.add(id);
                            else if (FieldType.isTagType(fieldType)) tagIds.add(id);
                            else if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType)) planIds.add(id);
                            else if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType)) descriptionIds.add(id);
                        }
                    }
                }
            }
        }

        LabelCache cache = new LabelCache();

        for (List<UUID> chunk : ReportServiceImpl.chunked(referenceIds)) {
            List<ReferenceEntity> references = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(chunk)
                    .collectAs(new BaseFieldSet().ensure(Reference._id).ensure(Reference._label));
            if (references != null) for (ReferenceEntity reference : references) cache.references.put(reference.getId(), reference.getLabel());
        }
        for (List<UUID> chunk : ReportServiceImpl.chunked(tagIds)) {
            List<TagEntity> tags = this.queryFactory.query(TagQuery.class).disableTracking().ids(chunk)
                    .collectAs(new BaseFieldSet().ensure(Tag._id).ensure(Tag._label));
            if (tags != null) for (TagEntity tag : tags) cache.tags.put(tag.getId(), tag.getLabel());
        }
        for (List<UUID> chunk : ReportServiceImpl.chunked(planIds)) {
            List<PlanEntity> plans = this.queryFactory.query(PlanQuery.class).disableTracking().ids(chunk).isActive(IsActive.Active, IsActive.Inactive)
                    .collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._label));
            if (plans != null) for (PlanEntity plan : plans) cache.plans.put(plan.getId(), plan.getLabel());
        }
        for (List<UUID> chunk : ReportServiceImpl.chunked(descriptionIds)) {
            List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(chunk).isActive(IsActive.Active, IsActive.Inactive)
                    .collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._label));
            if (descriptions != null) for (DescriptionEntity description : descriptions) cache.descriptions.put(description.getId(), description.getLabel());
        }

        return cache;
    }

    /**
     * Reads one question of one subject.
     *
     * <p>A question is <em>not applicable</em> when the version the subject was written
     * against does not carry its fields, or when every one of them is hidden by a
     * visibility rule; it is <em>not answered</em> when it was asked and left empty. The
     * distinction is the whole point of re-evaluating visibility here: hidden fields are
     * persisted empty, so without it a conditional question would read as skipped.</p>
     */
    public Extraction extract(ReportSubject subject, ReportQuestionSpec spec, ReportDefinitionResolver.ParsedVersion version, Supplier<VisibilityService> visibility, LabelCache cache, ReportDateBucket dateBucket) {
        Extraction extraction = new Extraction();

        if (version == null) {
            extraction.state = ReportAnswerState.NotApplicable;
            return extraction;
        }

        List<ReportQuestionSpec.FieldTarget> targets = spec.targetsIn(version);
        if (targets.isEmpty()) {
            extraction.state = ReportAnswerState.NotApplicable;
            return extraction;
        }

        Map<String, PropertyDefinitionFieldSetEntity> answered = subject.properties() != null && subject.properties().getFieldSets() != null
                ? subject.properties().getFieldSets()
                : Map.of();

        // A semantic can be carried by fields of several fieldsets; each is read where it lives.
        Map<String, List<String>> fieldIdsByFieldSet = new LinkedHashMap<>();
        for (ReportQuestionSpec.FieldTarget target : targets) {
            fieldIdsByFieldSet.computeIfAbsent(target.fieldSetId(), x -> new ArrayList<>()).add(target.fieldId());
        }

        boolean anyOpened = false;
        boolean anyVisible = false;
        for (Map.Entry<String, List<String>> entry : fieldIdsByFieldSet.entrySet()) {
            PropertyDefinitionFieldSetEntity fieldSet = answered.get(entry.getKey());
            // A fieldset the author never opened has no items at all: nothing was hidden, it
            // was simply left empty.
            if (fieldSet == null || fieldSet.getItems() == null || fieldSet.getItems().isEmpty()) continue;
            anyOpened = true;

            for (PropertyDefinitionFieldSetItemEntity item : fieldSet.getItems()) {
                if (item == null) continue;
                for (String fieldId : entry.getValue()) {
                    // Evaluating the rules costs a pass over the whole subject; a field no rule can hide skips it.
                    if (version.canBeHidden(fieldId) && !visibility.get().isVisible(fieldId, item.getOrdinal())) continue;
                    anyVisible = true;

                    FieldEntity value = item.getFields() != null ? item.getFields().get(fieldId) : null;
                    if (value == null) continue;
                    this.appendValues(extraction, value, version.field(fieldId), cache, dateBucket);
                }
            }
        }

        if (!anyOpened) extraction.state = ReportAnswerState.NotAnswered;
        else if (!anyVisible) extraction.state = ReportAnswerState.NotApplicable;
        else if (extraction.values.isEmpty()) extraction.state = ReportAnswerState.NotAnswered;
        else extraction.state = ReportAnswerState.Answered;

        return extraction;
    }

    private void appendValues(Extraction extraction, FieldEntity value, org.opencdmp.commons.types.descriptiontemplate.FieldEntity definition, LabelCache cache, ReportDateBucket dateBucket) {
        if (definition == null || definition.getData() == null) return;
        FieldType fieldType = definition.getData().getFieldType();
        if (fieldType == null) return;

        // UPLOAD and RADIO_BOX are text-typed but carry an id and an option code rather than
        // a readable value, so they are handled before the plain text branch.
        if (FieldType.UPLOAD.equals(fieldType)) {
            if (isNotBlank(value.getTextValue())) extraction.values.add(VALUE_PROVIDED);
            return;
        }
        if (FieldType.RADIO_BOX.equals(fieldType)) {
            if (!isNotBlank(value.getTextValue())) return;
            extraction.values.add(radioLabel(definition, value.getTextValue()));
            return;
        }
        if (FieldType.isReferenceType(fieldType)) {
            appendFromCache(extraction, value.getTextListValue(), cache.references);
            return;
        }
        if (FieldType.isTagType(fieldType)) {
            appendFromCache(extraction, value.getTextListValue(), cache.tags);
            return;
        }
        if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType)) {
            appendFromCache(extraction, value.getTextListValue(), cache.plans);
            return;
        }
        if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType)) {
            appendFromCache(extraction, value.getTextListValue(), cache.descriptions);
            return;
        }
        if (FieldType.SELECT.equals(fieldType)) {
            if (value.getTextListValue() == null) return;
            for (String raw : value.getTextListValue()) {
                if (!isNotBlank(raw)) continue;
                extraction.values.add(selectLabel(definition, raw));
            }
            return;
        }
        if (FieldType.isTextType(fieldType)) {
            String text = stripHtml(value.getTextValue());
            if (isNotBlank(text)) extraction.values.add(text);
            return;
        }
        if (FieldType.isDateType(fieldType)) {
            if (value.getDateValue() != null) extraction.values.add(bucketOf(value.getDateValue(), dateBucket));
            return;
        }
        if (FieldType.isBooleanType(fieldType)) {
            if (value.getBooleanValue() != null) extraction.values.add(Boolean.TRUE.equals(value.getBooleanValue()) ? VALUE_YES : VALUE_NO);
            return;
        }
        if (FieldType.isNumberType(fieldType)) {
            if (value.getNumberValue() == null) return;
            extraction.numbers.add(value.getNumberValue());
            extraction.values.add(formatNumber(value.getNumberValue()));
            return;
        }
        if (FieldType.isExternalIdentifierType(fieldType)) {
            if (value.getExternalIdentifier() != null && isNotBlank(value.getExternalIdentifier().getIdentifier())) extraction.values.add(VALUE_PROVIDED);
        }
    }

    /**
     * The same answer arrives as "Zenodo", "zenodo" and "Zenodo " depending on who typed it.
     * Such spellings are counted as one, under the one most subjects used, so the totals,
     * the breakdowns and the export all carry the same label.
     */
    public static void mergeSpellings(Collection<Map<String, Extraction>> extractions) {
        Map<String, Map<String, Map<String, Long>>> spellings = new HashMap<>();
        for (Map<String, Extraction> perQuestion : extractions) {
            for (Map.Entry<String, Extraction> entry : perQuestion.entrySet()) {
                for (String value : entry.getValue().values) {
                    spellings.computeIfAbsent(entry.getKey(), x -> new HashMap<>())
                            .computeIfAbsent(spellingKey(value), x -> new HashMap<>())
                            .merge(value, 1L, Long::sum);
                }
            }
        }

        Map<String, Map<String, String>> chosen = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Long>>> question : spellings.entrySet()) {
            Map<String, String> byKey = new HashMap<>();
            for (Map.Entry<String, Map<String, Long>> variants : question.getValue().entrySet()) {
                if (variants.getValue().size() < 2) continue;
                byKey.put(variants.getKey(), variants.getValue().entrySet().stream()
                        .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry.<String, Long>comparingByKey().reversed()))
                        .map(Map.Entry::getKey).orElseThrow());
            }
            if (!byKey.isEmpty()) chosen.put(question.getKey(), byKey);
        }
        if (chosen.isEmpty()) return;

        for (Map<String, Extraction> perQuestion : extractions) {
            for (Map.Entry<String, Extraction> entry : perQuestion.entrySet()) {
                Map<String, String> byKey = chosen.get(entry.getKey());
                if (byKey == null || entry.getValue().values.isEmpty()) continue;
                Set<String> merged = new LinkedHashSet<>();
                for (String value : entry.getValue().values) merged.add(byKey.getOrDefault(spellingKey(value), value));
                entry.getValue().values.clear();
                entry.getValue().values.addAll(merged);
            }
        }
    }

    private static String spellingKey(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static void appendFromCache(Extraction extraction, List<String> rawIds, Map<UUID, String> labels) {
        if (rawIds == null) return;
        for (String raw : rawIds) {
            UUID id = toUuid(raw);
            if (id == null) continue;
            String label = labels.get(id);
            // A deleted reference keeps its id in the answer; report it rather than dropping
            // the subject's answer altogether.
            extraction.values.add(isNotBlank(label) ? label.trim() : "(deleted) " + id);
        }
    }

    private static String selectLabel(org.opencdmp.commons.types.descriptiontemplate.FieldEntity definition, String rawValue) {
        if (definition.getData() instanceof SelectDataEntity data && data.getOptions() != null) {
            for (SelectDataEntity.OptionEntity option : data.getOptions()) {
                if (rawValue.equals(option.getValue()) && isNotBlank(option.getLabel())) return option.getLabel().trim();
            }
        }
        return rawValue.trim();
    }

    private static String radioLabel(org.opencdmp.commons.types.descriptiontemplate.FieldEntity definition, String rawValue) {
        if (definition.getData() instanceof RadioBoxDataEntity data && data.getOptions() != null) {
            for (RadioBoxDataEntity.RadioBoxDataOptionEntity option : data.getOptions()) {
                if (rawValue.equals(option.getValue()) && isNotBlank(option.getLabel())) return option.getLabel().trim();
            }
        }
        return rawValue.trim();
    }

    static String bucketOf(java.time.Instant instant, ReportDateBucket dateBucket) {
        ZonedDateTime moment = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        if (ReportDateBucket.Month.equals(dateBucket)) return String.format("%04d-%02d", moment.getYear(), moment.getMonthValue());
        return String.valueOf(moment.getYear());
    }

    static String formatNumber(Double number) {
        if (number == null) return "";
        if (number == Math.floor(number) && !number.isInfinite()) return String.valueOf(number.longValue());
        return String.valueOf(number);
    }

    static String otherTextLabel(long distinctValues) {
        return OTHER_TEXT + " (" + distinctValues + ")";
    }

    private static String stripHtml(String value) {
        if (value == null) return null;
        return HtmlUtils.htmlUnescape(value.replaceAll("<[^>]*>", " ")).replace(' ', ' ').replaceAll("\\s+", " ").trim();
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static UUID toUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Resolved labels for every id the selected questions refer to. */
    public static class LabelCache {

        private final Map<UUID, String> references = new HashMap<>();
        private final Map<UUID, String> tags = new HashMap<>();
        private final Map<UUID, String> plans = new HashMap<>();
        private final Map<UUID, String> descriptions = new HashMap<>();
    }

    /** An answer that carries no values, only the reason there are none. */
    static Extraction stateOnly(ReportAnswerState state) {
        Extraction extraction = new Extraction();
        extraction.state = state;
        return extraction;
    }

    /** What one subject said in answer to one question. */
    public static class Extraction {

        private ReportAnswerState state = ReportAnswerState.NotAnswered;
        private final Set<String> values = new LinkedHashSet<>();
        private final List<Double> numbers = new ArrayList<>();

        public ReportAnswerState getState() { return this.state; }

        public Set<String> getValues() { return this.values; }

        public List<Double> getNumbers() { return this.numbers; }
    }
}
