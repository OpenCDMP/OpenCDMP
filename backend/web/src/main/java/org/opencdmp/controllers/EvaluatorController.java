package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.transaction.Transactional;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration;
import org.opencdmp.evaluatorbase.models.misc.RankModel;
import org.opencdmp.model.evaluator.EvaluateRequestModel;
import org.opencdmp.model.file.ExportRequestModel;
import org.opencdmp.service.evaluator.EvaluatorService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(value = "/api/evaluator")
@SwaggerCommonErrorResponses
public class EvaluatorController {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EvaluatorController.class));

    private final EvaluatorService evaluatorService;

    private final AuditService auditService;

    @Autowired
    public EvaluatorController(EvaluatorService evaluatorService, AuditService auditService){
        this.evaluatorService = evaluatorService;
        this.auditService = auditService;
    }

    @GetMapping("/available")
    @OperationWithTenantHeader(summary = "Fetch all evaluators", description = SwaggerHelpers.Evaluator.endpoint_get_available_evaluators,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = EvaluatorConfiguration.class
                            )))
            ))
    public List<EvaluatorConfiguration> getAvailableConfigurations() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("getAvailableConfigurations"));

        List<EvaluatorConfiguration> model = this.evaluatorService.getAvailableEvaluators();
        this.auditService.track(AuditableAction.Evaluator_GetAvailableConfigurations);

        return model;
    }
    @Transactional
    @PostMapping("/rank-plan")
    @OperationWithTenantHeader(summary = "Rank a plan", description = SwaggerHelpers.Evaluator.endpoint_rank_plans,
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public ResponseEntity<RankModel> rankPlan(@RequestBody EvaluateRequestModel requestModel) throws Exception {
        logger.debug(new MapLogEntry("ranking plan"));

        RankModel rankModel = this.evaluatorService.rankPlan(requestModel.getId(), requestModel.getEvaluatorId(), requestModel.getFormat(), false);

        return new ResponseEntity<>(rankModel, HttpStatus.OK);
    }
    @PostMapping("/rank-description")
    @OperationWithTenantHeader(summary = "Rank a description", description = SwaggerHelpers.Evaluator.endpoint_rank_descriptions,
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public ResponseEntity<RankModel> rankDescription(@RequestBody EvaluateRequestModel requestModel) throws Exception {
        logger.debug(new MapLogEntry("ranking description"));

        RankModel rankModel = this.evaluatorService.rankDescription(requestModel.getId(), requestModel.getEvaluatorId(), requestModel.getFormat(), true);

        return new ResponseEntity<>(rankModel, HttpStatus.OK);
    }

    @GetMapping("/{evaluatorId}/logo")
    @OperationWithTenantHeader(summary = "Fetch a specific evaluator logo by id", description = SwaggerHelpers.Deposit.endpoint_get_logo,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = String.class
                    ))
            ))
    @Swagger404
    public String getLogo(
            @Parameter(name = "evaluatorId", description = "The id of an evaluator of which to fetch the logo", example = "zenodo", required = true) @PathVariable("evaluatorId") String evaluatorId
    ) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("get logo" + EvaluatorConfiguration.class.getSimpleName()).And("evaluatorId", evaluatorId));

        String logo = this.evaluatorService.getLogo(evaluatorId);
        this.auditService.track(AuditableAction.Deposit_GetLogo, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("evaluatorId", evaluatorId)
        ));

        return logo;
    }
}
