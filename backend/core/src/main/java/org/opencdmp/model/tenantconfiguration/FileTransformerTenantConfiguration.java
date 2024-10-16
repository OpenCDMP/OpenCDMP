package org.opencdmp.model.tenantconfiguration;

import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;
import org.opencdmp.model.filetransformer.FileTransformerSource;

import java.util.List;

public class FileTransformerTenantConfiguration {

	private List<FileTransformerSource> sources;
	public static final String _sources = "sources";
	private Boolean disableSystemSources;
	public static final String _disableSystemSources = "disableSystemSources";

	public List<FileTransformerSource> getSources() {
		return sources;
	}

	public void setSources(List<FileTransformerSource> sources) {
		this.sources = sources;
	}

	public Boolean getDisableSystemSources() {
		return disableSystemSources;
	}

	public void setDisableSystemSources(Boolean disableSystemSources) {
		this.disableSystemSources = disableSystemSources;
	}
}
