package org.opencdmp.service.referencetype;

import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.persist.ReferenceTypePersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface ReferenceTypeService {

    ReferenceType persist(ReferenceTypePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
}
