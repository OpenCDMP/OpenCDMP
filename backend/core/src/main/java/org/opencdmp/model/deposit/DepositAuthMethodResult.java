package org.opencdmp.model.deposit;

import org.opencdmp.commons.enums.DepositAuthMethod;

import java.util.List;

public class DepositAuthMethodResult {

	private boolean hasMyAccountValue;
	private List<DepositAuthMethod> depositAuthInfoTypes;
	private String preselected;
	private String organisationAccountName;

	public String getPreselected() {
		return preselected;
	}

	public void setPreselected(String preselected) {
		this.preselected = preselected;
	}

	public String getOrganisationAccountName() {
		return organisationAccountName;
	}

	public void setOrganisationAccountName(String organisationAccountName) {
		this.organisationAccountName = organisationAccountName;
	}

	public boolean isHasMyAccountValue() {
		return hasMyAccountValue;
	}

	public void setHasMyAccountValue(boolean hasMyAccountValue) {
		this.hasMyAccountValue = hasMyAccountValue;
	}

	public List<DepositAuthMethod> getDepositAuthInfoTypes() {
		return depositAuthInfoTypes;
	}

	public void setDepositAuthInfoTypes(List<DepositAuthMethod> depositAuthInfoTypes) {
		this.depositAuthInfoTypes = depositAuthInfoTypes;
	}
}
