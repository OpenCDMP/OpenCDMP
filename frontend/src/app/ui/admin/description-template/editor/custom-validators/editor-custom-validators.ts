import { AbstractControl, FormGroup, UntypedFormArray, ValidationErrors, ValidatorFn } from "@angular/forms";
import { TemplateSectionForm } from "../description-template-editor.model";
import { PlanBlueprintRootSectionForm } from "@app/ui/admin/plan-blueprint/editor/plan-blueprint-editor.model";


export class EditorCustomValidators {

	static atLeastOneElementListValidator(arrayToCheck): ValidatorFn {
		return (control: AbstractControl): ValidationErrors | null => {

			const fa = control.get(arrayToCheck) as UntypedFormArray;

			if (!fa || fa.length === 0) {
				return { [EditorCustomValidatorsEnum.emptyArray]: true };
			}
			return null;
		}
	}
	
	// static pagesHaveAtLeastOneSection(pagesArrayName: string, sectionsArrayName: string): ValidatorFn {

	// 	return (control: AbstractControl): ValidationErrors | null => {

	// 		const pages = control.get(pagesArrayName) as UntypedFormArray;
	// 		const sections = control.get(sectionsArrayName) as UntypedFormArray;


	// 		const pageIdsArray = pages.controls.map(page => page.get('id').value);
	// 		const sectionsPageIds = sections.controls.map(section => section.get('page').value);

	// 		let invalidMessage = null;

	// 		pageIdsArray.forEach(pageId => {
	// 			if (!sectionsPageIds.includes(pageId)) {
	// 				invalidMessage = { [EditorCustomValidatorsEnum.atLeastOneSectionInPage]: true };
	// 			}
	// 		})

	// 		return invalidMessage;
	// 	}
	// }

	static sectionHasAtLeastOneChildOf(fieldsetsArrayName, sectionsArrayName): ValidatorFn {

		return (control: AbstractControl): ValidationErrors | null => {

			const fieldsets = control.get(fieldsetsArrayName) as UntypedFormArray;
			const sections = control.get(sectionsArrayName) as UntypedFormArray;


			if ((fieldsets && fieldsets.length) || (sections && sections.length)) {
				return null;
			}

			return { [EditorCustomValidatorsEnum.sectionMustHaveOneChild]: true };
		}
	}

    static sectionHasTemplatesOrChildOf(fieldsetsArrayName, sectionsArrayName): ValidatorFn {
        return (control: FormGroup<TemplateSectionForm | PlanBlueprintRootSectionForm>): ValidationErrors | null => {
            if(control.get('hasTemplates')?.value == true){
                return null;
            }
			const fieldsets = control.get(fieldsetsArrayName) as UntypedFormArray;
			const sections = control.get(sectionsArrayName) as UntypedFormArray;


			if ((fieldsets && fieldsets.length) || (sections && sections.length)) {
				return null;
			}

			return { [EditorCustomValidatorsEnum.sectionMustHaveOneChildOrTemplate]: true };
		}
    }

    // Mirrors RootSectionPersist: assets that authors can't extend need at least one listed template.
    static sectionTemplatesRequired(): ValidatorFn {
        return (control: FormGroup<PlanBlueprintRootSectionForm>): ValidationErrors | null => {
            if (control.get('hasTemplates')?.value !== true || control.get('canEditDescriptionTemplates')?.value !== false) {
                return null;
            }
            const templates = control.get('descriptionTemplates') as UntypedFormArray;
            return templates?.length ? null : { [EditorCustomValidatorsEnum.sectionTemplatesRequired]: true };
        }
    }
}



export enum EditorCustomValidatorsEnum {
	sectionMustHaveOneChildOrTemplate = "sectionMustHaveOneChildOrTemplate",
	sectionMustHaveOneChild = "sectionMustHaveOneChild",
	sectionTemplatesRequired = "sectionTemplatesRequired",
	atLeastOneSectionInPage = 'atLeastOneSectionInPage',
	emptyArray = "emptyArray"
}