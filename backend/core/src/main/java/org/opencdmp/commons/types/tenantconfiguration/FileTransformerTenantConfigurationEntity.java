package org.opencdmp.commons.types.tenantconfiguration;

import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;

import java.util.List;

public class FileTransformerTenantConfigurationEntity {

	private List<FileTransformerSourceEntity> sources;
	private boolean disableSystemSources;

	public List<FileTransformerSourceEntity> getSources() {
		return sources;
	}

	public void setSources(List<FileTransformerSourceEntity> sources) {
		this.sources = sources;
	}

	public boolean getDisableSystemSources() {
		return disableSystemSources;
	}

	public void setDisableSystemSources(boolean disableSystemSources) {
		this.disableSystemSources = disableSystemSources;
	}
}
