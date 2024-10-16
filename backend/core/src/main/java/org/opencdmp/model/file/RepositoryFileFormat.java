package org.opencdmp.model.file;

import org.opencdmp.filetransformerbase.enums.FileTransformerEntityType;
import org.opencdmp.filetransformerbase.models.misc.FileFormat;

import java.util.List;

public class RepositoryFileFormat {
	private String format;
	private Boolean hasLogo;
	private String icon;
	private String repositoryId;

	private List<FileTransformerEntityType> entityTypes;

	public RepositoryFileFormat(String repositoryId, FileFormat format, List<FileTransformerEntityType> entityTypes) {
		this.repositoryId = repositoryId;
		this.format = format.getFormat();
		this.hasLogo = format.getHasLogo();
		this.icon = format.getIcon();
		this.entityTypes = entityTypes;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Boolean getHasLogo() {
		return hasLogo;
	}

	public void setHasLogo(Boolean hasLogo) {
		this.hasLogo = hasLogo;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public List<FileTransformerEntityType> getEntityTypes() {
		return entityTypes;
	}

	public void setEntityTypes(List<FileTransformerEntityType> entityTypes) {
		this.entityTypes = entityTypes;
	}
}
