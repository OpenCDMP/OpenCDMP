package org.opencdmp.service.deposit;

import org.opencdmp.service.semantic.PluginSemantics;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.commonmodels.models.deposit.DepositPreviewModel;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.depositbase.enums.DepositFileKind;
import org.opencdmp.model.deposit.DepositAuthMethodResult;
import org.opencdmp.model.deposit.DepositFile;
import org.opencdmp.model.file.FileEnvelope;
import org.opencdmp.model.persist.deposit.DepositPreviewRequest;
import org.opencdmp.model.persist.deposit.DepositRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public interface DepositService {
	/** The semantics manifests of the tenant's deposit plugins that publish one. */
	List<PluginSemantics> getSemanticsManifests() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	List<org.opencdmp.model.deposit.DepositConfiguration> getAvailableConfigurations(FieldSet fieldSet) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	EntityDoi deposit(DepositRequest planDepositModel) throws Exception;

	DepositPreviewModel preview(DepositPreviewRequest previewRequest) throws Exception;

	List<DepositFile> getFiles(String repositoryId, UUID planId) throws Exception;

	FileEnvelope getFile(String repositoryId, UUID planId, DepositFileKind kind) throws Exception;

	List<org.opencdmp.commonmodels.models.deposit.PreviewFieldOption> searchVocabulary(String repositoryId, java.util.UUID planId, String fieldCode, String like) throws Exception;

	String getLogo(String repositoryId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	DepositAuthMethodResult getAuthMethods(String repositoryId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

}
