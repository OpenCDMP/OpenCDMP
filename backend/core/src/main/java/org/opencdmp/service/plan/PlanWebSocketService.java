package org.opencdmp.service.plan;

import org.opencdmp.model.user.User;

import java.util.List;
import java.util.UUID;

public interface PlanWebSocketService {

    List<User> getUsersSubscribedToPlan(UUID planId);

    User getUserFromSubjectId(String credential);
}
