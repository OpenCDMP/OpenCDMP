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

	void sendIndicatorCreateEntryEvents() throws InvalidApplicationException;

	void sendIndicatorResetEntryEvents() throws InvalidApplicationException;

	void sendIndicatorAccessEntryEvents() throws InvalidApplicationException;

	void sendIndicatorPointEntryEvents() throws InvalidApplicationException;

}
