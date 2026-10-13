package org.opencdmp.service.report;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.opencdmp.commons.enums.ReportAnswerState;
import org.opencdmp.commons.enums.ReportSubjectType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes the rows behind a report: one line per subject, with the plan and status columns a
 * reader needs to filter further, then each selected question as a value column and a state
 * column.
 *
 * <p>This is the deliverable when a funder asks for the underlying answers rather than the
 * counts — the office that receives it can drop test plans and re-count in a spreadsheet
 * without asking for another database extract.</p>
 */
@Service
public class ReportCsvWriter {

    private static final String STATE_ANSWERED = "Answered";
    private static final String STATE_NOT_ANSWERED = "Not answered";
    private static final String STATE_NOT_APPLICABLE = "Not applicable";
    private static final String STATE_NOT_MIGRATED = "Not readable";
    private static final String VALUE_SEPARATOR = "; ";

    public byte[] write(ReportAnalysis analysis) throws IOException {
        boolean planSubject = ReportSubjectType.Plan.equals(analysis.getSubject());
        List<ReportQuestionSpec> planSpecs = analysis.getPlanSpecs() != null ? analysis.getPlanSpecs() : List.of();

        List<String> headers = new ArrayList<>();
        if (analysis.isInstallationWide()) headers.add("Tenant");
        headers.addAll(List.of(
                "Plan id",
                "Plan",
                "Plan version",
                "Plan status",
                "Plan access",
                "Plan version status"));
        if (!planSubject) {
            headers.add("Asset id");
            headers.add("Asset");
            headers.add("Asset status");
            headers.add("Template version");
        } else {
            headers.add("Blueprint version");
        }
        headers.add("Created");
        headers.add("Finalized");

        for (ReportQuestionSpec spec : analysis.getSpecs()) {
            headers.add(spec.label());
            headers.add(spec.label() + " — state");
        }
        // Answers of the parent plan sit beside the asset's own, so one row carries the
        // whole story: which grant funded it, and where its data is going.
        for (ReportQuestionSpec spec : planSpecs) {
            headers.add("Plan: " + spec.label());
            headers.add("Plan: " + spec.label() + " — state");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, false, StandardCharsets.UTF_8);
        // Excel needs the byte order mark to read the file as UTF-8; without it every
        // accented plan title arrives mangled.
        writer.write('﻿');

        CSVFormat format = CSVFormat.DEFAULT.withHeader(headers.stream().map(ReportCsvWriter::formulaSafe).toArray(String[]::new)).withQuoteMode(QuoteMode.NON_NUMERIC);
        CSVPrinter printer = new CSVPrinter(writer, format);

        for (ReportSubject subject : analysis.getSubjects()) {
            Map<String, ReportValueExtractor.Extraction> extraction = analysis.getExtractions().get(subject.id());
            ReportDefinitionResolver.ParsedVersion version = analysis.getSource().getVersions().get(subject.sourceVersionId());

            List<String> record = new ArrayList<>();
            if (analysis.isInstallationWide()) record.add(nullToEmpty(analysis.getTenantCodes().get(subject.tenantId())));
            record.add(subject.planId() != null ? subject.planId().toString() : "");
            record.add(nullToEmpty(subject.planLabel()));
            record.add(subject.planVersion() != null ? String.valueOf(subject.planVersion()) : "");
            record.add(nullToEmpty(analysis.getPlanStatusNames().get(subject.planStatusId())));
            record.add(subject.planAccessType() != null ? subject.planAccessType().name() : "");
            record.add(subject.planVersionStatus() != null ? subject.planVersionStatus().name() : "");
            if (!planSubject) {
                record.add(subject.id().toString());
                record.add(nullToEmpty(subject.label()));
                record.add(nullToEmpty(analysis.getDescriptionStatusNames().get(subject.statusId())));
            }
            record.add(versionLabel(analysis, version));
            record.add(subject.createdAt() != null ? subject.createdAt().toString() : "");
            record.add(subject.finalizedAt() != null ? subject.finalizedAt().toString() : "");

            for (ReportQuestionSpec spec : analysis.getSpecs()) {
                ReportValueExtractor.Extraction value = extraction != null ? extraction.get(spec.key()) : null;
                record.add(value != null ? String.join(VALUE_SEPARATOR, value.getValues()) : "");
                record.add(stateLabel(value != null ? value.getState() : null));
            }

            Map<String, ReportValueExtractor.Extraction> planExtraction = subject.planId() != null && analysis.getPlanExtractions() != null
                    ? analysis.getPlanExtractions().get(subject.planId())
                    : null;
            for (ReportQuestionSpec spec : planSpecs) {
                ReportValueExtractor.Extraction value = planExtraction != null ? planExtraction.get(spec.key()) : null;
                record.add(value != null ? String.join(VALUE_SEPARATOR, value.getValues()) : "");
                record.add(stateLabel(value != null ? value.getState() : null));
            }

            printer.printRecord(record.stream().map(ReportCsvWriter::formulaSafe).toList());
        }

        printer.flush();
        writer.flush();
        return out.toByteArray();
    }

    /** Across templates the version alone is ambiguous, so the template name comes with it. */
    private static String versionLabel(ReportAnalysis analysis, ReportDefinitionResolver.ParsedVersion version) {
        if (version == null || version.getVersion() == null) return "";
        return analysis.getSource().spansAllGroups() ? version.getLabel() + " v" + version.getVersion() : "v" + version.getVersion();
    }

    private static String stateLabel(ReportAnswerState state) {
        if (state == null) return "";
        return switch (state) {
            case Answered -> STATE_ANSWERED;
            case NotAnswered -> STATE_NOT_ANSWERED;
            case NotApplicable -> STATE_NOT_APPLICABLE;
            case NotMigrated -> STATE_NOT_MIGRATED;
        };
    }

    /**
     * Answers are typed by users, and a spreadsheet runs a cell starting with = + - @ as a
     * formula; a leading quote keeps it text. Plain negative numbers are left alone.
     */
    private static String formulaSafe(String value) {
        if (value == null || value.isEmpty() || "=+-@\t\r".indexOf(value.charAt(0)) < 0) return value;
        if (value.matches("-?\\d+(\\.\\d+)?")) return value;
        return "'" + value;
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
