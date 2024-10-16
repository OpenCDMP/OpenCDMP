package org.opencdmp.service.fieldsetexpander;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "field-set-expander")
public class FieldSetExpanderServiceProperties {
	private List<Mapping> mappings;

	public FieldSetExpanderServiceProperties() {
	}

	public List<Mapping> getMappings() {
		return this.mappings;
	}

	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}

	public static class Mapping {
		private List<String> fields;
		private String key;

		public Mapping() {
		}

		public List<String> getFields() {
			return this.fields;
		}

		public void setFields(List<String> fields) {
			this.fields = fields;
		}

		public String getKey() {
			return this.key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}
}

