package org.opencdmp.commons.scope.user;


import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface UserScope {

    Boolean isSet();

    UUID getUserId() throws InvalidApplicationException;

    UUID getUserIdSafe();

    void setUserId(UUID userId);
}