package org.opencdmp.service.filetransformer;

import jakarta.xml.bind.JAXBException;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.model.file.RepositoryFileFormat;
import org.opencdmp.model.persist.PlanCommonModelConfig;

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
	List<RepositoryFileFormat> getAvailableExportFileFormats() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportPlan(UUID planId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	org.opencdmp.model.file.FileEnvelope exportDescription(UUID descriptionId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	PlanModel importPlan(PlanCommonModelConfig planCommonModelConfig) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, JAXBException;

	PreprocessingPlanModel preprocessingPlan(UUID fileId, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException;
}
