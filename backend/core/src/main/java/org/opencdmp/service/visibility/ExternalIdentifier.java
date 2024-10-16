package org.opencdmp.service.visibility;

import org.opencdmp.commons.types.description.ExternalIdentifierEntity;
import org.opencdmp.model.persist.descriptionproperties.ExternalIdentifierPersist;

public class ExternalIdentifier {
    private final String identifier;
    private final String type;

    public String getIdentifier() {
        return identifier;
    }

    public String getType() {
        return type;
    }

    public  ExternalIdentifier(ExternalIdentifierPersist persist){
        this.identifier = persist.getIdentifier();
        this.type = persist.getType();
    }

    public  ExternalIdentifier(ExternalIdentifierEntity entity){
        this.identifier = entity.getIdentifier();
        this.type = entity.getType();
    }
}
