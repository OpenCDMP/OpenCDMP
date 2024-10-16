package org.opencdmp.service.planblueprint;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commons.enums.PlanBlueprintSystemFieldType;
import org.opencdmp.commons.types.planblueprint.importexport.BlueprintImportExport;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.persist.PlanBlueprintPersist;
import org.opencdmp.model.persist.NewVersionPlanBlueprintPersist;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.UUID;

public interface PlanBlueprintService {

    PlanBlueprint persist(PlanBlueprintPersist model, UUID groupId, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, TransformerException, ParserConfigurationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
    boolean fieldInBlueprint(PlanBlueprintEntity planBlueprintEntity, PlanBlueprintSystemFieldType type);

    boolean fieldInBlueprint(UUID id, PlanBlueprintSystemFieldType type) throws InvalidApplicationException;

    PlanBlueprint buildClone(UUID id, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException;

    PlanBlueprint createNewVersion(NewVersionPlanBlueprintPersist model, FieldSet fieldSet) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException;

    BlueprintImportExport getExportXmlEntity(UUID id, boolean ignoreAuthorize) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException;

    ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, TransformerException, InvalidApplicationException;

    PlanBlueprint importXml(BlueprintImportExport planBlueprintDefinition, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException;

    PlanBlueprint importXml(byte[] bytes, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException;
    PlanBlueprint importCommonModel(PlanBlueprintModel planBlueprintModel, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException;
}
