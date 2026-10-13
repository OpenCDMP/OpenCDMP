package org.opencdmp.model.descriptiontemplate;

import java.time.Instant;

public class Rule {

	public final static String _target = "target";
	private String target;

	private String textValue;
	public static final String _textValue = "textValue";

	private Instant dateValue;
	public static final String _dateValue = "dateValue";

	private Boolean booleanValue;
	public static final String _booleanValue = "booleanValue";

	private Double numberValue;
	public static final String _numberValue = "numberValue";

	public String getTarget() {
		return this.target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTextValue() {
		return this.textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public Instant getDateValue() {
		return this.dateValue;
	}

	public void setDateValue(Instant dateValue) {
		this.dateValue = dateValue;
	}

	public Boolean getBooleanValue() {
		return this.booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public Double getNumberValue() {
		return numberValue;
	}

	public void setNumberValue(Double numberValue) {
		this.numberValue = numberValue;
	}
}
