package org.opencdmp.elastic.data.nested;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import gr.cite.tools.elastic.ElasticConstants;

import java.util.UUID;

public class NestedTagElasticEntity {
	@Id
	@Field(value = NestedTagElasticEntity._id, type = FieldType.Keyword)
	private UUID id;
	public final static String _id = "id";

	@MultiField(mainField = @Field(value = NestedTagElasticEntity._label, type = FieldType.Text), otherFields = {
			@InnerField(suffix = ElasticConstants.SubFields.keyword, type = FieldType.Keyword)
	})
	private String label;
	public final static String _label = "label";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
