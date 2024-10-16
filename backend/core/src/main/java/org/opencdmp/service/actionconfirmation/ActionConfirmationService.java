package org.opencdmp.service.actionconfirmation;

import org.opencdmp.model.actionconfirmation.ActionConfirmation;
import org.opencdmp.model.persist.ActionConfirmationPersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.UUID;

public interface ActionConfirmationService {

    ActionConfirmation persist(ActionConfirmationPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException;

}
