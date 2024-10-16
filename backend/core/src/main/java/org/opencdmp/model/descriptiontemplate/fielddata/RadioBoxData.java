package org.opencdmp.model.descriptiontemplate.fielddata;


import java.util.List;

public class RadioBoxData extends BaseFieldData {


    public final static String _options = "options";
    private List<RadioBoxOption> options;

    public List<RadioBoxOption> getOptions() {
        return options;
    }

    public void setOptions(List<RadioBoxOption> options) {
        this.options = options;
    }

	public static class RadioBoxOption {
		public final static String _label = "label";
		private String label;
		public final static String _value = "value";
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
