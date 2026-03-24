package org.opencdmp.service.planblueprinttype;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.model.PlanBlueprintType;
import org.opencdmp.model.persist.PlanBlueprintTypePersist;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface PlanBlueprintTypeService {

    PlanBlueprintType persist(PlanBlueprintTypePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

}
