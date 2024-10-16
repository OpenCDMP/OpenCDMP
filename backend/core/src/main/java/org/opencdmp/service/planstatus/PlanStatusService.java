package org.opencdmp.service.planstatus;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.model.persist.planstatus.PlanStatusPersist;
import org.opencdmp.model.planstatus.PlanStatus;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface PlanStatusService {
    PlanStatus persist(PlanStatusPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
}
