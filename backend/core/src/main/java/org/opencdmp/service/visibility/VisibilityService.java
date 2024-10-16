package org.opencdmp.service.visibility;

import java.util.Map;

public interface VisibilityService {
	boolean isVisible(String id, Integer ordinal);

	Map<FieldKey, Boolean> getVisibilityStates();
}
