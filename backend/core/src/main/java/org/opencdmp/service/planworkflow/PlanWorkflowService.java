package org.opencdmp.service.planworkflow;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commons.types.planworkflow.PlanWorkflowDefinitionEntity;
import org.opencdmp.model.persist.planworkflow.PlanWorkflowPersist;
import org.opencdmp.model.planworkflow.PlanWorkflow;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface PlanWorkflowService {
    PlanWorkflow persist(PlanWorkflowPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

    PlanWorkflowDefinitionEntity getActiveWorkFlowDefinition() throws InvalidApplicationException;
}
