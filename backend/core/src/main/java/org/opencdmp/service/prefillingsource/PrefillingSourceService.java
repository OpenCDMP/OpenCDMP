package org.opencdmp.service.prefillingsource;

import org.opencdmp.model.description.Description;
import org.opencdmp.model.Prefilling;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.model.persist.PrefillingSearchRequest;
import org.opencdmp.model.persist.DescriptionPrefillingRequest;
import org.opencdmp.model.persist.PrefillingSourcePersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface PrefillingSourceService {

    PrefillingSource persist(PrefillingSourcePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

    List<Prefilling> searchPrefillings(PrefillingSearchRequest model);

    Description getPrefilledDescription(DescriptionPrefillingRequest model, FieldSet fields) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException;

}
