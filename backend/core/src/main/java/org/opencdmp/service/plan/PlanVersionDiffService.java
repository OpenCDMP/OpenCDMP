package org.opencdmp.service.plan;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import org.opencdmp.model.plan.versiondiff.PlanVersionDiff;

import java.util.UUID;

public interface PlanVersionDiffService {

    PlanVersionDiff diff(UUID planId, UUID otherPlanId) throws MyForbiddenException, MyApplicationException, MyNotFoundException;
}
