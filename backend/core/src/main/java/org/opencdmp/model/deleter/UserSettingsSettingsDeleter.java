package org.opencdmp.model.deleter;

import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.UserSettingsEntity;
import org.opencdmp.query.UserSettingsQuery;
import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
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
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserSettingsSettingsDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserSettingsSettingsDeleter.class));
    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;


    @Autowired
    public UserSettingsSettingsDeleter(
            TenantEntityManager entityManager,
            QueryFactory queryFactory
    ) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<UserSettingsEntity> data = this.queryFactory.query(UserSettingsQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<UserSettingsEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<UserSettingsEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;

        for (UserSettingsEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            logger.trace("deleting item");
            this.entityManager.remove(item);
            logger.trace("deleted item");
        }
    }

}
