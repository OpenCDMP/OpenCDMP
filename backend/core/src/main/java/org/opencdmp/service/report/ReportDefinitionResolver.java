package org.opencdmp.service.report;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.opencdmp.commons.enums.ReportSubjectType;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.PageEntity;
import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.report.ReportOutline;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.PlanBlueprintQuery;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parses template and blueprint versions once and indexes them for the report engine: a
 * field id to its definition in <em>that</em> version, a fieldset id to the number the
 * editor's table of contents shows for it, and a semantic tag to the fields carrying it.
 *
 * <p>All versions of a lineage are needed, not just the newest one, because assets keep
 * pointing at the version they were written against. Field ids survive a new version, so
 * one selected question resolves in each version that still contains it; a version that
 * dropped the field simply yields no definition and its assets count as not applicable.</p>
 *
 * <p>A source can also span <em>every</em> lineage of the tenant. Field ids mean nothing
 * across templates, so such a source only serves questions addressed by semantic.</p>
 */
@Service
public class ReportDefinitionResolver {

    private static final int PARSED_VERSION_CACHE_LIMIT = 20000;

    private final QueryFactory queryFactory;
    private final XmlHandlingService xmlHandlingService;
    private final MessageSource messageSource;
    private final Map<UUID, CachedVersion> parsedVersions = new ConcurrentHashMap<>();

    public ReportDefinitionResolver(QueryFactory queryFactory, XmlHandlingService xmlHandlingService, MessageSource messageSource) {
        this.queryFactory = queryFactory;
        this.xmlHandlingService = xmlHandlingService;
        this.messageSource = messageSource;
    }

    //region asset templates

    public ParsedSource resolveDescriptionTemplateGroup(UUID groupId) throws InvalidApplicationException {
        // Deleted versions are read too: assets written against them still hold answers.
        List<DescriptionTemplateEntity> templates = this.queryFactory.query(DescriptionTemplateQuery.class)
                .disableTracking()
                .groupIds(groupId)
                .isActive(IsActive.Active, IsActive.Inactive)
                .collectAs(templateFieldSet(false));

        if (templates == null || templates.stream().noneMatch(x -> IsActive.Active.equals(x.getIsActive())))
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{groupId, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        ParsedSource source = assemble(this.parsedTemplateVersions(templates).values());
        source.groupId = groupId;
        return source;
    }

    /**
     * Every version of every asset template lineage that carries one of the semantics, as one
     * source. Versions without them are left out, so their assets are not read at all.
     */
    public ParsedSource resolveDescriptionTemplatesCarrying(Set<String> semantics) throws InvalidApplicationException {
        List<DescriptionTemplateEntity> templates = this.queryFactory.query(DescriptionTemplateQuery.class)
                .disableTracking()
                .isActive(IsActive.Active, IsActive.Inactive)
                .collectAs(templateFieldSet(false));
        ParsedSource source = assemble(carrying(this.parsedTemplateVersions(templates != null ? templates : List.of()).values(), semantics));
        source.groupId = null;
        source.label = null;
        source.latest = null;
        return source;
    }

    /**
     * The current version of each asset template lineage, one source per lineage, for
     * searching questions. Only the current version is parsed — it is the one whose
     * structure and labels the outline shows — while the version count still reflects the
     * whole lineage.
     */
    public List<ParsedSource> resolveCurrentDescriptionTemplates() throws InvalidApplicationException {
        List<DescriptionTemplateEntity> current = this.queryFactory.query(DescriptionTemplateQuery.class)
                .disableTracking()
                .isActive(IsActive.Active)
                .versionStatuses(DescriptionTemplateVersionStatus.Current)
                .collectAs(templateFieldSet(false));
        List<DescriptionTemplateEntity> all = this.queryFactory.query(DescriptionTemplateQuery.class)
                .disableTracking()
                .isActive(IsActive.Active)
                .collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._groupId));

        Map<UUID, Integer> versionCounts = new HashMap<>();
        if (all != null) for (DescriptionTemplateEntity template : all) versionCounts.merge(template.getGroupId(), 1, Integer::sum);

        List<ParsedSource> sources = new ArrayList<>();
        if (current == null) return sources;
        for (ParsedVersion version : this.parsedTemplateVersions(current).values()) {
            ParsedSource source = assemble(List.of(version));
            source.groupId = version.groupId;
            source.versionCount = versionCounts.getOrDefault(version.groupId, 1);
            sources.add(source);
        }
        sources.sort(Comparator.comparing(x -> x.label != null ? x.label : "", String.CASE_INSENSITIVE_ORDER));
        return sources;
    }

    /**
     * The parsed versions of the given template rows, by id. Parsing a definition is the costly
     * part of a report, so a version is parsed once and kept until it changes; only the rows
     * not already cached have their definition fetched, in one query per chunk.
     */
    private Map<UUID, ParsedVersion> parsedTemplateVersions(List<DescriptionTemplateEntity> rows) throws InvalidApplicationException {
        Map<UUID, ParsedVersion> parsed = new LinkedHashMap<>();
        List<UUID> misses = new ArrayList<>();
        for (DescriptionTemplateEntity row : rows) {
            ParsedVersion cached = this.cachedVersion(row.getId(), row.getUpdatedAt(), IsActive.Active.equals(row.getIsActive()));
            if (cached != null) parsed.put(row.getId(), cached);
            else misses.add(row.getId());
        }
        for (List<UUID> chunk : ReportServiceImpl.chunked(misses)) {
            List<DescriptionTemplateEntity> templates = this.queryFactory.query(DescriptionTemplateQuery.class)
                    .disableTracking()
                    .ids(chunk)
                    .isActive(IsActive.Active, IsActive.Inactive)
                    .collectAs(templateFieldSet(true));
            if (templates == null) continue;
            for (DescriptionTemplateEntity template : templates) {
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, template.getDefinition());
                ParsedVersion version = new ParsedVersion();
                version.id = template.getId();
                version.groupId = template.getGroupId();
                version.label = template.getLabel();
                version.version = template.getVersion();
                version.active = IsActive.Active.equals(template.getIsActive());
                version.tenantId = template.getTenantId();
                indexDefinition(version, definition);
                this.cache(version, template.getUpdatedAt());
                parsed.put(version.id, version);
            }
        }
        return parsed;
    }

    /** One source over the given versions, in version order. */
    private static ParsedSource assemble(java.util.Collection<ParsedVersion> versions) {
        ParsedSource source = new ParsedSource();
        List<ParsedVersion> ordered = new ArrayList<>(versions);
        ordered.sort(Comparator.comparing(x -> x.version == null ? Short.MIN_VALUE : x.version));
        for (ParsedVersion version : ordered) source.versions.put(version.id, version);
        setLatest(source);
        return source;
    }

    private static List<ParsedVersion> carrying(java.util.Collection<ParsedVersion> versions, Set<String> semantics) {
        List<ParsedVersion> kept = new ArrayList<>();
        for (ParsedVersion version : versions) {
            if (semantics.stream().anyMatch(x -> !version.fieldIdsWithSemantic(x).isEmpty())) kept.add(version);
        }
        return kept;
    }

    private ParsedVersion cachedVersion(UUID id, Instant updatedAt, boolean active) {
        CachedVersion cached = this.parsedVersions.get(id);
        if (cached == null || !Objects.equals(cached.updatedAt(), updatedAt) || cached.version().active != active) return null;
        return cached.version();
    }

    private void cache(ParsedVersion version, Instant updatedAt) {
        // A crude bound: the cache only ever saves parsing, so starting over is always safe.
        if (this.parsedVersions.size() >= PARSED_VERSION_CACHE_LIMIT) this.parsedVersions.clear();
        this.parsedVersions.put(version.id, new CachedVersion(updatedAt, version));
    }

    private record CachedVersion(Instant updatedAt, ParsedVersion version) {
    }

    /** The newest active version names the source and numbers its questions; deleted ones only serve their assets. */
    private static void setLatest(ParsedSource source) {
        ParsedVersion latest = null;
        int active = 0;
        for (ParsedVersion version : source.versions.values()) {
            if (!version.active) continue;
            latest = version;
            active++;
        }
        source.latest = latest;
        source.label = latest != null ? latest.label : null;
        source.tenantId = latest != null ? latest.tenantId : null;
        source.versionCount = active;
    }

    private static FieldSet templateFieldSet(boolean withDefinition) {
        FieldSet fieldSet = new BaseFieldSet()
                .ensure(DescriptionTemplate._id)
                .ensure(DescriptionTemplate._label)
                .ensure(DescriptionTemplate._version)
                .ensure(DescriptionTemplate._groupId)
                .ensure(DescriptionTemplate._isActive)
                .ensure(DescriptionTemplate._updatedAt)
                // Maps to the tenant column, which the installation-wide search names.
                .ensure(DescriptionTemplate._belongsToCurrentTenant);
        if (withDefinition) fieldSet = fieldSet.ensure(DescriptionTemplate._definition);
        return fieldSet;
    }

    /**
     * Templates written before the page migration keep their questions under {@code pages}
     * rather than root sections, and installations are not obliged to have run the
     * {@code description-template/migrate-pages} action. A page occupies the same outermost
     * numbering level a root section does — which is what the migration turns it into — so
     * both shapes number identically and a report reads either one.
     */
    private static void indexDefinition(ParsedVersion version, DefinitionEntity definition) {
        List<SectionEntity> visibilitySections = new ArrayList<>();
        version.sections = visibilitySections;
        if (definition == null) return;

        if (definition.getSections() != null && !definition.getSections().isEmpty()) {
            version.rootSections = definition.getSections();
            visibilitySections.addAll(definition.getSections());
            indexSections(version, definition.getSections(), "");
            return;
        }

        if (definition.getPages() == null || definition.getPages().isEmpty()) return;

        List<PageEntity> pages = new ArrayList<>(definition.getPages());
        pages.sort(Comparator.comparingInt(PageEntity::getOrdinal));
        version.pages = pages;

        int index = 0;
        for (PageEntity page : pages) {
            index++;
            String numbering = String.valueOf(index);
            version.sectionNumbering.put(page.getId(), numbering);
            version.sectionTitle.put(page.getId(), page.getTitle());
            if (page.getSections() == null) continue;
            visibilitySections.addAll(page.getSections());
            indexSections(version, page.getSections(), numbering);
        }
    }

    /**
     * Numbering mirrors the editor's table of contents: the outermost level is numbered from
     * one, and nested sections and fieldsets each restart from one under their parent — so
     * the fieldset a template author knows as 5.2.4 is reported as 5.2.4.
     */
    private static void indexSections(ParsedVersion version, List<? extends SectionEntity> sections, String parentNumbering) {
        indexSections(version, sections, parentNumbering, List.of());
    }

    /** {@code ancestors} are the ids of the enclosing sections, outermost first. */
    private static void indexSections(ParsedVersion version, List<? extends SectionEntity> sections, String parentNumbering, List<String> ancestors) {
        if (sections == null) return;

        List<SectionEntity> ordered = new ArrayList<>(sections);
        ordered.sort(Comparator.comparingInt(SectionEntity::getOrdinal));

        int index = 0;
        for (SectionEntity section : ordered) {
            index++;
            String numbering = parentNumbering.isEmpty() ? String.valueOf(index) : parentNumbering + "." + index;
            version.sectionNumbering.put(section.getId(), numbering);
            version.sectionTitle.put(section.getId(), section.getTitle());

            List<String> chain = new ArrayList<>(ancestors);
            chain.add(section.getId());
            indexFieldSets(version, section.getFieldSets(), numbering, chain);
            indexSections(version, section.getSections(), numbering, chain);
        }
    }

    private static void indexFieldSets(ParsedVersion version, List<FieldSetEntity> fieldSets, String parentNumbering, List<String> sectionIds) {
        if (fieldSets == null) return;

        List<FieldSetEntity> ordered = new ArrayList<>(fieldSets);
        ordered.sort(Comparator.comparingInt(FieldSetEntity::getOrdinal));

        int index = 0;
        for (FieldSetEntity fieldSet : ordered) {
            index++;
            String numbering = parentNumbering.isEmpty() ? String.valueOf(index) : parentNumbering + "." + index;
            version.fieldSets.put(fieldSet.getId(), fieldSet);
            version.fieldSetNumbering.put(fieldSet.getId(), numbering);

            if (fieldSet.getFields() == null) continue;
            List<String> fieldAncestors = new ArrayList<>(sectionIds);
            fieldAncestors.add(fieldSet.getId());
            for (FieldEntity field : fieldSet.getFields()) {
                if (field.getVisibilityRules() != null) {
                    for (org.opencdmp.commons.types.descriptiontemplate.RuleEntity rule : field.getVisibilityRules()) {
                        if (rule != null && rule.getTarget() != null) version.ruleTargets.add(rule.getTarget());
                    }
                }
                if (isSystemField(field)) continue;
                version.fields.put(field.getId(), field);
                version.fieldSetOfField.put(field.getId(), fieldSet.getId());
                version.ancestorsOfField.put(field.getId(), fieldAncestors);
                if (field.getSemantics() == null) continue;
                for (String semantic : field.getSemantics()) {
                    String key = normaliseSemantic(semantic);
                    if (key == null) continue;
                    version.fieldIdsBySemantic.computeIfAbsent(key, x -> new ArrayList<>()).add(field.getId());
                }
            }
        }
    }

    //endregion

    //region plan blueprints

    /**
     * Blueprints carry their questions in the same section/fieldset/field structure as
     * templates do, so the same indexing serves both. System fields are left out: a plan's
     * title, language or access rights are columns the report already exposes as filters and
     * export columns, and offering them as questions would invite two ways of counting the
     * same thing.
     */
    public ParsedSource resolvePlanBlueprintGroup(UUID groupId) throws InvalidApplicationException {
        List<PlanBlueprintEntity> blueprints = this.queryFactory.query(PlanBlueprintQuery.class)
                .disableTracking()
                .groupIds(groupId)
                .isActive(IsActive.Active, IsActive.Inactive)
                .collectAs(blueprintFieldSet(false));

        if (blueprints == null || blueprints.stream().noneMatch(x -> IsActive.Active.equals(x.getIsActive())))
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{groupId, PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        ParsedSource source = assemble(this.parsedBlueprintVersions(blueprints).values());
        source.groupId = groupId;
        return source;
    }

    /** Every version of every blueprint lineage that carries one of the semantics, as one source. */
    public ParsedSource resolvePlanBlueprintsCarrying(Set<String> semantics) throws InvalidApplicationException {
        List<PlanBlueprintEntity> blueprints = this.queryFactory.query(PlanBlueprintQuery.class)
                .disableTracking()
                .isActive(IsActive.Active, IsActive.Inactive)
                .collectAs(blueprintFieldSet(false));
        ParsedSource source = assemble(carrying(this.parsedBlueprintVersions(blueprints != null ? blueprints : List.of()).values(), semantics));
        source.groupId = null;
        source.label = null;
        source.latest = null;
        return source;
    }

    /** The current version of each blueprint lineage, one source per lineage. */
    public List<ParsedSource> resolveCurrentPlanBlueprints() throws InvalidApplicationException {
        List<PlanBlueprintEntity> current = this.queryFactory.query(PlanBlueprintQuery.class)
                .disableTracking()
                .isActive(IsActive.Active)
                .versionStatuses(PlanBlueprintVersionStatus.Current)
                .collectAs(blueprintFieldSet(false));
        List<PlanBlueprintEntity> all = this.queryFactory.query(PlanBlueprintQuery.class)
                .disableTracking()
                .isActive(IsActive.Active)
                .collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._groupId));

        Map<UUID, Integer> versionCounts = new HashMap<>();
        if (all != null) for (PlanBlueprintEntity blueprint : all) versionCounts.merge(blueprint.getGroupId(), 1, Integer::sum);

        List<ParsedSource> sources = new ArrayList<>();
        if (current == null) return sources;
        for (ParsedVersion version : this.parsedBlueprintVersions(current).values()) {
            ParsedSource source = assemble(List.of(version));
            source.groupId = version.groupId;
            source.versionCount = versionCounts.getOrDefault(version.groupId, 1);
            sources.add(source);
        }
        sources.sort(Comparator.comparing(x -> x.label != null ? x.label : "", String.CASE_INSENSITIVE_ORDER));
        return sources;
    }

    /** The blueprint counterpart of {@link #parsedTemplateVersions}. */
    private Map<UUID, ParsedVersion> parsedBlueprintVersions(List<PlanBlueprintEntity> rows) throws InvalidApplicationException {
        Map<UUID, ParsedVersion> parsed = new LinkedHashMap<>();
        List<UUID> misses = new ArrayList<>();
        for (PlanBlueprintEntity row : rows) {
            ParsedVersion cached = this.cachedVersion(row.getId(), row.getUpdatedAt(), IsActive.Active.equals(row.getIsActive()));
            if (cached != null) parsed.put(row.getId(), cached);
            else misses.add(row.getId());
        }
        for (List<UUID> chunk : ReportServiceImpl.chunked(misses)) {
            List<PlanBlueprintEntity> blueprints = this.queryFactory.query(PlanBlueprintQuery.class)
                    .disableTracking()
                    .ids(chunk)
                    .isActive(IsActive.Active, IsActive.Inactive)
                    .collectAs(blueprintFieldSet(true));
            if (blueprints == null) continue;
            for (PlanBlueprintEntity blueprint : blueprints) {
                org.opencdmp.commons.types.planblueprint.DefinitionEntity definition =
                        this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, blueprint.getDefinition());
                ParsedVersion version = new ParsedVersion();
                version.id = blueprint.getId();
                version.groupId = blueprint.getGroupId();
                version.label = blueprint.getLabel();
                version.version = blueprint.getVersion();
                version.active = IsActive.Active.equals(blueprint.getIsActive());
                version.tenantId = blueprint.getTenantId();
                List<SectionEntity> sections = new ArrayList<>();
                if (definition != null && definition.getSections() != null) sections.addAll(definition.getSections());
                version.sections = sections;
                version.rootSections = sections;
                indexSections(version, sections, "");
                this.cache(version, blueprint.getUpdatedAt());
                parsed.put(version.id, version);
            }
        }
        return parsed;
    }

    private static FieldSet blueprintFieldSet(boolean withDefinition) {
        FieldSet fieldSet = new BaseFieldSet()
                .ensure(PlanBlueprint._id)
                .ensure(PlanBlueprint._label)
                .ensure(PlanBlueprint._version)
                .ensure(PlanBlueprint._groupId)
                .ensure(PlanBlueprint._isActive)
                .ensure(PlanBlueprint._updatedAt)
                .ensure(PlanBlueprint._belongsToCurrentTenant);
        if (withDefinition) fieldSet = fieldSet.ensure(PlanBlueprint._definition);
        return fieldSet;
    }

    //endregion

    //region outline

    public ReportOutline buildOutline(ParsedSource source, ReportSubjectType subject) {
        ReportOutline outline = new ReportOutline();
        outline.setSubject(subject);
        outline.setGroupId(source.groupId);
        outline.setLabel(source.label);
        outline.setVersionCount(source.versionCount);
        if (source.latest == null) {
            outline.setSections(new ArrayList<>());
            return outline;
        }

        outline.setLatestVersionId(source.latest.id);
        outline.setLatestVersion(source.latest.version);

        if (source.latest.pages != null) {
            List<ReportOutline.Section> roots = new ArrayList<>();
            for (PageEntity page : source.latest.pages) {
                ReportOutline.Section model = new ReportOutline.Section();
                model.setId(page.getId());
                model.setNumbering(source.latest.sectionNumbering.get(page.getId()));
                model.setTitle(page.getTitle());
                model.setOrdinal(page.getOrdinal());
                model.setSections(buildOutlineSections(source.latest, page.getSections()));
                model.setFieldSets(new ArrayList<>());
                roots.add(model);
            }
            outline.setSections(roots);
        } else {
            outline.setSections(buildOutlineSections(source.latest, source.latest.rootSections));
        }
        return outline;
    }

    private static List<ReportOutline.Section> buildOutlineSections(ParsedVersion version, List<? extends SectionEntity> sections) {
        List<ReportOutline.Section> models = new ArrayList<>();
        if (sections == null) return models;

        List<SectionEntity> ordered = new ArrayList<>(sections);
        ordered.sort(Comparator.comparingInt(SectionEntity::getOrdinal));

        for (SectionEntity section : ordered) {
            ReportOutline.Section model = new ReportOutline.Section();
            model.setId(section.getId());
            model.setNumbering(version.sectionNumbering.get(section.getId()));
            model.setTitle(section.getTitle());
            model.setOrdinal(section.getOrdinal());
            model.setSections(buildOutlineSections(version, section.getSections()));
            model.setFieldSets(buildOutlineFieldSets(version, section.getFieldSets()));
            // A section that holds nothing reportable — only system fields, say — is noise in a picker.
            if (model.getSections().isEmpty() && model.getFieldSets().isEmpty()) continue;
            models.add(model);
        }
        return models;
    }

    private static List<ReportOutline.FieldSet> buildOutlineFieldSets(ParsedVersion version, List<FieldSetEntity> fieldSets) {
        List<ReportOutline.FieldSet> models = new ArrayList<>();
        if (fieldSets == null) return models;

        List<FieldSetEntity> ordered = new ArrayList<>(fieldSets);
        ordered.sort(Comparator.comparingInt(FieldSetEntity::getOrdinal));

        for (FieldSetEntity fieldSet : ordered) {
            ReportOutline.FieldSet model = new ReportOutline.FieldSet();
            model.setId(fieldSet.getId());
            model.setNumbering(version.fieldSetNumbering.get(fieldSet.getId()));
            model.setTitle(fieldSet.getTitle());
            model.setHasMultiplicity(fieldSet.getHasMultiplicity());
            model.setFields(buildOutlineFields(fieldSet.getFields()));
            // A fieldset whose only field was a system one has nothing to report on.
            if (!model.getFields().isEmpty()) models.add(model);
        }
        return models;
    }

    /** The reportable fields of a fieldset as outline models, in display order. */
    public static List<ReportOutline.Field> buildOutlineFields(List<FieldEntity> fields) {
        List<ReportOutline.Field> models = new ArrayList<>();
        if (fields == null) return models;

        List<FieldEntity> ordered = new ArrayList<>(fields);
        ordered.sort(Comparator.comparingInt(FieldEntity::getOrdinal));
        for (FieldEntity field : ordered) {
            if (isSystemField(field)) continue;
            ReportOutline.Field model = new ReportOutline.Field();
            model.setId(field.getId());
            model.setOrdinal(field.getOrdinal());
            model.setSemantics(field.getSemantics());
            model.setControlsOtherFields(field.getVisibilityRules() != null && !field.getVisibilityRules().isEmpty());
            if (field.getData() != null) {
                model.setLabel(field.getData().getLabel());
                model.setFieldType(field.getData().getFieldType() != null ? field.getData().getFieldType().getValue() : null);
            }
            models.add(model);
        }
        return models;
    }

    /**
     * The fields of a fieldset counted by default. A yes/no beside other fields — "are there
     * ethical issues? please specify" — is the question itself, and the rest only elaborates,
     * so the yes/no alone is counted. A tick box that reveals a sibling — "can't
     * find it? type it in" — only records how the answer was entered, so it is set aside when
     * siblings carry the actual answers.
     */
    public static List<FieldEntity> countableFields(FieldSetEntity fieldSet) {
        List<FieldEntity> countable = new ArrayList<>();
        List<FieldEntity> reportable = new ArrayList<>();
        List<FieldEntity> decisions = new ArrayList<>();
        if (fieldSet.getFields() == null) return countable;

        for (FieldEntity field : fieldSet.getFields()) {
            if (isSystemField(field)) continue;
            reportable.add(field);
            if (isDecision(field)) decisions.add(field);
            else if (!isControlField(field)) countable.add(field);
        }
        List<FieldEntity> chosen = !decisions.isEmpty() ? decisions : countable.isEmpty() ? reportable : countable;
        chosen.sort(Comparator.comparingInt(FieldEntity::getOrdinal));
        return chosen;
    }

    /**
     * Semantics are free text typed by template authors, so the same tag arrives as
     * {@code rda.dataset.type}, {@code RDA.Dataset.Type } and with doubled spaces. Case and
     * whitespace do not make a different question; the normalised form is the identity.
     */
    public static String normaliseSemantic(String semantic) {
        if (semantic == null) return null;
        String key = semantic.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return key.isEmpty() ? null : key;
    }

    private static boolean isControlField(FieldEntity field) {
        if (field.getVisibilityRules() == null || field.getVisibilityRules().isEmpty()) return false;
        return field.getData() != null && FieldType.CHECK_BOX.equals(field.getData().getFieldType());
    }

    private static boolean isDecision(FieldEntity field) {
        return field.getData() != null && FieldType.BOOLEAN_DECISION.equals(field.getData().getFieldType());
    }

    private static boolean isSystemField(FieldEntity field) {
        return field.getData() != null && field.getData().getFieldType() != null && FieldType.isSystemType(field.getData().getFieldType());
    }

    //endregion

    /** Every parsed version of one lineage — or of all lineages, when {@code groupId} is null. */
    public static class ParsedSource {

        private UUID groupId;
        private String label;
        private UUID tenantId;
        private int versionCount;
        private ParsedVersion latest;
        private final Map<UUID, ParsedVersion> versions = new LinkedHashMap<>();

        public UUID getGroupId() { return this.groupId; }

        public String getLabel() { return this.label; }

        /** The tenant owning the lineage; null for the default tenant's, which every tenant shares. */
        public UUID getTenantId() { return this.tenantId; }

        /** Versions in the lineage, including ones not parsed here (see the search resolvers). */
        public int getVersionCount() { return this.versionCount; }

        public ParsedVersion getLatest() { return this.latest; }

        public Map<UUID, ParsedVersion> getVersions() { return this.versions; }

        /** True when the source spans every lineage rather than one. */
        public boolean spansAllGroups() { return this.groupId == null; }

        /** True when no version of the lineage carries the field. */
        public boolean isUnknownField(String fieldId) {
            return this.versions.values().stream().noneMatch(x -> x.fields.containsKey(fieldId));
        }

        public boolean isUnknownFieldSet(String fieldSetId) {
            return this.versions.values().stream().noneMatch(x -> x.fieldSets.containsKey(fieldSetId));
        }

        public boolean isUnknownSemantic(String semantic) {
            String key = normaliseSemantic(semantic);
            return key == null || this.versions.values().stream().noneMatch(x -> x.fieldIdsBySemantic.containsKey(key));
        }
    }

    /** One parsed version, indexed by field, fieldset and semantic. */
    public static class ParsedVersion {

        private UUID id;
        private UUID groupId;
        private String label;
        private Short version;
        private boolean active;
        private UUID tenantId;
        /** Flattened sections, for visibility evaluation: pages carry no rules of their own. */
        private List<SectionEntity> sections;
        private List<? extends SectionEntity> rootSections;
        private List<PageEntity> pages;
        private final Map<String, FieldSetEntity> fieldSets = new LinkedHashMap<>();
        private final Map<String, FieldEntity> fields = new LinkedHashMap<>();
        private final Map<String, String> fieldSetOfField = new LinkedHashMap<>();
        private final Map<String, String> fieldSetNumbering = new LinkedHashMap<>();
        private final Map<String, String> sectionNumbering = new LinkedHashMap<>();
        private final Map<String, String> sectionTitle = new LinkedHashMap<>();
        private final Map<String, List<String>> fieldIdsBySemantic = new LinkedHashMap<>();
        private final Set<String> ruleTargets = new java.util.HashSet<>();
        private final Map<String, List<String>> ancestorsOfField = new HashMap<>();

        public UUID getId() { return this.id; }

        public UUID getGroupId() { return this.groupId; }

        public String getLabel() { return this.label; }

        public Short getVersion() { return this.version; }

        public List<SectionEntity> getSections() { return this.sections; }

        /** Fieldsets in table-of-contents order. */
        public Map<String, FieldSetEntity> getFieldSets() { return this.fieldSets; }

        public Map<String, List<String>> getFieldIdsBySemantic() { return this.fieldIdsBySemantic; }

        public FieldSetEntity fieldSet(String fieldSetId) { return this.fieldSets.get(fieldSetId); }

        public FieldEntity field(String fieldId) { return this.fields.get(fieldId); }

        public String fieldSetOf(String fieldId) { return this.fieldSetOfField.get(fieldId); }

        /**
         * Whether a visibility rule can hide the field: only a rule targeting the field, its
         * fieldset or an enclosing section does. Every other field is always visible, so its
         * answers need no evaluation of the rules.
         */
        public boolean canBeHidden(String fieldId) {
            if (this.ruleTargets.isEmpty()) return false;
            if (this.ruleTargets.contains(fieldId)) return true;
            List<String> ancestors = this.ancestorsOfField.get(fieldId);
            if (ancestors == null) return true;
            for (String ancestor : ancestors) if (this.ruleTargets.contains(ancestor)) return true;
            return false;
        }

        public List<String> fieldIdsWithSemantic(String semantic) {
            String key = normaliseSemantic(semantic);
            return key == null ? List.of() : this.fieldIdsBySemantic.getOrDefault(key, List.of());
        }

        public String fieldSetNumbering(String fieldSetId) { return this.fieldSetNumbering.get(fieldSetId); }

        public String fieldSetTitle(String fieldSetId) {
            FieldSetEntity fieldSet = this.fieldSets.get(fieldSetId);
            return fieldSet != null ? fieldSet.getTitle() : null;
        }
    }
}
