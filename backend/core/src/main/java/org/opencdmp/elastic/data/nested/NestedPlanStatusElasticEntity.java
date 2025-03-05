package org.opencdmp.elastic.data.nested;

import gr.cite.tools.elastic.ElasticConstants;
import org.opencdmp.commons.enums.PlanStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.util.UUID;

public class NestedPlanStatusElasticEntity {

    @Id
    @Field(value = NestedPlanStatusElasticEntity._id, type = FieldType.Keyword)
    private UUID id;
    public final static String _id = "id";

    @MultiField(mainField = @Field(value = NestedPlanStatusElasticEntity._name, type = FieldType.Text), otherFields = @InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword))
    private String name;
    public final static String _name = "name";

    @Field(value = NestedPlanStatusElasticEntity._internalStatus, type = FieldType.Short)
    private PlanStatus internalStatus;
    public final static String _internalStatus = "internalStatus";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlanStatus getInternalStatus() {
        return internalStatus;
    }

    public void setInternalStatus(PlanStatus internalStatus) {
        this.internalStatus = internalStatus;
    }
}
