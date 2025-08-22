package org.opencdmp.service.description;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commons.types.description.importexport.DescriptionImportExport;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.model.DescriptionValidationResult;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.persist.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
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
import java.util.Map;
import java.util.UUID;

public interface DescriptionService {

	Map<UUID, List<String>> getDescriptionSectionPermissions(DescriptionSectionPermissionResolver model);

	Description persist(DescriptionPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;
    List<Description> persistMultiple(DescriptionMultiplePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;
    Description persistStatus(DescriptionStatusPersist model, FieldSet fields) throws IOException, InvalidApplicationException;
    Description buildClone(UUID id, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException;

    String reassignDescriptionProperties(DescriptionEntity existingDescriptionEntity) throws InvalidApplicationException;

    List<DescriptionValidationResult> validate(List<UUID> descriptionIds) throws InvalidApplicationException;

    ResponseEntity<byte[]> export(UUID id, String exportType, boolean isPublic) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

    StorageFile uploadFieldFile(DescriptionFieldFilePersist model, MultipartFile file, FieldSet fields) throws IOException;
    StorageFileEntity getFieldFile(UUID descriptionId, UUID storageFileId);
	void updateDescriptionTemplate(UpdateDescriptionTemplatePersist model) throws InvalidApplicationException, IOException, JAXBException;

	DescriptionImportExport exportXmlEntity(UUID id, boolean ignoreAuthorize, boolean isPublic) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

	ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

    ResponseEntity<byte[]> exportPublicXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

    Description importXml(DescriptionImportExport descriptionXml, UUID planId, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException;

    Description importCommonModel(DescriptionModel model, UUID planId, FieldSet fields) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException, JAXBException, ParserConfigurationException, TransformerException, InstantiationException, IllegalAccessException, SAXException;

}
