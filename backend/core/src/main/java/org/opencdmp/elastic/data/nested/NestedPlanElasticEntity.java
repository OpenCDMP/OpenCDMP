package org.opencdmp.elastic.data.nested;

import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanVersionStatus;
import gr.cite.tools.elastic.ElasticConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NestedPlanElasticEntity {
	@Id
	@Field(value = NestedPlanElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@MultiField(mainField = @Field(value = NestedPlanElasticEntity._label, type = FieldType.Text), otherFields = {
			@InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword)
	})
	private String label;
	public final static String _label = "label";

	@Field(value = NestedPlanElasticEntity._description, type = FieldType.Text)
	private String description;
	public final static String _description = "description";

	@Field(value = NestedPlanElasticEntity._version, type = FieldType.Keyword)
	private Short version;
	public final static String _version = "version";
	
	@Field(value = NestedPlanElasticEntity._versionStatus, type = FieldType.Short)
	private PlanVersionStatus versionStatus;
	public final static String _versionStatus = "versionStatus";

	@Field(value = NestedPlanElasticEntity._statusId, type = FieldType.Keyword)
	private UUID statusId;
	public final static String _statusId = "statusId";

	@Field(value = NestedPlanElasticEntity._accessType, type = FieldType.Short)
	private PlanAccessType accessType;
	public final static String _accessType = "accessType";

	@Field(value = NestedPlanElasticEntity._language, type = FieldType.Keyword)
	private String language;
	public final static String _language = "language";

	@Field(value = NestedPlanElasticEntity._blueprintId, type = FieldType.Keyword)
	private UUID blueprintId;
	public final static String _blueprintId = "blueprintId";

	@Field(value = NestedPlanElasticEntity._groupId, type = FieldType.Keyword)
	private UUID groupId;
	public final static String _groupId = "groupId";

	@Field(value = NestedPlanElasticEntity._finalizedAt, type = FieldType.Date)
	private Date finalizedAt;
	public final static String _finalizedAt = "finalizedAt";

	@Field(value = NestedPlanElasticEntity._references, type = FieldType.Nested)
	private List<NestedReferenceElasticEntity> references;
	public final static String _references = "references";

	@Field(value = NestedPlanElasticEntity._collaborators, type = FieldType.Nested)
	private List<NestedCollaboratorElasticEntity> collaborators;
	public final static String _collaborators = "collaborators";

	@Field(value = NestedPlanElasticEntity._dois, type = FieldType.Nested)
	private List<NestedDoiElasticEntity> dois;
	public final static String _dois = "dois";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Short getVersion() {
		return version;
	}

	public void setVersion(Short version) {
		this.version = version;
	}

	public UUID getStatusId() {
		return statusId;
	}

	public void setStatusId(UUID statusId) {
		this.statusId = statusId;
	}

	public PlanAccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(PlanAccessType accessType) {
		this.accessType = accessType;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public UUID getBlueprintId() {
		return blueprintId;
	}

	public void setBlueprintId(UUID blueprintId) {
		this.blueprintId = blueprintId;
	}

	public UUID getGroupId() {
		return groupId;
	}

	public void setGroupId(UUID groupId) {
		this.groupId = groupId;
	}

	public Date getFinalizedAt() {
		return finalizedAt;
	}

	public void setFinalizedAt(Date finalizedAt) {
		this.finalizedAt = finalizedAt;
	}

	public List<NestedReferenceElasticEntity> getReferences() {
		return references;
	}

	public void setReferences(List<NestedReferenceElasticEntity> references) {
		this.references = references;
	}

	public List<NestedCollaboratorElasticEntity> getCollaborators() {
		return collaborators;
	}

	public void setCollaborators(List<NestedCollaboratorElasticEntity> collaborators) {
		this.collaborators = collaborators;
	}

	public List<NestedDoiElasticEntity> getDois() {
		return dois;
	}

	public void setDois(List<NestedDoiElasticEntity> dois) {
		this.dois = dois;
	}

	public PlanVersionStatus getVersionStatus() {
		return versionStatus;
	}

	public void setVersionStatus(PlanVersionStatus versionStatus) {
		this.versionStatus = versionStatus;
	}
}
