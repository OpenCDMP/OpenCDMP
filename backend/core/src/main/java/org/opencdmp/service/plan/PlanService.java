package org.opencdmp.service.plan;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commons.types.plan.importexport.PlanImportExport;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.model.PlanInvitationResult;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.PlanValidationResult;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.persist.*;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public interface PlanService {

    Plan persist(PlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, IOException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException;

    void finalize(UUID id, List<UUID> descriptionIds) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;

    void undoFinalize(UUID id, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;

    PlanValidationResult validate(UUID id) throws InvalidApplicationException;

    Plan createNewVersion(NewVersionPlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, IOException, TransformerException;

    Plan buildClone(ClonePlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, IOException, InvalidApplicationException;
    Plan buildPublicClone(ClonePlanPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, IOException, InvalidApplicationException;

    List<PlanUser> assignUsers(UUID planId, List<PlanUserPersist> model, FieldSet fields, boolean disableDelete) throws InvalidApplicationException, IOException;
    Plan removeUser(PlanUserRemovePersist model, FieldSet fields) throws InvalidApplicationException, IOException;

    ResponseEntity<byte[]> export(UUID id, String transformerId, String exportType, boolean isPublic) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

    void inviteUserOrAssignUsers(UUID id, List<PlanUserPersist> users) throws InvalidApplicationException, JAXBException, IOException;

    PlanInvitationResult planInvitationAccept(String token) throws InvalidApplicationException, IOException;

    PlanImportExport exportXmlEntity(UUID id, boolean ignoreAuthorize, boolean isPublic) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

    ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

    ResponseEntity<byte[]> exportPublicXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

    Plan importXml(byte[] bytes, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException;

    Plan importJson(PlanCommonModelConfig planCommonModelConfig, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ParserConfigurationException, TransformerException, InstantiationException, IllegalAccessException, SAXException;

    PreprocessingPlanModel preprocessingPlan(UUID fileId, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException;
}
