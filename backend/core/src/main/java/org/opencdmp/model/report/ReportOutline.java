package org.opencdmp.model.report;

import org.opencdmp.commons.enums.ReportSubjectType;

import java.util.List;
import java.util.UUID;

/**
 * The question tree of one template or blueprint lineage, numbered the way the editor's
 * table of contents numbers it (root section . nested section . fieldset), so that a
 * report refers to a question by the number its authors and reviewers already use.
 *
 * <p>Structure and labels come from the newest version of the lineage; the field ids are
 * stable across versions, so a question selected here also addresses older versions.</p>
 */
public class ReportOutline {

    private ReportSubjectType subject;
    private UUID groupId;
    private UUID latestVersionId;
    private String label;
    private Short latestVersion;
    private int versionCount;
    private List<Section> sections;

    public ReportSubjectType getSubject() { return this.subject; }

    public void setSubject(ReportSubjectType subject) { this.subject = subject; }

    public UUID getGroupId() { return this.groupId; }

    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public UUID getLatestVersionId() { return this.latestVersionId; }

    public void setLatestVersionId(UUID latestVersionId) { this.latestVersionId = latestVersionId; }

    public String getLabel() { return this.label; }

    public void setLabel(String label) { this.label = label; }

    public Short getLatestVersion() { return this.latestVersion; }

    public void setLatestVersion(Short latestVersion) { this.latestVersion = latestVersion; }

    public int getVersionCount() { return this.versionCount; }

    public void setVersionCount(int versionCount) { this.versionCount = versionCount; }

    public List<Section> getSections() { return this.sections; }

    public void setSections(List<Section> sections) { this.sections = sections; }

    public static class Section {

        private String id;
        private String numbering;
        private String title;
        private int ordinal;
        private List<Section> sections;
        private List<FieldSet> fieldSets;

        public String getId() { return this.id; }

        public void setId(String id) { this.id = id; }

        public String getNumbering() { return this.numbering; }

        public void setNumbering(String numbering) { this.numbering = numbering; }

        public String getTitle() { return this.title; }

        public void setTitle(String title) { this.title = title; }

        public int getOrdinal() { return this.ordinal; }

        public void setOrdinal(int ordinal) { this.ordinal = ordinal; }

        public List<Section> getSections() { return this.sections; }

        public void setSections(List<Section> sections) { this.sections = sections; }

        public List<FieldSet> getFieldSets() { return this.fieldSets; }

        public void setFieldSets(List<FieldSet> fieldSets) { this.fieldSets = fieldSets; }
    }

    public static class FieldSet {

        private String id;
        private String numbering;
        private String title;
        private boolean hasMultiplicity;
        private List<Field> fields;

        public String getId() { return this.id; }

        public void setId(String id) { this.id = id; }

        public String getNumbering() { return this.numbering; }

        public void setNumbering(String numbering) { this.numbering = numbering; }

        public String getTitle() { return this.title; }

        public void setTitle(String title) { this.title = title; }

        public boolean getHasMultiplicity() { return this.hasMultiplicity; }

        public void setHasMultiplicity(boolean hasMultiplicity) { this.hasMultiplicity = hasMultiplicity; }

        public List<Field> getFields() { return this.fields; }

        public void setFields(List<Field> fields) { this.fields = fields; }
    }

    public static class Field {

        private String id;
        private String label;
        private String fieldType;
        private int ordinal;
        private List<String> semantics;
        private boolean controlsOtherFields;

        public String getId() { return this.id; }

        public void setId(String id) { this.id = id; }

        public String getLabel() { return this.label; }

        public void setLabel(String label) { this.label = label; }

        public String getFieldType() { return this.fieldType; }

        public void setFieldType(String fieldType) { this.fieldType = fieldType; }

        public int getOrdinal() { return this.ordinal; }

        public void setOrdinal(int ordinal) { this.ordinal = ordinal; }

        public List<String> getSemantics() { return this.semantics; }

        public void setSemantics(List<String> semantics) { this.semantics = semantics; }

        /**
         * True when this field's own value decides whether sibling fields are shown. Such a
         * field is a control rather than an answer — a "can't find it? type it in" tick box —
         * so counting it alongside its siblings mixes Yes and No into their values.
         */
        public boolean getControlsOtherFields() { return this.controlsOtherFields; }

        public void setControlsOtherFields(boolean controlsOtherFields) { this.controlsOtherFields = controlsOtherFields; }
    }
}
