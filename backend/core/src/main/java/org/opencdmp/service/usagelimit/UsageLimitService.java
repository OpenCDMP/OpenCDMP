package org.opencdmp.service.usagelimit;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.model.UsageLimit;
import org.opencdmp.model.persist.UsageLimitPersist;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface UsageLimitService {

    UsageLimit persist(UsageLimitPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

    void checkIncrease(UsageLimitTargetMetric metric) throws InvalidApplicationException;

}
