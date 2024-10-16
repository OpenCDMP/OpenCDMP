package org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class RadioBoxDataImportExport extends BaseFieldDataImportExport {
    public static final String XmlElementName = "radioBoxData";

    @XmlElementWrapper(name = "options")
    @XmlElement(name = "option")
    private List<RadioBoxOption> options;

    public List<RadioBoxOption> getOptions() {
        return options;
    }

    public void setOptions(List<RadioBoxOption> options) {
        this.options = options;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RadioBoxOption {
        @XmlAttribute(name = "label")
        private String label;
        @XmlAttribute(name = "value")
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

