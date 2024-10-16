package org.opencdmp.service.user.settings;

import org.opencdmp.model.UserSettings;
import org.opencdmp.model.persist.UserSettingsPersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;

public interface UserSettingsService {
	UserSettings persist(UserSettingsPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;
}
