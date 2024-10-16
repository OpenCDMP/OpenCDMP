package org.opencdmp.elastic.data;

import gr.cite.tools.elastic.ElasticConstants;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.elastic.data.nested.NestedDescriptionTemplateElasticEntity;
import org.opencdmp.elastic.data.nested.NestedPlanElasticEntity;
import org.opencdmp.elastic.data.nested.NestedReferenceElasticEntity;
import org.opencdmp.elastic.data.nested.NestedTagElasticEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(indexName = "description")
public class DescriptionElasticEntity {
	
	@Id
	@Field(value = DescriptionElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@Field(value = DescriptionElasticEntity._tenantId, type = FieldType.Keyword)
	private UUID tenantId;
	public final static String _tenantId = "tenantId";

	@MultiField(mainField = @Field(value = DescriptionElasticEntity._label, type = FieldType.Text), otherFields = @InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword))
	private String label;
	public final static String _label = "label";

	@Field(value = DescriptionElasticEntity._description, type = FieldType.Text)
	private String description;
	public final static String _description = "description";

	@Field(value = DescriptionElasticEntity._status, type = FieldType.Short)
	private DescriptionStatus status;
	public final static String _status = "status";

	@Field(value = DescriptionElasticEntity._finalizedAt, type = FieldType.Date)
	private Date finalizedAt;
	public final static String _finalizedAt = "finalizedAt";

	@Field(value = DescriptionElasticEntity._tags, type = FieldType.Nested)
	private List<NestedTagElasticEntity> tags;
	public final static String _tags = "tags";

	@Field(value = DescriptionElasticEntity._descriptionTemplate, type = FieldType.Object)
	private NestedDescriptionTemplateElasticEntity descriptionTemplate;
	public final static String _descriptionTemplate = "descriptionTemplate";

	@Field(value = DescriptionElasticEntity._plan, type = FieldType.Object)
	private NestedPlanElasticEntity plan;
	public final static String _plan = "plan";

	@Field(value = DescriptionElasticEntity._references, type = FieldType.Nested)
	private List<NestedReferenceElasticEntity> references;
	public final static String _references = "references";

	@Field(value = DescriptionElasticEntity._updatedAt, type = FieldType.Date)
	private Date updatedAt;
	public final static String _updatedAt = "updatedAt";

	@Field(value = DescriptionElasticEntity._createdAt, type = FieldType.Date)
	private Date createdAt;
	public final static String _createdAt = "createdAt";

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getTenantId() {
		return this.tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
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

	public DescriptionStatus getStatus() {
		return this.status;
	}

	public void setStatus(DescriptionStatus status) {
		this.status = status;
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

	public NestedDescriptionTemplateElasticEntity getDescriptionTemplate() {
		return this.descriptionTemplate;
	}

	public void setDescriptionTemplate(NestedDescriptionTemplateElasticEntity descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}

	public NestedPlanElasticEntity getPlan() {
		return this.plan;
	}

	public void setPlan(NestedPlanElasticEntity plan) {
		this.plan = plan;
	}

	public List<NestedReferenceElasticEntity> getReferences() {
		return this.references;
	}

	public void setReferences(List<NestedReferenceElasticEntity> references) {
		this.references = references;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
}
