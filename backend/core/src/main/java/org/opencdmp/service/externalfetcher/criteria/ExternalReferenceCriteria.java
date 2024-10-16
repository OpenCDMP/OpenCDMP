package org.opencdmp.service.externalfetcher.criteria;

import org.opencdmp.model.reference.Reference;

import java.util.List;

public class ExternalReferenceCriteria {
	private String like;
	private String page;
	private String pageSize;
	private String path;
	private String host;
	
	private List<Reference> dependencyReferences;

	public ExternalReferenceCriteria(String like, List<Reference> dependencyReferences) {
		this.like = like;
		this.dependencyReferences = dependencyReferences;
	}

	public String getLike() {
		return like;
	}
	public void setLike(String like) {
		this.like = like;
	}

	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}

	public String getPageSize() {
		return pageSize;
	}
	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public ExternalReferenceCriteria() {
	}

	public List<Reference> getDependencyReferences() {
		return dependencyReferences;
	}

	public void setDependencyReferences(List<Reference> dependencyReferences) {
		this.dependencyReferences = dependencyReferences;
	}
}
