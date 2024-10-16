package org.opencdmp.commons.types.actionconfirmation;

import org.opencdmp.commons.enums.PlanUserRole;
import jakarta.xml.bind.annotation.*;

import java.util.UUID;

@XmlRootElement(name = "plan-invitation")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanInvitationEntity {

	@XmlAttribute(name = "email")
	private String email;

	@XmlAttribute(name = "plan")
	private UUID planId;

	@XmlAttribute(name = "plan-role")
	private PlanUserRole role;

	@XmlAttribute(name = "section")
	private UUID sectionId;

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

	public PlanUserRole getRole() {
		return role;
	}

	public void setRole(PlanUserRole role) {
		this.role = role;
	}

	public UUID getSectionId() {
		return sectionId;
	}

	public void setSectionId(UUID sectionId) {
		this.sectionId = sectionId;
	}
}
