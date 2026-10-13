package org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SelectDataImportExport extends LabelAndMultiplicityDataImportExport {
    public static final String XmlElementName = "selectData";
    @XmlElementWrapper(name = "options")
    @XmlElement(name = "options")
    private List<OptionImportExport> options;


    public List<OptionImportExport> getOptions() {
        return options;
    }

    public void setOptions(List<OptionImportExport> options) {
        this.options = options;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OptionImportExport {
        @XmlAttribute(name = "label")
        private String label;
        @XmlAttribute(name = "value")
        private String value;
        @XmlElementWrapper(name = "semantics")
        @XmlElement(name = "semantic")
        private List<SemanticValueImportExport> semantics;
        @XmlAttribute(name = "exportsNothing")
        private boolean exportsNothing;

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

        public List<SemanticValueImportExport> getSemantics() {
            return semantics;
        }
        public void setSemantics(List<SemanticValueImportExport> semantics) {
            this.semantics = semantics;
        }

        public boolean getExportsNothing() {
            return exportsNothing;
        }
        public void setExportsNothing(boolean exportsNothing) {
            this.exportsNothing = exportsNothing;
        }

    }
}
