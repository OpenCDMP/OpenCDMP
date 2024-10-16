package org.opencdmp.service.tenantconfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.opencdmp.model.persist.tenantconfiguration.TenantConfigurationPersist;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public interface TenantConfigurationService {

    TenantConfiguration persist(TenantConfigurationPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JsonProcessingException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

}
