package org.opencdmp.service.descriptiontemplatetype;

import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.persist.DescriptionTemplateTypePersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface DescriptionTemplateTypeService {

    DescriptionTemplateType persist(DescriptionTemplateTypePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

}
