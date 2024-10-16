package org.opencdmp.service.descriptionstatus;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusPersist;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface DescriptionStatusService {

    DescriptionStatus persist(DescriptionStatusPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
}
