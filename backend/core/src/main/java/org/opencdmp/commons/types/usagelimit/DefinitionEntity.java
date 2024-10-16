package org.opencdmp.commons.types.usagelimit;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.ReferenceFieldDataType;
import org.opencdmp.commons.enums.UsageLimitPeriodicityRange;
import org.opencdmp.commons.types.reference.FieldEntity;

import java.util.List;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefinitionEntity {

	@XmlAttribute(name = "hasPeriodicity")
	private Boolean hasPeriodicity;

	@XmlAttribute(name = "periodicityRange")
	private UsageLimitPeriodicityRange periodicityRange;

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
