package org.opencdmp.service.planupdaterequest;

import tools.jackson.core.JacksonException;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.model.persist.PlanSuggestion;
import org.opencdmp.model.persist.PlanUpdateRequestPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planupdaterequest.PlanUpdateRequest;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public interface PlanUpdateRequestService {

    PlanUpdateRequest persist(PlanUpdateRequestPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, JacksonException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

    PreprocessingPlanModel preprocessing(UUID id) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

    Plan buildPlan(PlanSuggestion suggestion) throws InvalidAlgorithmParameterException, JAXBException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeyException;

}
