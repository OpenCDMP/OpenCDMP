package org.opencdmp.model.deleter;

import gr.cite.tools.data.deleter.Deleter;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.*;
import org.opencdmp.query.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserDeleter implements Deleter {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserDeleter.class));
    private final TenantEntityManager entityManager;

    protected final QueryFactory queryFactory;

    protected final DeleterFactory deleterFactory;

    @Autowired
    public UserDeleter(
            TenantEntityManager entityManager,
            QueryFactory queryFactory,
            DeleterFactory deleterFactory
    ) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactory;
        this.deleterFactory = deleterFactory;
    }

    public void deleteAndSaveByIds(List<UUID> ids) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("collecting to delete").And("count", Optional.ofNullable(ids).map(List::size).orElse(0)).And("ids", ids));
        List<UserEntity> data = this.queryFactory.query(UserQuery.class).ids(ids).collect();
        logger.trace("retrieved {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.deleteAndSave(data);
    }

    public void deleteAndSave(List<UserEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        this.delete(data);
        logger.trace("saving changes");
        this.entityManager.flush();
        logger.trace("changes saved");
    }

    public void delete(List<UserEntity> data) throws InvalidApplicationException {
        logger.debug("will delete {} items", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty())
            return;
        List<UUID> ids = data.stream().map(UserEntity::getId).distinct().collect(Collectors.toList());
        {
            logger.debug("checking related - {}", UserRoleEntity.class.getSimpleName());
            List<UserRoleEntity> items = this.queryFactory.query(UserRoleQuery.class).userIds(ids).collect();
            UserRoleDeleter deleter = this.deleterFactory.deleter(UserRoleDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", UserCredentialEntity.class.getSimpleName());
            List<UserCredentialEntity> items = this.queryFactory.query(UserCredentialQuery.class).userIds(ids).collect();
            UserCredentialDeleter deleter = this.deleterFactory.deleter(UserCredentialDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", UserContactInfoEntity.class.getSimpleName());
            List<UserContactInfoEntity> items = this.queryFactory.query(UserContactInfoQuery.class).userIds(ids).collect();
            UserContactInfoDeleter deleter = this.deleterFactory.deleter(UserContactInfoDeleter.class);
            deleter.delete(items);
        }
        {
            logger.debug("checking related - {}", TenantUserEntity.class.getSimpleName());
            List<TenantUserEntity> items = this.queryFactory.query(TenantUserQuery.class).userIds(ids).collect();
            TenantUserDeleter deleter = this.deleterFactory.deleter(TenantUserDeleter.class);
            deleter.delete(items);
        }
        Instant now = Instant.now();

        for (UserEntity item : data) {
            logger.trace("deleting item {}", item.getId());
            item.setIsActive(IsActive.Inactive);
            item.setUpdatedAt(now);
            logger.trace("updating item");
            this.entityManager.merge(item);
            logger.trace("updated item");
        }
    }

}
