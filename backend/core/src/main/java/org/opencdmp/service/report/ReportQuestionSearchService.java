package org.opencdmp.service.report;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.opencdmp.commons.enums.ReportSubjectType;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.report.ReportOutline;
import org.opencdmp.model.report.ReportQuestionSearch;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.lookup.ReportQuestionSearchLookup;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds a question by what a person remembers of it — its number, a word of its title, a
 * field label — across every template and blueprint of the tenant, so a report starts from
 * the question rather than from the template it happens to live in.
 *
 * <p>Semantics are searched alongside. A tag that several templates share is the one thing
 * that lets "where will the data be deposited" be asked of all assets at once, so the same
 * search that finds FCT's 5.2.4 also offers the cross-template version of it.</p>
 *
 * <p>Parsing the current version of every lineage costs about a second on a tenant with a
 * few hundred templates, and a search box asks on every keystroke, so the parsed sources are
 * kept per tenant for a short while. A template published in that window is simply found a
 * minute later.</p>
 */
@Service
public class ReportQuestionSearchService {

    private static final int DEFAULT_LIMIT = 30;

    private final ReportDefinitionResolver definitionResolver;
    private final TenantScopeFactory tenantScopeFactory;
    private final ReportProperties properties;
    private final QueryFactory queryFactory;
    private final Map<String, CachedSources> cache = new ConcurrentHashMap<>();

    public ReportQuestionSearchService(ReportDefinitionResolver definitionResolver, TenantScopeFactory tenantScopeFactory, ReportProperties properties, QueryFactory queryFactory) {
        this.definitionResolver = definitionResolver;
        this.tenantScopeFactory = tenantScopeFactory;
        this.properties = properties;
        this.queryFactory = queryFactory;
    }

    public ReportQuestionSearch search(ReportQuestionSearchLookup lookup, boolean installationWide) throws InvalidApplicationException {
        String like = normalise(lookup.getLike());
        int limit = lookup.getLimit() != null && lookup.getLimit() > 0 ? lookup.getLimit() : DEFAULT_LIMIT;

        List<ReportQuestionSearch.QuestionHit> questions = new ArrayList<>();
        Map<String, SemanticAccumulator> semantics = new LinkedHashMap<>();

        List<ReportDefinitionResolver.ParsedSource> descriptionSources = lookup.getSubject() == null || ReportSubjectType.Description.equals(lookup.getSubject())
                ? this.currentSources(ReportSubjectType.Description, installationWide) : List.of();
        List<ReportDefinitionResolver.ParsedSource> planSources = lookup.getSubject() == null || ReportSubjectType.Plan.equals(lookup.getSubject())
                ? this.currentSources(ReportSubjectType.Plan, installationWide) : List.of();
        // Across tenants several lineages can share a name; the tenant code tells them apart.
        Map<UUID, String> tenantCodes = installationWide ? this.tenantCodes(descriptionSources, planSources) : null;

        for (ReportDefinitionResolver.ParsedSource source : descriptionSources) {
            this.index(source, ReportSubjectType.Description, like, tenantCodes, questions, semantics);
        }
        for (ReportDefinitionResolver.ParsedSource source : planSources) {
            this.index(source, ReportSubjectType.Plan, like, tenantCodes, questions, semantics);
        }

        ReportQuestionSearch result = new ReportQuestionSearch();
        // Without a search term the caller wants the semantics in use, not every question there is.
        // The limit is per subject: asset templates are indexed first and would otherwise use it all up.
        List<ReportQuestionSearch.QuestionHit> limited = new ArrayList<>();
        if (!like.isEmpty()) {
            limited.addAll(questions.stream().filter(x -> ReportSubjectType.Description.equals(x.getSubject())).limit(limit).toList());
            limited.addAll(questions.stream().filter(x -> ReportSubjectType.Plan.equals(x.getSubject())).limit(limit).toList());
        }
        result.setQuestions(limited);

        List<ReportQuestionSearch.SemanticHit> semanticHits = new ArrayList<>();
        for (SemanticAccumulator accumulator : semantics.values()) {
            if (!like.isEmpty() && !accumulator.matches(like)) continue;
            semanticHits.add(accumulator.toHit());
        }
        // The tags most templates share come first: they are the ones worth a cross-template
        // report. The cap applies here too — a tenant can carry hundreds of tags, and the
        // caller shows a handful.
        semanticHits.sort(Comparator.comparingInt(ReportQuestionSearch.SemanticHit::getGroupCount).reversed()
                .thenComparing(ReportQuestionSearch.SemanticHit::getSemantic));
        result.setSemantics(semanticHits.stream().limit(limit).toList());

        if (Boolean.TRUE.equals(lookup.getIncludeSources())) {
            List<ReportQuestionSearch.SourceHit> sources = new ArrayList<>();
            sources.addAll(sourceHits(descriptionSources, ReportSubjectType.Description, like, tenantCodes));
            sources.addAll(sourceHits(planSources, ReportSubjectType.Plan, like, tenantCodes));
            result.setSources(sources.stream().limit(limit).toList());
        }
        return result;
    }

    /** The lineages whose name contains the text, or all of them without one; by name, then tenant. */
    private static List<ReportQuestionSearch.SourceHit> sourceHits(List<ReportDefinitionResolver.ParsedSource> sources, ReportSubjectType subject, String like, Map<UUID, String> tenantCodes) {
        List<ReportQuestionSearch.SourceHit> hits = new ArrayList<>();
        for (ReportDefinitionResolver.ParsedSource source : sources) {
            if (source.getGroupId() == null || source.getLabel() == null) continue;
            if (!like.isEmpty() && !contains(source.getLabel(), like)) continue;
            ReportQuestionSearch.SourceHit hit = new ReportQuestionSearch.SourceHit();
            hit.setSubject(subject);
            hit.setGroupId(source.getGroupId());
            hit.setLabel(source.getLabel());
            hit.setTenantCode(tenantCodes != null ? tenantCodes.get(source.getTenantId()) : null);
            hit.setVersionCount(source.getVersionCount());
            hits.add(hit);
        }
        hits.sort(Comparator.comparing(ReportQuestionSearch.SourceHit::getLabel, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(x -> x.getTenantCode() != null ? x.getTenantCode() : ""));
        return hits;
    }

    /** The parsed current versions of one subject's lineages, per tenant or for the installation, for a short while. */
    private List<ReportDefinitionResolver.ParsedSource> currentSources(ReportSubjectType subject, boolean installationWide) throws InvalidApplicationException {
        String key = (installationWide ? "installation" : this.tenantKey()) + "|" + subject.name();
        long ttl = Math.max(0, this.properties.getSearchCacheSeconds()) * 1000L;
        CachedSources cached = this.cache.get(key);
        if (cached != null && System.currentTimeMillis() - cached.loadedAt() < ttl) return cached.sources();

        List<ReportDefinitionResolver.ParsedSource> sources = ReportSubjectType.Plan.equals(subject)
                ? this.definitionResolver.resolveCurrentPlanBlueprints()
                : this.definitionResolver.resolveCurrentDescriptionTemplates();
        if (ttl > 0) this.cache.put(key, new CachedSources(sources, System.currentTimeMillis()));
        return sources;
    }

    private String tenantKey() throws InvalidApplicationException {
        TenantScope tenantScope = this.tenantScopeFactory.getInstance();
        if (tenantScope == null || !Boolean.TRUE.equals(tenantScope.isSet()) || Boolean.TRUE.equals(tenantScope.isDefaultTenant())) return "default";
        return String.valueOf(tenantScope.getTenant());
    }

    private record CachedSources(List<ReportDefinitionResolver.ParsedSource> sources, long loadedAt) {
    }

    /** Tenant codes by tenant id; the default tenant's lineages, which carry no tenant, are under the null key. */
    private Map<UUID, String> tenantCodes(List<ReportDefinitionResolver.ParsedSource> descriptionSources, List<ReportDefinitionResolver.ParsedSource> planSources) throws InvalidApplicationException {
        Map<UUID, String> codes = new HashMap<>();
        codes.put(null, this.tenantScopeFactory.getInstance().getDefaultTenantCode());
        Set<UUID> ids = new HashSet<>();
        for (ReportDefinitionResolver.ParsedSource source : descriptionSources) if (source.getTenantId() != null) ids.add(source.getTenantId());
        for (ReportDefinitionResolver.ParsedSource source : planSources) if (source.getTenantId() != null) ids.add(source.getTenantId());
        if (ids.isEmpty()) return codes;
        List<TenantEntity> items = this.queryFactory.query(TenantQuery.class).disableTracking().ids(ids)
                .collectAs(new BaseFieldSet().ensure(Tenant._id).ensure(Tenant._code));
        if (items != null) for (TenantEntity item : items) codes.put(item.getId(), item.getCode());
        return codes;
    }

    private void index(ReportDefinitionResolver.ParsedSource source, ReportSubjectType subject, String like, Map<UUID, String> tenantCodes,
                       List<ReportQuestionSearch.QuestionHit> questions, Map<String, SemanticAccumulator> semantics) {
        ReportDefinitionResolver.ParsedVersion version = source.getLatest();
        if (version == null) return;
        String tenantCode = tenantCodes != null ? tenantCodes.get(source.getTenantId()) : null;

        for (FieldSetEntity fieldSet : version.getFieldSets().values()) {
            List<FieldEntity> countable = ReportDefinitionResolver.countableFields(fieldSet);
            if (countable.isEmpty()) continue;
            // Every reportable field is searched and indexed, not only the ones counted by
            // default: a tag on the follow-up of a yes/no is still a question worth finding.
            List<FieldEntity> reportable = fieldSet.getFields().stream().filter(x -> version.field(x.getId()) != null).toList();

            String numbering = version.fieldSetNumbering(fieldSet.getId());
            for (FieldEntity field : reportable) {
                if (field.getSemantics() == null) continue;
                for (String semantic : field.getSemantics()) {
                    String key = ReportDefinitionResolver.normaliseSemantic(semantic);
                    if (key == null) continue;
                    semantics.computeIfAbsent(subject.name() + "|" + key, x -> new SemanticAccumulator(subject, key))
                            .add(source, tenantCode, fieldSet, field);
                }
            }

            if (like.isEmpty() || !matchesFieldSet(like, numbering, fieldSet, reportable)) continue;

            ReportQuestionSearch.QuestionHit hit = new ReportQuestionSearch.QuestionHit();
            hit.setSubject(subject);
            hit.setGroupId(source.getGroupId());
            hit.setGroupLabel(source.getLabel());
            hit.setTenantCode(tenantCode);
            hit.setVersionCount(source.getVersionCount());
            hit.setFieldSetId(fieldSet.getId());
            hit.setNumbering(numbering);
            hit.setTitle(fieldSet.getTitle());
            hit.setHasMultiplicity(fieldSet.getHasMultiplicity());
            hit.setFields(ReportDefinitionResolver.buildOutlineFields(countable));
            hit.setAllFields(ReportDefinitionResolver.buildOutlineFields(fieldSet.getFields()));
            questions.add(hit);
        }
    }

    /**
     * A numbering matches from its start ("5.2" finds 5.2.4 and 5.2.1, not 15.2); words match
     * anywhere in the fieldset title or in a field label.
     */
    private static boolean matchesFieldSet(String like, String numbering, FieldSetEntity fieldSet, List<FieldEntity> fields) {
        if (numbering != null && (numbering.equals(like) || numbering.startsWith(like + ".") || numbering.startsWith(like))) return true;
        if (contains(fieldSet.getTitle(), like) || contains(fieldSet.getDescription(), like)) return true;
        for (FieldEntity field : fields) {
            if (field.getData() != null && contains(field.getData().getLabel(), like)) return true;
        }
        return false;
    }

    private static boolean contains(String text, String like) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(like);
    }

    private static String normalise(String like) {
        if (like == null) return "";
        return like.replace("%", "").trim().toLowerCase(Locale.ROOT);
    }

    /** One semantic tag as seen across the templates that use it. */
    private static final class SemanticAccumulator {

        private final ReportSubjectType subject;
        private final String semantic;
        private final Map<UUID, String> groupLabels = new LinkedHashMap<>();
        private final Map<String, Integer> titles = new LinkedHashMap<>();
        private final Set<String> fieldTypes = new LinkedHashSet<>();

        private SemanticAccumulator(ReportSubjectType subject, String semantic) {
            this.subject = subject;
            this.semantic = semantic;
        }

        /** Lineages are counted by id: across tenants two of them can share a name. */
        private void add(ReportDefinitionResolver.ParsedSource source, String tenantCode, FieldSetEntity fieldSet, FieldEntity field) {
            if (source.getGroupId() != null && source.getLabel() != null) {
                this.groupLabels.putIfAbsent(source.getGroupId(), tenantCode != null ? source.getLabel() + " (" + tenantCode + ")" : source.getLabel());
            }
            if (fieldSet.getTitle() != null && !fieldSet.getTitle().isBlank()) this.titles.merge(fieldSet.getTitle().trim(), 1, Integer::sum);
            if (field.getData() != null && field.getData().getFieldType() != null) this.fieldTypes.add(field.getData().getFieldType().getValue());
        }

        /**
         * A tag matches on its own name or on the title most templates give it — not on every
         * title, because one template's stray tagging would otherwise surface the tag for
         * words it has nothing to do with.
         */
        private boolean matches(String like) {
            if (this.semantic.toLowerCase(Locale.ROOT).contains(like)) return true;
            String label = this.label();
            return label != null && label.toLowerCase(Locale.ROOT).contains(like);
        }

        private String label() {
            return this.titles.entrySet().stream()
                    .max(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.<String, Integer>comparingByKey().reversed()))
                    .map(Map.Entry::getKey).orElse(null);
        }

        private ReportQuestionSearch.SemanticHit toHit() {
            ReportQuestionSearch.SemanticHit hit = new ReportQuestionSearch.SemanticHit();
            hit.setSubject(this.subject);
            hit.setSemantic(this.semantic);
            hit.setLabel(this.label());
            hit.setTitles(this.titles.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                    .map(Map.Entry::getKey).toList());
            hit.setGroupCount(this.groupLabels.size());
            hit.setGroupLabels(new ArrayList<>(this.groupLabels.values()));
            hit.setFieldTypes(new ArrayList<>(this.fieldTypes));
            return hit;
        }
    }
}
