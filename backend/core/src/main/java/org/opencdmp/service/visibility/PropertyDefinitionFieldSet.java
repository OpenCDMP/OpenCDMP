package org.opencdmp.service.visibility;

import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionFieldSetItemPersist;
import org.opencdmp.model.persist.descriptionproperties.PropertyDefinitionFieldSetPersist;

import java.util.ArrayList;
import java.util.List;

public class PropertyDefinitionFieldSet {

    private final List<PropertyDefinitionFieldSetItem> items;

    public List<PropertyDefinitionFieldSetItem> getItems() {
        return this.items;
    }

    public  PropertyDefinitionFieldSet(PropertyDefinitionFieldSetPersist persist){
        if (persist == null || persist.getItems() == null || persist.getItems().isEmpty()) {
            this.items = null;
            return;
        }
        this.items = new ArrayList<>();
        for (PropertyDefinitionFieldSetItemPersist item : persist.getItems()){
            this.items.add(new PropertyDefinitionFieldSetItem(item));
        }
    }

    public  PropertyDefinitionFieldSet(PropertyDefinitionFieldSetEntity entity){
        if (entity == null || entity.getItems() == null || entity.getItems().isEmpty()) {
            this.items = null;
            return;
        }
        this.items = new ArrayList<>();
        for (PropertyDefinitionFieldSetItemEntity item : entity.getItems()){
            this.items.add(new PropertyDefinitionFieldSetItem(item));
        }
    }
}


