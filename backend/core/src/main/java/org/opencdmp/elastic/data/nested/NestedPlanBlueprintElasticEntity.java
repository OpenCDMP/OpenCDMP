package org.opencdmp.elastic.data.nested;

import gr.cite.tools.elastic.ElasticConstants;
import org.opencdmp.commons.enums.PlanBlueprintVersionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.util.UUID;

public class NestedPlanBlueprintElasticEntity {
    @Id
    @Field(value = NestedPlanBlueprintElasticEntity._id, type = FieldType.Keyword)
    private UUID id;
    public final static String _id = "id";

    @MultiField(mainField = @Field(value = NestedPlanBlueprintElasticEntity._label, type = FieldType.Text), otherFields = @InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword))
    private String label;
    public final static String _label = "label";

    @Field(value = NestedPlanBlueprintElasticEntity._groupId, type = FieldType.Keyword)
    private UUID groupId;
    public final static String _groupId = "groupId";

    @Field(value = NestedPlanBlueprintElasticEntity._versionStatus, type = FieldType.Short)
    private PlanBlueprintVersionStatus versionStatus;
    public final static String _versionStatus = "versionStatus";

    @Field(value = NestedPlanBlueprintElasticEntity._typeId, type = FieldType.Keyword)
    private UUID typeId;
    public final static String _typeId = "typeId";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UUID getGroupId() {
        return this.groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public PlanBlueprintVersionStatus getVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(PlanBlueprintVersionStatus versionStatus) {
        this.versionStatus = versionStatus;
    }

    public UUID getTypeId() {
        return typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }
}
