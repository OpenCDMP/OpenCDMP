package org.opencdmp.elastic.data.nested;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.UUID;

public class NestedDoiElasticEntity {
	
	@Id
	@Field(value = NestedDoiElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@Field(value = NestedDoiElasticEntity._repositoryId, type = FieldType.Keyword)
	private String repositoryId;
	public final static String _repositoryId = "repositoryId";

	@Field(value = NestedDoiElasticEntity._doi, type = FieldType.Keyword)
	private String doi;
	public final static String _doi = "doi";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}
}
