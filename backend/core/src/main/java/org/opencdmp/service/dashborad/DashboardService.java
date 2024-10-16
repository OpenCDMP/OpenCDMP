package org.opencdmp.service.dashborad;

import org.opencdmp.model.DashboardStatistics;
import org.opencdmp.model.RecentActivityItem;
import org.opencdmp.model.RecentActivityItemLookup;

import javax.management.InvalidApplicationException;
import java.util.List;

public interface DashboardService {
    List<RecentActivityItem> getMyRecentActivityItems(RecentActivityItemLookup model) throws InvalidApplicationException;

    DashboardStatistics getPublicDashboardStatistics();

    DashboardStatistics getMyDashboardStatistics() throws InvalidApplicationException;
}
