package org.opencdmp.commons.types.descriptiontemplate.fielddata;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SelectDataEntity extends LabelAndMultiplicityDataEntity {
    public static final String XmlElementName = "selectData";
    @XmlElementWrapper(name = "options")
    @XmlElement(name = "options")
    private List<OptionEntity> options;

    public List<OptionEntity> getOptions() {
        return options;
    }

    public void setOptions(List<OptionEntity> optionEntities) {
        this.options = optionEntities;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OptionEntity {
        @XmlAttribute(name="label")
        private String label;
        @XmlAttribute(name="value")
        private String value;

        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}

