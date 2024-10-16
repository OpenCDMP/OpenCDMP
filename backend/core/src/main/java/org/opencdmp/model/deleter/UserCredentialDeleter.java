package org.opencdmp.model.deleter;

import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.UserCredentialEntity;
import org.opencdmp.event.EventBroker;
import org.opencdmp.event.UserCredentialTouchedEvent;
import org.opencdmp.query.UserCredentialQuery;
import org.opencdmp.service.keycloak.KeycloakService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserCredentialDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserCredentialDeleter.class));
    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;
    private final KeycloakService keycloakService;
    private final EventBroker eventBroker;


    @Autowired
    public UserCredentialDeleter(
		    TenantEntityManager entityManager,
		    QueryFactory queryFactory, KeycloakService keycloakService, EventBroker eventBroker
    ) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
	    this.keycloakService = keycloakService;
	    this.eventBroker = eventBroker;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<UserCredentialEntity> data = this.queryFactory.query(UserCredentialQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<UserCredentialEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<UserCredentialEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        for (UserCredentialEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            logger.trace("deleting item");
            this.entityManager.remove(item);
            logger.trace("deleted item");
            
            this.keycloakService.removeFromAllGroups(item.getExternalId());

            this.eventBroker.emit(new UserCredentialTouchedEvent(item.getId(), item.getExternalId()));
        }
    }

}
