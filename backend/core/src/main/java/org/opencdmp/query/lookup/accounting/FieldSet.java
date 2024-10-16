package org.opencdmp.query.lookup.accounting;

import java.util.List;

public class FieldSet{
    private List<String> fields;

    public FieldSet(List<String> fields) {
        this.fields = fields;
    }

    public List<String> getFields() {
        return this.fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
