package org.opencdmp.model.description;

public class ExternalIdentifier {

	private String identifier;
	public static final String _identifier = "identifier";

	private String type;
	public static final String _type = "type";

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
