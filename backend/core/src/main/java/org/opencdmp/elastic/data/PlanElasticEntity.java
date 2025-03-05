package org.opencdmp.elastic.data;

import gr.cite.tools.elastic.ElasticConstants;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.elastic.data.nested.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(indexName = "plan")
public class PlanElasticEntity {
	@Id
	@Field(value = PlanElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@Field(value = PlanElasticEntity._tenantId, type = FieldType.Keyword)
	private UUID tenantId;
	public final static String _tenantId = "tenantId";

	@MultiField(mainField = @Field(value = PlanElasticEntity._label, type = FieldType.Text), otherFields = @InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword))
	private String label;
	public final static String _label = "label";

	@Field(value = PlanElasticEntity._description, type = FieldType.Text)
	private String description;
	public final static String _description = "description";

	@Field(value = PlanElasticEntity._version, type = FieldType.Keyword)
	private Short version;
	public final static String _version = "version";

	@Field(value = PlanElasticEntity._planStatus, type = FieldType.Object)
	private NestedPlanStatusElasticEntity planStatus;
	public final static String _planStatus = "planStatus";

	@Field(value = PlanElasticEntity._accessType, type = FieldType.Short)
	private PlanAccessType accessType;
	public final static String _accessType = "accessType";
	@Field(value = PlanElasticEntity._versionStatus, type = FieldType.Short)
	private PlanVersionStatus versionStatus;
	public final static String _versionStatus = "versionStatus";

	@Field(value = PlanElasticEntity._language, type = FieldType.Keyword)
	private String language;
	public final static String _language = "language";

	@Field(value = PlanElasticEntity._blueprintId, type = FieldType.Keyword)
	private UUID blueprintId;
	public final static String _blueprintId = "blueprintId";

	@Field(value = PlanElasticEntity._groupId, type = FieldType.Keyword)
	private UUID groupId;
	public final static String _groupId = "groupId";

	@Field(value = PlanElasticEntity._finalizedAt, type = FieldType.Date)
	private Date finalizedAt;
	public final static String _finalizedAt = "finalizedAt";

	@Field(value = PlanElasticEntity._references, type = FieldType.Nested)
	private List<NestedReferenceElasticEntity> references;
	public final static String _references = "references";

	@Field(value = PlanElasticEntity._collaborators, type = FieldType.Nested)
	private List<NestedCollaboratorElasticEntity> collaborators;
	public final static String _collaborators = "collaborators";

	@Field(value = PlanElasticEntity._descriptions, type = FieldType.Nested)
	private List<NestedDescriptionElasticEntity> descriptions;
	public final static String _descriptions = "descriptions";

	@Field(value = PlanElasticEntity._planDescriptionTemplates, type = FieldType.Nested)
	private List<NestedPlanDescriptionTemplateElasticEntity> planDescriptionTemplates;
	public final static String _planDescriptionTemplates = "planDescriptionTemplates";

	@Field(value = PlanElasticEntity._dois, type = FieldType.Nested)
	private List<NestedDoiElasticEntity> dois;
	public final static String _dois = "dois";

	@Field(value = PlanElasticEntity._updatedAt, type = FieldType.Date)
	private Date updatedAt;
	public final static String _updatedAt = "updatedAt";

	@Field(value = PlanElasticEntity._createdAt, type = FieldType.Date)
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

	public Short getVersion() {
		return this.version;
	}

	public void setVersion(Short version) {
		this.version = version;
	}

	public NestedPlanStatusElasticEntity getPlanStatus() {
		return planStatus;
	}

	public void setPlanStatus(NestedPlanStatusElasticEntity planStatus) {
		this.planStatus = planStatus;
	}

	public PlanAccessType getAccessType() {
		return this.accessType;
	}

	public void setAccessType(PlanAccessType accessType) {
		this.accessType = accessType;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public UUID getBlueprintId() {
		return this.blueprintId;
	}

	public void setBlueprintId(UUID blueprintId) {
		this.blueprintId = blueprintId;
	}

	public UUID getGroupId() {
		return this.groupId;
	}

	public void setGroupId(UUID groupId) {
		this.groupId = groupId;
	}

	public Date getFinalizedAt() {
		return this.finalizedAt;
	}

	public void setFinalizedAt(Date finalizedAt) {
		this.finalizedAt = finalizedAt;
	}

	public List<NestedReferenceElasticEntity> getReferences() {
		return this.references;
	}

	public void setReferences(List<NestedReferenceElasticEntity> references) {
		this.references = references;
	}

	public List<NestedCollaboratorElasticEntity> getCollaborators() {
		return this.collaborators;
	}

	public void setCollaborators(List<NestedCollaboratorElasticEntity> collaborators) {
		this.collaborators = collaborators;
	}

	public List<NestedDescriptionElasticEntity> getDescriptions() {
		return this.descriptions;
	}

	public void setDescriptions(List<NestedDescriptionElasticEntity> descriptions) {
		this.descriptions = descriptions;
	}

	public List<NestedDoiElasticEntity> getDois() {
		return this.dois;
	}

	public void setDois(List<NestedDoiElasticEntity> dois) {
		this.dois = dois;
	}

	public PlanVersionStatus getVersionStatus() {
		return this.versionStatus;
	}

	public void setVersionStatus(PlanVersionStatus versionStatus) {
		this.versionStatus = versionStatus;
	}

	public List<NestedPlanDescriptionTemplateElasticEntity> getPlanDescriptionTemplates() {
		return this.planDescriptionTemplates;
	}

	public void setPlanDescriptionTemplates(List<NestedPlanDescriptionTemplateElasticEntity> planDescriptionTemplates) {
		this.planDescriptionTemplates = planDescriptionTemplates;
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
