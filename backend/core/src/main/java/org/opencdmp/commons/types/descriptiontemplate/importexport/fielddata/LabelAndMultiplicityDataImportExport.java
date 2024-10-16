package org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class LabelAndMultiplicityDataImportExport extends BaseFieldDataImportExport {
    public static final String XmlElementName = "labelAndMultiplicityData";

    @XmlAttribute(name = "multipleSelect")
    private Boolean multipleSelect;

    public Boolean getMultipleSelect() {
        return this.multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

}

