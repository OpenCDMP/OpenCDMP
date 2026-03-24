package org.opencdmp.service.filetransformer;

import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
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
import java.util.UUID;

public interface FileTransformerService {
	List<FileTransformerConfiguration> getAvailableExportFileFormats() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportPlan(UUID planId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportPlanInternal(UUID planId, String repositoryId, String format) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportDescription(UUID descriptionId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	PlanModel importPlan(PlanCommonModelConfig planCommonModelConfig) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, JAXBException;

	PreprocessingPlanModel preprocessingPlan(UUID fileId, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException;

	PreprocessingPlanModel preprocessingPlan(byte[] fileData, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	PlanModel importPlan(byte[] data, PlanSuggestion suggestion, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, JAXBException;
}
