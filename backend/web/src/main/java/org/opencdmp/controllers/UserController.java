package org.opencdmp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.UserEntity;
import org.opencdmp.model.PlanAssociatedUser;
import org.opencdmp.model.UserRole;
import org.opencdmp.model.builder.PlanAssociatedUserBuilder;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.censorship.PlanAssociatedUserCensor;
import org.opencdmp.model.censorship.UserCensor;
import org.opencdmp.model.persist.UserMergeRequestPersist;
import org.opencdmp.model.persist.UserPersist;
import org.opencdmp.model.persist.UserRolePatchPersist;
import org.opencdmp.model.persist.UserTenantUsersInviteRequest;
import org.opencdmp.model.persist.actionconfirmation.RemoveCredentialRequestPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.model.user.User;
import org.opencdmp.query.UserQuery;
import org.opencdmp.query.lookup.UserLookup;
import org.opencdmp.service.responseutils.ResponseUtilsService;
import org.opencdmp.service.user.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/user")
@Tag(name = "Users", description = "Manage users")
@SwaggerCommonErrorResponses
public class UserController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final UserService userTypeService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final UserScope userScope;

    private final MessageSource messageSource;

    private final ResponseUtilsService responseUtilsService;

    public UserController(
            BuilderFactory builderFactory,
            AuditService auditService,
            UserService userTypeService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            UserScope userScope,
            MessageSource messageSource,
            ResponseUtilsService responseUtilsService) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.userTypeService = userTypeService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.userScope = userScope;
        this.messageSource = messageSource;
        this.responseUtilsService = responseUtilsService;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all users", description = SwaggerHelpers.User.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.User.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.User.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = User.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.User.endpoint_query_response_example
            ))))
    public QueryResult<User> query(@RequestBody UserLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", User.class.getSimpleName());

        this.censorFactory.censor(UserCensor.class).censor(lookup.getProject(), null);

        UserQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<UserEntity> data = query.collectAs(lookup.getProject());
        List<User> models = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.User_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @PostMapping("plan-associated/query")
    @OperationWithTenantHeader(summary = "Query all plan associated users", description = SwaggerHelpers.User.endpoint_query_plan_associated, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.User.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.User.endpoint_query_plan_associated_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = PlanAssociatedUser.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.User.endpoint_query_plan_associated_response_example
            ))))
    public QueryResult<PlanAssociatedUser> queryPlanAssociated(@RequestBody UserLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", User.class.getSimpleName());

        this.censorFactory.censor(PlanAssociatedUserCensor.class).censor(lookup.getProject(), null);

        UserQuery query = lookup.enrich(this.queryFactory).planAssociated(true).isActive(IsActive.Active);

        List<UserEntity> data = query.collectAs(lookup.getProject());
        List<PlanAssociatedUser> models = this.builderFactory.builder(PlanAssociatedUserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.User_PlanAssociatedQuery, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific user by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = User.class
                    ))
            ))
    @Swagger404
    public User get(
            @Parameter(name = "id", description = "The id of a user to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + User.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(UserCensor.class).censor(fieldSet, id);

        UserQuery query = this.queryFactory.query(UserQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        User model = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.User_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("/by-email/{email}")
    @OperationWithTenantHeader(summary = "Fetch a specific user by email", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = User.class
                    ))
            ))
    @Swagger404
    public User get(
            @Parameter(name = "email", description = "The email of a user to fetch", example = "opencdmp@cite.gr", required = true) @PathVariable("email") String email,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + User.class.getSimpleName()).And("email", email).And("fields", fieldSet));

        this.censorFactory.censor(UserCensor.class).censor(fieldSet, null);

        UserQuery query = this.queryFactory.query(UserQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).emails(email);
        User model = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{email, User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.User_LookupByEmail, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("email", email),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("/export/csv/{hasTenantAdminMode}")
    @OperationWithTenantHeader(summary = "Export users in a .csv file", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public ResponseEntity<byte[]> exportCsv(
            @Parameter(name = "hasTenantAdminMode", description = "Controls whether to fetch users as a tenant admin or not", example = "false", required = true) @PathVariable("hasTenantAdminMode") Boolean hasTenantAdminMode
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, IOException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export" + User.class.getSimpleName()).And("hasTenantAdminMode", hasTenantAdminMode));

//        this.censorFactory.censor(UserCensor.class).censor(fieldSet, null);
        byte[] bytes = this.userTypeService.exportCsv(hasTenantAdminMode);

        this.auditService.track(AuditableAction.User_ExportCsv, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("hasTenantAdminMode", hasTenantAdminMode)
        ));

        return this.responseUtilsService.buildResponseFileFromText(new String(bytes, StandardCharsets.UTF_8), "Users_dump.csv");
    }

    @GetMapping("mine")
    @OperationWithTenantHeader(summary = "Fetch information for the logged in user", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = User.class
                    ))
            ))
    @Swagger404
    public User getMine(
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving me" + User.class.getSimpleName()).And("fields", fieldSet));

        this.censorFactory.censor(UserCensor.class).censor(fieldSet, this.userScope.getUserId());

        UserQuery query = this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScope.getUserId()).authorize(AuthorizationFlags.AllExceptPublic);
        User model = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{this.userScope.getUserId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.User_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("mine/language/{language}")
    @OperationWithTenantHeader(summary = "Update the language for the logged in user", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Transactional
    public void updateLanguageMine(
            @Parameter(name = "language", description = "The updated language", example = "en", required = true) @PathVariable("language") String language
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JsonProcessingException {
        logger.debug(new MapLogEntry("persisting" + User.class.getSimpleName()).And("language", language));
        this.userTypeService.updateLanguageMine(language);

        this.auditService.track(AuditableAction.User_LanguageMine, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("language", language)
        ));
    }

    @GetMapping("mine/timezone/{timezone}")
    @OperationWithTenantHeader(summary = "Update the timezone for the logged in user", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Transactional
    public void updateTimezoneMine(
            @Parameter(name = "timezone", description = "The updated timezone", example = "Europe/Budapest", required = true) @PathVariable("timezone") String timezone
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JsonProcessingException {
        logger.debug(new MapLogEntry("persisting" + User.class.getSimpleName()).And("timezone", timezone));
        this.userTypeService.updateTimezoneMine(timezone);

        this.auditService.track(AuditableAction.User_TimezoneMine, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("timezone", timezone)
        ));
    }

    @GetMapping("mine/culture/{culture}")
    @OperationWithTenantHeader(summary = "Update the culture for the logged in user", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Transactional
    public void updateCultureMine(
            @Parameter(name = "culture", description = "The updated culture", example = "en-US", required = true) @PathVariable("culture") String culture
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JsonProcessingException {
        logger.debug(new MapLogEntry("persisting" + User.class.getSimpleName()).And("culture", culture));
        this.userTypeService.updateCultureMine(culture);

        this.auditService.track(AuditableAction.User_CultureMine, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("culture", culture)
        ));
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing user", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = User.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = UserPersist.UserPersistValidator.ValidatorName, argumentName = "model")
    public User persist(
            @RequestBody UserPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + User.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        User persisted = this.userTypeService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.User_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @PostMapping("persist/roles")
    @OperationWithTenantHeader(summary = "Update user roles", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = User.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = UserRolePatchPersist.UserRolePatchPersistValidator.ValidatorName, argumentName = "model")
    public User persistRoles(
            @RequestBody UserRolePatchPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + UserRole.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        User persisted = this.userTypeService.patchRoles(model, fieldSet);

        this.auditService.track(AuditableAction.User_PersistRoles, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a user by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of a user to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + User.class.getSimpleName()).And("id", id));

        this.userTypeService.deleteAndSave(id);

        this.auditService.track(AuditableAction.User_Delete, "id", id);
    }

    @PostMapping("mine/merge-account-request")
    @OperationWithTenantHeader(summary = "Merge user accounts", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = UserMergeRequestPersist.UserMergeRequestPersistValidator.ValidatorName, argumentName = "model")
    public Boolean mergeAccount(@RequestBody UserMergeRequestPersist model) throws InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("merge account to user").And("email", model));

        this.userTypeService.sendMergeAccountConfirmation(model);

        this.auditService.track(AuditableAction.User_MergeRequest, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return true;
    }

    @GetMapping("mine/confirm-merge-account/token/{token}")
    @OperationWithTenantHeader(summary = "Confirm the merge of user accounts", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Transactional
    public Boolean confirmMergeAccount(
            @Parameter(name = "token", description = "The token for the action", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("token") String token
    ) throws InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("confirm merge account to user").And("token", token));

        this.userTypeService.confirmMergeAccount(token);

        this.auditService.track(AuditableAction.User_MergeConfirm, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("token", token)
        ));

        return true;
    }

    @GetMapping("mine/allow-merge-account/token/{token}")
    @OperationWithTenantHeader(summary = "Allow the merge of user accounts", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public Boolean getUserTokenPermission(
            @Parameter(name = "token", description = "The token for the action", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("token") String token
    ) throws InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("allow merge account to user").And("token", token));

        this.auditService.track(AuditableAction.User_AllowMergeAccount);

        return this.userTypeService.doesTokenBelongToLoggedInUser(token);
    }

    @PostMapping("mine/remove-credential-request")
    @OperationWithTenantHeader(summary = "Remove user credentials", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger400
    @Transactional
    @ValidationFilterAnnotation(validator = RemoveCredentialRequestPersist.RemoveCredentialRequestPersistValidator.ValidatorName, argumentName = "model")
    public Boolean removeCredentialAccount(@RequestBody RemoveCredentialRequestPersist model) throws InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("remove credential request to user").And("model", model));

        this.userTypeService.sendRemoveCredentialConfirmation(model);

        this.auditService.track(AuditableAction.User_RemoveCredentialRequest, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("email", model)
        ));

        return true;
    }

    @GetMapping("mine/confirm-remove-credential/token/{token}")
    @OperationWithTenantHeader(summary = "Confirm the removal of user credentials", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Transactional
    public Boolean confirmRemoveCredentialAccount(
            @Parameter(name = "token", description = "The token for the action", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("token") String token
    ) throws InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("confirm remove credential to user").And("token", token));

        this.userTypeService.confirmRemoveCredential(token);

        this.auditService.track(AuditableAction.User_RemoveCredentialConfirm, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", token)
        ));

        return true;
    }

    @PostMapping("invite-users-to-tenant")
    @OperationWithTenantHeader(summary = "Invite users to a tenant", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger400
    @Transactional
    @ValidationFilterAnnotation(validator = UserTenantUsersInviteRequest.UserTenantUsersInviteRequestValidator.ValidatorName, argumentName = "model")
    public Boolean inviteUsersToTenant(@RequestBody UserTenantUsersInviteRequest users) throws InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("send tenant invitation to users").And("users", users));

        this.userTypeService.sendUserToTenantInvitation(users);

        this.auditService.track(AuditableAction.User_InviteToTenant, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("users", users)
        ));

        return true;
    }

    @GetMapping("confirm-invite-user-to-tenant/token/{token}")
    @OperationWithTenantHeader(summary = "Confirm user tenant invitation", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger400
    @Transactional
    public Boolean confirmInviteUserToTenant(
            @Parameter(name = "token", description = "The token for the action", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("token") String token
    ) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("confirm merge account to user").And("token", token));

        this.userTypeService.confirmUserInviteToTenant(token);

        this.auditService.track(AuditableAction.User_InviteToTenantConfirm, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("token", token)
        ));

        return true;
    }
}
