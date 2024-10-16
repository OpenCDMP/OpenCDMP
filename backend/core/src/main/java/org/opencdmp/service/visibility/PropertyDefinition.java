package org.opencdmp.service.visibility;

import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionFieldSetPersist;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionPersist;

import java.util.HashMap;
import java.util.Map;

public class PropertyDefinition {

    private final Map<String, PropertyDefinitionFieldSet> fieldSets;

    public Map<String, PropertyDefinitionFieldSet> getFieldSets() {
        return this.fieldSets;
    }

    public  PropertyDefinition(PropertyDefinitionPersist persist){
        this.fieldSets = new HashMap<>();
        if (persist == null || persist.getFieldSets() ==  null || persist.getFieldSets().isEmpty()) return;
        for (Map.Entry<String, PropertyDefinitionFieldSetPersist> item : persist.getFieldSets().entrySet()){
            this.fieldSets.put(item.getKey(), new PropertyDefinitionFieldSet(item.getValue()));
        }
    }

    public  PropertyDefinition(PropertyDefinitionEntity entity){
        this.fieldSets = new HashMap<>();
        if (entity == null || entity.getFieldSets() ==  null || entity.getFieldSets().isEmpty()) return;
        for (Map.Entry<String, PropertyDefinitionFieldSetEntity> item : entity.getFieldSets().entrySet()){
            this.fieldSets.put(item.getKey(), new PropertyDefinitionFieldSet(item.getValue()));
        }
    }
}
