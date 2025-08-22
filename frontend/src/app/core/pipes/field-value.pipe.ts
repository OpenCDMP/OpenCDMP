import { Pipe, PipeTransform } from "@angular/core";
import { Guid } from "@common/types/guid";
import { TranslateService } from "@ngx-translate/core";
import { Observable, map, of, switchMap } from "rxjs";
import { nameof } from "ts-simple-nameof";
import { DescriptionTemplateFieldType } from "../common/enum/description-template-field-type";
import { DescriptionTemplateField, DescriptionTemplateLabelAndMultiplicityData, DescriptionTemplateRadioBoxData, DescriptionTemplateReferenceTypeData, DescriptionTemplateSelectData } from "../model/description-template/description-template";
import { Description, DescriptionFieldPersist } from "../model/description/description";
import { StorageFile } from "../model/storage-file/storage-file";
import { DescriptionService } from "../services/description/description.service";
import { PlanService } from "../services/plan/plan.service";
import { StorageFileService } from "../services/storage-file/storage-file.service";
import { DateTimeFormatPipe } from "./date-time-format.pipe";
import { Plan } from "../model/plan/plan";

@Pipe({
    name: 'fieldValue',
    standalone: false
})
export class FieldValuePipe implements PipeTransform {

	constructor(
		private dateTimeFormatPipe: DateTimeFormatPipe,
		private planService: PlanService,
		private storageFileService: StorageFileService,
		private descriptionService: DescriptionService,
		private language: TranslateService) {
	}

	transform(controlValue: DescriptionFieldPersist, field: DescriptionTemplateField): Observable<string> {
		if (field?.data?.fieldType && controlValue) {
			switch (field.data.fieldType) {
				case DescriptionTemplateFieldType.BOOLEAN_DECISION:
					return of(controlValue.booleanValue ? this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.YES') : this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.NO'));
				case DescriptionTemplateFieldType.CHECK_BOX:
					return of(controlValue.booleanValue ? this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.YES') : this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.NO'));
				case DescriptionTemplateFieldType.RADIO_BOX: {
					const data = <DescriptionTemplateRadioBoxData>field.data;
					if (data?.options) {
						return of(data.options?.find(option => option.value === controlValue.textValue)?.label);
					}
					break;
				}
				case DescriptionTemplateFieldType.DATE_PICKER:
					return of(this.dateTimeFormatPipe.transform(controlValue.dateValue, 'dd/MM/yyyy'));
				case DescriptionTemplateFieldType.FREE_TEXT:
					return of(controlValue.textValue);
				case DescriptionTemplateFieldType.SELECT: {
					const data = <DescriptionTemplateSelectData>field.data;
					if (data?.options && !data?.multipleSelect) {
						return of(data.options?.find(option => controlValue.textValue == option.value)?.label);
					} else if (data?.options && data?.multipleSelect) {
						return of(data?.options?.filter(option => controlValue.textListValue?.includes(option.value)).map(option => option.label).join(','));
					}
					break;
				}
				case DescriptionTemplateFieldType.RICH_TEXT_AREA:
					if (controlValue.textValue) {
						return of(controlValue.textValue.replace(/&nbsp;/g, ' ').replace(/(\r\n|\n|\r| +(?= ))|\s\s+/gm, " ").replace(/<[^>]*>/g, ''));
					}
					break;
				case DescriptionTemplateFieldType.TEXT_AREA:
					return of(controlValue.textValue);
				case DescriptionTemplateFieldType.REFERENCE_TYPES: {
					const data = <DescriptionTemplateReferenceTypeData>field.data;
					if (!data?.multipleSelect && controlValue.reference) {
						return of(controlValue.reference?.label);
					} else if (data?.multipleSelect && controlValue.references) {
						return of(controlValue.references.map(option => option.label).join(','));
					}
					break;
				}
				case DescriptionTemplateFieldType.TAGS: {
					if (controlValue.tags) {
						return of(controlValue.tags.join(', '));
					}
					break;
				}
				case DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS: {
					const data = <DescriptionTemplateLabelAndMultiplicityData>field.data;
					if (!data?.multipleSelect && controlValue.textValue && controlValue.textValue.length > 0) {
						return this.planService.getSingle(Guid.parse(controlValue.textValue), [nameof<Plan>(x => x.id), nameof<Plan>(x => x.label)])
                            .pipe(map((x) => x?.label));
					} else if (data?.multipleSelect && controlValue.references && controlValue.textListValue && controlValue.textListValue.length > 0) {
						return this.planService.query(this.planService.buildAutocompleteLookup(null, null, controlValue.textListValue.map(x => Guid.parse(x))))
                            .pipe(map(x => x.items?.map(y => y.label).join(',')));
					}
					break;
				}
				case DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
					const data = <DescriptionTemplateLabelAndMultiplicityData>field.data;
					if (!data?.multipleSelect && controlValue.textValue && controlValue.textValue.length > 0) {
						return this.descriptionService.getSingle(
                            Guid.parse(controlValue.textValue), [nameof<Description>(x => x.id), nameof<Description>(x => x.label),])
                            .pipe(map(x => x?.label));
					} else if (data?.multipleSelect && controlValue.references && controlValue.textListValue && controlValue.textListValue.length > 0) {
						return this.descriptionService.query(
                            this.descriptionService.buildAutocompleteLookup(null, null, controlValue.textListValue.map(x => Guid.parse(x)))
                        ).pipe(map(x => x.items?.map(y => y.label).join(',')));
					}
					break;
				case DescriptionTemplateFieldType.UPLOAD: {
					if (controlValue.textValue && controlValue.textValue.length > 0) {
						return this.storageFileService.getSingle(Guid.parse(controlValue.textValue), [
							nameof<StorageFile>(x => x.name)
						]).pipe(map(x => x.name));
					}
					break;
				}
				case DescriptionTemplateFieldType.DATASET_IDENTIFIER:
				case DescriptionTemplateFieldType.VALIDATION:
					if (controlValue.externalIdentifier?.identifier) {
						return of(controlValue.externalIdentifier?.identifier);
					}
					break;
				default:
					return of(null);
			}
		}
		return of(null);
	}

	public parseJson(value: any, field: string = 'name') {
		if (Array.isArray(value)) {
			return value.map(element => JSON.parse(element)[field]).join(',');
		} else {
			return JSON.parse(value)[field];
		}
	}
}
