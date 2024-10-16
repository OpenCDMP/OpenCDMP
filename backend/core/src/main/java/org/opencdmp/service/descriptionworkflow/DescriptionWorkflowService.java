package org.opencdmp.service.descriptionworkflow;

import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.model.descriptionworkflow.DescriptionWorkflow;
import org.opencdmp.model.persist.descriptionworkflow.DescriptionWorkflowPersist;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface DescriptionWorkflowService {
    DescriptionWorkflow persist(DescriptionWorkflowPersist persist, FieldSet fields) throws InvalidApplicationException;

    void deleteAndSave(UUID id) throws InvalidApplicationException;
}
