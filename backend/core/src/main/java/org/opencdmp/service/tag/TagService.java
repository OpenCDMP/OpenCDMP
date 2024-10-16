package org.opencdmp.service.tag;

import org.opencdmp.model.Tag;
import org.opencdmp.model.persist.TagPersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface TagService {

    Tag persist(TagPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

}
