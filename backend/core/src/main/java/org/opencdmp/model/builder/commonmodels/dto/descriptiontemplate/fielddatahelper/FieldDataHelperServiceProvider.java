package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import org.opencdmp.commonmodels.enums.FieldType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("dto.FieldDataHelperServiceProvider")
public class FieldDataHelperServiceProvider {

	@Autowired
	private ApplicationContext applicationContext;
	
	public FieldDataHelperService get(FieldType type){
		BaseFieldDataHelperService<?, ? > item = null;
		
		switch (type) {
			case INTERNAL_ENTRIES_DESCRIPTIONS:
			case INTERNAL_ENTRIES_PlANS: {
				item = this.applicationContext.getBean(LabelAndMultiplicityFieldDataHelperService.class);
				break;
			}
			case RADIO_BOX:  {
				item = this.applicationContext.getBean(RadioBoxFieldDataHelperService.class);
				break;
			}
			case RICH_TEXT_AREA:
			case DATE_PICKER: 
			case TEXT_AREA:  
			case FREE_TEXT:
			case TAGS:
			case DATASET_IDENTIFIER:
			case CHECK_BOX:
			case BOOLEAN_DECISION:
			case VALIDATION: {
				item = this.applicationContext.getBean(LabelFieldDataHelperService.class);
				break;
			} 
			case UPLOAD:  {
				item = this.applicationContext.getBean(UploadFieldDataHelperService.class);
				break;
			}
			case SELECT: {
				item = this.applicationContext.getBean(SelectFieldDataHelperService.class);
				break;
			}
			case REFERENCE_TYPES:{
				item = this.applicationContext.getBean(ReferenceTypeFieldDataHelperService.class);
				break;
			}
			default: throw new RuntimeException("unrecognized builder " + type.getValue());
		}
		item.setFieldType(type);
		return item;
	}
}
