package org.opencdmp.service.maintenance;

import javax.management.InvalidApplicationException;

public interface MaintenanceService {


	void sendUserTouchEvents() throws InvalidApplicationException;

	void sendTenantTouchEvents() throws InvalidApplicationException;

	void sendPlanTouchEvents() throws InvalidApplicationException;

	void sendDescriptionTouchEvents() throws InvalidApplicationException;

	void sendPlanAccountingEntriesEvents() throws InvalidApplicationException;

	void sendBlueprintAccountingEntriesEvents() throws InvalidApplicationException;

	void sendDescriptionAccountingEntriesEvents() throws InvalidApplicationException;

	void sendDescriptionTemplateAccountingEntriesEvents() throws InvalidApplicationException;

	void sendDescriptionTemplateTypeAccountingEntriesEvents() throws InvalidApplicationException;

	void sendPrefillingSourceAccountingEntriesEvents() throws InvalidApplicationException;

	void sendReferenceTypeAccountingEntriesEvents() throws InvalidApplicationException;

	void sendUserAccountingEntriesEvents() throws InvalidApplicationException;

	void sendPlanStatusAccountingEntriesEvents() throws InvalidApplicationException;

	void sendDescriptionStatusAccountingEntriesEvents() throws InvalidApplicationException;

	void sendEvaluationPlanAccountingEntriesEvents() throws InvalidApplicationException;

	void sendEvaluationDescriptionAccountingEntriesEvents() throws InvalidApplicationException;

	void sendReferenceAccountingEntriesEvents() throws InvalidApplicationException;

	void sendLanguageAccountingEntriesEvents() throws InvalidApplicationException;

	void sendIndicatorCreateEntryEvents() throws InvalidApplicationException;

	void sendIndicatorResetEntryEvents() throws InvalidApplicationException;

	void sendIndicatorAccessEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointPlanEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointDescriptionEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointReferenceEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointUserEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointPlanBlueprintEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointDescriptionTemplateEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointTenantEntryEvents() throws InvalidApplicationException;

	void setPlanBlueprintCanEditDescriptionTemplates() throws InvalidApplicationException;

}
