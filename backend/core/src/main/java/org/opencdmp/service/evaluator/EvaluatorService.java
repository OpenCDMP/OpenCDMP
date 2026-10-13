package org.opencdmp.service.evaluator;

import org.opencdmp.evaluatorbase.models.misc.RankResultModel;

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

public interface EvaluatorService {

    List<RankResultModel> rankPlan(UUID planId, String repositoryId, String format, List<String> benchmarkIds, List<String> testIds) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException;

    List<RankResultModel> rankDescription(UUID descriptionId, String repositoryId, String format, List<String> benchmarkIds, List<String> testIds, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException;

    List<org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration> getAvailableEvaluators() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

    String getLogo(String evaluatorId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

}
