package org.opencdmp.model.planupdaterequest.preview;

public class IncomingField {

    private String key;
    public static final String _key = "key";

    private String label;
    public static final String _label = "label";

    private Object value;
    public static final String _value = "value";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
