package org.opencdmp.service.deposit;

import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.deposit.DepositAuthMethodResult;
import org.opencdmp.model.persist.deposit.DepositRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface DepositService {
	List<org.opencdmp.model.deposit.DepositConfiguration> getAvailableConfigurations(FieldSet fieldSet) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	EntityDoi deposit(DepositRequest planDepositModel) throws Exception;

	String getLogo(String repositoryId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

	DepositAuthMethodResult getAuthMethods(String repositoryId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

}
