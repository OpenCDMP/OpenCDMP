package org.opencdmp.service.evaluation;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.evaluatorbase.models.misc.RankConfig;
import org.opencdmp.evaluatorbase.models.misc.RankModel;
import org.opencdmp.model.evaluation.Evaluation;
import org.opencdmp.model.persist.EvaluationPersist;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface EvaluationService {

    Evaluation persist(EvaluationPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

    void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;

    void persistInternal(RankModel model, RankConfig config, UUID id, EntityType type, String EntityId, UUID createdBy)  throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

}

