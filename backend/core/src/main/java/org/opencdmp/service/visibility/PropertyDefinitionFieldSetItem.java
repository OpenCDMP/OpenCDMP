package org.opencdmp.service.visibility;

import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.model.persist.descriptionproperties.FieldPersist;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionFieldSetItemPersist;

import java.util.HashMap;
import java.util.Map;

public class PropertyDefinitionFieldSetItem {

    private final Map<String, Field> fields;

    private final Integer ordinal;

    public Map<String, Field> getFields() {
        return this.fields;
    }


    public Integer getOrdinal() {
        return this.ordinal;
    }


    public  PropertyDefinitionFieldSetItem(PropertyDefinitionFieldSetItemPersist persist){
        this.ordinal = persist.getOrdinal();
        
        if (persist.getFields() ==  null || persist.getFields().isEmpty()) {
            this.fields = null;
            return;
        }
        this.fields = new HashMap<>();
        for (Map.Entry<String, FieldPersist> item : persist.getFields().entrySet()){
            this.fields.put(item.getKey(), new Field(item.getValue()));
        }
    }

    public  PropertyDefinitionFieldSetItem(PropertyDefinitionFieldSetItemEntity entity){
        this.ordinal = entity.getOrdinal();

        if (entity.getFields() ==  null || entity.getFields().isEmpty()) {
            this.fields = null;
            return;
        }
        this.fields = new HashMap<>();
        for (Map.Entry<String, FieldEntity> item : entity.getFields().entrySet()){
            this.fields.put(item.getKey(), new Field(item.getValue()));
        }
    }
}


