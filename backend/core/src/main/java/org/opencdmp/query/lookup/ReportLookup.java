package org.opencdmp.query.lookup;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opencdmp.commons.enums.ReportDateBucket;
import org.opencdmp.commons.enums.ReportDimensionType;
import org.opencdmp.commons.enums.ReportSubjectType;

import java.util.List;
import java.util.UUID;

/**
 * An answer-report request: which subjects to count, which questions of their template or
 * blueprint lineage to read, and how to break the counts down.
 *
 * <p>The structural filters are the listing lookups themselves ({@link DescriptionLookup},
 * {@link PlanLookup}), so anything the asset or plan listing can filter by is available
 * here without a second filter vocabulary. Their paging and ordering are ignored — a
 * report always reads the whole matching set, capped by <code>report.maxSubjects</code>.</p>
 */
public class ReportLookup {

    @Schema(description = "The unit to count: assets (descriptions) or plans")
    private ReportSubjectType subject;

    @Schema(description = "Asset template group whose questions are reported on. Required when subject is Description and a question names a fieldset; may be left out when every question is a semantic, in which case every asset template is read")
    private UUID descriptionTemplateGroupId;

    @Schema(description = "Plan blueprint group whose questions are reported on. Required when subject is Plan and a question names a fieldset; may be left out when every question is a semantic, in which case every blueprint is read")
    private UUID planBlueprintGroupId;

    @Schema(description = "The questions to report on. At least one is required")
    private List<Question> questions;

    @Schema(description = "Questions of the parent plan's blueprint, available as breakdowns and as export columns when counting assets. Requires planBlueprintGroupId. Their keys are p0, p1, ... in request order")
    private List<Question> planQuestions;

    @Schema(description = "Dimensions to break the counts down by, in the order they should nest")
    private List<Dimension> groupBy;

    @Schema(description = "Asset filters, reusing the asset listing lookup")
    private DescriptionLookup descriptionSubQuery;

    @Schema(description = "Plan filters, reusing the plan listing lookup. Applied to the parent plan when counting assets")
    private PlanLookup planSubQuery;

    @Schema(description = "Count only the current version of each plan lineage. Defaults to true")
    private Boolean latestVersionOnly;

    @Schema(description = "Include deleted subjects. Defaults to false")
    private Boolean includeInactive;

    @Schema(description = "Granularity for date answers and date dimensions. Defaults to Year")
    private ReportDateBucket dateBucket;

    @Schema(description = "How many distinct free-text answers to report before grouping the rest as other text. Defaults to 20")
    private Integer textTopN;

    @Schema(description = "Count across every tenant of the installation instead of the current one. Requires the installation report permissions")
    private Boolean installationWide;

    public ReportSubjectType getSubject() { return this.subject; }

    public void setSubject(ReportSubjectType subject) { this.subject = subject; }

    public UUID getDescriptionTemplateGroupId() { return this.descriptionTemplateGroupId; }

    public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) { this.descriptionTemplateGroupId = descriptionTemplateGroupId; }

    public UUID getPlanBlueprintGroupId() { return this.planBlueprintGroupId; }

    public void setPlanBlueprintGroupId(UUID planBlueprintGroupId) { this.planBlueprintGroupId = planBlueprintGroupId; }

    public List<Question> getQuestions() { return this.questions; }

    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public List<Question> getPlanQuestions() { return this.planQuestions; }

    public void setPlanQuestions(List<Question> planQuestions) { this.planQuestions = planQuestions; }

    public List<Dimension> getGroupBy() { return this.groupBy; }

    public void setGroupBy(List<Dimension> groupBy) { this.groupBy = groupBy; }

    public DescriptionLookup getDescriptionSubQuery() { return this.descriptionSubQuery; }

    public void setDescriptionSubQuery(DescriptionLookup descriptionSubQuery) { this.descriptionSubQuery = descriptionSubQuery; }

    public PlanLookup getPlanSubQuery() { return this.planSubQuery; }

    public void setPlanSubQuery(PlanLookup planSubQuery) { this.planSubQuery = planSubQuery; }

    public Boolean getLatestVersionOnly() { return this.latestVersionOnly; }

    public void setLatestVersionOnly(Boolean latestVersionOnly) { this.latestVersionOnly = latestVersionOnly; }

    public Boolean getIncludeInactive() { return this.includeInactive; }

    public void setIncludeInactive(Boolean includeInactive) { this.includeInactive = includeInactive; }

    public ReportDateBucket getDateBucket() { return this.dateBucket; }

    public void setDateBucket(ReportDateBucket dateBucket) { this.dateBucket = dateBucket; }

    public Integer getTextTopN() { return this.textTopN; }

    public void setTextTopN(Integer textTopN) { this.textTopN = textTopN; }

    public Boolean getInstallationWide() { return this.installationWide; }

    public void setInstallationWide(Boolean installationWide) { this.installationWide = installationWide; }

    /**
     * One logical question, addressed in one of two ways.
     *
     * <p>By <em>fieldset and fields</em>: the fields of a single fieldset whose values should
     * be counted together. Selecting a reference field together with its free-text "insert
     * manually" sibling reports both as one answer, which is how such pairs are authored.</p>
     *
     * <p>By <em>semantic</em>: every field, in every template or blueprint version in scope,
     * that a template author tagged with the given semantic ({@code
     * rda.dataset.distribution.host.title}, say). Field ids differ from one template to the
     * next, so this is the only way one question can span templates.</p>
     */
    public static class Question {

        @Schema(description = "Id of the fieldset the fields belong to. Leave out when addressing the question by semantic")
        private String fieldSetId;

        @Schema(description = "Ids of the fields whose values are counted together. Leave out when addressing the question by semantic")
        private List<String> fieldIds;

        @Schema(description = "Semantic tag the fields carry, e.g. rda.dataset.distribution.host.title. Matches across every template version in scope")
        private String semantic;

        @Schema(description = "Optional display label. Defaults to the numbering and title of the fieldset")
        private String label;

        public String getFieldSetId() { return this.fieldSetId; }

        public void setFieldSetId(String fieldSetId) { this.fieldSetId = fieldSetId; }

        public List<String> getFieldIds() { return this.fieldIds; }

        public void setFieldIds(List<String> fieldIds) { this.fieldIds = fieldIds; }

        public String getSemantic() { return this.semantic; }

        public void setSemantic(String semantic) { this.semantic = semantic; }

        public String getLabel() { return this.label; }

        public void setLabel(String label) { this.label = label; }
    }

    /** A group-by key. {@code questionKey} names the question when the type is {@code Question}. */
    public static class Dimension {

        @Schema(description = "What to group by")
        private ReportDimensionType type;

        @Schema(description = "Key of the question to group by, when type is Question. Keys are q0, q1, ... in request order")
        private String questionKey;

        public ReportDimensionType getType() { return this.type; }

        public void setType(ReportDimensionType type) { this.type = type; }

        public String getQuestionKey() { return this.questionKey; }

        public void setQuestionKey(String questionKey) { this.questionKey = questionKey; }
    }
}
