package org.opencdmp.model.publicapi.datasetwizard;

import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.MultiplicityEntity;

import java.util.*;
import java.util.stream.Collectors;
 
public class FieldSet implements Comparable<Object>, PropertiesModelBuilder {
    private String id;
    private Integer ordinal;
    private String title;
    private String numbering;
    private String description;
    private String extendedDescription;
    private String additionalInformation;
    private MultiplicityEntity multiplicity;
    private List<Field> fields;
    private List<FieldSet> multiplicityItems;
    private boolean hasCommentField;
    private String commentFieldValue;

    public List<Field> getFields() {
        Collections.sort(this.fields);
        return this.fields;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtendedDescription() {
        return this.extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public MultiplicityEntity getMultiplicity() {
        return this.multiplicity;
    }

    public void setMultiplicity(MultiplicityEntity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public List<FieldSet> getMultiplicityItems() {
        if (this.multiplicityItems != null) Collections.sort(this.multiplicityItems);
        return this.multiplicityItems;
    }

    public String getNumbering() {
        return this.numbering;
    }

    public void setNumbering(String numbering) {
        this.numbering = numbering;
    }

    public void setMultiplicityItems(List<FieldSet> multiplicityItems) {
        this.multiplicityItems = multiplicityItems;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public void setHasCommentField(boolean hasCommentField) {
        this.hasCommentField = hasCommentField;
    }

    public boolean getHasCommentField() {
        return this.hasCommentField;
    }

    public String getCommentFieldValue() {
        return this.commentFieldValue;
    }

    public void setCommentFieldValue(String commentFieldValue) {
        this.commentFieldValue = commentFieldValue;
    }

    public String getAdditionalInformation() {
        return this.additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public FieldSetEntity toDatabaseDefinition(FieldSetEntity item) {
//        List<FieldEntity> viewStylefields = new ModelBuilder().toViewStyleDefinition(this.fields, FieldEntity.class);
//        item.setFields(viewStylefields);
//        item.setId(this.id);
//        item.setOrdinal(this.ordinal);
//        item.setHasCommentField(this.hasCommentField);
//        item.setMultiplicity(this.multiplicity);
////        item.setCommentFieldValue(this.commentFieldValue);
//        return item;
        return null;
    }


    public void fromDatabaseDefinition(FieldSetEntity item) {
//        this.fields = new ModelBuilder().fromViewStyleDefinition(item.getFields(), Field.class);
//        this.id = item.getId();
//        this.ordinal = item.getOrdinal();
//        this.title = item.getTitle();
//        this.description = item.getDescription();
//        this.additionalInformation=item.getAdditionalInformation();
//        this.numbering = item.getNumbering();
//        this.extendedDescription = item.getExtendedDescription();
//        this.hasCommentField = item.getHasCommentField();
//        this.multiplicity = item.getMultiplicity();
////        this.commentFieldValue = item.getCommentFieldValue();
    }

    @Override
    public void fromJsonObject(Map<String, Object> properties) {
        this.commentFieldValue = (String) properties.get("commentFieldValue" + this.id);
        this.multiplicityItems = new LinkedList<FieldSet>();
        this.fields.forEach(item -> {
            item.fromJsonObject(properties);
        });
        List<String> compositeKeysFather = properties.keySet().stream().filter(keys -> keys.startsWith("multiple_" + this.getId())).collect(Collectors.toList());
        List<String> Ids=new ArrayList<>();
        int index = 1;
        for (String composite : compositeKeysFather) {
            String[] split = composite.split("_");
            if (!Ids.contains(split[2])) {
                Ids.add(split[2]);
                this.multiplicityItems.add(this.CloneForMultiplicity2(properties.keySet().stream().filter(keys -> keys.startsWith("multiple_" + this.getId() + "_" + split[2])).collect(Collectors.toList()), properties,split, index));
                index++;
            }
        }
    }

    private FieldSet CloneForMultiplicity2(List<String> key, Map<String, Object> properties,String[] ids, int index){
        FieldSet newFieldSet = new FieldSet();
        newFieldSet.id = ids[0]+"_"+ids[1]+"_"+ids[2] + (ids.length > 4 ? "_" + ids[3] : "");
        newFieldSet.description = this.description;
        newFieldSet.extendedDescription = this.extendedDescription;
        newFieldSet.additionalInformation=this.additionalInformation;
        newFieldSet.title = this.title;
        newFieldSet.ordinal = ids.length > 4 ? Integer.valueOf(ids[3]) : this.ordinal;
        newFieldSet.fields = new LinkedList();

        for (Field field: this.fields) {
            newFieldSet.fields.add(field.cloneForMultiplicity(newFieldSet.id + "_" + field.getId(), properties, index));
        }
        return newFieldSet;
    }



    @Override
    public int compareTo(Object o) {
        return this.ordinal.compareTo(((FieldSet) o).getOrdinal());
    }

    @Override
    public void fromJsonObject(Map<String, Object> properties, String path) {
        // TODO Auto-generated method stub

    }

    public void toMap(Map<String, Object> fieldValues) {
        fieldValues.put("commentFieldValue" + this.id, this.commentFieldValue);
        this.fields.forEach(item -> item.toMap(fieldValues));
        Map<String, Object> multiplicity = new HashMap<String, Object>();
        if (this.multiplicityItems != null) {
            this.multiplicityItems.forEach(item -> item.toMap(fieldValues, this.multiplicityItems.indexOf(item)));
        }
        //fieldValues.put(this.id,multiplicity);
    }

    public void toMap(Map<String, Object> fieldValues, int index) {
        this.fields.forEach(item -> item.toMap(fieldValues, index));
        //this.multiplicityItems.forEach(item->item.toMap(fieldValues,index));
    }
}
