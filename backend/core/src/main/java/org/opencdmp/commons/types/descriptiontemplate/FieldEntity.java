package org.opencdmp.commons.types.descriptiontemplate;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class FieldEntity {
    @XmlAttribute(name="id")
    private String id;
    @XmlAttribute(name="ordinal")
    private int ordinal;
    @XmlElementWrapper(name = "semantics")
    @XmlElement(name = "semantic")
    private List<String> semantics;
    @XmlElement(name="defaultValue")
    private DefaultValueEntity defaultValue;
    @XmlElementWrapper(name = "visibilityRules")
    @XmlElement(name = "rule")
    private List<RuleEntity> visibilityRules;

    @XmlElements({
            @XmlElement(name = LabelDataEntity.XmlElementName, type = LabelDataEntity.class),
            @XmlElement(name = LabelAndMultiplicityDataEntity.XmlElementName, type = LabelAndMultiplicityDataEntity.class),
            @XmlElement(name = UploadDataEntity.XmlElementName, type = UploadDataEntity.class),
            @XmlElement(name = RadioBoxDataEntity.XmlElementName, type = RadioBoxDataEntity.class),
            @XmlElement(name = SelectDataEntity.XmlElementName, type = SelectDataEntity.class),
            @XmlElement(name = ReferenceTypeDataEntity.XmlElementName, type = ReferenceTypeDataEntity.class)
    })
    private BaseFieldDataEntity data;
    @XmlElementWrapper(name = "validations")
    @XmlElement(name = "validation")
    private List<FieldValidationType> validations;
    @XmlAttribute(name="includeInExport")
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

    public List<String> getSemantics() {
        return this.semantics;
    }
    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }

    public BaseFieldDataEntity getData() {
        return this.data;
    }
    public void setData(BaseFieldDataEntity data) {
        this.data = data;
    }

    public DefaultValueEntity getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(DefaultValueEntity defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<FieldValidationType> getValidations() {
        return this.validations;
    }
    public void setValidations(List<FieldValidationType> validations) {
        this.validations = validations;
    }

    public Boolean getIncludeInExport() {
        return this.includeInExport;
    }

    public void setIncludeInExport(Boolean includeInExport) {
        this.includeInExport = includeInExport;
    }

    public List<RuleEntity> getVisibilityRules() {
        return this.visibilityRules;
    }

    public void setVisibilityRules(List<RuleEntity> visibilityRules) {
        this.visibilityRules = visibilityRules;
    }
}
