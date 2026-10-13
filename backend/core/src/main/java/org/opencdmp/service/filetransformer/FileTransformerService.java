package org.opencdmp.service.filetransformer;

import org.opencdmp.service.semantic.PluginSemantics;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commonmodels.enums.PluginEntityType;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.filetransformerbase.models.misc.TemplateValidationResultModel;
import org.opencdmp.model.file.FileTransformerConfiguration;
import org.opencdmp.model.persist.PlanCommonModelConfig;
import org.opencdmp.model.persist.PlanSuggestion;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FileTransformerService {
	/** A transformer's built-in template, for admins to start a custom one from; {@code null} when it has none. */
	byte[] getDefaultTemplate(String repositoryId, PluginEntityType entityType) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	/** A PDF of a plan or asset rendered with a transformer's settings as edited, for the settings pages. */
	org.opencdmp.model.file.FileEnvelope previewWithSettings(org.opencdmp.model.file.PluginSettingsPreviewRequest request) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	/** The semantics manifests of the tenant's file transformers that publish one. */
	List<PluginSemantics> getSemanticsManifests() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	/** Whether the transformer is set up and switched on for the current tenant. */
	boolean isAvailable(String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	List<FileTransformerConfiguration> getAvailableExportFileFormats() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportPlan(UUID planId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportPlan(UUID planId, String repositoryId, String format, boolean isPublic, Map<String, String> options) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportPlanInternal(UUID planId, String repositoryId, String format) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportDescription(UUID descriptionId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportDescription(UUID descriptionId, String repositoryId, String format, boolean isPublic, Map<String, String> options) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportDescriptionInternal(UUID descriptionId, String repositoryId, String format) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	PlanModel importPlan(PlanCommonModelConfig planCommonModelConfig) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, JAXBException;

	PreprocessingPlanModel preprocessingPlan(UUID fileId, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException;

	TemplateValidationResultModel validateTemplate(UUID fileId, String repositoryId, PluginEntityType entityType) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException;

	PreprocessingPlanModel preprocessingPlan(byte[] fileData, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	PlanModel importPlan(byte[] data, PlanSuggestion suggestion, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, JAXBException;

	PlanModel importPlanPreview(byte[] data, PlanSuggestion suggestion, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, JAXBException;

	PreprocessingPlanModel preprocessingPlanPreview(byte[] fileData, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;
}
