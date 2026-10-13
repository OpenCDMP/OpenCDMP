import { Pipe, PipeTransform } from "@angular/core";
import { Guid } from "@common/types/guid";
import { TranslateService } from "@ngx-translate/core";
import { Observable, map, of } from "rxjs";
import { nameof } from "ts-simple-nameof";
import { FieldType } from "../common/enum/field-type";
import { BaseField, FieldLabelAndMultiplicityData, FieldRadioBoxData, FieldReferenceTypeData, FieldSelectData } from "../model/description-template/description-template";
import { Description, PropertyDefinitionField, FieldPersist, DescriptionTag } from "../model/description/description";
import { StorageFile } from "../model/storage-file/storage-file";
import { DescriptionService } from "../services/description/description.service";
import { PlanService } from "../services/plan/plan.service";
import { StorageFileService } from "../services/storage-file/storage-file.service";
import { DateTimeFormatPipe } from "./date-time-format.pipe";
import { Plan, PlanProperties } from "../model/plan/plan";
import { SystemFieldType } from "../common/enum/system-field-type";
import { LanguageInfoService } from "../services/culture/language-info-service";
import { EnumUtils } from "../services/utilities/enum-utils.service";

@Pipe({ name: 'fieldValue' })
export class FieldValuePipe implements PipeTransform {

	constructor(
		private dateTimeFormatPipe: DateTimeFormatPipe,
		private planService: PlanService,
		private storageFileService: StorageFileService,
		private descriptionService: DescriptionService,
		private language: TranslateService,
        private languageInfoService: LanguageInfoService,
        private enumUtils: EnumUtils
    ) {
	}

	transform(controlValue: FieldPersist | PropertyDefinitionField, field: BaseField, entity?: Description | Plan): Observable<string> {
		if (field?.data?.fieldType) {
			switch (field.data.fieldType) {
				case FieldType.BOOLEAN_DECISION:
					return of(controlValue?.booleanValue ? this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.YES') : this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.NO'));
				case FieldType.CHECK_BOX:
					return of(controlValue?.booleanValue ? this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.YES') : this.language.instant('DESCRIPTION-TEMPLATE-EDITOR.STEPS.FORM.FIELD.DEFAULT-VALUES.BOOLEAN-DECISION.NO'));
				case FieldType.RADIO_BOX: {
					const data = <FieldRadioBoxData>field.data;
					if (data?.options) {
						return of(data.options?.find(option => option.value === controlValue?.textValue)?.label);
					}
					break;
				}
				case FieldType.DATE_PICKER:
					return of(this.dateTimeFormatPipe.transform(controlValue?.dateValue, 'dd/MM/yyyy'));
				case FieldType.FREE_TEXT:
					return of(controlValue?.textValue);
				case FieldType.SELECT: {
					const data = <FieldSelectData>field.data;
					if (data?.options && !data?.multipleSelect) {
						return of(data.options?.find(option => controlValue?.textValue == option.value)?.label);
					} else if (data?.options && data?.multipleSelect) {
						return of(data?.options?.filter(option => controlValue?.textListValue?.includes(option.value)).map(option => option.label).join(','));
					}
					break;
				}
				case FieldType.RICH_TEXT_AREA:
					if (controlValue?.textValue) {
						return of(controlValue?.textValue);//.replace(/&nbsp;/g, ' ').replace(/(\r\n|\n|\r| +(?= ))|\s\s+/gm, " ").replace(/<[^>]*>/g, ''));
					}
					break;
				case FieldType.TEXT_AREA:
					return of(controlValue?.textValue);
				case FieldType.REFERENCE_TYPES: {
					const data = <FieldReferenceTypeData>field.data;
					if (!data?.multipleSelect && (controlValue['reference'] || controlValue['references'])) {
						return of(controlValue['reference']?.label ?? controlValue['references']?.map((x) => x.label)?.join(', '));
					} else if (data?.multipleSelect && controlValue?.references) {
						return of(controlValue?.references.map(option => option.label).join(','));
					}
					break;
				}
				case FieldType.TAGS: {
					if (controlValue?.tags) {
						return of(controlValue?.tags?.map((x) => x.label ?? x)?.join(', '));
					}
					break;
				}
				case FieldType.INTERNAL_ENTRIES_PLANS: {
					const data = <FieldLabelAndMultiplicityData>field.data;
					if (!data?.multipleSelect && controlValue?.textValue && controlValue?.textValue.length > 0) {
						return this.planService.getSingle(Guid.parse(controlValue?.textValue), [nameof<Plan>(x => x.id), nameof<Plan>(x => x.label)])
                            .pipe(map((x) => x?.label));
					} else if (data?.multipleSelect && controlValue?.references && controlValue?.textListValue && controlValue?.textListValue.length > 0) {
						return this.planService.query(this.planService.buildAutocompleteLookup(null, null, controlValue?.textListValue.map(x => Guid.parse(x))))
                            .pipe(map(x => x.items?.map(y => y.label).join(',')));
					}
					break;
				}
				case FieldType.INTERNAL_ENTRIES_DESCRIPTIONS:
					const data = <FieldLabelAndMultiplicityData>field.data;
					if (!data?.multipleSelect && controlValue?.textValue && controlValue?.textValue.length > 0) {
						return this.descriptionService.getSingle(
                            Guid.parse(controlValue?.textValue), [nameof<Description>(x => x.id), nameof<Description>(x => x.label),])
                            .pipe(map(x => x?.label));
					} else if (data?.multipleSelect && controlValue?.references && controlValue?.textListValue && controlValue?.textListValue.length > 0) {
						return this.descriptionService.query(
                            this.descriptionService.buildAutocompleteLookup(null, null, controlValue?.textListValue.map(x => Guid.parse(x)))
                        ).pipe(map(x => x.items?.map(y => y.label).join(',')));
					}
					break;
				case FieldType.UPLOAD: {
					if (controlValue?.textValue && controlValue?.textValue.length > 0) {
						return this.storageFileService.getSingle(Guid.parse(controlValue.textValue), [
							nameof<StorageFile>(x => x.name)
						]).pipe(map(x => x.name));
					}
					break;
				}
				case FieldType.DATASET_IDENTIFIER:
				case FieldType.VALIDATION:
					if (controlValue?.externalIdentifier?.identifier) {
						return of(controlValue?.externalIdentifier?.identifier);
					}
					break;
                case FieldType.NUMBER:
                    return of(controlValue?.numberValue?.toString());
                case FieldType.SYSTEM:
                    switch(field.data?.['systemFieldType'] as SystemFieldType){
                        case SystemFieldType.PlanTitle:
                        case SystemFieldType.PlanDescriptionTitle:
                            return of(entity?.['label']);
                        case SystemFieldType.PlanDescription:
                        case SystemFieldType.PlanDescriptionDescription:
                            if (entity?.['description'] != null) {
                                return of(entity?.['description']);//.replace(/&nbsp;/g, ' ').replace(/(\r\n|\n|\r| +(?= ))|\s\s+/gm, " ").replace(/<[^>]*>/g, ''));
                            }
                            break;
                        case SystemFieldType.PlanLanguage:
                            const code = entity?.['language'];
                            if(code != null){
                                return of(this.languageInfoService.getLanguageInfoValues()?.find(x => x.code === code)?.name || code);
                            }
                            break;
                        case SystemFieldType.PlanContact:
                            return of((entity?.['properties'] as PlanProperties)?.contacts?.map((x) => `${x.firstName} ${x.lastName}`)?.join(', '))
                        case SystemFieldType.PlanAccessRights:
                            if(entity?.['accessType'] != null){
                                return of(this.enumUtils.toPlanAccessTypeString(entity['accessType']));
                            }
                            break;
                        case SystemFieldType.PlanDescriptionTag:
                            return of((entity?.['descriptionTags'] as DescriptionTag[])?.map(x => x.tag?.label)?.join(', '))
                    }
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
