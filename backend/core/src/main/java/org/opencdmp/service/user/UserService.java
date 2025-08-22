package org.opencdmp.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.model.persist.*;
import org.opencdmp.model.persist.actionconfirmation.RemoveCredentialRequestPersist;
import org.opencdmp.model.user.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public interface UserService {

    User persist(UserPersist model, FieldSet fields)throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JsonProcessingException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;

    void updateLanguageMine(String language) throws InvalidApplicationException, JsonProcessingException;

    void updateTimezoneMine(String timezone) throws JsonProcessingException, InvalidApplicationException;

    void updateCultureMine(String culture) throws JsonProcessingException, InvalidApplicationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

    byte[] exportCsv(boolean hasTenantAdminMode) throws IOException, InvalidApplicationException;

    User patchRoles(UserRolePatchPersist model, FieldSet fields) throws InvalidApplicationException;

    void sendMergeAccountConfirmation(UserMergeRequestPersist model) throws InvalidApplicationException, JAXBException;

    void sendRemoveCredentialConfirmation(RemoveCredentialRequestPersist model) throws InvalidApplicationException, JAXBException;

    void sendUserToTenantInvitation(UserTenantUsersInviteRequest users) throws InvalidApplicationException, JAXBException;

    boolean doesTokenBelongToLoggedInUser(String token) throws InvalidApplicationException, IOException;

    void confirmMergeAccount(String token) throws InvalidApplicationException, IOException;

    void confirmRemoveCredential(String token) throws InvalidApplicationException;

    void confirmUserInviteToTenant(String token) throws InvalidApplicationException;
}
