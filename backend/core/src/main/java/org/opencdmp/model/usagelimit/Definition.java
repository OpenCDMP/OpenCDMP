package org.opencdmp.model.usagelimit;

import org.opencdmp.commons.enums.UsageLimitPeriodicityRange;

public class Definition {

	private Boolean hasPeriodicity;
	public final static String _hasPeriodicity = "hasPeriodicity";

	private UsageLimitPeriodicityRange periodicityRange;
	public final static String _periodicityRange = "periodicityRange";

	public Boolean getHasPeriodicity() {
		return hasPeriodicity;
	}

	public void setHasPeriodicity(Boolean hasPeriodicity) {
		this.hasPeriodicity = hasPeriodicity;
	}

	public UsageLimitPeriodicityRange getPeriodicityRange() {
		return periodicityRange;
	}

	public void setPeriodicityRange(UsageLimitPeriodicityRange periodicityRange) {
		this.periodicityRange = periodicityRange;
	}
}
