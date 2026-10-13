package org.opencdmp.model.descriptiontemplate.fielddata;

import java.util.List;

public class SelectData extends LabelAndMultiplicityData {
    public final static String _options = "options";
    private List<Option> options;

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public static class Option {
        public final static String _label = "label";
        private String label;
        public final static String _value = "value";
        private String value;
        public final static String _semantics = "semantics";
        private List<SemanticValue> semantics;
        public final static String _exportsNothing = "exportsNothing";
        private Boolean exportsNothing;
    
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
    
        public List<SemanticValue> getSemantics() {
            return semantics;
        }
    
        public void setSemantics(List<SemanticValue> semantics) {
            this.semantics = semantics;
        }
    
        public Boolean getExportsNothing() {
            return exportsNothing;
        }
    
        public void setExportsNothing(Boolean exportsNothing) {
            this.exportsNothing = exportsNothing;
        }
    }
}
