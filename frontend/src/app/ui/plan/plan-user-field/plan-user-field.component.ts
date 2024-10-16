import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, Input, OnInit } from '@angular/core';
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

@Component({
	selector: 'app-plan-user-field-component',
	templateUrl: 'plan-user-field.component.html',
	styleUrls: ['./plan-user-field.component.scss']
})
export class PlanUserFieldComponent extends BaseComponent implements OnInit {

	@Input() form;
	@Input() validationErrorModel: ValidationErrorModel;
	@Input() label: string = null;
	@Input() required: boolean = false;
	@Input() placeholder: string;
	@Input() sections: PlanBlueprintDefinitionSection[] = null;
	@Input() initializeUsers: boolean = false;
	@Input() viewOnly: boolean = false;
	@Input() enableSorting: boolean = true;
	hoveredUser:number = -1;
	hasTemplatesSections: PlanBlueprintDefinitionSection[] = null;

	planUserTypeEnum = PlanUserType;
	planUserTypeEnumValues = this.enumUtils.getEnumValues<PlanUserType>(PlanUserType);
	planUserRoleEnumValues = this.enumUtils.getEnumValues<PlanUserRole>(PlanUserRole);
	planUserRoleEnum = PlanUserRole;

	multipleAutoCompleteSearchConfiguration: MultipleAutoCompleteConfiguration;


	constructor(
		public enumUtils: EnumUtils,
		public userService: UserService
	) { super(); }

	ngOnInit() {
		this.hasTemplatesSections = this.sections?.filter(x => x.hasTemplates) || null;
		if(this.initializeUsers) {
			this.addUser();
		}
	}

	addUser(): void {
		const userArray = this.form.get('users') as FormArray;
		const planUser: PlanUserEditorModel = new PlanUserEditorModel(this.validationErrorModel);
		userArray.push(planUser.buildForm({ rootPath: "users[" + userArray.length + "]." }));
	}

	removeUser(userIndex: number): void {
		(this.form.get('users') as FormArray).removeAt(userIndex);

		PlanEditorModel.reApplyUsersValidators(
			{
				formGroup: this.form,
				validationErrorModel: this.validationErrorModel,
			}
		);
		this.form.get('users').markAsDirty();
	}

	//
	//
	// Users
	//
	//

	dropUsers(event: CdkDragDrop<string[]>) {
		const usersFormArray = (this.form.get('users') as FormArray);

		moveItemInArray(usersFormArray.controls, event.previousIndex, event.currentIndex);
		usersFormArray.updateValueAndValidity();

		PlanEditorModel.reApplyUsersValidators(
			{
				formGroup: this.form,
				validationErrorModel: this.validationErrorModel
			}
		);
		this.form.get('users').markAsDirty();
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
			(this.form.get('users') as FormArray).at(userIndex).get('email').patchValue(null);
		} else {
			(this.form.get('users') as FormArray).at(userIndex).get('user').patchValue(null);
		}
	}
}
