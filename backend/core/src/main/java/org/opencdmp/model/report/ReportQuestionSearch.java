package org.opencdmp.model.report;

import org.opencdmp.commons.enums.ReportSubjectType;

import java.util.List;
import java.util.UUID;

/**
 * The questions matching a search, in two kinds.
 *
 * <p>A <em>question</em> hit is one fieldset of one template or blueprint lineage — what a
 * template author calls 5.2.4. A <em>semantic</em> hit is a tag several templates share, so
 * a report on it spans every template at once; it is what a funder means by "where will the
 * data be deposited" when the tenant runs a dozen templates.</p>
 */
public class ReportQuestionSearch {

    private List<QuestionHit> questions;
    private List<SemanticHit> semantics;
    /** Only when asked for: the lineages themselves, for browsing them across tenants. */
    private List<SourceHit> sources;

    public List<QuestionHit> getQuestions() { return this.questions; }

    public void setQuestions(List<QuestionHit> questions) { this.questions = questions; }

    public List<SemanticHit> getSemantics() { return this.semantics; }

    public void setSemantics(List<SemanticHit> semantics) { this.semantics = semantics; }

    public List<SourceHit> getSources() { return this.sources; }

    public void setSources(List<SourceHit> sources) { this.sources = sources; }

    /** One asset template or plan blueprint lineage, by its current version. */
    public static class SourceHit {

        private ReportSubjectType subject;
        private UUID groupId;
        private String label;
        /** Set only on an installation-wide search. */
        private String tenantCode;
        private int versionCount;

        public ReportSubjectType getSubject() { return this.subject; }

        public void setSubject(ReportSubjectType subject) { this.subject = subject; }

        public UUID getGroupId() { return this.groupId; }

        public void setGroupId(UUID groupId) { this.groupId = groupId; }

        public String getLabel() { return this.label; }

        public void setLabel(String label) { this.label = label; }

        public String getTenantCode() { return this.tenantCode; }

        public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }

        public int getVersionCount() { return this.versionCount; }

        public void setVersionCount(int versionCount) { this.versionCount = versionCount; }
    }

    public static class QuestionHit {

        private ReportSubjectType subject;
        private UUID groupId;
        private String groupLabel;
        /** The tenant owning the lineage; set only on an installation-wide search. */
        private String tenantCode;
        private int versionCount;
        private String fieldSetId;
        private String numbering;
        private String title;
        private boolean hasMultiplicity;
        /** The fields that carry answers — controls that only reveal siblings are left out. */
        private List<ReportOutline.Field> fields;
        /** Every field of the fieldset, for the advanced "fields counted" view. */
        private List<ReportOutline.Field> allFields;

        public ReportSubjectType getSubject() { return this.subject; }

        public void setSubject(ReportSubjectType subject) { this.subject = subject; }

        public UUID getGroupId() { return this.groupId; }

        public void setGroupId(UUID groupId) { this.groupId = groupId; }

        public String getTenantCode() { return this.tenantCode; }

        public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }

        public String getGroupLabel() { return this.groupLabel; }

        public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }

        public int getVersionCount() { return this.versionCount; }

        public void setVersionCount(int versionCount) { this.versionCount = versionCount; }

        public String getFieldSetId() { return this.fieldSetId; }

        public void setFieldSetId(String fieldSetId) { this.fieldSetId = fieldSetId; }

        public String getNumbering() { return this.numbering; }

        public void setNumbering(String numbering) { this.numbering = numbering; }

        public String getTitle() { return this.title; }

        public void setTitle(String title) { this.title = title; }

        public boolean getHasMultiplicity() { return this.hasMultiplicity; }

        public void setHasMultiplicity(boolean hasMultiplicity) { this.hasMultiplicity = hasMultiplicity; }

        public List<ReportOutline.Field> getFields() { return this.fields; }

        public void setFields(List<ReportOutline.Field> fields) { this.fields = fields; }

        public List<ReportOutline.Field> getAllFields() { return this.allFields; }

        public void setAllFields(List<ReportOutline.Field> allFields) { this.allFields = allFields; }
    }

    public static class SemanticHit {

        private ReportSubjectType subject;
        private String semantic;
        /** The fieldset title most templates give the question — how templates word it, not its name. */
        private String label;
        /** Every distinct wording, most common first; several wordings hint at loose tagging. */
        private List<String> titles;
        private int groupCount;
        private List<String> groupLabels;
        private List<String> fieldTypes;

        public ReportSubjectType getSubject() { return this.subject; }

        public void setSubject(ReportSubjectType subject) { this.subject = subject; }

        public String getSemantic() { return this.semantic; }

        public void setSemantic(String semantic) { this.semantic = semantic; }

        public String getLabel() { return this.label; }

        public void setLabel(String label) { this.label = label; }

        public List<String> getTitles() { return this.titles; }

        public void setTitles(List<String> titles) { this.titles = titles; }

        public int getGroupCount() { return this.groupCount; }

        public void setGroupCount(int groupCount) { this.groupCount = groupCount; }

        public List<String> getGroupLabels() { return this.groupLabels; }

        public void setGroupLabels(List<String> groupLabels) { this.groupLabels = groupLabels; }

        public List<String> getFieldTypes() { return this.fieldTypes; }

        public void setFieldTypes(List<String> fieldTypes) { this.fieldTypes = fieldTypes; }
    }
}
