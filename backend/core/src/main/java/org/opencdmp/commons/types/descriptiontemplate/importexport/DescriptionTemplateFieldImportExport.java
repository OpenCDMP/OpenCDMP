package org.opencdmp.commons.types.descriptiontemplate.importexport;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionTemplateFieldImportExport {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "ordinal")
    private int ordinal;

    @XmlElementWrapper(name = "validations")
    @XmlElement(name = "validation")
    private List<FieldValidationType> validations;

    @XmlElement(name = "defaultValue")
    private DescriptionTemplateDefaultValueImportExport defaultValue;

    @XmlElementWrapper(name = "visibilityRules")
    @XmlElement(name = "visibilityRule")
    private List<DescriptionTemplateRuleImportExport> visibilityRules;

    @XmlElement(name = "fieldType")
    private FieldType fieldType;

    @XmlElements({
            @XmlElement(name = LabelDataImportExport.XmlElementName, type = LabelDataImportExport.class),
            @XmlElement(name = LabelAndMultiplicityDataImportExport.XmlElementName, type = LabelAndMultiplicityDataImportExport.class),
            @XmlElement(name = UploadDataImportExport.XmlElementName, type = UploadDataImportExport.class),
            @XmlElement(name = RadioBoxDataImportExport.XmlElementName, type = RadioBoxDataImportExport.class),
            @XmlElement(name = SelectDataImportExport.XmlElementName, type = SelectDataImportExport.class),
            @XmlElement(name = ReferenceTypeDataImportExport.XmlElementName, type = ReferenceTypeDataImportExport.class),
    })
    private BaseFieldDataImportExport data;

    @XmlElementWrapper(name = "semantics")
    @XmlElement(name = "semantic")
    private List<String> semantics;

    @XmlElement(name="includeInExport")
    private Boolean includeInExport;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public DescriptionTemplateDefaultValueImportExport getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(DescriptionTemplateDefaultValueImportExport defaultValue) {
        this.defaultValue = defaultValue;
    }

    public FieldType getFieldType() {
        return this.fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public BaseFieldDataImportExport getData() {
        return this.data;
    }

    public void setData(BaseFieldDataImportExport data) {
        this.data = data;
    }

    public List<FieldValidationType> getValidations() {
        return this.validations;
    }

    public void setValidations(List<FieldValidationType> validations) {
        this.validations = validations;
    }

    public List<String> getSemantics() {
        return this.semantics;
    }

    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }

    public List<DescriptionTemplateRuleImportExport> getVisibilityRules() {
        return this.visibilityRules;
    }

    public void setVisibilityRules(List<DescriptionTemplateRuleImportExport> visibilityRules) {
        this.visibilityRules = visibilityRules;
    }

    public Boolean getIncludeInExport() {
        return this.includeInExport;
    }

    public void setIncludeInExport(Boolean includeInExport) {
        this.includeInExport = includeInExport;
    }
}
