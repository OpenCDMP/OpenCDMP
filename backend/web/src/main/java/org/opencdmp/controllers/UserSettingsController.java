package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import jakarta.transaction.Transactional;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.data.UserSettingsEntity;
import org.opencdmp.model.UserSettings;
import org.opencdmp.model.builder.UserSettingsBuilder;
import org.opencdmp.model.censorship.UserSettingsCensor;
import org.opencdmp.model.persist.UserSettingsPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.UserSettingsQuery;
import org.opencdmp.query.lookup.UserSettingsLookup;
import org.opencdmp.service.user.settings.UserSettingsService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(path = "api/user-settings")
public class UserSettingsController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserSettingsController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final UserSettingsService settingsService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    @Autowired
    public UserSettingsController(
            BuilderFactory builderFactory,
            AuditService auditService,
            UserSettingsService settingsService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.settingsService = settingsService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    public QueryResult<UserSettings> Query(@RequestBody UserSettingsLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", UserSettings.class.getSimpleName());
        this.censorFactory.censor(UserSettingsCensor.class).censor(lookup.getProject(), null);
        UserSettingsQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<UserSettingsEntity> data = query.collectAs(lookup.getProject());
        List<UserSettings> models = this.builderFactory.builder(UserSettingsBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.User_Settings_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{key}")
    @Transactional
    public UserSettings Get(@PathVariable("key") String key) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + UserSettings.class.getSimpleName()).And("key", key));

        BaseFieldSet fieldSet = new BaseFieldSet();
        fieldSet.setFields(Set.of(
                UserSettings._id,
                UserSettings._key,
                UserSettings._value,
                UserSettings._entityId,
                UserSettings._createdAt,
                UserSettings._updatedAt,
                UserSettings._type
        ));
        UserSettingsQuery query = this.queryFactory.query(UserSettingsQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).keys(key);
        UserSettings model = this.builderFactory.builder(UserSettingsBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));

        this.auditService.track(AuditableAction.User_Settings_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("key", key)
        ));

        return model;
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = UserSettingsPersist.UserSettingsPersistValidator.ValidatorName, argumentName = "model")
    public UserSettings Persist(@RequestBody UserSettingsPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + UserSettings.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

        UserSettings persisted = this.settingsService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.User_Settings_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

}
