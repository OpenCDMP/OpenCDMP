import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, computed, Input, OnInit } from '@angular/core';
import { FormArray } from '@angular/forms';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanUserType } from '@app/core/common/enum/plan-user-type';
import { PlanBlueprintDefinitionSection } from '@app/core/model/plan-blueprint/plan-blueprint';
import { UserService } from '@app/core/services/user/user.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { MultipleAutoCompleteConfiguration } from '@app/library/auto-complete/multiple/multiple-auto-complete-configuration';
import { BaseComponent } from '@common/base/base.component';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { PlanEditorModel, PlanUserEditorModel } from '../plan-editor-blueprint/plan-editor.model';
import { MatButtonToggleChange } from '@angular/material/button-toggle';
import { DragAndDropAccessibilityService } from '@app/core/services/accessibility/drag-and-drop-accessibility.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-plan-user-field-component',
    templateUrl: 'plan-user-field.component.html',
    styleUrls: ['./plan-user-field.component.scss'],
    standalone: false,
    providers: [DragAndDropAccessibilityService]
})
export class PlanUserFieldComponent extends BaseComponent implements OnInit {
	@Input() form: FormArray;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() label: string = null;
	@Input() required: boolean = false;
	@Input() placeholder: string;
	@Input() sections: PlanBlueprintDefinitionSection[] = null;
	@Input() initializeUsers: boolean = false;
	@Input() viewOnly: boolean = false;
	@Input() enableSorting: boolean = true;
	@Input() isActivePlan: boolean = true;
	hoveredUser:number = -1;
	hasTemplatesSections: PlanBlueprintDefinitionSection[] = null;

	planUserTypeEnum = PlanUserType;
	planUserTypeEnumValues = this.enumUtils.getEnumValues<PlanUserType>(PlanUserType);
	planUserRoleEnumValues = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
	planUserRoleEnum = PlanUserRole;

	multipleAutoCompleteSearchConfiguration: MultipleAutoCompleteConfiguration;

    reorderAssistiveText = computed(() => this.dragAndDropService.assistiveTextSignal());
    get reorderMode(){
        return this.enableSorting && this.dragAndDropService.reorderMode;
    }
	constructor(
		public enumUtils: EnumUtils,
		public userService: UserService,
        private language: TranslateService,
        private dragAndDropService: DragAndDropAccessibilityService
	) { super(); }

	ngOnInit() {
		this.hasTemplatesSections = this.sections?.filter(x => x.hasTemplates) || null;
		if(this.initializeUsers) {
			this.addUser();
		}
	}

	addUser(): void {
		const userArray = this.form as FormArray;
		const planUser: PlanUserEditorModel = new PlanUserEditorModel(this.validationErrorModel);
		        
		planUser.ordinal = null;
		userArray?.push(planUser.buildForm({ rootPath: "users[" + userArray.length + "]." }));
		}

	removeUser(userIndex: number): void {
		(this.form as FormArray).removeAt(userIndex);
		this.updateOrdinals();

		PlanEditorModel.reApplyUsersValidators(
			{
				usersFormArray: this.form as FormArray,
				validationErrorModel: this.validationErrorModel,
			}
		);
		this.form.markAsDirty();
	}

	//
	//
	// Users
	//
	//

	dropUsers(event: {previousIndex: number, currentIndex: number}) {
		const usersFormArray = (this.form as FormArray);

		moveItemInArray(usersFormArray.controls, event.previousIndex, event.currentIndex);
		usersFormArray.updateValueAndValidity();
		this.updateOrdinals();

		PlanEditorModel.reApplyUsersValidators(
			{
				usersFormArray: this.form as FormArray,
				validationErrorModel: this.validationErrorModel
			}
		);
		this.form.markAsDirty();
		this.updateOrdinals();
	}

	isUserSelected(userId: number): boolean {
		return this.hoveredUser === userId;
	}

	onUserHover(userIndex: number): void {
		if (this.enableSorting === false) return;

		this.hoveredUser = userIndex;
	}

	clearHoveredUser(): void {
		this.hoveredUser = -1;
	}

	userTypeChange(type: MatButtonToggleChange, userIndex: number){
		if (type.value === PlanUserType.Internal){
			(this.form as FormArray).at(userIndex).get('email').patchValue(null);
		} else {
			(this.form as FormArray).at(userIndex).get('user').patchValue(null);
		}
	}

    onDragKeyDown($event: KeyboardEvent, index: number){
        const usersFormArray = (this.form as FormArray);
        this.dragAndDropService.onKeyDown({
            $event,
            currentIndex: index,
            itemName: this.language.instant('PLAN-EDITOR.FIELDS.USER')  + ' ' + (index + 1),
            listLength: usersFormArray.length,
            moveDownFn:() => {
                this.dropUsers({
                    previousIndex: index,
                    currentIndex: index + 1
                });
                setTimeout(() => document.getElementById('drag-handle-' + (index + 1))?.focus());
            },
            moveUpFn: () => { 
                this.dropUsers({
                    previousIndex: index,
                    currentIndex: index - 1
                });
                setTimeout(() => document.getElementById('drag-handle-' + (index - 1))?.focus());
            }
        })
		this.updateOrdinals();
    }
	
	updateOrdinals(): void {
    const usersFormArray = this.form as FormArray;
    usersFormArray.controls.forEach((control, index) => {

	if (control.get('ordinal').value !== index) {
        control.get('ordinal').setValue(index);
        control.get('ordinal').updateValueAndValidity();
      }
    });
  }
}
