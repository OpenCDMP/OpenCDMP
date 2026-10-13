package org.opencdmp.integrationevent.outbox.dashboardtouched;


public interface DashboardTouchedEventHandler {

	void handle(DashboardTouchedEvent event);

}
