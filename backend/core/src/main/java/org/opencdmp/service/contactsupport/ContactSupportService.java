package org.opencdmp.service.contactsupport;

import org.opencdmp.model.DashboardStatistics;
import org.opencdmp.model.RecentActivityItem;
import org.opencdmp.model.RecentActivityItemLookup;
import org.opencdmp.model.persist.ContactSupportPersist;
import org.opencdmp.model.persist.PublicContactSupportPersist;

import javax.management.InvalidApplicationException;
import java.util.List;

public interface ContactSupportService {
    void sendContactEmail(ContactSupportPersist model) throws InvalidApplicationException;
    void sendPublicContactEmail(PublicContactSupportPersist model) throws InvalidApplicationException;
}
