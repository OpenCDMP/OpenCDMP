package org.opencdmp.service.lock;

import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.model.Lock;
import org.opencdmp.model.LockStatus;
import org.opencdmp.model.persist.LockPersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface LockService {

    Lock persist(LockPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    LockStatus isLocked(UUID target, FieldSet fields) throws InvalidApplicationException;

    void lock(UUID target, LockTargetType targetType) throws InvalidApplicationException;

    void touch(UUID target) throws InvalidApplicationException;

    void unlock(UUID target) throws InvalidApplicationException;

    void deleteAndSave(UUID id, UUID target) throws MyForbiddenException, InvalidApplicationException;
}
