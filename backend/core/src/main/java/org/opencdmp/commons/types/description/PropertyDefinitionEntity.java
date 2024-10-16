package org.opencdmp.commons.types.description;

import java.util.Map;

public class PropertyDefinitionEntity {
    private Map<String, PropertyDefinitionFieldSetEntity> fieldSets;

    public Map<String, PropertyDefinitionFieldSetEntity> getFieldSets() {
        return this.fieldSets;
    }

    public void setFieldSets(Map<String, PropertyDefinitionFieldSetEntity> fieldSets) {
        this.fieldSets = fieldSets;
    }
}



