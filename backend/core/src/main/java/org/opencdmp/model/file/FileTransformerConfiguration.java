package org.opencdmp.model.file;

import org.opencdmp.commonmodels.enums.PluginEntityType;
import org.opencdmp.commonmodels.models.ConfigurationField;
import org.opencdmp.commons.enums.PluginType;

import org.opencdmp.filetransformerbase.models.misc.FileFormat;

import java.util.List;

public class FileTransformerConfiguration {
	private String fileTransformerId;

	private List<FileFormat> exportVariants;
	private List<PluginEntityType> exportEntityTypes;
	private List<FileFormat> importVariants;
	private List<PluginEntityType> importEntityTypes;
	private List<ConfigurationField> configurationFields;
	private List<ConfigurationField> userConfigurationFields;
	private PluginType pluginType;

	public FileTransformerConfiguration(String fileTransformerId, List<FileFormat> exportVariants, List<PluginEntityType> exportEntityTypes, List<FileFormat> importVariants, List<PluginEntityType> importEntityTypes, List<ConfigurationField> configurationFields, List<ConfigurationField> userConfigurationFields, PluginType pluginType) {
		this.fileTransformerId = fileTransformerId;
		this.exportVariants = exportVariants;
		this.exportEntityTypes = exportEntityTypes;
		this.importVariants = importVariants;
		this.importEntityTypes = importEntityTypes;
		this.configurationFields = configurationFields;
		this.userConfigurationFields = userConfigurationFields;
		this.pluginType = pluginType;
	}

	public String getFileTransformerId() {
		return fileTransformerId;
	}

	public void setFileTransformerId(String fileTransformerId) {
		this.fileTransformerId = fileTransformerId;
	}

	public List<PluginEntityType> getExportEntityTypes() {
		return exportEntityTypes;
	}

	public void setExportEntityTypes(List<PluginEntityType> exportEntityTypes) {
		this.exportEntityTypes = exportEntityTypes;
	}

	public List<FileFormat> getExportVariants() {
		return exportVariants;
	}

	public void setExportVariants(List<FileFormat> exportVariants) {
		this.exportVariants = exportVariants;
	}

	public List<FileFormat> getImportVariants() {
		return importVariants;
	}

	public void setImportVariants(List<FileFormat> importVariants) {
		this.importVariants = importVariants;
	}

	public List<PluginEntityType> getImportEntityTypes() {
		return importEntityTypes;
	}

	public void setImportEntityTypes(List<PluginEntityType> importEntityTypes) {
		this.importEntityTypes = importEntityTypes;
	}

	public List<ConfigurationField> getConfigurationFields() {
		return configurationFields;
	}

	public void setConfigurationFields(List<ConfigurationField> configurationFields) {
		this.configurationFields = configurationFields;
	}

	public List<ConfigurationField> getUserConfigurationFields() {
		return userConfigurationFields;
	}

	public void setUserConfigurationFields(List<ConfigurationField> userConfigurationFields) {
		this.userConfigurationFields = userConfigurationFields;
	}

	public PluginType getPluginType() {
		return pluginType;
	}

	public void setPluginType(PluginType pluginType) {
		this.pluginType = pluginType;
	}
}
