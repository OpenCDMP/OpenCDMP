package org.opencdmp.service.reference;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.persist.ReferencePersist;
import org.opencdmp.query.lookup.ReferenceSearchLookup;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;
import java.util.UUID;

public interface ReferenceService {
	Reference persist(ReferencePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, TransformerException, ParserConfigurationException;

	void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

	List<Reference> searchReferenceData(ReferenceSearchLookup lookup) throws MyNotFoundException, InvalidApplicationException;

	Boolean findReference(String reference, UUID referenceTypeId);
}
