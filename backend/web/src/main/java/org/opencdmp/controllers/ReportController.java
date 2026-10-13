package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.commons.enums.ReportSubjectType;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.model.report.Report;
import org.opencdmp.model.report.ReportOutline;
import org.opencdmp.model.report.ReportQuestionSearch;
import org.opencdmp.query.lookup.ReportLookup;
import org.opencdmp.query.lookup.ReportQuestionSearchLookup;
import org.opencdmp.service.report.ReportService;
import org.opencdmp.service.responseutils.ResponseUtilsService;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/report")
@Tag(name = "Reports", description = "Aggregate the answers given in plans and assets")
@SwaggerCommonErrorResponses
public class ReportController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReportController.class));

    private final ReportService reportService;
    private final ResponseUtilsService responseUtilsService;
    private final AuditService auditService;

    public ReportController(ReportService reportService, ResponseUtilsService responseUtilsService, AuditService auditService) {
        this.reportService = reportService;
        this.responseUtilsService = responseUtilsService;
        this.auditService = auditService;
    }

    @GetMapping("outline/description-template/{groupId}")
    @OperationWithTenantHeader(summary = "Fetch the numbered question outline of an asset template lineage",
            description = "Returns the sections, fieldsets and fields of the newest version of the template group, numbered the way the template editor numbers them, so a report can refer to a question by the number its authors use.",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = ReportOutline.class))))
    @Swagger404
    public ReportOutline outlineForDescriptionTemplate(
            @Parameter(name = "groupId", description = "The group id of the asset template", example = "7ae1c3d3-91a9-4315-9aec-b0f173010f2a", required = true) @PathVariable("groupId") UUID groupId,
            @Parameter(name = "installationWide", description = "Look the template up across every tenant. Requires the installation report permission") @RequestParam(value = "installationWide", required = false) Boolean installationWide
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("outline " + ReportOutline.class.getSimpleName()).And("groupId", groupId));

        ReportOutline model = this.reportService.outline(ReportSubjectType.Description, groupId, installationWide);

        this.auditService.track(AuditableAction.Report_Outline, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("groupId", groupId),
                new AbstractMap.SimpleEntry<String, Object>("installationWide", Boolean.TRUE.equals(installationWide))
        ));

        return model;
    }

    @GetMapping("outline/plan-blueprint/{groupId}")
    @OperationWithTenantHeader(summary = "Fetch the numbered question outline of a plan blueprint lineage",
            description = "The blueprint counterpart of the asset template outline. System fields (title, description, language, contact, access rights) are left out: they are plan columns the report already offers as filters, breakdowns and export columns.",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = ReportOutline.class))))
    @Swagger404
    public ReportOutline outlineForPlanBlueprint(
            @Parameter(name = "groupId", description = "The group id of the plan blueprint", example = "744476ee-1b3c-4a3f-b0a1-4b1e7a2f9c31", required = true) @PathVariable("groupId") UUID groupId,
            @Parameter(name = "installationWide", description = "Look the blueprint up across every tenant. Requires the installation report permission") @RequestParam(value = "installationWide", required = false) Boolean installationWide
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("outline " + ReportOutline.class.getSimpleName()).And("groupId", groupId).And("subject", ReportSubjectType.Plan));

        ReportOutline model = this.reportService.outline(ReportSubjectType.Plan, groupId, installationWide);

        this.auditService.track(AuditableAction.Report_Outline, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("groupId", groupId),
                new AbstractMap.SimpleEntry<String, Object>("installationWide", Boolean.TRUE.equals(installationWide))
        ));

        return model;
    }

    @PostMapping("questions/search")
    @OperationWithTenantHeader(summary = "Find questions across every asset template and plan blueprint",
            description = "Matches the text against question numbering, titles and field labels of the current version of each template and blueprint lineage, and against the semantic tags their fields carry. With an empty text it lists the semantics in use, which are the questions that can be reported across templates.",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = ReportQuestionSearch.class))))
    @Swagger400
    public ReportQuestionSearch searchQuestions(@RequestBody ReportQuestionSearchLookup lookup) throws MyApplicationException, MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("searching " + ReportQuestionSearch.class.getSimpleName()).And("like", lookup != null ? lookup.getLike() : null));

        ReportQuestionSearch model = this.reportService.searchQuestions(lookup);

        this.auditService.track(AuditableAction.Report_SearchQuestions, "lookup", lookup);

        return model;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Count how the selected questions were answered",
            description = "Reads the answers of every asset matching the filters and returns, per question, how many subjects answered, skipped or were never asked it, plus the distribution of the values and an optional breakdown by status, plan or another answer.",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = Report.class))))
    @Swagger400
    public Report query(@RequestBody ReportLookup lookup) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug("querying {}", Report.class.getSimpleName());

        Report model = this.reportService.query(lookup);

        this.auditService.track(AuditableAction.Report_Query, "lookup", lookup);

        return model;
    }

    @PostMapping("export/csv")
    @OperationWithTenantHeader(summary = "Export the answers behind a report as a .csv file",
            description = "Same selection as the query endpoint, written one row per subject with the parent plan, the statuses and one column per question, for further analysis in a spreadsheet.",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger400
    public ResponseEntity<byte[]> exportCsv(@RequestBody ReportLookup lookup) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException {
        logger.debug("exporting {}", Report.class.getSimpleName());

        byte[] bytes = this.reportService.exportCsv(lookup);

        this.auditService.track(AuditableAction.Report_ExportCsv, "lookup", lookup);

        return this.responseUtilsService.buildResponseFileFromText(new String(bytes, StandardCharsets.UTF_8), "answers_" + LocalDate.now() + ".csv");
    }
}
