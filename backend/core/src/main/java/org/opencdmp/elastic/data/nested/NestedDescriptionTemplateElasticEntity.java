package org.opencdmp.elastic.data.nested;

import gr.cite.tools.elastic.ElasticConstants;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.util.UUID;

public class NestedDescriptionTemplateElasticEntity {
	
	@Id
	@Field(value = NestedDescriptionTemplateElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@MultiField(mainField = @Field(value = NestedDescriptionTemplateElasticEntity._label, type = FieldType.Text), otherFields = @InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword))
	private String label;
	public final static String _label = "label";

	@Field(value = NestedDescriptionTemplateElasticEntity._groupId, type = FieldType.Keyword)
	private UUID groupId;
	public final static String _groupId = "groupId";
	
	@Field(value = NestedDescriptionTemplateElasticEntity._versionStatus, type = FieldType.Short)
	private DescriptionTemplateVersionStatus versionStatus;
	public final static String _versionStatus = "versionStatus";

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

	public DescriptionTemplateVersionStatus getVersionStatus() {
		return this.versionStatus;
	}

	public void setVersionStatus(DescriptionTemplateVersionStatus versionStatus) {
		this.versionStatus = versionStatus;
	}
}
