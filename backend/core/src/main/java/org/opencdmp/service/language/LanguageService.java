package org.opencdmp.service.language;

import org.opencdmp.model.Language;
import org.opencdmp.model.persist.LanguagePersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.UUID;

public interface LanguageService {

    Language persist(LanguagePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    String getPayload(String code) throws IOException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
}
