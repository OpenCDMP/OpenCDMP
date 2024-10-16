package org.opencdmp.model.publicapi.datasetwizard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONException;
import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.MultiplicityEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Field implements Comparable, PropertiesModelBuilder {
    private static final Logger logger = LoggerFactory.getLogger(Field.class);
    private String id;
    private Integer ordinal;
    private Object value;
    private FieldDescriptionEntity viewStyle;
    private String datatype;
    private String numbering;
    private int page; 
    private DefaultValueEntity defaultValue;
    private MultiplicityEntity multiplicity;
    private Object data;
    private List<Field> multiplicityItems;
    private List<FieldValidationType> validations;
    private VisibilityEntity visible;
    private List<String> semantics;

    private Boolean export;

    public List<Field> getMultiplicityItems() {
        return this.multiplicityItems;
    }

    public void setMultiplicityItems(List<Field> multiplicityItems) {
        this.multiplicityItems = multiplicityItems;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public FieldDescriptionEntity getViewStyle() {
        return this.viewStyle;
    }

    public void setViewStyle(FieldDescriptionEntity viewStyle) {
        this.viewStyle = viewStyle;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public DefaultValueEntity getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(DefaultValueEntity defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDatatype() {
        return this.datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public MultiplicityEntity getMultiplicity() {
        return this.multiplicity;
    }

    public void setMultiplicity(MultiplicityEntity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public VisibilityEntity getVisible() {
        return this.visible;
    }

    public void setVisible(VisibilityEntity visible) {
        this.visible = visible;
    }

    public List<Integer> getValidations() {
        if(this.validations == null) {
            return null;
        }
        return this.validations.stream().map(item -> (int) item.getValue()).collect(Collectors.toList());
    }

    public void setValidations(List<Integer> validations) {
        this.validations = validations.stream().map(x-> FieldValidationType.of(x.shortValue())).collect(Collectors.toList());
    }

    public String getNumbering() {
        return this.numbering;
    }

    public void setNumbering(String numbering) {
        this.numbering = numbering;
    }

    public List<String> getSemantics() {
        return this.semantics;
    }

    public void setSemantics(List<String> semantics) {
        this.semantics = semantics;
    }

    public Boolean getExport() {
        return this.export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    Field cloneForMultiplicity(String key, Map<String, Object> properties, int index) {
        Field newField = new Field();
        newField.id = key;
        newField.ordinal = this.ordinal;
        newField.value = properties.containsKey(key)? properties.get(key): null;
        newField.viewStyle = this.viewStyle;
        newField.datatype = this.datatype;
        newField.page = this.page;
        newField.defaultValue = this.defaultValue;
        newField.data = this.data;
        newField.validations = this.validations;
        newField.semantics = this.semantics;
        newField.numbering = "mult" + index + "_" + this.numbering;
        newField.export = this.export;
        return newField;
    }

    public FieldEntity toDatabaseDefinition(FieldEntity fieldEntity) {
        fieldEntity.setId(this.id);
        fieldEntity.setOrdinal(this.ordinal);
        throw new NotImplementedException(" Use new logic");
        //TODO: Use new logic
//        fieldEntity.setData(new FieldDataHelper().toFieldData(data, this.viewStyle.getFieldType()));
//        fieldEntity.setDefaultValue(this.defaultValue.getValue());
//        fieldEntity.setVisibilityRules(this.visible.getRules());
//        fieldEntity.setValidations(this.validations);
//        fieldEntity.setSchematics(this.semantics);
//        fieldEntity.setIncludeInExport(this.export);
//        return fieldEntity;
    }

    public void fromDatabaseDefinition(FieldEntity item) {
        this.id = item.getId();
        this.ordinal = item.getOrdinal();
        FieldDescriptionEntity fieldDescription = new FieldDescriptionEntity();
        fieldDescription.setFieldType(item.getData().getFieldType());
        this.viewStyle =  fieldDescription;
//        this.numbering = item.getNumbering();
        this.data = item.getData();

        DefaultValueEntity defaultValueEntity = new DefaultValueEntity();
        defaultValueEntity.setValue(item.getDefaultValue().getTextValue()); //TODO
        this.defaultValue = defaultValueEntity;
        VisibilityEntity visibilityEntity = new VisibilityEntity();
        visibilityEntity.setRules(item.getVisibilityRules());
        this.visible = visibilityEntity;
        this.validations = item.getValidations();
        this.semantics = item.getSemantics();
        this.export = item.getIncludeInExport();
    }

    @Override
    public void fromJsonObject(Map<String, Object> properties) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> stringList = mapper.readValue(properties.get(this.id).toString(), LinkedList.class);
            this.value = stringList;
        } catch (JSONException | NullPointerException | IOException e) {
            try {
                this.value = (String) properties.get(this.id);
            } catch (ClassCastException ce) {
                this.value = properties.get(this.id);
            }
        }
        this.multiplicityItems = new LinkedList<>();
        List<String> compositeKeys = properties.keySet().stream().filter(keys -> keys.startsWith("multiple_" + this.getId())).collect(Collectors.toList());
        int index = 1;
        for (String key : compositeKeys) {
            this.multiplicityItems.add(this.cloneForMultiplicity(key, properties, index));
        }
    }

    @Override
    public int compareTo(Object o) {
        Field comparedField = (Field) o;
        if(this.ordinal != null) {
            return this.ordinal.compareTo(comparedField.getOrdinal());
        } else if (comparedField.getOrdinal() != null) {
            return comparedField.getOrdinal().compareTo(this.ordinal);
        } else {
            return 0;
        }
    }

    @Override
    public void fromJsonObject(Map<String, Object> properties, String path) {
        this.value = (String) properties.get(path);
    }

    public void toMap(Map<String, Object> fieldValues) {
        if (this.value != null) {
            if ((this.viewStyle != null && this.viewStyle.getFieldType().equals("datasetIdentifier") && this.value instanceof Map || this.value instanceof Collection)) {
                ObjectMapper mapper = new ObjectMapper();
                String valueString = null;
                try {
                    valueString = mapper.writeValueAsString(this.value);
                    fieldValues.put(this.id, valueString);
                } catch (JsonProcessingException e) {
                    logger.error(e.getMessage(), e);
                }
            } /*else if (this.value instanceof Collection) {
                Collection valueCollection = (Collection) this.value;
                StringBuilder valueBuilder = new StringBuilder();
                valueBuilder.append("[");
               for (int i = 0; i < valueCollection.size(); i++) {
                   valueBuilder.append("\"").append(valueCollection.toArray()[i]).append("\"");
                   if (i < valueCollection.size() - 1) {
                       valueBuilder.append(", ");
                   }
               }
               valueBuilder.append("]");
               fieldValues.put(this.id, valueBuilder.toString());
            }*/
            else if ((this.viewStyle != null && this.viewStyle.getFieldType().equals("upload"))) {
                fieldValues.put(this.id, this.value);
            }
            else {
                fieldValues.put(this.id, this.value.toString());
            }
        } else {
            fieldValues.put(this.id, "");
        }
    }

    public void toMap(Map<String, Object> fieldValues, int index) {
        fieldValues.put(this.id, this.value);
    }
}