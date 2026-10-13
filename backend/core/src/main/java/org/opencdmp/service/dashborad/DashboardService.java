package org.opencdmp.service.dashborad;

import org.opencdmp.model.DashboardStatistics;
import org.opencdmp.model.RecentActivityItem;
import org.opencdmp.model.RecentActivityItemLookup;
import org.opencdmp.model.result.QueryResult;

import javax.management.InvalidApplicationException;

public interface DashboardService {
    QueryResult<RecentActivityItem> getMyRecentActivityItems(RecentActivityItemLookup model) throws InvalidApplicationException;

    DashboardStatistics getPublicDashboardStatistics();

    DashboardStatistics getMyDashboardStatistics() throws InvalidApplicationException;
}
