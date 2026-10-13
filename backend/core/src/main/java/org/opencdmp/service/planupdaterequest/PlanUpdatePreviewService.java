package org.opencdmp.service.planupdaterequest;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.planupdaterequest.preview.PlanUpdatePreview;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface PlanUpdatePreviewService {

    PlanUpdatePreview preview(UUID planUpdateRequestId) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    Description descriptionFromPreview(UUID planUpdateRequestId, String datasetId, UUID planDescriptionTemplateId) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;
}
