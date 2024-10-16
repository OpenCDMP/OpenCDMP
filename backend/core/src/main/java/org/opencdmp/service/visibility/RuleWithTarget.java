package org.opencdmp.service.visibility;

import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.descriptiontemplate.RuleEntity;

import java.time.Instant;

public class RuleWithTarget{
	private final String target;
	private final String source;
	private final String textValue;
	private final Instant dateValue;
	private final Boolean booleanValue;
	private final FieldEntity fieldEntity;

	public RuleWithTarget(String source, RuleEntity rule, FieldEntity fieldEntity) {
		this.target = rule.getTarget();
		this.source = source;
		this.fieldEntity = fieldEntity;
		this.textValue = rule.getTextValue();
		this.dateValue = rule.getDateValue();
		this.booleanValue = rule.getBooleanValue();
	}

	public String getTarget() {
		return this.target;
	}

	public String getSource() {
		return this.source;
	}

	public String getTextValue() {
		return this.textValue;
	}

	public Instant getDateValue() {
		return this.dateValue;
	}

	public Boolean getBooleanValue() {
		return this.booleanValue;
	}

	public FieldEntity getFieldEntity() {
		return this.fieldEntity;
	}
}
