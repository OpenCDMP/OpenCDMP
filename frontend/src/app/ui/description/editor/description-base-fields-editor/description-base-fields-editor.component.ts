import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionTemplateVersionStatus } from '@app/core/common/enum/description-template-version-status';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { DescriptionTemplate } from '@app/core/model/description-template/description-template';
import { Description } from '@app/core/model/description/description';
import { PlanDescriptionTemplate } from '@app/core/model/plan/plan';
import { DescriptionService } from '@app/core/services/description/description.service';
import { BaseComponent } from '@common/base/base.component';
import { takeUntil } from 'rxjs/operators';
import { DeprecatedDescriptionTemplateDialog } from './dialog-description-template/deprecated-description-template-dialog.component';

@Component({
	selector: 'app-description-base-fields-editor-component',
	templateUrl: 'description-base-fields-editor.component.html',
	styleUrls: ['./description-base-fields-editor.component.scss']
})
export class DescriptionBaseFieldsEditorComponent extends BaseComponent {

	@Input() formGroup: UntypedFormGroup;
	@Input() description: Description;
	availableDescriptionTemplates: DescriptionTemplate[] = [];

	@Output() refresh: EventEmitter<any> = new EventEmitter<any>();

	constructor(private dialog: MatDialog,
		private descriptionService: DescriptionService) {
		super();
	}

	ngOnInit() {
		this.loadDescriptionTemplates();
	}

	public compareWith(value1: string, value2: string): boolean {
		return value1 && value2 && value1 === value2;
	}

	private loadDescriptionTemplates(): void {
		const planDescriptionTemplates: PlanDescriptionTemplate[] = this.description.plan.planDescriptionTemplates.filter(x => x.sectionId == this.description.planDescriptionTemplate.sectionId && x.isActive == IsActive.Active);
		const currentVersionsOfDescriptionTemplates = planDescriptionTemplates.map(x => x.currentDescriptionTemplate);
		this.availableDescriptionTemplates.push(...currentVersionsOfDescriptionTemplates);

		if (this.description?.descriptionTemplate != null) {
			const isPreviousVersion: boolean = this.description.descriptionTemplate.versionStatus === DescriptionTemplateVersionStatus.Previous;
			if (isPreviousVersion === true) {
				if (this.description.status === DescriptionStatusEnum.Draft) {
					this.openDeprecatedDescriptionTemplateDialog();
				} else {
					this.availableDescriptionTemplates.push(this.description.descriptionTemplate);
				}
			}
		}
	}

	private openDeprecatedDescriptionTemplateDialog(): void {
		const dialogRef = this.dialog.open(DeprecatedDescriptionTemplateDialog, {
			data: {
				label: this.description.descriptionTemplate.label
			}
		});
		dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(
			result => {
				if (result) {
					this.descriptionService.updateDescriptionTemplate({
						id: this.description.id,
						hash: this.description.hash
					})
						.subscribe(
							result => {
								this.refresh.emit(result);
							},
							error => console.error(error));
				} else {
					this.availableDescriptionTemplates.push(this.description.descriptionTemplate);
				}
			});
	}
}
