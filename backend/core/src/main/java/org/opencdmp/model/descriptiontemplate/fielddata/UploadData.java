package org.opencdmp.model.descriptiontemplate.fielddata;


import java.util.List;

public class UploadData extends BaseFieldData {
    public final static String _types = "types";
    private List<UploadOption> types;

    public List<UploadOption> getTypes() {
        return types;
    }

	public final static String _maxFileSizeInMB = "maxFileSizeInMB";
	private Integer maxFileSizeInMB;

    public void setTypes(List<UploadOption> types) {
        this.types = types;
    }

    public Integer getMaxFileSizeInMB() {
        return maxFileSizeInMB;
    }

    public void setMaxFileSizeInMB(Integer maxFileSizeInMB) {
        this.maxFileSizeInMB = maxFileSizeInMB;
    }


	public static class UploadOption {
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

