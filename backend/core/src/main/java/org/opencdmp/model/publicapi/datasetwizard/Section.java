package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.commons.types.descriptiontemplate.SectionEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Section implements Comparable<Object>, PropertiesModelBuilder {
    private List<Section> sections;
    private List<FieldSet> compositeFields;
    private Boolean defaultVisibility;
    private String numbering;
    private String page;
    private Integer ordinal;
    private String id;
    private String title;
    private String description;
    private Boolean multiplicity;

    public List<Section> getSections() {
        Collections.sort(this.sections);
        return this.sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public List<FieldSet> getCompositeFields() {
        Collections.sort(this.compositeFields);
        return this.compositeFields;
    }

    public void setCompositeFields(List<FieldSet> compositeFields) {
        this.compositeFields = compositeFields;
    }

    public Boolean getDefaultVisibility() {
        return this.defaultVisibility;
    }

    public void setDefaultVisibility(Boolean defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    public String getPage() {
        return this.page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getNumbering() {
        return this.numbering;
    }

    public void setNumbering(String numbering) {
        this.numbering = numbering;
    }

    public Boolean getMultiplicity() {
        return this.multiplicity;
    }

    public void setMultiplicity(Boolean multiplicity) {
        this.multiplicity = multiplicity;
    }

    public SectionEntity toDatabaseDefinition(SectionEntity item) {
//        item.setDefaultVisibility(this.defaultVisibility);
//        item.setDescription(this.description);
//        if (this.compositeFields != null)
//            item.setFieldSets(new ModelBuilder().toViewStyleDefinition(this.compositeFields, FieldSetEntity.class));
//        item.setId(this.id);
//        item.setOrdinal(this.ordinal);
//        item.setPage(this.page);
//        if (this.sections != null)
//            item.setSections(new ModelBuilder().toViewStyleDefinition(this.sections, SectionEntity.class));
//        item.setTitle(this.title);
//        item.setMultiplicity(this.multiplicity);
//        return item;
        return null;
    }

    public void fromDatabaseDefinition(SectionEntity item) {
//        this.defaultVisibility = item.isDefaultVisibility();
//        this.description = item.getDescription();
//        this.compositeFields = new ModelBuilder().fromViewStyleDefinition(item.getFieldSets(), FieldSet.class);
//        this.id = item.getId();
//        this.ordinal = item.getOrdinal();
//        this.numbering = item.getNumbering();
//        this.page = item.getPage();
//        this.sections = new ModelBuilder().fromViewStyleDefinition(item.getSections(), Section.class);
//        this.title = item.getTitle();
//        this.multiplicity = item.getMultiplicity();
    }

    @Override
    public void fromJsonObject(Map<String, Object> properties) {
        this.sections.forEach(item -> item.fromJsonObject(properties));
        this.compositeFields.forEach(item -> item.fromJsonObject(properties));
    }

    @Override
    public int compareTo(Object o) {
        return this.ordinal.compareTo(((Section) o).getOrdinal());
    }

    @Override
    public void fromJsonObject(Map<String, Object> properties, String index) {
        // TODO Auto-generated method stub

    }

    public void toMap(Map<String, Object> fieldValues) {
        this.sections.forEach(item -> item.toMap(fieldValues));
        this.compositeFields.forEach(item -> item.toMap(fieldValues));
    }

    public void toMap(Map<String, Object> fieldValues, int index) {

    }
}
