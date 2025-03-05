package org.opencdmp.elastic.data.nested;

import gr.cite.tools.elastic.ElasticConstants;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.elastic.data.DescriptionElasticEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NestedDescriptionElasticEntity {
	
	@Id
	@Field(value = NestedDescriptionElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@Field(value = NestedDescriptionElasticEntity._planId, type = FieldType.Keyword)
	private UUID planId;
	public final static String _planId = "planId";

	@MultiField(mainField = @Field(value = NestedDescriptionElasticEntity._label, type = FieldType.Text), otherFields = @InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword))
	private String label;
	public final static String _label = "label";

	@Field(value = NestedDescriptionElasticEntity._description, type = FieldType.Text)
	private String description;
	public final static String _description = "description";

	@Field(value = NestedDescriptionElasticEntity._statusId, type = FieldType.Keyword)
	private UUID statusId;
	public final static String _statusId = "statusId";

	@Field(value = NestedDescriptionElasticEntity._finalizedAt, type = FieldType.Date)
	private Date finalizedAt;
	public final static String _finalizedAt = "finalizedAt";

	@Field(value = NestedDescriptionElasticEntity._tags, type = FieldType.Nested)
	private List<NestedTagElasticEntity> tags;
	public final static String _tags = "tags";

	@Field(value = NestedDescriptionElasticEntity._references, type = FieldType.Nested)
	private List<NestedReferenceElasticEntity> references;
	public final static String _references = "references";

	@Field(value = DescriptionElasticEntity._descriptionTemplate, type = FieldType.Object)
	private NestedDescriptionTemplateElasticEntity descriptionTemplate;
	public final static String _descriptionTemplate = "descriptionTemplate";

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getPlanId() {
		return this.planId;
	}

	public void setPlanId(UUID planId) {
		this.planId = planId;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UUID getStatusId() {
		return statusId;
	}

	public void setStatusId(UUID statusId) {
		this.statusId = statusId;
	}

	public Date getFinalizedAt() {
		return this.finalizedAt;
	}

	public void setFinalizedAt(Date finalizedAt) {
		this.finalizedAt = finalizedAt;
	}

	public List<NestedTagElasticEntity> getTags() {
		return this.tags;
	}

	public void setTags(List<NestedTagElasticEntity> tags) {
		this.tags = tags;
	}

	public List<NestedReferenceElasticEntity> getReferences() {
		return this.references;
	}

	public void setReferences(List<NestedReferenceElasticEntity> references) {
		this.references = references;
	}

	public NestedDescriptionTemplateElasticEntity getDescriptionTemplate() {
		return this.descriptionTemplate;
	}

	public void setDescriptionTemplate(NestedDescriptionTemplateElasticEntity descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}
}
