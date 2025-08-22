package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.censorship.EntityDoiCensor;
import org.opencdmp.model.censorship.deposit.DepositConfigurationCensor;
import org.opencdmp.model.deposit.DepositAuthMethodResult;
import org.opencdmp.model.deposit.DepositConfiguration;
import org.opencdmp.model.persist.deposit.DepositRequest;
import org.opencdmp.service.deposit.DepositService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/deposit/")
@Tag(name = "Deposit", description = "Manage deposit repositories, make deposits", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "12")))
@SwaggerCommonErrorResponses
public class DepositController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DepositController.class));

    private final DepositService depositService;

    private final CensorFactory censorFactory;

    private final AuditService auditService;

    private final MessageSource messageSource;

    @Autowired
    public DepositController(DepositService depositService, CensorFactory censorFactory, AuditService auditService, MessageSource messageSource) {
        this.depositService = depositService;
        this.censorFactory = censorFactory;
        this.auditService = auditService;
        this.messageSource = messageSource;
    }

    @GetMapping("/repositories/available")
    @OperationWithTenantHeader(summary = "Fetch all repositories", description = SwaggerHelpers.Deposit.endpoint_get_available_repos,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = DepositConfiguration.class
                            )))
            ),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "1")))
    public List<DepositConfiguration> getAvailableRepos(
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema( type = "array", example = SwaggerHelpers.Deposit.endpoint_get_available_repos_example, allowableValues = SwaggerHelpers.Deposit.endpoint_query_field_set_example )) FieldSet fieldSet
    ) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("retrieving" + DepositConfiguration.class.getSimpleName()).And("fields", fieldSet));

        this.censorFactory.censor(DepositConfigurationCensor.class).censor(fieldSet, null);

        List<DepositConfiguration> model = this.depositService.getAvailableConfigurations(fieldSet);
        this.auditService.track(AuditableAction.Deposit_GetAvailableRepositories, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("/deposit")
    @OperationWithTenantHeader(summary = "Make a plan deposit request", description = SwaggerHelpers.Deposit.endpoint_deposit,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = EntityDoi.class
                    ))
            ),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "3")))
    @Swagger400
    @Transactional
    @ValidationFilterAnnotation(validator = DepositRequest.DepositRequestValidator.ValidatorName, argumentName = "model")
    public EntityDoi deposit(@RequestBody DepositRequest model) throws Exception {
        logger.debug(new MapLogEntry("persisting" + DepositRequest.class.getSimpleName()).And("model", model).And("fieldSet", model.getProject()));
        this.censorFactory.censor(EntityDoiCensor.class).censor(model.getProject(), null);

        EntityDoi persisted = this.depositService.deposit(model);
        this.auditService.track(AuditableAction.Deposit_Deposit, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return persisted;
    }

    @GetMapping("/repositories/{repositoryId}")
    @OperationWithTenantHeader(summary = "Fetch a specific deposit repository by id", description = SwaggerHelpers.Deposit.endpoint_get_repository,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DepositConfiguration.class
                    ))
            ),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "2")))
    @Swagger404
    public DepositConfiguration getRepository(
            @Parameter(name = "repositoryId", description = "The id of a repository to fetch", example = "zenodo", required = true) @PathVariable("repositoryId") String repositoryId,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema( type = "array", example = SwaggerHelpers.Deposit.endpoint_get_available_repos_example, allowableValues = SwaggerHelpers.Deposit.endpoint_query_field_set_example )) FieldSet fieldSet
    ) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("retrieving" + DepositConfiguration.class.getSimpleName()).And("fields", fieldSet).And("repositoryId", repositoryId));

        this.censorFactory.censor(DepositConfigurationCensor.class).censor(fieldSet, null);

        List<DepositConfiguration> models = this.depositService.getAvailableConfigurations(fieldSet);
        org.opencdmp.model.deposit.DepositConfiguration model = models.stream().filter(x -> x.getRepositoryId().equals(repositoryId)).findFirst().orElse(null);
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, org.opencdmp.model.deposit.DepositConfiguration.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Deposit_GetRepository, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("repositoryId", repositoryId),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("/repositories/{repositoryId}/logo")
    @OperationWithTenantHeader(summary = "Fetch a specific deposit repository logo by id", description = SwaggerHelpers.Deposit.endpoint_get_logo,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = String.class
                    ))
            ),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "4")))
    @Swagger404
    public String getLogo(
            @Parameter(name = "repositoryId", description = "The id of a repository of which to fetch the logo", example = "zenodo", required = true) @PathVariable("repositoryId") String repositoryId
    ) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("get logo" + DepositConfiguration.class.getSimpleName()).And("repositoryId", repositoryId));

        String logo = this.depositService.getLogo(repositoryId);
        this.auditService.track(AuditableAction.Deposit_GetLogo, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("repositoryId", repositoryId)
        ));

        return logo;
    }

    @GetMapping("/repositories/{repositoryId}/get-available-auth-methods")
    @OperationWithTenantHeader(summary = "Fetch available user authentication methods for a deposit repository", description = SwaggerHelpers.Deposit.endpoint_get_auth_methods,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = String.class
                    ))
            ),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "5")))
    @Swagger404
    public DepositAuthMethodResult getAvailableAuthMethods(
            @Parameter(name = "repositoryId", description = "The id of a repository", example = "zenodo", required = true) @PathVariable("repositoryId") String repositoryId
    ) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("get auth methods" + DepositConfiguration.class.getSimpleName()).And("repositoryId", repositoryId));

        DepositAuthMethodResult result = this.depositService.getAuthMethods(repositoryId);
        this.auditService.track(AuditableAction.Deposit_GetAuthMethods, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("repositoryId", repositoryId)
        ));

        return result;
    }
}
