package org.opencdmp.service.elastic;

import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanEntity;

import javax.management.InvalidApplicationException;
import java.io.IOException;

public interface ElasticService {
    boolean enabled();
    boolean existsPlanIndex() throws IOException;

    boolean existsDescriptionIndex() throws IOException;

    void ensurePlanIndex() throws IOException;

    void ensureDescriptionIndex() throws IOException;

    void ensureIndexes() throws IOException;

    void persistPlan(PlanEntity plan) throws IOException;

    void deletePlan(PlanEntity plan) throws IOException;

    void persistDescription(DescriptionEntity description) throws IOException, InvalidApplicationException;

    void deleteDescription(DescriptionEntity description) throws IOException, InvalidApplicationException;

	void deletePlanIndex() throws IOException;

    void deleteDescriptionIndex() throws IOException;

	void resetPlanIndex() throws IOException, InvalidApplicationException;

    void resetDescriptionIndex() throws IOException, InvalidApplicationException;
}
