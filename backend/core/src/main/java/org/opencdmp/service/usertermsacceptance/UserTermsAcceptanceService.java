package org.opencdmp.service.usertermsacceptance;

import javax.management.InvalidApplicationException;

public interface UserTermsAcceptanceService {

    UserTermsStatus getStatus() throws InvalidApplicationException;

    void accept(String clientIp) throws InvalidApplicationException;
}
