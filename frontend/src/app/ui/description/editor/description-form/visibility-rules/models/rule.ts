import { BaseField, FieldRule } from "@app/core/model/description-template/description-template";

export class RuleWithTarget {
	source: string;
	target: string;
	textValue: string;
	dateValue: Date;
	booleanValue: boolean;
    numberValue: number;
	field: BaseField;

	public constructor(source: string, rule: FieldRule , fieldEntity: BaseField) {
		this.target = rule.target;
		this.source = source;
		this.field = fieldEntity;
		this.textValue = rule.textValue;
		this.dateValue = rule.dateValue;
		this.booleanValue = rule.booleanValue;
		this.numberValue = rule.numberValue;
	}
}
