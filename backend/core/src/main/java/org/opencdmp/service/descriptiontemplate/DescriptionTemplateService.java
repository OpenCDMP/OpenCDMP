package org.opencdmp.service.descriptiontemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateImportExport;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.persist.DescriptionTemplatePersist;
import org.opencdmp.model.persist.NewVersionDescriptionTemplatePersist;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;
import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.UUID;

public interface DescriptionTemplateService {

    DescriptionTemplate persist(DescriptionTemplatePersist model, UUID groupId, FieldSet fields)throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException;
    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
    DescriptionTemplate buildClone(UUID id, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException;
    DescriptionTemplate createNewVersion(NewVersionDescriptionTemplatePersist model, FieldSet fields)  throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    DescriptionTemplate importXml(DescriptionTemplateImportExport importXml, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException;

    DescriptionTemplate importXml(byte[] bytes, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException;
    DescriptionTemplateImportExport exportXmlEntity(UUID id, boolean ignoreAuthorize) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

    ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, TransformerException, InvalidApplicationException;

    DescriptionTemplate importCommonModel(DescriptionTemplateModel commonModel, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException;
}
