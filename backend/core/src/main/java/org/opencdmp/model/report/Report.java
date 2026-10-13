package org.opencdmp.model.report;

import org.opencdmp.commons.enums.ReportDimensionType;
import org.opencdmp.commons.enums.ReportSubjectType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The result of an answer report: how many subjects were counted, and for each selected
 * question the distribution of its answers, optionally broken down by dimensions.
 *
 * <p>Answer states are per question rather than per report, because two questions of the
 * same request can disagree about whether a subject answered. The report-level counters
 * only carry what is unambiguous: how many subjects and plans were in scope.</p>
 */
public class Report {

    private ReportSubjectType subject;
    private UUID sourceGroupId;
    private String sourceLabel;
    private int sourceVersionCount;
    /** True when the report read every template or blueprint of the tenant rather than one lineage. */
    private boolean sourceSpansAllGroups;
    private Instant generatedAt;
    private long subjects;
    private long distinctPlans;
    private List<Dimension> dimensions;
    private List<QuestionResult> questions;
    private List<CrossTabRow> crossTab;

    public ReportSubjectType getSubject() { return this.subject; }

    public void setSubject(ReportSubjectType subject) { this.subject = subject; }

    public UUID getSourceGroupId() { return this.sourceGroupId; }

    public void setSourceGroupId(UUID sourceGroupId) { this.sourceGroupId = sourceGroupId; }

    public String getSourceLabel() { return this.sourceLabel; }

    public void setSourceLabel(String sourceLabel) { this.sourceLabel = sourceLabel; }

    public int getSourceVersionCount() { return this.sourceVersionCount; }

    public void setSourceVersionCount(int sourceVersionCount) { this.sourceVersionCount = sourceVersionCount; }

    public boolean getSourceSpansAllGroups() { return this.sourceSpansAllGroups; }

    public void setSourceSpansAllGroups(boolean sourceSpansAllGroups) { this.sourceSpansAllGroups = sourceSpansAllGroups; }

    public Instant getGeneratedAt() { return this.generatedAt; }

    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }

    public long getSubjects() { return this.subjects; }

    public void setSubjects(long subjects) { this.subjects = subjects; }

    public long getDistinctPlans() { return this.distinctPlans; }

    public void setDistinctPlans(long distinctPlans) { this.distinctPlans = distinctPlans; }

    public List<Dimension> getDimensions() { return this.dimensions; }

    public void setDimensions(List<Dimension> dimensions) { this.dimensions = dimensions; }

    public List<QuestionResult> getQuestions() { return this.questions; }

    public void setQuestions(List<QuestionResult> questions) { this.questions = questions; }

    public List<CrossTabRow> getCrossTab() { return this.crossTab; }

    public void setCrossTab(List<CrossTabRow> crossTab) { this.crossTab = crossTab; }

    /** A group-by key that was applied, in the order its values appear in a cross-tab row. */
    public static class Dimension {

        private ReportDimensionType type;
        private String label;
        private String questionKey;

        public ReportDimensionType getType() { return this.type; }

        public void setType(ReportDimensionType type) { this.type = type; }

        public String getLabel() { return this.label; }

        public void setLabel(String label) { this.label = label; }

        public String getQuestionKey() { return this.questionKey; }

        public void setQuestionKey(String questionKey) { this.questionKey = questionKey; }
    }

    /** How many subjects answered a question, skipped it, or were never asked it. */
    public static class AnswerStates {

        private long answered;
        private long notAnswered;
        private long notApplicable;
        private long notMigrated;

        public long getAnswered() { return this.answered; }

        public void setAnswered(long answered) { this.answered = answered; }

        public long getNotAnswered() { return this.notAnswered; }

        public void setNotAnswered(long notAnswered) { this.notAnswered = notAnswered; }

        public long getNotApplicable() { return this.notApplicable; }

        public void setNotApplicable(long notApplicable) { this.notApplicable = notApplicable; }

        /** Subjects whose answers are still in the pre-1.4 shape and cannot be read. */
        public long getNotMigrated() { return this.notMigrated; }

        public void setNotMigrated(long notMigrated) { this.notMigrated = notMigrated; }
    }

    /** One answer value and how many subjects gave it. */
    public static class DistributionEntry {

        private String value;
        private long count;
        private double percentOfAnswered;

        public String getValue() { return this.value; }

        public void setValue(String value) { this.value = value; }

        public long getCount() { return this.count; }

        public void setCount(long count) { this.count = count; }

        public double getPercentOfAnswered() { return this.percentOfAnswered; }

        public void setPercentOfAnswered(double percentOfAnswered) { this.percentOfAnswered = percentOfAnswered; }
    }

    /** Aggregates for a numeric question, alongside its bucketed distribution. */
    public static class NumberSummary {

        private double sum;
        private double average;
        private double min;
        private double max;
        private long count;

        public double getSum() { return this.sum; }

        public void setSum(double sum) { this.sum = sum; }

        public double getAverage() { return this.average; }

        public void setAverage(double average) { this.average = average; }

        public double getMin() { return this.min; }

        public void setMin(double min) { this.min = min; }

        public double getMax() { return this.max; }

        public void setMax(double max) { this.max = max; }

        public long getCount() { return this.count; }

        public void setCount(long count) { this.count = count; }
    }

    /** The distribution of one selected question over the whole subject set. */
    public static class QuestionResult {

        private String key;
        private String label;
        private String numbering;
        private String fieldType;
        private boolean multiValue;
        private AnswerStates states;
        private List<DistributionEntry> distribution;
        private NumberSummary numberSummary;

        public String getKey() { return this.key; }

        public void setKey(String key) { this.key = key; }

        public String getLabel() { return this.label; }

        public void setLabel(String label) { this.label = label; }

        public String getNumbering() { return this.numbering; }

        public void setNumbering(String numbering) { this.numbering = numbering; }

        public String getFieldType() { return this.fieldType; }

        public void setFieldType(String fieldType) { this.fieldType = fieldType; }

        /** True when one subject can name several values, so the distribution sums above the answered count. */
        public boolean getMultiValue() { return this.multiValue; }

        public void setMultiValue(boolean multiValue) { this.multiValue = multiValue; }

        public AnswerStates getStates() { return this.states; }

        public void setStates(AnswerStates states) { this.states = states; }

        public List<DistributionEntry> getDistribution() { return this.distribution; }

        public void setDistribution(List<DistributionEntry> distribution) { this.distribution = distribution; }

        public NumberSummary getNumberSummary() { return this.numberSummary; }

        public void setNumberSummary(NumberSummary numberSummary) { this.numberSummary = numberSummary; }
    }

    /** One question's numbers inside a single cross-tab row. */
    public static class QuestionCell {

        private String key;
        private AnswerStates states;
        private List<DistributionEntry> distribution;

        public String getKey() { return this.key; }

        public void setKey(String key) { this.key = key; }

        public AnswerStates getStates() { return this.states; }

        public void setStates(AnswerStates states) { this.states = states; }

        public List<DistributionEntry> getDistribution() { return this.distribution; }

        public void setDistribution(List<DistributionEntry> distribution) { this.distribution = distribution; }
    }

    /** One combination of dimension values and the numbers of the subjects in it. */
    public static class CrossTabRow {

        private List<String> dimensionValues;
        private long subjects;
        private long distinctPlans;
        private List<QuestionCell> questions;

        public List<String> getDimensionValues() { return this.dimensionValues; }

        public void setDimensionValues(List<String> dimensionValues) { this.dimensionValues = dimensionValues; }

        public long getSubjects() { return this.subjects; }

        public void setSubjects(long subjects) { this.subjects = subjects; }

        public long getDistinctPlans() { return this.distinctPlans; }

        public void setDistinctPlans(long distinctPlans) { this.distinctPlans = distinctPlans; }

        public List<QuestionCell> getQuestions() { return this.questions; }

        public void setQuestions(List<QuestionCell> questions) { this.questions = questions; }
    }
}
