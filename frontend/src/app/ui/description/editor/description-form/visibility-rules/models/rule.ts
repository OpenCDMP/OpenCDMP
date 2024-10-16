import { DescriptionTemplateField, DescriptionTemplateRule } from "@app/core/model/description-template/description-template";

export class RuleWithTarget {
	source: string;
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
	field: DescriptionTemplateField;

	public constructor(source: string, rule: DescriptionTemplateRule , fieldEntity: DescriptionTemplateField) {
		this.target = rule.target;
		this.source = source;
		this.field = fieldEntity;
		this.textValue = rule.textValue;
		this.dateValue = rule.dateValue;
		this.booleanValue = rule.booleanValue;
	}
}
