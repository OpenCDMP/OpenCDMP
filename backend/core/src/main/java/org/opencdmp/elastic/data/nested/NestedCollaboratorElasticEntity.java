package org.opencdmp.elastic.data.nested;

import gr.cite.tools.elastic.ElasticConstants;
import org.opencdmp.commons.enums.PlanUserRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.util.UUID;

public class NestedCollaboratorElasticEntity {
	@Id
	@Field(value = NestedCollaboratorElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";
	
	@Field(value = NestedCollaboratorElasticEntity._userId, type = FieldType.Keyword)
	private UUID userId;
	public final static String _userId = "userId";

	@MultiField(mainField = @Field(value = NestedCollaboratorElasticEntity._name, type = FieldType.Text), otherFields = @InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword))
	private String name;
	public final static String _name = "name";

	@Field(value = NestedCollaboratorElasticEntity._role, type = FieldType.Short)
	private PlanUserRole role;
	public final static String _role = "role";

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PlanUserRole getRole() {
		return this.role;
	}

	public void setRole(PlanUserRole role) {
		this.role = role;
	}

	public UUID getUserId() {
		return this.userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}
}
