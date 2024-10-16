package org.opencdmp.elastic.data.nested;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.UUID;

public class NestedPlanDescriptionTemplateElasticEntity {
	
	@Id
	@Field(value = NestedPlanDescriptionTemplateElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@Field(value = NestedDescriptionElasticEntity._planId, type = FieldType.Keyword)
	private UUID planId;
	public final static String _planId = "planId";

	@Field(value = NestedPlanDescriptionTemplateElasticEntity._descriptionTemplateGroupId, type = FieldType.Keyword)
	private UUID descriptionTemplateGroupId;
	public final static String _descriptionTemplateGroupId = "descriptionTemplateGroupId";

	@Field(value = NestedPlanDescriptionTemplateElasticEntity._sectionId, type = FieldType.Keyword)
	private UUID sectionId;
	public final static String _sectionId = "sectionId";
	

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getDescriptionTemplateGroupId() {
		return this.descriptionTemplateGroupId;
	}

	public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) {
		this.descriptionTemplateGroupId = descriptionTemplateGroupId;
	}

	public UUID getSectionId() {
		return this.sectionId;
	}

	public void setSectionId(UUID sectionId) {
		this.sectionId = sectionId;
	}

	public UUID getPlanId() {
		return this.planId;
	}

	public void setPlanId(UUID planId) {
		this.planId = planId;
	}
}
