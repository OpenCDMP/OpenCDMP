package org.opencdmp.commons.scope.user;

import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service(UserScopeImpl.QualifierName)
@Qualifier(UserScopeImpl.QualifierName)
@RequestScope
public class UserScopeImpl implements UserScope{
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserScope.class));
    private final AtomicReference<UUID> userId = new AtomicReference<>();
    public static final String QualifierName = "userScopeImpl";

    public Boolean isSet() {
        return this.userId.get() != null;
    }

    public UUID getUserId() throws InvalidApplicationException {
        if (this.userId.get() == null) throw new InvalidApplicationException("user not set");
        return this.userId.get();
    }

    public UUID getUserIdSafe() {
        return this.userId.get();
    }

    public void setUserId(UUID userId) {
        this.userId.set(userId);
    }
}

