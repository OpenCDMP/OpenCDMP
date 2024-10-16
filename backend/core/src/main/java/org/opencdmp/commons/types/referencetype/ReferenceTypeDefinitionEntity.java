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
}
