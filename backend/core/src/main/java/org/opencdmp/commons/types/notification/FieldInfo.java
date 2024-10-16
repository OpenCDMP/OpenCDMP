package org.opencdmp.commons.types.notification;

public class FieldInfo {
	private String key;
	private DataType type;
	private String value;

	public FieldInfo(String key, DataType type, String value) {
		this.key = key;
		this.type = type;
		this.value = value;
	}

	public FieldInfo() {
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
