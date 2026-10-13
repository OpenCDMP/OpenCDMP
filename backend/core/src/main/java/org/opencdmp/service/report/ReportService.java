package org.opencdmp.service.report;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import org.opencdmp.commons.enums.ReportSubjectType;
import org.opencdmp.model.report.Report;
import org.opencdmp.model.report.ReportOutline;
import org.opencdmp.model.report.ReportQuestionSearch;
import org.opencdmp.query.lookup.ReportLookup;
import org.opencdmp.query.lookup.ReportQuestionSearchLookup;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.UUID;

public interface ReportService {

    ReportOutline outline(ReportSubjectType subject, UUID groupId, Boolean installationWide) throws MyForbiddenException, MyNotFoundException, MyApplicationException, InvalidApplicationException;

    ReportQuestionSearch searchQuestions(ReportQuestionSearchLookup lookup) throws MyForbiddenException, MyApplicationException, InvalidApplicationException;

    Report query(ReportLookup lookup) throws MyForbiddenException, MyNotFoundException, MyApplicationException, InvalidApplicationException;

    byte[] exportCsv(ReportLookup lookup) throws MyForbiddenException, MyNotFoundException, MyApplicationException, InvalidApplicationException, IOException;
}
