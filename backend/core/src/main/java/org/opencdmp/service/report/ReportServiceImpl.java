package org.opencdmp.service.report;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.Paging;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.enums.ReportAnswerState;
import org.opencdmp.commons.enums.ReportDateBucket;
import org.opencdmp.commons.enums.ReportSubjectType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.model.Tenant;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.description.PropertyDefinition;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.report.Report;
import org.opencdmp.model.report.ReportOutline;
import org.opencdmp.model.report.ReportQuestionSearch;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.DescriptionStatusQuery;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.PlanBlueprintQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.PlanStatusQuery;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.lookup.PlanLookup;
import org.opencdmp.query.lookup.ReportLookup;
import org.opencdmp.query.lookup.ReportQuestionSearchLookup;
import org.opencdmp.service.visibility.VisibilityService;
import org.opencdmp.service.visibility.VisibilityServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Answers "how many subjects said what" over the answers stored in plans and assets.
 *
 * <p>The engine reads rather than indexes: it collects the matching subjects with the
 * listing queries, parses each template or blueprint version once, re-evaluates visibility
 * per subject and buckets the values by label. At the volumes a single lineage reaches — a
 * few hundred to a few thousand — that is fast enough to keep the answers exact and free of
 * a derived store that could fall behind the plans it describes.</p>
 *
 * <p>Both levels use one engine because plan answers and asset answers have the same shape:
 * a blueprint fieldset holds fields exactly as a template fieldset does. That also lets an
 * asset report break its counts down by an answer given on the parent plan — "where do the
 * datasets of FCT-funded plans go" is one report rather than two.</p>
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReportServiceImpl.class));

    private static final String NOT_ANSWERED_LABEL = "(not answered)";
    private static final String NOT_APPLICABLE_LABEL = "(not applicable)";
    private static final String NOT_MIGRATED_LABEL = "(not readable)";
    private static final String NOT_FINALIZED_LABEL = "(not finalized)";
    private static final String UNKNOWN_LABEL = "(unknown)";
    private static final int ID_CHUNK = 1000;
    // Large enough that the per-batch queries stay few, small enough to bound the answers held at once.
    private static final int READ_BATCH = 2000;

    private final AuthorizationService authorizationService;
    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final ReportDefinitionResolver definitionResolver;
    private final ReportQuestionSearchService questionSearch;
    private final ReportValueExtractor valueExtractor;
    private final ReportCsvWriter csvWriter;
    private final ReportProperties properties;
    private final ErrorThesaurusProperties errors;
    private final TenantScopeFactory tenantScopeFactory;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    public ReportServiceImpl(
            AuthorizationService authorizationService,
            QueryFactory queryFactory,
            JsonHandlingService jsonHandlingService,
            ReportDefinitionResolver definitionResolver,
            ReportQuestionSearchService questionSearch,
            ReportValueExtractor valueExtractor,
            ReportCsvWriter csvWriter,
            ReportProperties properties,
            ErrorThesaurusProperties errors,
            TenantScopeFactory tenantScopeFactory,
            TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.definitionResolver = definitionResolver;
        this.questionSearch = questionSearch;
        this.valueExtractor = valueExtractor;
        this.csvWriter = csvWriter;
        this.properties = properties;
        this.errors = errors;
        this.tenantScopeFactory = tenantScopeFactory;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    public ReportOutline outline(ReportSubjectType subject, UUID groupId, Boolean installationWide) throws MyForbiddenException, MyNotFoundException, MyApplicationException, InvalidApplicationException {
        ReportSubjectType resolved = subject != null ? subject : ReportSubjectType.Description;
        boolean wide = this.enterScope(installationWide, Permission.BrowseReport, Permission.BrowseInstallationReport);
        try {
            if (groupId == null) throw this.questionNotFound();

            ReportDefinitionResolver.ParsedSource source = ReportSubjectType.Plan.equals(resolved)
                    ? this.definitionResolver.resolvePlanBlueprintGroup(groupId)
                    : this.definitionResolver.resolveDescriptionTemplateGroup(groupId);
            return this.definitionResolver.buildOutline(source, resolved);
        } finally {
            this.leaveScope(wide);
        }
    }

    @Override
    public ReportQuestionSearch searchQuestions(ReportQuestionSearchLookup lookup) throws MyForbiddenException, MyApplicationException, InvalidApplicationException {
        ReportQuestionSearchLookup resolved = lookup != null ? lookup : new ReportQuestionSearchLookup();
        boolean wide = this.enterScope(resolved.getInstallationWide(), Permission.BrowseReport, Permission.BrowseInstallationReport);
        try {
            return this.questionSearch.search(resolved, wide);
        } finally {
            this.leaveScope(wide);
        }
    }

    @Override
    public Report query(ReportLookup lookup) throws MyForbiddenException, MyNotFoundException, MyApplicationException, InvalidApplicationException {
        boolean wide = this.enterScope(lookup != null ? lookup.getInstallationWide() : null, Permission.BrowseReport, Permission.BrowseInstallationReport);
        try {
            return this.aggregate(this.analyze(lookup, wide));
        } finally {
            this.leaveScope(wide);
        }
    }

    @Override
    public byte[] exportCsv(ReportLookup lookup) throws MyForbiddenException, MyNotFoundException, MyApplicationException, InvalidApplicationException, IOException {
        // Row-level answers leave the platform in the export, so it carries its own
        // permission: an installation can grant the aggregates more widely than the rows.
        boolean wide = this.enterScope(lookup != null ? lookup.getInstallationWide() : null, Permission.ExportReport, Permission.ExportInstallationReport);
        try {
            return this.csvWriter.write(this.analyze(lookup, wide));
        } finally {
            this.leaveScope(wide);
        }
    }

    /**
     * Authorizes the request for its scope and, for an installation-wide one, lifts the tenant
     * filters until {@link #leaveScope}, as the statistics name lookups do.
     */
    private boolean enterScope(Boolean installationWide, String tenantPermission, String installationPermission) {
        boolean wide = Boolean.TRUE.equals(installationWide);
        this.authorizationService.authorizeForce(wide ? installationPermission : tenantPermission);
        if (wide) this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
        return wide;
    }

    private void leaveScope(boolean wide) throws InvalidApplicationException {
        if (wide) this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
    }

    //region analysis

    private ReportAnalysis analyze(ReportLookup lookup, boolean installationWide) throws MyApplicationException, MyNotFoundException, InvalidApplicationException {
        if (lookup == null) throw this.questionNotFound();

        ReportSubjectType subject = lookup.getSubject() != null ? lookup.getSubject() : ReportSubjectType.Description;
        boolean includeInactive = Boolean.TRUE.equals(lookup.getIncludeInactive());
        boolean latestVersionOnly = lookup.getLatestVersionOnly() == null || lookup.getLatestVersionOnly();
        boolean wantsPlanQuestions = ReportSubjectType.Description.equals(subject)
                && lookup.getPlanQuestions() != null && !lookup.getPlanQuestions().isEmpty();

        ReportDefinitionResolver.ParsedSource source;

        // Questions addressed by semantic need no lineage: without one, every template or
        // blueprint of the tenant is in scope. A question addressed by fieldset still does.
        boolean allBySemantic = lookup.getQuestions() != null && !lookup.getQuestions().isEmpty()
                && lookup.getQuestions().stream().allMatch(x -> x != null && x.getSemantic() != null && !x.getSemantic().isBlank());

        // Across lineages only the versions carrying a requested tag are read, and only the
        // subjects written on them: a template that never asks the question has nothing to count.
        Set<String> semantics = allBySemantic
                ? lookup.getQuestions().stream().map(x -> ReportDefinitionResolver.normaliseSemantic(x.getSemantic())).collect(Collectors.toSet())
                : null;
        boolean planSubject = ReportSubjectType.Plan.equals(subject);
        if (planSubject) {
            if (lookup.getPlanBlueprintGroupId() != null) source = this.definitionResolver.resolvePlanBlueprintGroup(lookup.getPlanBlueprintGroupId());
            else if (allBySemantic) source = this.definitionResolver.resolvePlanBlueprintsCarrying(semantics);
            else throw this.questionNotFound();
        } else {
            if (lookup.getDescriptionTemplateGroupId() != null) source = this.definitionResolver.resolveDescriptionTemplateGroup(lookup.getDescriptionTemplateGroupId());
            else if (allBySemantic) source = this.definitionResolver.resolveDescriptionTemplatesCarrying(semantics);
            else throw this.questionNotFound();
        }
        List<ReportQuestionSpec> specs = this.buildSpecs(lookup.getQuestions(), source, "q");
        Set<UUID> sourceVersionIds = source.spansAllGroups() ? source.getVersions().keySet() : null;

        ReportDefinitionResolver.ParsedSource planSource = null;
        List<ReportQuestionSpec> planSpecs = new ArrayList<>();
        if (wantsPlanQuestions) {
            if (lookup.getPlanBlueprintGroupId() == null) throw this.questionNotFound();
            planSource = this.definitionResolver.resolvePlanBlueprintGroup(lookup.getPlanBlueprintGroupId());
            planSpecs = this.buildSpecs(lookup.getPlanQuestions(), planSource, "p");
        }
        this.validateDimensions(lookup, subject, specs, planSpecs, installationWide);
        ReportDateBucket dateBucket = lookup.getDateBucket() != null ? lookup.getDateBucket() : ReportDateBucket.Year;

        Collected collected = new Collected();
        if (planSubject) {
            this.readPlanSubjects(lookup, includeInactive, latestVersionOnly, installationWide, sourceVersionIds, source, specs, dateBucket, collected);
        } else {
            this.readDescriptionSubjects(lookup, includeInactive, latestVersionOnly, installationWide, sourceVersionIds, source, specs,
                    wantsPlanQuestions ? planSource : null, planSpecs, dateBucket, collected);
        }
        List<ReportSubject> subjects = collected.subjects;
        Map<UUID, Map<String, ReportValueExtractor.Extraction>> extractions = collected.extractions;
        Map<UUID, Map<String, ReportValueExtractor.Extraction>> planExtractions = collected.planExtractions;
        ReportValueExtractor.mergeSpellings(extractions.values());
        ReportValueExtractor.mergeSpellings(planExtractions.values());

        ReportAnalysis analysis = new ReportAnalysis();
        analysis.setSubject(subject);
        analysis.setSource(source);
        analysis.setSpecs(specs);
        analysis.setPlanSource(planSource);
        analysis.setPlanSpecs(planSpecs);
        analysis.setDimensions(buildDimensionSpecs(lookup, specs, planSpecs));
        analysis.setSubjects(subjects);
        analysis.setExtractions(extractions);
        analysis.setPlanExtractions(planExtractions);
        analysis.setDateBucket(dateBucket);
        analysis.setTextTopN(lookup.getTextTopN() != null && lookup.getTextTopN() > 0 ? lookup.getTextTopN() : this.properties.getTextTopN());
        analysis.setDescriptionStatusNames(this.collectDescriptionStatusNames(subjects));
        analysis.setPlanStatusNames(this.collectPlanStatusNames(subjects));
        analysis.setBlueprintLabels(this.collectBlueprintLabels(subjects));
        analysis.setInstallationWide(installationWide);
        analysis.setTenantCodes(installationWide ? this.collectTenantCodes(subjects) : new HashMap<>());

        logger.debug("report analysed {} subjects over {} questions", subjects.size(), specs.size());
        return analysis;
    }

    private Map<String, ReportValueExtractor.Extraction> extractFor(
            ReportSubject subject, ReportDefinitionResolver.ParsedSource source, List<ReportQuestionSpec> specs,
            ReportValueExtractor.LabelCache cache, ReportDateBucket dateBucket) {

        Map<String, ReportValueExtractor.Extraction> perQuestion = new LinkedHashMap<>();
        if (specs.isEmpty()) return perQuestion;

        // Answers written before the 1.4 field migration cannot be read at all. Saying so is
        // the point: counting them as unanswered would understate every repository in the
        // report, and counting them as not applicable would hide the migration entirely.
        if (subject.legacyProperties()) {
            for (ReportQuestionSpec spec : specs) perQuestion.put(spec.key(), ReportValueExtractor.stateOnly(ReportAnswerState.NotMigrated));
            return perQuestion;
        }

        ReportDefinitionResolver.ParsedVersion version = source != null ? source.getVersions().get(subject.sourceVersionId()) : null;
        if (version == null) {
            for (ReportQuestionSpec spec : specs) perQuestion.put(spec.key(), ReportValueExtractor.stateOnly(ReportAnswerState.NotApplicable));
            return perQuestion;
        }

        // Built on first use: most questions sit where no rule can hide them, and building it
        // copies every answer of the subject.
        VisibilityService[] visibilityService = new VisibilityService[1];
        java.util.function.Supplier<VisibilityService> visibility = () -> {
            if (visibilityService[0] == null) visibilityService[0] = VisibilityServiceImpl.fromEntity(
                    version.getSections(),
                    subject.properties() != null && subject.properties().getFieldSets() != null ? subject.properties().getFieldSets() : new HashMap<>());
            return visibilityService[0];
        };

        for (ReportQuestionSpec spec : specs) {
            perQuestion.put(spec.key(), this.valueExtractor.extract(subject, spec, version, visibility, cache, dateBucket));
        }
        return perQuestion;
    }

    /** Wraps a plan's own answers as a subject so the same extractor reads both levels. */
    private static ReportSubject planSubjectOf(PlanAnswers answers) {
        return new ReportSubject(answers.planId(), null, null, null, answers.blueprintId(), answers.properties(), answers.legacy(),
                answers.planId(), null, null, null, null, null, answers.blueprintId(), null, null);
    }

    private List<ReportSubject> buildDescriptionSubjects(List<DescriptionEntity> descriptions, Map<UUID, PlanEntity> plans) {
        List<ReportSubject> subjects = new ArrayList<>();
        for (DescriptionEntity description : descriptions) {
            PlanEntity plan = description.getPlanId() != null ? plans.get(description.getPlanId()) : null;
            // A plan dropped by the latest-version pruning takes its assets with it.
            if (description.getPlanId() != null && plan == null) continue;

            PropertyDefinitionEntity propertyDefinition = description.getProperties() != null
                    ? this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, description.getProperties())
                    : null;

            subjects.add(new ReportSubject(
                    description.getId(),
                    description.getLabel(),
                    description.getTenantId(),
                    description.getStatusId(),
                    description.getDescriptionTemplateId(),
                    propertyDefinition,
                    false,
                    description.getPlanId(),
                    plan != null ? plan.getLabel() : null,
                    plan != null ? plan.getVersion() : null,
                    plan != null ? plan.getVersionStatus() : null,
                    plan != null ? plan.getStatusId() : null,
                    plan != null ? plan.getAccessType() : null,
                    plan != null ? plan.getBlueprintId() : null,
                    description.getCreatedAt(),
                    description.getFinalizedAt()));
        }
        return subjects;
    }

    private void readPlanSubjects(ReportLookup lookup, boolean includeInactive, boolean latestVersionOnly, boolean installationWide, Set<UUID> blueprintIds,
                                  ReportDefinitionResolver.ParsedSource source, List<ReportQuestionSpec> specs, ReportDateBucket dateBucket, Collected collected) throws MyApplicationException, InvalidApplicationException {
        if (blueprintIds != null && blueprintIds.isEmpty()) return;
        PlanQuery query = lookup.getPlanSubQuery() != null
                ? lookup.getPlanSubQuery().enrich(this.queryFactory)
                : this.queryFactory.query(PlanQuery.class);
        query.authorize(EnumSet.of(AuthorizationFlags.None)).disableTracking().isActive(activeValues(includeInactive));
        if (lookup.getPlanBlueprintGroupId() != null) {
            query.planBlueprintSubQuery(this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().groupIds(lookup.getPlanBlueprintGroupId()));
        } else if (blueprintIds != null) {
            query.planBlueprintSubQuery(this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(blueprintIds));
        }
        if (latestVersionOnly) query.versionStatuses(PlanVersionStatus.Current, PlanVersionStatus.NotFinalized);
        TenantQuery tenantQuery = this.currentTenantQuery(installationWide);
        if (tenantQuery != null) query.tenantSubQuery(tenantQuery);
        query.setOrder(new Ordering().addAscending(Plan._createdAt).addAscending(Plan._id));

        // The filters run once, for the ids; the rows are then read by id a batch at a time.
        // Paging the filtered query itself would re-run its filters and sort for every batch.
        int limit = this.maxSubjects(installationWide);
        query.setPage(new Paging(0, limit + 1));
        List<PlanEntity> matching = query.collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._createdAt));
        if (matching == null || matching.isEmpty()) return;
        if (matching.size() > limit) {
            query.setPage(null);
            throw this.subjectCapExceeded(query.count(), limit);
        }

        for (List<UUID> chunk : batches(matching.stream().map(PlanEntity::getId).toList())) {
            List<PlanEntity> items = this.inOrder(chunk, this.queryFactory.query(PlanQuery.class)
                    .authorize(EnumSet.of(AuthorizationFlags.None))
                    .disableTracking()
                    .ids(chunk)
                    .isActive(activeValues(includeInactive))
                    .collectAs(planFieldSet(true)), PlanEntity::getId);

            Map<UUID, PlanEntity> kept = new LinkedHashMap<>();
            for (PlanEntity plan : items) kept.put(plan.getId(), plan);
            if (latestVersionOnly) kept = this.keepLatestOfEachLineage(kept, includeInactive, installationWide);

            List<ReportSubject> batch = new ArrayList<>();
            for (PlanEntity plan : items) {
                if (!kept.containsKey(plan.getId())) continue;
                PlanAnswers answers = this.readPlanAnswers(plan);
                batch.add(new ReportSubject(
                        plan.getId(), plan.getLabel(), plan.getTenantId(), plan.getStatusId(), plan.getBlueprintId(),
                        answers.properties(), answers.legacy(),
                        plan.getId(), plan.getLabel(), plan.getVersion(), plan.getVersionStatus(), plan.getStatusId(),
                        plan.getAccessType(), plan.getBlueprintId(), plan.getCreatedAt(), plan.getFinalizedAt()));
            }
            this.extractBatch(batch, source, specs, dateBucket, collected.subjects, collected.extractions);
        }
    }

    private static <T> List<List<T>> batches(List<T> items) {
        List<List<T>> batches = new ArrayList<>();
        for (int start = 0; start < items.size(); start += READ_BATCH) batches.add(items.subList(start, Math.min(start + READ_BATCH, items.size())));
        return batches;
    }

    /** The rows of one batch in the order their ids were matched, which is the report's order. */
    private <T> List<T> inOrder(List<UUID> ids, List<T> rows, java.util.function.Function<T, UUID> idOf) {
        Map<UUID, T> byId = new HashMap<>();
        if (rows != null) for (T row : rows) byId.put(idOf.apply(row), row);
        List<T> ordered = new ArrayList<>();
        for (UUID id : ids) {
            T row = byId.get(id);
            if (row != null) ordered.add(row);
        }
        return ordered;
    }

    /**
     * Reads one batch of subjects into the report and keeps only what the report needs of
     * them: the answers are parsed, read and let go, so memory holds one batch of answers at
     * a time rather than the whole matching set.
     */
    private void extractBatch(List<ReportSubject> batch, ReportDefinitionResolver.ParsedSource source, List<ReportQuestionSpec> specs, ReportDateBucket dateBucket,
                              List<ReportSubject> subjects, Map<UUID, Map<String, ReportValueExtractor.Extraction>> extractions) throws InvalidApplicationException {
        if (batch.isEmpty()) return;
        ReportValueExtractor.LabelCache cache = this.valueExtractor.buildLabelCache(batch, specs, source);
        for (ReportSubject reportSubject : batch) {
            extractions.put(reportSubject.id(), this.extractFor(reportSubject, source, specs, cache, dateBucket));
            if (subjects != null) subjects.add(reportSubject.withoutAnswers());
        }
    }

    /** What a report keeps of the subjects it read. */
    private static final class Collected {

        private final List<ReportSubject> subjects = new ArrayList<>();
        private final Map<UUID, Map<String, ReportValueExtractor.Extraction>> extractions = new LinkedHashMap<>();
        private final Map<UUID, Map<String, ReportValueExtractor.Extraction>> planExtractions = new LinkedHashMap<>();
    }

    /**
     * Plan answers moved from a flat {@code planBlueprintValues} list to the same fieldset
     * shape assets use. Installations that have not run
     * {@code misc-actions/plan/migrate-fields} still hold the old shape, which parses to no
     * fieldsets at all — detected here so the report can say "not migrated" rather than
     * quietly report those plans as empty.
     */
    private PlanAnswers readPlanAnswers(PlanEntity plan) {
        if (plan.getProperties() == null || plan.getProperties().isBlank())
            return new PlanAnswers(plan.getId(), plan.getBlueprintId(), null, false);

        PlanPropertiesEntity properties = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, plan.getProperties());
        boolean hasFieldSets = properties != null && properties.getFieldSets() != null && !properties.getFieldSets().isEmpty();
        boolean legacy = !hasFieldSets && plan.getProperties().contains("planBlueprintValues");

        PropertyDefinitionEntity propertyDefinition = null;
        if (hasFieldSets) {
            propertyDefinition = new PropertyDefinitionEntity();
            propertyDefinition.setFieldSets(properties.getFieldSets());
        }
        return new PlanAnswers(plan.getId(), plan.getBlueprintId(), propertyDefinition, legacy);
    }

    private static IsActive[] activeValues(boolean includeInactive) {
        return includeInactive ? new IsActive[]{IsActive.Active, IsActive.Inactive} : new IsActive[]{IsActive.Active};
    }

    private static FieldSet planFieldSet(boolean withProperties) {
        FieldSet fieldSet = new BaseFieldSet()
                .ensure(Plan._id)
                .ensure(Plan._label)
                .ensure(Plan._version)
                .ensure(Plan._versionStatus)
                .ensure(Plan._status)
                .ensure(Plan._accessType)
                .ensure(Plan._blueprint)
                .ensure(Plan._groupId)
                .ensure(Plan._createdAt)
                .ensure(Plan._finalizedAt)
                .ensure(PlanEntity._tenantId);
        if (withProperties) fieldSet.ensure(Plan._properties);
        return fieldSet;
    }

    private static List<ReportDimensionSpec> buildDimensionSpecs(ReportLookup lookup, List<ReportQuestionSpec> specs, List<ReportQuestionSpec> planSpecs) {
        List<ReportDimensionSpec> dimensions = new ArrayList<>();
        if (lookup.getGroupBy() == null) return dimensions;

        for (ReportLookup.Dimension dimension : lookup.getGroupBy()) {
            String label = switch (dimension.getType()) {
                case DescriptionStatus -> "Asset status";
                case PlanStatus -> "Plan status";
                case PlanAccessType -> "Plan access";
                case PlanBlueprint -> "Plan blueprint";
                case PlanVersionStatus -> "Plan version status";
                case TemplateVersion -> "Template version";
                case CreatedPeriod -> "Created";
                case FinalizedPeriod -> "Finalized";
                case Question -> labelOfQuestion(specs, dimension.getQuestionKey());
                case PlanQuestion -> "Plan: " + labelOfQuestion(planSpecs, dimension.getQuestionKey());
                case Tenant -> "Tenant";
            };
            dimensions.add(new ReportDimensionSpec(dimension.getType(), dimension.getQuestionKey(), label));
        }
        return dimensions;
    }

    private static String labelOfQuestion(List<ReportQuestionSpec> specs, String key) {
        return specs.stream().filter(x -> x.key().equals(key)).map(ReportQuestionSpec::label).findFirst().orElse(key);
    }

    private List<ReportQuestionSpec> buildSpecs(List<ReportLookup.Question> questions, ReportDefinitionResolver.ParsedSource source, String keyPrefix) throws MyApplicationException {
        if (questions == null || questions.isEmpty()) throw this.questionNotFound();
        if (questions.size() > this.properties.getMaxQuestions()) throw this.questionNotFound();

        List<ReportQuestionSpec> specs = new ArrayList<>();
        int index = 0;
        for (ReportLookup.Question question : questions) {
            if (question == null) throw this.questionNotFound();
            boolean bySemantic = question.getSemantic() != null && !question.getSemantic().isBlank();
            specs.add(bySemantic
                    ? this.semanticSpec(keyPrefix + index, question, source)
                    : this.fieldSetSpec(keyPrefix + index, question, source));
            index++;
        }
        return specs;
    }

    private ReportQuestionSpec fieldSetSpec(String key, ReportLookup.Question question, ReportDefinitionResolver.ParsedSource source) throws MyApplicationException {
        if (question.getFieldSetId() == null || question.getFieldIds() == null || question.getFieldIds().isEmpty()) throw this.questionNotFound();
        // Field ids only mean something inside one lineage.
        if (source.spansAllGroups() || source.isUnknownFieldSet(question.getFieldSetId())) throw this.questionNotFound();
        for (String fieldId : question.getFieldIds()) {
            if (source.isUnknownField(fieldId)) throw this.questionNotFound();
        }

        ReportDefinitionResolver.ParsedVersion latest = source.getLatest();
        String numbering = latest != null ? latest.fieldSetNumbering(question.getFieldSetId()) : null;
        String title = latest != null ? latest.fieldSetTitle(question.getFieldSetId()) : null;
        String label = question.getLabel() != null && !question.getLabel().isBlank()
                ? question.getLabel().trim()
                : joinNonBlank(numbering, title);

        org.opencdmp.commons.types.descriptiontemplate.FieldEntity representative = null;
        for (String fieldId : question.getFieldIds()) {
            representative = latest != null ? latest.field(fieldId) : null;
            if (representative != null) break;
        }
        // A field dropped from the latest version is still reportable on the older ones.
        if (representative == null) {
            for (ReportDefinitionResolver.ParsedVersion version : source.getVersions().values()) {
                for (String fieldId : question.getFieldIds()) {
                    representative = version.field(fieldId);
                    if (representative != null) break;
                }
                if (representative != null) break;
            }
        }

        return new ReportQuestionSpec(key, label, numbering, question.getFieldSetId(), List.copyOf(question.getFieldIds()), null, fieldTypeOf(representative));
    }

    /**
     * A semantic question has no single fieldset behind it: each version contributes the
     * fields it tagged. The label and type come from wherever the tag is found first, unless
     * the caller names the question itself.
     */
    private ReportQuestionSpec semanticSpec(String key, ReportLookup.Question question, ReportDefinitionResolver.ParsedSource source) throws MyApplicationException {
        String semantic = ReportDefinitionResolver.normaliseSemantic(question.getSemantic());
        if (semantic == null || source.isUnknownSemantic(semantic)) throw this.questionNotFound();

        org.opencdmp.commons.types.descriptiontemplate.FieldEntity representative = null;
        for (ReportDefinitionResolver.ParsedVersion version : source.getVersions().values()) {
            List<String> fieldIds = version.fieldIdsWithSemantic(semantic);
            if (fieldIds.isEmpty()) continue;
            representative = version.field(fieldIds.get(0));
            break;
        }

        String label = question.getLabel() != null && !question.getLabel().isBlank() ? question.getLabel().trim() : semantic;
        return new ReportQuestionSpec(key, label, null, null, null, semantic, fieldTypeOf(representative));
    }

    private static String fieldTypeOf(org.opencdmp.commons.types.descriptiontemplate.FieldEntity field) {
        return field != null && field.getData() != null && field.getData().getFieldType() != null
                ? field.getData().getFieldType().getValue()
                : null;
    }


    private void validateDimensions(ReportLookup lookup, ReportSubjectType subject, List<ReportQuestionSpec> specs, List<ReportQuestionSpec> planSpecs, boolean installationWide) throws MyApplicationException {
        if (lookup.getGroupBy() == null || lookup.getGroupBy().isEmpty()) return;
        if (lookup.getGroupBy().size() > this.properties.getMaxDimensions()) throw this.questionNotFound();

        Set<String> keys = specs.stream().map(ReportQuestionSpec::key).collect(Collectors.toSet());
        Set<String> planKeys = planSpecs.stream().map(ReportQuestionSpec::key).collect(Collectors.toSet());
        boolean planSubject = ReportSubjectType.Plan.equals(subject);

        for (ReportLookup.Dimension dimension : lookup.getGroupBy()) {
            if (dimension == null || dimension.getType() == null) throw this.questionNotFound();

            switch (dimension.getType()) {
                case Question -> {
                    if (dimension.getQuestionKey() == null || !keys.contains(dimension.getQuestionKey())) throw this.questionNotFound();
                }
                case PlanQuestion -> {
                    // On a plan report the blueprint answers are the questions themselves.
                    if (planSubject || dimension.getQuestionKey() == null || !planKeys.contains(dimension.getQuestionKey())) throw this.questionNotFound();
                }
                case DescriptionStatus, TemplateVersion -> {
                    if (planSubject) throw this.questionNotFound();
                }
                case Tenant -> {
                    if (!installationWide) throw this.questionNotFound();
                }
                default -> {
                }
            }
        }
    }

    private void readDescriptionSubjects(ReportLookup lookup, boolean includeInactive, boolean latestVersionOnly, boolean installationWide, Set<UUID> templateIds,
                                         ReportDefinitionResolver.ParsedSource source, List<ReportQuestionSpec> specs,
                                         ReportDefinitionResolver.ParsedSource planSource, List<ReportQuestionSpec> planSpecs,
                                         ReportDateBucket dateBucket, Collected collected) throws MyApplicationException, InvalidApplicationException {
        if (templateIds != null && templateIds.isEmpty()) return;
        IsActive[] activeValues = activeValues(includeInactive);

        DescriptionQuery query = lookup.getDescriptionSubQuery() != null
                ? lookup.getDescriptionSubQuery().enrich(this.queryFactory)
                : this.queryFactory.query(DescriptionQuery.class);

        // A report is tenant-wide by intent: the caller holds a tenant-level permission, so
        // collaborator filtering would silently report only the reader's own plans. The tenant
        // is pinned explicitly, as the entity filter also lets default-tenant rows through.
        query.authorize(EnumSet.of(AuthorizationFlags.None)).disableTracking();
        TenantQuery tenantQuery = this.currentTenantQuery(installationWide);
        if (tenantQuery != null) query.tenantSubQuery(tenantQuery);

        if (lookup.getDescriptionTemplateGroupId() != null || templateIds != null || (lookup.getDescriptionSubQuery() != null && lookup.getDescriptionSubQuery().getDescriptionTemplateSubQuery() != null)) {
            DescriptionTemplateQuery templateQuery = lookup.getDescriptionSubQuery() != null && lookup.getDescriptionSubQuery().getDescriptionTemplateSubQuery() != null
                    ? lookup.getDescriptionSubQuery().getDescriptionTemplateSubQuery().enrich(this.queryFactory)
                    : this.queryFactory.query(DescriptionTemplateQuery.class);
            templateQuery.disableTracking();
            if (lookup.getDescriptionTemplateGroupId() != null) templateQuery.groupIds(lookup.getDescriptionTemplateGroupId());
            else if (templateIds != null) templateQuery.ids(templateIds);
            query.descriptionTemplateSubQuery(templateQuery);
        }

        PlanLookup planLookup = lookup.getPlanSubQuery() != null
                ? lookup.getPlanSubQuery()
                : (lookup.getDescriptionSubQuery() != null ? lookup.getDescriptionSubQuery().getPlanSubQuery() : null);
        PlanQuery planQuery = planLookup != null ? planLookup.enrich(this.queryFactory) : this.queryFactory.query(PlanQuery.class);
        planQuery.authorize(EnumSet.of(AuthorizationFlags.None)).disableTracking().isActive(activeValues);
        if (latestVersionOnly) planQuery.versionStatuses(PlanVersionStatus.Current, PlanVersionStatus.NotFinalized);
        if (lookup.getPlanBlueprintGroupId() != null) {
            planQuery.planBlueprintSubQuery(this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().groupIds(lookup.getPlanBlueprintGroupId()));
        }
        query.planSubQuery(planQuery);

        query.isActive(activeValues);
        query.setOrder(new Ordering().addAscending(Description._createdAt).addAscending(Description._id));

        FieldSet fieldSet = new BaseFieldSet()
                .ensure(Description._id)
                .ensure(Description._label)
                // DescriptionQuery maps this column by prefix only, so a bare "properties"
                // selects nothing and every asset would read as unanswered.
                .ensure(Description._properties + "." + PropertyDefinition._fieldSets)
                .ensure(Description._status)
                .ensure(Description._descriptionTemplate)
                .ensure(Description._plan)
                .ensure(Description._createdAt)
                .ensure(Description._finalizedAt)
                .ensure(DescriptionEntity._tenantId);

        // The filters run once, for the ids; the rows are then read by id a batch at a time.
        // Paging the filtered query itself would re-run its filters and sort for every batch.
        // One over the cap so an oversized set is detected rather than silently truncated.
        int limit = this.maxSubjects(installationWide);
        query.setPage(new Paging(0, limit + 1));
        List<DescriptionEntity> matching = query.collectAs(new BaseFieldSet().ensure(Description._id).ensure(Description._createdAt));
        if (matching == null || matching.isEmpty()) return;
        if (matching.size() > limit) {
            query.setPage(null);
            throw this.subjectCapExceeded(query.count(), limit);
        }

        // Parent plans are fetched, pruned to their newest version and read once, however many
        // batches their assets span; the ones pruned away stay out of the map.
        Map<UUID, PlanEntity> plans = new HashMap<>();
        Set<UUID> seenPlanIds = new HashSet<>();
        for (List<UUID> chunk : batches(matching.stream().map(DescriptionEntity::getId).toList())) {
            List<DescriptionEntity> items = this.inOrder(chunk, this.queryFactory.query(DescriptionQuery.class)
                    .authorize(EnumSet.of(AuthorizationFlags.None))
                    .disableTracking()
                    .ids(chunk)
                    .isActive(activeValues)
                    .collectAs(fieldSet), DescriptionEntity::getId);

            Set<UUID> newPlanIds = items.stream().map(DescriptionEntity::getPlanId).filter(x -> x != null && !seenPlanIds.contains(x)).collect(Collectors.toSet());
            seenPlanIds.addAll(newPlanIds);
            Map<UUID, PlanEntity> newPlans = this.collectPlans(newPlanIds, includeInactive, planSource != null);
            if (latestVersionOnly) newPlans = this.keepLatestOfEachLineage(newPlans, includeInactive, installationWide);
            plans.putAll(newPlans);

            this.extractBatch(this.buildDescriptionSubjects(items, plans), source, specs, dateBucket, collected.subjects, collected.extractions);

            // Plan answers are read once per plan, not once per asset: a plan with twenty
            // assets would otherwise be parsed and evaluated twenty times over.
            if (planSource != null) {
                List<ReportSubject> carriers = new ArrayList<>();
                for (PlanEntity plan : newPlans.values()) carriers.add(planSubjectOf(this.readPlanAnswers(plan)));
                this.extractBatch(carriers, planSource, planSpecs, dateBucket, null, collected.planExtractions);
                for (PlanEntity plan : newPlans.values()) plan.setProperties(null);
            }
        }
    }

    private Map<UUID, PlanEntity> collectPlans(Set<UUID> planIds, boolean includeInactive, boolean withProperties) throws InvalidApplicationException {
        Map<UUID, PlanEntity> plans = new HashMap<>();
        if (planIds.isEmpty()) return plans;

        for (List<UUID> chunk : chunked(planIds)) {
            List<PlanEntity> items = this.queryFactory.query(PlanQuery.class)
                    .authorize(EnumSet.of(AuthorizationFlags.None))
                    .disableTracking()
                    .ids(chunk)
                    .isActive(activeValues(includeInactive))
                    .collectAs(planFieldSet(withProperties));
            if (items != null) for (PlanEntity plan : items) plans.put(plan.getId(), plan);
        }
        return plans;
    }

    /**
     * A plan lineage can hold a finalized current version and a draft of the next one at the
     * same time; the newest one stands for the plan. It is chosen over the whole lineage, not
     * over the filtered plans: otherwise a status filter that drops the draft would bring the
     * older version back, and the filter and the breakdown by status would disagree.
     */
    private Map<UUID, PlanEntity> keepLatestOfEachLineage(Map<UUID, PlanEntity> plans, boolean includeInactive, boolean installationWide) throws InvalidApplicationException {
        Set<UUID> groupIds = plans.values().stream().map(PlanEntity::getGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, PlanEntity> newestOfGroup = new HashMap<>();
        for (List<UUID> chunk : chunked(groupIds)) {
            PlanQuery query = this.queryFactory.query(PlanQuery.class)
                    .authorize(EnumSet.of(AuthorizationFlags.None))
                    .disableTracking()
                    .groupIds(chunk)
                    .versionStatuses(PlanVersionStatus.Current, PlanVersionStatus.NotFinalized)
                    .isActive(activeValues(includeInactive));
            TenantQuery tenantQuery = this.currentTenantQuery(installationWide);
            if (tenantQuery != null) query.tenantSubQuery(tenantQuery);
            List<PlanEntity> lineage = query.collectAs(new BaseFieldSet().ensure(Plan._id).ensure(Plan._groupId).ensure(Plan._version));
            if (lineage != null) for (PlanEntity plan : lineage) {
                PlanEntity current = newestOfGroup.get(plan.getGroupId());
                if (current == null || versionOf(plan) > versionOf(current)) newestOfGroup.put(plan.getGroupId(), plan);
            }
        }

        Map<UUID, PlanEntity> kept = new LinkedHashMap<>();
        for (PlanEntity plan : plans.values()) {
            PlanEntity newest = plan.getGroupId() != null ? newestOfGroup.get(plan.getGroupId()) : null;
            if (newest == null || newest.getId().equals(plan.getId())) kept.put(plan.getId(), plan);
        }
        return kept;
    }

    /**
     * Id lists that grow with the report are queried in chunks: PostgreSQL's driver refuses a
     * statement with more than 32,767 parameters, which a large installation-wide report reaches.
     */
    static <T> List<List<T>> chunked(java.util.Collection<T> items) {
        List<T> all = new ArrayList<>(items);
        List<List<T>> chunks = new ArrayList<>();
        for (int start = 0; start < all.size(); start += ID_CHUNK) chunks.add(all.subList(start, Math.min(start + ID_CHUNK, all.size())));
        return chunks;
    }

    private static int versionOf(PlanEntity plan) {
        return plan.getVersion() != null ? plan.getVersion() : Integer.MIN_VALUE;
    }

    /**
     * The current tenant, or null when no pinning is needed: across the installation, and in
     * the default tenant, whose entity filter already keeps to its own rows.
     */
    private TenantQuery currentTenantQuery(boolean installationWide) throws InvalidApplicationException {
        if (installationWide) return null;
        UUID tenantId = this.tenantScopeFactory.getInstance().getTenant();
        return tenantId != null ? this.queryFactory.query(TenantQuery.class).disableTracking().ids(tenantId) : null;
    }

    private Map<UUID, String> collectTenantCodes(List<ReportSubject> subjects) throws InvalidApplicationException {
        Map<UUID, String> codes = new HashMap<>();
        codes.put(null, this.tenantScopeFactory.getInstance().getDefaultTenantCode());
        Set<UUID> ids = subjects.stream().map(ReportSubject::tenantId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return codes;
        List<TenantEntity> items = this.queryFactory.query(TenantQuery.class).disableTracking().ids(ids)
                .collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
        if (items != null) for (TenantEntity item : items) codes.put(item.getId(), item.getCode());
        return codes;
    }

    private Map<UUID, String> collectDescriptionStatusNames(List<ReportSubject> subjects) throws InvalidApplicationException {
        Set<UUID> ids = subjects.stream().map(ReportSubject::statusId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> names = new HashMap<>();
        if (ids.isEmpty()) return names;
        List<DescriptionStatusEntity> items = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().ids(ids)
                .collectAs(new BaseFieldSet().ensure(DescriptionStatus._id).ensure(DescriptionStatus._name));
        if (items != null) for (DescriptionStatusEntity item : items) names.put(item.getId(), item.getName());
        return names;
    }

    private Map<UUID, String> collectPlanStatusNames(List<ReportSubject> subjects) throws InvalidApplicationException {
        Set<UUID> ids = subjects.stream().map(ReportSubject::planStatusId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> names = new HashMap<>();
        if (ids.isEmpty()) return names;
        List<PlanStatusEntity> items = this.queryFactory.query(PlanStatusQuery.class).disableTracking().ids(ids)
                .collectAs(new BaseFieldSet().ensure(PlanStatus._id).ensure(PlanStatus._name));
        if (items != null) for (PlanStatusEntity item : items) names.put(item.getId(), item.getName());
        return names;
    }

    private Map<UUID, String> collectBlueprintLabels(List<ReportSubject> subjects) throws InvalidApplicationException {
        Set<UUID> ids = subjects.stream().map(ReportSubject::planBlueprintId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> labels = new HashMap<>();
        if (ids.isEmpty()) return labels;
        List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(ids)
                .collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._label));
        if (items != null) for (PlanBlueprintEntity item : items) labels.put(item.getId(), item.getLabel());
        return labels;
    }

    private MyApplicationException questionNotFound() {
        return new MyApplicationException(this.errors.getReportQuestionNotFound().getCode(), this.errors.getReportQuestionNotFound().getMessage());
    }

    /** Across every tenant the limit is separate, and higher: the same report simply reaches further. */
    private int maxSubjects(boolean installationWide) {
        return installationWide ? this.properties.getMaxSubjectsInstallation() : this.properties.getMaxSubjects();
    }

    /** Says how far over the limit the report is, so the reader knows how much to narrow it. */
    private MyApplicationException subjectCapExceeded(long matched, int limit) {
        return new MyApplicationException(this.errors.getReportSubjectCapExceeded().getCode(),
                this.errors.getReportSubjectCapExceeded().getMessage() + " (matched: " + matched + ", limit: " + limit + ")");
    }

    //endregion

    //region aggregation

    private Report aggregate(ReportAnalysis analysis) {
        List<ReportDimensionSpec> dimensions = analysis.getDimensions();

        Bucket global = new Bucket(analysis.getSpecs());
        Map<List<String>, Bucket> crossTab = new LinkedHashMap<>();

        for (ReportSubject subject : analysis.getSubjects()) {
            Map<String, ReportValueExtractor.Extraction> extraction = analysis.getExtractions().get(subject.id());
            global.add(subject, extraction, analysis.getSpecs());

            if (dimensions.isEmpty()) continue;
            List<String> key = new ArrayList<>();
            for (ReportDimensionSpec dimension : dimensions) key.add(this.dimensionValue(analysis, subject, extraction, dimension));
            crossTab.computeIfAbsent(key, x -> new Bucket(analysis.getSpecs())).add(subject, extraction, analysis.getSpecs());
        }

        Map<String, Set<String>> keptTextValues = new HashMap<>();
        for (ReportQuestionSpec spec : analysis.getSpecs()) {
            Set<String> kept = keptValuesFor(spec, global.questions.get(spec.key()), analysis.getTextTopN());
            if (kept != null) keptTextValues.put(spec.key(), kept);
        }

        Report report = new Report();
        report.setSubject(analysis.getSubject());
        report.setSourceGroupId(analysis.getSource().getGroupId());
        report.setSourceLabel(analysis.getSource().getLabel());
        report.setSourceVersionCount(analysis.getSource().getVersionCount());
        report.setSourceSpansAllGroups(analysis.getSource().spansAllGroups());
        report.setGeneratedAt(Instant.now());
        report.setSubjects(global.subjects);
        report.setDistinctPlans(global.planIds.size());

        List<Report.Dimension> dimensionModels = new ArrayList<>();
        for (ReportDimensionSpec dimension : dimensions) {
            Report.Dimension model = new Report.Dimension();
            model.setType(dimension.type());
            model.setQuestionKey(dimension.questionKey());
            model.setLabel(dimension.label());
            dimensionModels.add(model);
        }
        report.setDimensions(dimensionModels);

        List<Report.QuestionResult> questionResults = new ArrayList<>();
        for (ReportQuestionSpec spec : analysis.getSpecs()) {
            QuestionAccumulator accumulator = global.questions.get(spec.key());
            Report.QuestionResult result = new Report.QuestionResult();
            result.setKey(spec.key());
            result.setLabel(spec.label());
            result.setNumbering(spec.numbering());
            result.setFieldType(spec.fieldType());
            // Only when someone actually gave several answers can the counts outrun the answered total.
            result.setMultiValue(accumulator != null && accumulator.multiValued);
            result.setStates(statesOf(accumulator));
            result.setDistribution(distributionOf(accumulator, keptTextValues.get(spec.key())));
            result.setNumberSummary(numberSummaryOf(accumulator));
            questionResults.add(result);
        }
        report.setQuestions(questionResults);

        List<Report.CrossTabRow> rows = new ArrayList<>();
        for (Map.Entry<List<String>, Bucket> entry : crossTab.entrySet()) {
            Report.CrossTabRow row = new Report.CrossTabRow();
            row.setDimensionValues(entry.getKey());
            row.setSubjects(entry.getValue().subjects);
            row.setDistinctPlans(entry.getValue().planIds.size());

            List<Report.QuestionCell> cells = new ArrayList<>();
            for (ReportQuestionSpec spec : analysis.getSpecs()) {
                QuestionAccumulator accumulator = entry.getValue().questions.get(spec.key());
                Report.QuestionCell cell = new Report.QuestionCell();
                cell.setKey(spec.key());
                cell.setStates(statesOf(accumulator));
                cell.setDistribution(distributionOf(accumulator, keptTextValues.get(spec.key())));
                cells.add(cell);
            }
            row.setQuestions(cells);
            rows.add(row);
        }
        rows.sort((left, right) -> compareDimensionValues(left.getDimensionValues(), right.getDimensionValues()));
        report.setCrossTab(rows);

        return report;
    }

    private String dimensionValue(ReportAnalysis analysis, ReportSubject subject, Map<String, ReportValueExtractor.Extraction> extraction, ReportDimensionSpec dimension) {
        return switch (dimension.type()) {
            case DescriptionStatus -> orUnknown(analysis.getDescriptionStatusNames().get(subject.statusId()));
            case PlanStatus -> orUnknown(analysis.getPlanStatusNames().get(subject.planStatusId()));
            case PlanAccessType -> subject.planAccessType() != null ? subject.planAccessType().name() : UNKNOWN_LABEL;
            case PlanBlueprint -> orUnknown(analysis.getBlueprintLabels().get(subject.planBlueprintId()));
            case PlanVersionStatus -> subject.planVersionStatus() != null ? subject.planVersionStatus().name() : UNKNOWN_LABEL;
            case TemplateVersion -> {
                ReportDefinitionResolver.ParsedVersion version = analysis.getSource().getVersions().get(subject.sourceVersionId());
                if (version == null || version.getVersion() == null) yield UNKNOWN_LABEL;
                // Across templates a bare "v2" says nothing; the template's name has to come along.
                yield analysis.getSource().spansAllGroups() ? version.getLabel() + " v" + version.getVersion() : "v" + version.getVersion();
            }
            case CreatedPeriod -> subject.createdAt() != null ? ReportValueExtractor.bucketOf(subject.createdAt(), analysis.getDateBucket()) : UNKNOWN_LABEL;
            case FinalizedPeriod -> subject.finalizedAt() != null ? ReportValueExtractor.bucketOf(subject.finalizedAt(), analysis.getDateBucket()) : NOT_FINALIZED_LABEL;
            case Question -> answerAsDimensionValue(extraction != null ? extraction.get(dimension.questionKey()) : null);
            case PlanQuestion -> {
                Map<String, ReportValueExtractor.Extraction> planExtraction = subject.planId() != null ? analysis.getPlanExtractions().get(subject.planId()) : null;
                yield answerAsDimensionValue(planExtraction != null ? planExtraction.get(dimension.questionKey()) : null);
            }
            case Tenant -> orUnknown(analysis.getTenantCodes().get(subject.tenantId()));
        };
    }

    static String answerAsDimensionValue(ReportValueExtractor.Extraction extraction) {
        if (extraction == null) return UNKNOWN_LABEL;
        if (ReportAnswerState.NotApplicable.equals(extraction.getState())) return NOT_APPLICABLE_LABEL;
        if (ReportAnswerState.NotMigrated.equals(extraction.getState())) return NOT_MIGRATED_LABEL;
        if (extraction.getValues().isEmpty()) return NOT_ANSWERED_LABEL;
        return String.join("; ", extraction.getValues());
    }

    /** Orders cross-tab rows by their dimension values, outermost dimension first. */
    private static int compareDimensionValues(List<String> left, List<String> right) {
        int shared = Math.min(left.size(), right.size());
        for (int index = 0; index < shared; index++) {
            int comparison = naturalCompare(left.get(index), right.get(index));
            if (comparison != 0) return comparison;
        }
        return Integer.compare(left.size(), right.size());
    }

    /** Compares runs of digits by value, so "v5" sorts before "v18". */
    private static int naturalCompare(String left, String right) {
        int i = 0, j = 0;
        while (i < left.length() && j < right.length()) {
            char a = left.charAt(i), b = right.charAt(j);
            if (Character.isDigit(a) && Character.isDigit(b)) {
                int startA = i, startB = j;
                while (i < left.length() && Character.isDigit(left.charAt(i))) i++;
                while (j < right.length() && Character.isDigit(right.charAt(j))) j++;
                int comparison = new java.math.BigInteger(left.substring(startA, i)).compareTo(new java.math.BigInteger(right.substring(startB, j)));
                if (comparison != 0) return comparison;
                continue;
            }
            int comparison = Character.compare(Character.toLowerCase(a), Character.toLowerCase(b));
            if (comparison != 0) return comparison;
            i++;
            j++;
        }
        return Integer.compare(left.length() - i, right.length() - j);
    }

    private static String orUnknown(String value) {
        return value != null && !value.isBlank() ? value : UNKNOWN_LABEL;
    }

    private static String joinNonBlank(String numbering, String title) {
        if (numbering == null || numbering.isBlank()) return title != null ? title : "";
        if (title == null || title.isBlank()) return numbering;
        return numbering + " " + title;
    }

    private static Report.AnswerStates statesOf(QuestionAccumulator accumulator) {
        Report.AnswerStates states = new Report.AnswerStates();
        if (accumulator == null) return states;
        states.setAnswered(accumulator.answered);
        states.setNotAnswered(accumulator.notAnswered);
        states.setNotApplicable(accumulator.notApplicable);
        states.setNotMigrated(accumulator.notMigrated);
        return states;
    }

    /**
     * Free text has as many distinct answers as there are authors, so only the most common
     * ones are reported and the tail is summarised. Which values survive is decided once,
     * over the whole report, so a cross-tab row uses the same labels as the totals.
     */
    private static Set<String> keptValuesFor(ReportQuestionSpec spec, QuestionAccumulator accumulator, int topN) {
        if (accumulator == null || spec.fieldType() == null) return null;
        boolean freeText = org.opencdmp.commons.enums.FieldType.Names.FreeText.equals(spec.fieldType())
                || org.opencdmp.commons.enums.FieldType.Names.TextArea.equals(spec.fieldType())
                || org.opencdmp.commons.enums.FieldType.Names.RichTextarea.equals(spec.fieldType());
        if (!freeText || accumulator.values.size() <= topN) return null;

        return accumulator.values.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<Report.DistributionEntry> distributionOf(QuestionAccumulator accumulator, Set<String> keptValues) {
        List<Report.DistributionEntry> entries = new ArrayList<>();
        if (accumulator == null) return entries;

        long folded = 0;
        long foldedDistinct = 0;
        List<Map.Entry<String, Long>> ordered = accumulator.values.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .toList();

        for (Map.Entry<String, Long> value : ordered) {
            if (keptValues != null && !keptValues.contains(value.getKey())) {
                folded += value.getValue();
                foldedDistinct++;
                continue;
            }
            entries.add(distributionEntry(value.getKey(), value.getValue(), accumulator.answered));
        }
        if (folded > 0) entries.add(distributionEntry(ReportValueExtractor.otherTextLabel(foldedDistinct), folded, accumulator.answered));
        return entries;
    }

    private static Report.DistributionEntry distributionEntry(String value, long count, long answered) {
        Report.DistributionEntry entry = new Report.DistributionEntry();
        entry.setValue(value);
        entry.setCount(count);
        entry.setPercentOfAnswered(answered > 0 ? Math.round(count * 1000.0 / answered) / 10.0 : 0.0);
        return entry;
    }

    private static Report.NumberSummary numberSummaryOf(QuestionAccumulator accumulator) {
        if (accumulator == null || accumulator.numberCount == 0) return null;
        Report.NumberSummary summary = new Report.NumberSummary();
        summary.setCount(accumulator.numberCount);
        summary.setSum(accumulator.sum);
        summary.setAverage(accumulator.sum / accumulator.numberCount);
        summary.setMin(accumulator.min != null ? accumulator.min : 0.0);
        summary.setMax(accumulator.max != null ? accumulator.max : 0.0);
        return summary;
    }

    //endregion

    /** Running counts for one question inside one bucket. */
    private static final class QuestionAccumulator {

        private long answered;
        private long notAnswered;
        private long notApplicable;
        private long notMigrated;
        private final Map<String, Long> values = new LinkedHashMap<>();
        private double sum;
        private long numberCount;
        private Double min;
        private Double max;
        private boolean multiValued;

        private void add(ReportValueExtractor.Extraction extraction) {
            if (extraction == null) {
                this.notApplicable++;
                return;
            }
            if (extraction.getValues().size() > 1) this.multiValued = true;
            switch (extraction.getState()) {
                case Answered -> this.answered++;
                case NotAnswered -> this.notAnswered++;
                case NotApplicable -> this.notApplicable++;
                case NotMigrated -> this.notMigrated++;
            }
            for (String value : extraction.getValues()) this.values.merge(value, 1L, Long::sum);
            for (Double number : extraction.getNumbers()) {
                this.sum += number;
                this.numberCount++;
                this.min = this.min == null ? number : Math.min(this.min, number);
                this.max = this.max == null ? number : Math.max(this.max, number);
            }
        }
    }

    /** The subjects sharing one combination of dimension values, or all of them. */
    private static final class Bucket {

        private long subjects;
        private final Set<UUID> planIds = new HashSet<>();
        private final Map<String, QuestionAccumulator> questions = new LinkedHashMap<>();

        private Bucket(List<ReportQuestionSpec> specs) {
            for (ReportQuestionSpec spec : specs) this.questions.put(spec.key(), new QuestionAccumulator());
        }

        private void add(ReportSubject subject, Map<String, ReportValueExtractor.Extraction> extraction, List<ReportQuestionSpec> specs) {
            this.subjects++;
            if (subject.planId() != null) this.planIds.add(subject.planId());
            for (ReportQuestionSpec spec : specs) {
                this.questions.get(spec.key()).add(extraction != null ? extraction.get(spec.key()) : null);
            }
        }
    }

    /** A plan's readable answers, or the fact that they are still in the pre-1.4 shape. */
    private record PlanAnswers(UUID planId, UUID blueprintId, PropertyDefinitionEntity properties, boolean legacy) {
    }
}
