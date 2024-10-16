package org.opencdmp.service.visibility;

import java.util.Objects;

public class FieldKey {
	private final String fieldId;
	private final Integer ordinal;
	private final int hashCode;


	public FieldKey(String fieldId, Integer ordinal) {
		this.fieldId = fieldId;
		this.ordinal = ordinal;
		hashCode = Objects.hash(this.fieldId, this.ordinal);
	}

	public Integer getOrdinal() {
		return ordinal;
	}

	public String getFieldId() {
		return fieldId;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FieldKey that = (FieldKey) o;
		return Objects.equals(ordinal, that.getOrdinal()) && Objects.equals(fieldId, that.getFieldId());
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}
}
