import { Component, computed, HostBinding, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BaseComponent } from '@common/base/base.component';
import { UserService } from '@app/core/services/user/user.service';
import { SingleAutoCompleteConfiguration } from '@app/library/auto-complete/single/single-auto-complete-configuration';
import { map } from 'rxjs';
import { PlanAssociatedUser } from '@app/core/model/user/user';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';

@Component({
    selector: 'app-plan-contact-prefill-dialog',
    templateUrl: './plan-contact-prefill-dialog.component.html',
    styleUrls: ['./plan-contact-prefill-dialog.component.scss'],
    standalone: false
})
export class PlanContactPrefillDialogComponent extends BaseComponent {
   

	label: string;
	selectedUser: PlanAssociatedUser; 

	singleAutoCompletePlanAssociatedUserConfiguration: SingleAutoCompleteConfiguration;

	constructor(
		private userService: UserService,
		public enumUtils: EnumUtils,
		public dialogRef: MatDialogRef<PlanContactPrefillDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
	) {
		super();
		this.label = data.label;
	}

	ngOnInit(): void {
		this.singleAutoCompletePlanAssociatedUserConfiguration = {
			initialItems: (data?: any) => this.userService.queryPlanAssociated(this.userService.buildAutocompleteLookup()).pipe(map(x => x.items)),
			filterFn: (searchQuery: string, data?: any) => this.userService.queryPlanAssociated(this.userService.buildAutocompleteLookup(searchQuery)).pipe(map(x => x.items)),
			getSelectedItem: (selectedItem: any) => this.userService.queryPlanAssociated(this.userService.buildAutocompleteLookup(null, null, [selectedItem.id])).pipe(map(x => x.items[0])),
			displayFn: (item: PlanAssociatedUser) => item.name,
			subtitleFn: (item: PlanAssociatedUser) => item.email,
			titleFn: (item: PlanAssociatedUser) => item.name,
			valueAssign: (item: PlanAssociatedUser) => item,
			uniqueAssign: (item: PlanAssociatedUser) => item.id
		};
	}

	cancel() {
		this.dialogRef.close(false);
	}

	confirm() {
		this.dialogRef.close(this.selectedUser);	
	}
}
