package org.opencdmp.service.entitydoi;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.persist.EntityDoiPersist;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface EntityDoiService {

    EntityDoi persist(EntityDoiPersist model, boolean disableAuthorization, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

}
