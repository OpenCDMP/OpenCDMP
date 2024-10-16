package org.opencdmp.model;

import java.util.Map;

public class Prefilling {

    private String id;
    public static final String _id = "reference_id";

    private String label;
    public static final String _label = "label";

    private Map<String, String> data;
    public static final String _data = "data";
    private String key;
    public static final String _key = "key";
    private String tag;
    public static final String _tag = "tag";

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getData() {
        return this.data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
