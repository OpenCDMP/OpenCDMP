package org.opencdmp.commons.types.referencetype;

import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherBaseSourceConfigurationEntity;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherApiSourceConfigurationEntity;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherStaticOptionSourceConfigurationEntity;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceTypeDefinitionEntity {
	@XmlElementWrapper(name = "fields")
	@XmlElement(name = "field")
	private List<ReferenceTypeFieldEntity> fields;

	@XmlElementWrapper(name = "sources")
	@XmlElements({
			@XmlElement(name = ExternalFetcherSourceType.Names.API, type = ExternalFetcherApiSourceConfigurationEntity.class),
			@XmlElement(name = ExternalFetcherSourceType.Names.STATIC, type = ExternalFetcherStaticOptionSourceConfigurationEntity.class),
	})
	private List<ExternalFetcherBaseSourceConfigurationEntity> sources;

	@XmlElement(name = "labelTemplate")
	private String labelTemplate;

	@XmlElementWrapper(name = "referenceIdSemantics")
	@XmlElement(name = "semantic")
	private List<String> referenceIdSemantics;

	@XmlElementWrapper(name = "labelSemantics")
	@XmlElement(name = "semantic")
	private List<String> labelSemantics;

	@XmlElementWrapper(name = "descriptionSemantics")
	@XmlElement(name = "semantic")
	private List<String> descriptionSemantics;

	public List<ReferenceTypeFieldEntity> getFields() {
		return fields;
	}

	public void setFields(List<ReferenceTypeFieldEntity> fields) {
		this.fields = fields;
	}

	public List<ExternalFetcherBaseSourceConfigurationEntity> getSources() {
		return sources;
	}

	public void setSources(List<ExternalFetcherBaseSourceConfigurationEntity> sources) {
		this.sources = sources;
	}

	public String getLabelTemplate() {
		return labelTemplate;
	}

	public void setLabelTemplate(String labelTemplate) {
		this.labelTemplate = labelTemplate;
	}

	public List<String> getReferenceIdSemantics() {
		return referenceIdSemantics;
	}

	public void setReferenceIdSemantics(List<String> referenceIdSemantics) {
		this.referenceIdSemantics = referenceIdSemantics;
	}

	public List<String> getLabelSemantics() {
		return labelSemantics;
	}

	public void setLabelSemantics(List<String> labelSemantics) {
		this.labelSemantics = labelSemantics;
	}

	public List<String> getDescriptionSemantics() {
		return descriptionSemantics;
	}

	public void setDescriptionSemantics(List<String> descriptionSemantics) {
		this.descriptionSemantics = descriptionSemantics;
	}
}
