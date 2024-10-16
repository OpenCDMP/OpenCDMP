package org.opencdmp.model.actionconfirmation;

import org.opencdmp.commons.enums.PlanUserRole;

import java.util.UUID;

public class PlanInvitation {

	private String email;
	public static final String _email = "email";

	private UUID planId;
	public static final String _planId = "planId";

	private UUID sectionId;
	public static final String _sectionId = "sectionId";

	private PlanUserRole role;
	public static final String _role = "role";


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UUID getPlanId() {
		return planId;
	}

	public void setPlanId(UUID planId) {
		this.planId = planId;
	}

	public UUID getSectionId() {
		return sectionId;
	}

	public void setSectionId(UUID sectionId) {
		this.sectionId = sectionId;
	}

	public PlanUserRole getRole() {
		return role;
	}

	public void setRole(PlanUserRole role) {
		this.role = role;
	}
}
