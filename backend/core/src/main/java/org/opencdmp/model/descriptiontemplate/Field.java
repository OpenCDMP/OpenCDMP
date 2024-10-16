package org.opencdmp.model.descriptiontemplate;

import org.opencdmp.commons.enums.FieldValidationType;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;

import java.util.List;

public class Field {

	public final static String _id = "id";
	private String id;

	public final static String _ordinal = "ordinal";
	private Integer ordinal;

	public final static String _semantics = "semantics";
	private List<String> semantics;

	public final static String _defaultValue = "defaultValue";
	private DefaultValue defaultValue;

	public final static String _visibilityRules = "visibilityRules";
	private List<Rule> visibilityRules;

	public final static String _validations = "validations";
	private List<FieldValidationType> validations;

	public final static String _includeInExport = "includeInExport";
	private Boolean includeInExport;

	public final static String _data = "data";
	private BaseFieldData data;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getOrdinal() {
		return this.ordinal;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	public List<String> getSemantics() {
		return this.semantics;
	}

	public void setSemantics(List<String> semantics) {
		this.semantics = semantics;
	}

	public DefaultValue getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(DefaultValue defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<Rule> getVisibilityRules() {
		return this.visibilityRules;
	}

	public void setVisibilityRules(List<Rule> visibilityRules) {
		this.visibilityRules = visibilityRules;
	}

	public List<FieldValidationType> getValidations() {
		return this.validations;
	}

	public void setValidations(List<FieldValidationType> validations) {
		this.validations = validations;
	}

	public Boolean getIncludeInExport() {
		return this.includeInExport;
	}

	public void setIncludeInExport(Boolean includeInExport) {
		this.includeInExport = includeInExport;
	}

	public BaseFieldData getData() {
		return this.data;
	}

	public void setData(BaseFieldData data) {
		this.data = data;
	}
}
