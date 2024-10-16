package org.opencdmp.service.supportivematerial;

import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.model.SupportiveMaterial;
import org.opencdmp.model.persist.SupportiveMaterialPersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.xml.bind.JAXBException;
import org.springframework.http.ResponseEntity;

public interface SupportiveMaterialService {

    byte[] loadFromFile(String language, SupportiveMaterialFieldType type) throws IOException;

    SupportiveMaterial persist(SupportiveMaterialPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
}
