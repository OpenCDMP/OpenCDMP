package org.opencdmp.service.visibility;

import org.opencdmp.commons.types.description.FieldEntity;
import org.opencdmp.model.persist.ReferencePersist;
import org.opencdmp.model.persist.descriptionproperties.FieldPersist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Field {

    private final String textValue;

    private final List<String> textListValue;

    private final Instant dateValue;
    private final Boolean booleanValue;
    
    private final ExternalIdentifier externalIdentifier;

    private Double numberValue;

    public String getTextValue() {
        return this.textValue;
    }


    public List<String> getTextListValue() {
        return this.textListValue;
    }


    public Instant getDateValue() {
        return this.dateValue;
    }

    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public ExternalIdentifier getExternalIdentifier() {
        return this.externalIdentifier;
    }

    public Double getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Double numberValue) {
        this.numberValue = numberValue;
    }

    public  Field(FieldPersist persist){
	    List<String> tempTextListValue;
	    this.textValue = persist.getTextValue();
        this.dateValue = persist.getDateValue();
        this.booleanValue = persist.getBooleanValue();
        this.numberValue = persist.getNumberValue();
        tempTextListValue = persist.getTextListValue();
        if (persist.getExternalIdentifier() != null) this.externalIdentifier = new ExternalIdentifier(persist.getExternalIdentifier());
        else this.externalIdentifier = null;
        List<ReferencePersist> references = persist.getReferences() != null ? new ArrayList<>(persist.getReferences()) : new ArrayList<>();
        if (persist.getReference() != null) references.add(persist.getReference());
        if (!references.isEmpty()){
            tempTextListValue = references.stream().filter(x-> x.getId() != null).map(x-> x.getId().toString()).toList();
        }
        if (persist.getTags() != null && !persist.getTags().isEmpty()){
            tempTextListValue = persist.getTags();
        }
	    this.textListValue = tempTextListValue;
    }


    public  Field(FieldEntity entity){
        this.textValue = entity.getTextValue();
        this.dateValue = entity.getDateValue();
        this.booleanValue = entity.getBooleanValue();
        this.numberValue = entity.getNumberValue();
        this.textListValue = entity.getTextListValue();
        if (entity.getExternalIdentifier() != null) this.externalIdentifier = new ExternalIdentifier(entity.getExternalIdentifier());
        else this.externalIdentifier = null;
    }

}


