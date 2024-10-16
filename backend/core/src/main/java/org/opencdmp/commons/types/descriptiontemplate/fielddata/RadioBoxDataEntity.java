package org.opencdmp.commons.types.descriptiontemplate.fielddata;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class RadioBoxDataEntity extends BaseFieldDataEntity {
    public static final String XmlElementName = "radioBoxData";
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RadioBoxDataOptionEntity {
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

    @XmlElementWrapper(name = "options")
    @XmlElement(name = "option")
    private List<RadioBoxDataOptionEntity> options;

    public List<RadioBoxDataOptionEntity> getOptions() {
        return options;
    }

    public void setOptions(List<RadioBoxDataOptionEntity> options) {
        this.options = options;
    }
}
